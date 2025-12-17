package org.example.dto;
import java.math.BigDecimal;
public class AssetCardDTO {
    private Integer assetId;
    private String ticker;          
    private String assetName;       
    private BigDecimal currentPrice; 
    private BigDecimal priceChange; 
    private String iconUrl;         
    private String iconColor;       
    private String currency;        
    public AssetCardDTO() {
        this.currency = "BRL";
    }
    public AssetCardDTO(Integer assetId, String ticker, String assetName, BigDecimal currentPrice, BigDecimal priceChange) {
        this.assetId = assetId;
        this.ticker = ticker;
        this.assetName = assetName;
        this.currentPrice = currentPrice;
        this.priceChange = priceChange;
        this.currency = "BRL";
    }
    public Integer getAssetId() {
        return assetId;
    }
    public void setAssetId(Integer assetId) {
        this.assetId = assetId;
    }
    public String getTicker() {
        return ticker;
    }
    public void setTicker(String ticker) {
        this.ticker = ticker;
    }
    public String getAssetName() {
        return assetName;
    }
    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }
    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }
    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }
    public BigDecimal getPriceChange() {
        return priceChange;
    }
    public void setPriceChange(BigDecimal priceChange) {
        this.priceChange = priceChange;
    }
    public String getIconUrl() {
        return iconUrl;
    }
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
    public String getIconColor() {
        return iconColor;
    }
    public void setIconColor(String iconColor) {
        this.iconColor = iconColor;
    }
    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    public Boolean isPositiveChange() {
        return priceChange != null && priceChange.compareTo(BigDecimal.ZERO) >= 0;
    }
}