package com.ecommerce.controller;

import com.ecommerce.dto.AddCartItemRequest;
import com.ecommerce.dto.CartSummaryDto;
import com.ecommerce.security.SecurityUtils;
import com.ecommerce.service.ICartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final ICartService cartService;

    public CartController(ICartService cartService) {
        this.cartService = cartService;
    }

    private Integer getCustomerId() {
        Integer id = SecurityUtils.currentCustomerId();
        if (id == null) throw new IllegalStateException("customer_id claim missing or invalid.");
        return id;
    }

    @GetMapping
    public ResponseEntity<?> get() {
        return ResponseEntity.ok(cartService.getCartItems(getCustomerId()));
    }

    @GetMapping("/summary")
    public ResponseEntity<CartSummaryDto> getSummary() {
        return ResponseEntity.ok(cartService.getCartSummary(getCustomerId()));
    }

    @PostMapping("/items")
    public ResponseEntity<?> addItem(@RequestBody AddCartItemRequest request) {
        if (request.quantity() == null || request.quantity() < 1 || request.quantity() > 999) {
            return ResponseEntity.badRequest().body(Map.of("message", "Quantity must be between 1 and 999."));
        }
        cartService.addToCart(getCustomerId(), request.productId(), request.quantity(), request.variantId());
        CartSummaryDto summary = cartService.getCartSummary(getCustomerId());
        return ResponseEntity.ok(Map.of("success", true, "itemCount", summary.itemCount(), "cartTotal", summary.total()));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Integer id, @RequestBody Integer quantity) {
        if (quantity == null || quantity < 0 || quantity > 999) {
            return ResponseEntity.badRequest().body(Map.of("message", "Quantity must be between 0 and 999."));
        }
        cartService.updateQuantity(getCustomerId(), id, quantity);
        CartSummaryDto summary = cartService.getCartSummary(getCustomerId());
        return ResponseEntity.ok(Map.of("success", true, "itemCount", summary.itemCount(), "cartTotal", summary.total()));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<?> removeItem(@PathVariable Integer id) {
        cartService.removeFromCart(getCustomerId(), id);
        CartSummaryDto summary = cartService.getCartSummary(getCustomerId());
        return ResponseEntity.ok(Map.of("success", true, "itemCount", summary.itemCount(), "cartTotal", summary.total()));
    }

    @DeleteMapping
    public ResponseEntity<?> clear() {
        cartService.clearCart(getCustomerId());
        return ResponseEntity.ok(Map.of("success", true));
    }
}
