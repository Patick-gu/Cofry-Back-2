package org.example.dto;
public class BoletoRequestDTO {
    private String title; 
    private String amount; 
    private String dueDate; 
    private String bankCode; 
    private String walletCode; 
    private String ourNumber; 
    private Integer userId; 
    public BoletoRequestDTO() {
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getAmount() {
        return amount;
    }
    public void setAmount(String amount) {
        this.amount = amount;
    }
    public String getDueDate() {
        return dueDate;
    }
    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
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
    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}