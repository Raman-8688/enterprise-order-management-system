package com.enterprise.oms.order.client.fallback;

import com.enterprise.oms.order.client.ProductClient;
import com.enterprise.oms.order.dto.response.ProductResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class ProductClientFallback implements ProductClient {

    @Override
    public ProductResponse getProductBySku(String sku) {
        log.warn("Product service is unavailable! Using fallback for SKU: {}", sku);

        return ProductResponse.builder()
                .sku(sku)
                .name("Product Unavailable - Service Down")
                .price(BigDecimal.ZERO)
                .active(false)
                .build();
    }
}