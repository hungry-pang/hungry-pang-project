package com.example.hungrypangproject.domain.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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

    @Builder
    public Webhook(String webhookId, String paymentId, String eventStatus, WebhookStatus status, LocalDateTime receivedAt) {
        this.webhookId = webhookId;
        this.paymentId = paymentId;
        this.eventStatus = eventStatus;
        this.status = status != null ? status : WebhookStatus.RECEIVED;
        this.receivedAt = receivedAt != null ? receivedAt : LocalDateTime.now();
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
