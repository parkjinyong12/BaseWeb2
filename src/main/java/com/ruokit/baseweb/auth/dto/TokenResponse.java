package com.ruokit.baseweb.auth.dto;

public record TokenResponse(String accessToken, long expiresIn) {
}
