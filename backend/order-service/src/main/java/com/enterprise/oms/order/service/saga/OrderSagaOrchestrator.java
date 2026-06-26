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

        Order order = createPendingOrder(request);
        order = orderRepository.save(order);
        log.info("Order created with ID: {} in PENDING state", order.getId());

        try {
            validateAndReserveInventory(order);
            log.info("Inventory reserved successfully for order: {}", order.getId());

            processPayment(order);
            log.info("Payment processed successfully for order: {}", order.getId());

            confirmOrder(order);
            log.info("Order confirmed successfully: {}", order.getId());

            eventPublisher.publishOrderCreatedEvent(order);

        } catch (Exception e) {
            log.error("Saga failed for order: {}. Starting compensation...", order.getId(), e);
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
            try {
                ProductResponse product = productClient.getProductBySku(itemReq.getProductSku());

                if (product == null || product.getSku() == null) {
                    throw new RuntimeException("Product not found for SKU: " + itemReq.getProductSku());
                }

                if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new RuntimeException("Invalid product price for SKU: " + itemReq.getProductSku());
                }

                OrderItem item = OrderItem.builder()
                        .productSku(product.getSku())
                        .productName(product.getName() != null ? product.getName() : product.getSku())
                        .quantity(itemReq.getQuantity())
                        .unitPrice(product.getPrice())
                        .subtotal(product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())))
                        .build();

                order.getItems().add(item);
                totalAmount = totalAmount.add(item.getSubtotal());

            } catch (Exception e) {
                log.error("Error fetching product for SKU: {}", itemReq.getProductSku(), e);
                throw new RuntimeException("Failed to fetch product: " + itemReq.getProductSku() + " - " + e.getMessage(), e);
            }
        }

        order.setTotalAmount(totalAmount);
        return order;
    }

    private void validateAndReserveInventory(Order order) {
        log.info("Validating inventory for order: {}", order.getId());

        for (OrderItem item : order.getItems()) {
            InventoryResponse inventory = inventoryClient.checkStock(item.getProductSku());

            if (inventory == null || !Boolean.TRUE.equals(inventory.getAvailable()) ||
                    inventory.getAvailableQuantity() == null || inventory.getAvailableQuantity() < item.getQuantity()) {
                throw new InsufficientStockException(
                        String.format("Insufficient stock for SKU: %s. Available: %d, Requested: %d",
                                item.getProductSku(),
                                inventory != null && inventory.getAvailableQuantity() != null ? inventory.getAvailableQuantity() : 0,
                                item.getQuantity())
                );
            }

            InventoryResponse reserveResponse = inventoryClient.reserveStock(item.getProductSku(), item.getQuantity());

            if (reserveResponse == null || !Boolean.TRUE.equals(reserveResponse.getAvailable())) {
                throw new RuntimeException(
                        String.format("Failed to reserve stock for SKU: %s",
                                item.getProductSku())
                );
            }

            log.info("Reserved {} units for SKU: {}", item.getQuantity(), item.getProductSku());
        }
    }

    private void processPayment(Order order) {
        log.info("Processing payment for order: {}", order.getId());
        order.setPaymentId("PAY-" + System.currentTimeMillis());
    }

    private void confirmOrder(Order order) {
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }

    private void compensateOrder(Order order, String reason) {
        log.info("Starting compensation for order: {}", order.getId());

        try {
            for (OrderItem item : order.getItems()) {
                InventoryResponse releaseResponse = inventoryClient.releaseStock(item.getProductSku(), item.getQuantity());
                if (releaseResponse != null && Boolean.TRUE.equals(releaseResponse.getAvailable())) {
                    log.info("Released inventory for SKU: {}", item.getProductSku());
                }
            }
        } catch (Exception e) {
            log.error("Failed to release inventory for order: {}", order.getId(), e);
        }

        order.setStatus(OrderStatus.FAILED);
        order.setFailureReason(reason);
        orderRepository.save(order);

        log.info("Compensation completed for order: {}", order.getId());
    }
}