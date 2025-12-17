package org.example.controller;
import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.dto.PixRequestDTO;
import org.example.dto.PixResponseDTO;
import org.example.service.PixService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
@WebServlet(name = "PixServlet", urlPatterns = {"/api/pix/transfer"})
public class PixServlet extends HttpServlet {
    private PixService pixService;
    @Override
    public void init() throws ServletException {
        super.init();
        pixService = new PixService();
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            PixRequestDTO pixRequest = RequestParser.parseJson(request, PixRequestDTO.class);
            PixResponseDTO pixResponse = pixService.processPixTransfer(pixRequest);
            JsonResponse.sendSuccess(response, pixResponse, HttpServletResponse.SC_OK);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao processar transferência PIX: " + e.getMessage());
        }
    }
}