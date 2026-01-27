package com.runners.app.community.exception;

import com.runners.app.global.exception.DomainException;
import com.runners.app.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class CommunityDomainException extends DomainException {
    public CommunityDomainException(HttpStatus status, ErrorCode errorCode, String message) {
        super(status, errorCode, message);
    }

    public static CommunityDomainException userNotFound() {
        return new CommunityDomainException(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND, "User not found");
    }

    public static CommunityDomainException postNotFound() {
        return new CommunityDomainException(HttpStatus.NOT_FOUND, ErrorCode.POST_NOT_FOUND, "Post not found");
    }

    public static CommunityDomainException commentNotFound() {
        return new CommunityDomainException(HttpStatus.NOT_FOUND, ErrorCode.COMMENT_NOT_FOUND, "Comment not found");
    }

    public static CommunityDomainException parentCommentNotFound() {
        return new CommunityDomainException(HttpStatus.NOT_FOUND, ErrorCode.PARENT_COMMENT_NOT_FOUND, "Parent comment not found");
    }

    public static CommunityDomainException invalidId(String fieldName) {
        return new CommunityDomainException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_ID, "Invalid " + fieldName);
    }

    public static CommunityDomainException badRequest(String message) {
        return new CommunityDomainException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, message);
    }

    public static CommunityDomainException commentNotInPost() {
        return new CommunityDomainException(HttpStatus.BAD_REQUEST, ErrorCode.COMMENT_NOT_IN_POST, "Comment does not belong to the post");
    }

    public static CommunityDomainException parentCommentNotInPost() {
        return new CommunityDomainException(HttpStatus.BAD_REQUEST, ErrorCode.COMMENT_NOT_IN_POST, "Parent comment does not belong to the post");
    }

    public static CommunityDomainException noPermission(String message) {
        return new CommunityDomainException(HttpStatus.FORBIDDEN, ErrorCode.NO_PERMISSION, message);
    }

    public static CommunityDomainException queryRequired() {
        return new CommunityDomainException(HttpStatus.BAD_REQUEST, ErrorCode.QUERY_REQUIRED, "Query is required");
    }
}
