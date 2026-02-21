package com.ruokit.baseweb.security.kiwoom.dto;

public record KiwoomTokenGetResponse(
    String token,
    TokenSourceCode tokenSourceCode
) {
}
