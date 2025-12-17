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
@WebServlet(name = "TransactionServlet", urlPatterns = {"/api/transactions", "/api/transactions/*"})
public class TransactionServlet extends HttpServlet {
    private TransactionService transactionService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @Override
    public void init() throws ServletException {
        super.init();
        transactionService = new TransactionService();
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                String accountIdParam = request.getParameter("accountId");
                if (accountIdParam != null) {
                    Integer accountId = Integer.parseInt(accountIdParam);
                    List<Transaction> transactions = transactionService.getTransactionsBySourceAccount(accountId);
                    JsonResponse.sendSuccess(response, transactions);
                } else {
                    List<Transaction> transactions = transactionService.getAllTransactions();
                    JsonResponse.sendSuccess(response, transactions);
                }
                return;
            }
            Integer id = RequestParser.extractIdFromPath(pathInfo);
            if (id == null) {
                JsonResponse.sendBadRequest(response, "ID inválido");
                return;
            }
            Transaction transaction = transactionService.getTransactionById(id);
            if (transaction == null) {
                JsonResponse.sendNotFound(response, "Transação não encontrada");
                return;
            }
            JsonResponse.sendSuccess(response, transaction);
        } catch (NumberFormatException e) {
            JsonResponse.sendBadRequest(response, "ID inválido");
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao buscar transações: " + e.getMessage());
        }
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String requestBody = RequestParser.getRequestBody(request);
            com.google.gson.JsonObject jsonObject = com.google.gson.JsonParser.parseString(requestBody).getAsJsonObject();
            Transaction transaction = new Transaction();
            if (jsonObject.has("sourceAccountId")) {
                transaction.setSourceAccountId(jsonObject.get("sourceAccountId").getAsInt());
            }
            if (jsonObject.has("destinationAccountId") && !jsonObject.get("destinationAccountId").isJsonNull()) {
                transaction.setDestinationAccountId(jsonObject.get("destinationAccountId").getAsInt());
            }
            if (jsonObject.has("amount")) {
                transaction.setAmount(parseAmount(jsonObject.get("amount")));
            }
            transaction.setTransactionType(determineTransactionType(jsonObject, transaction));
            if (jsonObject.has("description")) {
                transaction.setDescription(jsonObject.get("description").getAsString());
            }
            if (jsonObject.has("transactionDate")) {
                LocalDate date = parseDate(jsonObject.get("transactionDate").getAsString());
                transaction.setTransactionDate(date);
            } else {
                transaction.setTransactionDate(LocalDate.now());
            }
            Transaction createdTransaction = transactionService.createTransaction(transaction);
            JsonResponse.sendSuccess(response, createdTransaction, HttpServletResponse.SC_CREATED);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao criar transação: " + e.getMessage());
        }
    }
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