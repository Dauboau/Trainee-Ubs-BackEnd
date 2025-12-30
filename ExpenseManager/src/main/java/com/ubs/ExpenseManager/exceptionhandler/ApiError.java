package com.ubs.ExpenseManager.exceptionhandler;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ApiError(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {

    public ApiError(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path);
    }
}
