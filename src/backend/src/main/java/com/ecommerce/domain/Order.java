package com.ecommerce.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "EcommerceOrder")
@Table(name = "\"Order\"")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CustomerId", nullable = false)
    private Integer customerId;

    @Column(name = "OrderDate", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "OrderStatus", nullable = false)
    private String orderStatus;

    @Column(name = "OrderNumber", nullable = false)
    private String orderNumber;

    @Column(name = "SubTotal", nullable = false, precision = 18, scale = 2)
    private BigDecimal subTotal;

    @Column(name = "ShippingTotal", nullable = false, precision = 18, scale = 2)
    private BigDecimal shippingTotal;

    @Column(name = "TaxTotal", nullable = false, precision = 18, scale = 2)
    private BigDecimal taxTotal;

    @Column(name = "GrandTotal", nullable = false, precision = 18, scale = 2)
    private BigDecimal grandTotal;

    @Column(name = "ShippingAddressId")
    private Integer shippingAddressId;

    @Column(name = "BillingAddressId")
    private Integer billingAddressId;

    @Column(name = "IdempotencyKey")
    private String idempotencyKey;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    public Order() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public BigDecimal getSubTotal() { return subTotal; }
    public void setSubTotal(BigDecimal subTotal) { this.subTotal = subTotal; }
    public BigDecimal getShippingTotal() { return shippingTotal; }
    public void setShippingTotal(BigDecimal shippingTotal) { this.shippingTotal = shippingTotal; }
    public BigDecimal getTaxTotal() { return taxTotal; }
    public void setTaxTotal(BigDecimal taxTotal) { this.taxTotal = taxTotal; }
    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }
    public Integer getShippingAddressId() { return shippingAddressId; }
    public void setShippingAddressId(Integer shippingAddressId) { this.shippingAddressId = shippingAddressId; }
    public Integer getBillingAddressId() { return billingAddressId; }
    public void setBillingAddressId(Integer billingAddressId) { this.billingAddressId = billingAddressId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }
}
