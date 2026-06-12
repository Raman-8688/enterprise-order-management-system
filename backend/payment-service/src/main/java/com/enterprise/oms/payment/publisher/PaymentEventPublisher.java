package com.enterprise.oms.payment.publisher;

import com.enterprise.oms.payment.dto.event.PaymentProcessedEvent;
import com.enterprise.oms.payment.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentProcessedEvent(Payment payment) {
        PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .orderNumber(payment.getOrderNumber())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .failureReason(payment.getFailureReason())
                .timestamp(System.currentTimeMillis())
                .build();

        kafkaTemplate.send("payment-processed-events", payment.getOrderId(), event);
        log.info(" Published PaymentProcessedEvent for Order: {}, Status: {}",
                payment.getOrderNumber(), payment.getStatus());
    }
}