package org.example.controller;
import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.dto.ChangePasswordRequestDTO;
import org.example.service.AuthService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
@WebServlet(name = "ChangePasswordServlet", urlPatterns = {"/api/auth/change-password"})
public class ChangePasswordServlet extends HttpServlet {
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
            ChangePasswordRequestDTO changePasswordDTO = RequestParser.parseJson(request, ChangePasswordRequestDTO.class);
            authService.changePassword(changePasswordDTO);
            JsonResponse.sendSuccess(response, "Senha alterada com sucesso", HttpServletResponse.SC_OK);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao alterar senha: " + e.getMessage());
        }
    }
}