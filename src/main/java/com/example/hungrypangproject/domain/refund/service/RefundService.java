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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final RefundHistoryService refundHistoryService;
    private final ObjectMapper objectMapper;

    @Value("${portone.api.base-url:https://api.portone.io}")
    private String portOneBaseUrl;

    @Value("${portone.api.v2-secret:${portone.api.secret}}")
    private String portOneV2Secret;

    @Transactional
    public RefundAllResponse refundAll(Long memberId, String dbPaymentId, @Valid RefundAllRequest refundAllRequest) {
        // 1) 결제 조회 및 환불 가능 여부 검증
        Payment payment = paymentRepository.findByDbPaymentId(dbPaymentId)
                .orElseThrow(() -> new RefundException(ErrorCode.PAYMENT_NOT_FOUND));

        // 본인 결제인지 검증
        if (!payment.getOrder().getMember().getMemberId().equals(memberId)) {
            throw new RefundException(ErrorCode.ORDER_CANCEL_FORBIDDEN);
        }

        // 요청 주문 ID와 결제의 주문 ID가 일치하는지 검증
        if (!payment.getOrder().getId().equals(refundAllRequest.getOrderId())) {
            throw new RefundException(ErrorCode.PAYMENT_INFO_MISMATCH);
        }

        validateRefundable(payment);

        // 2) 환불 요청 이력 저장
        String refundGroupId = "rf-grp" + UUID.randomUUID();
        String reason = refundAllRequest.getReason();
        refundHistoryService.saveRequestHistory(payment.getId(), payment.getTotalAmount(), reason, refundGroupId);

        String portOneRefundId = null;

        try {
            // 3) PortOne 환불 요청
            portOneRefundId = requestPortOneRefund(payment, refundAllRequest);

            // 4) 환불 완료 처리
            completeRefund(payment, reason, portOneRefundId, refundGroupId);

            return new RefundAllResponse(payment.getOrder().getId(), payment.getOrder().getOrderNum());
        } catch (RefundException e) {
            saveFailHistorySafely(payment, reason, portOneRefundId, refundGroupId, e.getMessage());
            throw e;
        } catch (Exception e) {
            saveFailHistorySafely(payment, reason, portOneRefundId, refundGroupId, e.getMessage());
            throw new RefundException(ErrorCode.PORTONE_API_ERROR, e.getMessage());
        }
    }

    private String requestPortOneRefund(Payment payment, RefundAllRequest refundAllRequest) {
        // PortOne v2 환불 API: POST /payments/{paymentId}/cancel
        // paymentId = 결제창에 넘긴 paymentId = dbPaymentId
        if (payment.getDbPaymentId() == null || payment.getDbPaymentId().isBlank()) {
            throw new RefundException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        try {
            String requestUrl = portOneBaseUrl + "/payments/" + payment.getDbPaymentId() + "/cancel";
            String reason = (refundAllRequest.getReason() != null && !refundAllRequest.getReason().isBlank())
                    ? refundAllRequest.getReason() : "고객 요청";

            // 요청 바디 생성
            String requestBody = objectMapper.writeValueAsString(Map.of("reason", reason));

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(requestUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "PortOne " + portOneV2Secret)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                JsonNode errorNode = objectMapper.readTree(response.body());
                String message = errorNode.has("message") ? errorNode.get("message").asText() : "환불 요청 실패";
                throw new RefundException(ErrorCode.PORTONE_API_ERROR, message);
            }

            // cancellationId 추출 (환불 고유 ID)
            JsonNode responseNode = objectMapper.readTree(response.body());
            if (responseNode.has("cancellationId") && !responseNode.get("cancellationId").isNull()) {
                return responseNode.get("cancellationId").asText();
            }

            // cancellationId가 없으면 dbPaymentId + "_cancel" 을 대체값으로 사용
            return payment.getDbPaymentId() + "_cancel";

        } catch (Exception e) {
            throw new RefundException(ErrorCode.PORTONE_API_ERROR, e.getMessage());
        }
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
    private void completeRefund(Payment payment, String reason, String portOneRefundId, String refundGroupId) {
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
        orderService.refundOrder(payment.getOrder().getMember().getMemberId(), payment.getOrder().getId());
    }

    private void saveFailHistorySafely(Payment payment, String reason, String portOneRefundId, String refundGroupId, String failureMessage) {
        String failReason = reason + " | 실패원인: " + failureMessage;

        try {
            refundHistoryService.saveFailHistory(
                    payment.getId(),
                    payment.getTotalAmount(),
                    failReason,
                    portOneRefundId,
                    refundGroupId
            );
        } catch (Exception historyException) {
            log.error("환불 실패 이력 저장 중 예외 발생 - paymentId: {}, refundGroupId: {}",
                    payment.getId(), refundGroupId, historyException);
        }
    }
}
