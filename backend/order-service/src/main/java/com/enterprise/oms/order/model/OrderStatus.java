package com.enterprise.oms.order.model;

public enum OrderStatus {
    PENDING,      // Order created, not processed
    VALIDATING,   // Checking inventory and payment
    CONFIRMED,    // All checks passed
    PROCESSING,   // Being fulfilled
    SHIPPED,      // Dispatched
    DELIVERED,    // Received by customer
    CANCELLED,    // Cancelled by user or system
    FAILED        // Processing failed
}