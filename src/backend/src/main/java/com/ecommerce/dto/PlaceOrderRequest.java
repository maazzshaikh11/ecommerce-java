package com.ecommerce.dto;

public record PlaceOrderRequest(
    Integer addressId,
    String shippingMethod,
    String idempotencyKey
) {}
