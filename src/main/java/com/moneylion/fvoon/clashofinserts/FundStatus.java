package com.moneylion.fvoon.clashofinserts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
@AllArgsConstructor
public enum FundStatus {
    NONE(null, "", Arrays.asList(FundOptionTypes.ACH, FundOptionTypes.CREDIT_CARD, FundOptionTypes.DEBIT_CARD, FundOptionTypes.DEMAND_DEPOSIT_ACCOUNT, FundOptionTypes.ML_INVESTMENT, FundOptionTypes.ML_INVESTMENT_ESCROW)),
    INACTIVE("FUND003", "Inactive", Arrays.asList(FundOptionTypes.DEMAND_DEPOSIT_ACCOUNT, FundOptionTypes.ML_INVESTMENT, FundOptionTypes.ML_INVESTMENT_ESCROW)),
    ACH_HARD_RETURN("FUND005", "ACH has hard return", Collections.singletonList(FundOptionTypes.ACH)),
    UNVERIFIED("FUND007", "Fund option is not verified", Collections.singletonList(FundOptionTypes.ACH)),
    INELIGIBLE_WITHDRAWAL("FUND008", "Fund option is not eligible for withdrawal", Collections.singletonList(FundOptionTypes.ML_INVESTMENT)),
    SUSPECTED_FRAUD("FUND009", "Suspected fraud", Collections.singletonList(FundOptionTypes.ACH)),

    // To refactor description and name to include credit card
    DEBIT_CARD_EXPIRED("FUND001", "Debit card is expired", Arrays.asList(FundOptionTypes.DEBIT_CARD, FundOptionTypes.CREDIT_CARD)),
    DEBIT_CARD_REQUIRED_RETOKENIZATION("FUND002", "Debit card requires re-tokenization", Arrays.asList(FundOptionTypes.DEBIT_CARD, FundOptionTypes.CREDIT_CARD)),
    DEBIT_CARD_HARD_RETURN("FUND004", "Debit card has hard return", Arrays.asList(FundOptionTypes.DEBIT_CARD, FundOptionTypes.CREDIT_CARD)),
    DEBIT_CARD_ACCOUNT_CLOSED("FUND006", "Debit card account is closed", Arrays.asList(FundOptionTypes.DEBIT_CARD, FundOptionTypes.CREDIT_CARD)),
    ;

    private final String code;
    private final String description;
    private final List<FundOptionTypes> fundOptionType;

    public static FundStatus of(String code) {
        return Arrays.stream(values())
                .filter(status -> StringUtils.equalsIgnoreCase(status.getCode(), code))
                .findFirst()
                .orElse(null);
    }
}