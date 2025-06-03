package com.moneylion.fvoon.clashofinserts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundDetails {

    private boolean isDefault;

    /**
     * ACH details
     */
    private String bankAccountNumber;
    private String bankRoutingNumber;
    private String bankAccountType;
    private String bankName;

    /**
     * Card details
     */
    private String last4Digits;
    private String expiryDate;
    private String postCode;
    private String cardType;
    private String cardHolder;
    private Boolean isMoneylionCard;
    private String cardHash;
    private String cardBin;

    private String virtualExpiryDate; // DDMMYY

    private String logo;

    public void lookupAndSetBankAccountType(String bankAccountType) {
        BankAccountTypes type = BankAccountTypes.lookup(bankAccountType);
        this.bankAccountType = type.getValue();
    }
}