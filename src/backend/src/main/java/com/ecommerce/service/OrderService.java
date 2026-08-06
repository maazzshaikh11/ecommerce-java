package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.domain.Order;
import com.ecommerce.domain.OrderItem;
import com.ecommerce.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public OrderConfirmationDto getOrderById(Integer orderId) {
        return orderRepository.findOrderWithItems(orderId)
            .map(this::buildConfirmation)
            .orElse(null);
    }

    @Override
    public List<OrderDto> getOrderHistory(Integer customerId) {
        return orderRepository.findOrdersByCustomer(customerId).stream()
            .map(o -> new OrderDto(
                o.getId(), o.getOrderNumber(), o.getOrderDate(), o.getOrderStatus(),
                o.getSubTotal(), o.getShippingTotal(), o.getTaxTotal(), o.getGrandTotal()
            )).toList();
    }

    OrderConfirmationDto buildConfirmation(Order order) {
        return new OrderConfirmationDto(
            order.getId(),
            order.getOrderNumber(),
            order.getOrderDate(),
            order.getOrderStatus(),
            order.getSubTotal(),
            order.getShippingTotal(),
            order.getTaxTotal(),
            order.getGrandTotal(),
            null,
            order.getOrderItems().stream()
                .map(oi -> new OrderItemDto(
                    oi.getProductName(), oi.getQuantity(), oi.getUnitPrice(), oi.getLineTotal()))
                .toList()
        );
    }
}
