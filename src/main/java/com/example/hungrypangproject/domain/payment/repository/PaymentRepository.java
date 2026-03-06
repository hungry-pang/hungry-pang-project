package com.example.hungrypangproject.domain.payment.repository;

import com.example.hungrypangproject.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
