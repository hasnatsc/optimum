package com.hasnat.optimum.commercial.repository;
import com.hasnat.optimum.commercial.entity.HsCode;
import com.hasnat.optimum.commercial.entity.HsCode.HsType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface HsCodeRepository extends JpaRepository<HsCode, Long> {
    Optional<HsCode> findByOrganizationIdAndHsCode(Long orgId, String hsCode);
    List<HsCode> findByOrganizationIdAndIsActiveTrueOrderByHsCode(Long orgId);
    List<HsCode> findByOrganizationIdAndHsType(Long orgId, HsType type);
    List<HsCode> findByOrganizationIdAndIsBondedAllowedTrue(Long orgId);
    @Query("SELECT h FROM HsCode h WHERE h.organization.id=:orgId AND h.isActive=true " +
           "AND (LOWER(h.hsCode) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(h.description) LIKE LOWER(CONCAT('%',:q,'%')))")
    List<HsCode> search(@Param("orgId") Long orgId, @Param("q") String q);
    boolean existsByOrganizationIdAndHsCode(Long orgId, String hsCode);
}
