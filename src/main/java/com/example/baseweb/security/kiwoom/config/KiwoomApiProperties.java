package com.example.baseweb.security.kiwoom.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.kiwoom")
public record KiwoomApiProperties(
    String baseUrl,
    String apiId
) {
}
