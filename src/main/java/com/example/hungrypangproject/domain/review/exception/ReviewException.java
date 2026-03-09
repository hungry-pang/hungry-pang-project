package com.example.hungrypangproject.domain.review.exception;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.common.exception.ServiceException;

public class ReviewException extends ServiceException {

    public ReviewException(ErrorCode errorCode) {
        super(errorCode);
    }
}
