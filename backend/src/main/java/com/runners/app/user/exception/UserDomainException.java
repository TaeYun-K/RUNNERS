package com.runners.app.user.exception;

import com.runners.app.global.exception.DomainException;
import com.runners.app.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class UserDomainException extends DomainException {
    public UserDomainException(HttpStatus status, ErrorCode errorCode, String message) {
        super(status, errorCode, message);
    }

    public static UserDomainException nicknameRequired() {
        return new UserDomainException(HttpStatus.BAD_REQUEST, ErrorCode.NICKNAME_REQUIRED, "Nickname is required");
    }

    public static UserDomainException nicknameDuplicated() {
        return new UserDomainException(HttpStatus.CONFLICT, ErrorCode.NICKNAME_DUPLICATED, "Nickname already in use");
    }
}

