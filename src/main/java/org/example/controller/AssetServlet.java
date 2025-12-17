package org.example.controller;

import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.model.Asset;
import org.example.service.AssetService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "AssetServlet", urlPatterns = {"/api/assets", "/api/assets/*"})
public class AssetServlet extends HttpServlet {
    
    private AssetService assetService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        assetService = new AssetService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            String allParam = request.getParameter("all");
            
            if (pathInfo == null || pathInfo.equals("/")) {
                List<Asset> assets;
                if (allParam != null && allParam.equalsIgnoreCase("true")) {
                    assets = assetService.getAllAssets();
                } else {
                    assets = assetService.getActiveAssets();
                }
                JsonResponse.sendSuccess(response, assets);
            } else if (pathInfo.startsWith("/ticker/")) {
                String ticker = pathInfo.replace("/ticker/", "").replace("/ticker", "");
                if (ticker.isEmpty()) {
                    JsonResponse.sendBadRequest(response, "Ticker is required");
                    return;
                }
                try {
                    Asset asset = assetService.getAssetByTicker(ticker);
                    JsonResponse.sendSuccess(response, asset);
                } catch (IllegalArgumentException e) {
                    JsonResponse.sendNotFound(response, e.getMessage());
                }
            } else {
                Integer id = RequestParser.extractIdFromPath(pathInfo);
                if (id == null) {
                    JsonResponse.sendBadRequest(response, "Invalid asset ID");
                    return;
                }
                try {
                    Asset asset = assetService.getAssetById(id);
                    JsonResponse.sendSuccess(response, asset);
                } catch (IllegalArgumentException e) {
                    JsonResponse.sendNotFound(response, e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error processing request: " + e.getMessage());
        }
    }
}
