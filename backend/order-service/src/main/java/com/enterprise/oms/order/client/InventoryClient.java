package com.enterprise.oms.order.client;

import com.enterprise.oms.order.client.fallback.InventoryClientFallback;
import com.enterprise.oms.order.dto.response.InventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "inventory-service", fallback = InventoryClientFallback.class)
public interface InventoryClient {

    @GetMapping("/api/inventory/{sku}")
    InventoryResponse checkStock(@PathVariable("sku") String sku);

    @PostMapping("/api/inventory/{sku}/reserve")
    Boolean reserveStock(@PathVariable("sku") String sku,
                         @RequestParam Integer quantity);
}