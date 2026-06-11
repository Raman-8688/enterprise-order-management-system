package com.enterprise.oms.order.dto.response;

import com.enterprise.oms.order.model.OrderStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private String id;
    private String orderNumber;
    private String customerEmail;
    private String customerName;
    private String shippingAddress;
    private List<OrderItemResponse> items;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String paymentId;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class OrderItemResponse {
        private String productSku;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}