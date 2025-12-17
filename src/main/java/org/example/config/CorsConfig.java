package org.example.config;
import javax.servlet.http.HttpServletResponse;
public class CorsConfig {
    public static void addCorsHeaders(HttpServletResponse response, String origin) {
        if (origin != null && !origin.isEmpty()) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        } else {
            response.setHeader("Access-Control-Allow-Origin", "*");
        }
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
        response.setHeader("Access-Control-Allow-Headers", 
            "Content-Type, Authorization, X-Requested-With, Accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Expose-Headers", 
            "Content-Type, Authorization, X-Requested-With");
        response.setHeader("Access-Control-Max-Age", "3600");
    }
    public static void addCorsHeaders(HttpServletResponse response) {
        addCorsHeaders(response, null);
    }
    public static boolean handlePreflight(HttpServletResponse response) {
        addCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
        return true;
    }
}