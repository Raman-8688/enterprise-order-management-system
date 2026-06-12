package com.enterprise.oms.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class PaymentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
        System.out.println("=".repeat(50));
        System.out.println("Payment Service Started Successfully!");
        System.out.println("Port: http://localhost:8085");
        System.out.println("Eureka: Registered as PAYMENT-SERVICE");
        System.out.println("Kafka: Consumer & Producer Ready");
        System.out.println("=".repeat(50));
    }
}