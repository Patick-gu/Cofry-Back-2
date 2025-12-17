package org.example.controller;

import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.model.User;
import org.example.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "UserServlet", urlPatterns = {"/api/users", "/api/users/*"})
public class UserServlet extends HttpServlet {
    
    private UserService userService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        userService = new UserService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                List<User> users = userService.getAllUsers();
                JsonResponse.sendSuccess(response, users);
            } else if (pathInfo.equals("/complete") || pathInfo.equals("/complete/")) {
                JsonResponse.sendBadRequest(response, "Para buscar informações completas, use /api/users/{id}/complete");
            } else {
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 3 && pathParts[2].equals("complete")) {
                    Integer id = RequestParser.extractIdFromPath("/" + pathParts[1]);
                    if (id == null) {
                        JsonResponse.sendBadRequest(response, "ID inválido");
                        return;
                    }
                    
                    try {
                        org.example.dto.UserCompleteDTO userComplete = userService.getUserCompleteInfo(id);
                        JsonResponse.sendSuccess(response, userComplete);
                    } catch (IllegalArgumentException e) {
                        JsonResponse.sendNotFound(response, e.getMessage());
                    }
                } else {
                    Integer id = RequestParser.extractIdFromPath(pathInfo);
                    if (id == null) {
                        JsonResponse.sendBadRequest(response, "ID inválido");
                        return;
                    }
                    
                    try {
                        User user = userService.getUserById(id);
                        JsonResponse.sendSuccess(response, user);
                    } catch (IllegalArgumentException e) {
                        JsonResponse.sendNotFound(response, e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao processar requisição: " + e.getMessage());
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            User user = RequestParser.parseJson(request, User.class);
            
            User createdUser = userService.createUser(user);
            JsonResponse.sendSuccess(response, createdUser, HttpServletResponse.SC_CREATED);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao criar usuário: " + e.getMessage());
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonResponse.sendBadRequest(response, "ID é obrigatório para atualização");
                return;
            }
            
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length >= 3 && pathParts[2].equals("plan")) {
                Integer userId = RequestParser.extractIdFromPath("/" + pathParts[1]);
                if (userId == null) {
                    JsonResponse.sendBadRequest(response, "ID do usuário inválido");
                    return;
                }
                
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> requestBody = (java.util.Map<String, Object>) RequestParser.parseJson(request, java.util.Map.class);
                
                Object planIdObj = requestBody != null ? requestBody.get("planId") : null;
                Integer planId = null;
                
                if (planIdObj != null) {
                    if (planIdObj instanceof Integer) {
                        planId = (Integer) planIdObj;
                    } else if (planIdObj instanceof Double) {
                        planId = ((Double) planIdObj).intValue();
                    } else if (planIdObj instanceof Number) {
                        planId = ((Number) planIdObj).intValue();
                    }
                }
                
                if (planId == null) {
                    JsonResponse.sendBadRequest(response, "planId é obrigatório");
                    return;
                }
                
                try {
                    User updatedUser = userService.changeUserPlan(userId, planId);
                    JsonResponse.sendSuccess(response, updatedUser);
                } catch (IllegalArgumentException e) {
                    JsonResponse.sendBadRequest(response, e.getMessage());
                }
            } else {
                Integer id = RequestParser.extractIdFromPath(pathInfo);
                if (id == null) {
                    JsonResponse.sendBadRequest(response, "ID inválido");
                    return;
                }
                
                User user = RequestParser.parseJson(request, User.class);
                user.setUserId(id);
                
                User updatedUser = userService.updateUser(user);
                JsonResponse.sendSuccess(response, updatedUser);
            }
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao atualizar usuário: " + e.getMessage());
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonResponse.sendBadRequest(response, "ID é obrigatório para remoção");
                return;
            }
            
            Integer id = RequestParser.extractIdFromPath(pathInfo);
            if (id == null) {
                JsonResponse.sendBadRequest(response, "ID inválido");
                return;
            }
            
            userService.deleteUser(id);
            JsonResponse.sendSuccess(response, "Usuário removido com sucesso");
        } catch (IllegalArgumentException e) {
            JsonResponse.sendNotFound(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao remover usuário: " + e.getMessage());
        }
    }
}
