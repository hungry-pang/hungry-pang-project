package com.example.hungrypangproject.domain.refund.service;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.domain.order.service.OrderService;
import com.example.hungrypangproject.domain.payment.entity.Payment;
import com.example.hungrypangproject.domain.payment.repository.PaymentRepository;
import com.example.hungrypangproject.domain.refund.dto.RefundAllRequest;
import com.example.hungrypangproject.domain.refund.dto.RefundAllResponse;
import com.example.hungrypangproject.domain.refund.entity.Refund;
import com.example.hungrypangproject.domain.refund.exception.RefundException;
import com.example.hungrypangproject.domain.refund.repository.RefundRepository;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final RefundHistoryService refundHistoryService;
    private final IamportClient iamportClient;

    @Transactional
    public RefundAllResponse refundAll(String dbPaymentId, @Valid RefundAllRequest refundAllRequest) {
        // 1) 결제 조회 및 환불 가능 여부 검증
        Payment payment = paymentRepository.findByDbPaymentId(dbPaymentId)
                .orElseThrow(() -> new RefundException(ErrorCode.PAYMENT_NOT_FOUND));

        validateRefundable(payment);

        // 2) 환불 요청 이력 저장
        String refundGroupId = "rf-grp" + UUID.randomUUID();
        refundHistoryService.saveRequestHistory(payment.getId(), payment.getTotalAmount(), refundAllRequest.getReason(), refundGroupId);

        // 3) PortOne 환불 요청
        String portOneRefundId = requestPortOneRefund(payment, refundAllRequest);

        // 4) 환불 완료 처리
        completeRefund(payment, refundAllRequest.getReason(), portOneRefundId, refundGroupId);

        return new RefundAllResponse(payment.getOrder().getId(), payment.getOrder().getOrderNum());
    }

    private String requestPortOneRefund(Payment payment, RefundAllRequest refundAllRequest) {
        if (payment.getPaymentId() == null || payment.getPaymentId().isBlank()) {
            throw new RefundException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        try {
            CancelData cancelData = new CancelData(payment.getPaymentId(), true, payment.getTotalAmount());
            cancelData.setReason(refundAllRequest.getReason());

            IamportResponse<com.siot.IamportRestClient.response.Payment> response = iamportClient.cancelPaymentByImpUid(cancelData);
            if (response.getCode() != 0) {
                throw toRefundException(response.getCode(), response.getMessage());
            }

            com.siot.IamportRestClient.response.Payment portOnePayment = response.getResponse();
            return portOnePayment.getImpUid();
        } catch (IamportResponseException | IOException e) {
            throw toRefundException(-1, e.getMessage());
        }
    }

    private RefundException toRefundException(int responseCode, String responseMessage) {
        String message = (responseMessage == null || responseMessage.isBlank())
                ? ErrorCode.PORTONE_API_ERROR.getMessage()
                : responseMessage;

        if (responseCode == 1) {
            return new RefundException(ErrorCode.PAYMENT_NOT_FOUND, message);
        }

        return new RefundException(ErrorCode.PORTONE_API_ERROR, message);
    }

    // 환불 가능 상태 검증
    private void validateRefundable(Payment payment) {

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

    // 환불 완료 로직
    public void completeRefund(Payment payment, String reason, String portOneRefundId, String refundGroupId) {
        Refund completedRefund = Refund.createCompleted(
                payment.getId(),
                payment.getTotalAmount(),
                reason,
                portOneRefundId,
                refundGroupId
        );

        // 환불 완료 이력 저장
        refundRepository.save(completedRefund);

        // 결제 및 주문 상태 변경
        payment.refund();
        orderService.cancelOrder(payment.getOrder().getMember().getMemberId(), payment.getOrder().getId());
    }
}
