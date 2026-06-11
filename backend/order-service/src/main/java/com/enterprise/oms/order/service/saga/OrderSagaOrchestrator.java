package com.enterprise.oms.order.service.saga;

import com.enterprise.oms.order.client.InventoryClient;
import com.enterprise.oms.order.client.ProductClient;
import com.enterprise.oms.order.dto.request.CreateOrderRequest;
import com.enterprise.oms.order.dto.response.InventoryResponse;
import com.enterprise.oms.order.dto.response.ProductResponse;
import com.enterprise.oms.order.events.publisher.OrderEventPublisher;
import com.enterprise.oms.order.exception.InsufficientStockException;
import com.enterprise.oms.order.model.Order;
import com.enterprise.oms.order.model.OrderItem;
import com.enterprise.oms.order.model.OrderStatus;
import com.enterprise.oms.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSagaOrchestrator {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;
    private final OrderEventPublisher eventPublisher;

    @Transactional
    public Order processOrder(CreateOrderRequest request) {
        log.info("Starting Saga orchestration for order from customer: {}", request.getCustomerEmail());

        // Step 1: Create Order in PENDING state
        Order order = createPendingOrder(request);
        order = orderRepository.save(order);
        log.info("Order created with ID: {} in PENDING state", order.getId());

        try {
            // Step 2: Validate and reserve inventory (Compensating transaction)
            validateAndReserveInventory(order);
            log.info("Inventory reserved successfully for order: {}", order.getId());

            // Step 3: Process payment (Compensating transaction)
            processPayment(order);
            log.info("Payment processed successfully for order: {}", order.getId());

            // Step 4: Confirm order
            confirmOrder(order);
            log.info("Order confirmed successfully: {}", order.getId());

            // Step 5: Publish success event
            eventPublisher.publishOrderCreatedEvent(order);

        } catch (Exception e) {
            log.error("Saga failed for order: {}. Starting compensation...", order.getId(), e);

            // COMPENSATING TRANSACTIONS (Rollback)
            compensateOrder(order, e.getMessage());
        }

        return order;
    }

    private Order createPendingOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setCustomerEmail(request.getCustomerEmail());
        order.setCustomerName(request.getCustomerName());
        order.setShippingAddress(request.getShippingAddress());
        order.setStatus(OrderStatus.PENDING);
        order.setItems(new ArrayList<>());

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CreateOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            // Get product details from Product Service
            ProductResponse product = productClient.getProductBySku(itemReq.getProductSku());

            if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Invalid product price for SKU: " + itemReq.getProductSku());
            }

            OrderItem item = OrderItem.builder()
                    .productSku(product.getSku())
                    .productName(product.getName())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())))
                    .build();

            order.getItems().add(item);
            totalAmount = totalAmount.add(item.getSubtotal());
        }

        order.setTotalAmount(totalAmount);
        return order;
    }

    private void validateAndReserveInventory(Order order) {
        log.info("Validating inventory for order: {}", order.getId());

        for (OrderItem item : order.getItems()) {
            // Check stock availability
            InventoryResponse inventory = inventoryClient.checkStock(item.getProductSku());

            // FIXED: Use getAvailable() instead of getInStock()
            if (inventory == null || !inventory.getAvailable() ||
                    inventory.getAvailableQuantity() < item.getQuantity()) {
                throw new InsufficientStockException(
                        String.format("Insufficient stock for SKU: %s. Available: %d, Requested: %d",
                                item.getProductSku(),
                                inventory != null ? inventory.getAvailableQuantity() : 0,
                                item.getQuantity())
                );
            }

            // Reserve stock
            Boolean reserved = inventoryClient.reserveStock(item.getProductSku(), item.getQuantity());
            if (!Boolean.TRUE.equals(reserved)) {
                throw new RuntimeException("Failed to reserve stock for SKU: " + item.getProductSku());
            }

            log.info("Reserved {} units for SKU: {}", item.getQuantity(), item.getProductSku());
        }
    }

    private void processPayment(Order order) {
        log.info("Processing payment for order: {}", order.getId());
        // TODO: Call Payment Service via Feign (Day 5)
        // For now, simulate successful payment
        order.setPaymentId("PAY-" + System.currentTimeMillis());
        log.info("Payment processed with ID: {}", order.getPaymentId());
    }

    private void confirmOrder(Order order) {
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }

    private void compensateOrder(Order order, String reason) {
        log.info("Starting compensation for order: {} due to: {}", order.getId(), reason);

        // Compensating Step 1: Release reserved inventory
        try {
            for (OrderItem item : order.getItems()) {
                // Release stock (negative reserve means release)
                inventoryClient.reserveStock(item.getProductSku(), -item.getQuantity());
                log.info("Released inventory for SKU: {}", item.getProductSku());
            }
        } catch (Exception e) {
            log.error("Failed to release inventory during compensation for order: {}", order.getId(), e);
        }

        // Compensating Step 2: Mark order as FAILED
        order.setStatus(OrderStatus.FAILED);
        order.setFailureReason(reason);
        orderRepository.save(order);

        log.info("Compensation completed for order: {}", order.getId());
    }
}