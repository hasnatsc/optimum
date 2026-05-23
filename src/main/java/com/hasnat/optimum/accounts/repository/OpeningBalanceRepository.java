package com.hasnat.optimum.accounts.repository;
import com.hasnat.optimum.accounts.entity.OpeningBalance;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface OpeningBalanceRepository extends JpaRepository<OpeningBalance, Long> {
    List<OpeningBalance> findByOrganizationIdAndAccountingPeriodId(Long orgId, Long periodId);
    Optional<OpeningBalance> findByAccountIdAndAccountingPeriodId(Long accountId, Long periodId);
    List<OpeningBalance> findByOrganizationIdAndIsPostedTrue(Long orgId);
    boolean existsByAccountIdAndAccountingPeriodId(Long accountId, Long periodId);
}
