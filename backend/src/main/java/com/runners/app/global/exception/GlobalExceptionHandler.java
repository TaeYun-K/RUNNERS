package com.runners.app.global.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiErrorResponse> handleDomainException(DomainException e) {
        HttpStatus status = e.getStatus();
        return error(status, e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        if (fieldError == null) {
            return error(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED, "Validation failed");
        }

        ValidationError mapped = mapFieldErrorCodeBased(fieldError);
        return error(HttpStatus.BAD_REQUEST, mapped.errorCode(), mapped.message());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        ConstraintViolation<?> violation = e.getConstraintViolations().stream().findFirst().orElse(null);
        if (violation == null) {
            return error(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED, "Validation failed");
        }
        String message = Objects.toString(violation.getMessage(), "Validation failed");
        return error(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED, message);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException e) {
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        ErrorCode code = toErrorCode(status);
        String reason = e.getReason();
        String message = (reason == null || reason.isBlank()) ? status.getReasonPhrase() : reason;
        return error(status, code, message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        String message = e.getMessage() == null ? "Invalid request" : e.getMessage();
        return error(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnhandled(Exception e) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, "Internal server error");
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, ErrorCode errorCode, String message) {
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(status.value(), errorCode.name(), message));
    }

    private record ValidationError(ErrorCode errorCode, String message) {
    }

    // "코드 기반" validation: 애노테이션 속성/descriptor를 읽지 않고, DTO 스펙을 코드로 고정해 errorCode를 안정적으로 매핑
    private ValidationError mapFieldErrorCodeBased(FieldError fieldError) {
        String field = Objects.toString(fieldError.getField(), "");
        String constraint = extractConstraint(fieldError);

        Integer len = stringLength(fieldError.getRejectedValue());

        // UpdateProfileRequest
        if ("nickname".equals(field)) {
            if ("NotBlank".equals(constraint)) return new ValidationError(ErrorCode.NICKNAME_REQUIRED, "Nickname is required");
            if ("Size".equals(constraint)) {
                if (len != null && len < 2) return new ValidationError(ErrorCode.NICKNAME_TOO_SHORT, "nickname length must be >= 2");
                return new ValidationError(ErrorCode.NICKNAME_TOO_LONG, "nickname length must be <= 20");
            }
            if ("Pattern".equals(constraint)) return new ValidationError(ErrorCode.NICKNAME_INVALID_FORMAT, "nickname has invalid format");
            return new ValidationError(ErrorCode.VALIDATION_FAILED, "Validation failed");
        }
        if ("intro".equals(field)) {
            if ("Size".equals(constraint)) return new ValidationError(ErrorCode.INTRO_TOO_LONG, "intro length must be <= 30");
            return new ValidationError(ErrorCode.VALIDATION_FAILED, "Validation failed");
        }

        // CreateCommunityPostRequest
        if ("title".equals(field)) {
            if ("NotBlank".equals(constraint)) return new ValidationError(ErrorCode.TITLE_REQUIRED, "title is required");
            if ("Size".equals(constraint)) return new ValidationError(ErrorCode.TITLE_TOO_LONG, "title length must be <= 200");
            return new ValidationError(ErrorCode.VALIDATION_FAILED, "Validation failed");
        }
        if ("content".equals(field)) {
            if ("NotBlank".equals(constraint)) return new ValidationError(ErrorCode.CONTENT_REQUIRED, "content is required");
            return new ValidationError(ErrorCode.VALIDATION_FAILED, "Validation failed");
        }
        if ("imageKeys".equals(field)) {
            if ("Size".equals(constraint)) return new ValidationError(ErrorCode.IMAGE_TOO_MANY, "imageKeys size must be <= 10");
            return new ValidationError(ErrorCode.VALIDATION_FAILED, "Validation failed");
        }
        if (field.startsWith("imageKeys") && "Size".equals(constraint)) {
            return new ValidationError(ErrorCode.IMAGE_KEY_TOO_LONG, "imageKey length must be <= 512");
        }

        return new ValidationError(ErrorCode.VALIDATION_FAILED, "Validation failed");
    }

    private String defaultMessage(FieldError fieldError, String fallback) {
        String msg = fieldError.getDefaultMessage();
        return (msg == null || msg.isBlank()) ? fallback : msg;
    }

    private Integer stringLength(Object value) {
        if (value instanceof String s) return s.length();
        return null;
    }

    private String extractConstraint(FieldError fieldError) {
        String[] codes = fieldError.getCodes();
        if (codes == null) return null;
        for (String code : codes) {
            if (code == null) continue;
            int idx = code.indexOf('.');
            if (idx > 0) return code.substring(0, idx);
        }
        return null;
    }

    private ErrorCode toErrorCode(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> ErrorCode.BAD_REQUEST;
            case UNAUTHORIZED -> ErrorCode.UNAUTHORIZED;
            case FORBIDDEN -> ErrorCode.FORBIDDEN;
            case NOT_FOUND -> ErrorCode.NOT_FOUND;
            case CONFLICT -> ErrorCode.CONFLICT;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }
}
