package com.ecommerce.dto;

import java.math.BigDecimal;

public record AddCartItemRequest(
    Integer productId,
    Integer variantId,
    Integer quantity
) {}
