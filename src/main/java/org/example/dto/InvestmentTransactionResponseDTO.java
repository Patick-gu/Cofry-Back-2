package org.example.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public class InvestmentTransactionResponseDTO {
    private Integer id;
    private Integer userId;
    private Integer assetId;
    private String assetTicker;
    private String assetName;
    private String type; 
    private BigDecimal price;
    private BigDecimal quantity;
    private BigDecimal totalValue;
    private LocalDateTime transactionDate;
    private String status;
    public InvestmentTransactionResponseDTO() {
    }
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public Integer getAssetId() {
        return assetId;
    }
    public void setAssetId(Integer assetId) {
        this.assetId = assetId;
    }
    public String getAssetTicker() {
        return assetTicker;
    }
    public void setAssetTicker(String assetTicker) {
        this.assetTicker = assetTicker;
    }
    public String getAssetName() {
        return assetName;
    }
    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    public BigDecimal getQuantity() {
        return quantity;
    }
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
    public BigDecimal getTotalValue() {
        return totalValue;
    }
    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }
    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }
    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}