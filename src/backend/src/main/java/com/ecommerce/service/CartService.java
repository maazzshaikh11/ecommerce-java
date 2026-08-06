package com.ecommerce.service;

import com.ecommerce.dto.CartItemDto;
import com.ecommerce.dto.CartSummaryDto;
import com.ecommerce.domain.CartItem;
import com.ecommerce.domain.Product;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartItemRepositoryCustom;
import com.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartService implements ICartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<CartItemDto> getCartItems(Integer customerId) {
        List<CartItem> items = ((CartItemRepositoryCustom) cartItemRepository).getCartItemsByCustomer(customerId);
        return items.stream().map(ci -> new CartItemDto(
            ci.getId(),
            ci.getProductId(),
            ci.getProduct() != null ? ci.getProduct().getName() : "Unknown Product",
            ci.getVariant() != null ? ci.getVariant().getVariantName() : null,
            ci.getProduct() != null ? ci.getProduct().getThumbnailUrl() : null,
            ci.getUnitPrice(),
            ci.getQuantity(),
            ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity()))
        )).toList();
    }

    @Override
    public CartSummaryDto getCartSummary(Integer customerId) {
        List<CartItem> items = ((CartItemRepositoryCustom) cartItemRepository).getCartItemsByCustomer(customerId);
        int itemCount = items.stream().mapToInt(CartItem::getQuantity).sum();
        BigDecimal subTotal = items.stream()
            .map(ci -> ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartSummaryDto(itemCount, subTotal, subTotal);
    }

    @Override
    public void addToCart(Integer customerId, Integer productId, Integer quantity, Integer variantId) {
        if (quantity == null || quantity < 1) quantity = 1;

        List<CartItem> existing = ((CartItemRepositoryCustom) cartItemRepository).getCartItemsByCustomer(customerId);
        CartItem match = existing.stream()
            .filter(ci -> ci.getProductId().equals(productId) &&
                   (variantId == null ? ci.getVariantId() == null : variantId.equals(ci.getVariantId())))
            .findFirst()
            .orElse(null);

        if (match != null) {
            match.setQuantity(match.getQuantity() + quantity);
            cartItemRepository.save(match);
            return;
        }

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        CartItem item = new CartItem();
        item.setCustomerId(customerId);
        item.setProductId(productId);
        item.setVariantId(variantId);
        item.setQuantity(quantity);
        item.setUnitPrice(product.getPrice());
        item.setCreatedDate(LocalDateTime.now());
        cartItemRepository.save(item);
    }

    @Override
    public void updateQuantity(Integer customerId, Integer cartItemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(cartItemId).orElse(null);
        if (item == null || !customerId.equals(item.getCustomerId())) return;

        if (quantity == null || quantity < 1) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
    }

    @Override
    public void removeFromCart(Integer customerId, Integer cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId).orElse(null);
        if (item == null || !customerId.equals(item.getCustomerId())) return;
        cartItemRepository.delete(item);
    }

    @Override
    public void clearCart(Integer customerId) {
        cartItemRepository.clearCartByCustomerId(customerId);
    }
}
