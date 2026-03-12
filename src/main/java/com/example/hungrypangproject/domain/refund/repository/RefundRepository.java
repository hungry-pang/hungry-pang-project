package com.example.hungrypangproject.domain.refund.repository;

import com.example.hungrypangproject.domain.payment.entity.Payment;
import com.example.hungrypangproject.domain.refund.entity.Refund;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.dbPaymentId = :dbPaymentId")
    Optional<Payment> findByDbPaymentIdWithLock(@Param("dbPaymentId") String dbPaymentId);
}
