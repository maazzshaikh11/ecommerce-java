package com.ecommerce.service;

import com.ecommerce.dto.OrderConfirmationDto;
import com.ecommerce.dto.OrderDto;

import java.util.List;

public interface IOrderService {
    OrderConfirmationDto getOrderById(Integer orderId);
    List<OrderDto> getOrderHistory(Integer customerId);
}
