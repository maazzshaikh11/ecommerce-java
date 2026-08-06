package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.service.IProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductsController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IProductService productService;

    @Test
    void getFeaturedProducts_shouldReturnList() throws Exception {
        ProductListResponseDto dto = new ProductListResponseDto();
        dto.setId(1);
        dto.setName("Mouse");
        dto.setPrice(new BigDecimal("999"));
        dto.setThumbnailUrl("/thumb.jpg");

        when(productService.getFeaturedProducts(6)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/products/featured"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Mouse"));
    }

    @Test
    void getProductById_shouldReturnProduct() throws Exception {
        ProductDetailDto dto = new ProductDetailDto();
        dto.setId(1);
        dto.setName("Mouse");
        dto.setDescription("Desc");
        dto.setPrice(new BigDecimal("999"));
        dto.setImageUrl("/img.jpg");
        dto.setThumbnailUrl("/thumb.jpg");
        dto.setImages(Collections.emptyList());
        dto.setVariants(Collections.emptyList());

        when(productService.getProductDetail(1)).thenReturn(dto);

        mockMvc.perform(get("/api/products/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Mouse"));
    }

    @Test
    void getProductById_whenNotFound_shouldReturn404() throws Exception {
        when(productService.getProductDetail(99)).thenReturn(null);

        mockMvc.perform(get("/api/products/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void searchProducts_shouldReturnResults() throws Exception {
        PagedResult<ProductListResponseDto> result = new PagedResult<>();
        result.setItems(Collections.emptyList());
        result.setCurrentPage(1);
        result.setPageSize(12);
        result.setTotalCount(0);
        result.setTotalPages(0);

        when(productService.getProducts(any(), any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(result);

        mockMvc.perform(get("/api/products").param("searchQuery", "mouse"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCount").value(0));
    }
}
