package com.enterprise.oms.payment.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String paymentReference;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private String orderNumber;

    @Column(nullable = false)
    private String customerEmail;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String transactionId;

    private String failureReason;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = PaymentStatus.PENDING;
        }
        if (paymentReference == null) {
            paymentReference = "PAY-" + System.currentTimeMillis();
        }
    }
}