package com.ecommerce.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "Address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CustomerId", nullable = false)
    private Integer customerId;

    @Column(name = "AddressType", nullable = false)
    private String addressType;

    @Column(name = "StreetAddress", nullable = false)
    private String streetAddress;

    @Column(name = "City", nullable = false)
    private String city;

    @Column(name = "StateProvince")
    private String stateProvince;

    @Column(name = "PostalCode")
    private String postalCode;

    @Column(name = "Country", nullable = false)
    private String country;

    @Column(name = "IsDefault", nullable = false)
    private Boolean isDefault;

    public Address() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    public String getAddressType() { return addressType; }
    public void setAddressType(String addressType) { this.addressType = addressType; }
    public String getStreetAddress() { return streetAddress; }
    public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getStateProvince() { return stateProvince; }
    public void setStateProvince(String stateProvince) { this.stateProvince = stateProvince; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
}
