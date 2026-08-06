package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.domain.Product;
import com.ecommerce.repository.*;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService implements IProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
                          ProductImageRepository productImageRepository, ProductVariantRepository productVariantRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @Override
    public PagedResult<ProductListResponseDto> getProducts(Integer categoryId, String keyword,
                                                               java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice,
                                                               String sortBy, int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 12;

        List<Product> products;
        if (categoryId != null) {
            products = getProductsByCategoryTree(categoryId);
        } else {
            products = productRepository.findActiveProducts();
        }

        if (keyword != null && !keyword.isBlank()) {
            String term = keyword.trim().toLowerCase();
            products = products.stream()
                .filter(p -> p.getName().toLowerCase().contains(term) ||
                       (p.getDescription() != null && p.getDescription().toLowerCase().contains(term)))
                .collect(Collectors.toList());
        }

        if (minPrice != null) {
            products = products.stream().filter(p -> p.getPrice().compareTo(minPrice) >= 0).collect(Collectors.toList());
        }
        if (maxPrice != null) {
            products = products.stream().filter(p -> p.getPrice().compareTo(maxPrice) <= 0).collect(Collectors.toList());
        }

        String sort = sortBy != null ? sortBy.toLowerCase() : null;
        if ("price_asc".equals(sort)) {
            products.sort(Comparator.comparing(Product::getPrice));
        } else if ("price_desc".equals(sort)) {
            products.sort(Comparator.comparing(Product::getPrice).reversed());
        } else if ("name".equals(sort)) {
            products.sort(Comparator.comparing(Product::getName));
        } else {
            products.sort(Comparator.comparing(Product::getId).reversed());
        }

        int total = products.size();
        int totalPages = (int) Math.ceil((double) total / pageSize);
        List<Product> pageItems = products.stream()
            .skip((long) (page - 1) * pageSize)
            .limit(pageSize)
            .toList();

        List<ProductListResponseDto> dtos = pageItems.stream().map(p -> {
            ProductListResponseDto dto = new ProductListResponseDto();
            dto.setId(p.getId());
            dto.setName(p.getName());
            dto.setPrice(p.getPrice());
            dto.setThumbnailUrl(p.getThumbnailUrl());
            dto.setCategoryName(null);
            return dto;
        }).toList();

        PagedResult<ProductListResponseDto> result = new PagedResult<>();
        result.setItems(dtos);
        result.setCurrentPage(page);
        result.setPageSize(pageSize);
        result.setTotalCount(total);
        result.setTotalPages(totalPages);
        return result;
    }

    private List<Product> getProductsByCategoryTree(Integer rootCategoryId) {
        List<com.ecommerce.domain.Category> allCategories = categoryRepository.findAll();
        HashSet<Integer> targetIds = new HashSet<>();
        targetIds.add(rootCategoryId);
        java.util.Queue<Integer> queue = new java.util.LinkedList<>();
        queue.add(rootCategoryId);
        while (!queue.isEmpty()) {
            Integer parentId = queue.poll();
            for (com.ecommerce.domain.Category c : allCategories) {
                if (parentId.equals(c.getParentCategoryId()) && targetIds.add(c.getId())) {
                    queue.add(c.getId());
                }
            }
        }
        return productRepository.findActiveProducts().stream()
            .filter(p -> p.getIsActive() && targetIds.contains(p.getCategoryId()))
            .collect(Collectors.toList());
    }

    @Override
    public ProductDetailDto getProductDetail(Integer id) {
        Optional<Product> optional = productRepository.findById(id);
        if (optional.isEmpty()) return null;
        Product p = optional.get();

        ProductDetailDto dto = new ProductDetailDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setPrice(p.getPrice());
        dto.setImageUrl(p.getImageUrl());
        dto.setThumbnailUrl(p.getThumbnailUrl());
        dto.setIsActive(p.getIsActive());
        dto.setCreatedDate(p.getCreatedDate());

        categoryRepository.findById(p.getCategoryId()).ifPresent(cat -> dto.setCategoryName(cat.getName()));

        dto.setImages(productImageRepository.findByProductId(p.getId()).stream().map(i -> {
            ProductImageDto img = new ProductImageDto();
            img.setId(i.getId());
            img.setImageUrl(i.getImageUrl());
            img.setIsPrimary(i.getIsPrimary());
            img.setDisplayOrder(i.getDisplayOrder());
            return img;
        }).toList());

        dto.setVariants(productVariantRepository.findByProductId(p.getId()).stream().map(v -> {
            ProductVariantDto vd = new ProductVariantDto();
            vd.setId(v.getId());
            vd.setVariantName(v.getVariantName());
            vd.setVariantValue(v.getVariantValue());
            vd.setPriceAdjustment(v.getPriceAdjustment());
            vd.setStockQuantity(v.getStockQuantity());
            return vd;
        }).toList());

        return dto;
    }

    @Override
    public List<ProductListResponseDto> getFeaturedProducts(int count) {
        return productRepository.findActiveProducts().stream()
            .limit(count)
            .map(p -> {
                ProductListResponseDto dto = new ProductListResponseDto();
                dto.setId(p.getId());
                dto.setName(p.getName());
                dto.setPrice(p.getPrice());
                dto.setThumbnailUrl(p.getThumbnailUrl());
                return dto;
            }).toList();
    }
}
