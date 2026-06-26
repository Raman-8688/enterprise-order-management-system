package com.enterprise.oms.order.client.fallback;

import com.enterprise.oms.order.client.InventoryClient;
import com.enterprise.oms.order.dto.response.InventoryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InventoryClientFallback implements InventoryClient {

    @Override
    public InventoryResponse checkStock(String sku) {
        log.warn("Inventory service is unavailable! Using fallback for SKU: {}", sku);

        return InventoryResponse.builder()
                .sku(sku)
                .available(false)
                .availableQuantity(0)
                .message("SERVICE_UNAVAILABLE")
                .build();
    }

    @Override
    public InventoryResponse reserveStock(String sku, Integer quantity) {  // ← Changed from Boolean to InventoryResponse
        log.warn("Inventory service is unavailable! Cannot reserve stock for SKU: {}, quantity: {}", sku, quantity);

        return InventoryResponse.builder()
                .sku(sku)
                .available(false)
                .availableQuantity(0)
                .message("RESERVE_FAILED_SERVICE_UNAVAILABLE")
                .build();
    }

    @Override
    public InventoryResponse releaseStock(String sku, Integer quantity) {
        log.warn("Inventory service is unavailable! Cannot release stock for SKU: {}, quantity: {}", sku, quantity);

        return InventoryResponse.builder()
                .sku(sku)
                .available(false)
                .availableQuantity(0)
                .message("RELEASE_FAILED_SERVICE_UNAVAILABLE")
                .build();
    }

}