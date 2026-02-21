package com.example.baseweb.stock.dto;

public record KiwoomTokenProxyResponse(
    String apiId,
    String contYn,
    String nextKey,
    KiwoomTokenResponse body
) {
}
