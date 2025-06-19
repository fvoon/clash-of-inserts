package com.moneylion.fvoon.clashofinserts;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FundACHService {
    private final FundService fundService;

    @Transactional(isolation = Isolation.SERIALIZABLE)
//    @Transactional
    public void addACHWithFraudCheck(FundOption fundOption) {
        try {
            fundService.persistFundOption(fundOption);
        } catch (Exception e) {
            System.out.println("Failed to persist fund option: " + e);
            throw e;
        }
    }

    /**
     * no transactional at fund ach service, fund service has retry and serializable isolation + requires new
     * Inserted FundOptions: 130
     * Total time (ms): 4001
     * Avg time per thread (ms): 2132
     *
     * transactional serializable isolation at fund ach service, fund service has retry and serializable isolation + requires new
     * Inserted FundOptions: 134
     * Total time (ms): 3831
     * Avg time per thread (ms): 2057
     *
     * transactional at fund ach service, fund service has retry and serializable isolation + requires new
     * Inserted FundOptions: 129
     * Total time (ms): 4394
     * Avg time per thread (ms): 2175
     */
}
