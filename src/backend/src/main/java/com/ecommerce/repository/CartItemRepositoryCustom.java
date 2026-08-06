package com.ecommerce.repository;

import com.ecommerce.domain.CartItem;

import java.util.List;

public interface CartItemRepositoryCustom {
    List<CartItem> getCartItemsByCustomer(Integer customerId);
}
