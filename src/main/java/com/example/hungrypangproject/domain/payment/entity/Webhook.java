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

    private String webhookId;

    private String paymentId;
    private String eventStatus;

    @Enumerated(EnumType.STRING)
    private WebhookStatus status;

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
