package org.example.controller;
import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.dto.InvestmentTransactionRequestDTO;
import org.example.dto.PortfolioSummaryDTO;
import org.example.dto.AssetDistributionDTO;
import org.example.model.InvestmentTransaction;
import org.example.service.InvestmentFormService;
import org.example.service.InvestmentTransactionService;
import org.example.service.InvestmentPortfolioService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
@WebServlet(name = "InvestmentServlet", urlPatterns = {
    "/api/investments/transaction",
    private Integer extractUserIdFromPath(String requestURI) {
        try {
            int userIndex = requestURI.indexOf("/user/");
            if (userIndex == -1) {
                return null;
            }
            String afterUser = requestURI.substring(userIndex + "/user/".length());
            if (afterUser.contains("/")) {
                afterUser = afterUser.substring(0, afterUser.indexOf("/"));
            }
            if (afterUser.contains("?")) {
                afterUser = afterUser.substring(0, afterUser.indexOf("?"));
            }
            return Integer.parseInt(afterUser.trim());
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            return null;
        }
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            InvestmentTransactionRequestDTO transactionDTO = RequestParser.parseJson(request, InvestmentTransactionRequestDTO.class);
            InvestmentTransaction transaction = investmentFormService.createTransactionFromForm(transactionDTO);
            JsonResponse.sendSuccess(response, transaction, HttpServletResponse.SC_CREATED);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error creating transaction: " + e.getMessage());
        }
    }
}