package com.hasnat.optimum.accounts.repository;
import com.hasnat.optimum.accounts.entity.AccountingPeriod;
import com.hasnat.optimum.accounts.entity.AccountingPeriod.PeriodType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.*;
@Repository
public interface AccountingPeriodRepository extends JpaRepository<AccountingPeriod, Long> {
    Optional<AccountingPeriod> findByPeriodName(String name);
    List<AccountingPeriod> findByOrganizationIdOrderByStartDateDesc(Long orgId);
    List<AccountingPeriod> findByOrganizationIdAndFiscalYear(Long orgId, int fiscalYear);
    List<AccountingPeriod> findByOrganizationIdAndIsActiveTrueAndIsClosedFalse(Long orgId);
    List<AccountingPeriod> findByOrganizationIdAndPeriodType(Long orgId, PeriodType type);
    @Query("SELECT p FROM AccountingPeriod p WHERE p.organization.id=:orgId " +
           "AND p.startDate <= :date AND p.endDate >= :date AND p.isActive=true AND p.isClosed=false")
    Optional<AccountingPeriod> findOpenPeriodForDate(@Param("orgId") Long orgId,
                                                     @Param("date") LocalDate date);
}
