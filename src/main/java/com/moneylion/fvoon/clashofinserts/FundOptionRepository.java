package com.moneylion.fvoon.clashofinserts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FundOptionRepository extends JpaRepository<FundOption, Long> {

    @Query("""
        SELECT fo
        FROM FundOption fo
        WHERE fo.userId = :userId
        GROUP BY fo
        """)
    List<FundOption> findAllByUserId(@Param("userId") String userId);
}
