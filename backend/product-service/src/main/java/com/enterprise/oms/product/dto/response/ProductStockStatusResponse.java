package com.enterprise.oms.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStockStatusResponse {
    private String sku;
    private String productName;
    private Boolean productActive;
    private Boolean inventoryAvailable;
    private Integer availableQuantity;
    private String message;
    private Boolean circuitBreakerOpen;
}