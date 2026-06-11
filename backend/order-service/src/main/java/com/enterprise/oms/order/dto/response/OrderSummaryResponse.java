package com.enterprise.oms.order.dto.response;

import com.enterprise.oms.order.model.OrderStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderSummaryResponse {
    private String id;
    private String orderNumber;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
}