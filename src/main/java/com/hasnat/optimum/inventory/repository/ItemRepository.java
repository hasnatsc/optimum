package com.hasnat.optimum.inventory.repository;
import com.hasnat.optimum.common.enums.ItemType;
import com.hasnat.optimum.inventory.entity.Item;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {
    Optional<Item> findByOrganizationIdAndItemCode(Long orgId, String code);
    Optional<Item> findByOrganizationIdAndItemName(Long orgId, String name);
    Optional<Item> findBySku(String sku);
    Optional<Item> findByBarcode(String barcode);
    List<Item> findByOrganizationIdAndIsActiveTrueOrderByItemName(Long orgId);
    List<Item> findByOrganizationIdAndItemType(Long orgId, ItemType type);
    List<Item> findByOrganizationIdAndCategoryId(Long orgId, Long categoryId);
    List<Item> findByOrganizationIdAndIsApprovedFalse(Long orgId);
    @Query("SELECT i FROM Item i WHERE i.organization.id=:orgId AND i.isActive=true " +
           "AND (LOWER(i.itemCode) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(i.itemName) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(i.sku) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<Item> search(@Param("orgId") Long orgId, @Param("q") String q, Pageable pageable);
    @Query("SELECT i FROM Item i WHERE i.organization.id=:orgId " +
           "AND i.isActive=true AND i.minimumStock IS NOT NULL " +
           "AND EXISTS (SELECT sb FROM StockBalance sb WHERE sb.item=i " +
           "AND sb.quantity <= i.reorderLevel)")
    List<Item> findBelowReorderLevel(@Param("orgId") Long orgId);
    boolean existsByOrganizationIdAndItemCode(Long orgId, String code);
    boolean existsByOrganizationIdAndItemName(Long orgId, String name);
}
