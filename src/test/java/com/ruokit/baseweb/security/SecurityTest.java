package com.ruokit.baseweb.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

import com.ruokit.baseweb.security.kiwoom.dto.KiwoomTokenProxyResponse;
import com.ruokit.baseweb.security.kiwoom.dto.KiwoomTokenResponse;
import com.ruokit.baseweb.security.kiwoom.service.KiwoomTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

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
        String token = jwtService.createAccessToken("test-subject", java.util.List.of("ROLE_USER"));

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
    void kiwoomTokenEndpointWithoutTokenReturns200() throws Exception {
        when(kiwoomTokenService.issueAccessToken(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
            .thenReturn(new KiwoomTokenProxyResponse(null, null, null, new KiwoomTokenResponse("Bearer", "token", 3600)));

        mockMvc.perform(post("/api/security/oauth2/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "appkey": "appkey",
                      "secretkey": "secretkey",
                      "grantType": "client_credentials"
                    }
                    """))
            .andExpect(status().isOk());
    }
}
