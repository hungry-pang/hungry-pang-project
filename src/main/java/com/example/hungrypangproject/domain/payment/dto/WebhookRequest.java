package com.example.hungrypangproject.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PortOne 웹훅 요청 DTO
 *
 * PortOne에서 결제 상태 변경 시 자동으로 호출하는 웹훅의 요청 데이터
 * - imp_uid: PortOne에서 발급한 결제 고유번호
 * - merchant_uid: 가맹점에서 생성한 주문번호 (우리 시스템의 dbPaymentId)
 * - status: 결제 상태 (paid, failed, cancelled 등)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WebhookRequest {

    /**
     * PortOne 결제 고유번호
     * 예: imp_1234567890
     */
    private String imp_uid;

    /**
     * 가맹점 주문번호 (우리 DB의 dbPaymentId)
     * 예: PAY_abc123def456
     */
    private String merchant_uid;

    /**
     * 결제 상태
     * - paid: 결제 완료
     * - failed: 결제 실패
     * - cancelled: 결제 취소 (전체 취소)
     * - partial_cancelled: 부분 취소
     */
    private String status;
}
