package com.runners.app.auth.exception;

import com.runners.app.global.exception.DomainException;
import com.runners.app.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class AuthDomainException extends DomainException {
    public AuthDomainException(HttpStatus status, ErrorCode errorCode, String message) {
        super(status, errorCode, message);
    }

    public static AuthDomainException requestBodyNotAllowed() {
        return new AuthDomainException(HttpStatus.BAD_REQUEST, ErrorCode.REQUEST_BODY_NOT_ALLOWED, "Request body is not allowed");
    }

    public static AuthDomainException refreshTokenMissing() {
        return new AuthDomainException(HttpStatus.UNAUTHORIZED, ErrorCode.REFRESH_TOKEN_MISSING, "Missing refresh token");
    }

    public static AuthDomainException refreshTokenInvalid() {
        return new AuthDomainException(HttpStatus.UNAUTHORIZED, ErrorCode.REFRESH_TOKEN_INVALID, "Invalid refresh token");
    }

    public static AuthDomainException unauthorized() {
        return new AuthDomainException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "Unauthorized");
    }

    public static AuthDomainException invalidTokenSubject() {
        return new AuthDomainException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_TOKEN, "Invalid token subject");
    }

    public static AuthDomainException notFound() {
        return new AuthDomainException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Not found");
    }
}
