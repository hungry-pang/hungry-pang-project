package com.example.hungrypangproject.domain.coupon.exception;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.common.exception.ServiceException;

public class CouponException extends ServiceException {
    public CouponException(ErrorCode errorCode) {super(errorCode);}
}
