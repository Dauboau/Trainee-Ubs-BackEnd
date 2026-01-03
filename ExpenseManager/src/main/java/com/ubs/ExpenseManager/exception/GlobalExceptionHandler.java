package com.ubs.ExpenseManager.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;

import javax.naming.AuthenticationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException ex, WebRequest request) {
        HttpStatus status = ex.getStatus();
        return build(status, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(WebRequest request) {
        String message = "Formato da requisição inválido";
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentialsException(WebRequest request) {
        String message = "Credenciais inválidas";
        return build(HttpStatus.UNAUTHORIZED, message, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthenticationException(WebRequest request) {
        String message = "Credenciais inválidas";
        return build(HttpStatus.UNAUTHORIZED, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
        WebRequest request) {
        String message = "Formato da requisição inválido";
        if (ex.getCause() instanceof InvalidFormatException invalidFormatException) {
            Class<?> targetType = invalidFormatException.getTargetType();
            if (targetType.equals(CurrencyCode.class)) {
                message = "Código de moeda inválido";
            }
        }
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, WebRequest request) {
        ApiError error = new ApiError(status.value(), status.getReasonPhrase(), message,
            request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(status).body(error);
    }
}
