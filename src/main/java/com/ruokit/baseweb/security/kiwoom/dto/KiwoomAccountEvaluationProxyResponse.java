package com.ruokit.baseweb.security.kiwoom.dto;

public record KiwoomAccountEvaluationProxyResponse(
    String apiId,
    String contYn,
    String nextKey,
    KiwoomAccountEvaluationResponse body
) {
}
