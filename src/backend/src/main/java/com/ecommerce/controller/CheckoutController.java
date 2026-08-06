package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.repository.AddressRepository;
import com.ecommerce.security.SecurityUtils;
import com.ecommerce.service.ICheckoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final ICheckoutService checkoutService;
    private final AddressRepository addressRepository;

    public CheckoutController(ICheckoutService checkoutService, AddressRepository addressRepository) {
        this.checkoutService = checkoutService;
        this.addressRepository = addressRepository;
    }

    private Integer getCustomerId() {
        Integer id = SecurityUtils.currentCustomerId();
        if (id == null) throw new IllegalStateException("customer_id claim missing or invalid.");
        return id;
    }

    @GetMapping("/addresses")
    public ResponseEntity<?> getAddresses() {
        return ResponseEntity.ok(checkoutService.getCustomerAddresses(getCustomerId()));
    }

    @PostMapping("/addresses")
    public ResponseEntity<?> addAddress(@RequestBody AddressDto dto) {
        com.ecommerce.domain.Address address = new com.ecommerce.domain.Address();
        address.setCustomerId(getCustomerId());
        address.setAddressType("Shipping");
        address.setStreetAddress(dto.streetAddress());
        address.setCity(dto.city());
        address.setStateProvince(dto.stateProvince());
        address.setPostalCode(dto.postalCode());
        address.setCountry(dto.country());
        address.setIsDefault(dto.isDefault());
        addressRepository.save(address);
        return ResponseEntity.ok(Map.of("id", address.getId()));
    }

    @GetMapping("/review")
    public ResponseEntity<?> getReview() {
        return ResponseEntity.ok(Map.of());
    }

    @GetMapping("/shipping-options")
    public ResponseEntity<?> getShippingOptions() {
        return ResponseEntity.ok(checkoutService.getShippingOptions());
    }

    @PostMapping("/validate-payment")
    public ResponseEntity<?> validatePayment(@RequestBody PaymentRequest request) {
        boolean valid = checkoutService.validatePayment(request);
        return ResponseEntity.ok(Map.of("valid", valid));
    }

    @PostMapping("/orders")
    public ResponseEntity<?> placeOrder(@RequestBody PlaceOrderRequest request, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Idempotency-Key header is required."));
        }
        try {
            String shipping = request.shippingMethod() != null ? request.shippingMethod() : "Standard";
            OrderConfirmationDto confirmation = checkoutService.placeOrder(getCustomerId(), request.addressId(), shipping, idempotencyKey);
            return ResponseEntity.status(201).body(confirmation);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/orders/{id}/confirmation")
    public ResponseEntity<?> getConfirmation(@PathVariable Integer id) {
        OrderConfirmationDto confirmation = checkoutService.getOrderConfirmation(id);
        if (confirmation == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(confirmation);
    }
}
