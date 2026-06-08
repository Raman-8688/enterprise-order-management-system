package com.enterprise.oms.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockLevelResponse {
    private String sku;
    private Integer currentStock;
    private Integer reservedStock;
    private Integer availableStock;
    private String status; // "IN_STOCK", "LOW_STOCK", "OUT_OF_STOCK", "OVER_STOCK"
    private String suggestion;
}