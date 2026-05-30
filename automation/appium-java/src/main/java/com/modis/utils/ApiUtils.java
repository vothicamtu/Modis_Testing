package com.modis.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ApiUtils {
    
    private static final Logger logger = LoggerUtil.getLogger(ApiUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static OkHttpClient httpClient;
    
    // API Configuration
    private static String baseUrl;
    private static String authToken;
    private static final Map<String, String> defaultHeaders = new HashMap<>();
    
    static {
        initializeHttpClient();
        loadApiConfiguration();
    }
    
    // Private constructor to prevent instantiation
    private ApiUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    // ==================== INITIALIZATION ====================
private static void initializeHttpClient() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }
private static void loadApiConfiguration() {
        baseUrl = ConfigReader.getProperty("api.baseUrl", "http://localhost:8080/api");
        
        // Set default headers
        defaultHeaders.put("Content-Type", "application/json");
        defaultHeaders.put("Accept", "application/json");
        defaultHeaders.put("User-Agent", "Modis-Automation-Tests/1.0");
    }
public static void setAuthToken(String token) {
        authToken = token;
        defaultHeaders.put("Authorization", "Bearer " + token);
        logger.info("Authentication token set");
    }
public static void clearAuthToken() {
        authToken = null;
        defaultHeaders.remove("Authorization");
        logger.info("Authentication token cleared");
    }
    
    // ==================== HTTP METHODS ====================
public static ApiResponse get(String endpoint) {
        return get(endpoint, new HashMap<>());
    }
public static ApiResponse get(String endpoint, Map<String, String> queryParams) {
        try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + endpoint).newBuilder();
            
            // Add query parameters
            for (Map.Entry<String, String> param : queryParams.entrySet()) {
                urlBuilder.addQueryParameter(param.getKey(), param.getValue());
            }
            
            Request request = new Request.Builder()
                    .url(urlBuilder.build())
                    .headers(Headers.of(defaultHeaders))
                    .get()
                    .build();
            
            return executeRequest(request);
            
        } catch (Exception e) {
            logger.error("GET request failed for endpoint: {}", endpoint, e);
            return new ApiResponse(500, "Request failed: " + e.getMessage(), null);
        }
    }
public static ApiResponse post(String endpoint, String requestBody) {
        try {
            RequestBody body = RequestBody.create(requestBody, MediaType.parse("application/json"));
            
            Request request = new Request.Builder()
                    .url(baseUrl + endpoint)
                    .headers(Headers.of(defaultHeaders))
                    .post(body)
                    .build();
            
            return executeRequest(request);
            
        } catch (Exception e) {
            logger.error("POST request failed for endpoint: {}", endpoint, e);
            return new ApiResponse(500, "Request failed: " + e.getMessage(), null);
        }
    }
public static ApiResponse post(String endpoint, Object requestObject) {
        try {
            String jsonBody = objectMapper.writeValueAsString(requestObject);
            return post(endpoint, jsonBody);
        } catch (Exception e) {
            logger.error("Failed to serialize request object", e);
            return new ApiResponse(500, "Serialization failed: " + e.getMessage(), null);
        }
    }
public static ApiResponse put(String endpoint, String requestBody) {
        try {
            RequestBody body = RequestBody.create(requestBody, MediaType.parse("application/json"));
            
            Request request = new Request.Builder()
                    .url(baseUrl + endpoint)
                    .headers(Headers.of(defaultHeaders))
                    .put(body)
                    .build();
            
            return executeRequest(request);
            
        } catch (Exception e) {
            logger.error("PUT request failed for endpoint: {}", endpoint, e);
            return new ApiResponse(500, "Request failed: " + e.getMessage(), null);
        }
    }
public static ApiResponse put(String endpoint, Object requestObject) {
        try {
            String jsonBody = objectMapper.writeValueAsString(requestObject);
            return put(endpoint, jsonBody);
        } catch (Exception e) {
            logger.error("Failed to serialize request object", e);
            return new ApiResponse(500, "Serialization failed: " + e.getMessage(), null);
        }
    }
public static ApiResponse delete(String endpoint) {
        try {
            Request request = new Request.Builder()
                    .url(baseUrl + endpoint)
                    .headers(Headers.of(defaultHeaders))
                    .delete()
                    .build();
            
            return executeRequest(request);
            
        } catch (Exception e) {
            logger.error("DELETE request failed for endpoint: {}", endpoint, e);
            return new ApiResponse(500, "Request failed: " + e.getMessage(), null);
        }
    }
    
    // ==================== REQUEST EXECUTION ====================
private static ApiResponse executeRequest(Request request) {
        try (Response response = httpClient.newCall(request).execute()) {
            
            int statusCode = response.code();
            String responseBody = response.body() != null ? response.body().string() : "";
            
            logger.debug("API Request: {} {}", request.method(), request.url());
            logger.debug("API Response: {} - {}", statusCode, responseBody);
            
            JsonNode jsonResponse = null;
            if (!responseBody.isEmpty()) {
                try {
                    jsonResponse = objectMapper.readTree(responseBody);
                } catch (Exception e) {
                    logger.debug("Response is not valid JSON: {}", responseBody);
                }
            }
            
            return new ApiResponse(statusCode, responseBody, jsonResponse);
            
        } catch (IOException e) {
            logger.error("Request execution failed", e);
            return new ApiResponse(500, "Request execution failed: " + e.getMessage(), null);
        }
    }
    
    public static boolean loginUser(String username, String password) {
        try {
            Map<String, String> loginData = new HashMap<>();
            loginData.put("username", username);
            loginData.put("password", password);
            
            ApiResponse response = post("/auth/login", loginData);
            
            if (response.isSuccessful()) {
                JsonNode responseJson = response.getJsonResponse();
                if (responseJson != null && responseJson.has("token")) {
                    String token = responseJson.get("token").asText();
                    setAuthToken(token);
                    logger.info("User logged in successfully: {}", username);
                    return true;
                }
            }
            
            logger.warn("Login failed for user: {} - Status: {}", username, response.getStatusCode());
            return false;
            
        } catch (Exception e) {
            logger.error("Login request failed for user: {}", username, e);
            return false;
        }
    }
    
    public static boolean logoutUser() {
        try {
            ApiResponse response = post("/auth/logout", "{}");
            clearAuthToken();
            
            logger.info("User logged out - Status: {}", response.getStatusCode());
            return response.isSuccessful();
            
        } catch (Exception e) {
            logger.error("Logout request failed", e);
            clearAuthToken(); // Clear token anyway
            return false;
        }
    }
    
    // ==================== USER MANAGEMENT ====================
public static ApiResponse createUser(Map<String, String> userData) {
        return post("/users", userData);
    }
public static ApiResponse getUserProfile(String userId) {
        return get("/users/" + userId);
    }
public static ApiResponse updateUserProfile(String userId, Map<String, String> userData) {
        return put("/users/" + userId, userData);
    }
public static ApiResponse deleteUser(String userId) {
        return delete("/users/" + userId);
    }
    
    // ==================== FRIENDS MANAGEMENT ====================
public static ApiResponse sendFriendRequest(String targetUserId) {
        Map<String, String> requestData = new HashMap<>();
        requestData.put("targetUserId", targetUserId);
        return post("/friends/request", requestData);
    }
public static ApiResponse acceptFriendRequest(String requestId) {
        return put("/friends/request/" + requestId + "/accept", "{}");
    }
public static ApiResponse declineFriendRequest(String requestId) {
        return put("/friends/request/" + requestId + "/decline", "{}");
    }
public static ApiResponse getFriendsList() {
        return get("/friends");
    }
    
    // ==================== MESSAGING ====================
public static ApiResponse sendMessage(String conversationId, String messageText) {
        Map<String, String> messageData = new HashMap<>();
        messageData.put("conversationId", conversationId);
        messageData.put("text", messageText);
        return post("/messages", messageData);
    }
public static ApiResponse getConversationMessages(String conversationId) {
        return get("/conversations/" + conversationId + "/messages");
    }
public static ApiResponse getConversationsList() {
        return get("/conversations");
    }
    
    // ==================== PHOTOS ====================
public static ApiResponse uploadPhoto(Map<String, Object> photoData) {
        return post("/photos", photoData);
    }
public static ApiResponse getUserPhotos(String userId) {
        return get("/users/" + userId + "/photos");
    }
    
    // ==================== TEST DATA CLEANUP ====================
public static void cleanupTestData(String testUserId) {
        try {
            logger.info("Cleaning up test data for user: {}", testUserId);
            
            // Delete user photos
            ApiResponse photosResponse = getUserPhotos(testUserId);
            if (photosResponse.isSuccessful() && photosResponse.getJsonResponse() != null) {
                JsonNode photos = photosResponse.getJsonResponse().get("photos");
                if (photos != null && photos.isArray()) {
                    for (JsonNode photo : photos) {
                        String photoId = photo.get("id").asText();
                        delete("/photos/" + photoId);
                    }
                }
            }
            
            // Delete user
            deleteUser(testUserId);
            
            logger.info("Test data cleanup completed for user: {}", testUserId);
            
        } catch (Exception e) {
            logger.error("Failed to cleanup test data for user: {}", testUserId, e);
        }
    }
    
    // ==================== UTILITY METHODS ====================
public static boolean isApiHealthy() {
        try {
            ApiResponse response = get("/health");
            return response.isSuccessful();
        } catch (Exception e) {
            logger.error("API health check failed", e);
            return false;
        }
    }
public static boolean waitForApiReady(int timeoutSeconds) {
        long startTime = System.currentTimeMillis();
        long timeout = timeoutSeconds * 1000L;
        
        while (System.currentTimeMillis() - startTime < timeout) {
            if (isApiHealthy()) {
                logger.info("API is ready");
                return true;
            }
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        
        logger.warn("API readiness timeout after {} seconds", timeoutSeconds);
        return false;
    }
    
    // ==================== INNER CLASS ====================
public static class ApiResponse {
        private final int statusCode;
        private final String responseBody;
        private final JsonNode jsonResponse;
        
        public ApiResponse(int statusCode, String responseBody, JsonNode jsonResponse) {
            this.statusCode = statusCode;
            this.responseBody = responseBody;
            this.jsonResponse = jsonResponse;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
        
        public String getResponseBody() {
            return responseBody;
        }
        
        public JsonNode getJsonResponse() {
            return jsonResponse;
        }
        
        public boolean isSuccessful() {
            return statusCode >= 200 && statusCode < 300;
        }
        
        public boolean isClientError() {
            return statusCode >= 400 && statusCode < 500;
        }
        
        public boolean isServerError() {
            return statusCode >= 500;
        }
        
        @Override
        public String toString() {
            return String.format("ApiResponse{statusCode=%d, body='%s'}", statusCode, responseBody);
        }
    }
}