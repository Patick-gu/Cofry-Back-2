package org.example.service;
import org.example.dao.UserAssetDAO;
import org.example.dao.AssetDAO;
import org.example.dao.AssetCategoryDAO;
import org.example.dto.AssetDistributionDTO;
import org.example.dto.PortfolioSummaryDTO;
import org.example.model.UserAsset;
import org.example.model.Asset;
import org.example.model.AssetCategory;
import org.example.persistence.JdbcUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class InvestmentPortfolioService {
    private final UserAssetDAO userAssetDAO;
    private final AssetDAO assetDAO;
    private final AssetCategoryDAO assetCategoryDAO;
    public InvestmentPortfolioService() {
        this.userAssetDAO = new UserAssetDAO();
        this.assetDAO = new AssetDAO();
        this.assetCategoryDAO = new AssetCategoryDAO();
    }
    public List<AssetDistributionDTO> getAssetDistribution(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        List<UserAsset> positions = userAssetDAO.findByUserId(userId);
        if (positions.isEmpty()) {
            return new ArrayList<>();
        }
        BigDecimal totalValue = BigDecimal.ZERO;
        Map<Integer, AssetDistributionDTO> distributionMap = new HashMap<>();
        for (UserAsset position : positions) {
            if (position.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                continue; 
            }
            Asset asset = assetDAO.findById(position.getAssetId())
                    .orElseThrow(() -> new RuntimeException("Ativo não encontrado: " + position.getAssetId()));
            AssetCategory category = assetCategoryDAO.findById(asset.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Categoria não encontrada: " + asset.getCategoryId()));
            BigDecimal positionValue = position.getQuantity().multiply(position.getAveragePrice());
            totalValue = totalValue.add(positionValue);
            AssetDistributionDTO distribution = new AssetDistributionDTO();
            distribution.setAssetId(asset.getId());
            distribution.setTicker(asset.getTicker());
            distribution.setAssetName(asset.getName());
            distribution.setCategoryId(category.getId());
            distribution.setCategoryName(category.getName());
            distribution.setQuantity(position.getQuantity());
            distribution.setAveragePrice(position.getAveragePrice());
            distribution.setTotalValue(positionValue);
            distributionMap.put(asset.getId(), distribution);
        }
        List<AssetDistributionDTO> distributionList = new ArrayList<>(distributionMap.values());
        for (AssetDistributionDTO dist : distributionList) {
            if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal percentage = dist.getTotalValue()
                        .divide(totalValue, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .setScale(2, RoundingMode.HALF_UP);
                dist.setPercentage(percentage);
            } else {
                dist.setPercentage(BigDecimal.ZERO);
            }
        }
        distributionList.sort((a, b) -> b.getTotalValue().compareTo(a.getTotalValue()));
        return distributionList;
    }
    public List<AssetDistributionDTO> getDistributionByCategory(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        List<AssetDistributionDTO> detailedDistribution = getAssetDistribution(userId);
        BigDecimal totalValue = detailedDistribution.stream()
                .map(AssetDistributionDTO::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<Integer, AssetDistributionDTO> categoryMap = new HashMap<>();
        for (AssetDistributionDTO dist : detailedDistribution) {
            Integer categoryId = dist.getCategoryId();
            String categoryName = dist.getCategoryName();
            AssetDistributionDTO categoryDist = categoryMap.get(categoryId);
            if (categoryDist == null) {
                categoryDist = new AssetDistributionDTO();
                categoryDist.setCategoryId(categoryId);
                categoryDist.setCategoryName(categoryName);
                categoryDist.setTotalValue(BigDecimal.ZERO);
                categoryMap.put(categoryId, categoryDist);
            }
            categoryDist.setTotalValue(categoryDist.getTotalValue().add(dist.getTotalValue()));
        }
        List<AssetDistributionDTO> categoryDistribution = new ArrayList<>(categoryMap.values());
        for (AssetDistributionDTO dist : categoryDistribution) {
            if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal percentage = dist.getTotalValue()
                        .divide(totalValue, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .setScale(2, RoundingMode.HALF_UP);
                dist.setPercentage(percentage);
            } else {
                dist.setPercentage(BigDecimal.ZERO);
            }
        }
        categoryDistribution.sort((a, b) -> b.getTotalValue().compareTo(a.getTotalValue()));
        return categoryDistribution;
    }
    public PortfolioSummaryDTO getPortfolioSummary(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        List<AssetDistributionDTO> distribution = getAssetDistribution(userId);
        List<AssetDistributionDTO> distributionByCategory = getDistributionByCategory(userId);
        BigDecimal totalValue = distribution.stream()
                .map(AssetDistributionDTO::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long totalAssets = distribution.stream()
                .filter(d -> d.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                .count();
        PortfolioSummaryDTO summary = new PortfolioSummaryDTO();
        summary.setUserId(userId);
        summary.setTotalPortfolioValue(totalValue);
        summary.setTotalAssets((int) totalAssets);
        summary.setDistribution(distribution);
        summary.setDistributionByCategory(distributionByCategory);
        return summary;
    }
}