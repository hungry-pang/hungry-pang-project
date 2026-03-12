package com.example.hungrypangproject.domain.refund.exception;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.common.exception.ServiceException;

public class RefundException extends ServiceException {
    public RefundException(ErrorCode errorCode) {super(errorCode);}

    public RefundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
