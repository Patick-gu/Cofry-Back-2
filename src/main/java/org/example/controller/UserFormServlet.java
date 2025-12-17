package org.example.controller;

import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.dto.UserRequestDTO;
import org.example.dto.UserResponseDTO;
import org.example.dto.UserUpdateDTO;
import org.example.service.UserFormService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "UserFormServlet", urlPatterns = {
    "/api/form/user",
    "/api/form/user/*"
})
public class UserFormServlet extends HttpServlet {
    
    private UserFormService userFormService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        userFormService = new UserFormService();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            UserRequestDTO userDTO = RequestParser.parseJson(request, UserRequestDTO.class);
            UserResponseDTO createdUser = userFormService.createUserFromForm(userDTO);
            JsonResponse.sendSuccess(response, createdUser, HttpServletResponse.SC_CREATED);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error creating user: " + e.getMessage());
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonResponse.sendBadRequest(response, "User ID is required in path");
                return;
            }
            
            Integer userId = RequestParser.extractIdFromPath(pathInfo);
            if (userId == null) {
                JsonResponse.sendBadRequest(response, "Invalid user ID");
                return;
            }
            
            UserUpdateDTO updateDTO = RequestParser.parseJson(request, UserUpdateDTO.class);
            UserResponseDTO updatedUser = userFormService.updateUserFromForm(userId, updateDTO);
            JsonResponse.sendSuccess(response, updatedUser);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error updating user: " + e.getMessage());
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        JsonResponse.sendBadRequest(response, "GET method not supported. Use POST to create or PUT to update.");
    }
}
