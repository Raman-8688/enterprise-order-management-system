package com.enterprise.oms.inventory.service.impl;

import com.enterprise.oms.inventory.dto.request.UpdateStockRequest;
import com.enterprise.oms.inventory.dto.response.InventoryResponse;
import com.enterprise.oms.inventory.dto.response.StockLevelResponse;
import com.enterprise.oms.inventory.exception.InsufficientStockException;
import com.enterprise.oms.inventory.exception.InventoryNotFoundException;
import com.enterprise.oms.inventory.model.Inventory;
import com.enterprise.oms.inventory.repository.InventoryRepository;
import com.enterprise.oms.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional
    public InventoryResponse initializeInventory(String sku, Integer initialQuantity, String location) {
        log.info("Initializing inventory for SKU: {} with quantity: {}", sku, initialQuantity);

        if (inventoryRepository.existsBySku(sku)) {
            log.warn("Inventory already exists for SKU: {}", sku);
            return checkStock(sku);
        }

        Inventory inventory = Inventory.builder()
                .sku(sku)
                .quantity(initialQuantity != null ? initialQuantity : 0)
                .reservedQuantity(0)
                .minimumStockLevel(5)      //  Explicit default
                .maximumStockLevel(1000)   //  Explicit default
                .reorderPoint(10)          //  Explicit default
                .location(location != null ? location : "WAREHOUSE_MAIN")
                .lastRestockedAt(LocalDateTime.now())
                .lastRestockedBy("SYSTEM")
                .build();

        Inventory saved = inventoryRepository.save(inventory);
        log.info("Inventory initialized for SKU: {} with ID: {}", sku, saved.getId());

        return convertToResponse(saved);
    }

    @Override
    public InventoryResponse checkStock(String sku) {
        log.debug("Checking stock for SKU: {}", sku);

        Inventory inventory = inventoryRepository.findBySku(sku)
                .orElseThrow(() -> new InventoryNotFoundException(sku));

        return convertToResponse(inventory);
    }

    @Override
    @Transactional
    public InventoryResponse updateStock(String sku, UpdateStockRequest request) {
        log.info("Updating stock for SKU: {} with operation: {} and quantity: {}",
                sku, request.getOperation(), request.getQuantityChange());

        Inventory inventory = inventoryRepository.findBySku(sku)
                .orElseThrow(() -> new InventoryNotFoundException(sku));

        //  Ensure defaults exist
        if (inventory.getMinimumStockLevel() == null) inventory.setMinimumStockLevel(5);
        if (inventory.getMaximumStockLevel() == null) inventory.setMaximumStockLevel(1000);
        if (inventory.getReorderPoint() == null) inventory.setReorderPoint(10);

        Integer currentQuantity = inventory.getQuantity() != null ? inventory.getQuantity() : 0;

        switch (request.getOperation().toUpperCase()) {
            case "ADD":
                inventory.setQuantity(currentQuantity + request.getQuantityChange());
                inventory.setLastRestockedAt(LocalDateTime.now());
                inventory.setLastRestockedBy(request.getUpdatedBy());
                log.info("Added {} units to SKU: {}. New quantity: {}",
                        request.getQuantityChange(), sku, inventory.getQuantity());
                break;

            case "REMOVE":
                int newQuantity = currentQuantity - request.getQuantityChange();
                if (newQuantity < 0) {
                    throw new InsufficientStockException(sku, request.getQuantityChange(),
                            inventory.getAvailableQuantity());
                }
                inventory.setQuantity(newQuantity);
                log.info("Removed {} units from SKU: {}. New quantity: {}",
                        request.getQuantityChange(), sku, inventory.getQuantity());
                break;

            case "SET":
                inventory.setQuantity(request.getQuantityChange());
                log.info("Set quantity for SKU: {} to: {}", sku, request.getQuantityChange());
                break;

            default:
                throw new IllegalArgumentException("Invalid operation: " + request.getOperation());
        }

        Inventory updated = inventoryRepository.save(inventory);
        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public Boolean reserveStock(String sku, Integer quantity) {
        log.info("Reserving {} units for SKU: {}", quantity, sku);

        Inventory inventory = inventoryRepository.findBySku(sku)
                .orElseThrow(() -> new InventoryNotFoundException(sku));

        if (!inventory.hasSufficientStock(quantity)) {
            log.warn("Insufficient stock for SKU: {}. Available: {}, Requested: {}",
                    sku, inventory.getAvailableQuantity(), quantity);
            throw new InsufficientStockException(sku, quantity, inventory.getAvailableQuantity());
        }

        int updated = inventoryRepository.reserveStock(sku, quantity);

        if (updated > 0) {
            log.info("Successfully reserved {} units for SKU: {}", quantity, sku);
            return true;
        }

        log.error("Failed to reserve stock for SKU: {}", sku);
        return false;
    }

    @Override
    @Transactional
    public Boolean releaseStock(String sku, Integer quantity) {
        log.info("Releasing {} units for SKU: {}", quantity, sku);

        Inventory inventory = inventoryRepository.findBySku(sku)
                .orElseThrow(() -> new InventoryNotFoundException(sku));

        Integer reservedQuantity = inventory.getReservedQuantity() != null ? inventory.getReservedQuantity() : 0;

        if (reservedQuantity < quantity) {
            log.warn("Cannot release {} units for SKU: {}. Only {} reserved",
                    quantity, sku, reservedQuantity);
            return false;
        }

        int updated = inventoryRepository.releaseStock(sku, quantity);

        if (updated > 0) {
            log.info("Successfully released {} units for SKU: {}", quantity, sku);
            return true;
        }

        log.error("Failed to release stock for SKU: {}", sku);
        return false;
    }

    @Override
    @Transactional
    public Boolean confirmStockDeduction(String sku, Integer quantity) {
        log.info("Confirming stock deduction of {} units for SKU: {}", quantity, sku);

        int updated = inventoryRepository.confirmStockDeduction(sku, quantity);

        if (updated > 0) {
            log.info("Successfully deducted {} units from SKU: {}", quantity, sku);
            return true;
        }

        log.error("Failed to deduct stock for SKU: {}", sku);
        return false;
    }

    @Override
    public List<InventoryResponse> getAllInventory() {
        log.debug("Fetching all inventory");

        List<Inventory> inventories = inventoryRepository.findAll();
        return inventories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<InventoryResponse> getAllInventory(Pageable pageable) {
        log.debug("Fetching all inventory with pagination");

        Page<Inventory> inventoryPage = inventoryRepository.findAll(pageable);
        return inventoryPage.map(this::convertToResponse);
    }

    @Override
    public List<InventoryResponse> getLowStockItems() {
        log.debug("Fetching low stock items");

        List<Inventory> lowStock = inventoryRepository.findLowStockItems();
        return lowStock.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryResponse> getItemsNeedingReorder() {
        log.debug("Fetching items needing reorder");

        List<Inventory> needsReorder = inventoryRepository.findItemsNeedingReorder();
        return needsReorder.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public StockLevelResponse getStockLevelStatus(String sku) {
        log.debug("Getting stock level status for SKU: {}", sku);

        Inventory inventory = inventoryRepository.findBySku(sku)
                .orElseThrow(() -> new InventoryNotFoundException(sku));

        // Safe null handling
        Integer currentStock = inventory.getQuantity() != null ? inventory.getQuantity() : 0;
        Integer reservedStock = inventory.getReservedQuantity() != null ? inventory.getReservedQuantity() : 0;
        Integer available = currentStock - reservedStock;

        //  Handle NULL values with defaults
        Integer reorderPoint = inventory.getReorderPoint() != null ? inventory.getReorderPoint() : 10;
        Integer maxStockLevel = inventory.getMaximumStockLevel() != null ? inventory.getMaximumStockLevel() : 1000;

        String status;
        String suggestion;

        if (available <= 0) {
            status = "OUT_OF_STOCK";
            suggestion = "Immediate restocking required";
        } else if (available <= reorderPoint) {
            status = "LOW_STOCK";
            suggestion = "Restock soon - below reorder point";
        } else if (available >= maxStockLevel) {
            status = "OVER_STOCK";
            suggestion = "Excess inventory - consider promotion";
        } else {
            status = "IN_STOCK";
            suggestion = "Stock level is adequate";
        }

        return StockLevelResponse.builder()
                .sku(sku)
                .currentStock(currentStock)
                .reservedStock(reservedStock)
                .availableStock(available)
                .status(status)
                .suggestion(suggestion)
                .build();
    }

    @Override
    @Transactional
    public void deleteInventory(String sku) {
        log.info("Deleting inventory for SKU: {}", sku);

        Inventory inventory = inventoryRepository.findBySku(sku)
                .orElseThrow(() -> new InventoryNotFoundException(sku));

        inventoryRepository.delete(inventory);
        log.info("Inventory deleted for SKU: {}", sku);
    }

    // Private helper method with null safety
    private InventoryResponse convertToResponse(Inventory inventory) {
        //  Safe null handling for all fields
        Integer quantity = inventory.getQuantity() != null ? inventory.getQuantity() : 0;
        Integer reservedQuantity = inventory.getReservedQuantity() != null ? inventory.getReservedQuantity() : 0;
        Integer availableQuantity = quantity - reservedQuantity;

        Integer minStockLevel = inventory.getMinimumStockLevel() != null ? inventory.getMinimumStockLevel() : 5;
        Integer reorderPoint = inventory.getReorderPoint() != null ? inventory.getReorderPoint() : 10;

        return InventoryResponse.builder()
                .sku(inventory.getSku())
                .quantity(quantity)
                .reservedQuantity(reservedQuantity)
                .availableQuantity(availableQuantity)
                .available(availableQuantity > 0)
                .message(availableQuantity > 0 ? "Stock available" : "Out of stock")
                .location(inventory.getLocation() != null ? inventory.getLocation() : "WAREHOUSE_MAIN")
                .minimumStockLevel(minStockLevel)
                .reorderPoint(reorderPoint)
                .needsReorder(availableQuantity <= reorderPoint)
                .lastRestockedAt(inventory.getLastRestockedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}