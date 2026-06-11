package com.enterprise.oms.order.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {

    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @NotEmpty(message = "Order must have at least one item")
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        @NotBlank(message = "Product SKU is required")
        private String productSku;

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private Integer quantity;
    }
}