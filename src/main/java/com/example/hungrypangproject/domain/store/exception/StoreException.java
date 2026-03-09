package com.example.hungrypangproject.domain.store.exception;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.common.exception.ServiceException;

public class StoreException extends ServiceException {

    public StoreException(ErrorCode errorCode) {
        super(errorCode);
    }
}
