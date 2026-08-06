package com.ecommerce.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ProductVariant")
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductId", nullable = false)
    private Product product;

    @Column(name = "VariantName", nullable = false)
    private String variantName;

    @Column(name = "VariantValue", nullable = false)
    private String variantValue;

    @Column(name = "PriceAdjustment", nullable = false, precision = 18, scale = 2)
    private BigDecimal priceAdjustment;

    @Column(name = "StockQuantity", nullable = false)
    private Integer stockQuantity;

    public ProductVariant() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Integer getProductId() { return product != null ? product.getId() : null; }
    public String getVariantName() { return variantName; }
    public void setVariantName(String variantName) { this.variantName = variantName; }
    public String getVariantValue() { return variantValue; }
    public void setVariantValue(String variantValue) { this.variantValue = variantValue; }
    public BigDecimal getPriceAdjustment() { return priceAdjustment; }
    public void setPriceAdjustment(BigDecimal priceAdjustment) { this.priceAdjustment = priceAdjustment; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
}
