package com.enterprise.oms.order.controller;

import com.enterprise.oms.order.dto.request.CreateOrderRequest;
import com.enterprise.oms.order.dto.request.UpdateOrderStatusRequest;
import com.enterprise.oms.order.dto.response.OrderResponse;
import com.enterprise.oms.order.dto.response.OrderSummaryResponse;
import com.enterprise.oms.order.model.Order;
import com.enterprise.oms.order.model.OrderStatus;
import com.enterprise.oms.order.service.OrderService;
import com.enterprise.oms.order.service.saga.OrderSagaOrchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final OrderSagaOrchestrator sagaOrchestrator;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("REST request to create order for customer: {}", request.getCustomerEmail());
        try {
            Order order = sagaOrchestrator.processOrder(request);
            return new ResponseEntity<>(convertToResponse(order), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Failed to create order: {}", e.getMessage(), e);
            throw e; // Let global exception handler handle it
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String id) {
        log.info("REST request to get order: {}", id);
        try {
            Order order = orderService.getOrderById(id);
            return ResponseEntity.ok(convertToResponse(order));
        } catch (Exception e) {
            log.error("Failed to get order: {}", id, e);
            throw e;
        }
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderByNumber(@PathVariable String orderNumber) {
        log.info("REST request to get order by number: {}", orderNumber);
        Order order = orderService.getOrderByNumber(orderNumber);
        return ResponseEntity.ok(convertToResponse(order));
    }

    @GetMapping("/customer/{email}")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(@PathVariable String email) {
        log.info("REST request to get orders for customer: {}", email);
        List<Order> orders = orderService.getOrdersByCustomer(email);
        return ResponseEntity.ok(orders.stream().map(this::convertToResponse).toList());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<OrderSummaryResponse>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("REST request to get orders with status: {}", status);
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Order> orders = orderService.getOrdersByStatus(status, pageable);
            return ResponseEntity.ok(orders.map(this::convertToSummaryResponse));
        } catch (Exception e) {
            log.error("Failed to get orders by status: {}", status, e);
            // Return empty page instead of throwing error
            Pageable pageable = PageRequest.of(page, size);
            return ResponseEntity.ok(Page.empty(pageable));
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable String id) {
        log.info("REST request to cancel order: {}", id);
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        log.info("REST request to update order status: {} -> {}", id, request.getStatus());
        Order order = orderService.updateOrderStatus(id, request.getStatus(), request.getFailureReason());
        return ResponseEntity.ok(convertToResponse(order));
    }

    @GetMapping
    public ResponseEntity<Page<OrderSummaryResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("REST request to get all orders");
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Order> orders = orderService.getAllOrders(pageable);
            return ResponseEntity.ok(orders.map(this::convertToSummaryResponse));
        } catch (Exception e) {
            log.error("Failed to get all orders", e);
            Pageable pageable = PageRequest.of(page, size);
            return ResponseEntity.ok(Page.empty(pageable));
        }
    }

    private OrderResponse convertToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerEmail(order.getCustomerEmail())
                .customerName(order.getCustomerName())
                .shippingAddress(order.getShippingAddress())
                .items(order.getItems().stream()
                        .map(item -> OrderResponse.OrderItemResponse.builder()
                                .productSku(item.getProductSku())
                                .productName(item.getProductName())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .subtotal(item.getSubtotal())
                                .build())
                        .toList())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentId(order.getPaymentId())
                .failureReason(order.getFailureReason())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderSummaryResponse convertToSummaryResponse(Order order) {
        return OrderSummaryResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}