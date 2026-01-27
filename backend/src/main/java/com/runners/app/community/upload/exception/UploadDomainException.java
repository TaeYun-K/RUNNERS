package com.runners.app.community.upload.exception;

import com.runners.app.global.exception.DomainException;
import com.runners.app.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class UploadDomainException extends DomainException {
    public UploadDomainException(HttpStatus status, ErrorCode errorCode, String message) {
        super(status, errorCode, message);
    }

    public static UploadDomainException invalidKey() {
        return new UploadDomainException(HttpStatus.BAD_REQUEST, ErrorCode.UPLOAD_INVALID_KEY, "Invalid key");
    }

    public static UploadDomainException filesRequired() {
        return new UploadDomainException(HttpStatus.BAD_REQUEST, ErrorCode.UPLOAD_FILES_REQUIRED, "files is required");
    }

    public static UploadDomainException tooManyFiles() {
        return new UploadDomainException(HttpStatus.BAD_REQUEST, ErrorCode.UPLOAD_TOO_MANY_FILES, "Too many files");
    }

    public static UploadDomainException fileRequired() {
        return new UploadDomainException(HttpStatus.BAD_REQUEST, ErrorCode.UPLOAD_FILE_REQUIRED, "file is required");
    }

    public static UploadDomainException contentTypeRequired() {
        return new UploadDomainException(HttpStatus.BAD_REQUEST, ErrorCode.UPLOAD_CONTENT_TYPE_REQUIRED, "contentType is required");
    }

    public static UploadDomainException contentTypeNotAllowed() {
        return new UploadDomainException(HttpStatus.BAD_REQUEST, ErrorCode.UPLOAD_CONTENT_TYPE_NOT_ALLOWED, "Only image/* contentType is allowed");
    }

    public static UploadDomainException contentLengthInvalid() {
        return new UploadDomainException(HttpStatus.BAD_REQUEST, ErrorCode.UPLOAD_CONTENT_LENGTH_INVALID, "contentLength must be positive");
    }

    public static UploadDomainException fileTooLarge() {
        return new UploadDomainException(HttpStatus.BAD_REQUEST, ErrorCode.UPLOAD_FILE_TOO_LARGE, "File is too large");
    }

    public static UploadDomainException s3ConfigInvalid(String message) {
        return new UploadDomainException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.UPLOAD_S3_CONFIG_INVALID, message);
    }

    public static UploadDomainException deleteProfileImageFailed() {
        return new UploadDomainException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.UPLOAD_S3_DELETE_FAILED, "Failed to delete profile image");
    }
}

