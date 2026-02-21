package com.ruokit.baseweb.auth;

import com.ruokit.baseweb.auth.dto.LoginRequest;
import com.ruokit.baseweb.auth.dto.RegisterRequest;
import com.ruokit.baseweb.auth.dto.TokenResponse;
import com.ruokit.baseweb.auth.service.AuthService;
import com.ruokit.baseweb.auth.service.RefreshTokenService;
import com.ruokit.baseweb.config.AuthCookieProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

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
        log.info("API 진입: POST /api/auth/login username={}", request.username());
        AuthService.LoginResult result = authService.login(request.username(), request.password());
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(result.refreshToken(), cookieProperties.refreshMaxAgeSeconds()).toString())
            .body(result.tokenResponse());
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        log.info("API 진입: POST /api/auth/register email={}", request.email());
        authService.register(request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token cookie")
    public ResponseEntity<TokenResponse> refresh(HttpServletRequest request) {
        log.info("API 진입: POST /api/auth/refresh");
        String refreshToken = readCookie(request, "refreshToken");
        AuthService.LoginResult result = authService.refresh(refreshToken);

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(result.refreshToken(), cookieProperties.refreshMaxAgeSeconds()).toString())
            .body(result.tokenResponse());
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and revoke refresh token")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        log.info("API 진입: POST /api/auth/logout");
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
