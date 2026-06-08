package com.enterprise.oms.product.exception;

import lombok.Getter;

@Getter
public class DuplicateSkuException extends RuntimeException {
    private final String sku;

    public DuplicateSkuException(String sku) {
        super(String.format("Product with SKU '%s' already exists", sku));
        this.sku = sku;
    }
}