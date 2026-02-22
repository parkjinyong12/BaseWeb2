package com.ruokit.baseweb.security.kiwoom.service;

import com.ruokit.baseweb.security.kiwoom.config.KiwoomApiProperties;
import com.ruokit.baseweb.security.kiwoom.dto.KiwoomAccountEvaluationProxyResponse;
import com.ruokit.baseweb.security.kiwoom.dto.KiwoomAccountEvaluationRequest;
import com.ruokit.baseweb.security.kiwoom.dto.KiwoomAccountEvaluationResponse;
import com.ruokit.baseweb.security.kiwoom.dto.KiwoomTokenProxyResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
public class KiwoomAccountEvaluationService {

    private final RestClient restClient;
    private final KiwoomApiProperties properties;
    private final KiwoomTokenService kiwoomTokenService;

    public KiwoomAccountEvaluationService(
        RestClient.Builder restClientBuilder,
        KiwoomApiProperties properties,
        KiwoomTokenService kiwoomTokenService
    ) {
        this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
        this.properties = properties;
        this.kiwoomTokenService = kiwoomTokenService;
    }

    public KiwoomAccountEvaluationProxyResponse getAccountEvaluation(
        KiwoomAccountEvaluationRequest request,
        String authorization,
        String contYn,
        String nextKey
    ) {
        KiwoomTokenProxyResponse tokenResponse = kiwoomTokenService.getOrIssueAccessToken(authorization, null, null);
        String bearerToken = toBearerToken(tokenResponse);

        ResponseEntity<KiwoomAccountEvaluationResponse> response = restClient.post()
            .uri("/api/dostk/acnt")
            .contentType(MediaType.APPLICATION_JSON)
            .header("api-id", properties.accountEvaluationApiId())
            .header(HttpHeaders.AUTHORIZATION, bearerToken)
            .headers(headers -> {
                if (StringUtils.hasText(contYn)) {
                    headers.set("cont-yn", contYn);
                }
                if (StringUtils.hasText(nextKey)) {
                    headers.set("next-key", nextKey);
                }
            })
            .body(request)
            .retrieve()
            .toEntity(KiwoomAccountEvaluationResponse.class);

        HttpHeaders headers = response.getHeaders();
        return new KiwoomAccountEvaluationProxyResponse(
            headers.getFirst("api-id"),
            headers.getFirst("cont-yn"),
            headers.getFirst("next-key"),
            response.getBody()
        );
    }

    private String toBearerToken(KiwoomTokenProxyResponse tokenResponse) {
        String tokenType = tokenResponse.body().tokenType();
        String token = tokenResponse.body().token();

        String normalizedTokenType = tokenType.substring(0, 1).toUpperCase() + tokenType.substring(1).toLowerCase();
        return normalizedTokenType + " " + token;
    }
}
