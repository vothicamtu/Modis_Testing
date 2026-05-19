package com.modis.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Test Data Manager for handling test data from JSON files and generating fake data
 */
public class TestDataManager {
    
    private static final Logger logger = LoggerUtil.getLogger(TestDataManager.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Faker faker = new Faker();
    private static final Map<String, JsonNode> testDataCache = new HashMap<>();
    
    // Private constructor to prevent instantiation
    private TestDataManager() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    // ==================== JSON DATA LOADING ====================
    
    /**
     * Load test data from JSON file
     * @param fileName The JSON file name (without extension)
     * @return JsonNode containing the test data
     */
    public static JsonNode loadTestData(String fileName) {
        String fullFileName = fileName.endsWith(".json") ? fileName : fileName + ".json";
        
        if (testDataCache.containsKey(fullFileName)) {
            return testDataCache.get(fullFileName);
        }
        
        try (InputStream inputStream = TestDataManager.class.getClassLoader()
                .getResourceAsStream("testdata/" + fullFileName)) {
            
            if (inputStream == null) {
                logger.error("Test data file not found: {}", fullFileName);
                return objectMapper.createObjectNode();
            }
            
            JsonNode data = objectMapper.readTree(inputStream);
            testDataCache.put(fullFileName, data);
            logger.info("Loaded test data from: {}", fullFileName);
            return data;
            
        } catch (IOException e) {
            logger.error("Failed to load test data from: {}", fullFileName, e);
            return objectMapper.createObjectNode();
        }
    }
    
    /**
     * Get specific test data by path
     * @param fileName The JSON file name
     * @param path The JSON path (e.g., "users.testUser1.username")
     * @return The value at the specified path
     */
    public static String getTestData(String fileName, String path) {
        JsonNode data = loadTestData(fileName);
        JsonNode node = data;
        
        String[] pathParts = path.split("\\.");
        for (String part : pathParts) {
            if (node.has(part)) {
                node = node.get(part);
            } else {
                logger.warn("Path not found in test data: {} -> {}", fileName, path);
                return "";
            }
        }
        
        return node.isTextual() ? node.asText() : node.toString();
    }
    
    // ==================== USER DATA ====================
    
    /**
     * Get test user data
     * @param userType The type of user (e.g., "validUser", "invalidUser")
     * @return Map containing user data
     */
    public static Map<String, String> getTestUser(String userType) {
        JsonNode users = loadTestData("users");
        JsonNode user = users.get("testUsers").get(userType);
        
        if (user == null) {
            logger.warn("User type not found: {}", userType);
            return Collections.emptyMap();
        }
        
        Map<String, String> userData = new HashMap<>();
        user.fields().forEachRemaining(entry -> 
            userData.put(entry.getKey(), entry.getValue().asText()));
        
        return userData;
    }
    
    /**
     * Generate random user data
     * @return Map containing random user data
     */
    public static Map<String, String> generateRandomUser() {
        Map<String, String> userData = new HashMap<>();
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        userData.put("username", "user" + timestamp);
        userData.put("fullName", faker.name().fullName());
        userData.put("email", "test" + timestamp + "@example.com");
        userData.put("phone", faker.phoneNumber().cellPhone().replaceAll("[^0-9]", ""));
        userData.put("password", "password123");
        
        return userData;
    }
    
    // ==================== MESSAGE DATA ====================
    
    /**
     * Get test message by category
     * @param category The message category (e.g., "greeting", "response")
     * @return Random message from the category
     */
    public static String getTestMessage(String category) {
        JsonNode messages = loadTestData("messages");
        JsonNode templates = messages.get("messageTemplates").get(category);
        
        if (templates == null || !templates.isArray() || templates.size() == 0) {
            logger.warn("Message category not found or empty: {}", category);
            return "Test message";
        }
        
        Random random = new Random();
        int index = random.nextInt(templates.size());
        return templates.get(index).asText();
    }
    
    /**
     * Get conversation scenario
     * @param scenarioName The scenario name
     * @return List of messages for the scenario
     */
    public static List<String> getConversationScenario(String scenarioName) {
        JsonNode messages = loadTestData("messages");
        JsonNode scenarios = messages.get("conversationScenarios");
        
        List<String> messageList = new ArrayList<>();
        
        for (JsonNode scenario : scenarios) {
            if (scenario.get("name").asText().equals(scenarioName)) {
                JsonNode messageArray = scenario.get("messages");
                for (JsonNode message : messageArray) {
                    messageList.add(message.asText());
                }
                break;
            }
        }
        
        if (messageList.isEmpty()) {
            logger.warn("Conversation scenario not found: {}", scenarioName);
            messageList.add("Hello!");
            messageList.add("Hi there!");
        }
        
        return messageList;
    }
    
    /**
     * Generate random message
     * @return Random message text
     */
    public static String generateRandomMessage() {
        String[] categories = {"greeting", "response", "question"};
        String randomCategory = categories[new Random().nextInt(categories.length)];
        return getTestMessage(randomCategory);
    }
    
    // ==================== FRIEND DATA ====================
    
    /**
     * Get test friend data
     * @param friendId The friend ID
     * @return Map containing friend data
     */
    public static Map<String, String> getTestFriend(String friendId) {
        JsonNode friends = loadTestData("friends");
        JsonNode friendsArray = friends.get("testFriends");
        
        for (JsonNode friend : friendsArray) {
            if (friend.get("id").asText().equals(friendId)) {
                Map<String, String> friendData = new HashMap<>();
                friend.fields().forEachRemaining(entry -> 
                    friendData.put(entry.getKey(), entry.getValue().asText()));
                return friendData;
            }
        }
        
        logger.warn("Friend not found: {}", friendId);
        return Collections.emptyMap();
    }
    
    /**
     * Get all test friends
     * @return List of friend data maps
     */
    public static List<Map<String, String>> getAllTestFriends() {
        JsonNode friends = loadTestData("friends");
        JsonNode friendsArray = friends.get("testFriends");
        
        List<Map<String, String>> friendsList = new ArrayList<>();
        
        for (JsonNode friend : friendsArray) {
            Map<String, String> friendData = new HashMap<>();
            friend.fields().forEachRemaining(entry -> 
                friendData.put(entry.getKey(), entry.getValue().asText()));
            friendsList.add(friendData);
        }
        
        return friendsList;
    }
    
    // ==================== PHOTO DATA ====================
    
    /**
     * Get photo caption by category
     * @param category The caption category
     * @return Random caption from the category
     */
    public static String getPhotoCaption(String category) {
        JsonNode photos = loadTestData("photos");
        JsonNode templates = photos.get("captionTemplates").get(category);
        
        if (templates == null || !templates.isArray() || templates.size() == 0) {
            logger.warn("Caption category not found or empty: {}", category);
            return "Test photo";
        }
        
        Random random = new Random();
        int index = random.nextInt(templates.size());
        return templates.get(index).asText();
    }
    
    /**
     * Generate random photo caption
     * @return Random photo caption
     */
    public static String generateRandomPhotoCaption() {
        String[] categories = {"nature", "food", "friends", "lifestyle"};
        String randomCategory = categories[new Random().nextInt(categories.length)];
        return getPhotoCaption(randomCategory);
    }
    
    // ==================== VALIDATION DATA ====================
    
    /**
     * Get validation rules for a specific field
     * @param fileName The data file name
     * @param fieldName The field name
     * @return Map containing validation rules
     */
    public static Map<String, Object> getValidationRules(String fileName, String fieldName) {
        JsonNode data = loadTestData(fileName);
        JsonNode validation = data.get("validation");
        
        if (validation == null) {
            return Collections.emptyMap();
        }
        
        Map<String, Object> rules = new HashMap<>();
        
        if (validation.has("maxLength")) {
            rules.put("maxLength", validation.get("maxLength").asInt());
        }
        if (validation.has("minLength")) {
            rules.put("minLength", validation.get("minLength").asInt());
        }
        if (validation.has("required")) {
            rules.put("required", validation.get("required").asBoolean());
        }
        if (validation.has("pattern")) {
            rules.put("pattern", validation.get("pattern").asText());
        }
        
        return rules;
    }
    
    // ==================== PERFORMANCE TEST DATA ====================
    
    /**
     * Get performance test configuration
     * @param fileName The data file name
     * @return Map containing performance test settings
     */
    public static Map<String, Integer> getPerformanceTestData(String fileName) {
        JsonNode data = loadTestData(fileName);
        JsonNode perfData = data.get("performanceTestData");
        
        Map<String, Integer> perfConfig = new HashMap<>();
        
        if (perfData != null) {
            perfData.fields().forEachRemaining(entry -> {
                if (entry.getValue().isInt()) {
                    perfConfig.put(entry.getKey(), entry.getValue().asInt());
                }
            });
        }
        
        return perfConfig;
    }
    
    // ==================== FAKE DATA GENERATION ====================
    
    /**
     * Generate fake name
     * @return Random full name
     */
    public static String generateFakeName() {
        return faker.name().fullName();
    }
    
    /**
     * Generate fake email
     * @return Random email address
     */
    public static String generateFakeEmail() {
        return faker.internet().emailAddress();
    }
    
    /**
     * Generate fake phone number
     * @return Random phone number (digits only)
     */
    public static String generateFakePhone() {
        return faker.phoneNumber().cellPhone().replaceAll("[^0-9]", "");
    }
    
    /**
     * Generate fake username
     * @return Random username
     */
    public static String generateFakeUsername() {
        return faker.name().username() + System.currentTimeMillis();
    }
    
    /**
     * Generate fake address
     * @return Random address
     */
    public static String generateFakeAddress() {
        return faker.address().fullAddress();
    }
    
    /**
     * Generate fake company name
     * @return Random company name
     */
    public static String generateFakeCompany() {
        return faker.company().name();
    }
    
    /**
     * Generate fake text
     * @param sentences Number of sentences
     * @return Random text
     */
    public static String generateFakeText(int sentences) {
        return faker.lorem().sentence(sentences);
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Clear test data cache
     */
    public static void clearCache() {
        testDataCache.clear();
        logger.info("Test data cache cleared");
    }
    
    /**
     * Get random element from array
     * @param array The array to choose from
     * @return Random element
     */
    public static <T> T getRandomElement(T[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        Random random = new Random();
        return array[random.nextInt(array.length)];
    }
    
    /**
     * Get random element from list
     * @param list The list to choose from
     * @return Random element
     */
    public static <T> T getRandomElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        Random random = new Random();
        return list.get(random.nextInt(list.size()));
    }
    
    /**
     * Generate timestamp string
     * @return Current timestamp as string
     */
    public static String generateTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }
    
    /**
     * Generate unique ID
     * @param prefix The prefix for the ID
     * @return Unique ID with prefix
     */
    public static String generateUniqueId(String prefix) {
        return prefix + "_" + System.currentTimeMillis() + "_" + new Random().nextInt(1000);
    }
}