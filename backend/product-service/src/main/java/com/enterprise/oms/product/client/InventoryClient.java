package com.enterprise.oms.product.client;

import com.enterprise.oms.product.client.fallback.InventoryClientFallback;
import com.enterprise.oms.product.dto.InventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "inventory-service", fallback = InventoryClientFallback.class)
public interface InventoryClient {

    @GetMapping("/api/inventory/{sku}")
    InventoryResponse checkStock(@PathVariable("sku") String sku);

    @PostMapping("/api/inventory/{sku}/reserve")
    Boolean reserveStock(@PathVariable("sku") String sku, @RequestParam Integer quantity);

    @PostMapping("/api/inventory/{sku}/release")
    Boolean releaseStock(@PathVariable("sku") String sku, @RequestParam Integer quantity);
}
