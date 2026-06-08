package com.enterprise.oms.product.client.fallback;

import com.enterprise.oms.product.client.InventoryClient;
import com.enterprise.oms.product.dto.InventoryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryClientFallback implements InventoryClient {

    @Override
    public InventoryResponse checkStock(String sku) {
        log.warn("Circuit Breaker OPEN - Inventory service is DOWN for SKU: {}", sku);

        return InventoryResponse.builder()
                .sku(sku)
                .available(false)
                .message("Inventory service is currently unavailable. Please try again later.")
                .availableQuantity(0)
                .build();
    }

    @Override
    public Boolean reserveStock(String sku, Integer quantity) {
        log.warn("Circuit Breaker OPEN - Cannot reserve stock for SKU: {}", sku);
        return false;
    }

    @Override
    public Boolean releaseStock(String sku, Integer quantity) {
        log.warn("Circuit Breaker OPEN - Cannot release stock for SKU: {}", sku);
        return false;
    }
}