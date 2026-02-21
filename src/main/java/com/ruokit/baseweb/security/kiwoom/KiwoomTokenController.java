package com.ruokit.baseweb.security.kiwoom;

import com.ruokit.baseweb.common.ApiResponse;
import com.ruokit.baseweb.security.kiwoom.dto.KiwoomTokenProxyResponse;
import com.ruokit.baseweb.security.kiwoom.dto.KiwoomTokenRequest;
import com.ruokit.baseweb.security.kiwoom.service.KiwoomTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
        @Valid @RequestBody KiwoomTokenRequest request,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
        @RequestHeader(value = "cont-yn", required = false) String contYn,
        @RequestHeader(value = "next-key", required = false) String nextKey
    ) {
        log.info("API 진입: POST /api/security/oauth2/token grantType={} hasAuthorization={}", request.grantType(), authorization != null && !authorization.isBlank());
        return ApiResponse.ok(kiwoomTokenService.issueAccessToken(request, authorization, contYn, nextKey));
    }
}
