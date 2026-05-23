package com.hasnat.optimum.organization.repository;
import com.hasnat.optimum.common.enums.ItemType;
import com.hasnat.optimum.organization.entity.Warehouse;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Optional<Warehouse> findByWarehouseCode(String code);
    List<Warehouse> findByBusinessUnitOrganizationIdAndIsActiveTrueOrderByWarehouseName(Long orgId);
    List<Warehouse> findByBusinessUnitOrganizationIdAndItemType(Long orgId, ItemType itemType);
    List<Warehouse> findByBusinessUnitId(Long buId);
    boolean existsByWarehouseCode(String code);
}
