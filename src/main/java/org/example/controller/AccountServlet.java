package org.example.controller;

import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.model.Account;
import org.example.service.AccountService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "AccountServlet", urlPatterns = {"/api/accounts", "/api/accounts/*"})
public class AccountServlet extends HttpServlet {
    
    private AccountService accountService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        accountService = new AccountService();
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
                        List<Account> accounts = accountService.getAccountsByUserId(userId);
                        JsonResponse.sendSuccess(response, accounts);
                    } catch (NumberFormatException e) {
                        JsonResponse.sendBadRequest(response, "Invalid user ID");
                    }
                } else {
                    List<Account> accounts = accountService.getAllAccounts();
                    JsonResponse.sendSuccess(response, accounts);
                }
            } else {
                Integer id = RequestParser.extractIdFromPath(pathInfo);
                if (id == null) {
                    JsonResponse.sendBadRequest(response, "Invalid account ID");
                    return;
                }
                try {
                    Account account = accountService.getAccountById(id);
                    JsonResponse.sendSuccess(response, account);
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
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonResponse.sendBadRequest(response, "Account ID is required");
                return;
            }
            
            Integer id = RequestParser.extractIdFromPath(pathInfo);
            if (id == null) {
                JsonResponse.sendBadRequest(response, "Invalid account ID");
                return;
            }
            
            Account account = RequestParser.parseJson(request, Account.class);
            account.setAccountId(id);
            
            Account updatedAccount = accountService.updateAccount(account);
            JsonResponse.sendSuccess(response, updatedAccount);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error updating account: " + e.getMessage());
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonResponse.sendBadRequest(response, "Account ID is required");
                return;
            }
            
            Integer id = RequestParser.extractIdFromPath(pathInfo);
            if (id == null) {
                JsonResponse.sendBadRequest(response, "Invalid account ID");
                return;
            }
            
            accountService.deleteAccount(id);
            JsonResponse.sendSuccess(response, "Account deleted successfully");
        } catch (IllegalArgumentException e) {
            JsonResponse.sendNotFound(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error deleting account: " + e.getMessage());
        }
    }
}
