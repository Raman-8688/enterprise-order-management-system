package com.enterprise.oms.order.service;

import com.enterprise.oms.order.model.Order;
import com.enterprise.oms.order.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    Order getOrderById(String id);
    Order getOrderByNumber(String orderNumber);
    List<Order> getOrdersByCustomer(String customerEmail);
    Page<Order> getOrdersByStatus(OrderStatus status, Pageable pageable);
    Order cancelOrder(String id);
    Order updateOrderStatus(String id, OrderStatus status, String failureReason);
    Page<Order> getAllOrders(Pageable pageable);
}