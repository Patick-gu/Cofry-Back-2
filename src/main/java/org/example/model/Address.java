package org.example.model;
import javax.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "addresses")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Integer addressId;
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    @Column(name = "street", nullable = false, length = 255)
    private String street;
    @Column(name = "number", nullable = false, length = 20)
    private String number;
    @Column(name = "complement", length = 100)
    private String complement;
    @Column(name = "neighborhood", length = 100)
    private String neighborhood;
    @Column(name = "city", nullable = false, length = 100)
    private String city;
    @Column(name = "state", nullable = false, length = 50)
    private String state;
    @Column(name = "zip_code", nullable = false, length = 20)
    private String zipCode;
    @Column(name = "country", length = 50)
    private String country;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    public Address() {
        this.country = "Brazil";
    }
    public Address(Integer userId, String street, String number, String city, String state, String zipCode) {
        this.userId = userId;
        this.street = street;
        this.number = number;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = "Brazil";
    }
    @PrePersist
    protected void onCreate() {
        if (country == null) {
            country = "Brazil";
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    public Integer getAddressId() {
        return addressId;
    }
    public void setAddressId(Integer addressId) {
        this.addressId = addressId;
    }
    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public String getStreet() {
        return street;
    }
    public void setStreet(String street) {
        this.street = street;
    }
    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public String getComplement() {
        return complement;
    }
    public void setComplement(String complement) {
        this.complement = complement;
    }
    public String getNeighborhood() {
        return neighborhood;
    }
    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
    public String getZipCode() {
        return zipCode;
    }
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}