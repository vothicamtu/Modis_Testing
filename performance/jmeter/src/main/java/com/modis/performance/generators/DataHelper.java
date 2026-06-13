package com.modis.performance.generators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class DataHelper {
    private static final Logger log = LoggerFactory.getLogger(DataHelper.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final List<String> FIRST_NAMES = Arrays.asList(
            "Alice", "Bob", "Charlie", "Diana", "Eve", "Frank", "Grace", "Henry",
            "Ivy", "Jack", "Kate", "Liam", "Mia", "Noah", "Olivia", "Paul",
            "Quinn", "Ruby", "Sam", "Tina", "Uma", "Victor", "Wendy", "Xander", "Yara", "Zoe"
    );
    private static final List<String> LAST_NAMES = Arrays.asList(
            "Anderson", "Brown", "Clark", "Davis", "Evans", "Foster", "Garcia", "Harris",
            "Johnson", "King", "Lee", "Miller", "Nelson", "O'Connor", "Parker", "Quinn",
            "Roberts", "Smith", "Taylor", "Underwood", "Valdez", "Wilson", "Young", "Zhang"
    );
    private static final List<String> MESSAGE_TEMPLATES = Arrays.asList(
            "Hello! How are you doing today?",
            "Great to see you online!",
            "Thanks for sharing that photo!",
            "Looking forward to our meeting.",
            "Have a wonderful day!",
            "Check out this amazing view!",
            "Just finished a great workout.",
            "Coffee time! ☕",
            "Beautiful sunset tonight 🌅",
            "Weekend plans anyone?"
    );
    private static final List<String> PHOTO_CAPTIONS = Arrays.asList(
            "Beautiful day! 🌞",
            "Living my best life ✨",
            "Good vibes only 😊",
            "Making memories 📸",
            "Adventure time! 🚀",
            "Blessed and grateful 🙏",
            "Sunset magic 🌅",
            "Coffee and contemplation ☕",
            "Friends forever 👫",
            "Nature therapy 🌿"
    );
    private static final List<String> REACTIONS = Arrays.asList(
            "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣",
            "😊", "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰",
            "👍", "👎", "👌", "✌️", "🤞", "🤟", "🤘", "👏",
            "❤️", "💙", "💚", "💛", "🧡", "💜", "🖤", "🤍"
    );

    public static Map<String, String> generateRandomUser(int userIndex) {
        Map<String, String> user = new HashMap<>();
        try {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String firstName = getRandomElement(FIRST_NAMES);
            String lastName = getRandomElement(LAST_NAMES);
            user.put("username", "user" + userIndex + "_" + timestamp);
            user.put("fullName", firstName + " " + lastName);
            user.put("firstName", firstName);
            user.put("lastName", lastName);
            user.put("email", firstName.toLowerCase() + "." + lastName.toLowerCase() + userIndex + "@testmail.com");
            user.put("phone", generateRandomPhone());
            user.put("password", "TestPass123!" + userIndex);
        } catch (Exception e) {
            log.error("Error generating random user: {}", e.getMessage());
            user.put("username", "default_user_" + userIndex);
            user.put("fullName", "Test User " + userIndex);
            user.put("email", "test" + userIndex + "@example.com");
            user.put("phone", "1234567890");
            user.put("password", "password123");
        }
        return user;
    }

    public static Map<String, String> generateRandomUser() {
        return generateRandomUser(0);
    }

    public static String generateRandomMessage() {
        try {
            return getRandomElement(MESSAGE_TEMPLATES);
        } catch (Exception e) {
            log.error("Error generating random message: {}", e.getMessage());
            return "Hello there!";
        }
    }

    public static String generateRandomPhotoCaption() {
        try {
            return getRandomElement(PHOTO_CAPTIONS);
        } catch (Exception e) {
            log.error("Error generating random photo caption: {}", e.getMessage());
            return "Great photo!";
        }
    }

    public static String generateRandomReaction() {
        try {
            return getRandomElement(REACTIONS);
        } catch (Exception e) {
            log.error("Error generating random reaction: {}", e.getMessage());
            return "👍";
        }
    }

    public static String generateRandomPhone() {
        try {
            StringBuilder phone = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                phone.append(ThreadLocalRandom.current().nextInt(0, 10));
            }
            return phone.toString();
        } catch (Exception e) {
            log.error("Error generating random phone: {}", e.getMessage());
            return "1234567890";
        }
    }

    public static String generateRandomSearchQuery() {
        try {
            List<String> queries = Arrays.asList(
                    getRandomElement(FIRST_NAMES),
                    getRandomElement(LAST_NAMES),
                    getRandomElement(FIRST_NAMES) + " " + getRandomElement(LAST_NAMES),
                    "test",
                    "user",
                    "photo",
                    "message"
            );
            return getRandomElement(queries);
        } catch (Exception e) {
            log.error("Error generating random search query: {}", e.getMessage());
            return "test";
        }
    }

    public static String createUserRegistrationBody(Map<String, String> userData) {
        try {
            return mapper.writeValueAsString(userData);
        } catch (Exception e) {
            log.error("Error creating user registration body: {}", e.getMessage());
            return "{}";
        }
    }

    public static String createMessageBody(String recipientId, String messageText) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("recipientId", recipientId);
            messageData.put("text", messageText);
            messageData.put("timestamp", System.currentTimeMillis());
            return mapper.writeValueAsString(messageData);
        } catch (Exception e) {
            log.error("Error creating message body: {}", e.getMessage());
            return "{}";
        }
    }

    public static String createPhotoUploadBody(String caption, List<String> recipients) {
        try {
            Map<String, Object> photoData = new HashMap<>();
            photoData.put("caption", caption);
            photoData.put("recipients", recipients != null ? recipients : Arrays.asList());
            photoData.put("timestamp", System.currentTimeMillis());
            photoData.put("location", "Test Location");
            return mapper.writeValueAsString(photoData);
        } catch (Exception e) {
            log.error("Error creating photo upload body: {}", e.getMessage());
            return "{}";
        }
    }

    public static String createPhotoUploadBody(String caption) {
        return createPhotoUploadBody(caption, Arrays.asList());
    }

    public static String createFriendRequestBody(String targetUserId) {
        try {
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("targetUserId", targetUserId);
            requestData.put("timestamp", System.currentTimeMillis());
            return mapper.writeValueAsString(requestData);
        } catch (Exception e) {
            log.error("Error creating friend request body: {}", e.getMessage());
            return "{}";
        }
    }

    public static String extractUserId(String responseData) {
        try {
            if (responseData == null || responseData.trim().isEmpty()) {
                return null;
            }
            JsonNode jsonResponse = mapper.readTree(responseData);
            if (jsonResponse.has("userId")) return jsonResponse.get("userId").asText();
            if (jsonResponse.has("id")) return jsonResponse.get("id").asText();
            if (jsonResponse.has("user") && jsonResponse.get("user").has("id"))
                return jsonResponse.get("user").get("id").asText();
            if (jsonResponse.has("data") && jsonResponse.get("data").has("id"))
                return jsonResponse.get("data").get("id").asText();
            return null;
        } catch (Exception e) {
            log.error("Error extracting user ID: {}", e.getMessage());
            return null;
        }
    }

    public static long generateRandomDelay(long minMs, long maxMs) {
        try {
            return ThreadLocalRandom.current().nextLong(minMs, maxMs + 1);
        } catch (Exception e) {
            log.error("Error generating random delay: {}", e.getMessage());
            return 1000;
        }
    }

    public static long generateRandomDelay() {
        return generateRandomDelay(500, 3000);
    }

    private static <T> T getRandomElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        int randomIndex = ThreadLocalRandom.current().nextInt(list.size());
        return list.get(randomIndex);
    }

    public static long generateRecentTimestamp(int daysBack) {
        try {
            long now = System.currentTimeMillis();
            long daysInMs = daysBack * 24L * 60L * 60L * 1000L;
            long randomOffset = ThreadLocalRandom.current().nextLong(0, daysInMs);
            return now - randomOffset;
        } catch (Exception e) {
            log.error("Error generating recent timestamp: {}", e.getMessage());
            return System.currentTimeMillis();
        }
    }

    public static long generateRecentTimestamp() {
        return generateRecentTimestamp(30);
    }

    public static boolean validateResponseStructure(String responseData, List<String> requiredFields) {
        try {
            if (responseData == null || responseData.trim().isEmpty()) {
                return false;
            }
            JsonNode jsonResponse = mapper.readTree(responseData);
            for (String field : requiredFields) {
                if (!jsonResponse.has(field)) {
                    log.warn("Required field '{}' not found in response", field);
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("Error validating response structure: {}", e.getMessage());
            return false;
        }
    }
}
