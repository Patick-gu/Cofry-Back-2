package org.example.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
public class OrderHistoryItemDTO {
    private Integer transactionId;
    private String assetTicker;      
    private String assetName;        
    private String type;             
    private LocalDate date;          
    private BigDecimal value;        
    private String status;           
    public OrderHistoryItemDTO() {
    }
    public OrderHistoryItemDTO(Integer transactionId, String assetTicker, String type, LocalDate date, BigDecimal value, String status) {
        this.transactionId = transactionId;
        this.assetTicker = assetTicker;
        this.type = type;
        this.date = date;
        this.value = value;
        this.status = status;
    }
    public Integer getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
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
    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }
    public BigDecimal getValue() {
        return value;
    }
    public void setValue(BigDecimal value) {
        this.value = value;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Boolean isBuy() {
        return "Compra".equalsIgnoreCase(type) || "BUY".equalsIgnoreCase(type);
    }
    public Boolean isSell() {
        return "Venda".equalsIgnoreCase(type) || "SELL".equalsIgnoreCase(type);
    }
}