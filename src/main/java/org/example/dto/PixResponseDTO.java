package org.example.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
public class PixResponseDTO {
    private Integer transactionId;
    private Integer sourceAccountId;
    private Integer destinationAccountId;
    private Integer sourceUserId;
    private Integer destinationUserId;
    private BigDecimal amount;
    private String description;
    private LocalDate transactionDate;
    private LocalDateTime createdAt;
    private String status; 
    private String message;
    public PixResponseDTO() {
    }
    public Integer getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }
    public Integer getSourceAccountId() {
        return sourceAccountId;
    }
    public void setSourceAccountId(Integer sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }
    public Integer getDestinationAccountId() {
        return destinationAccountId;
    }
    public void setDestinationAccountId(Integer destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }
    public Integer getSourceUserId() {
        return sourceUserId;
    }
    public void setSourceUserId(Integer sourceUserId) {
        this.sourceUserId = sourceUserId;
    }
    public Integer getDestinationUserId() {
        return destinationUserId;
    }
    public void setDestinationUserId(Integer destinationUserId) {
        this.destinationUserId = destinationUserId;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}