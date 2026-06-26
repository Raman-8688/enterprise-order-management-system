package com.enterprise.oms.order.service.impl;

import com.enterprise.oms.order.exception.OrderNotFoundException;
import com.enterprise.oms.order.model.Order;
import com.enterprise.oms.order.model.OrderStatus;
import com.enterprise.oms.order.repository.OrderRepository;
import com.enterprise.oms.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    public Order getOrderById(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
    }

    @Override
    public Order getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with number: " + orderNumber));
    }

    @Override
    public List<Order> getOrdersByCustomer(String customerEmail) {
        return orderRepository.findByCustomerEmail(customerEmail);
    }

    @Override
    public Page<Order> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    @Override
    public Order cancelOrder(String id) {
        Order order = getOrderById(id);

        if (order.getStatus() == OrderStatus.CONFIRMED ||
                order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CANCELLED);
            order = orderRepository.save(order);
            log.info("Order cancelled: {}", order.getOrderNumber());
        } else {
            throw new IllegalStateException("Cannot cancel order with status: " + order.getStatus());
        }

        return order;
    }

    @Override
    public Order updateOrderStatus(String id, OrderStatus status, String failureReason) {
        Order order = getOrderById(id);
        order.setStatus(status);
        if (failureReason != null) {
            order.setFailureReason(failureReason);
        }
        order = orderRepository.save(order);
        log.info("Order status updated: {} -> {}", order.getOrderNumber(), status);
        return order;
    }


    @Override
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }
}