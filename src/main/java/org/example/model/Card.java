package org.example.model;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(name = "cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")
    private Integer cardId;
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    @Column(name = "account_id")
    private Integer accountId; 
    @Column(name = "card_number", nullable = false, length = 25)
    private String cardNumber; 
    @Column(name = "card_holder_name", nullable = false, length = 100)
    private String cardHolderName;
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;
    @Column(name = "cvv", length = 4)
    private String cvv; 
    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false)
    private CardTypeEnum cardType;
    @Column(name = "brand", length = 50)
    private String brand; 
    @Column(name = "status", length = 20)
    private String status; 
    @Column(name = "limit_amount", precision = 15, scale = 2)
    private java.math.BigDecimal limitAmount; 
    @Column(name = "current_balance", precision = 15, scale = 2)
    private java.math.BigDecimal currentBalance; 
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    public Card() {
        this.status = "ACTIVE";
        this.currentBalance = java.math.BigDecimal.ZERO;
    }
    public Card(Integer userId, String cardNumber, String cardHolderName, LocalDate expiryDate, CardTypeEnum cardType) {
        this.userId = userId;
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expiryDate = expiryDate;
        this.cardType = cardType;
        this.status = "ACTIVE";
        this.currentBalance = java.math.BigDecimal.ZERO;
    }
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (status == null) {
            status = "ACTIVE";
        }
        if (currentBalance == null) {
            currentBalance = java.math.BigDecimal.ZERO;
        }
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    public Integer getCardId() {
        return cardId;
    }
    public void setCardId(Integer cardId) {
        this.cardId = cardId;
    }
    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public Integer getAccountId() {
        return accountId;
    }
    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }
    public String getCardNumber() {
        return cardNumber;
    }
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    public String getCardHolderName() {
        return cardHolderName;
    }
    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }
    public LocalDate getExpiryDate() {
        return expiryDate;
    }
    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }
    public String getCvv() {
        return cvv;
    }
    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
    public CardTypeEnum getCardType() {
        return cardType;
    }
    public void setCardType(CardTypeEnum cardType) {
        this.cardType = cardType;
    }
    public String getBrand() {
        return brand;
    }
    public void setBrand(String brand) {
        this.brand = brand;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public java.math.BigDecimal getLimitAmount() {
        return limitAmount;
    }
    public void setLimitAmount(java.math.BigDecimal limitAmount) {
        this.limitAmount = limitAmount;
    }
    public java.math.BigDecimal getCurrentBalance() {
        return currentBalance;
    }
    public void setCurrentBalance(java.math.BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}