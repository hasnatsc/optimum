package com.hasnat.optimum.inventory.repository;
import com.hasnat.optimum.inventory.entity.ItemModel;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface ItemModelRepository extends JpaRepository<ItemModel, Long> {
    List<ItemModel> findByOrganizationIdAndIsActiveTrueAndIsApprovedTrue(Long orgId);
    List<ItemModel> findByBrandIdAndIsActiveTrueOrderByModelName(Long brandId);
    Optional<ItemModel> findByOrganizationIdAndBrandIdAndModelCode(Long orgId, Long brandId, String code);
    boolean existsByOrganizationIdAndBrandIdAndModelCode(Long orgId, Long brandId, String code);
}
