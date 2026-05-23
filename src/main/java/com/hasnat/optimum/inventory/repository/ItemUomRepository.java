package com.hasnat.optimum.inventory.repository;
import com.hasnat.optimum.inventory.entity.ItemUom;
import com.hasnat.optimum.inventory.entity.ItemUom.UomCategory;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface ItemUomRepository extends JpaRepository<ItemUom, Long> {
    Optional<ItemUom> findByOrganizationIdAndCode(Long orgId, String code);
    List<ItemUom> findByOrganizationIdAndActiveTrueOrderByName(Long orgId);
    List<ItemUom> findByOrganizationIdAndCategory(Long orgId, UomCategory category);
    Optional<ItemUom> findByOrganizationIdAndIsBaseUnitTrueAndCategory(Long orgId, UomCategory cat);
    boolean existsByOrganizationIdAndCode(Long orgId, String code);
}
