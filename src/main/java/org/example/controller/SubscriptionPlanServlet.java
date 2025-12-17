package org.example.controller;

import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.model.SubscriptionPlan;
import org.example.service.SubscriptionPlanService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "SubscriptionPlanServlet", urlPatterns = {"/api/plans", "/api/plans/*"})
public class SubscriptionPlanServlet extends HttpServlet {
    
    private SubscriptionPlanService planService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        planService = new SubscriptionPlanService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                List<SubscriptionPlan> plans = planService.getAllPlans();
                JsonResponse.sendSuccess(response, plans);
            } else {
                Integer id = RequestParser.extractIdFromPath(pathInfo);
                if (id == null) {
                    JsonResponse.sendBadRequest(response, "ID inválido");
                    return;
                }
                
                try {
                    SubscriptionPlan plan = planService.getPlanById(id);
                    JsonResponse.sendSuccess(response, plan);
                } catch (IllegalArgumentException e) {
                    JsonResponse.sendNotFound(response, e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao processar requisição: " + e.getMessage());
        }
    }
}
