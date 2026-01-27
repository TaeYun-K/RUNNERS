package com.runners.app.global.exception;

public enum ErrorCode {
    VALIDATION_FAILED,

    // User / profile
    NICKNAME_DUPLICATED,
    NICKNAME_INVALID_FORMAT,
    NICKNAME_TOO_SHORT,
    NICKNAME_TOO_LONG,
    NICKNAME_REQUIRED,
    INTRO_TOO_LONG,

    // Community
    TITLE_REQUIRED,
    TITLE_TOO_LONG,
    CONTENT_REQUIRED,
    IMAGE_TOO_MANY,
    IMAGE_KEY_TOO_LONG,

    // Auth / security
    UNAUTHORIZED,
    INVALID_TOKEN,
    FORBIDDEN,

    // Generic
    BAD_REQUEST,
    NOT_FOUND,
    CONFLICT,
    INTERNAL_ERROR,
}

