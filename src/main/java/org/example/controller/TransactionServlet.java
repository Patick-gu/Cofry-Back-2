package org.example.controller;
import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.model.Transaction;
import org.example.model.TransactionTypeEnum;
import org.example.service.TransactionService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            if (dateStr.length() == 10) {
                return LocalDate.parse(dateStr, DATE_FORMATTER);
            }
            if (dateStr.contains("T")) {
                String dateOnly = dateStr.substring(0, 10); 
                return LocalDate.parse(dateOnly, DATE_FORMATTER);
            }
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de data inválido: " + dateStr + ". Use o formato YYYY-MM-DD");
        }
    }
    private TransactionTypeEnum determineTransactionType(com.google.gson.JsonObject jsonObject, Transaction transaction) {
        if (transaction.getDestinationAccountId() != null) {
            return TransactionTypeEnum.TRANSFER;
        }
        if (jsonObject.has("isIncome")) {
            boolean isIncome = jsonObject.get("isIncome").getAsBoolean();
            if (isIncome) {
                return TransactionTypeEnum.DEPOSIT; 
            } else {
                return TransactionTypeEnum.PAYMENT; 
            }
        }
        return TransactionTypeEnum.PAYMENT;
    }
    private java.math.BigDecimal parseAmount(com.google.gson.JsonElement element) {
        if (element.isJsonNull()) {
            return null;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return element.getAsBigDecimal();
        }
        String valueStr = element.getAsString();
        if (valueStr == null || valueStr.trim().isEmpty()) {
            return null;
        }
        valueStr = valueStr.replace("R$", "")
                          .replace("$", "")
                          .replace("€", "")
                          .replace("£", "")
                          .trim();
        valueStr = valueStr.replace(".", "");
        valueStr = valueStr.replace(",", ".");
        try {
            return new java.math.BigDecimal(valueStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato de valor inválido: " + element.getAsString());
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
            Integer id = RequestParser.extractIdFromPath(pathInfo);
            if (id == null) {
                JsonResponse.sendBadRequest(response, "ID inválido");
                return;
            }
            Transaction transaction = RequestParser.parseJson(request, Transaction.class);
            transaction.setTransactionId(id);
            Transaction updatedTransaction = transactionService.updateTransaction(transaction);
            JsonResponse.sendSuccess(response, updatedTransaction);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao atualizar transação: " + e.getMessage());
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
            transactionService.deleteTransaction(id);
            JsonResponse.sendSuccess(response, "Transação removida com sucesso");
        } catch (IllegalArgumentException e) {
            JsonResponse.sendNotFound(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao remover transação: " + e.getMessage());
        }
    }
}