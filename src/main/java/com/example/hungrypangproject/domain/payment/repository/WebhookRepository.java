package com.example.hungrypangproject.domain.payment.repository;

import com.example.hungrypangproject.domain.payment.entity.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookRepository extends JpaRepository<Webhook, Long> {
}
