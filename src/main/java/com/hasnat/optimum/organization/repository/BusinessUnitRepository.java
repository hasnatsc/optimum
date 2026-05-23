package com.hasnat.optimum.organization.repository;
import com.hasnat.optimum.organization.entity.BusinessUnit;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface BusinessUnitRepository extends JpaRepository<BusinessUnit, Long> {
    Optional<BusinessUnit> findByCode(String code);
    List<BusinessUnit> findByOrganizationIdAndIsActiveTrueOrderByName(Long orgId);
    List<BusinessUnit> findByOrganizationId(Long orgId);
    boolean existsByOrganizationIdAndCode(Long orgId, String code);
}
