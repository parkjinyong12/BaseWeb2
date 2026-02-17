package com.example.baseweb.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final long accessTokenValiditySeconds;
    private final Map<String, String> refreshStore = new ConcurrentHashMap<>();

    public AuthService(AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider,
                       @Value("${security.jwt.access-token-validity-seconds:3600}") long accessTokenValiditySeconds) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
    }

    public TokenResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (AuthenticationException ex) {
            throw ex;
        }

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = UUID.randomUUID().toString();
        refreshStore.put(refreshToken, authentication.getName());
        return new TokenResponse(accessToken, refreshToken, "Bearer", Instant.now().plusSeconds(accessTokenValiditySeconds).getEpochSecond());
    }

    public TokenResponse refresh(RefreshTokenRequest request) {
        String username = refreshStore.get(request.refreshToken());
        if (username == null) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(username, null);
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        return new TokenResponse(accessToken, request.refreshToken(), "Bearer", Instant.now().plusSeconds(accessTokenValiditySeconds).getEpochSecond());
    }
}
