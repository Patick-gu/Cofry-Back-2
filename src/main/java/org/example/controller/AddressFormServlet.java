package org.example.controller;
import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.dto.AddressRequestDTO;
import org.example.dto.AddressResponseDTO;
import org.example.service.AddressFormService;
import org.example.utils.BrazilCities;
import org.example.utils.BrazilStates;
import org.example.utils.ZipCodeValidator;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
@WebServlet(name = "AddressFormServlet", urlPatterns = {
    "/api/form/address",
    "/api/form/address/lookup",
    "/api/form/address/states",
    "/api/form/address/cities"
})
public class AddressFormServlet extends HttpServlet {
    private AddressFormService addressFormService;
    @Override
    public void init() throws ServletException {
        super.init();
        addressFormService = new AddressFormService();
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String requestURI = request.getRequestURI();
            if (requestURI.contains("/lookup")) {
                String zipCode = request.getParameter("zipCode");
                if (zipCode == null || zipCode.trim().isEmpty()) {
                    JsonResponse.sendBadRequest(response, "Zip code parameter is required");
                    return;
                }
                ZipCodeValidator.ZipCodeInfo zipCodeInfo = addressFormService.lookupZipCode(zipCode);
                if (zipCodeInfo == null) {
                    JsonResponse.sendNotFound(response, "Zip code not found");
                    return;
                }
                JsonResponse.sendSuccess(response, zipCodeInfo);
            } else if (requestURI.contains("/states")) {
                List<BrazilStates.StateInfo> states = BrazilStates.getAllStates();
                JsonResponse.sendSuccess(response, states);
            } else if (requestURI.contains("/cities")) {
                String state = request.getParameter("state");
                if (state == null || state.trim().isEmpty()) {
                    JsonResponse.sendBadRequest(response, "State parameter is required");
                    return;
                }
                if (!BrazilStates.isValidStateCode(state)) {
                    JsonResponse.sendBadRequest(response, "Invalid state code");
                    return;
                }
                List<BrazilCities.CityInfo> cities = BrazilCities.getCitiesByState(state);
                JsonResponse.sendSuccess(response, cities);
            } else {
                JsonResponse.sendBadRequest(response, "Invalid endpoint");
            }
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error processing request: " + e.getMessage());
        }
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            AddressRequestDTO addressDTO = RequestParser.parseJson(request, AddressRequestDTO.class);
            AddressResponseDTO createdAddress = addressFormService.createAddressFromForm(addressDTO);
            JsonResponse.sendSuccess(response, createdAddress, HttpServletResponse.SC_CREATED);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error creating address: " + e.getMessage());
        }
    }
}