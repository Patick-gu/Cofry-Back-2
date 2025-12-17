package org.example.controller;

import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.dto.BoletoRequestDTO;
import org.example.dto.BoletoResponseDTO;
import org.example.model.BoletoStatus;
import org.example.service.BoletoFormService;
import org.example.service.BoletoService;

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

@WebServlet(name = "BoletoFormServlet", urlPatterns = {
    "/api/form/boleto",
    "/api/form/boleto/*"
})
public class BoletoFormServlet extends HttpServlet {
    
    private BoletoFormService boletoFormService;
    private BoletoService boletoService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        boletoFormService = new BoletoFormService();
        boletoService = new BoletoService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String requestURI = request.getRequestURI();
            String pathInfo = request.getPathInfo();
            
            if (requestURI.contains("/user/")) {
                if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/user")) {
                    JsonResponse.sendBadRequest(response, "User ID is required in path");
                    return;
                }
                
                String userIdStr = pathInfo.replace("/user/", "").replace("/user", "");
                Integer userId;
                try {
                    userId = Integer.parseInt(userIdStr);
                } catch (NumberFormatException e) {
                    JsonResponse.sendBadRequest(response, "Invalid user ID format");
                    return;
                }
                
                try {
                    List<BoletoResponseDTO> boletos = boletoFormService.listBoletosByUserId(userId);
                    JsonResponse.sendSuccess(response, boletos);
                } catch (IllegalArgumentException e) {
                    JsonResponse.sendBadRequest(response, e.getMessage());
                }
                
            } else if (requestURI.contains("/cpf/")) {
                if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/cpf")) {
                    JsonResponse.sendBadRequest(response, "CPF is required in path");
                    return;
                }
                
                String cpf = pathInfo.replace("/cpf/", "").replace("/cpf", "");
                if (cpf.isEmpty()) {
                    JsonResponse.sendBadRequest(response, "CPF cannot be empty");
                    return;
                }
                
                try {
                    List<BoletoResponseDTO> boletos = boletoFormService.listBoletosByCpf(cpf);
                    JsonResponse.sendSuccess(response, boletos);
                } catch (IllegalArgumentException e) {
                    JsonResponse.sendBadRequest(response, e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                    JsonResponse.sendInternalError(response, "Error searching boletos by CPF: " + e.getMessage());
                }
                
            } else if (requestURI.contains("/status/")) {
                if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/status")) {
                    JsonResponse.sendBadRequest(response, "Status is required in path");
                    return;
                }
                
                String statusStr = pathInfo.replace("/status/", "").replace("/status", "").toUpperCase();
                BoletoStatus status;
                try {
                    status = BoletoStatus.valueOf(statusStr);
                } catch (IllegalArgumentException e) {
                    JsonResponse.sendBadRequest(response, "Invalid status. Available: OPEN, OVERDUE, PAID");
                    return;
                }
                
                try {
                    List<org.example.model.Boleto> boletos = boletoService.listBoletosByStatus(status);
                    List<BoletoResponseDTO> dtos = new ArrayList<>();
                    for (org.example.model.Boleto boleto : boletos) {
                        BoletoResponseDTO dto = new BoletoResponseDTO();
                        dto.setId(boleto.getId());
                        dto.setTitle(boleto.getTitle());
                        dto.setAmount(boleto.getAmount());
                        dto.setFormattedAmount(formatCurrency(boleto.getAmount()));
                        dto.setDueDate(boleto.getDueDate());
                        dto.setStatus(boleto.getStatus().name());
                        dto.setStatusLabel(getStatusLabel(boleto.getStatus()));
                        dto.setBankCode(boleto.getBankCode());
                        dto.setWalletCode(boleto.getWalletCode());
                        dto.setOurNumber(boleto.getOurNumber());
                        dto.setBoletoCode(boleto.getBoletoCode());
                        dto.setUserId(boleto.getUserId());
                        dto.setPaidAt(boleto.getPaidAt());
                        dto.setCreatedAt(boleto.getCreatedAt());
                        dto.setUpdatedAt(boleto.getUpdatedAt());
                        dtos.add(dto);
                    }
                    JsonResponse.sendSuccess(response, dtos);
                } catch (IllegalArgumentException e) {
                    JsonResponse.sendBadRequest(response, e.getMessage());
                }
                
            } else {
                try {
                    List<BoletoResponseDTO> boletos = boletoFormService.listBoletos();
                    JsonResponse.sendSuccess(response, boletos);
                } catch (Exception e) {
                    e.printStackTrace();
                    JsonResponse.sendInternalError(response, "Error listing boletos: " + e.getMessage());
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
            String requestURI = request.getRequestURI();
            
            if (requestURI.contains("/pay")) {
                String pathInfo = request.getPathInfo();
                if (pathInfo == null || pathInfo.equals("/")) {
                    JsonResponse.sendBadRequest(response, "Boleto ID is required in path");
                    return;
                }
                
                String path = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
                String[] parts = path.split("/");
                
                if (parts.length < 2 || !parts[1].equals("pay")) {
                    JsonResponse.sendBadRequest(response, "Invalid path format. Expected: /api/form/boleto/{id}/pay");
                    return;
                }
                
                Long boletoId;
                try {
                    boletoId = Long.parseLong(parts[0]);
                } catch (NumberFormatException e) {
                    JsonResponse.sendBadRequest(response, "Invalid boleto ID format: " + parts[0]);
                    return;
                }
                
                Map<String, Object> requestBody;
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> parsedBody = (Map<String, Object>) RequestParser.parseJson(request, Map.class);
                    requestBody = parsedBody;
                } catch (Exception e) {
                    e.printStackTrace();
                    JsonResponse.sendBadRequest(response, "Erro ao processar corpo da requisição: " + e.getMessage());
                    return;
                }
                
                Integer accountId = null;
                if (requestBody != null && requestBody.get("accountId") != null) {
                    Object accountIdObj = requestBody.get("accountId");
                    if (accountIdObj instanceof Integer) {
                        accountId = (Integer) accountIdObj;
                    } else if (accountIdObj instanceof Double) {
                        accountId = ((Double) accountIdObj).intValue();
                    } else if (accountIdObj instanceof Number) {
                        accountId = ((Number) accountIdObj).intValue();
                    }
                }
                String description = requestBody != null ? (String) requestBody.get("description") : null;
                
                if (accountId == null) {
                    JsonResponse.sendBadRequest(response, "accountId is required");
                    return;
                }
                
                try {
                    BoletoService.PayBoletoResult result = boletoService.payBoleto(boletoId, accountId, description);
                    
                    BoletoResponseDTO boletoDTO = boletoFormService.convertBoletoToDTO(result.getBoleto());
                    
                    Map<String, Object> responseData = new HashMap<>();
                    responseData.put("success", true);
                    responseData.put("message", "Boleto pago com sucesso");
                    responseData.put("boleto", boletoDTO);
                    
                    Map<String, Object> transactionData = new HashMap<>();
                    transactionData.put("transactionId", result.getTransaction().getTransactionId());
                    transactionData.put("sourceAccountId", result.getTransaction().getSourceAccountId());
                    transactionData.put("amount", result.getTransaction().getAmount());
                    transactionData.put("transactionType", result.getTransaction().getTransactionType().name());
                    transactionData.put("description", result.getTransaction().getDescription());
                    transactionData.put("transactionDate", result.getTransaction().getTransactionDate().toString());
                    responseData.put("transaction", transactionData);
                    
                    responseData.put("newBalance", result.getNewBalance());
                    
                    JsonResponse.sendSuccess(response, responseData);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    JsonResponse.sendBadRequest(response, e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                    JsonResponse.sendInternalError(response, "Erro ao pagar boleto: " + e.getMessage());
                }
                
            } else if (requestURI.contains("/automatize")) {
                String pathInfo = request.getPathInfo();
                if (pathInfo == null || pathInfo.equals("/")) {
                    JsonResponse.sendBadRequest(response, "Boleto ID is required in path");
                    return;
                }
                
                String path = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
                String[] parts = path.split("/");
                
                if (parts.length < 2 || !parts[1].equals("automatize")) {
                    JsonResponse.sendBadRequest(response, "Invalid path format. Expected: /api/form/boleto/{id}/automatize");
                    return;
                }
                
                Long boletoId;
                try {
                    boletoId = Long.parseLong(parts[0]);
                } catch (NumberFormatException e) {
                    JsonResponse.sendBadRequest(response, "Invalid boleto ID format: " + parts[0]);
                    return;
                }
                
                Map<String, Object> requestBody;
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> parsedBody = (Map<String, Object>) RequestParser.parseJson(request, Map.class);
                    requestBody = parsedBody;
                } catch (Exception e) {
                    e.printStackTrace();
                    JsonResponse.sendBadRequest(response, "Erro ao processar corpo da requisição: " + e.getMessage());
                    return;
                }
                
                Integer accountId = null;
                if (requestBody != null && requestBody.get("accountId") != null) {
                    Object accountIdObj = requestBody.get("accountId");
                    if (accountIdObj instanceof Integer) {
                        accountId = (Integer) accountIdObj;
                    } else if (accountIdObj instanceof Double) {
                        accountId = ((Double) accountIdObj).intValue();
                    } else if (accountIdObj instanceof Number) {
                        accountId = ((Number) accountIdObj).intValue();
                    }
                }
                
                Boolean enabled = requestBody != null ? (Boolean) requestBody.get("enabled") : null;
                
                if (accountId == null) {
                    JsonResponse.sendBadRequest(response, "accountId is required");
                    return;
                }
                if (enabled == null) {
                    JsonResponse.sendBadRequest(response, "enabled is required");
                    return;
                }
                
                try {
                    org.example.model.Boleto boleto = boletoService.getBoletoById(boletoId);
                    BoletoResponseDTO boletoDTO = boletoFormService.convertBoletoToDTO(boleto);
                    
                    Map<String, Object> responseData = new HashMap<>();
                    responseData.put("success", true);
                    responseData.put("message", enabled 
                        ? "Pagamento automático configurado com sucesso"
                        : "Pagamento automático desativado");
                    responseData.put("boleto", boletoDTO);
                    
                    JsonResponse.sendSuccess(response, responseData);
                } catch (IllegalArgumentException e) {
                    JsonResponse.sendBadRequest(response, e.getMessage());
                }
                
            } else {
                BoletoRequestDTO boletoDTO = RequestParser.parseJson(request, BoletoRequestDTO.class);
                
                BoletoResponseDTO createdBoleto = boletoFormService.createBoletoFromForm(boletoDTO);
                
                JsonResponse.sendSuccess(response, createdBoleto, HttpServletResponse.SC_CREATED);
            }
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error processing request: " + e.getMessage());
        }
    }
    
    private String formatCurrency(java.math.BigDecimal amount) {
        if (amount == null) {
            return "R$ 0,00";
        }
        java.text.NumberFormat formatter = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("pt", "BR"));
        return formatter.format(amount);
    }
    
    private String getStatusLabel(BoletoStatus status) {
        if (status == null) {
            return "Desconhecido";
        }
        
        switch (status) {
            case OPEN:
                return "Em aberto";
            case OVERDUE:
                return "Vencido";
            case PAID:
                return "Pago";
            default:
                return status.name();
        }
    }
}
