package com.example.baseweb.stock;

import com.example.baseweb.common.ApiResponse;
import com.example.baseweb.stock.dto.KiwoomTokenProxyResponse;
import com.example.baseweb.stock.dto.KiwoomTokenRequest;
import com.example.baseweb.stock.service.KiwoomStockService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final KiwoomStockService kiwoomStockService;

    public StockController(KiwoomStockService kiwoomStockService) {
        this.kiwoomStockService = kiwoomStockService;
    }

    @PostMapping("/oauth2/token")
    public ApiResponse<KiwoomTokenProxyResponse> issueAccessToken(
        @Valid @RequestBody KiwoomTokenRequest request,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
        @RequestHeader(value = "cont-yn", required = false) String contYn,
        @RequestHeader(value = "next-key", required = false) String nextKey
    ) {
        return ApiResponse.ok(kiwoomStockService.issueAccessToken(request, authorization, contYn, nextKey));
    }
}
