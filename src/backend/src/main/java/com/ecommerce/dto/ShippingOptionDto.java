package com.ecommerce.dto;

import java.math.BigDecimal;

public record ShippingOptionDto(
    String method,
    String label,
    BigDecimal cost
) {}
