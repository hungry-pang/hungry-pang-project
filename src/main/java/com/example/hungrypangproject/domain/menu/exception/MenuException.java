package com.example.hungrypangproject.domain.menu.exception;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.common.exception.ServiceException;

public class MenuException extends ServiceException {

  public MenuException(ErrorCode errorCode) {
    super(errorCode);
  }
}
