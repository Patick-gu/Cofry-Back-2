package org.example.model;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(name = "savings_goals")
public class SavingsGoal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "goal_id")
    private Integer goalId;
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    @Column(name = "target_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal targetAmount;
    @Column(name = "current_amount", precision = 15, scale = 2)
    private BigDecimal currentAmount;
    @Column(name = "target_date")
    private LocalDate targetDate;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private GoalStatusEnum status;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    public SavingsGoal() {
        this.currentAmount = BigDecimal.ZERO;
        this.status = GoalStatusEnum.IN_PROGRESS;
    }
    public SavingsGoal(Integer userId, String name, BigDecimal targetAmount) {
        this.userId = userId;
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = BigDecimal.ZERO;
        this.status = GoalStatusEnum.IN_PROGRESS;
    }
    @PrePersist
    protected void onCreate() {
        if (currentAmount == null) {
            currentAmount = BigDecimal.ZERO;
        }
        if (status == null) {
            status = GoalStatusEnum.IN_PROGRESS;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    public Integer getGoalId() {
        return goalId;
    }
    public void setGoalId(Integer goalId) {
        this.goalId = goalId;
    }
    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public BigDecimal getTargetAmount() {
        return targetAmount;
    }
    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }
    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }
    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
    }
    public LocalDate getTargetDate() {
        return targetDate;
    }
    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }
    public GoalStatusEnum getStatus() {
        return status;
    }
    public void setStatus(GoalStatusEnum status) {
        this.status = status;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}