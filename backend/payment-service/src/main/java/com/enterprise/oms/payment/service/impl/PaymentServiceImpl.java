package com.enterprise.oms.payment.service.impl;

import com.enterprise.oms.payment.dto.event.OrderCreatedEvent;
import com.enterprise.oms.payment.model.Payment;
import com.enterprise.oms.payment.model.PaymentStatus;
import com.enterprise.oms.payment.publisher.PaymentEventPublisher;
import com.enterprise.oms.payment.repository.PaymentRepository;
import com.enterprise.oms.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher eventPublisher;
    private final Random random = new Random();

    @Override
    public Payment processPayment(OrderCreatedEvent event) {
        log.info(" Processing payment for Order: {}", event.getOrderNumber());

        // Check if payment already exists
        if (paymentRepository.existsByOrderId(event.getOrderId())) {
            log.warn("Payment already exists for Order: {}", event.getOrderNumber());
            return paymentRepository.findByOrderId(event.getOrderId()).get();
        }

        // Create payment record
        Payment payment = Payment.builder()
                .orderId(event.getOrderId())
                .orderNumber(event.getOrderNumber())
                .customerEmail(event.getCustomerEmail())
                .amount(event.getTotalAmount())
                .status(PaymentStatus.PROCESSING)
                .build();

        payment = paymentRepository.save(payment);
        log.info("   Payment record created: {}", payment.getPaymentReference());

        // Simulate payment processing
        boolean success = simulatePaymentProcessing();

        if (success) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setTransactionId("TXN-" + System.currentTimeMillis());
            log.info("   Payment COMPLETED - Transaction: {}", payment.getTransactionId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Insufficient funds / Payment declined");
            log.error("    Payment FAILED");
        }

        payment = paymentRepository.save(payment);

        // Publish event
        eventPublisher.publishPaymentProcessedEvent(payment);

        return payment;
    }

    private boolean simulatePaymentProcessing() {
        // Simulate processing delay
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 85% success rate for demo
        return random.nextInt(100) < 85;
    }

    @Override
    public Payment getPaymentByOrderId(String orderId) {
        log.info("Fetching payment for orderId: {}", orderId);
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
    }

    @Override
    public Payment getPaymentById(String id) {
        log.info("Fetching payment by id: {}", id);
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
    }

    @Override
    public Payment getPaymentByPaymentReference(String paymentReference) {
        log.info("Fetching payment by reference: {}", paymentReference);
        return paymentRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new RuntimeException("Payment not found with reference: " + paymentReference));
    }
}