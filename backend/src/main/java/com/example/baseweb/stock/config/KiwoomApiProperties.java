package com.example.baseweb.stock.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.stock.kiwoom")
public record KiwoomApiProperties(
    String baseUrl,
    String apiId
) {
}
