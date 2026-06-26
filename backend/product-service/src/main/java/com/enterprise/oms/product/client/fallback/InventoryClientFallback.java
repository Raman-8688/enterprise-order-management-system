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
        log.warn("Inventory service fallback triggered for SKU: {}", sku);
        return InventoryResponse.builder()
                .sku(sku)
                .available(false)
                .availableQuantity(0)
                .message("Inventory service unavailable")
                .build();
    }

    @Override
    public Boolean reserveStock(String sku, Integer quantity) {
        log.warn("Inventory reserve fallback triggered for SKU: {}", sku);
        return false;
    }

    @Override
    public Boolean releaseStock(String sku, Integer quantity) {
        log.warn("Inventory release fallback triggered for SKU: {}", sku);
        return false;
    }
}