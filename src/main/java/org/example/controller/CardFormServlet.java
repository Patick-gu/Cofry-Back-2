package org.example.controller;
import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.dto.CardRequestDTO;
import org.example.dto.CardResponseDTO;
import org.example.model.CardTypeEnum;
import org.example.service.CardFormService;
import org.example.service.CardService;
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
@WebServlet(name = "CardFormServlet", urlPatterns = {
    "/api/form/card",
    "/api/form/card/*",
    "/api/form/card/types"
})
public class CardFormServlet extends HttpServlet {
    private CardFormService cardFormService;
    @Override
    public void init() throws ServletException {
        super.init();
        cardFormService = new CardFormService();
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String requestURI = request.getRequestURI();
            if (requestURI.contains("/types")) {
                List<Map<String, Object>> types = new ArrayList<>();
                for (CardTypeEnum type : CardTypeEnum.values()) {
                    Map<String, Object> typeInfo = new HashMap<>();
                    typeInfo.put("name", type.name());
                    typeInfo.put("value", type.name());
                    types.add(typeInfo);
                }
                JsonResponse.sendSuccess(response, types);
            } else if (requestURI.contains("/user/")) {
                String[] parts = requestURI.split("/user/");
                if (parts.length > 1) {
                    String userIdStr = parts[1].split("/")[0];
                    try {
                        Integer userId = Integer.parseInt(userIdStr);
                        CardService cardService = new CardService();
                        List<org.example.model.Card> cards = cardService.getCardsByUserId(userId);
                        List<CardResponseDTO> cardDTOs = new ArrayList<>();
                        for (org.example.model.Card card : cards) {
                            CardResponseDTO dto = new CardResponseDTO();
                            dto.setCardId(card.getCardId());
                            dto.setUserId(card.getUserId());
                            dto.setAccountId(card.getAccountId());
                            dto.setCardNumber(card.getCardNumber());
                            dto.setCardHolderName(card.getCardHolderName());
                            dto.setExpiryDate(card.getExpiryDate());
                            dto.setCardType(card.getCardType() != null ? card.getCardType().name() : null);
                            dto.setBrand(card.getBrand());
                            dto.setStatus(card.getStatus());
                            dto.setLimitAmount(card.getLimitAmount());
                            dto.setCurrentBalance(card.getCurrentBalance());
                            cardDTOs.add(dto);
                        }
                        JsonResponse.sendSuccess(response, cardDTOs);
                    } catch (NumberFormatException e) {
                        JsonResponse.sendBadRequest(response, "Invalid user ID");
                    }
                } else {
                    JsonResponse.sendBadRequest(response, "User ID is required");
                }
            } else {
                String pathInfo = request.getPathInfo();
                if (pathInfo != null && !pathInfo.equals("/")) {
                    Integer id = RequestParser.extractIdFromPath(pathInfo);
                    if (id != null) {
                        CardService cardService = new CardService();
                        org.example.model.Card card = cardService.getCardById(id);
                        if (card != null) {
                            CardResponseDTO dto = new CardResponseDTO();
                            dto.setCardId(card.getCardId());
                            dto.setUserId(card.getUserId());
                            dto.setAccountId(card.getAccountId());
                            dto.setCardNumber(card.getCardNumber());
                            dto.setCardHolderName(card.getCardHolderName());
                            dto.setExpiryDate(card.getExpiryDate());
                            dto.setCardType(card.getCardType() != null ? card.getCardType().name() : null);
                            dto.setBrand(card.getBrand());
                            dto.setStatus(card.getStatus());
                            dto.setLimitAmount(card.getLimitAmount());
                            dto.setCurrentBalance(card.getCurrentBalance());
                            JsonResponse.sendSuccess(response, dto);
                        } else {
                            JsonResponse.sendNotFound(response, "Card not found");
                        }
                    } else {
                        JsonResponse.sendBadRequest(response, "Invalid card ID");
                    }
                } else {
                    JsonResponse.sendBadRequest(response, "Invalid endpoint");
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
            CardRequestDTO cardDTO = RequestParser.parseJson(request, CardRequestDTO.class);
            CardResponseDTO createdCard = cardFormService.createCardFromForm(cardDTO);
            JsonResponse.sendSuccess(response, createdCard, HttpServletResponse.SC_CREATED);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error creating card: " + e.getMessage());
        }
    }
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonResponse.sendBadRequest(response, "Card ID is required");
                return;
            }
            Integer cardId = RequestParser.extractIdFromPath(pathInfo);
            if (cardId == null) {
                JsonResponse.sendBadRequest(response, "Invalid card ID");
                return;
            }
            CardRequestDTO cardDTO = RequestParser.parseJson(request, CardRequestDTO.class);
            CardService cardService = new CardService();
            org.example.model.Card existingCard = cardService.getCardById(cardId);
            if (existingCard == null) {
                JsonResponse.sendNotFound(response, "Card not found");
                return;
            }
            if (cardDTO.getLimitAmount() != null && !cardDTO.getLimitAmount().trim().isEmpty()) {
                existingCard.setLimitAmount(new java.math.BigDecimal(cardDTO.getLimitAmount().replace(",", ".")));
            }
            org.example.model.Card updatedCard = cardService.updateCard(existingCard);
            CardResponseDTO dto = new CardResponseDTO();
            dto.setCardId(updatedCard.getCardId());
            dto.setUserId(updatedCard.getUserId());
            dto.setAccountId(updatedCard.getAccountId());
            dto.setCardNumber(updatedCard.getCardNumber());
            dto.setCardHolderName(updatedCard.getCardHolderName());
            dto.setExpiryDate(updatedCard.getExpiryDate());
            dto.setCardType(updatedCard.getCardType() != null ? updatedCard.getCardType().name() : null);
            dto.setBrand(updatedCard.getBrand());
            dto.setStatus(updatedCard.getStatus());
            dto.setLimitAmount(updatedCard.getLimitAmount());
            dto.setCurrentBalance(updatedCard.getCurrentBalance());
            JsonResponse.sendSuccess(response, dto);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error updating card: " + e.getMessage());
        }
    }
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonResponse.sendBadRequest(response, "Card ID is required");
                return;
            }
            Integer cardId = RequestParser.extractIdFromPath(pathInfo);
            if (cardId == null) {
                JsonResponse.sendBadRequest(response, "Invalid card ID");
                return;
            }
            CardService cardService = new CardService();
            cardService.deleteCard(cardId);
            JsonResponse.sendSuccess(response, "Card deleted successfully");
        } catch (IllegalArgumentException e) {
            JsonResponse.sendNotFound(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error deleting card: " + e.getMessage());
        }
    }
}