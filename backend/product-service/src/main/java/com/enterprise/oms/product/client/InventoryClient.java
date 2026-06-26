package com.enterprise.oms.product.client;

import com.enterprise.oms.product.client.fallback.InventoryClientFallback;
import com.enterprise.oms.product.dto.response.InventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "inventory-service", fallback = InventoryClientFallback.class)
public interface InventoryClient {

    @GetMapping("/api/inventory/{sku}")
    InventoryResponse checkStock(@PathVariable("sku") String sku);

    @PostMapping("/api/inventory/reserve")
    Boolean reserveStock(
            @RequestParam("sku") String sku,
            @RequestParam("quantity") Integer quantity
    );

    @PostMapping("/api/inventory/release")
    Boolean releaseStock(
            @RequestParam("sku") String sku,
            @RequestParam("quantity") Integer quantity
    );




}
