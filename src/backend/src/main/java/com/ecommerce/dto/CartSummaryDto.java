package com.ecommerce.dto;

import java.math.BigDecimal;

public record CartSummaryDto(
    Integer itemCount,
    BigDecimal subTotal,
    BigDecimal total
) {}
