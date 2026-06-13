package com.modis.performance.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class AuthHelper {
    private static final Logger log = LoggerFactory.getLogger(AuthHelper.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String extractAuthToken(SampleResult sampleResult) {
        try {
            if (sampleResult == null || !sampleResult.isSuccessful()) {
                log.warn("Sample result is null or unsuccessful");
                return null;
            }
            String responseData = sampleResult.getResponseDataAsString();
            if (responseData == null || responseData.trim().isEmpty()) {
                log.warn("Response data is empty");
                return null;
            }
            JsonNode jsonResponse = mapper.readTree(responseData);
            String token = null;
            if (jsonResponse.has("token")) token = jsonResponse.get("token").asText();
            else if (jsonResponse.has("accessToken")) token = jsonResponse.get("accessToken").asText();
            else if (jsonResponse.has("access_token")) token = jsonResponse.get("access_token").asText();
            else if (jsonResponse.has("authToken")) token = jsonResponse.get("authToken").asText();
            else if (jsonResponse.has("jwt")) token = jsonResponse.get("jwt").asText();
            if (token != null && !token.isEmpty()) {
                log.info("Successfully extracted auth token");
                return token;
            } else {
                log.warn("No auth token found in response");
                return null;
            }
        } catch (Exception e) {
            log.error("Error extracting auth token: {}", e.getMessage());
            return null;
        }
    }

    public static void setAuthorizationHeader(HTTPSamplerProxy sampler, String token) {
        if (sampler == null || token == null || token.trim().isEmpty()) {
            log.warn("Cannot set authorization header - sampler or token is null/empty");
            return;
        }
        try {
            if (sampler.getHeaderManager() != null) {
                sampler.getHeaderManager().removeHeaderNamed("Authorization");
                sampler.getHeaderManager().add(new org.apache.jmeter.protocol.http.control.Header("Authorization", "Bearer " + token));
                log.debug("Set Authorization header with token");
            } else {
                log.warn("Sampler has no HeaderManager");
            }
        } catch (Exception e) {
            log.error("Error setting authorization header: {}", e.getMessage());
        }
    }

    public static String createLoginRequestBody(String username, String password) {
        try {
            Map<String, String> loginData = new HashMap<>();
            loginData.put("username", username);
            loginData.put("password", password);
            return mapper.writeValueAsString(loginData);
        } catch (Exception e) {
            log.error("Error creating login request body: {}", e.getMessage());
            return "{}";
        }
    }

    public static boolean isAuthenticationSuccessful(SampleResult sampleResult) {
        try {
            if (sampleResult == null || !sampleResult.isSuccessful()) {
                return false;
            }
            String responseData = sampleResult.getResponseDataAsString();
            if (responseData == null || responseData.trim().isEmpty()) {
                return false;
            }
            JsonNode jsonResponse = mapper.readTree(responseData);
            boolean hasToken = jsonResponse.has("token") ||
                    jsonResponse.has("accessToken") ||
                    jsonResponse.has("access_token") ||
                    jsonResponse.has("authToken") ||
                    jsonResponse.has("jwt");
            boolean hasSuccessFlag = false;
            if (jsonResponse.has("success") && jsonResponse.get("success").isBoolean() && jsonResponse.get("success").asBoolean())
                hasSuccessFlag = true;
            if (jsonResponse.has("status") && "success".equals(jsonResponse.get("status").asText()))
                hasSuccessFlag = true;
            if (jsonResponse.has("authenticated") && jsonResponse.get("authenticated").isBoolean() && jsonResponse.get("authenticated").asBoolean())
                hasSuccessFlag = true;
            return hasToken || hasSuccessFlag;
        } catch (Exception e) {
            log.error("Error validating authentication response: {}", e.getMessage());
            return false;
        }
    }

    public static Map<String, String> generateTestCredentials(int userIndex) {
        Map<String, String> creds = new HashMap<>();
        try {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String username = "loadtest_user_" + userIndex + "_" + timestamp;
            String password = "LoadTest123!" + userIndex;
            creds.put("username", username);
            creds.put("password", password);
        } catch (Exception e) {
            log.error("Error generating test credentials: {}", e.getMessage());
            creds.put("username", "default_user");
            creds.put("password", "default_pass");
        }
        return creds;
    }

    public static boolean isTokenExpired(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return true;
            }
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                log.warn("Invalid JWT token structure");
                return true;
            }
            String payload = new String(Base64.getDecoder().decode(parts[1]));
            JsonNode payloadJson = mapper.readTree(payload);
            if (payloadJson.has("exp")) {
                long expirationTime = payloadJson.get("exp").asLong();
                long currentTime = System.currentTimeMillis() / 1000;
                return currentTime >= expirationTime;
            }
            return false;
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    public static String refreshAuthToken(String refreshToken) {
        try {
            log.info("Token refresh requested - implement refresh endpoint call");
            return null;
        } catch (Exception e) {
            log.error("Error refreshing auth token: {}", e.getMessage());
            return null;
        }
    }
}
