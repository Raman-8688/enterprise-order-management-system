package com.enterprise.oms.payment.dto.event;

import com.enterprise.oms.payment.model.PaymentStatus;
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
    private PaymentStatus status;
    private String transactionId;
    private String failureReason;
    private Long timestamp;
}