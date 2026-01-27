package com.runners.app.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DomainException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorCode errorCode;

    public DomainException(HttpStatus status, ErrorCode errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

}

