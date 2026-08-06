package com.ecommerce.dto;

import java.math.BigDecimal;

public record CartItemDto(
    Integer id,
    Integer productId,
    String productName,
    String variantName,
    String imageUrl,
    BigDecimal unitPrice,
    Integer quantity,
    BigDecimal lineTotal
) {}
