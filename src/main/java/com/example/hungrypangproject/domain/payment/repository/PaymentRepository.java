package com.example.hungrypangproject.domain.payment.repository;

import com.example.hungrypangproject.domain.order.entity.Order;
import com.example.hungrypangproject.domain.payment.entity.Payment;
import com.example.hungrypangproject.domain.payment.entity.PaymentStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // 주문과 상태로 결제 존재 여부 확인 (중복 결제 방지)
    boolean existsByOrderAndStatusIn(Order order, List<PaymentStatus> statuses);

    // dbPaymentId로 결제 조회
    Optional<Payment> findByDbPaymentId(String dbPaymentId);

    // 상세조회용: 회원 본인 결제만 조회
    Optional<Payment> findByDbPaymentIdAndOrderMemberMemberId(String dbPaymentId, Long memberId);

    // 비관적 락 조회 메서드
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")}) // 3초 대기 후 타임아웃
    @Query("SELECT p FROM Payment p WHERE p.dbPaymentId = :dbPaymentId")
    Optional<Payment> findByDbPaymentIdWithLock(@Param("dbPaymentId") String dbPaymentId);
}
