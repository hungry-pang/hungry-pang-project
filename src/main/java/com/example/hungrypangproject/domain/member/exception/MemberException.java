package com.example.hungrypangproject.domain.member.exception;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.common.exception.ServiceException;

public class MemberException extends ServiceException {
    public MemberException(ErrorCode errorCode) {
        super(errorCode);
    }
}
