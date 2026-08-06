package com.ecommerce.dto;

import java.math.BigDecimal;

public class ProductVariantDto {
    private Integer id;
    private String variantName;
    private String variantValue;
    private BigDecimal priceAdjustment;
    private Integer stockQuantity;

    public ProductVariantDto() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getVariantName() { return variantName; }
    public void setVariantName(String variantName) { this.variantName = variantName; }
    public String getVariantValue() { return variantValue; }
    public void setVariantValue(String variantValue) { this.variantValue = variantValue; }
    public BigDecimal getPriceAdjustment() { return priceAdjustment; }
    public void setPriceAdjustment(BigDecimal priceAdjustment) { this.priceAdjustment = priceAdjustment; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
}
