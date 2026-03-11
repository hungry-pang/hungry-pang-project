package com.example.hungrypangproject.domain.payment.repository;

import com.example.hungrypangproject.domain.payment.entity.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WebhookRepository extends JpaRepository<Webhook, Long> {

    /**
     * imp_uid로 웹훅 조회 (중복 웹훅 체크용)
     */
    Optional<Webhook> findByWebhookId(String webhookId);

    /**
     * imp_uid + eventStatus 조합으로 웹훅 존재 여부 확인
     * 같은 imp_uid라도 다른 상태(paid, cancelled 등)의 웹훅은 별도로 처리
     */
    boolean existsByWebhookIdAndEventStatus(String webhookId, String eventStatus);
}
