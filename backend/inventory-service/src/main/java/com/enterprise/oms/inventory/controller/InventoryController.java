package com.enterprise.oms.inventory.controller;

import com.enterprise.oms.inventory.dto.request.UpdateStockRequest;
import com.enterprise.oms.inventory.dto.response.InventoryResponse;
import com.enterprise.oms.inventory.dto.response.StockLevelResponse;
import com.enterprise.oms.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/initialize")
    public ResponseEntity<InventoryResponse> initializeInventory(
            @RequestParam String sku,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String location) {
        log.info("REST request to initialize inventory for SKU: {}", sku);
        InventoryResponse response = inventoryService.initializeInventory(sku, quantity, location);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{sku}")
    public ResponseEntity<InventoryResponse> checkStock(@PathVariable String sku) {
        log.info("REST request to check stock for SKU: {}", sku);
        InventoryResponse response = inventoryService.checkStock(sku);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sku}/update")
    public ResponseEntity<InventoryResponse> updateStock(
            @PathVariable String sku,
            @Valid @RequestBody UpdateStockRequest request) {
        log.info("REST request to update stock for SKU: {}", sku);
        InventoryResponse response = inventoryService.updateStock(sku, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sku}/reserve")
    public ResponseEntity<Boolean> reserveStock(
            @PathVariable String sku,
            @RequestParam Integer quantity) {
        log.info("REST request to reserve stock for SKU: {} quantity: {}", sku, quantity);
        Boolean result = inventoryService.reserveStock(sku, quantity);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{sku}/release")
    public ResponseEntity<Boolean> releaseStock(
            @PathVariable String sku,
            @RequestParam Integer quantity) {
        log.info("REST request to release stock for SKU: {} quantity: {}", sku, quantity);
        Boolean result = inventoryService.releaseStock(sku, quantity);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{sku}/deduct")
    public ResponseEntity<Boolean> confirmStockDeduction(
            @PathVariable String sku,
            @RequestParam Integer quantity) {
        log.info("REST request to confirm stock deduction for SKU: {} quantity: {}", sku, quantity);
        Boolean result = inventoryService.confirmStockDeduction(sku, quantity);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getAllInventory() {
        log.info("REST request to get all inventory");
        List<InventoryResponse> inventory = inventoryService.getAllInventory();
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<InventoryResponse>> getAllInventoryPaged(
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("REST request to get all inventory with pagination");
        Page<InventoryResponse> inventory = inventoryService.getAllInventory(pageable);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryResponse>> getLowStockItems() {
        log.info("REST request to get low stock items");
        List<InventoryResponse> lowStock = inventoryService.getLowStockItems();
        return ResponseEntity.ok(lowStock);
    }

    @GetMapping("/needs-reorder")
    public ResponseEntity<List<InventoryResponse>> getItemsNeedingReorder() {
        log.info("REST request to get items needing reorder");
        List<InventoryResponse> needsReorder = inventoryService.getItemsNeedingReorder();
        return ResponseEntity.ok(needsReorder);
    }

    @GetMapping("/{sku}/status")
    public ResponseEntity<StockLevelResponse> getStockLevelStatus(@PathVariable String sku) {
        log.info("REST request to get stock level status for SKU: {}", sku);
        StockLevelResponse response = inventoryService.getStockLevelStatus(sku);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{sku}")
    public ResponseEntity<Void> deleteInventory(@PathVariable String sku) {
        log.info("REST request to delete inventory for SKU: {}", sku);
        inventoryService.deleteInventory(sku);
        return ResponseEntity.noContent().build();
    }
}