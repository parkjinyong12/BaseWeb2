package com.ruokit.baseweb.security.kiwoom.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KiwoomAccountEvaluationResponse(
    @JsonProperty("tot_pur_amt") String totalPurchaseAmount,
    @JsonProperty("tot_evlt_amt") String totalEvaluationAmount,
    @JsonProperty("tot_evlt_pl") String totalEvaluationProfitLoss,
    @JsonProperty("tot_prft_rt") String totalProfitRate,
    @JsonProperty("prsm_dpst_aset_amt") String estimatedDepositAssetAmount,
    @JsonProperty("tot_loan_amt") String totalLoanAmount,
    @JsonProperty("tot_crd_loan_amt") String totalCreditLoanAmount,
    @JsonProperty("tot_crd_ls_amt") String totalCreditShortAmount,
    @JsonProperty("acnt_evlt_remn_indv_tot") List<KiwoomAccountEvaluationItem> accountEvaluationItems,
    @JsonProperty("return_code") Integer returnCode,
    @JsonProperty("return_msg") String returnMessage
) {
}
