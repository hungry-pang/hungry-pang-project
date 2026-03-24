package com.example.hungrypangproject.domain.payment.service;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.domain.order.entity.Order;
import com.example.hungrypangproject.domain.order.entity.OrderStatus;
import com.example.hungrypangproject.domain.payment.entity.Payment;
import com.example.hungrypangproject.domain.payment.entity.PaymentStatus;
import com.example.hungrypangproject.domain.payment.exception.PaymentException;
import com.example.hungrypangproject.domain.payment.repository.PaymentRepository;
import com.example.hungrypangproject.domain.payment.dto.PaymentVerifyRequest;
import com.example.hungrypangproject.domain.payment.dto.PaymentVerifyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentInnerService {

    private final PaymentRepository paymentRepository;
    private final WebhookService webhookService;

    @Transactional
    public PaymentVerifyContext reserveForVerify(Long memberId, PaymentVerifyRequest request) {
        Payment payment = paymentRepository.findByDbPaymentIdWithLock(request.getDbPaymentId())
                .orElseThrow(() -> new PaymentException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.getOrder().getMember().getMemberId().equals(memberId)) {
            throw new PaymentException(ErrorCode.ORDER_CANCEL_FORBIDDEN);
        }

        BigDecimal finalAmount = calculateFinalAmount(payment);

        if (payment.isPaid()) {
            log.info("이미 결제 완료된 건입니다 - dbPaymentId: {}", request.getDbPaymentId());
            return PaymentVerifyContext.alreadyProcessed(payment.getDbPaymentId(), finalAmount);
        }

        if (payment.isVerifying() || payment.isRefunding()) {
            log.warn("이미 처리 중인 결제입니다 - dbPaymentId: {}, status: {}", request.getDbPaymentId(), payment.getStatus());
            throw new PaymentException(ErrorCode.LOCK_IN_PROGRESS);
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.warn("결제 검증을 시작할 수 없는 상태입니다 - dbPaymentId: {}, status: {}", request.getDbPaymentId(), payment.getStatus());
            throw new PaymentException(ErrorCode.PAYMENT_ALREADY_PROCESSED);
        }

        payment.startVerification();
        return PaymentVerifyContext.of(payment.getDbPaymentId(), finalAmount);
    }

    @Transactional
    public PaymentVerifyResponse completeVerifySuccess(PaymentVerifyContext context, String paymentId) {
        Payment payment = paymentRepository.findByDbPaymentIdWithLock(context.dbPaymentId())
                .orElseThrow(() -> new PaymentException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.isPaid()) {
            log.info("다른 요청에서 이미 결제 완료 처리됨 - dbPaymentId: {}", context.dbPaymentId());
            return buildSuccessResponse(payment);
        }

        payment.completePayment(paymentId);
        payment.getOrder().updateStatus(OrderStatus.PREPARING);

        log.info("결제 성공 처리 완료 - paymentId: {}", payment.getId());
        return buildSuccessResponse(payment);
    }

    @Transactional
    public void completeVerifyFailure(String dbPaymentId, boolean retryable) {
        Payment payment = paymentRepository.findByDbPaymentIdWithLock(dbPaymentId)
                .orElseThrow(() -> new PaymentException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.isPaid()) {
            log.info("이미 결제 완료 상태라 실패 복구를 건너뜁니다 - dbPaymentId: {}", dbPaymentId);
            return;
        }

        if (!payment.isVerifying()) {
            log.info("결제 검증 중 상태가 아니라 실패 복구를 건너뜁니다 - dbPaymentId: {}, status: {}", dbPaymentId, payment.getStatus());
            return;
        }

        if (retryable) {
            payment.restorePending();
            log.warn("일시적 장애로 결제 상태를 PENDING 으로 복구합니다 - dbPaymentId: {}", dbPaymentId);
            return;
        }

        payment.failPayment();
        log.warn("비즈니스 검증 실패로 결제 상태를 FAIL 로 변경합니다 - dbPaymentId: {}", dbPaymentId);
    }


    /**
     * PortOne 결제 응답과 DB 결제 데이터를 비교해 최종 상태를 반영한다.
     */
    @Transactional
    public void processPaymentData(Long webhookId,
                                    com.siot.IamportRestClient.response.Payment portOnePayment,
                                    String impUid) {
        // 1. DB에서 결제 정보 조회
        Payment dbPayment = paymentRepository.findByDbPaymentIdWithLock(portOnePayment.getMerchantUid())
                .orElseThrow(() -> {
                    webhookService.markWebhookAsFailed(webhookId);
                    return new PaymentException(ErrorCode.PAYMENT_NOT_FOUND);
                });

        // 2. merchant_uid 일치 확인 (위변조 방지)
        if (!portOnePayment.getMerchantUid().equals(dbPayment.getDbPaymentId())) {
            log.error("merchant_uid 불일치 - PortOne: {}, DB: {}",
                    portOnePayment.getMerchantUid(), dbPayment.getDbPaymentId());
            webhookService.markWebhookAsFailed(webhookId);
            throw new PaymentException(ErrorCode.PAYMENT_INFO_MISMATCH);
        }

        // 3. 금액 검증 (위변조 방지)
        BigDecimal finalAmount = dbPayment.getTotalAmount().subtract(
                dbPayment.getPointsToUse() != null ? dbPayment.getPointsToUse() : BigDecimal.ZERO
        );

        if (portOnePayment.getAmount().compareTo(finalAmount) != 0) {
            log.error("결제 금액 불일치 - PortOne: {}, DB: {}",
                    portOnePayment.getAmount(), finalAmount);
            webhookService.markWebhookAsFailed(webhookId);
            throw new PaymentException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 4. 결제 상태에 따른 처리
        Order order = dbPayment.getOrder();
        String portOneStatus = portOnePayment.getStatus();

        switch (portOneStatus) {
            case "paid":
                // 4-1. 멱등성 체크 (이미 처리된 결제인지)
                if (dbPayment.getStatus() == PaymentStatus.PAID) {
                    log.info("이미 처리된 결제 - paymentId: {}", dbPayment.getId());
                    return;
                }

                // 결제 성공 처리
                dbPayment.completePayment(impUid);
                log.info("결제 성공 처리 완료 - paymentId: {}", dbPayment.getId());

                // 주문 상태를 PREPARING(조리 준비)로 변경
                order.updateStatus(OrderStatus.PREPARING);
                log.info("주문 상태 변경 완료 - orderId: {}, status: PREPARING", order.getId());
                break;

            case "failed":
                // 결제 실패 처리
                dbPayment.failPayment();
                log.info("결제 실패 처리 완료 - paymentId: {}", dbPayment.getId());

                // 주문 상태는 WATING 유지 (다시 결제 시도 가능)
                break;

            case "cancelled":
                // 결제 취소 처리
                dbPayment.failPayment();
                log.info("결제 취소 처리 완료 - paymentId: {}", dbPayment.getId());

                // 주문 상태를 CANCELLED로 변경
                order.updateStatus(OrderStatus.CANCELLED);
                log.info("주문 상태 변경 완료 - orderId: {}, status: CANCELLED", order.getId());
                break;

            default:
                log.error("알 수 없는 결제 상태 - status: {}", portOneStatus);
                webhookService.markWebhookAsFailed(webhookId);
                throw new PaymentException(ErrorCode.WEBHOOK_INVALID_STATUS);
        }
    }

    private BigDecimal calculateFinalAmount(Payment payment) {
        return payment.getTotalAmount().subtract(
                payment.getPointsToUse() != null ? payment.getPointsToUse() : BigDecimal.ZERO
        );
    }

    private PaymentVerifyResponse buildSuccessResponse(Payment payment) {
        return PaymentVerifyResponse.success(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getPaymentId(),
                calculateFinalAmount(payment),
                payment.getOrder().getOrderStatus().getDescription()
        );
    }
}
