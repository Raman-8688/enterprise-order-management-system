package com.enterprise.oms.inventory.repository;

import com.enterprise.oms.inventory.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, String> {

    Optional<Inventory> findBySku(String sku);

    boolean existsBySku(String sku);

    List<Inventory> findByQuantityLessThanEqual(Integer threshold);

    List<Inventory> findByLocation(String location);

    @Query("SELECT i FROM Inventory i WHERE (i.quantity - i.reservedQuantity) <= i.reorderPoint")
    List<Inventory> findItemsNeedingReorder();

    @Query("SELECT i FROM Inventory i WHERE i.quantity < i.minimumStockLevel")
    List<Inventory> findLowStockItems();

    @Modifying
    @Transactional
    @Query("UPDATE Inventory i SET i.quantity = i.quantity + :quantity WHERE i.sku = :sku")
    int addStock(@Param("sku") String sku, @Param("quantity") Integer quantity);

    @Modifying
    @Transactional
    @Query("UPDATE Inventory i SET i.reservedQuantity = i.reservedQuantity + :quantity WHERE i.sku = :sku AND (i.quantity - i.reservedQuantity) >= :quantity")
    int reserveStock(@Param("sku") String sku, @Param("quantity") Integer quantity);

    @Modifying
    @Transactional
    @Query("UPDATE Inventory i SET i.reservedQuantity = i.reservedQuantity - :quantity WHERE i.sku = :sku AND i.reservedQuantity >= :quantity")
    int releaseStock(@Param("sku") String sku, @Param("quantity") Integer quantity);

    @Modifying
    @Transactional
    @Query("UPDATE Inventory i SET i.quantity = i.quantity - :quantity, i.reservedQuantity = i.reservedQuantity - :quantity WHERE i.sku = :sku AND i.reservedQuantity >= :quantity")
    int confirmStockDeduction(@Param("sku") String sku, @Param("quantity") Integer quantity);
}