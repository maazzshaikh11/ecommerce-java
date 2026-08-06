package com.ecommerce.dto;

public class ProductImageDto {
    private Integer id;
    private String imageUrl;
    private Boolean isPrimary;
    private Integer displayOrder;

    public ProductImageDto() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Boolean getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}
