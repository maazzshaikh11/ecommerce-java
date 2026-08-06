package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.domain.Product;

import java.util.List;

public interface IProductService {
    PagedResult<ProductListResponseDto> getProducts(Integer categoryId, String keyword,
                                                      java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice,
                                                      String sortBy, int page, int pageSize);
    ProductDetailDto getProductDetail(Integer id);
    List<ProductListResponseDto> getFeaturedProducts(int count);
}
