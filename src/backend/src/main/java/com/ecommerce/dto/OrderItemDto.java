package com.ecommerce.dto;

import java.math.BigDecimal;

public record OrderItemDto(
    String productName,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal lineTotal
) {}
