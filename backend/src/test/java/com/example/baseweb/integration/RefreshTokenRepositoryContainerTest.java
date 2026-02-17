package com.example.baseweb.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.baseweb.auth.entity.RefreshToken;
import com.example.baseweb.auth.repo.RefreshTokenRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class RefreshTokenRepositoryContainerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Test
    void migrationAndRepositoryWork() {
        RefreshToken saved = refreshTokenRepository.save(RefreshToken.builder()
            .userId(UUID.randomUUID())
            .tokenHash("hash-value")
            .expiresAt(Instant.now().plusSeconds(1000))
            .revoked(false)
            .createdAt(Instant.now())
            .build());

        assertThat(saved.getId()).isNotNull();
        assertThat(refreshTokenRepository.findByTokenHash("hash-value")).isPresent();
    }
}
