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

    public static UserDomainException userIdInvalid() {
        return new UserDomainException(HttpStatus.BAD_REQUEST, ErrorCode.USER_ID_INVALID, "Invalid userId");
    }

    public static UserDomainException userNotFound() {
        return new UserDomainException(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND, "User not found");
    }

    public static UserDomainException profileImageKeyRequired() {
        return new UserDomainException(HttpStatus.BAD_REQUEST, ErrorCode.PROFILE_IMAGE_KEY_REQUIRED, "key is required");
    }

    public static UserDomainException profileImageKeyInvalid() {
        return new UserDomainException(HttpStatus.BAD_REQUEST, ErrorCode.PROFILE_IMAGE_KEY_INVALID, "Invalid key");
    }

    public static UserDomainException profileImageFilesCountInvalid() {
        return new UserDomainException(HttpStatus.BAD_REQUEST, ErrorCode.PROFILE_IMAGE_FILES_COUNT_INVALID, "files must have exactly 1 item");
    }
}
