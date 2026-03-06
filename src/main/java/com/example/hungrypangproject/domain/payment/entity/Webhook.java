package com.example.hungrypangproject.domain.payment.entity;

import com.example.hungrypangproject.domain.payment.consts.WebhookStatus;
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

    private String webhookId;

    private String paymentId;
    private String eventStatus;

    @Enumerated(EnumType.STRING)
    private WebhookStatus status;

    private LocalDateTime receivedAt;

    public Webhook(String webhookId, String paymentId, String eventStatus, WebhookStatus status, LocalDateTime receivedAt) {
        this.webhookId = webhookId;
        this.paymentId = paymentId;
        this.eventStatus = eventStatus;
        this.status = WebhookStatus.RECEIVED;
        this.receivedAt = receivedAt;

    }
}
