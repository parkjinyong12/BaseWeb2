package com.ruokit.baseweb.security.kiwoom;

import com.ruokit.baseweb.common.ApiResponse;
import com.ruokit.baseweb.security.kiwoom.dto.KiwoomAccountEvaluationProxyResponse;
import com.ruokit.baseweb.security.kiwoom.dto.KiwoomAccountEvaluationRequest;
import com.ruokit.baseweb.security.kiwoom.service.KiwoomAccountEvaluationService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/security/kiwoom/accounts")
public class KiwoomAccountEvaluationController {

    private static final Logger log = LoggerFactory.getLogger(KiwoomAccountEvaluationController.class);

    private final KiwoomAccountEvaluationService accountEvaluationService;

    public KiwoomAccountEvaluationController(KiwoomAccountEvaluationService accountEvaluationService) {
        this.accountEvaluationService = accountEvaluationService;
    }

    @PostMapping("/evaluation")
    public ApiResponse<KiwoomAccountEvaluationProxyResponse> getAccountEvaluation(
        @Valid @RequestBody KiwoomAccountEvaluationRequest request,
        @Parameter(hidden = true)
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
        @Parameter(hidden = true)
        @RequestHeader(value = "cont-yn", required = false) String contYn,
        @Parameter(hidden = true)
        @RequestHeader(value = "next-key", required = false) String nextKey
    ) {
        log.info("API 진입: POST /api/security/kiwoom/accounts/evaluation hasAuthorization={} qryTp={} dmstStexTp={}",
            authorization != null && !authorization.isBlank(), request.qryTp(), request.dmstStexTp());

        return ApiResponse.ok(accountEvaluationService.getAccountEvaluation(request, authorization, contYn, nextKey));
    }
}
