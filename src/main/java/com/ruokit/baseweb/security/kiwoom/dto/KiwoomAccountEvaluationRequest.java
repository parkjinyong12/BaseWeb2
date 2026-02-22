package com.ruokit.baseweb.security.kiwoom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record KiwoomAccountEvaluationRequest(
    @NotBlank
    @Pattern(regexp = "[12]", message = "qryTp must be 1 or 2")
    @JsonProperty("qry_tp")
    String qryTp,

    @NotBlank
    @Pattern(regexp = "KRX|NXT", message = "dmstStexTp must be KRX or NXT")
    @JsonProperty("dmst_stex_tp")
    String dmstStexTp
) {
}
