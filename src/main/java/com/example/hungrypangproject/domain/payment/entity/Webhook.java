package com.example.hungrypangproject.domain.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Webhook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "webhook_id", nullable = false, unique = true, length = 100)
    private String webhookId;

    @Column(name = "payment_id", nullable = false, length = 100)
    private String paymentId;

    @Column(name = "event_status", nullable = false, length = 30)
    private String eventStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private WebhookStatus status;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    // 웹훅 수신 시 생성
    public static Webhook create(String webhookId, String paymentId, String eventStatus) {
        Webhook webhook = new Webhook();
        webhook.webhookId = webhookId;
        webhook.paymentId = paymentId;
        webhook.eventStatus = eventStatus;
        webhook.status = WebhookStatus.RECEIVED;
        webhook.receivedAt = LocalDateTime.now();
        return webhook;
    }

    // Webhook 처리 완료
    public void markAsProcessed() {
        this.status = WebhookStatus.PROCESSED;
    }

    // Webhook 처리 실패
    public void markAsFailed() {
        this.status = WebhookStatus.FAILED;
    }
}
