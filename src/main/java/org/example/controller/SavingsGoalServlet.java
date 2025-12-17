package org.example.controller;

import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.model.SavingsGoal;
import org.example.model.GoalStatusEnum;
import org.example.service.SavingsGoalService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet(name = "SavingsGoalServlet", urlPatterns = {"/api/savings-goals", "/api/savings-goals/*"})
public class SavingsGoalServlet extends HttpServlet {
    
    private SavingsGoalService savingsGoalService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        savingsGoalService = new SavingsGoalService();
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
                        List<SavingsGoal> goals = savingsGoalService.getGoalsByUserId(userId);
                        JsonResponse.sendSuccess(response, goals);
                    } catch (NumberFormatException e) {
                        JsonResponse.sendBadRequest(response, "Invalid user ID");
                    }
                } else {
                    List<SavingsGoal> goals = savingsGoalService.getAllGoals();
                    JsonResponse.sendSuccess(response, goals);
                }
            } else {
                Integer id = RequestParser.extractIdFromPath(pathInfo);
                if (id == null) {
                    JsonResponse.sendBadRequest(response, "Invalid goal ID");
                    return;
                }
                try {
                    SavingsGoal goal = savingsGoalService.getGoalById(id);
                    JsonResponse.sendSuccess(response, goal);
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
            SavingsGoal goal = RequestParser.parseJson(request, SavingsGoal.class);
            SavingsGoal createdGoal = savingsGoalService.createGoal(goal);
            JsonResponse.sendSuccess(response, createdGoal, HttpServletResponse.SC_CREATED);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error creating goal: " + e.getMessage());
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonResponse.sendBadRequest(response, "Goal ID is required");
                return;
            }
            
            Integer id = RequestParser.extractIdFromPath(pathInfo);
            if (id == null) {
                JsonResponse.sendBadRequest(response, "Invalid goal ID");
                return;
            }
            
            SavingsGoal goal = RequestParser.parseJson(request, SavingsGoal.class);
            goal.setGoalId(id);
            
            SavingsGoal updatedGoal = savingsGoalService.updateGoal(goal);
            JsonResponse.sendSuccess(response, updatedGoal);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error updating goal: " + e.getMessage());
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonResponse.sendBadRequest(response, "Goal ID is required");
                return;
            }
            
            Integer id = RequestParser.extractIdFromPath(pathInfo);
            if (id == null) {
                JsonResponse.sendBadRequest(response, "Invalid goal ID");
                return;
            }
            
            savingsGoalService.deleteGoal(id);
            JsonResponse.sendSuccess(response, "Goal deleted successfully");
        } catch (IllegalArgumentException e) {
            JsonResponse.sendNotFound(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error deleting goal: " + e.getMessage());
        }
    }
}
