package com.hasnat.optimum.inventory.repository;
import com.hasnat.optimum.inventory.entity.ItemBrand;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface ItemBrandRepository extends JpaRepository<ItemBrand, Long> {
    List<ItemBrand> findByOrganizationIdAndIsActiveTrueAndIsApprovedTrueOrderByBrandName(Long orgId);
    List<ItemBrand> findByOrganizationIdAndCountryOfOriginId(Long orgId, Long countryId);
    Optional<ItemBrand> findByOrganizationIdAndBrandCode(Long orgId, String code);
    boolean existsByOrganizationIdAndBrandCode(Long orgId, String code);
}
