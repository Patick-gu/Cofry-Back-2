package org.example.utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
public class ZipCodeValidator {
    private static final String VIACEP_API_URL = "https:
    private static final String VIACEP_API_FORMAT = "/json/";
    public static boolean isValidFormat(String zipCode) {
        if (zipCode == null || zipCode.trim().isEmpty()) {
            return false;
        }
        String cleanZipCode = zipCode.replaceAll("[^0-9]", "");
        return cleanZipCode.length() == 8;
    }
    public static String format(String zipCode) {
        if (!isValidFormat(zipCode)) {
            return null;
        }
        String cleanZipCode = zipCode.replaceAll("[^0-9]", "");
        if (cleanZipCode.length() != 8) {
            return null;
        }
        return cleanZipCode.substring(0, 5) + "-" + cleanZipCode.substring(5);
    }
    public static String unformat(String zipCode) {
        if (zipCode == null || zipCode.trim().isEmpty()) {
            return null;
        }
        String cleanZipCode = zipCode.replaceAll("[^0-9]", "");
        if (cleanZipCode.length() != 8) {
            return null;
        }
        return cleanZipCode;
    }
    public static ZipCodeInfo lookupAddress(String zipCode) {
        if (!isValidFormat(zipCode)) {
            System.err.println("Invalid zip code format: " + zipCode);
            return null;
        }
        String cleanZipCode = unformat(zipCode);
        try {
            String urlString = VIACEP_API_URL + cleanZipCode + VIACEP_API_FORMAT;
            System.out.println("Fetching address from ViaCEP: " + urlString);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Accept", "application/json");
            int responseCode = connection.getResponseCode();
            System.out.println("ViaCEP response code: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "UTF-8")
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                String jsonResponse = response.toString();
                System.out.println("ViaCEP response: " + jsonResponse);
                ZipCodeInfo info = parseViaCepResponse(jsonResponse);
                if (info != null) {
                    System.out.println("Parsed address - City: " + info.getCity() + ", Street: " + info.getStreet());
                } else {
                    System.err.println("Failed to parse ViaCEP response");
                }
                return info;
            } else {
                System.err.println("ViaCEP returned error code: " + responseCode);
                try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), "UTF-8"))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    System.err.println("Error response: " + errorResponse.toString());
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching zip code info: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    private static ZipCodeInfo parseViaCepResponse(String jsonResponse) {
        try {
            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                System.err.println("ViaCEP response is empty");
                return null;
            }
            JsonObject jsonObject = new JsonParser().parse(jsonResponse).getAsJsonObject();
            if (jsonObject.has("erro") && jsonObject.get("erro").getAsBoolean()) {
                System.err.println("ViaCEP returned error: CEP not found");
                return null;
            }
            ZipCodeInfo info = new ZipCodeInfo();
            info.setZipCode(getJsonStringValue(jsonObject, "cep"));
            info.setStreet(getJsonStringValue(jsonObject, "logradouro"));
            info.setDistrict(getJsonStringValue(jsonObject, "bairro"));
            info.setCity(getJsonStringValue(jsonObject, "localidade"));
            info.setState(getJsonStringValue(jsonObject, "uf"));
            if (info.getZipCode() == null && info.getCity() == null) {
                System.err.println("ViaCEP response missing essential fields");
                return null;
            }
            return info;
        } catch (Exception e) {
            System.err.println("Error parsing ViaCEP response: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    private static String getJsonStringValue(JsonObject jsonObject, String key) {
        if (!jsonObject.has(key) || jsonObject.get(key).isJsonNull()) {
            return null;
        }
        String value = jsonObject.get(key).getAsString();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
    public static class ZipCodeInfo {
        private String zipCode;
        private String street;
        private String district;
        private String city;
        private String state;
        public String getZipCode() {
            return zipCode;
        }
        public void setZipCode(String zipCode) {
            this.zipCode = zipCode;
        }
        public String getStreet() {
            return street;
        }
        public void setStreet(String street) {
            this.street = street;
        }
        public String getDistrict() {
            return district;
        }
        public void setDistrict(String district) {
            this.district = district;
        }
        public String getCity() {
            return city;
        }
        public void setCity(String city) {
            this.city = city;
        }
        public String getState() {
            return state;
        }
        public void setState(String state) {
            this.state = state;
        }
    }
}