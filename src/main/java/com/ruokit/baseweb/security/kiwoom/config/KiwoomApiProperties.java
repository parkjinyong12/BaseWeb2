package com.ruokit.baseweb.security.kiwoom.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.kiwoom")
public record KiwoomApiProperties(
    String baseUrl,
    String tokenApiId,
    String accountEvaluationApiId,
    String appKey,
    String secretKey,
    String grantType
) {
}
