package com.example.baseweb.common;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "Invalid request payload."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "Authentication is required."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "A002", "Access denied."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S001", "Internal server error.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
