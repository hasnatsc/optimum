package com.hasnat.optimum.organization.repository;
import com.hasnat.optimum.organization.entity.CostCenter;
import com.hasnat.optimum.organization.entity.CostCenter.CostCenterType;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface CostCenterRepository extends JpaRepository<CostCenter, Long> {
    Optional<CostCenter> findByCostCenterCode(String code);
    List<CostCenter> findByBusinessUnitIdAndIsActiveTrueOrderByCostCenterName(Long buId);
    List<CostCenter> findByBusinessUnitOrganizationIdAndIsActiveTrue(Long orgId);
    List<CostCenter> findByBusinessUnitOrganizationIdAndCostCenterType(Long orgId, CostCenterType type);
    List<CostCenter> findByParentCostCenterIdIsNullAndBusinessUnitOrganizationId(Long orgId);
    boolean existsByCostCenterCode(String code);
}
