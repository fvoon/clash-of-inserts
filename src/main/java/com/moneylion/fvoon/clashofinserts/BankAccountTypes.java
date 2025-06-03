package com.moneylion.fvoon.clashofinserts;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
@AllArgsConstructor
public enum BankAccountTypes {

    DEFAULT(null, Arrays.asList("", null)),
    CHECKING("C", Arrays.asList("Personal Checking", "Checking Account", "C")),
    SAVINGS("S", Collections.singletonList("S"));

    private final String value;
    private final List<String> acceptedValues;

    public static BankAccountTypes lookup(String bankAccountType) {

        if(bankAccountType == null) {
            return DEFAULT;
        }

        return Arrays.stream(BankAccountTypes.values())
                .filter(type -> type.getAcceptedValues().contains(bankAccountType.trim()))
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Unrecognized Bank Account Type"));
    }
}