package com.hasnat.optimum.commercial.repository;
import com.hasnat.optimum.commercial.entity.LcSettlement;
import com.hasnat.optimum.commercial.entity.LcSettlement.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.*;
@Repository
public interface LcSettlementRepository extends JpaRepository<LcSettlement, Long> {
    List<LcSettlement> findByLcId(Long lcId);
    List<LcSettlement> findByDocumentId(Long documentId);
    List<LcSettlement> findByLcIdAndStatus(Long lcId, SettlementStatus status);
    @Query("SELECT COALESCE(SUM(s.amountUsd), 0) FROM LcSettlement s " +
           "WHERE s.lc.id=:lcId AND s.status NOT IN ('REVERSED')")
    BigDecimal sumSettledUsd(@Param("lcId") Long lcId);
    @Query("SELECT COALESCE(SUM(s.amountBdt), 0) FROM LcSettlement s " +
           "WHERE s.lc.id=:lcId AND s.status NOT IN ('REVERSED')")
    BigDecimal sumSettledBdt(@Param("lcId") Long lcId);
}
