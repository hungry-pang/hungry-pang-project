package com.example.hungrypangproject.domain.point.exception;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.common.exception.ServiceException;

public class PointException extends ServiceException {
    public PointException(ErrorCode errorCode) {
        super(errorCode);
    }
}
