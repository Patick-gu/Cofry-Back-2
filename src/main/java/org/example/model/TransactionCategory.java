package org.example.model;
import javax.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "transaction_categories")
public class TransactionCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer categoryId;
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;
    @Column(name = "icon_code", length = 50)
    private String iconCode;
    @Column(name = "created_at", nullable = true, updatable = false)
    private LocalDateTime createdAt;
    public TransactionCategory() {
    }
    public TransactionCategory(String name, String iconCode) {
        this.name = name;
        this.iconCode = iconCode;
    }
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    public Integer getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getIconCode() {
        return iconCode;
    }
    public void setIconCode(String iconCode) {
        this.iconCode = iconCode;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}