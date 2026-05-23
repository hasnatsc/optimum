package com.hasnat.optimum.global.repository;
import com.hasnat.optimum.common.enums.ItemType;
import com.hasnat.optimum.global.entity.InventoryLot;
import com.hasnat.optimum.global.entity.InventoryLot.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.*;
@Repository
public interface InventoryLotRepository extends JpaRepository<InventoryLot, Long>,
        JpaSpecificationExecutor<InventoryLot> {
    Optional<InventoryLot> findByOrganizationIdAndLotNumber(Long orgId, String lotNumber);
    List<InventoryLot> findByItemIdAndDeletedFalse(Long itemId);
    List<InventoryLot> findByItemIdAndStatusAndDeletedFalse(Long itemId, LotStatus status);
    List<InventoryLot> findByOrganizationIdAndItemTypeAndDeletedFalse(Long orgId, ItemType type);
    List<InventoryLot> findByOrganizationIdAndStatusAndDeletedFalse(Long orgId, LotStatus status);
    List<InventoryLot> findBySupplierIdAndDeletedFalse(Long supplierId);
    @Query("SELECT l FROM InventoryLot l WHERE l.organization.id=:orgId " +
           "AND l.deleted=false AND l.status='AVAILABLE' " +
           "AND l.expiryDate IS NOT NULL AND l.expiryDate <= :threshold")
    List<InventoryLot> findExpiringSoon(@Param("orgId") Long orgId,
                                        @Param("threshold") LocalDate threshold);
    @Query("SELECT l FROM InventoryLot l WHERE l.organization.id=:orgId " +
           "AND l.deleted=false " +
           "AND (LOWER(l.lotNumber) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(l.batchNo) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<InventoryLot> search(@Param("orgId") Long orgId, @Param("q") String q, Pageable pageable);
    boolean existsByOrganizationIdAndLotNumber(Long orgId, String lotNumber);
}
