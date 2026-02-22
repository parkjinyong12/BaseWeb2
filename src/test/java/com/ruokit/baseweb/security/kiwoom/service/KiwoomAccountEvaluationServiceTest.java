package com.ruokit.baseweb.security.kiwoom.service;

import com.ruokit.baseweb.security.kiwoom.config.KiwoomApiProperties;
import com.ruokit.baseweb.security.kiwoom.dto.KiwoomAccountEvaluationProxyResponse;
import com.ruokit.baseweb.security.kiwoom.dto.KiwoomAccountEvaluationRequest;
import com.ruokit.baseweb.security.kiwoom.dto.KiwoomTokenProxyResponse;
import com.ruokit.baseweb.security.kiwoom.dto.KiwoomTokenResponse;
import com.ruokit.baseweb.security.kiwoom.dto.TokenSourceCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KiwoomAccountEvaluationServiceTest {

    private MockRestServiceServer server;
    private KiwoomTokenService kiwoomTokenService;
    private KiwoomAccountEvaluationService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        this.server = MockRestServiceServer.bindTo(builder).build();
        this.kiwoomTokenService = mock(KiwoomTokenService.class);
        this.service = new KiwoomAccountEvaluationService(
            builder,
            new KiwoomApiProperties("https://api.kiwoom.com", "au10001", "kt00018", "test-app", "test-secret", "client_credentials"),
            kiwoomTokenService
        );
    }

    @Test
    void getAccountEvaluationCallsKiwoomApiAndReturnsHeadersAndBody() {
        when(kiwoomTokenService.getOrIssueAccessToken("Bearer initial", null, null)).thenReturn(
            new KiwoomTokenProxyResponse(
                "au10001",
                null,
                null,
                new KiwoomTokenResponse("20991107083713", "bearer", "issued-token", 0, "ok"),
                TokenSourceCode.ISSUED
            )
        );

        server.expect(requestTo("https://api.kiwoom.com/api/dostk/acnt"))
            .andExpect(method(POST))
            .andExpect(header("api-id", "kt00018"))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer issued-token"))
            .andExpect(header("cont-yn", "Y"))
            .andExpect(header("next-key", "NEXT001"))
            .andExpect(content().json("""
                {
                  "qry_tp":"1",
                  "dmst_stex_tp":"KRX"
                }
                """))
            .andRespond(withSuccess("""
                {
                  "tot_pur_amt":"000000017598258",
                  "tot_evlt_amt":"000000025789890",
                  "tot_evlt_pl":"000000008138825",
                  "tot_prft_rt":"46.25",
                  "acnt_evlt_remn_indv_tot":[
                    {
                      "stk_cd":"A005930",
                      "stk_nm":"삼성전자",
                      "evltv_prft":"-00000000196888"
                    }
                  ],
                  "return_code":0,
                  "return_msg":"조회가 완료되었습니다"
                }
                """, MediaType.APPLICATION_JSON)
                .header("api-id", "kt00018")
                .header("cont-yn", "N")
                .header("next-key", ""));

        KiwoomAccountEvaluationProxyResponse response = service.getAccountEvaluation(
            new KiwoomAccountEvaluationRequest("1", "KRX"),
            "Bearer initial",
            "Y",
            "NEXT001"
        );

        assertThat(response.apiId()).isEqualTo("kt00018");
        assertThat(response.contYn()).isEqualTo("N");
        assertThat(response.body()).isNotNull();
        assertThat(response.body().totalProfitRate()).isEqualTo("46.25");
        assertThat(response.body().accountEvaluationItems()).hasSize(1);
        assertThat(response.body().accountEvaluationItems().get(0).stockCode()).isEqualTo("A005930");
        server.verify();
    }
}
