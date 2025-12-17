package org.example.controller;
import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.dto.LoginRequestDTO;
import org.example.dto.LoginResponseDTO;
import org.example.service.AuthService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
@WebServlet(name = "AuthServlet", urlPatterns = {"/api/auth/login"})
public class AuthServlet extends HttpServlet {
    private AuthService authService;
    @Override
    public void init() throws ServletException {
        super.init();
        authService = new AuthService();
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            LoginRequestDTO loginDTO = RequestParser.parseJson(request, LoginRequestDTO.class);
            LoginResponseDTO loginResponse = authService.login(loginDTO);
            JsonResponse.sendSuccess(response, loginResponse, HttpServletResponse.SC_OK);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao realizar login: " + e.getMessage());
        }
    }
}