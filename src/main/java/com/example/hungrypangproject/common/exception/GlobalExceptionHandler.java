package com.example.hungrypangproject.common.exception;

import com.example.hungrypangproject.common.dto.ApiResponse;
import com.example.hungrypangproject.common.dto.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ApiResponse<ExceptionResponse>> handleServiceException(ServiceException exception, HttpServletRequest request) {
        ExceptionResponse response = ExceptionResponse.from(exception.getStatus().value(), exception.getMessage(), request.getRequestURI());
        return ResponseEntity.status(exception.getStatus()).body(ApiResponse.fail(exception.getStatus(), response));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ExceptionResponse>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String errorMessage = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("입력 값이 올바르지 않습니다.");

        ExceptionResponse response = ExceptionResponse.from(exception.getStatusCode().value(), errorMessage, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(HttpStatus.BAD_REQUEST, response));
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<ExceptionResponse>> handlePessimisticLock(
            PessimisticLockingFailureException e, HttpServletRequest request) {
        ExceptionResponse response = ExceptionResponse.from(
                HttpStatus.CONFLICT.value(),
                "주문이 몰려 처리에 실패했습니다. 잠시 후 다시 시도해주세요.",
                request.getRequestURI()
        );
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail(HttpStatus.CONFLICT, response));
    }
}
