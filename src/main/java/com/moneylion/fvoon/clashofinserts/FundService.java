package com.moneylion.fvoon.clashofinserts;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FundService {
    private final FundOptionRepository fundOptionRepository;

    //    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Transactional
    public FundOption persistFundOption(FundOption fundOption) {
        return fundOptionRepository.saveAndFlush(fundOption);
    }
}
