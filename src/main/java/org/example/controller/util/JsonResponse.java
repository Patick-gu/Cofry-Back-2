package org.example.controller.util;
import com.google.gson.Gson;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
public class JsonResponse {
    private static final Gson gson = GsonConfig.createGson();
    public static void sendSuccess(HttpServletResponse response, Object data, int statusCode) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(data));
        out.flush();
    }
    public static void sendSuccess(HttpServletResponse response, Object data) throws IOException {
        sendSuccess(response, data, HttpServletResponse.SC_OK);
    }
    public static void sendError(HttpServletResponse response, String message, int statusCode) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);
        ErrorResponse errorResponse = new ErrorResponse(message, statusCode);
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(errorResponse));
        out.flush();
    }
    public static void sendBadRequest(HttpServletResponse response, String message) throws IOException {
        sendError(response, message, HttpServletResponse.SC_BAD_REQUEST);
    }
    public static void sendNotFound(HttpServletResponse response, String message) throws IOException {
        sendError(response, message, HttpServletResponse.SC_NOT_FOUND);
    }
    public static void sendInternalError(HttpServletResponse response, String message) throws IOException {
        sendError(response, message, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    private static class ErrorResponse {
        private String error;
        private int status;
        public ErrorResponse(String error, int status) {
            this.error = error;
            this.status = status;
        }
        public String getError() {
            return error;
        }
        public int getStatus() {
            return status;
        }
    }
}