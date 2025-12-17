package org.example.model;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "budgets", 
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"user_id", "category_id", "period_month", "period_year"}
       ))
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_id")
    private Integer budgetId;
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    @Column(name = "category_id", nullable = false)
    private Integer categoryId;
    @Column(name = "amount_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountLimit;
    @Column(name = "period_month", nullable = false)
    private Integer periodMonth;
    @Column(name = "period_year", nullable = false)
    private Integer periodYear;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    public Budget() {
    }
    public Budget(Integer userId, Integer categoryId, BigDecimal amountLimit, Integer periodMonth, Integer periodYear) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.amountLimit = amountLimit;
        this.periodMonth = periodMonth;
        this.periodYear = periodYear;
    }
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    public Integer getBudgetId() {
        return budgetId;
    }
    public void setBudgetId(Integer budgetId) {
        this.budgetId = budgetId;
    }
    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public Integer getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
    public BigDecimal getAmountLimit() {
        return amountLimit;
    }
    public void setAmountLimit(BigDecimal amountLimit) {
        this.amountLimit = amountLimit;
    }
    public Integer getPeriodMonth() {
        return periodMonth;
    }
    public void setPeriodMonth(Integer periodMonth) {
        this.periodMonth = periodMonth;
    }
    public Integer getPeriodYear() {
        return periodYear;
    }
    public void setPeriodYear(Integer periodYear) {
        this.periodYear = periodYear;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}