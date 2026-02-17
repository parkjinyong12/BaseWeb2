package com.example.baseweb.auth.service;

import com.example.baseweb.auth.entity.RefreshToken;
import com.example.baseweb.auth.repo.RefreshTokenRepository;
import com.example.baseweb.security.JwtService;
import com.example.baseweb.user.UserAccount;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public String issueRefreshToken(UserAccount user) {
        String rawToken = generateRawToken();
        refreshTokenRepository.save(RefreshToken.builder()
            .userId(user.getId())
            .tokenHash(hash(rawToken))
            .expiresAt(Instant.now().plusSeconds(jwtService.refreshTokenValiditySeconds()))
            .revoked(false)
            .createdAt(Instant.now())
            .build());
        return rawToken;
    }

    @Transactional
    public RotationResult rotate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        String oldHash = hash(rawToken);
        RefreshToken existing = refreshTokenRepository.findByTokenHash(oldHash)
            .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (existing.isRevoked() || existing.getExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Refresh token expired or revoked");
        }

        String newToken = generateRawToken();
        String newHash = hash(newToken);
        existing.revoke(newHash);

        refreshTokenRepository.save(RefreshToken.builder()
            .userId(existing.getUserId())
            .tokenHash(newHash)
            .expiresAt(Instant.now().plusSeconds(jwtService.refreshTokenValiditySeconds()))
            .revoked(false)
            .createdAt(Instant.now())
            .build());

        return new RotationResult(existing.getUserId(), newToken);
    }

    @Transactional
    public void revoke(String rawToken) {
        Optional.ofNullable(rawToken)
            .map(this::hash)
            .flatMap(refreshTokenRepository::findByTokenHash)
            .ifPresent(token -> token.revoke(null));
    }

    public String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash token", e);
        }
    }

    private String generateRawToken() {
        byte[] random = UUID.randomUUID().toString().concat(UUID.randomUUID().toString()).getBytes(StandardCharsets.UTF_8);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(random);
    }

    public record RotationResult(UUID userId, String newRefreshToken) {
    }
}
