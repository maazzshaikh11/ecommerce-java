package com.ecommerce.service;

import com.ecommerce.dto.CategoryDto;
import com.ecommerce.domain.Category;
import com.ecommerce.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService implements ICategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategoryDto> getCategoryTree() {
        List<Category> all = categoryRepository.findAllWithProducts();
        List<Category> roots = all.stream()
            .filter(c -> c.getParentCategoryId() == null)
            .toList();
        return roots.stream()
            .map(c -> buildNode(c, all))
            .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getById(Integer id) {
        return categoryRepository.findById(id)
            .map(this::mapCategory)
            .orElse(null);
    }

    private CategoryDto buildNode(Category category, List<Category> all) {
        List<CategoryDto> children = all.stream()
            .filter(c -> category.getId().equals(c.getParentCategoryId()))
            .map(c -> buildNode(c, all))
            .toList();

        CategoryDto dto = mapCategory(category);
        dto.setChildren(children.isEmpty() ? null : children);
        return dto;
    }

    private CategoryDto mapCategory(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setThumbnailUrl(category.getThumbnailUrl());
        dto.setParentCategoryId(category.getParentCategoryId());
        return dto;
    }
}
