package org.example.model;
import javax.persistence.*;
import java.math.BigDecimal;
@Entity
@Table(name = "asset", schema = "investments")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "ticker", nullable = false, length = 10, unique = true)
    private String ticker;
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    @Column(name = "category_id", nullable = false)
    private Integer categoryId;
    @Column(name = "api_identifier", nullable = false, length = 50)
    private String apiIdentifier;
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    public Asset() {
        this.isActive = true;
    }
    public Asset(String ticker, String name, Integer categoryId, String apiIdentifier) {
        this.ticker = ticker;
        this.name = name;
        this.categoryId = categoryId;
        this.apiIdentifier = apiIdentifier;
        this.isActive = true;
    }
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getTicker() {
        return ticker;
    }
    public void setTicker(String ticker) {
        this.ticker = ticker;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Integer getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
    public String getApiIdentifier() {
        return apiIdentifier;
    }
    public void setApiIdentifier(String apiIdentifier) {
        this.apiIdentifier = apiIdentifier;
    }
    public Boolean getIsActive() {
        return isActive;
    }
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}