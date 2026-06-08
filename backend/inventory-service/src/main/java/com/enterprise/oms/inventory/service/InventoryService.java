package com.enterprise.oms.inventory.service;

import com.enterprise.oms.inventory.dto.request.UpdateStockRequest;
import com.enterprise.oms.inventory.dto.response.InventoryResponse;
import com.enterprise.oms.inventory.dto.response.StockLevelResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InventoryService {

    InventoryResponse initializeInventory(String sku, Integer initialQuantity, String location);

    InventoryResponse checkStock(String sku);

    InventoryResponse updateStock(String sku, UpdateStockRequest request);

    Boolean reserveStock(String sku, Integer quantity);

    Boolean releaseStock(String sku, Integer quantity);

    Boolean confirmStockDeduction(String sku, Integer quantity);

    List<InventoryResponse> getAllInventory();

    Page<InventoryResponse> getAllInventory(Pageable pageable);

    List<InventoryResponse> getLowStockItems();

    List<InventoryResponse> getItemsNeedingReorder();

    StockLevelResponse getStockLevelStatus(String sku);

    void deleteInventory(String sku);
}