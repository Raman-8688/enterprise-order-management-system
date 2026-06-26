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


}