package com.ruokit.baseweb.security.kiwoom.dto;

public record KiwoomTokenProxyResponse(
    String apiId,
    String contYn,
    String nextKey,
    KiwoomTokenResponse body,
    TokenSourceCode tokenSourceCode
) {
}
