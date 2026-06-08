package com.enterprise.oms.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    private String sku;
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private Boolean available;
    private String message;
    private String location;
    private Integer minimumStockLevel;
    private Integer reorderPoint;
    private Boolean needsReorder;
    private LocalDateTime lastRestockedAt;
    private LocalDateTime updatedAt;
}