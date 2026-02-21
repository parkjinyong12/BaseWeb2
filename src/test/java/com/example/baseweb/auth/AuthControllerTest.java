package com.example.baseweb.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.baseweb.auth.entity.RefreshToken;
import com.example.baseweb.auth.repo.RefreshTokenRepository;
import com.example.baseweb.user.UserAccount;
import com.example.baseweb.user.UserAccountRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;


    @Autowired
    UserAccountRepository userAccountRepository;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userAccountRepository.deleteAll();
        userAccountRepository.save(UserAccount.builder()
            .id(UUID.fromString("22222222-2222-2222-2222-222222222222"))
            .email("auth@test.com")
            .password(passwordEncoder.encode("password"))
            .role("ROLE_USER")
            .build());
    }

    @Test
    void loginSetsHttpOnlyCookie() throws Exception {
        String body = """
            {"username":"auth@test.com","password":"password"}
            """;

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists())
            .andReturn();

        String setCookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).contains("refreshToken=").contains("HttpOnly");
    }

    @Test
    void registerCreatesUser() throws Exception {
        String body = """
            {"email":"new@test.com","password":"password123"}
            """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated());

        UserAccount user = userAccountRepository.findByEmail("new@test.com").orElseThrow();
        assertThat(passwordEncoder.matches("password123", user.getPassword())).isTrue();
        assertThat(user.getRole()).isEqualTo("ROLE_USER");
    }

    @Test
    void registerReturnsConflictWhenEmailAlreadyExists() throws Exception {
        String body = """
            {"email":"auth@test.com","password":"password123"}
            """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").exists())
            .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    void refreshRotatesTokenAndRevokesOldToken() throws Exception {
        String body = """
            {"username":"auth@test.com","password":"password"}
            """;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andReturn();

        String cookie = loginResult.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        String oldToken = extractCookieValue(cookie);
        String oldHash = refreshTokenRepository.findAll().get(0).getTokenHash();

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                .header(HttpHeaders.COOKIE, "refreshToken=" + oldToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists())
            .andReturn();

        String rotatedCookie = refreshResult.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        String newToken = extractCookieValue(rotatedCookie);

        assertThat(newToken).isNotEqualTo(oldToken);
        RefreshToken revokedToken = refreshTokenRepository.findByTokenHash(oldHash).orElseThrow();
        assertThat(revokedToken.isRevoked()).isTrue();
        assertThat(revokedToken.getReplacedByTokenHash()).isNotBlank();
    }

    private String extractCookieValue(String setCookie) {
        return setCookie.split(";")[0].split("=")[1];
    }
}
