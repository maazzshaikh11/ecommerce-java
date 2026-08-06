package com.ecommerce.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderDto(
    Integer id,
    String orderNumber,
    LocalDateTime orderDate,
    String orderStatus,
    BigDecimal subTotal,
    BigDecimal shippingTotal,
    BigDecimal taxTotal,
    BigDecimal grandTotal
) {}
