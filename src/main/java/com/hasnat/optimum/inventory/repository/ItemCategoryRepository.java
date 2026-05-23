package com.hasnat.optimum.inventory.repository;
import com.hasnat.optimum.common.enums.ItemType;
import com.hasnat.optimum.inventory.entity.ItemCategory;
import com.hasnat.optimum.inventory.entity.ItemCategory.LayerType;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface ItemCategoryRepository extends JpaRepository<ItemCategory, Long> {
    Optional<ItemCategory> findByCategoryCode(String code);
    List<ItemCategory> findByOrganizationIdAndIsActiveTrueOrderByCategoryName(Long orgId);
    List<ItemCategory> findByOrganizationIdAndItemType(Long orgId, ItemType type);
    List<ItemCategory> findByOrganizationIdAndLayerType(Long orgId, LayerType layerType);
    List<ItemCategory> findByParentCategoryIdIsNullAndOrganizationId(Long orgId);
    List<ItemCategory> findByParentCategoryId(Long parentId);
    boolean existsByOrganizationIdAndCategoryCode(Long orgId, String code);
}
