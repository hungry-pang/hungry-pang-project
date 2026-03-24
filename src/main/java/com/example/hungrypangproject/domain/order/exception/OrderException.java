package com.example.hungrypangproject.domain.order.exception;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.common.exception.ServiceException;

public class OrderException extends ServiceException {
    public OrderException(ErrorCode errorCode) {
        super(errorCode);
    }
}
