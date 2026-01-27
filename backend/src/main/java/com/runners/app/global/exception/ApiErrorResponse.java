package com.runners.app.global.exception;

public record ApiErrorResponse(
        int status,
        String errorCode,
        String message
) {
}

