package com.ecommerce.dto;

public record AddressDto(
    Integer id,
    String streetAddress,
    String city,
    String stateProvince,
    String postalCode,
    String country,
    Boolean isDefault
) {}
