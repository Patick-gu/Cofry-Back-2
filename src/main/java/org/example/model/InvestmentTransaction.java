package org.example.model;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "transaction", schema = "investments")
public class InvestmentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    @Column(name = "asset_id", nullable = false)
    private Integer assetId;
    @Column(name = "type", nullable = false, length = 10)
    private String type; 
    @Column(name = "price", nullable = false, precision = 18, scale = 8)
    private BigDecimal price;
    @Column(name = "quantity", nullable = false, precision = 18, scale = 8)
    private BigDecimal quantity;
    @Column(name = "total_value", nullable = false, precision = 18, scale = 8)
    private BigDecimal totalValue;
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    public InvestmentTransaction() {
        this.transactionDate = LocalDateTime.now();
        this.status = "COMPLETED";
    }
    public InvestmentTransaction(Integer userId, Integer assetId, String type, 
                                 BigDecimal price, BigDecimal quantity) {
        this.userId = userId;
        this.assetId = assetId;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.totalValue = price.multiply(quantity);
        this.transactionDate = LocalDateTime.now();
        this.status = "COMPLETED";
    }
    @PrePersist
    protected void onCreate() {
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
        if (status == null) {
            status = "COMPLETED";
        }
        if (totalValue == null && price != null && quantity != null) {
            totalValue = price.multiply(quantity);
        }
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
        if (this.quantity != null && price != null) {
            this.totalValue = price.multiply(this.quantity);
        }
    }
    public BigDecimal getQuantity() {
        return quantity;
    }
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        if (this.price != null && quantity != null) {
            this.totalValue = this.price.multiply(quantity);
        }
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