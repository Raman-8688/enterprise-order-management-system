package com.enterprise.oms.payment.controller;

import com.enterprise.oms.payment.model.Payment;
import com.enterprise.oms.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Payment> getPaymentByOrderId(@PathVariable String orderId) {
        log.info("REST request to get payment for order: {}", orderId);
        Payment payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable String id) {
        log.info("REST request to get payment by id: {}", id);
        Payment payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/reference/{paymentReference}")
    public ResponseEntity<Payment> getPaymentByReference(@PathVariable String paymentReference) {
        log.info("REST request to get payment by reference: {}", paymentReference);
        Payment payment = paymentService.getPaymentByPaymentReference(paymentReference);
        return ResponseEntity.ok(payment);
    }
}