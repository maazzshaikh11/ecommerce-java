package com.ecommerce.controller;

import com.ecommerce.dto.OrderConfirmationDto;
import com.ecommerce.dto.OrderDto;
import com.ecommerce.security.SecurityUtils;
import com.ecommerce.service.IOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    private final IOrderService orderService;

    public OrdersController(IOrderService orderService) {
        this.orderService = orderService;
    }

    private Integer getCustomerId() {
        Integer id = SecurityUtils.currentCustomerId();
        if (id == null) throw new IllegalStateException("customer_id claim missing or invalid.");
        return id;
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> get() {
        return ResponseEntity.ok(orderService.getOrderHistory(getCustomerId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        OrderConfirmationDto order = orderService.getOrderById(id);
        if (order == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(order);
    }
}
