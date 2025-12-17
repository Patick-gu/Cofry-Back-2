package org.example.controller;

import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.model.Address;
import org.example.service.AddressService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "AddressServlet", urlPatterns = {"/api/addresses", "/api/addresses/*"})
public class AddressServlet extends HttpServlet {
    
    private AddressService addressService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        addressService = new AddressService();
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
                        List<Address> addresses = addressService.getAddressesByUserId(userId);
                        JsonResponse.sendSuccess(response, addresses);
                    } catch (NumberFormatException e) {
                        JsonResponse.sendBadRequest(response, "Invalid user ID");
                    }
                } else {
                    List<Address> addresses = addressService.getAllAddresses();
                    JsonResponse.sendSuccess(response, addresses);
                }
            } else {
                Integer id = RequestParser.extractIdFromPath(pathInfo);
                if (id == null) {
                    JsonResponse.sendBadRequest(response, "Invalid address ID");
                    return;
                }
                try {
                    Address address = addressService.getAddressById(id);
                    JsonResponse.sendSuccess(response, address);
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
            Address address = RequestParser.parseJson(request, Address.class);
            Address createdAddress = addressService.createAddress(address);
            JsonResponse.sendSuccess(response, createdAddress, HttpServletResponse.SC_CREATED);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error creating address: " + e.getMessage());
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonResponse.sendBadRequest(response, "Address ID is required");
                return;
            }
            
            Integer id = RequestParser.extractIdFromPath(pathInfo);
            if (id == null) {
                JsonResponse.sendBadRequest(response, "Invalid address ID");
                return;
            }
            
            Address address = RequestParser.parseJson(request, Address.class);
            address.setAddressId(id);
            
            Address updatedAddress = addressService.updateAddress(address);
            JsonResponse.sendSuccess(response, updatedAddress);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error updating address: " + e.getMessage());
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonResponse.sendBadRequest(response, "Address ID is required");
                return;
            }
            
            Integer id = RequestParser.extractIdFromPath(pathInfo);
            if (id == null) {
                JsonResponse.sendBadRequest(response, "Invalid address ID");
                return;
            }
            
            addressService.deleteAddress(id);
            JsonResponse.sendSuccess(response, "Address deleted successfully");
        } catch (IllegalArgumentException e) {
            JsonResponse.sendNotFound(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error deleting address: " + e.getMessage());
        }
    }
}
