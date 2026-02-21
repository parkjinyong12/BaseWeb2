package com.example.baseweb.auth.service;

import com.example.baseweb.auth.dto.TokenResponse;
import com.example.baseweb.security.JwtService;
import com.example.baseweb.user.UserAccount;
import com.example.baseweb.user.UserAccountRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserAccountRepository userAccountRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
        AuthenticationManager authenticationManager,
        UserAccountRepository userAccountRepository,
        JwtService jwtService,
        RefreshTokenService refreshTokenService,
        PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userAccountRepository = userAccountRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(String email, String password) {
        if (userAccountRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        UserAccount user = UserAccount.builder()
            .id(UUID.randomUUID())
            .email(email)
            .password(passwordEncoder.encode(password))
            .role("ROLE_USER")
            .build();
        userAccountRepository.save(user);
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
