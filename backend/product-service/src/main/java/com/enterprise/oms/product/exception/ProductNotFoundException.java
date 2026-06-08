package com.enterprise.oms.product.exception;

import lombok.Getter;

@Getter
public class ProductNotFoundException extends RuntimeException {
    private final String sku;
    private final String id;

    public ProductNotFoundException(String sku) {
        super(String.format("Product not found with SKU: %s", sku));
        this.sku = sku;
        this.id = null;
    }

    public ProductNotFoundException(Long id) {
        super(String.format("Product not found with ID: %d", id));
        this.id = String.valueOf(id);
        this.sku = null;
    }

    public ProductNotFoundException(String sku, String id) {
        super(String.format("Product not found - SKU: %s, ID: %s", sku, id));
        this.sku = sku;
        this.id = id;
    }
}