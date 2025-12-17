package org.example.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
public class BoletoResponseDTO {
    private Long id;
    private String title;
    private BigDecimal amount;
    private String formattedAmount; 
    private LocalDate dueDate;
    private String status;
    private String statusLabel; 
    private String bankCode;
    private String walletCode;
    private String ourNumber;
    private String boletoCode;
    private Integer userId;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public BoletoResponseDTO() {
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public String getFormattedAmount() {
        return formattedAmount;
    }
    public void setFormattedAmount(String formattedAmount) {
        this.formattedAmount = formattedAmount;
    }
    public LocalDate getDueDate() {
        return dueDate;
    }
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getStatusLabel() {
        return statusLabel;
    }
    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }
    public String getBankCode() {
        return bankCode;
    }
    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }
    public String getWalletCode() {
        return walletCode;
    }
    public void setWalletCode(String walletCode) {
        this.walletCode = walletCode;
    }
    public String getOurNumber() {
        return ourNumber;
    }
    public void setOurNumber(String ourNumber) {
        this.ourNumber = ourNumber;
    }
    public String getBoletoCode() {
        return boletoCode;
    }
    public void setBoletoCode(String boletoCode) {
        this.boletoCode = boletoCode;
    }
    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public LocalDateTime getPaidAt() {
        return paidAt;
    }
    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
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