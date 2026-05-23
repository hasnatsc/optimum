package com.hasnat.optimum.accounts.repository;
import com.hasnat.optimum.accounts.entity.ChartOfAccount;
import com.hasnat.optimum.common.enums.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface ChartOfAccountRepository extends JpaRepository<ChartOfAccount, Long> {
    Optional<ChartOfAccount> findByAccountCode(String code);
    List<ChartOfAccount> findByOrganizationIdAndIsActiveTrueOrderByAccountCode(Long orgId);
    List<ChartOfAccount> findByOrganizationIdAndAccountType(Long orgId, AccountType type);
    List<ChartOfAccount> findByOrganizationIdAndAccountNature(Long orgId, AccountNature nature);
    List<ChartOfAccount> findByOrganizationIdAndIsControlAccountTrue(Long orgId);
    List<ChartOfAccount> findByOrganizationIdAndAllowManualEntryTrue(Long orgId);
    List<ChartOfAccount> findByOrganizationIdAndLevel(Long orgId, int level);
    List<ChartOfAccount> findByParentAccountIdIsNullAndOrganizationId(Long orgId);
    List<ChartOfAccount> findByParentAccountId(Long parentId);
    @Query("SELECT a FROM ChartOfAccount a WHERE a.organization.id=:orgId " +
           "AND a.isActive=true " +
           "AND (LOWER(a.accountCode) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(a.accountName) LIKE LOWER(CONCAT('%',:q,'%')))")
    List<ChartOfAccount> search(@Param("orgId") Long orgId, @Param("q") String q);
    boolean existsByOrganizationIdAndAccountCode(Long orgId, String code);
}
