package com.ecommerce.dto;

public record PaymentRequest(
    String cardholderName,
    String cardNumber,
    String expirationDate,
    String cvv
) {}
