package com.example.baseweb.auth.dto;

public record TokenResponse(String accessToken, long expiresIn) {
}
