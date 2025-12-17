package org.example.model;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "user_asset", schema = "investments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "asset_id"}))
public class UserAsset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    @Column(name = "asset_id", nullable = false)
    private Integer assetId;
    @Column(name = "quantity", nullable = false, precision = 18, scale = 8)
    private BigDecimal quantity;
    @Column(name = "average_price", nullable = false, precision = 18, scale = 8)
    private BigDecimal averagePrice;
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    public UserAsset() {
        this.quantity = BigDecimal.ZERO;
        this.lastUpdated = LocalDateTime.now();
    }
    public UserAsset(Integer userId, Integer assetId, BigDecimal quantity, BigDecimal averagePrice) {
        this.userId = userId;
        this.assetId = assetId;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
        this.lastUpdated = LocalDateTime.now();
    }
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
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
    public BigDecimal getQuantity() {
        return quantity;
    }
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
    public BigDecimal getAveragePrice() {
        return averagePrice;
    }
    public void setAveragePrice(BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
    }
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    public BigDecimal getTotalValue() {
        if (quantity == null || averagePrice == null) {
            return BigDecimal.ZERO;
        }
        return quantity.multiply(averagePrice);
    }
}