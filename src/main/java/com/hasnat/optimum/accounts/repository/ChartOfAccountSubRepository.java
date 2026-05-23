package com.hasnat.optimum.accounts.repository;
import com.hasnat.optimum.accounts.entity.ChartOfAccountSub;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface ChartOfAccountSubRepository extends JpaRepository<ChartOfAccountSub, Long> {
    Optional<ChartOfAccountSub> findBySubAccountCode(String code);
    List<ChartOfAccountSub> findByOrganizationIdAndIsActiveTrueOrderBySubAccountName(Long orgId);
    List<ChartOfAccountSub> findByMainAccountId(Long mainAccountId);
    @Query("SELECT s FROM ChartOfAccountSub s WHERE s.organization.id=:orgId " +
           "AND TYPE(s)=:type AND s.isActive=true ORDER BY s.subAccountName")
    List<ChartOfAccountSub> findByOrgAndType(@Param("orgId") Long orgId,
                                              @Param("type") Class<?> type);
    @Query("SELECT s FROM ChartOfAccountSub s WHERE s.organization.id=:orgId " +
           "AND s.isActive=true " +
           "AND (LOWER(s.subAccountCode) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(s.subAccountName) LIKE LOWER(CONCAT('%',:q,'%')))")
    List<ChartOfAccountSub> search(@Param("orgId") Long orgId, @Param("q") String q);
    boolean existsBySubAccountCode(String code);
}
