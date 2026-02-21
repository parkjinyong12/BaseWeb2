package com.example.baseweb.stock.service;

import com.example.baseweb.stock.config.KiwoomApiProperties;
import com.example.baseweb.stock.dto.KiwoomTokenProxyResponse;
import com.example.baseweb.stock.dto.KiwoomTokenRequest;
import com.example.baseweb.stock.dto.KiwoomTokenResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class KiwoomStockService {

    private final RestClient restClient;
    private final KiwoomApiProperties properties;

    public KiwoomStockService(RestClient.Builder restClientBuilder, KiwoomApiProperties properties) {
        this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
        this.properties = properties;
    }

    public KiwoomTokenProxyResponse issueAccessToken(
        KiwoomTokenRequest request,
        String authorization,
        String contYn,
        String nextKey
    ) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("grant_type", request.resolvedGrantType());
        payload.put("appkey", request.appkey());
        payload.put("secretkey", request.secretkey());

        ResponseEntity<KiwoomTokenResponse> response = restClient.post()
            .uri("/oauth2/token")
            .contentType(MediaType.APPLICATION_JSON)
            .header("api-id", properties.apiId())
            .headers(headers -> {
                if (StringUtils.hasText(authorization)) {
                    headers.set(HttpHeaders.AUTHORIZATION, authorization);
                }
                if (StringUtils.hasText(contYn)) {
                    headers.set("cont-yn", contYn);
                }
                if (StringUtils.hasText(nextKey)) {
                    headers.set("next-key", nextKey);
                }
            })
            .body(payload)
            .retrieve()
            .toEntity(KiwoomTokenResponse.class);

        HttpHeaders headers = response.getHeaders();
        return new KiwoomTokenProxyResponse(
            headers.getFirst("api-id"),
            headers.getFirst("cont-yn"),
            headers.getFirst("next-key"),
            response.getBody()
        );
    }
}
