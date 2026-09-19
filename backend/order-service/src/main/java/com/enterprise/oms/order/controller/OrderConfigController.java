package com.enterprise.oms.order.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders/config")
@RefreshScope
public class OrderConfigController {

    @Value("${app.order.discount-percentage:10}")
    private int discountPercentage;

    @Value("${app.order.default-currency:USD}")
    private String defaultCurrency;

    @Value("${app.order.max-items-per-order:50}")
    private int maxItemsPerOrder;

    @Value("${app.order.enable-auto-cancellation:true}")
    private boolean enableAutoCancellation;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getLiveConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("status", "SUCCESS");
        config.put("message", "Live configuration fetched via Spring Cloud Config & @RefreshScope");
        config.put("discountPercentage", discountPercentage);
        config.put("defaultCurrency", defaultCurrency);
        config.put("maxItemsPerOrder", maxItemsPerOrder);
        config.put("enableAutoCancellation", enableAutoCancellation);
        return ResponseEntity.ok(config);
    }
}