package com.ruokit.baseweb.security.kiwoom;

import com.ruokit.baseweb.common.ApiResponse;
import com.ruokit.baseweb.security.kiwoom.dto.KiwoomTokenProxyResponse;
import com.ruokit.baseweb.security.kiwoom.service.KiwoomTokenService;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/security")
public class KiwoomTokenController {

    private static final Logger log = LoggerFactory.getLogger(KiwoomTokenController.class);

    private final KiwoomTokenService kiwoomTokenService;

    public KiwoomTokenController(KiwoomTokenService kiwoomTokenService) {
        this.kiwoomTokenService = kiwoomTokenService;
    }

    @PostMapping("/oauth2/token")
    public ApiResponse<KiwoomTokenProxyResponse> issueAccessToken(
        @Parameter(hidden = true)
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
        @Parameter(hidden = true)
        @RequestHeader(value = "cont-yn", required = false) String contYn,
        @Parameter(hidden = true)
        @RequestHeader(value = "next-key", required = false) String nextKey
    ) {
        log.info("API 진입: POST /api/security/oauth2/token hasAuthorization={}", authorization != null && !authorization.isBlank());
        return ApiResponse.ok(kiwoomTokenService.issueAccessToken(authorization, contYn, nextKey));
    }

    @GetMapping("/oauth2/token")
    public ApiResponse<KiwoomTokenProxyResponse> getOrIssueAccessToken(
        @Parameter(hidden = true)
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
        @Parameter(hidden = true)
        @RequestHeader(value = "cont-yn", required = false) String contYn,
        @Parameter(hidden = true)
        @RequestHeader(value = "next-key", required = false) String nextKey
    ) {
        log.info("API 진입: GET /api/security/oauth2/token hasAuthorization={}", authorization != null && !authorization.isBlank());
        return ApiResponse.ok(kiwoomTokenService.getOrIssueAccessToken(authorization, contYn, nextKey));
    }
}
