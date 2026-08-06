package com.ecommerce.service;

import com.ecommerce.dto.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CheckoutServicePureTest {

    private final CheckoutService checkoutService = new CheckoutService(null, null, null, null, null);

    @Test
    void validatePayment_withValidCard_shouldReturnTrue() {
        PaymentRequest req = new PaymentRequest("John Doe", "4111111111111111", "12/26", "123");
        assertThat(checkoutService.validatePayment(req)).isTrue();
    }

    @Test
    void validatePayment_withInvalidCardNumber_shouldReturnFalse() {
        PaymentRequest req = new PaymentRequest("John", "1234", "12/26", "123");
        assertThat(checkoutService.validatePayment(req)).isFalse();
    }

    @Test
    void validatePayment_withInvalidExpiry_shouldReturnFalse() {
        PaymentRequest req = new PaymentRequest("John", "4111111111111111", "13/26", "123");
        assertThat(checkoutService.validatePayment(req)).isFalse();
    }

    @Test
    void validatePayment_withInvalidCvv_shouldReturnFalse() {
        PaymentRequest req = new PaymentRequest("John", "4111111111111111", "12/26", "12");
        assertThat(checkoutService.validatePayment(req)).isFalse();
    }

    @Test
    void validatePayment_withNull_shouldReturnFalse() {
        assertThat(checkoutService.validatePayment(null)).isFalse();
    }

    @Test
    void getShippingOptions_shouldReturnTwoOptions() {
        List<ShippingOptionDto> opts = checkoutService.getShippingOptions();
        assertThat(opts).hasSize(2);
        assertThat(opts.get(0).method()).isEqualTo("Standard");
        assertThat(opts.get(1).method()).isEqualTo("Express");
    }

    @Test
    void validateAddress_withValidInput_shouldReturnTrue() {
        assertThat(checkoutService.validateAddress("St", "City", "State", "00000", "Country")).isTrue();
    }

    @Test
    void validateAddress_withBlankInput_shouldReturnFalse() {
        assertThat(checkoutService.validateAddress("", "City", "State", "00000", "Country")).isFalse();
    }

    @Test
    void validateAddress_withNullInput_shouldReturnFalse() {
        assertThat(checkoutService.validateAddress(null, "City", "State", "00000", "Country")).isFalse();
    }
}
