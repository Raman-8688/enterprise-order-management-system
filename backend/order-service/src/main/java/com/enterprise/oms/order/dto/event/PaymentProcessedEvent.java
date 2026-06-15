package com.enterprise.oms.order.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessedEvent {
    private String paymentId;
    private String orderId;
    private String orderNumber;
    private BigDecimal amount;
    private String status;  // COMPLETED, FAILED, REFUNDED
    private String transactionId;
    private String failureReason;
    private Long timestamp;
}