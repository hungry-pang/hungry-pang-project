package com.example.hungrypangproject.domain.refund.repository;

import com.example.hungrypangproject.domain.refund.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<Refund, Long> {
}
