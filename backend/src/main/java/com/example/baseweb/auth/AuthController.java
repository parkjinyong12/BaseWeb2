package com.example.baseweb.auth;

import com.example.baseweb.auth.dto.LoginRequest;
import com.example.baseweb.auth.dto.TokenResponse;
import com.example.baseweb.auth.service.AuthService;
import com.example.baseweb.auth.service.RefreshTokenService;
import com.example.baseweb.config.AuthCookieProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final AuthCookieProperties cookieProperties;

    public AuthController(AuthService authService, RefreshTokenService refreshTokenService, AuthCookieProperties cookieProperties) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.cookieProperties = cookieProperties;
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email(username field) and password")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.LoginResult result = authService.login(request.username(), request.password());
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(result.refreshToken(), cookieProperties.refreshMaxAgeSeconds()).toString())
            .body(result.tokenResponse());
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token cookie")
    public ResponseEntity<TokenResponse> refresh(HttpServletRequest request) {
        String refreshToken = readCookie(request, "refreshToken");
        AuthService.LoginResult result = authService.refresh(refreshToken);

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(result.refreshToken(), cookieProperties.refreshMaxAgeSeconds()).toString())
            .body(result.tokenResponse());
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and revoke refresh token")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        refreshTokenService.revoke(readCookie(request, "refreshToken"));
        return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, buildRefreshCookie("", 0).toString())
            .build();
    }

    private String readCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private ResponseCookie buildRefreshCookie(String value, long maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from("refreshToken", value)
            .httpOnly(true)
            .secure(cookieProperties.secure())
            .path("/")
            .sameSite(cookieProperties.sameSite())
            .maxAge(maxAge);

        if (cookieProperties.domain() != null && !cookieProperties.domain().isBlank()) {
            builder.domain(cookieProperties.domain());
        }

        return builder.build();
    }
}
