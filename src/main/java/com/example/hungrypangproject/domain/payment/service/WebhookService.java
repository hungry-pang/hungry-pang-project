package com.example.hungrypangproject.domain.payment.service;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.domain.payment.dto.WebhookRequest;
import com.example.hungrypangproject.domain.payment.entity.Webhook;
import com.example.hungrypangproject.domain.payment.entity.WebhookStatus;
import com.example.hungrypangproject.domain.payment.exception.PaymentException;
import com.example.hungrypangproject.domain.payment.repository.WebhookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 웹훅 기록 관리 서비스
 *
 * PaymentService와 별도의 클래스로 분리하여
 * REQUIRES_NEW 트랜잭션 전파가 정상 작동하도록 보장
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final WebhookRepository webhookRepository;

    /**
     * 중복 웹훅 체크 (imp_uid 단일 기준)
     *
     * @param impUid 포트원 결제 고유번호
     * @return 중복 여부
     */
    public boolean isDuplicateWebhook(String impUid) {
        return webhookRepository.existsByWebhookId(impUid);
    }

    /**
     * 웹훅 기록 저장 (별도 트랜잭션)
     *
     * REQUIRES_NEW를 사용하여 부모 트랜잭션과 독립적으로 실행
     * 예외 발생 시에도 웹훅 기록이 DB에 남도록 보장
     *
     * @param request 웹훅 요청
     * @return 저장된 웹훅 ID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long saveWebhookRecord(WebhookRequest request) {
        Webhook webhook = Webhook.builder()
                .webhookId(request.getImp_uid())
                .paymentId(request.getMerchant_uid())
                .eventStatus(request.getStatus())
                .status(WebhookStatus.RECEIVED)
                .build();

                        Webhook savedWebhook = webhookRepository.save(webhook);
        log.info("웹훅 기록 저장 완료 - webhookId: {}, impUid: {}, status: {}",
                savedWebhook.getId(), request.getImp_uid(), request.getStatus());

        return savedWebhook.getId();
    }

    /**
     * 웹훅 처리 완료 표시 (별도 트랜잭션)
     *
     * @param webhookId 웹훅 ID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markWebhookAsProcessed(Long webhookId) {
        Webhook webhook = webhookRepository.findById(webhookId)
                .orElseThrow(() -> new PaymentException(ErrorCode.PAYMENT_NOT_FOUND));
        webhook.markAsProcessed();
        webhookRepository.save(webhook);
        log.info("웹훅 처리 완료 표시 - webhookId: {}", webhookId);
    }

    /**
     * 웹훅 처리 실패 표시 (별도 트랜잭션)
     *
     * @param webhookId 웹훅 ID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markWebhookAsFailed(Long webhookId) {
        Webhook webhook = webhookRepository.findById(webhookId)
                .orElseThrow(() -> new PaymentException(ErrorCode.PAYMENT_NOT_FOUND));
        webhook.markAsFailed();
        webhookRepository.save(webhook);
        log.info("웹훅 처리 실패 표시 - webhookId: {}", webhookId);
    }
}
