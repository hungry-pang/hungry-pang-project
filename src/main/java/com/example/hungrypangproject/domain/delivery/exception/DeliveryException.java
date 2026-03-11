package com.example.hungrypangproject.domain.delivery.exception;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.common.exception.ServiceException;

public class DeliveryException extends ServiceException {
    public DeliveryException(ErrorCode errorCode) {
        super(errorCode);
    }
}
