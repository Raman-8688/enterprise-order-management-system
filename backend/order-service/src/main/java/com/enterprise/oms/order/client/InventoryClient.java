package com.enterprise.oms.order.client;

import com.enterprise.oms.order.client.fallback.InventoryClientFallback;
import com.enterprise.oms.order.dto.response.InventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "inventory-service", fallback = InventoryClientFallback.class)
public interface InventoryClient {

    @GetMapping("/api/inventory/{sku}")
    InventoryResponse checkStock(@PathVariable("sku") String sku);

    // FIXED: Add {sku} to the path
    @PostMapping("/api/inventory/{sku}/reserve")
    InventoryResponse reserveStock(
            @PathVariable("sku") String sku,  // Changed to PathVariable
            @RequestParam("quantity") Integer quantity
    );

    // FIXED: Add {sku} to the path
    @PostMapping("/api/inventory/{sku}/release")
    InventoryResponse releaseStock(
            @PathVariable("sku") String sku,  // Changed to PathVariable
            @RequestParam("quantity") Integer quantity
    );
}