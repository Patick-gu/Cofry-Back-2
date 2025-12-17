package org.example.controller;
import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.dto.AccountRequestDTO;
import org.example.dto.AccountResponseDTO;
import org.example.model.AccountPlan;
import org.example.service.AccountFormService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@WebServlet(name = "AccountFormServlet", urlPatterns = {
    "/api/form/account",
    "/api/form/account/plans"
})
public class AccountFormServlet extends HttpServlet {
    private AccountFormService accountFormService;
    @Override
    public void init() throws ServletException {
        super.init();
        accountFormService = new AccountFormService();
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String requestURI = request.getRequestURI();
            if (requestURI.contains("/plans")) {
                List<Map<String, Object>> plans = new ArrayList<>();
                for (AccountPlan plan : AccountPlan.values()) {
                    Map<String, Object> planInfo = new HashMap<>();
                    planInfo.put("name", plan.getName());
                    planInfo.put("value", plan.name());
                    planInfo.put("price", plan.getPrice());
                    plans.add(planInfo);
                }
                JsonResponse.sendSuccess(response, plans);
            } else {
                JsonResponse.sendBadRequest(response, "Invalid endpoint");
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
            AccountRequestDTO accountDTO = RequestParser.parseJson(request, AccountRequestDTO.class);
            AccountResponseDTO createdAccount = accountFormService.createAccountFromForm(accountDTO);
            JsonResponse.sendSuccess(response, createdAccount, HttpServletResponse.SC_CREATED);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error creating account: " + e.getMessage());
        }
    }
}