package com.example.hungrypangproject.domain.payment.service;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.domain.order.entity.Order;
import com.example.hungrypangproject.domain.order.entity.OrderStatus;
import com.example.hungrypangproject.domain.order.repository.OrderRepository;
import com.example.hungrypangproject.domain.payment.dto.PaymentPrepareRequest;
import com.example.hungrypangproject.domain.payment.dto.PaymentPrepareResponse;
import com.example.hungrypangproject.domain.payment.dto.PaymentVerifyRequest;
import com.example.hungrypangproject.domain.payment.dto.PaymentVerifyResponse;
import com.example.hungrypangproject.domain.payment.dto.WebhookRequest;
import com.example.hungrypangproject.domain.payment.entity.Payment;
import com.example.hungrypangproject.domain.payment.entity.PaymentStatus;
import com.example.hungrypangproject.domain.payment.entity.Webhook;
import com.example.hungrypangproject.domain.payment.entity.WebhookStatus;
import com.example.hungrypangproject.domain.payment.exception.PaymentException;
import com.example.hungrypangproject.domain.payment.repository.PaymentRepository;
import com.example.hungrypangproject.domain.payment.repository.WebhookRepository;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final IamportClient iamportClient;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final WebhookService webhookService;

    /**
     * 결제 준비
     * 1. 주문 정보 조회 및 검증
     * 2. 결제 금액 검증
     * 3. 중복 결제 방지
     * 4. Payment 엔티티 생성 및 저장
     */
    @Transactional
    public PaymentPrepareResponse preparePayment(PaymentPrepareRequest request) {
        log.info("결제 준비 시작 - orderId: {}, amount: {}", request.getOrderId(), request.getAmount());

        // 1. 주문 조회
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new PaymentException(ErrorCode.ORDER_NOT_FOUND));

        // 2. 결제 금액 검증
        BigDecimal pointsToUse = request.getPointsToUse() != null ? request.getPointsToUse() : BigDecimal.ZERO;
        BigDecimal finalAmount = request.getAmount().subtract(pointsToUse);

        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            log.error("최종 결제 금액 음수 - finalAmount: {}", finalAmount);
            throw new PaymentException(ErrorCode.PAYMENT_INVALID_AMOUNT);
        }

        // 주문 금액과 요청 금액이 일치하는지 검증
        if (order.getTotalPrice().compareTo(request.getAmount()) != 0) {
            log.error("주문 금액 불일치 - 주문금액: {}, 요청금액: {}", order.getTotalPrice(), request.getAmount());
            throw new PaymentException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 3. 중복 결제 방지 - 해당 주문에 대해 PENDING 또는 PAID 상태의 결제가 있는지 확인
        boolean hasActivePayment = paymentRepository.existsByOrderAndStatusIn(
                order,
                java.util.List.of(PaymentStatus.PENDING, PaymentStatus.PAID)
        );

        if (hasActivePayment) {
            log.error("중복 결제 시도 - orderId: {}", request.getOrderId());
            throw new PaymentException(ErrorCode.PAYMENT_DUPLICATE);
        }

        // 4. 고유한 결제 ID 생성 (merchant_uid로 사용)
        String dbPaymentId = "PAY_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);

        // 5. Payment 엔티티 생성 및 저장
        Payment payment = Payment.builder()
                .dbPaymentId(dbPaymentId)
                .order(order)
                .totalAmount(request.getAmount())
                .pointsToUse(pointsToUse)
                .status(PaymentStatus.PENDING)
                .build();

        paymentRepository.save(payment);
        log.info("결제 준비 완료 - dbPaymentId: {}", dbPaymentId);

        // 6. 응답 생성
        return PaymentPrepareResponse.builder()
                .dbPaymentId(dbPaymentId)
                .orderId(order.getId())
                .orderName("주문번호: " + order.getOrderNum())
                .amount(request.getAmount())
                .pointsToUse(pointsToUse)
                .finalAmount(finalAmount)
                .buyerName(request.getBuyerName())
                .buyerTel(request.getBuyerTel())
                .buyerEmail(request.getBuyerEmail())
                .payMethod(request.getPayMethod())
                .build();
    }

    /**
     * 결제 검증
     * 1. DB에서 결제 정보 조회
     * 2. 멱등성 체크 (이미 처리된 결제인지)
     * 3. PortOne API로 실제 결제 정보 조회
     * 4. merchant_uid 일치 확인
     * 5. 금액 검증 (위변조 방지)
     * 6. 결제 상태 확인
     * 7. 결제 성공 처리 및 주문 상태 변경
     */
    @Transactional
    public PaymentVerifyResponse verifyPayment(PaymentVerifyRequest request) {
        log.info("결제 검증 시작 - impUid: {}, merchantUid: {}", request.getImpUid(), request.getMerchantUid());

        // 1. DB에서 결제 정보 조회 (merchant_uid 사용)
        Payment dbPayment = paymentRepository.findByDbPaymentId(request.getMerchantUid())
                .orElseThrow(() -> new PaymentException(ErrorCode.PAYMENT_NOT_FOUND));

        // 2. 멱등성 체크 (이미 처리된 결제인지)
        if (dbPayment.getStatus() == PaymentStatus.PAID) {
            log.info("이미 처리된 결제 - paymentId: {}", dbPayment.getId());
            return PaymentVerifyResponse.alreadyProcessed();
        }

        try {
            // 3. PortOne API로 실제 결제 정보 조회 (imp_uid 사용)
            IamportResponse<com.siot.IamportRestClient.response.Payment> portOneResponse =
                    iamportClient.paymentByImpUid(request.getImpUid());

            // 4. PortOne API 응답 확인
            if (portOneResponse.getCode() != 0) {
                log.error("PortOne API 조회 실패 - code: {}, message: {}",
                        portOneResponse.getCode(), portOneResponse.getMessage());
                dbPayment.failPayment();
                throw new PaymentException(ErrorCode.PORTONE_API_ERROR, portOneResponse.getMessage());
            }

            com.siot.IamportRestClient.response.Payment portOnePayment = portOneResponse.getResponse();

            // 5. merchant_uid 일치 확인 (위변조 방지)
            if (!portOnePayment.getMerchantUid().equals(dbPayment.getDbPaymentId())) {
                log.error("merchant_uid 불일치 - PortOne: {}, DB: {}",
                        portOnePayment.getMerchantUid(), dbPayment.getDbPaymentId());
                dbPayment.failPayment();
                throw new PaymentException(ErrorCode.PAYMENT_INFO_MISMATCH);
            }

            // 6. 금액 검증 (위변조 방지 - 가장 중요!)
            BigDecimal finalAmount = dbPayment.getTotalAmount().subtract(
                    dbPayment.getPointsToUse() != null ? dbPayment.getPointsToUse() : BigDecimal.ZERO
            );

            if (portOnePayment.getAmount().compareTo(finalAmount) != 0) {
                log.error("결제 금액 불일치 - PortOne: {}, DB: {}",
                        portOnePayment.getAmount(), finalAmount);
                dbPayment.failPayment();
                throw new PaymentException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
            }

            // 7. 결제 상태 확인
            if (!"paid".equals(portOnePayment.getStatus())) {
                log.error("결제 미완료 - status: {}", portOnePayment.getStatus());
                dbPayment.failPayment();
                throw new PaymentException(ErrorCode.PAYMENT_NOT_COMPLETED);
            }

            // 8. 결제 성공 처리
            dbPayment.completePayment(request.getImpUid());
            log.info("결제 성공 처리 완료 - paymentId: {}", dbPayment.getId());

            // 9. 주문 상태를 PREPARING(조리 준비)로 변경
            Order order = dbPayment.getOrder();
            order.updateStatus(OrderStatus.PREPARING);
            log.info("주문 상태 변경 완료 - orderId: {}, status: PREPARING", order.getId());

            // 10. 응답 생성
            return PaymentVerifyResponse.success(
                    dbPayment.getId(),
                    order.getId(),
                    request.getImpUid(),
                    finalAmount,
                    OrderStatus.PREPARING.getDescription()
            );

        } catch (IamportResponseException e) {
            log.error("PortOne API 호출 중 예외 발생", e);
            dbPayment.failPayment();
            throw new PaymentException(ErrorCode.PORTONE_API_ERROR, e.getMessage());
        } catch (IOException e) {
            log.error("PortOne API 통신 중 IO 예외 발생", e);
            dbPayment.failPayment();
            throw new PaymentException(ErrorCode.PORTONE_API_ERROR, "API 통신 중 오류가 발생했습니다.");
        }
    }

    /**
     * 웹훅 처리
     *
     * PortOne에서 결제 상태가 변경될 때마다 호출되는 웹훅을 처리합니다.
     *
     * 처리 순서:
     * 1. 중복 웹훅 체크 (imp_uid + status 조합으로 확인)
     * 2. 웹훅 기록 저장 (별도 트랜잭션, 감사 추적용)
     * 3. PortOne API로 실제 결제 정보 조회 (검증) - 트랜잭션 외부에서 실행
     * 4. DB 결제 정보 조회 및 검증 (새 트랜잭션)
     * 5. 멱등성 체크 (이미 처리된 결제인지)
     * 6. merchant_uid 일치 확인
     * 7. 금액 검증
     * 8. 결제 상태에 따른 처리 (paid/failed/cancelled)
     * 9. 주문 상태 업데이트
     * 10. 웹훅 처리 완료 표시 (별도 트랜잭션)
     * 11. PortOne에 200 OK 응답
     */
    public String processWebhook(WebhookRequest request) {
        log.info("웹훅 수신 - impUid: {}, merchantUid: {}, status: {}",
                request.getImp_uid(), request.getMerchant_uid(), request.getStatus());

        // 1. 중복 웹훅 체크 (imp_uid + status 조합으로 확인)
        // 같은 imp_uid라도 다른 상태(paid → cancelled 등)는 별도 처리
        if (webhookService.isDuplicateWebhook(request.getImp_uid(), request.getStatus())) {
            log.info("이미 처리된 웹훅 - impUid: {}, status: {}", request.getImp_uid(), request.getStatus());
            return "OK"; // PortOne에 200 OK 응답 (멱등성 보장)
        }

        // 2. 웹훅 기록 저장 (별도 트랜잭션으로 실행하여 예외 발생 시에도 기록 유지)
        Long webhookId = webhookService.saveWebhookRecord(request);

        try {
            // 3. PortOne API로 실제 결제 정보 조회 (검증)
            // 트랜잭션 외부에서 실행하여 DB 커넥션 점유 방지
            IamportResponse<com.siot.IamportRestClient.response.Payment> portOneResponse =
                    iamportClient.paymentByImpUid(request.getImp_uid());

            // 4. PortOne API 응답 확인
            if (portOneResponse.getCode() != 0) {
                log.error("PortOne API 조회 실패 - code: {}, message: {}",
                        portOneResponse.getCode(), portOneResponse.getMessage());
                webhookService.markWebhookAsFailed(webhookId);
                // PortOne API 일시적 장애 → 재시도 필요
                throw new PaymentException(ErrorCode.PORTONE_API_ERROR, portOneResponse.getMessage(), true);
            }

            com.siot.IamportRestClient.response.Payment portOnePayment = portOneResponse.getResponse();

            // 5. DB 처리 (별도 트랜잭션으로 실행)
            processPaymentData(webhookId, portOnePayment, request.getImp_uid());

            // 6. 웹훅 처리 완료 표시 (별도 트랜잭션)
            webhookService.markWebhookAsProcessed(webhookId);
            log.info("웹훅 처리 완료 - webhookId: {}", webhookId);

            // 7. PortOne에 200 OK 응답
            return "OK";

        } catch (IamportResponseException e) {
            log.error("PortOne API 호출 중 예외 발생", e);
            webhookService.markWebhookAsFailed(webhookId);
            // PortOne API 일시적 장애 → 재시도 필요
            throw new PaymentException(ErrorCode.PORTONE_API_ERROR, e.getMessage(), true);
        } catch (IOException e) {
            log.error("PortOne API 통신 중 IO 예외 발생", e);
            webhookService.markWebhookAsFailed(webhookId);
            // 네트워크 일시적 장애 → 재시도 필요
            throw new PaymentException(ErrorCode.PORTONE_API_ERROR, "API 통신 중 오류가 발생했습니다.", true);
        } catch (PaymentException e) {
            // 재시도 가능 여부에 따라 분기
            if (e.isRetryable()) {
                // 일시적 장애 (PortOne API 다운, 네트워크 장애 등)
                // → 예외를 던져서 포트원이 나중에 다시 웹훅을 보내도록 유도
                log.error("웹훅 처리 중 일시적 장애 발생 (재시도 필요): {}", e.getMessage());
                throw e;
            } else {
                // 비즈니스 에러 (금액 불일치, 위변조 의심 등)
                // → 재시도해도 똑같은 결과이므로 OK 반환하여 무한 재시도 방지
                log.error("웹훅 비즈니스 검증 실패 (재시도 불필요): {}", e.getMessage());
                return "OK";
            }
        } catch (Exception e) {
            // 예상치 못한 시스템 에러 → 재시도 필요
            log.error("웹훅 시스템 에러 발생 (재시도 필요)", e);
            webhookService.markWebhookAsFailed(webhookId);
            throw e;
        }
    }

    /**
     * 결제 데이터 처리 (별도 트랜잭션)
     *
     * PortOne API 조회 후 DB 처리를 별도 트랜잭션으로 분리
     * 외부 API 호출 중 DB 커넥션을 점유하지 않도록 보장
     */
    @Transactional
    public void processPaymentData(Long webhookId,
                                    com.siot.IamportRestClient.response.Payment portOnePayment,
                                    String impUid) {
        // 1. DB에서 결제 정보 조회
        Payment dbPayment = paymentRepository.findByDbPaymentId(portOnePayment.getMerchantUid())
                .orElseThrow(() -> {
                    webhookService.markWebhookAsFailed(webhookId);
                    return new PaymentException(ErrorCode.PAYMENT_NOT_FOUND);
                });

        // 2. merchant_uid 일치 확인 (위변조 방지)
        if (!portOnePayment.getMerchantUid().equals(dbPayment.getDbPaymentId())) {
            log.error("merchant_uid 불일치 - PortOne: {}, DB: {}",
                    portOnePayment.getMerchantUid(), dbPayment.getDbPaymentId());
            webhookService.markWebhookAsFailed(webhookId);
            throw new PaymentException(ErrorCode.PAYMENT_INFO_MISMATCH);
        }

        // 3. 금액 검증 (위변조 방지)
        BigDecimal finalAmount = dbPayment.getTotalAmount().subtract(
                dbPayment.getPointsToUse() != null ? dbPayment.getPointsToUse() : BigDecimal.ZERO
        );

        if (portOnePayment.getAmount().compareTo(finalAmount) != 0) {
            log.error("결제 금액 불일치 - PortOne: {}, DB: {}",
                    portOnePayment.getAmount(), finalAmount);
            webhookService.markWebhookAsFailed(webhookId);
            throw new PaymentException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 4. 결제 상태에 따른 처리
        Order order = dbPayment.getOrder();
        String portOneStatus = portOnePayment.getStatus();

        switch (portOneStatus) {
            case "paid":
                // 4-1. 멱등성 체크 (이미 처리된 결제인지)
                if (dbPayment.getStatus() == PaymentStatus.PAID) {
                    log.info("이미 처리된 결제 - paymentId: {}", dbPayment.getId());
                    return;
                }

                // 결제 성공 처리
                dbPayment.completePayment(impUid);
                log.info("결제 성공 처리 완료 - paymentId: {}", dbPayment.getId());

                // 주문 상태를 PREPARING(조리 준비)로 변경
                order.updateStatus(OrderStatus.PREPARING);
                log.info("주문 상태 변경 완료 - orderId: {}, status: PREPARING", order.getId());
                break;

            case "failed":
                // 결제 실패 처리
                dbPayment.failPayment();
                log.info("결제 실패 처리 완료 - paymentId: {}", dbPayment.getId());

                // 주문 상태는 WATING 유지 (다시 결제 시도 가능)
                break;

            case "cancelled":
                // 결제 취소 처리
                dbPayment.failPayment();
                log.info("결제 취소 처리 완료 - paymentId: {}", dbPayment.getId());

                // 주문 상태를 CANCELLED로 변경
                order.updateStatus(OrderStatus.CANCELLED);
                log.info("주문 상태 변경 완료 - orderId: {}, status: CANCELLED", order.getId());
                break;

            default:
                log.error("알 수 없는 결제 상태 - status: {}", portOneStatus);
                webhookService.markWebhookAsFailed(webhookId);
                throw new PaymentException(ErrorCode.WEBHOOK_INVALID_STATUS);
        }
    }

}
