package com.example.hungrypangproject.domain.payment.service;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.domain.order.entity.Order;
import com.example.hungrypangproject.domain.order.entity.OrderStatus;
import com.example.hungrypangproject.domain.order.repository.OrderRepository;
import com.example.hungrypangproject.domain.payment.dto.PaymentPrepareRequest;
import com.example.hungrypangproject.domain.payment.dto.PaymentPrepareResponse;
import com.example.hungrypangproject.domain.payment.dto.PaymentVerifyRequest;
import com.example.hungrypangproject.domain.payment.dto.PaymentVerifyResponse;
import com.example.hungrypangproject.domain.payment.entity.Payment;
import com.example.hungrypangproject.domain.payment.entity.PaymentStatus;
import com.example.hungrypangproject.domain.payment.exception.PaymentException;
import com.example.hungrypangproject.domain.payment.repository.PaymentRepository;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final IamportClient iamportClient;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    /**
     * 결제 준비
     * 1. 주문 정보 조회 및 검증
     * 2. 결제 금액 검증
     * 3. 중복 결제 방지
     * 4. Payment 엔티티 생성 및 저장
     */
    @Transactional
    public PaymentPrepareResponse preparePayment(PaymentPrepareRequest request) {
        log.info("결제 준비 시작 - orderId: {}, amount: {}", request.getOrderId(), request.getAmount());

        // 1. 주문 조회
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new PaymentException(ErrorCode.ORDER_NOT_FOUND));

        // 2. 결제 금액 검증
        BigDecimal pointsToUse = request.getPointsToUse() != null ? request.getPointsToUse() : BigDecimal.ZERO;
        BigDecimal finalAmount = request.getAmount().subtract(pointsToUse);

        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            log.error("최종 결제 금액 음수 - finalAmount: {}", finalAmount);
            throw new PaymentException(ErrorCode.PAYMENT_INVALID_AMOUNT);
        }

        // 주문 금액과 요청 금액이 일치하는지 검증
        if (order.getTotalPrice().compareTo(request.getAmount()) != 0) {
            log.error("주문 금액 불일치 - 주문금액: {}, 요청금액: {}", order.getTotalPrice(), request.getAmount());
            throw new PaymentException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 3. 중복 결제 방지 - 해당 주문에 대해 PENDING 또는 PAID 상태의 결제가 있는지 확인
        boolean hasActivePayment = paymentRepository.existsByOrderAndStatusIn(
                order,
                java.util.List.of(PaymentStatus.PENDING, PaymentStatus.PAID)
        );

        if (hasActivePayment) {
            log.error("중복 결제 시도 - orderId: {}", request.getOrderId());
            throw new PaymentException(ErrorCode.PAYMENT_DUPLICATE);
        }

        // 4. 고유한 결제 ID 생성 (merchant_uid로 사용)
        String dbPaymentId = "PAY_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);

        // 5. Payment 엔티티 생성 및 저장
        Payment payment = Payment.builder()
                .dbPaymentId(dbPaymentId)
                .order(order)
                .totalAmount(request.getAmount())
                .pointsToUse(pointsToUse)
                .status(PaymentStatus.PENDING)
                .build();

        paymentRepository.save(payment);
        log.info("결제 준비 완료 - dbPaymentId: {}", dbPaymentId);

        // 6. 응답 생성
        return PaymentPrepareResponse.builder()
                .dbPaymentId(dbPaymentId)
                .orderId(order.getId())
                .orderName("주문번호: " + order.getOrderNum())
                .amount(request.getAmount())
                .pointsToUse(pointsToUse)
                .finalAmount(finalAmount)
                .buyerName(request.getBuyerName())
                .buyerTel(request.getBuyerTel())
                .buyerEmail(request.getBuyerEmail())
                .payMethod(request.getPayMethod())
                .build();
    }

    /**
     * 결제 검증
     * 1. DB에서 결제 정보 조회
     * 2. 멱등성 체크 (이미 처리된 결제인지)
     * 3. PortOne API로 실제 결제 정보 조회
     * 4. merchant_uid 일치 확인
     * 5. 금액 검증 (위변조 방지)
     * 6. 결제 상태 확인
     * 7. 결제 성공 처리 및 주문 상태 변경
     */
    @Transactional
    public PaymentVerifyResponse verifyPayment(PaymentVerifyRequest request) {
        log.info("결제 검증 시작 - impUid: {}, merchantUid: {}", request.getImpUid(), request.getMerchantUid());

        // 1. DB에서 결제 정보 조회 (merchant_uid 사용)
        Payment dbPayment = paymentRepository.findByDbPaymentId(request.getMerchantUid())
                .orElseThrow(() -> new PaymentException(ErrorCode.PAYMENT_NOT_FOUND));

        // 2. 멱등성 체크 (이미 처리된 결제인지)
        if (dbPayment.getStatus() == PaymentStatus.PAID) {
            log.info("이미 처리된 결제 - paymentId: {}", dbPayment.getId());
            return PaymentVerifyResponse.alreadyProcessed();
        }

        try {
            // 3. PortOne API로 실제 결제 정보 조회 (imp_uid 사용)
            IamportResponse<com.siot.IamportRestClient.response.Payment> portOneResponse =
                    iamportClient.paymentByImpUid(request.getImpUid());

            // 4. PortOne API 응답 확인
            if (portOneResponse.getCode() != 0) {
                log.error("PortOne API 조회 실패 - code: {}, message: {}",
                        portOneResponse.getCode(), portOneResponse.getMessage());
                dbPayment.failPayment();
                throw new PaymentException(ErrorCode.PORTONE_API_ERROR, portOneResponse.getMessage());
            }

            com.siot.IamportRestClient.response.Payment portOnePayment = portOneResponse.getResponse();

            // 5. merchant_uid 일치 확인 (위변조 방지)
            if (!portOnePayment.getMerchantUid().equals(dbPayment.getDbPaymentId())) {
                log.error("merchant_uid 불일치 - PortOne: {}, DB: {}",
                        portOnePayment.getMerchantUid(), dbPayment.getDbPaymentId());
                dbPayment.failPayment();
                throw new PaymentException(ErrorCode.PAYMENT_INFO_MISMATCH);
            }

            // 6. 금액 검증 (위변조 방지 - 가장 중요!)
            BigDecimal finalAmount = dbPayment.getTotalAmount().subtract(
                    dbPayment.getPointsToUse() != null ? dbPayment.getPointsToUse() : BigDecimal.ZERO
            );

            if (portOnePayment.getAmount().compareTo(finalAmount) != 0) {
                log.error("결제 금액 불일치 - PortOne: {}, DB: {}",
                        portOnePayment.getAmount(), finalAmount);
                dbPayment.failPayment();
                throw new PaymentException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
            }

            // 7. 결제 상태 확인
            if (!"paid".equals(portOnePayment.getStatus())) {
                log.error("결제 미완료 - status: {}", portOnePayment.getStatus());
                dbPayment.failPayment();
                throw new PaymentException(ErrorCode.PAYMENT_NOT_COMPLETED);
            }

            // 8. 결제 성공 처리
            dbPayment.completePayment(request.getImpUid());
            log.info("결제 성공 처리 완료 - paymentId: {}", dbPayment.getId());

            // 9. 주문 상태를 PREPARING(조리 준비)로 변경
            Order order = dbPayment.getOrder();
            order.updateStatus(OrderStatus.PREPARING);
            log.info("주문 상태 변경 완료 - orderId: {}, status: PREPARING", order.getId());

            // 10. 응답 생성
            return PaymentVerifyResponse.success(
                    dbPayment.getId(),
                    order.getId(),
                    request.getImpUid(),
                    finalAmount,
                    OrderStatus.PREPARING.getDescription()
            );

        } catch (IamportResponseException e) {
            log.error("PortOne API 호출 중 예외 발생", e);
            dbPayment.failPayment();
            throw new PaymentException(ErrorCode.PORTONE_API_ERROR, e.getMessage());
        } catch (IOException e) {
            log.error("PortOne API 통신 중 IO 예외 발생", e);
            dbPayment.failPayment();
            throw new PaymentException(ErrorCode.PORTONE_API_ERROR, "API 통신 중 오류가 발생했습니다.");
        }
    }
}
