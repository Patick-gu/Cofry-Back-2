package org.example.model;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Integer accountId;
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    @Column(name = "bank_code", length = 3)
    private String bankCode;
    @Column(name = "bank_name", length = 100)
    private String bankName;
    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;
    @Column(name = "agency_number", nullable = false, length = 10)
    private String agencyNumber;
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountTypeEnum accountType;
    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "status", length = 20)
    private String status;
    public Account() {
        this.accountType = AccountTypeEnum.CHECKING;
        this.balance = BigDecimal.ZERO;
        this.status = "ACTIVE";
    }
    public Account(Integer userId, String accountNumber, String agencyNumber, AccountTypeEnum accountType) {
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.agencyNumber = agencyNumber;
        this.accountType = accountType;
        this.balance = BigDecimal.ZERO;
        this.status = "ACTIVE";
    }
    @PrePersist
    protected void onCreate() {
        if (accountType == null) {
            accountType = AccountTypeEnum.CHECKING;
        }
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
        if (status == null) {
            status = "ACTIVE";
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    public Integer getAccountId() {
        return accountId;
    }
    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }
    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public String getAccountNumber() {
        return accountNumber;
    }
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    public String getAgencyNumber() {
        return agencyNumber;
    }
    public void setAgencyNumber(String agencyNumber) {
        this.agencyNumber = agencyNumber;
    }
    public String getBankCode() {
        return bankCode;
    }
    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }
    public String getBankName() {
        return bankName;
    }
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
    public AccountTypeEnum getAccountType() {
        return accountType;
    }
    public void setAccountType(AccountTypeEnum accountType) {
        this.accountType = accountType;
    }
    public BigDecimal getBalance() {
        return balance;
    }
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
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
}