package org.example.controller;

import com.google.gson.Gson;
import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.dto.LoginRequestDTO;
import org.example.dto.UserRequestDTO;
import org.example.persistence.ConnectionFactory;
import org.example.service.SupabaseAuthService;
import org.example.service.SupabaseAuthService.AuthResponse;
import org.example.service.SupabaseAuthService.UserMetadata;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@WebServlet(name = "SupabaseAuthServlet", urlPatterns = {
    "/api/auth/supabase/login",
    "/api/auth/supabase/signup",
    "/api/auth/supabase/logout",
    "/api/auth/supabase/me"
})
public class SupabaseAuthServlet extends HttpServlet {
    
    private SupabaseAuthService authService;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        super.init();
        authService = new SupabaseAuthService();
        gson = new Gson();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = request.getServletPath();
        }
        
        try {
            if (pathInfo.contains("/login")) {
                handleLogin(request, response);
            } else if (pathInfo.contains("/signup")) {
                handleSignup(request, response);
            } else if (pathInfo.contains("/logout")) {
                handleLogout(request, response);
            } else {
                JsonResponse.sendBadRequest(response, "Endpoint não encontrado");
            }
        } catch (SupabaseAuthService.SupabaseAuthException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro no servidor: " + e.getMessage());
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = request.getServletPath();
        }
        
        try {
            if (pathInfo.contains("/me")) {
                handleGetUserInfo(request, response);
            } else {
                JsonResponse.sendBadRequest(response, "Endpoint não encontrado");
            }
        } catch (SupabaseAuthService.SupabaseAuthException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro no servidor: " + e.getMessage());
        }
    }
    
    private void handleLogin(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        LoginRequestDTO loginDTO = RequestParser.parseJson(request, LoginRequestDTO.class);
        
        if (loginDTO.getEmail() == null || loginDTO.getEmail().trim().isEmpty()) {
            JsonResponse.sendBadRequest(response, "Email é obrigatório");
            return;
        }
        
        if (loginDTO.getPassword() == null || loginDTO.getPassword().trim().isEmpty()) {
            JsonResponse.sendBadRequest(response, "Senha é obrigatória");
            return;
        }
        
        AuthResponse authResponse = authService.login(loginDTO.getEmail(), loginDTO.getPassword());
        
        ConnectionFactory.setUserToken(authResponse.accessToken);
        
        JsonResponse.sendSuccess(response, authResponse);
    }
    
    private void handleSignup(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        UserRequestDTO userDTO = RequestParser.parseJson(request, UserRequestDTO.class);
        
        if (userDTO.getEmail() == null || userDTO.getEmail().trim().isEmpty()) {
            JsonResponse.sendBadRequest(response, "Email é obrigatório");
            return;
        }
        
        if (userDTO.getPassword() == null || userDTO.getPassword().trim().isEmpty()) {
            JsonResponse.sendBadRequest(response, "Senha é obrigatória");
            return;
        }
        
        UserMetadata metadata = new UserMetadata();
        metadata.firstName = userDTO.getFirstName();
        metadata.lastName = userDTO.getLastName();
        metadata.taxId = userDTO.getCpf();
        metadata.phoneNumber = userDTO.getPhoneNumber();
        
        if (userDTO.getDateOfBirth() != null) {
            metadata.dateOfBirth = userDTO.getDateOfBirth().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        
        AuthResponse authResponse = authService.signUp(userDTO.getEmail(), userDTO.getPassword(), metadata);
        
        ConnectionFactory.setUserToken(authResponse.accessToken);
        
        JsonResponse.sendSuccess(response, authResponse, HttpServletResponse.SC_CREATED);
    }
    
    private void handleLogout(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            JsonResponse.sendBadRequest(response, "Token não fornecido");
            return;
        }
        
        String token = authHeader.substring(7);
        
        authService.logout(token);
        
        ConnectionFactory.clearUserToken();
        
        JsonResponse.sendSuccess(response, "Logout realizado com sucesso");
    }
    
    private void handleGetUserInfo(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            JsonResponse.sendBadRequest(response, "Token não fornecido");
            return;
        }
        
        String token = authHeader.substring(7);
        
        SupabaseAuthService.UserInfo userInfo = authService.getUserInfo(token);
        
        JsonResponse.sendSuccess(response, userInfo);
    }
}
