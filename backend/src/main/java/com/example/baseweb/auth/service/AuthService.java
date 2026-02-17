package com.example.baseweb.auth.service;

import com.example.baseweb.auth.dto.TokenResponse;
import com.example.baseweb.security.JwtService;
import com.example.baseweb.user.UserAccount;
import com.example.baseweb.user.UserAccountRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserAccountRepository userAccountRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
        AuthenticationManager authenticationManager,
        UserAccountRepository userAccountRepository,
        JwtService jwtService,
        RefreshTokenService refreshTokenService
    ) {
        this.authenticationManager = authenticationManager;
        this.userAccountRepository = userAccountRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public LoginResult login(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password));
        User principal = (User) authentication.getPrincipal();
        UserAccount user = userAccountRepository.findByEmail(principal.getUsername()).orElseThrow();

        String accessToken = jwtService.createAccessToken(
            user.getId().toString(),
            List.of(user.getRole())
        );
        String refreshToken = refreshTokenService.issueRefreshToken(user);
        return new LoginResult(new TokenResponse(accessToken, jwtService.accessTokenValiditySeconds()), refreshToken);
    }

    public LoginResult refresh(String rawRefreshToken) {
        RefreshTokenService.RotationResult rotation = refreshTokenService.rotate(rawRefreshToken);
        UserAccount user = userAccountRepository.findById(rotation.userId()).orElseThrow();
        String accessToken = jwtService.createAccessToken(user.getId().toString(), List.of(user.getRole()));
        return new LoginResult(new TokenResponse(accessToken, jwtService.accessTokenValiditySeconds()), rotation.newRefreshToken());
    }

    public record LoginResult(TokenResponse tokenResponse, String refreshToken) {
    }
}
