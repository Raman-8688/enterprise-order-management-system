package com.enterprise.oms.order.events.publisher;

import com.enterprise.oms.order.dto.event.OrderCreatedEvent;
import com.enterprise.oms.order.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreatedEvent(Order order) {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerEmail(order.getCustomerEmail())
                .totalAmount(order.getTotalAmount())
                .timestamp(System.currentTimeMillis())
                .build();

        kafkaTemplate.send("order-created-events", order.getId(), event);
        log.info("Published OrderCreatedEvent for order: {}", order.getOrderNumber());
    }
}