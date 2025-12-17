package org.example.dto;
import java.math.BigDecimal;
import java.util.List;
public class PortfolioSummaryDTO {
    private Integer userId;
    private BigDecimal totalPortfolioValue;
    private Integer totalAssets;
    private List<AssetDistributionDTO> distribution;
    private List<AssetDistributionDTO> distributionByCategory;
    public PortfolioSummaryDTO() {
    }
    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public BigDecimal getTotalPortfolioValue() {
        return totalPortfolioValue;
    }
    public void setTotalPortfolioValue(BigDecimal totalPortfolioValue) {
        this.totalPortfolioValue = totalPortfolioValue;
    }
    public Integer getTotalAssets() {
        return totalAssets;
    }
    public void setTotalAssets(Integer totalAssets) {
        this.totalAssets = totalAssets;
    }
    public List<AssetDistributionDTO> getDistribution() {
        return distribution;
    }
    public void setDistribution(List<AssetDistributionDTO> distribution) {
        this.distribution = distribution;
    }
    public List<AssetDistributionDTO> getDistributionByCategory() {
        return distributionByCategory;
    }
    public void setDistributionByCategory(List<AssetDistributionDTO> distributionByCategory) {
        this.distributionByCategory = distributionByCategory;
    }
}