package com.ruokit.baseweb.security.kiwoom.service;

import com.ruokit.baseweb.security.kiwoom.config.KiwoomApiProperties;
import com.ruokit.baseweb.security.kiwoom.dto.KiwoomTokenProxyResponse;
import com.ruokit.baseweb.security.kiwoom.dto.KiwoomTokenResponse;
import com.ruokit.baseweb.security.kiwoom.entity.KiwoomToken;
import com.ruokit.baseweb.security.kiwoom.repo.KiwoomTokenRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
public class KiwoomTokenService {

    private static final DateTimeFormatter EXPIRES_DT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final ZoneId KIWOOM_ZONE = ZoneId.of("Asia/Seoul");

    private final RestClient restClient;
    private final KiwoomApiProperties properties;
    private final KiwoomTokenRepository kiwoomTokenRepository;

    public KiwoomTokenService(
        RestClient.Builder restClientBuilder,
        KiwoomApiProperties properties,
        KiwoomTokenRepository kiwoomTokenRepository
    ) {
        this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
        this.properties = properties;
        this.kiwoomTokenRepository = kiwoomTokenRepository;
    }

    @Transactional
    public KiwoomTokenProxyResponse issueAccessToken(String authorization, String contYn, String nextKey) {
        KiwoomTokenProxyResponse response = requestTokenFromKiwoom(authorization, contYn, nextKey);
        upsertToken(response.body());
        return response;
    }

    @Transactional
    public KiwoomTokenProxyResponse getOrIssueAccessToken(String authorization, String contYn, String nextKey) {
        return kiwoomTokenRepository.findTopByOrderByIdAsc()
            .filter(token -> !token.isExpired(Instant.now()))
            .map(this::toStoredTokenResponse)
            .orElseGet(() -> issueAccessToken(authorization, contYn, nextKey));
    }

    private KiwoomTokenProxyResponse requestTokenFromKiwoom(String authorization, String contYn, String nextKey) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("grant_type", properties.grantType());
        payload.put("appkey", properties.appKey());
        payload.put("secretkey", properties.secretKey());

        ResponseEntity<KiwoomTokenResponse> response = restClient.post()
            .uri("/oauth2/token")
            .contentType(MediaType.APPLICATION_JSON)
            .header("api-id", properties.apiId())
            .headers(headers -> {
                if (StringUtils.hasText(authorization)) {
                    headers.set(HttpHeaders.AUTHORIZATION, authorization);
                }
                if (StringUtils.hasText(contYn)) {
                    headers.set("cont-yn", contYn);
                }
                if (StringUtils.hasText(nextKey)) {
                    headers.set("next-key", nextKey);
                }
            })
            .body(payload)
            .retrieve()
            .toEntity(KiwoomTokenResponse.class);

        HttpHeaders headers = response.getHeaders();
        return new KiwoomTokenProxyResponse(
            headers.getFirst("api-id"),
            headers.getFirst("cont-yn"),
            headers.getFirst("next-key"),
            response.getBody()
        );
    }

    private void upsertToken(KiwoomTokenResponse body) {
        if (body == null || !StringUtils.hasText(body.token()) || !StringUtils.hasText(body.expiresDt())) {
            throw new IllegalStateException("Kiwoom token 응답에 필수 값이 없습니다.");
        }

        Instant now = Instant.now();
        Instant expiresAt = parseExpiresAt(body.expiresDt());

        KiwoomToken token = kiwoomTokenRepository.findTopByOrderByIdAsc()
            .map(existing -> {
                existing.update(body.tokenType(), body.token(), body.expiresDt(), expiresAt, now);
                return existing;
            })
            .orElseGet(() -> KiwoomToken.builder()
                .tokenType(body.tokenType())
                .token(body.token())
                .expiresDt(body.expiresDt())
                .expiresAt(expiresAt)
                .createdAt(now)
                .updatedAt(now)
                .build());

        kiwoomTokenRepository.save(token);
    }

    private KiwoomTokenProxyResponse toStoredTokenResponse(KiwoomToken token) {
        return new KiwoomTokenProxyResponse(
            properties.apiId(),
            null,
            null,
            new KiwoomTokenResponse(
                token.getExpiresDt(),
                token.getTokenType(),
                token.getToken(),
                0,
                "stored token"
            )
        );
    }

    private Instant parseExpiresAt(String expiresDt) {
        LocalDateTime localDateTime = LocalDateTime.parse(expiresDt, EXPIRES_DT_FORMATTER);
        return localDateTime.atZone(KIWOOM_ZONE).toInstant();
    }
}
