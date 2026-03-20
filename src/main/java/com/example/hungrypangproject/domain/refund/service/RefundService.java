package com.example.hungrypangproject.domain.refund.service;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.domain.refund.dto.RefundAllRequest;
import com.example.hungrypangproject.domain.refund.dto.RefundAllResponse;
import com.example.hungrypangproject.domain.refund.exception.RefundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundService {

    private final ObjectMapper objectMapper;
    private final RefundTxService refundTxService;

    @Value("${portone.api.base-url:https://api.portone.io}")
    private String portOneBaseUrl;

    @Value("${portone.api.v2-secret:${portone.api.secret}}")
    private String portOneV2Secret;

    public RefundAllResponse refundAll(Long memberId, String dbPaymentId, @Valid RefundAllRequest refundAllRequest) {
        RefundContext context = refundTxService.reserveRefund(memberId, dbPaymentId, refundAllRequest);

        String portOneRefundId = null;

        try {
            // 트랜잭션 밖에서 PortOne 환불 요청
            portOneRefundId = requestPortOneRefund(context.dbPaymentId(), context.reason());

            refundTxService.completeRefundSuccess(context, portOneRefundId);

            return new RefundAllResponse(context.orderId(), context.orderNum());
        } catch (RefundException e) {
            saveFailHistorySafely(context, portOneRefundId, e.getMessage(), e.isRetryable());
            throw e;
        } catch (IOException e) {
            log.error("PortOne v2 환불 응답 파싱 중 예외 발생", e);
            saveFailHistorySafely(context, portOneRefundId, "API 응답 처리 중 오류가 발생했습니다.", true);
            throw new RefundException(ErrorCode.PORTONE_API_ERROR, "API 응답 처리 중 오류가 발생했습니다.", true);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("PortOne v2 환불 호출 중 인터럽트 발생", e);
            saveFailHistorySafely(context, portOneRefundId, "API 통신 중 인터럽트가 발생했습니다.", true);
            throw new RefundException(ErrorCode.PORTONE_API_ERROR, "API 통신 중 인터럽트가 발생했습니다.", true);
        } catch (Exception e) {
            saveFailHistorySafely(context, portOneRefundId, e.getMessage(), true);
            throw new RefundException(ErrorCode.PORTONE_API_ERROR, e.getMessage(), true);
        }
    }

    private String requestPortOneRefund(String dbPaymentId, String reason) throws IOException, InterruptedException {
        // PortOne v2 환불 API: POST /payments/{paymentId}/cancel
        // paymentId = 결제창에 넘긴 paymentId = dbPaymentId
        if (dbPaymentId == null || dbPaymentId.isBlank()) {
            throw new RefundException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        String requestUrl = portOneBaseUrl + "/payments/" + dbPaymentId + "/cancel";
        String refundReason = (reason != null && !reason.isBlank()) ? reason : "고객 요청";

        // 요청 바디 생성
        String requestBody = objectMapper.writeValueAsString(Map.of("reason", refundReason));

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
            throw buildRefundException(response);
        }

        // cancellationId 추출 (환불 고유 ID)
        JsonNode responseNode = objectMapper.readTree(response.body());
        if (responseNode.has("cancellationId") && !responseNode.get("cancellationId").isNull()) {
            return responseNode.get("cancellationId").asText();
        }

        // cancellationId가 없으면 dbPaymentId + "_cancel" 을 대체값으로 사용
        return dbPaymentId + "_cancel";
    }

    private RefundException buildRefundException(HttpResponse<String> response) throws IOException {
        String message = "환불 요청 실패";

        if (response.body() != null && !response.body().isBlank()) {
            JsonNode errorNode = objectMapper.readTree(response.body());
            if (errorNode.has("message") && !errorNode.get("message").isNull()) {
                message = errorNode.get("message").asText();
            }
        }

        boolean retryable = response.statusCode() == 429 || response.statusCode() >= 500;
        return new RefundException(ErrorCode.PORTONE_API_ERROR, message, retryable);
    }

    private void saveFailHistorySafely(RefundContext context, String portOneRefundId, String failureMessage, boolean retryable) {
        try {
            refundTxService.completeRefundFailure(context, portOneRefundId, failureMessage, retryable);
        } catch (Exception historyException) {
            log.error("환불 실패 이력 저장 중 예외 발생 - paymentId: {}, refundGroupId: {}",
                    context.paymentId(), context.refundGroupId(), historyException);
        }
    }
}
