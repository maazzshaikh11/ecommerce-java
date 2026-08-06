package com.ecommerce.service;

import com.ecommerce.dto.CategoryDto;

import java.util.List;

public interface ICategoryService {
    List<CategoryDto> getCategoryTree();
    CategoryDto getById(Integer id);
}
