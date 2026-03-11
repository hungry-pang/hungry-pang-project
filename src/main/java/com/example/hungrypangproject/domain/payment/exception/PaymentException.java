package com.example.hungrypangproject.domain.payment.exception;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.common.exception.ServiceException;

public class PaymentException extends ServiceException {

    private final boolean retryable;

    public PaymentException(ErrorCode errorCode) {
        super(errorCode);
        this.retryable = false; // 기본값: 재시도 불가능 (비즈니스 에러)
    }

    public PaymentException(ErrorCode errorCode, String message) {
        super(errorCode, message);
        this.retryable = false;
    }

    /**
     * 재시도 가능 여부를 지정할 수 있는 생성자
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     * @param retryable 재시도 가능 여부 (true: 일시적 장애, false: 비즈니스 에러)
     */
    public PaymentException(ErrorCode errorCode, String message, boolean retryable) {
        super(errorCode, message);
        this.retryable = retryable;
    }

    /**
     * 재시도 가능한 예외 여부
     *
     * - true: PortOne API 장애, DB 일시적 다운 등 → 웹훅 재시도 필요 (예외 던짐)
     * - false: 금액 불일치, 위변조 등 → 웹훅 재시도 불필요 (OK 반환)
     */
    public boolean isRetryable() {
        return retryable;
    }
}
