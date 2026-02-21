package com.ruokit.baseweb.security;

import com.ruokit.baseweb.security.kiwoom.dto.KiwoomTokenProxyResponse;
import com.ruokit.baseweb.security.kiwoom.dto.KiwoomTokenResponse;
import com.ruokit.baseweb.security.kiwoom.dto.TokenSourceCode;
import com.ruokit.baseweb.security.kiwoom.service.KiwoomTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtService jwtService;

    @MockBean
    KiwoomTokenService kiwoomTokenService;

    @Test
    void protectedApiWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void versionApiWithoutTokenReturns200() throws Exception {
        mockMvc.perform(get("/api/version"))
            .andExpect(status().isOk());
    }

    @Test
    void protectedApiWithTokenReturns200() throws Exception {
        String token = jwtService.createAccessToken("test-subject", List.of("ROLE_USER"));

        mockMvc.perform(get("/api/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    void anySecuritySubPathWithoutTokenIsNotBlockedByAuth() throws Exception {
        mockMvc.perform(get("/api/security/not-exists"))
            .andExpect(status().isNotFound());
    }

    @Test
    void kiwoomTokenPostEndpointWithoutTokenReturns200() throws Exception {
        when(kiwoomTokenService.issueAccessToken(any(), any(), any()))
            .thenReturn(new KiwoomTokenProxyResponse(
                "au10001",
                null,
                null,
                new KiwoomTokenResponse("20991107083713", "bearer", "token", 0, "OK"),
                TokenSourceCode.ISSUED
            ));

        mockMvc.perform(post("/api/security/oauth2/token"))
            .andExpect(status().isOk());
    }

    @Test
    void kiwoomTokenGetEndpointWithoutTokenReturns200() throws Exception {
        when(kiwoomTokenService.getOrIssueAccessToken(any(), any(), any()))
            .thenReturn(new KiwoomTokenProxyResponse(
                "au10001",
                null,
                null,
                new KiwoomTokenResponse("20991107083713", "bearer", "token", 0, "OK"),
                TokenSourceCode.ISSUED
            ));

        mockMvc.perform(get("/api/security/oauth2/token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.token").value("Bearer token"))
            .andExpect(jsonPath("$.data.tokenSourceCode").value(TokenSourceCode.ISSUED.name()));
    }
}
