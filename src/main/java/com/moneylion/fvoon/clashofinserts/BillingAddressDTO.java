package com.moneylion.fvoon.clashofinserts;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BillingAddressDTO {
    private Long billingAddressId;
    private String addrLine1;
    private String addrLine2;
    private String city;
    private String postalCode;
    private String state;
}