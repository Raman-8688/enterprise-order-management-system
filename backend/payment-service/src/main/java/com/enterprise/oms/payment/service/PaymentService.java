package com.enterprise.oms.payment.service;

import com.enterprise.oms.payment.dto.event.OrderCreatedEvent;
import com.enterprise.oms.payment.model.Payment;

public interface PaymentService {
    Payment processPayment(OrderCreatedEvent event);
    Payment getPaymentByOrderId(String orderId);
    Payment getPaymentById(String id);
    Payment getPaymentByPaymentReference(String paymentReference);
}