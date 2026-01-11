package org.example.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.config.SupabaseConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

public class SupabaseAuthService {
    private final HttpClient httpClient;
    private final Gson gson;
    private final String authApiUrl;
    private final String apiKey;
    
    public SupabaseAuthService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
        this.authApiUrl = SupabaseConfig.getAuthApiUrl();
        this.apiKey = SupabaseConfig.getSupabaseAnonKey();
    }
    
    public AuthResponse signUp(String email, String password, UserMetadata metadata) {
        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("email", email);
            requestBody.addProperty("password", password);
            
            if (metadata != null) {
                JsonObject userMetadata = new JsonObject();
                if (metadata.firstName != null) {
                    userMetadata.addProperty("first_name", metadata.firstName);
                }
                if (metadata.lastName != null) {
                    userMetadata.addProperty("last_name", metadata.lastName);
                }
                if (metadata.taxId != null) {
                    userMetadata.addProperty("tax_id", metadata.taxId);
                }
                if (metadata.phoneNumber != null) {
                    userMetadata.addProperty("phone_number", metadata.phoneNumber);
                }
                if (metadata.dateOfBirth != null) {
                    userMetadata.addProperty("date_of_birth", metadata.dateOfBirth);
                }
                requestBody.add("user_metadata", userMetadata);
            }
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(authApiUrl + "/signup"))
                    .header("Content-Type", "application/json")
                    .header("apikey", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                return parseAuthResponse(jsonResponse);
            } else {
                JsonObject errorResponse = gson.fromJson(response.body(), JsonObject.class);
                String errorMsg = errorResponse.has("msg") 
                    ? errorResponse.get("msg").getAsString() 
                    : errorResponse.has("error_description")
                        ? errorResponse.get("error_description").getAsString()
                        : "Erro ao criar usuário no Supabase";
                throw new SupabaseAuthException(errorMsg, response.statusCode());
            }
        } catch (SupabaseAuthException e) {
            throw e;
        } catch (Exception e) {
            throw new SupabaseAuthException("Erro ao comunicar com Supabase Auth: " + e.getMessage(), e);
        }
    }
    
    public AuthResponse login(String email, String password) {
        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("email", email);
            requestBody.addProperty("password", password);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(authApiUrl + "/token?grant_type=password"))
                    .header("Content-Type", "application/json")
                    .header("apikey", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                return parseAuthResponse(jsonResponse);
            } else {
                JsonObject errorResponse = gson.fromJson(response.body(), JsonObject.class);
                String errorMsg = errorResponse.has("error_description") 
                    ? errorResponse.get("error_description").getAsString() 
                    : "Credenciais inválidas";
                throw new SupabaseAuthException(errorMsg, response.statusCode());
            }
        } catch (SupabaseAuthException e) {
            throw e;
        } catch (Exception e) {
            throw new SupabaseAuthException("Erro ao comunicar com Supabase Auth: " + e.getMessage(), e);
        }
    }
    
    public void logout(String accessToken) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(authApiUrl + "/logout"))
                    .header("Content-Type", "application/json")
                    .header("apikey", apiKey)
                    .header("Authorization", "Bearer " + accessToken)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new SupabaseAuthException("Erro ao fazer logout: " + e.getMessage(), e);
        }
    }
    
    public UserInfo getUserInfo(String accessToken) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(authApiUrl + "/user"))
                    .header("Content-Type", "application/json")
                    .header("apikey", apiKey)
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                return parseUserInfo(jsonResponse);
            } else {
                throw new SupabaseAuthException("Token inválido ou expirado", response.statusCode());
            }
        } catch (SupabaseAuthException e) {
            throw e;
        } catch (Exception e) {
            throw new SupabaseAuthException("Erro ao obter informações do usuário: " + e.getMessage(), e);
        }
    }
    
    private AuthResponse parseAuthResponse(JsonObject json) {
        AuthResponse authResponse = new AuthResponse();
        
        if (json.has("access_token")) {
            authResponse.accessToken = json.get("access_token").getAsString();
        }
        if (json.has("refresh_token")) {
            authResponse.refreshToken = json.get("refresh_token").getAsString();
        }
        if (json.has("expires_in")) {
            authResponse.expiresIn = json.get("expires_in").getAsLong();
        }
        if (json.has("token_type")) {
            authResponse.tokenType = json.get("token_type").getAsString();
        }
        
        if (json.has("user")) {
            JsonObject userObj = json.getAsJsonObject("user");
            authResponse.user = parseUserInfo(userObj);
        }
        
        return authResponse;
    }
    
    private UserInfo parseUserInfo(JsonObject userObj) {
        UserInfo userInfo = new UserInfo();
        
        if (userObj.has("id")) {
            userInfo.id = UUID.fromString(userObj.get("id").getAsString());
        }
        if (userObj.has("email")) {
            userInfo.email = userObj.get("email").getAsString();
        }
        if (userObj.has("email_confirmed_at")) {
            userInfo.emailConfirmedAt = userObj.get("email_confirmed_at").getAsString();
        }
        if (userObj.has("created_at")) {
            userInfo.createdAt = userObj.get("created_at").getAsString();
        }
        if (userObj.has("updated_at")) {
            userInfo.updatedAt = userObj.get("updated_at").getAsString();
        }
        
        if (userObj.has("user_metadata")) {
            JsonObject metadata = userObj.getAsJsonObject("user_metadata");
            UserMetadata userMetadata = new UserMetadata();
            
            if (metadata.has("first_name")) {
                userMetadata.firstName = metadata.get("first_name").getAsString();
            }
            if (metadata.has("last_name")) {
                userMetadata.lastName = metadata.get("last_name").getAsString();
            }
            if (metadata.has("tax_id")) {
                userMetadata.taxId = metadata.get("tax_id").getAsString();
            }
            if (metadata.has("phone_number")) {
                userMetadata.phoneNumber = metadata.get("phone_number").getAsString();
            }
            if (metadata.has("date_of_birth")) {
                userMetadata.dateOfBirth = metadata.get("date_of_birth").getAsString();
            }
            
            userInfo.userMetadata = userMetadata;
        }
        
        return userInfo;
    }
    
    public static class AuthResponse {
        public String accessToken;
        public String refreshToken;
        public Long expiresIn;
        public String tokenType;
        public UserInfo user;
    }
    
    public static class UserInfo {
        public UUID id;
        public String email;
        public String emailConfirmedAt;
        public String createdAt;
        public String updatedAt;
        public UserMetadata userMetadata;
    }
    
    public static class UserMetadata {
        public String firstName;
        public String lastName;
        public String taxId;
        public String phoneNumber;
        public String dateOfBirth;
    }
    
    public static class SupabaseAuthException extends RuntimeException {
        private final int statusCode;
        
        public SupabaseAuthException(String message) {
            super(message);
            this.statusCode = 0;
        }
        
        public SupabaseAuthException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }
        
        public SupabaseAuthException(String message, Throwable cause) {
            super(message, cause);
            this.statusCode = 0;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
    }
}
