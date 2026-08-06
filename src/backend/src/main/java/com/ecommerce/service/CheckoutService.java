package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.domain.Address;
import com.ecommerce.domain.CartItem;
import com.ecommerce.domain.Order;
import com.ecommerce.domain.ProductVariant;
import com.ecommerce.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

@Service
public class CheckoutService implements ICheckoutService {

    private static final BigDecimal GST_RATE = new BigDecimal("0.18");
    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("^\\d{16}$");
    private static final Pattern EXPIRY_PATTERN = Pattern.compile("^(0[1-9]|1[0-2])/\\d{2}$");
    private static final Pattern CVV_PATTERN = Pattern.compile("^\\d{3,4}$");

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final AddressRepository addressRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CartItemRepositoryCustom cartItemCustom;
    private final OrderService orderService;

    public CheckoutService(OrderRepository orderRepository, CartItemRepository cartItemRepository,
                           AddressRepository addressRepository, ProductVariantRepository productVariantRepository,
                           OrderService orderService) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.addressRepository = addressRepository;
        this.productVariantRepository = productVariantRepository;
        this.cartItemCustom = (CartItemRepositoryCustom) cartItemRepository;
        this.orderService = orderService;
    }

    @Override
    @Transactional
    public OrderConfirmationDto placeOrder(Integer customerId, Integer addressId, String shippingMethod, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key is required.");
        }

        var existing = orderRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return orderService.buildConfirmation(existing.get());
        }

        List<CartItem> cartItems = cartItemCustom.getCartItemsByCustomer(customerId);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cart is empty.");
        }

        for (CartItem item : cartItems) {
            if (item.getVariantId() != null) {
                productVariantRepository.findById(item.getVariantId()).ifPresent(variant -> {
                    variant.setStockQuantity(Math.max(0, variant.getStockQuantity() - item.getQuantity()));
                    productVariantRepository.save(variant);
                });
            }
        }

        Address shippingAddr = addressRepository.findById(addressId)
            .orElseThrow(() -> new IllegalStateException("Address not found."));

        Address billingAddr = new Address();
        billingAddr.setCustomerId(customerId);
        billingAddr.setAddressType("Billing");
        billingAddr.setStreetAddress(shippingAddr.getStreetAddress());
        billingAddr.setCity(shippingAddr.getCity());
        billingAddr.setStateProvince(shippingAddr.getStateProvince());
        billingAddr.setPostalCode(shippingAddr.getPostalCode());
        billingAddr.setCountry(shippingAddr.getCountry());
        billingAddr.setIsDefault(false);
        addressRepository.save(billingAddr);

        BigDecimal subTotal = cartItems.stream()
            .map(ci -> ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingTotal;
        if ("express".equalsIgnoreCase(shippingMethod)) {
            shippingTotal = new BigDecimal("199");
        } else {
            shippingTotal = new BigDecimal("99");
        }

        BigDecimal taxTotal = (subTotal.add(shippingTotal)).multiply(GST_RATE).setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal grandTotal = subTotal.add(shippingTotal).add(taxTotal);

        Order order = new Order();
        order.setCustomerId(customerId);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus("Paid");
        order.setOrderNumber(generateOrderNumber());
        order.setSubTotal(subTotal);
        order.setShippingTotal(shippingTotal);
        order.setTaxTotal(taxTotal);
        order.setGrandTotal(grandTotal);
        order.setShippingAddressId(shippingAddr.getId());
        order.setBillingAddressId(billingAddr.getId());
        order.setIdempotencyKey(idempotencyKey);

        order.setOrderItems(cartItems.stream().map(ci -> {
            com.ecommerce.domain.OrderItem oi = new com.ecommerce.domain.OrderItem();
            oi.setOrder(order);
            oi.setProductId(ci.getProductId());
            oi.setProductName(ci.getProduct() != null ? ci.getProduct().getName() : "Unknown Product");
            oi.setQuantity(ci.getQuantity());
            oi.setUnitPrice(ci.getUnitPrice());
            oi.setLineTotal(ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
            return oi;
        }).toList());

        try {
            orderRepository.save(order);
        } catch (DataIntegrityViolationException e) {
            var dup = orderRepository.findByIdempotencyKey(idempotencyKey);
            if (dup.isPresent()) {
                return orderService.buildConfirmation(dup.get());
            }
            throw e;
        }

        cartItemRepository.clearCartByCustomerId(customerId);

        return orderService.buildConfirmation(order);
    }

    @Override
    public boolean validateAddress(String streetAddress, String city, String stateProvince, String postalCode, String country) {
        return streetAddress != null && !streetAddress.isBlank()
            && city != null && !city.isBlank()
            && stateProvince != null && !stateProvince.isBlank()
            && postalCode != null && !postalCode.isBlank()
            && country != null && !country.isBlank();
    }

    @Override
    public List<AddressDto> getCustomerAddresses(Integer customerId) {
        return addressRepository.findByCustomerId(customerId).stream()
            .map(a -> new AddressDto(
                a.getId(), a.getStreetAddress(), a.getCity(),
                a.getStateProvince(), a.getPostalCode(), a.getCountry(), a.getIsDefault()))
            .toList();
    }

    @Override
    public OrderConfirmationDto getOrderConfirmation(Integer orderId) {
        return orderRepository.findOrderWithItems(orderId)
            .map(orderService::buildConfirmation)
            .orElse(null);
    }

    @Override
    public List<ShippingOptionDto> getShippingOptions() {
        return List.of(
            new ShippingOptionDto("Standard", "Standard Shipping (5-7 business days)", new BigDecimal("99")),
            new ShippingOptionDto("Express", "Express Shipping (2-3 business days)", new BigDecimal("199"))
        );
    }

    @Override
    public boolean validatePayment(PaymentRequest request) {
        if (request == null) return false;
        if (request.cardholderName() == null || request.cardholderName().isBlank()) return false;
        if (request.cardNumber() == null || !CARD_NUMBER_PATTERN.matcher(request.cardNumber()).matches()) return false;
        if (request.expirationDate() == null || !EXPIRY_PATTERN.matcher(request.expirationDate()).matches()) return false;
        if (request.cvv() == null || !CVV_PATTERN.matcher(request.cvv()).matches()) return false;
        return true;
    }

    private static String generateOrderNumber() {
        String datePart = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now());
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "RF-" + datePart + "-" + randomPart;
    }
}
