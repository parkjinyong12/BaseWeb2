package com.ruokit.baseweb.security;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.io.Encoders;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    @Test
    void createsTokenWhenSecretIsBase64UrlEncoded() {
        byte[] secretBytes = "01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8);
        String base64UrlSecret = Encoders.BASE64URL.encode(secretBytes);

        JwtProperties props = new JwtProperties(base64UrlSecret, 3600, 86400);
        JwtService jwtService = new JwtService(props);

        String token = jwtService.createAccessToken("user-1", List.of("ROLE_USER"));

        assertThat(jwtService.validateAccessToken(token)).isTrue();
        assertThat(jwtService.parseClaims(token).getSubject()).isEqualTo("user-1");
    }
}
