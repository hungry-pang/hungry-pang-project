package com.example.hungrypangproject.domain.payment.service;

import com.example.hungrypangproject.domain.order.entity.Order;
import com.example.hungrypangproject.domain.order.repository.OrderRepository;
import com.example.hungrypangproject.domain.payment.dto.PaymentPrepareRequest;
import com.example.hungrypangproject.domain.payment.dto.PaymentPrepareResponse;
import com.example.hungrypangproject.domain.payment.entity.Payment;
import com.example.hungrypangproject.domain.payment.entity.PaymentStatus;
import com.example.hungrypangproject.domain.payment.repository.PaymentRepository;
import com.siot.IamportRestClient.IamportClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. orderId: " + request.getOrderId()));

        // 2. 결제 금액 검증
        BigDecimal pointsToUse = request.getPointsToUse() != null ? request.getPointsToUse() : BigDecimal.ZERO;
        BigDecimal finalAmount = request.getAmount().subtract(pointsToUse);

        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("최종 결제 금액은 0보다 작을 수 없습니다.");
        }

        // 주문 금액과 요청 금액이 일치하는지 검증
        if (order.getTotalPrice().compareTo(request.getAmount()) != 0) {
            log.error("주문 금액 불일치 - 주문금액: {}, 요청금액: {}", order.getTotalPrice(), request.getAmount());
            throw new IllegalArgumentException("주문 금액이 일치하지 않습니다.");
        }

        // 3. 중복 결제 방지 - 해당 주문에 대해 PENDING 또는 PAID 상태의 결제가 있는지 확인
        boolean hasActivePayment = paymentRepository.existsByOrderAndStatusIn(
                order,
                java.util.List.of(PaymentStatus.PENDING, PaymentStatus.PAID)
        );

        if (hasActivePayment) {
            log.error("중복 결제 시도 - orderId: {}", request.getOrderId());
            throw new IllegalStateException("이미 진행 중이거나 완료된 결제가 있습니다.");
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
}
