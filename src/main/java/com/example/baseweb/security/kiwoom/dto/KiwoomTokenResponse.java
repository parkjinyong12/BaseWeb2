package com.example.baseweb.security.kiwoom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KiwoomTokenResponse(
    String expiresDt,
    String tokenType,
    String token,
    Integer returnCode,
    String returnMsg
) {
    public KiwoomTokenResponse(
        @JsonProperty("expires_dt") String expiresDt,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("token") String token,
        @JsonProperty("return_code") Integer returnCode,
        @JsonProperty("return_msg") String returnMsg
    ) {
        this.expiresDt = expiresDt;
        this.tokenType = tokenType;
        this.token = token;
        this.returnCode = returnCode;
        this.returnMsg = returnMsg;
    }
}
