package com.ecommerce.service;

import com.ecommerce.dto.CartItemDto;
import com.ecommerce.dto.CartSummaryDto;

import java.util.List;

public interface ICartService {
    List<CartItemDto> getCartItems(Integer customerId);
    CartSummaryDto getCartSummary(Integer customerId);
    void addToCart(Integer customerId, Integer productId, Integer quantity, Integer variantId);
    void updateQuantity(Integer customerId, Integer cartItemId, Integer quantity);
    void removeFromCart(Integer customerId, Integer cartItemId);
    void clearCart(Integer customerId);
}
