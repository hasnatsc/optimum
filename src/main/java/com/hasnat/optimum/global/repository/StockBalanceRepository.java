package com.hasnat.optimum.global.repository;
import com.hasnat.optimum.global.entity.StockBalance;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface StockBalanceRepository extends JpaRepository<StockBalance, Long> {
    Optional<StockBalance> findByItemIdAndWarehouseIdAndLotId(Long itemId, Long warehouseId, Long lotId);
    List<StockBalance> findByItemId(Long itemId);
    List<StockBalance> findByWarehouseId(Long warehouseId);
    List<StockBalance> findByItemIdAndWarehouseId(Long itemId, Long warehouseId);
    List<StockBalance> findByLotId(Long lotId);
    @Query("SELECT SUM(sb.quantity) FROM StockBalance sb WHERE sb.item.id=:itemId")
    Optional<java.math.BigDecimal> sumQuantityByItem(@Param("itemId") Long itemId);
    @Query("SELECT SUM(sb.quantity) FROM StockBalance sb " +
           "WHERE sb.item.id=:itemId AND sb.warehouse.id=:warehouseId")
    Optional<java.math.BigDecimal> sumQuantityByItemAndWarehouse(
        @Param("itemId") Long itemId, @Param("warehouseId") Long warehouseId);
    @Query("SELECT sb FROM StockBalance sb WHERE sb.warehouse.businessUnit.organization.id=:orgId " +
           "AND sb.quantity > 0 ORDER BY sb.item.itemName")
    List<StockBalance> findPositiveStockByOrg(@Param("orgId") Long orgId);
}
