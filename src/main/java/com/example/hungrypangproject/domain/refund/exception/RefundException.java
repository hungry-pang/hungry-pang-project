package com.example.hungrypangproject.domain.refund.exception;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.common.exception.ServiceException;

public class RefundException extends ServiceException {

    private final boolean retryable;

    public RefundException(ErrorCode errorCode) {
        super(errorCode);
        this.retryable = false;
    }

    public RefundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
        this.retryable = false;
    }

    public RefundException(ErrorCode errorCode, String message, boolean retryable) {
        super(errorCode, message);
        this.retryable = retryable;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
