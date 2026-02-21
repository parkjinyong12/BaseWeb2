package com.example.baseweb.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        // secret 값은 운영 환경마다 raw 문자열, Base64, Base64URL 중 하나로 들어올 수 있어
        // 순차적으로 디코딩을 시도한다.
        byte[] keyBytes = decodeSecret(jwtProperties.secret());
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    private byte[] decodeSecret(String secret) {
        try {
            return Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException | DecodingException ex) {
            // BASE64 실패 시 BASE64URL로 재시도한다.
            log.debug("JWT secret is not valid Base64, trying Base64URL decode", ex);
        }

        try {
            return Decoders.BASE64URL.decode(secret);
        } catch (IllegalArgumentException | DecodingException ex) {
            // 마지막 fallback: plain text secret(UTF-8 bytes)로 처리한다.
            log.warn("JWT secret is neither Base64 nor Base64URL. Falling back to raw UTF-8 bytes.", ex);
            return secret.getBytes(StandardCharsets.UTF_8);
        }
    }

    public String createAccessToken(String subject, List<String> roles) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(jwtProperties.accessTokenValiditySeconds());
        return Jwts.builder()
            .subject(subject)
            .claim("roles", roles)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(secretKey)
            .compact();
    }

    public boolean validateAccessToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }

    public long accessTokenValiditySeconds() {
        return jwtProperties.accessTokenValiditySeconds();
    }

    public long refreshTokenValiditySeconds() {
        return jwtProperties.refreshTokenValiditySeconds();
    }
}
