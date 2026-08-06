package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.security.JwtPrincipal;
import com.ecommerce.service.ICartService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ICartService cartService;

    @BeforeEach
    void setUpAuth() {
        JwtPrincipal principal = new JwtPrincipal(1, "test@example.com");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDownAuth() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCart_shouldReturnCartItems() throws Exception {
        CartItemDto item = new CartItemDto(1, 2, "Mouse", null, "/img.jpg", new BigDecimal("999"), 2, new BigDecimal("1998"));
        when(cartService.getCartItems(1)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/cart"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].productName").value("Mouse"));
    }

    @Test
    void getCartSummary_shouldReturnSummary() throws Exception {
        when(cartService.getCartSummary(1)).thenReturn(
            new CartSummaryDto(2, new BigDecimal("1998"), new BigDecimal("1998"))
        );

        mockMvc.perform(get("/api/cart/summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.itemCount").value(2))
            .andExpect(jsonPath("$.total").value(1998));
    }

    @Test
    void addToCart_shouldReturnUpdatedSummary() throws Exception {
        when(cartService.getCartSummary(1)).thenReturn(
            new CartSummaryDto(3, new BigDecimal("2997"), new BigDecimal("2997"))
        );

        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":2,\"quantity\":1,\"variantId\":null}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.itemCount").value(3));
    }

    @Test
    void updateCartItem_shouldReturnUpdatedSummary() throws Exception {
        when(cartService.getCartSummary(1)).thenReturn(
            new CartSummaryDto(1, new BigDecimal("999"), new BigDecimal("999"))
        );

        mockMvc.perform(put("/api/cart/items/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content("3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.itemCount").value(1));
    }

    @Test
    void removeCartItem_shouldReturnUpdatedSummary() throws Exception {
        when(cartService.getCartSummary(1)).thenReturn(
            new CartSummaryDto(0, BigDecimal.ZERO, BigDecimal.ZERO)
        );

        mockMvc.perform(delete("/api/cart/items/5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.itemCount").value(0));
    }

    @Test
    void clearCart_shouldReturnSuccess() throws Exception {
        mockMvc.perform(delete("/api/cart"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
}
