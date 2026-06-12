package com.enterprise.oms.payment.consumer;

import com.enterprise.oms.payment.dto.event.OrderCreatedEvent;
import com.enterprise.oms.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "order-created-events", groupId = "payment-service-group")
    public void consumeOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("📨 Received OrderCreatedEvent for Order: {}", event.getOrderNumber());
        log.info("   Customer: {}, Amount: ${}", event.getCustomerEmail(), event.getTotalAmount());

        try {
            paymentService.processPayment(event);
            log.info("✅ Payment processed successfully for Order: {}", event.getOrderNumber());
        } catch (Exception e) {
            log.error("❌ Payment failed for Order: {}", event.getOrderNumber(), e);
        }
    }
}