package org.example.dto;
public class BalanceUpdateDTO {
    private String balance; 
    public BalanceUpdateDTO() {
    }
    public BalanceUpdateDTO(String balance) {
        this.balance = balance;
    }
    public String getBalance() {
        return balance;
    }
    public void setBalance(String balance) {
        this.balance = balance;
    }
}