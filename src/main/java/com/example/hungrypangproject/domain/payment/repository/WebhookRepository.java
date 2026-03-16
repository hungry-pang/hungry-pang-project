package com.example.hungrypangproject.domain.payment.repository;

import com.example.hungrypangproject.domain.payment.entity.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookRepository extends JpaRepository<Webhook, Long> {


    /**
     * imp_uid(webhookId) 단일 기준으로 웹훅 존재 여부 확인
     */
    boolean existsByWebhookId(String webhookId);
}
