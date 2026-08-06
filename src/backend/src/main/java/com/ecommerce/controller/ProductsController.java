package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.service.IProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductsController {

    private final IProductService productService;

    public ProductsController(IProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<PagedResult<ProductListResponseDto>> get(
        @RequestParam(required = false) Integer categoryId,
        @RequestParam(required = false) String searchQuery,
        @RequestParam(required = false) java.math.BigDecimal minPrice,
        @RequestParam(required = false) java.math.BigDecimal maxPrice,
        @RequestParam(required = false) String sortBy,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "12") int pageSize) {
        return ResponseEntity.ok(productService.getProducts(categoryId, searchQuery, minPrice, maxPrice, sortBy, page, pageSize));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        ProductDetailDto dto = productService.getProductDetail(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ProductListResponseDto>> featured(@RequestParam(defaultValue = "6") int count) {
        return ResponseEntity.ok(productService.getFeaturedProducts(count));
    }
}
