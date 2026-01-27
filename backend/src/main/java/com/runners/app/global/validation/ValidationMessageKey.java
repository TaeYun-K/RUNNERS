package com.runners.app.global.validation;

public final class ValidationMessageKey {
    private ValidationMessageKey() {
    }

    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";

    // User / profile
    public static final String NICKNAME_TOO_SHORT = "NICKNAME_TOO_SHORT";
    public static final String NICKNAME_TOO_LONG = "NICKNAME_TOO_LONG";
    public static final String NICKNAME_INVALID_FORMAT = "NICKNAME_INVALID_FORMAT";
    public static final String INTRO_TOO_LONG = "INTRO_TOO_LONG";

    // Community
    public static final String TITLE_REQUIRED = "TITLE_REQUIRED";
    public static final String TITLE_TOO_LONG = "TITLE_TOO_LONG";
    public static final String CONTENT_REQUIRED = "CONTENT_REQUIRED";
    public static final String IMAGE_TOO_MANY = "IMAGE_TOO_MANY";
    public static final String IMAGE_KEY_REQUIRED = "IMAGE_KEY_REQUIRED";
    public static final String IMAGE_KEY_TOO_LONG = "IMAGE_KEY_TOO_LONG";
}
