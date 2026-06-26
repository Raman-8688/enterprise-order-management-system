package com.enterprise.oms.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    private String sku;
    private Integer availableQuantity;
    private Boolean available;
    private String message;
    private Integer reservedQuantity;
    private String location;
}