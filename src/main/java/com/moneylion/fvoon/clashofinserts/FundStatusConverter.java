package com.moneylion.fvoon.clashofinserts;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;

import java.util.Optional;

@Converter
public class FundStatusConverter implements AttributeConverter<FundStatus, String> {

    @Override
    public String convertToDatabaseColumn(FundStatus fundStatus) {
        if (ObjectUtils.isEmpty(fundStatus)) {
            return null;
        }
        return fundStatus.getCode();
    }

    @Override
    public FundStatus convertToEntityAttribute(String code) {
        if (StringUtils.isEmpty(code)) {
            return null;
        }

        return Optional.ofNullable(FundStatus.of(code))
                .orElseThrow(IllegalArgumentException::new);
    }
}