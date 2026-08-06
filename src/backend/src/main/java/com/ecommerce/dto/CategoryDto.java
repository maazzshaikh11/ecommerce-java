package com.ecommerce.dto;

import java.util.List;

public class CategoryDto {
    private Integer id;
    private String name;
    private String description;
    private String thumbnailUrl;
    private Integer parentCategoryId;
    private List<CategoryDto> children;

    public CategoryDto() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public Integer getParentCategoryId() { return parentCategoryId; }
    public void setParentCategoryId(Integer parentCategoryId) { this.parentCategoryId = parentCategoryId; }
    public List<CategoryDto> getChildren() { return children; }
    public void setChildren(List<CategoryDto> children) { this.children = children; }
}
