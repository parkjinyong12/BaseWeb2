package com.ruokit.baseweb.security.kiwoom.service;

import com.ruokit.baseweb.security.kiwoom.config.KiwoomApiProperties;
import com.ruokit.baseweb.security.kiwoom.dto.KiwoomTokenProxyResponse;
import com.ruokit.baseweb.security.kiwoom.dto.TokenSourceCode;
import com.ruokit.baseweb.security.kiwoom.entity.KiwoomToken;
import com.ruokit.baseweb.security.kiwoom.repo.KiwoomTokenRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KiwoomTokenServiceTest {

    private MockRestServiceServer server;
    private KiwoomTokenRepository repository;
    private KiwoomTokenService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        this.server = MockRestServiceServer.bindTo(builder).build();
        this.repository = mock(KiwoomTokenRepository.class);
        this.service = new KiwoomTokenService(
            builder,
            new KiwoomApiProperties("https://api.kiwoom.com", "au10001", "test-app", "test-secret", "client_credentials"),
            repository
        );
    }

    @Test
    void issueAccessTokenCallsKiwoomApiAndStoresResponse() {
        when(repository.findTopByOrderByIdAsc()).thenReturn(Optional.empty());
        when(repository.save(any(KiwoomToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        server.expect(requestTo("https://api.kiwoom.com/oauth2/token"))
            .andExpect(method(POST))
            .andExpect(header("api-id", "au10001"))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer seed-token"))
            .andExpect(header("cont-yn", "Y"))
            .andExpect(header("next-key", "NEXT123"))
            .andExpect(content().json("""
                {
                  "grant_type":"client_credentials",
                  "appkey":"test-app",
                  "secretkey":"test-secret"
                }
                """))
            .andRespond(withSuccess("""
                {
                  "expires_dt":"20991107083713",
                  "token_type":"bearer",
                  "token":"WQJCwyqInphKnR3bSRtB9NE1lv...",
                  "return_code":0,
                  "return_msg":"정상적으로 처리되었습니다"
                }
                """, MediaType.APPLICATION_JSON)
                .header("api-id", "au10001")
                .header("cont-yn", "N")
                .header("next-key", ""));

        KiwoomTokenProxyResponse response = service.issueAccessToken("Bearer seed-token", "Y", "NEXT123");

        assertThat(response.apiId()).isEqualTo("au10001");
        assertThat(response.contYn()).isEqualTo("N");
        assertThat(response.body()).isNotNull();
        assertThat(response.body().tokenType()).isEqualTo("bearer");
        assertThat(response.body().token()).startsWith("WQJCwyq");
        assertThat(response.tokenSourceCode()).isEqualTo(TokenSourceCode.ISSUED);
        verify(repository).save(any(KiwoomToken.class));
        server.verify();
    }

    @Test
    void getOrIssueAccessTokenReturnsStoredTokenWhenNotExpired() {
        when(repository.findTopByOrderByIdAsc()).thenReturn(Optional.of(
            KiwoomToken.builder()
                .id(1L)
                .tokenType("bearer")
                .token("stored-token")
                .expiresDt("20991107083713")
                .expiresAt(Instant.parse("2099-11-07T08:37:13Z"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build()
        ));

        KiwoomTokenProxyResponse response = service.getOrIssueAccessToken(null, null, null);

        assertThat(response.body()).isNotNull();
        assertThat(response.body().token()).isEqualTo("stored-token");
        assertThat(response.body().returnCode()).isEqualTo(0);
        assertThat(response.tokenSourceCode()).isEqualTo(TokenSourceCode.STORED);
    }
}
