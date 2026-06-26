package com.enterprise.oms.order.client;

import com.enterprise.oms.order.client.fallback.ProductClientFallback;
import com.enterprise.oms.order.dto.response.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", fallback = ProductClientFallback.class)
public interface ProductClient {



    @GetMapping("/api/products/sku/{sku}")
    ProductResponse getProductBySku(@PathVariable("sku") String sku);

}