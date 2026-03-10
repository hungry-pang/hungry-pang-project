package com.example.hungrypangproject.domain.payment.exception;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.common.exception.ServiceException;

public class PaymentException extends ServiceException {

    public PaymentException(ErrorCode errorCode) {
        super(errorCode);
    }

    public PaymentException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
