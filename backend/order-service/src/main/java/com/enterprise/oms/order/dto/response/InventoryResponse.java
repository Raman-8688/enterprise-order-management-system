package com.enterprise.oms.order.dto.response;

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
    private Integer quantity;           // Total quantity
    private Integer reservedQuantity;   // Reserved quantity
    private Integer availableQuantity;  // Available to sell
    private Boolean available;          // ← This is the correct field!
    private String message;
    private String location;
    private Integer minimumStockLevel;
    private Integer reorderPoint;
    private Boolean needsReorder;
    private LocalDateTime lastRestockedAt;
    private LocalDateTime updatedAt;
}