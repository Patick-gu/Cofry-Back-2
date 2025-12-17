package org.example.dto;
public class PixRequestDTO {
    private Integer sourceAccountId; 
    private Integer destinationAccountId; 
    private Integer destinationUserId; 
    private String amount; 
    private String description; 
    public PixRequestDTO() {
    }
    public Integer getSourceAccountId() {
        return sourceAccountId;
    }
    public void setSourceAccountId(Integer sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }
    public Integer getDestinationAccountId() {
        return destinationAccountId;
    }
    public void setDestinationAccountId(Integer destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }
    public Integer getDestinationUserId() {
        return destinationUserId;
    }
    public void setDestinationUserId(Integer destinationUserId) {
        this.destinationUserId = destinationUserId;
    }
    public String getAmount() {
        return amount;
    }
    public void setAmount(String amount) {
        this.amount = amount;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}