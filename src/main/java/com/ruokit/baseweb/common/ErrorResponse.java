package com.ruokit.baseweb.common;

import java.time.Instant;

public record ErrorResponse(
        String code,
        String message,
        String traceId,
        Instant timestamp
) {
    public static ErrorResponse of(ErrorCode errorCode, String traceId) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), traceId, Instant.now());
    }

    public static ErrorResponse of(ErrorCode errorCode, String customMessage, String traceId) {
        return new ErrorResponse(errorCode.getCode(), customMessage, traceId, Instant.now());
    }
}
