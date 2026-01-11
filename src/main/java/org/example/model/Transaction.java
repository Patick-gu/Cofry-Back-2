package org.example.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @Column(name = "transaction_id", columnDefinition = "uuid")
    private UUID transactionId;
    
    @Column(name = "source_account_id", nullable = false, columnDefinition = "uuid")
    private UUID sourceAccountId;
    
    @Column(name = "destination_account_id", columnDefinition = "uuid")
    private UUID destinationAccountId;
    
    @Column(name = "category_id")
    private Integer categoryId;
    
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionTypeEnum transactionType;
    
    @Column(name = "description", nullable = false, length = 255)
    private String description;
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;
    
    @Column(name = "is_recurring")
    private Boolean isRecurring;
    
    @Column(name = "installment_current")
    private Integer installmentCurrent;
    
    @Column(name = "installment_total")
    private Integer installmentTotal;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public Transaction() {
        this.transactionDate = LocalDate.now();
        this.isRecurring = false;
    }
    
    public Transaction(UUID sourceAccountId, BigDecimal amount, TransactionTypeEnum transactionType, String description) {
        this.sourceAccountId = sourceAccountId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.description = description;
        this.transactionDate = LocalDate.now();
        this.isRecurring = false;
    }
    
    @PrePersist
    protected void onCreate() {
        if (transactionDate == null) {
            transactionDate = LocalDate.now();
        }
        if (isRecurring == null) {
            isRecurring = false;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    public UUID getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }
    
    public UUID getSourceAccountId() {
        return sourceAccountId;
    }
    
    public void setSourceAccountId(UUID sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }
    
    public UUID getDestinationAccountId() {
        return destinationAccountId;
    }
    
    public void setDestinationAccountId(UUID destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }
    
    public Integer getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public TransactionTypeEnum getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(TransactionTypeEnum transactionType) {
        this.transactionType = transactionType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDate getTransactionDate() {
        return transactionDate;
    }
    
    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }
    
    public Boolean getIsRecurring() {
        return isRecurring;
    }
    
    public void setIsRecurring(Boolean isRecurring) {
        this.isRecurring = isRecurring;
    }
    
    public Integer getInstallmentCurrent() {
        return installmentCurrent;
    }
    
    public void setInstallmentCurrent(Integer installmentCurrent) {
        this.installmentCurrent = installmentCurrent;
    }
    
    public Integer getInstallmentTotal() {
        return installmentTotal;
    }
    
    public void setInstallmentTotal(Integer installmentTotal) {
        this.installmentTotal = installmentTotal;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
