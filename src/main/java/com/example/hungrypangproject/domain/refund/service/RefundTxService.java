package com.example.hungrypangproject.domain.refund.service;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.domain.order.service.OrderService;
import com.example.hungrypangproject.domain.payment.entity.Payment;
import com.example.hungrypangproject.domain.payment.repository.PaymentRepository;
import com.example.hungrypangproject.domain.refund.dto.RefundAllRequest;
import com.example.hungrypangproject.domain.refund.entity.Refund;
import com.example.hungrypangproject.domain.refund.exception.RefundException;
import com.example.hungrypangproject.domain.refund.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundTxService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final OrderService orderService;
    private final RefundHistoryService refundHistoryService;

    @Transactional
    public RefundContext reserveRefund(Long memberId, String dbPaymentId, RefundAllRequest refundAllRequest) {
        Payment payment = paymentRepository.findByDbPaymentIdWithLock(dbPaymentId)
                .orElseThrow(() -> new RefundException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.getOrder().getMember().getMemberId().equals(memberId)) {
            throw new RefundException(ErrorCode.ORDER_CANCEL_FORBIDDEN);
        }

        if (!payment.getOrder().getId().equals(refundAllRequest.getOrderId())) {
            throw new RefundException(ErrorCode.PAYMENT_INFO_MISMATCH);
        }

        validateRefundable(payment);
        payment.startRefund();

        String refundGroupId = "rf-grp" + UUID.randomUUID();
        String reason = refundAllRequest.getReason();
        refundHistoryService.saveRequestHistory(payment.getId(), payment.getTotalAmount(), reason, refundGroupId);

        return new RefundContext(
                payment.getId(),
                payment.getDbPaymentId(),
                payment.getTotalAmount(),
                payment.getOrder().getId(),
                payment.getOrder().getOrderNum(),
                reason,
                refundGroupId
        );
    }

    @Transactional
    public void completeRefundSuccess(RefundContext context, String portOneRefundId) {
        Payment payment = paymentRepository.findByDbPaymentIdWithLock(context.dbPaymentId())
                .orElseThrow(() -> new RefundException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.isRefund()) {
            log.info("이미 환불 완료된 건입니다 - dbPaymentId: {}", context.dbPaymentId());
            return;
        }

        Refund completedRefund = Refund.createCompleted(
                payment.getId(),
                payment.getTotalAmount(),
                context.reason(),
                portOneRefundId,
                context.refundGroupId()
        );

        refundRepository.save(completedRefund);
        payment.refund();
        orderService.refundOrder(payment.getOrder().getMember().getMemberId(), payment.getOrder().getId());
    }

    @Transactional
    public void completeRefundFailure(RefundContext context, String portOneRefundId, String failureMessage, boolean retryable) {
        Payment payment = paymentRepository.findByDbPaymentIdWithLock(context.dbPaymentId())
                .orElseThrow(() -> new RefundException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.isRefund()) {
            log.info("이미 환불 완료 상태라 실패 복구를 건너뜁니다 - dbPaymentId: {}", context.dbPaymentId());
            return;
        }

        if (!retryable && payment.isRefunding()) {
            payment.restorePaid();
            log.warn("확정 실패로 결제 상태를 PAID 로 복구합니다 - dbPaymentId: {}", context.dbPaymentId());
        }

        refundHistoryService.saveFailHistory(
                context.paymentId(),
                context.refundAmount(),
                context.reason() + " | 실패원인: " + failureMessage,
                portOneRefundId,
                context.refundGroupId()
        );
    }

    // 환불 가능 상태 검증
    private void validateRefundable(Payment payment) {
        if (payment.isRefunding()) {
            throw new RefundException(ErrorCode.LOCK_IN_PROGRESS);
        }
        if (payment.getOrder().isCancelled() || payment.getOrder().isRefunded()) {
            throw new RefundException(ErrorCode.ORDER_ALREADY_CANCELLED);
        }
        if (payment.getOrder().isCompleted()) {
            throw new RefundException(ErrorCode.ORDER_ALREADY_COMPLETED);
        }
        if (!payment.getOrder().isRefunable()) {
            throw new RefundException(ErrorCode.ORDER_STATUS_INVALID);
        }
        if (!payment.isPaid()) {
            throw new RefundException(ErrorCode.ORDER_STATUS_INVALID);
        }
    }
}


