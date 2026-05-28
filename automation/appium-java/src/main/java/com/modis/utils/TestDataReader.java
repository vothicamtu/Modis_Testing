package com.modis.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Utility class for reading test data from JSON files
 * Provides methods to access login credentials, user data, and test scenarios
 */
public class TestDataReader {
    private static final Logger logger = LoggerFactory.getLogger(TestDataReader.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private JsonNode usersData;
    private JsonNode friendsData;
    private JsonNode messagesData;
    private JsonNode photosData;

    public TestDataReader() {
        loadTestData();
    }

    private void loadTestData() {
        try {
            usersData = loadJsonFromResource("/testdata/users.json");
            friendsData = loadJsonFromResource("/testdata/friends.json");
            messagesData = loadJsonFromResource("/testdata/messages.json");
            photosData = loadJsonFromResource("/testdata/photos.json");
            logger.info("Test data loaded successfully");
        } catch (Exception e) {
            logger.error("Failed to load test data", e);
            throw new RuntimeException("Failed to load test data", e);
        }
    }

    private JsonNode loadJsonFromResource(String resourcePath) throws IOException {
        InputStream inputStream = getClass().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }
        return objectMapper.readTree(inputStream);
    }

    // ==================== LOGIN DATA METHODS ====================

    /**
     * Get valid login credentials from test data
     *
     * @return List of valid login credentials
     */
    public List<Map<String, Object>> getValidLoginCredentials() {
        List<Map<String, Object>> credentials = new ArrayList<>();

        JsonNode validCredentials = usersData.get("loginTestData").get("validCredentials");
        for (JsonNode credential : validCredentials) {
            Map<String, Object> credMap = new HashMap<>();
            credMap.put("id", credential.get("id").asText());
            credMap.put("username", credential.get("username").asText());
            credMap.put("password", credential.get("password").asText());
            credMap.put("email", credential.get("email").asText());
            credMap.put("phone", credential.get("phone").asText());
            credMap.put("fullname", credential.get("fullname").asText());
            credMap.put("role", credential.get("role").asText());
            credMap.put("status", credential.get("status").asText());
            credMap.put("expectedResult", credential.get("expectedResult").asText());
            credentials.add(credMap);
        }

        return credentials;
    }

    /**
     * Get invalid login credentials from test data
     *
     * @return List of invalid login credentials
     */
    public List<Map<String, Object>> getInvalidLoginCredentials() {
        List<Map<String, Object>> credentials = new ArrayList<>();

        JsonNode invalidCredentials = usersData.get("loginTestData").get("invalidCredentials");
        for (JsonNode credential : invalidCredentials) {
            Map<String, Object> credMap = new HashMap<>();
            credMap.put("id", credential.get("id").asText());
            credMap.put("username", credential.get("username").asText());
            credMap.put("password", credential.get("password").asText());
            credMap.put("expectedResult", credential.get("expectedResult").asText());
            credMap.put("expectedError", credential.get("expectedError").asText());
            credMap.put("description", credential.get("description").asText());
            credentials.add(credMap);
        }

        return credentials;
    }

    /**
     * Get retry login scenarios from test data
     *
     * @return List of retry login scenarios
     */
    public List<Map<String, Object>> getRetryLoginScenarios() {
        List<Map<String, Object>> scenarios = new ArrayList<>();

        JsonNode retryScenarios = usersData.get("retryTestScenarios");
        for (JsonNode scenario : retryScenarios) {
            Map<String, Object> scenarioMap = new HashMap<>();
            scenarioMap.put("id", scenario.get("id").asText());
            scenarioMap.put("description", scenario.get("description").asText());

            List<Map<String, Object>> attempts = new ArrayList<>();
            JsonNode attemptsNode = scenario.get("attempts");
            for (JsonNode attempt : attemptsNode) {
                Map<String, Object> attemptMap = new HashMap<>();
                attemptMap.put("attempt", attempt.get("attempt").asInt());
                attemptMap.put("username", attempt.get("username").asText());
                attemptMap.put("password", attempt.get("password").asText());
                attemptMap.put("expectedResult", attempt.get("expectedResult").asText());
                if (attempt.has("expectedError")) {
                    attemptMap.put("expectedError", attempt.get("expectedError").asText());
                }
                attempts.add(attemptMap);
            }
            scenarioMap.put("attempts", attempts);
            scenarios.add(scenarioMap);
        }

        return scenarios;
    }

    /**
     * Get a specific valid user by username
     *
     * @param username The username to search for
     * @return User data map or null if not found
     */
    public Map<String, Object> getValidUserByUsername(String username) {
        JsonNode validCredentials = usersData.get("loginTestData").get("validCredentials");
        for (JsonNode credential : validCredentials) {
            if (credential.get("username").asText().equals(username)) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", credential.get("id").asText());
                userMap.put("username", credential.get("username").asText());
                userMap.put("password", credential.get("password").asText());
                userMap.put("email", credential.get("email").asText());
                userMap.put("phone", credential.get("phone").asText());
                userMap.put("fullname", credential.get("fullname").asText());
                userMap.put("role", credential.get("role").asText());
                userMap.put("status", credential.get("status").asText());
                return userMap;
            }
        }
        return null;
    }

    // ==================== FRIENDS DATA METHODS ====================

    /**
     * Get test friends data
     *
     * @return List of test friends
     */
    public List<Map<String, Object>> getTestFriends() {
        List<Map<String, Object>> friends = new ArrayList<>();

        JsonNode testFriends = friendsData.get("testFriends");
        for (JsonNode friend : testFriends) {
            Map<String, Object> friendMap = new HashMap<>();
            friendMap.put("id", friend.get("id").asText());
            friendMap.put("username", friend.get("username").asText());
            friendMap.put("fullName", friend.get("fullName").asText());
            friendMap.put("email", friend.get("email").asText());
            friendMap.put("phone", friend.get("phone").asText());
            friendMap.put("status", friend.get("status").asText());
            if (friend.has("avatarUrl")) {
                friendMap.put("avatarUrl", friend.get("avatarUrl").asText());
            }
            friends.add(friendMap);
        }

        return friends;
    }

    /**
     * Get friend requests data
     *
     * @return List of friend requests
     */
    public List<Map<String, Object>> getFriendRequests() {
        List<Map<String, Object>> requests = new ArrayList<>();

        JsonNode friendRequests = friendsData.get("friendRequests");
        for (JsonNode request : friendRequests) {
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("id", request.get("id").asText());
            requestMap.put("senderId", request.get("senderId").asText());
            requestMap.put("senderUsername", request.get("senderUsername").asText());
            requestMap.put("senderFullName", request.get("senderFullName").asText());
            requestMap.put("receiverId", request.get("receiverId").asText());
            requestMap.put("requestDate", request.get("requestDate").asText());
            requestMap.put("status", request.get("status").asText());
            requests.add(requestMap);
        }

        return requests;
    }

    // ==================== MESSAGES DATA METHODS ====================

    /**
     * Get test messages data
     *
     * @return List of test messages
     */
    public List<Map<String, Object>> getTestMessages() {
        List<Map<String, Object>> messages = new ArrayList<>();

        JsonNode testMessages = messagesData.get("testMessages");
        for (JsonNode message : testMessages) {
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("id", message.get("id").asText());
            messageMap.put("senderId", message.get("senderId").asText());
            messageMap.put("senderUsername", message.get("senderUsername").asText());
            messageMap.put("receiverId", message.get("receiverId").asText());
            messageMap.put("receiverUsername", message.get("receiverUsername").asText());
            messageMap.put("content", message.get("content").asText());
            messageMap.put("type", message.get("type").asText());
            messageMap.put("timestamp", message.get("timestamp").asText());
            messageMap.put("category", message.get("category").asText());
            messages.add(messageMap);
        }

        return messages;
    }

    public String getTestData(String key) {

        JsonNode testData =
                messagesData.get("testData");

        if (testData == null || !testData.has(key)) {
            logger.warn("Test data key not found: {}", key);
            return "";
        }

        return testData.get(key).asText();
    }

    public List<String> getRapidMessages() {

        List<String> rapidMessages = new ArrayList<>();

        JsonNode messages =
                messagesData
                        .get("performanceTestData")
                        .get("rapidMessages");

        if (messages == null || !messages.isArray()) {
            return rapidMessages;
        }

        for (JsonNode message : messages) {
            rapidMessages.add(message.asText());
        }

        return rapidMessages;
    }
    public String getRandomMessageTemplate(String category) {

        JsonNode templates =
                messagesData
                        .get("messageTemplates")
                        .get(category);

        if (templates == null
                || !templates.isArray()
                || templates.size() == 0) {

            logger.warn("Message category not found: {}", category);
            return "Test message";
        }

        Random random = new Random();

        int randomIndex =
                random.nextInt(templates.size());

        return templates.get(randomIndex).asText();
    }

    /**
     * Get conversation scenarios
     *
     * @return List of conversation scenarios
     */
    public List<Map<String, Object>> getConversationScenarios() {
        List<Map<String, Object>> scenarios = new ArrayList<>();

        JsonNode conversationScenarios = messagesData.get("conversationScenarios");
        for (JsonNode scenario : conversationScenarios) {
            Map<String, Object> scenarioMap = new HashMap<>();
            scenarioMap.put("name", scenario.get("name").asText());
            scenarioMap.put("description", scenario.get("description").asText());

            List<String> participants = new ArrayList<>();
            JsonNode participantsNode = scenario.get("participants");
            for (JsonNode participant : participantsNode) {
                participants.add(participant.asText());
            }
            scenarioMap.put("participants", participants);

            List<String> messages = new ArrayList<>();
            JsonNode messagesNode = scenario.get("messages");
            for (JsonNode message : messagesNode) {
                messages.add(message.asText());
            }
            scenarioMap.put("messages", messages);
            scenarios.add(scenarioMap);
        }

        return scenarios;
    }

    // ==================== PHOTOS DATA METHODS ====================

    /**
     * Get test photos data
     *
     * @return List of test photos
     */
    public List<Map<String, Object>> getTestPhotos() {
        List<Map<String, Object>> photos = new ArrayList<>();

        JsonNode testPhotos = photosData.get("testPhotos");
        for (JsonNode photo : testPhotos) {
            Map<String, Object> photoMap = new HashMap<>();
            photoMap.put("id", photo.get("id").asText());
            photoMap.put("senderId", photo.get("senderId").asText());
            photoMap.put("senderUsername", photo.get("senderUsername").asText());
            photoMap.put("filename", photo.get("filename").asText());
            photoMap.put("caption", photo.get("caption").asText());
            photoMap.put("urlImage", photo.get("urlImage").asText());
            photoMap.put("timestamp", photo.get("timestamp").asText());

            List<String> recipients = new ArrayList<>();
            JsonNode recipientsNode = photo.get("recipients");
            for (JsonNode recipient : recipientsNode) {
                recipients.add(recipient.asText());
            }
            photoMap.put("recipients", recipients);

            List<String> recipientUsernames = new ArrayList<>();
            JsonNode recipientUsernamesNode = photo.get("recipientUsernames");
            for (JsonNode username : recipientUsernamesNode) {
                recipientUsernames.add(username.asText());
            }
            photoMap.put("recipientUsernames", recipientUsernames);

            photos.add(photoMap);
        }

        return photos;
    }

    /**
     * Get photo test scenarios
     *
     * @return Map of photo test scenarios
     */
    public Map<String, Map<String, Object>> getPhotoTestScenarios() {
        Map<String, Map<String, Object>> scenarios = new HashMap<>();

        JsonNode testScenarios = photosData.get("testScenarios");
        testScenarios.fieldNames().forEachRemaining(scenarioName -> {
            JsonNode scenario = testScenarios.get(scenarioName);
            Map<String, Object> scenarioMap = new HashMap<>();
            scenarioMap.put("description", scenario.get("description").asText());
            scenarioMap.put("caption", scenario.get("caption").asText());
            scenarioMap.put("expectedResult", scenario.get("expectedResult").asText());

            if (scenario.has("recipients")) {
                List<String> recipients = new ArrayList<>();
                JsonNode recipientsNode = scenario.get("recipients");
                for (JsonNode recipient : recipientsNode) {
                    recipients.add(recipient.asText());
                }
                scenarioMap.put("recipients", recipients);
            }

            if (scenario.has("selectAll")) {
                scenarioMap.put("selectAll", scenario.get("selectAll").asBoolean());
            }

            scenarios.put(scenarioName, scenarioMap);
        });

        return scenarios;
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Get random valid user credentials
     *
     * @return Random valid user credentials
     */
    public Map<String, Object> getRandomValidUser() {
        List<Map<String, Object>> validUsers = getValidLoginCredentials();
        if (validUsers.isEmpty()) {
            throw new RuntimeException("No valid users found in test data");
        }

        Random random = new Random();
        return validUsers.get(random.nextInt(validUsers.size()));
    }

    /**
     * Get random test message
     *
     * @return Random test message
     */
    public Map<String, Object> getRandomTestMessage() {
        List<Map<String, Object>> messages = getTestMessages();
        if (messages.isEmpty()) {
            throw new RuntimeException("No test messages found in test data");
        }

        Random random = new Random();
        return messages.get(random.nextInt(messages.size()));
    }

    /**
     * Get random test photo
     *
     * @return Random test photo
     */
    public Map<String, Object> getRandomTestPhoto() {
        List<Map<String, Object>> photos = getTestPhotos();
        if (photos.isEmpty()) {
            throw new RuntimeException("No test photos found in test data");
        }

        Random random = new Random();
        return photos.get(random.nextInt(photos.size()));
    }

    // ==================== INVALID LOGIN DATA METHODS ====================

    /**
     * Get invalid login credentials for negative testing
     *
     * @return Array of invalid login data [username, password, expectedError]
     */
    public static Object[][] getInvalidLoginData() {
        return new Object[][]{
                {"invalid_user", "invalid_pass", "Tài khoản hoặc mật khẩu không chính xác"},
                {"", "password123", "Vui lòng nhập đầy đủ thông tin!"},
                {"username", "", "Vui lòng nhập đầy đủ thông tin!"},
                {"", "", "Vui lòng nhập đầy đủ thông tin!"},
                {"nonexistent@email.com", "password123", "Tên đăng nhập không chính xác!"},
                {"admin", "wrongpassword", "Mật khẩu không chính xác!"},
                {"test123", "123456", "Tài khoản hoặc mật khẩu không chính xác"},
                {"user@domain", "pass", "Mật khẩu không chính xác!"},
                {"specialchar!@#", "password", "Tên đăng nhập không chính xác!"},
                {"verylongusernamethatexceedslimits", "password", "Tên đăng nhập không chính xác!"}
        };
    }

    /**
     * Get common invalid username patterns
     *
     * @return Array of invalid usernames
     */
    public static String[] getInvalidUsernames() {
        return new String[]{
                "",                              // Empty
                " ",                             // Space only
                "a",                             // Too short
                "verylongusernamethatexceedsnormallimitsandshouldfail", // Too long
                "user@#$%",                      // Special characters
                "user with spaces",              // Spaces
                "user\nwith\nnewlines",         // Newlines
                "user'with'quotes",             // Single quotes
                "user\"with\"doublequotes",     // Double quotes
                "user<script>alert('xss')</script>", // XSS attempt
                "../../etc/passwd",             // Path traversal
                "SELECT * FROM users",          // SQL injection attempt
                "null",                         // Null string
                "undefined",                    // Undefined string
                "0",                           // Numeric string
                "false",                       // Boolean string
                "{}",                          // JSON object
                "[]"                           // JSON array
        };
    }

    /**
     * Get common invalid password patterns
     *
     * @return Array of invalid passwords
     */
    public static String[] getInvalidPasswords() {
        return new String[]{
                "",                              // Empty
                " ",                             // Space only
                "1",                             // Too short
                "12",                            // Too short
                "123",                           // Too short
                "password",                      // Common weak password
                "123456",                        // Common weak password
                "admin",                         // Common weak password
                "test",                          // Common weak password
                "qwerty",                        // Common weak password
                "abc123",                        // Common weak password
                "password123",                   // Common pattern
                "admin123",                      // Common pattern
                "verylongpasswordthatexceedsnormallimitsandshouldfailvalidation", // Too long
                "pass with spaces",              // Spaces
                "pass\nwith\nnewlines",         // Newlines
                "pass'with'quotes",             // Single quotes
                "pass\"with\"doublequotes",     // Double quotes
                "<script>alert('xss')</script>", // XSS attempt
                "../../etc/passwd",             // Path traversal
                "DROP TABLE users",             // SQL injection attempt
                "null",                         // Null string
                "undefined",                    // Undefined string
                "false",                       // Boolean string
                "{}"                           // JSON object
        };
    }

    /**
     * Get test data for boundary value testing
     *
     * @return Array of boundary test data [username, password, description]
     */
    public static Object[][] getBoundaryLoginData() {
        return new Object[][]{
                {"", "", "Both empty"},
                {"a", "b", "Minimum length"},
                {"ab", "cd", "Two characters"},
                {"abc", "def", "Three characters"},
                {"user", "pass", "Four characters"},
                {"username", "password", "Normal length"},
                {"verylongusername", "verylongpassword", "Long credentials"},
                {"extremelylongusernamethatexceedsnormallimits", "extremelylongpasswordthatexceedsnormallimits", "Extremely long"},
                {"user123", "", "Username only"},
                {"", "pass123", "Password only"},
                {"   ", "   ", "Whitespace only"},
                {"user\t", "pass\t", "With tabs"},
                {"user\n", "pass\n", "With newlines"},
                {"user ", " pass", "With leading/trailing spaces"}
        };
    }
}