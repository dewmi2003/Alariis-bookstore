package com.bookstore.service;

import com.bookstore.entity.Order;
import com.bookstore.entity.User;
import java.util.List;

public interface OrderService {
    Order createOrder(User user);

    List<Order> findOrdersByUser(User user);

    Order findOrderById(Long id);

    void updateOrderStatus(Long orderId, String status);

    List<Order> findAllOrders();
}
