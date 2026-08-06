package com.ecommerce.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderConfirmationDto(
    Integer orderId,
    String orderNumber,
    LocalDateTime orderDate,
    String orderStatus,
    BigDecimal subTotal,
    BigDecimal shippingTotal,
    BigDecimal taxTotal,
    BigDecimal grandTotal,
    String shippingAddressFormatted,
    List<OrderItemDto> items
) {}
