package com.hasnat.optimum.global.repository;
import com.hasnat.optimum.common.enums.DocumentType;
import com.hasnat.optimum.global.entity.InventoryTransaction;
import com.hasnat.optimum.global.entity.InventoryTransaction.MovementType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.*;
@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    List<InventoryTransaction> findByItemIdOrderByTransactionDateDesc(Long itemId);
    List<InventoryTransaction> findByLotIdOrderByTransactionDateDesc(Long lotId);
    List<InventoryTransaction> findByWarehouseIdAndTransactionDateBetween(Long warehouseId,
        LocalDate from, LocalDate to);
    List<InventoryTransaction> findByBusinessDocumentId(Long documentId);
    List<InventoryTransaction> findByItemIdAndMovementType(Long itemId, MovementType type);
    @Query("SELECT t FROM InventoryTransaction t " +
           "WHERE t.item.id=:itemId AND t.warehouse.id=:warehouseId " +
           "AND t.transactionDate BETWEEN :from AND :to ORDER BY t.transactionDate DESC")
    List<InventoryTransaction> findLedger(@Param("itemId") Long itemId,
        @Param("warehouseId") Long warehouseId,
        @Param("from") LocalDate from, @Param("to") LocalDate to);
    @Query("SELECT t FROM InventoryTransaction t " +
           "WHERE t.warehouse.businessUnit.organization.id=:orgId " +
           "AND t.transactionDate BETWEEN :from AND :to ORDER BY t.transactionDate DESC")
    Page<InventoryTransaction> findByOrgAndDateRange(@Param("orgId") Long orgId,
        @Param("from") LocalDate from, @Param("to") LocalDate to, Pageable pageable);
}
