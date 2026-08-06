package com.ecommerce.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "IdentityUserId")
    private String identityUserId;

    @Column(name = "Email", nullable = false)
    private String email;

    @Column(name = "FirstName", nullable = false)
    private String firstName;

    @Column(name = "LastName", nullable = false)
    private String lastName;

    @Column(name = "PhoneNumber")
    private String phoneNumber;

    @Column(name = "CreatedDate", nullable = false)
    private LocalDateTime createdDate;

    public Customer() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getIdentityUserId() { return identityUserId; }
    public void setIdentityUserId(String identityUserId) { this.identityUserId = identityUserId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}
