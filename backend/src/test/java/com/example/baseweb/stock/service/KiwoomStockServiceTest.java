package com.example.baseweb.stock.service;

import com.example.baseweb.stock.config.KiwoomApiProperties;
import com.example.baseweb.stock.dto.KiwoomTokenProxyResponse;
import com.example.baseweb.stock.dto.KiwoomTokenRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

class KiwoomStockServiceTest {

    private MockRestServiceServer server;
    private KiwoomStockService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        this.server = MockRestServiceServer.bindTo(builder).build();
        this.service = new KiwoomStockService(builder, new KiwoomApiProperties("https://api.kiwoom.com", "au10001"));
    }

    @Test
    void issueAccessTokenCallsKiwoomApiAndMapsResponse() {
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
                  "expires_dt":"20241107083713",
                  "token_type":"bearer",
                  "token":"WQJCwyqInphKnR3bSRtB9NE1lv...",
                  "return_code":0,
                  "return_msg":"정상적으로 처리되었습니다"
                }
                """, MediaType.APPLICATION_JSON)
                .header("api-id", "au10001")
                .header("cont-yn", "N")
                .header("next-key", ""));

        KiwoomTokenProxyResponse response = service.issueAccessToken(
            new KiwoomTokenRequest("test-app", "test-secret", null),
            "Bearer seed-token",
            "Y",
            "NEXT123"
        );

        assertThat(response.apiId()).isEqualTo("au10001");
        assertThat(response.contYn()).isEqualTo("N");
        assertThat(response.body()).isNotNull();
        assertThat(response.body().tokenType()).isEqualTo("bearer");
        assertThat(response.body().token()).startsWith("WQJCwyq");
        server.verify();
    }
}
