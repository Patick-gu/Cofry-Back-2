package org.example.controller;

import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.model.Budget;
import org.example.service.BudgetService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "BudgetServlet", urlPatterns = {"/api/budgets", "/api/budgets/*"})
public class BudgetServlet extends HttpServlet {
    
    private BudgetService budgetService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        budgetService = new BudgetService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            String userIdParam = request.getParameter("userId");
            
            if (pathInfo == null || pathInfo.equals("/")) {
                if (userIdParam != null) {
                    try {
                        Integer userId = Integer.parseInt(userIdParam);
                        List<Budget> budgets = budgetService.getBudgetsByUserId(userId);
                        JsonResponse.sendSuccess(response, budgets);
                    } catch (NumberFormatException e) {
                        JsonResponse.sendBadRequest(response, "Invalid user ID");
                    }
                } else {
                    List<Budget> budgets = budgetService.getAllBudgets();
                    JsonResponse.sendSuccess(response, budgets);
                }
            } else {
                Integer id = RequestParser.extractIdFromPath(pathInfo);
                if (id == null) {
                    JsonResponse.sendBadRequest(response, "Invalid budget ID");
                    return;
                }
                try {
                    Budget budget = budgetService.getBudgetById(id);
                    JsonResponse.sendSuccess(response, budget);
                } catch (IllegalArgumentException e) {
                    JsonResponse.sendNotFound(response, e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error processing request: " + e.getMessage());
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            Budget budget = RequestParser.parseJson(request, Budget.class);
            Budget createdBudget = budgetService.createBudget(budget);
            JsonResponse.sendSuccess(response, createdBudget, HttpServletResponse.SC_CREATED);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error creating budget: " + e.getMessage());
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonResponse.sendBadRequest(response, "Budget ID is required");
                return;
            }
            
            Integer id = RequestParser.extractIdFromPath(pathInfo);
            if (id == null) {
                JsonResponse.sendBadRequest(response, "Invalid budget ID");
                return;
            }
            
            Budget budget = RequestParser.parseJson(request, Budget.class);
            budget.setBudgetId(id);
            
            Budget updatedBudget = budgetService.updateBudget(budget);
            JsonResponse.sendSuccess(response, updatedBudget);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error updating budget: " + e.getMessage());
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonResponse.sendBadRequest(response, "Budget ID is required");
                return;
            }
            
            Integer id = RequestParser.extractIdFromPath(pathInfo);
            if (id == null) {
                JsonResponse.sendBadRequest(response, "Invalid budget ID");
                return;
            }
            
            budgetService.deleteBudget(id);
            JsonResponse.sendSuccess(response, "Budget deleted successfully");
        } catch (IllegalArgumentException e) {
            JsonResponse.sendNotFound(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error deleting budget: " + e.getMessage());
        }
    }
}
