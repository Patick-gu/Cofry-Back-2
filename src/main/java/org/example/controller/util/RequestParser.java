package org.example.controller.util;
import com.google.gson.Gson;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
public class RequestParser {
    private static final Gson gson = GsonConfig.createGson();
    public static <T> T parseJson(HttpServletRequest request, Class<T> clazz) throws IOException {
        StringBuilder json = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
        }
        if (json.length() == 0) {
            throw new IllegalArgumentException("Corpo da requisição está vazio");
        }
        return gson.fromJson(json.toString(), clazz);
    }
    public static Integer extractIdFromPath(String pathInfo) {
        if (pathInfo == null || pathInfo.trim().isEmpty() || pathInfo.equals("/")) {
            return null;
        }
        String path = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        try {
            return Integer.parseInt(path);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    public static Integer getIntParameter(HttpServletRequest request, String paramName) {
        String param = request.getParameter(paramName);
        if (param == null || param.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(param);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}