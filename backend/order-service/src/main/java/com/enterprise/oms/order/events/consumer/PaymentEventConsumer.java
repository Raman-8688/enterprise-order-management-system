package com.enterprise.oms.order.events.consumer;

import com.enterprise.oms.order.dto.event.PaymentProcessedEvent;
import com.enterprise.oms.order.model.Order;
import com.enterprise.oms.order.model.OrderStatus;
import com.enterprise.oms.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(
            topics = "payment-processed-events",
            groupId = "order-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumePaymentProcessedEvent(PaymentProcessedEvent event) {
        log.info("Received PaymentProcessedEvent for Order: {}", event.getOrderNumber());
        log.info("   Payment Status: {}, Amount: ${}", event.getStatus(), event.getAmount());

        // Find the order
        Order order = orderRepository.findById(event.getOrderId())
                .orElse(null);

        if (order == null) {
            log.error("Order not found with ID: {}", event.getOrderId());
            return;
        }

        // Update order based on payment status
        if ("COMPLETED".equals(event.getStatus())) {
            order.setStatus(OrderStatus.CONFIRMED);
            order.setPaymentId(event.getPaymentId());
            log.info(" Order {} confirmed after successful payment", order.getOrderNumber());
        } else if ("FAILED".equals(event.getStatus())) {
            order.setStatus(OrderStatus.FAILED);
            order.setFailureReason("Payment failed: " + event.getFailureReason());
            log.error("Order {} failed due to payment error", order.getOrderNumber());
        }

        orderRepository.save(order);
    }
}