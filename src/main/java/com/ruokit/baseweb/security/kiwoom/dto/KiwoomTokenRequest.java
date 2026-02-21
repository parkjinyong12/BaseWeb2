package com.ruokit.baseweb.security.kiwoom.dto;

import jakarta.validation.constraints.NotBlank;

public record KiwoomTokenRequest(
    @NotBlank String appkey,
    @NotBlank String secretkey,
    String grantType
) {
    public String resolvedGrantType() {
        if (grantType == null || grantType.isBlank()) {
            return "client_credentials";
        }
        return grantType;
    }
}
