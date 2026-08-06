package com.ecommerce.service;

import com.ecommerce.dto.*;

import java.util.List;

public interface ICheckoutService {
    OrderConfirmationDto placeOrder(Integer customerId, Integer addressId, String shippingMethod, String idempotencyKey);
    boolean validateAddress(String streetAddress, String city, String stateProvince, String postalCode, String country);
    List<AddressDto> getCustomerAddresses(Integer customerId);
    OrderConfirmationDto getOrderConfirmation(Integer orderId);
    List<ShippingOptionDto> getShippingOptions();
    boolean validatePayment(PaymentRequest request);
}
