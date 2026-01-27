package com.runners.app.global.validation;

public final class ValidationMessageKey {
    private ValidationMessageKey() {
    }

    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";

    // Dev
    public static final String DEV_EMAIL_REQUIRED = "DEV_EMAIL_REQUIRED";
    public static final String DEV_EMAIL_INVALID_FORMAT = "DEV_EMAIL_INVALID_FORMAT";
    public static final String DEV_EMAIL_TOO_LONG = "DEV_EMAIL_TOO_LONG";
    public static final String DEV_NAME_TOO_LONG = "DEV_NAME_TOO_LONG";
    public static final String DEV_ROLE_TOO_LONG = "DEV_ROLE_TOO_LONG";

    // User / profile
    public static final String NICKNAME_TOO_SHORT = "NICKNAME_TOO_SHORT";
    public static final String NICKNAME_TOO_LONG = "NICKNAME_TOO_LONG";
    public static final String NICKNAME_INVALID_FORMAT = "NICKNAME_INVALID_FORMAT";
    public static final String INTRO_TOO_LONG = "INTRO_TOO_LONG";
    public static final String PROFILE_IMAGE_KEY_REQUIRED = "PROFILE_IMAGE_KEY_REQUIRED";
    public static final String PROFILE_IMAGE_KEY_INVALID = "PROFILE_IMAGE_KEY_INVALID";
    public static final String TOTAL_DISTANCE_REQUIRED = "TOTAL_DISTANCE_REQUIRED";
    public static final String TOTAL_DISTANCE_OUT_OF_RANGE = "TOTAL_DISTANCE_OUT_OF_RANGE";
    public static final String TOTAL_DURATION_REQUIRED = "TOTAL_DURATION_REQUIRED";
    public static final String TOTAL_DURATION_OUT_OF_RANGE = "TOTAL_DURATION_OUT_OF_RANGE";
    public static final String RUN_COUNT_REQUIRED = "RUN_COUNT_REQUIRED";
    public static final String RUN_COUNT_OUT_OF_RANGE = "RUN_COUNT_OUT_OF_RANGE";

    // Community
    public static final String TITLE_REQUIRED = "TITLE_REQUIRED";
    public static final String TITLE_TOO_LONG = "TITLE_TOO_LONG";
    public static final String CONTENT_REQUIRED = "CONTENT_REQUIRED";
    public static final String IMAGE_TOO_MANY = "IMAGE_TOO_MANY";
    public static final String IMAGE_KEY_REQUIRED = "IMAGE_KEY_REQUIRED";
    public static final String IMAGE_KEY_TOO_LONG = "IMAGE_KEY_TOO_LONG";

    // Upload
    public static final String UPLOAD_FILES_REQUIRED = "UPLOAD_FILES_REQUIRED";
    public static final String UPLOAD_TOO_MANY_FILES = "UPLOAD_TOO_MANY_FILES";
    public static final String UPLOAD_FILE_NAME_TOO_LONG = "UPLOAD_FILE_NAME_TOO_LONG";
    public static final String UPLOAD_CONTENT_TYPE_REQUIRED = "UPLOAD_CONTENT_TYPE_REQUIRED";
    public static final String UPLOAD_CONTENT_TYPE_TOO_LONG = "UPLOAD_CONTENT_TYPE_TOO_LONG";
    public static final String UPLOAD_CONTENT_LENGTH_INVALID = "UPLOAD_CONTENT_LENGTH_INVALID";
}
