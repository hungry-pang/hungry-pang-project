package com.example.hungrypangproject.domain.payment.repository;

import com.example.hungrypangproject.domain.order.entity.Order;
import com.example.hungrypangproject.domain.payment.entity.Payment;
import com.example.hungrypangproject.domain.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // 주문과 상태로 결제 존재 여부 확인 (중복 결제 방지)
    boolean existsByOrderAndStatusIn(Order order, List<PaymentStatus> statuses);

    // dbPaymentId로 결제 조회
    Optional<Payment> findByDbPaymentId(String dbPaymentId);
}
