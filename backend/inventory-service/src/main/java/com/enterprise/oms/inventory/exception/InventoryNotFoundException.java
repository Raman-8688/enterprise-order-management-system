package com.enterprise.oms.inventory.exception;

import lombok.Getter;

@Getter
public class InventoryNotFoundException extends RuntimeException {
    private final String sku;

    public InventoryNotFoundException(String sku) {
        super(String.format("Inventory not found for SKU: %s", sku));
        this.sku = sku;
    }
}