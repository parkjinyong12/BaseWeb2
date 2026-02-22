package com.ruokit.baseweb.security.kiwoom.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KiwoomAccountEvaluationItem(
    @JsonProperty("stk_cd") String stockCode,
    @JsonProperty("stk_nm") String stockName,
    @JsonProperty("evltv_prft") String evaluationProfit,
    @JsonProperty("prft_rt") String profitRate,
    @JsonProperty("pur_pric") String purchasePrice,
    @JsonProperty("pred_close_pric") String previousClosePrice,
    @JsonProperty("rmnd_qty") String remainingQuantity,
    @JsonProperty("trde_able_qty") String tradeableQuantity,
    @JsonProperty("cur_prc") String currentPrice,
    @JsonProperty("pur_amt") String purchaseAmount,
    @JsonProperty("evlt_amt") String evaluationAmount,
    @JsonProperty("poss_rt") String holdingRatio,
    @JsonProperty("crd_tp") String creditType,
    @JsonProperty("crd_tp_nm") String creditTypeName,
    @JsonProperty("crd_loan_dt") String creditLoanDate
) {
}
