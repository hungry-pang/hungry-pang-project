package com.example.hungrypangproject.domain.payment.service;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.domain.order.entity.Order;
import com.example.hungrypangproject.domain.order.entity.OrderStatus;
import com.example.hungrypangproject.domain.payment.entity.Payment;
import com.example.hungrypangproject.domain.payment.entity.PaymentStatus;
import com.example.hungrypangproject.domain.payment.exception.PaymentException;
import com.example.hungrypangproject.domain.payment.repository.PaymentRepository;
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
}
