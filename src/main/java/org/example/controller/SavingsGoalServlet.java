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
    private static class DepositRequest {
        private BigDecimal amount;
        public BigDecimal getAmount() {
            return amount;
        }
        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }
}