package com.modis.constants;

/**
 * Application constants for Modis mobile app automation
 * Contains configuration values, timeouts, and app-specific constants
 */
public class AppConstants {
    
    // ==================== APP INFORMATION ====================
    public static final String APP_NAME = "Modis";
    public static final String APP_PACKAGE = "com.modis";
    public static final String APP_ACTIVITY = "com.modis.MainActivity";
    public static final String APP_BUNDLE_ID = "com.modis.app";
    
    // ==================== APPIUM SERVER CONFIGURATION ====================
    public static final String APPIUM_SERVER_URL = "http://127.0.0.1:4723";
    public static final String APPIUM_SERVER_PATH = "/wd/hub";
    
    // ==================== DEVICE CONFIGURATION ====================
    public static final String DEFAULT_PLATFORM = "Android";
    public static final String DEFAULT_DEVICE_NAME = "Android Emulator";
    public static final String DEFAULT_PLATFORM_VERSION = "11.0";
    public static final String DEFAULT_AUTOMATION_NAME = "UiAutomator2";
    
    // ==================== TIMEOUT CONSTANTS (in seconds) ====================
    public static final int IMPLICIT_WAIT = 10;
    public static final int EXPLICIT_WAIT = 20;
    public static final int SHORT_WAIT = 5;
    public static final int LONG_WAIT = 60;
    public static final int ELEMENT_WAIT_TIMEOUT = 15;
    public static final int PAGE_LOAD_TIMEOUT = 30;
    public static final int NETWORK_WAIT = 3;
    public static final int ANIMATION_WAIT = 2;
    
    // ==================== RETRY CONFIGURATION ====================
    public static final int MAX_RETRY_COUNT = 3;
    public static final int RETRY_DELAY_MS = 1000;
    public static final int MAX_SCROLL_ATTEMPTS = 10;
    
    // ==================== SCREENSHOT CONFIGURATION ====================
    public static final String SCREENSHOT_DIR = "screenshots";
    public static final String SCREENSHOT_FORMAT = "png";
    public static final boolean TAKE_SCREENSHOT_ON_FAILURE = true;
    public static final boolean TAKE_SCREENSHOT_ON_PASS = false;
    
    // ==================== LOGGING CONFIGURATION ====================
    public static final String LOG_DIR = "logs";
    public static final String LOG_FILE_NAME = "automation.log";
    public static final boolean ENABLE_DEBUG_LOGGING = true;
    public static final boolean LOG_PAGE_SOURCE_ON_FAILURE = false;
    
    // ==================== REPORT CONFIGURATION ====================
    public static final String REPORTS_DIR = "reports";
    public static final String EXTENT_REPORT_NAME = "ModisAutomationReport.html";
    public static final String ALLURE_RESULTS_DIR = "allure-results";
    
    // ==================== TEST DATA CONFIGURATION ====================
    public static final String TEST_DATA_DIR = "src/test/resources/testdata";
    public static final String TEST_USERS_FILE = "users.json";
    public static final String TEST_MESSAGES_FILE = "messages.json";
    
    // ==================== GESTURE CONFIGURATION ====================
    public static final int SWIPE_DURATION_MS = 1000;
    public static final int TAP_DURATION_MS = 100;
    public static final int LONG_PRESS_DURATION_MS = 2000;
    public static final double SWIPE_START_PERCENTAGE = 0.8;
    public static final double SWIPE_END_PERCENTAGE = 0.2;
    
    // ==================== PERFORMANCE THRESHOLDS ====================
    public static final int MAX_APP_LAUNCH_TIME_MS = 10000;
    public static final int MAX_SCREEN_LOAD_TIME_MS = 5000;
    public static final int MAX_API_RESPONSE_TIME_MS = 3000;
    public static final int MAX_IMAGE_LOAD_TIME_MS = 8000;
    
    // ==================== NETWORK CONFIGURATION ====================
    public static final String DEFAULT_NETWORK_SPEED = "full";
    public static final boolean ENABLE_GPS = true;
    public static final boolean AUTO_GRANT_PERMISSIONS = true;
    public static final boolean AUTO_ACCEPT_ALERTS = true;
    
    // ==================== TEST ENVIRONMENT ====================
    public static final String DEFAULT_ENVIRONMENT = "dev";
    public static final String DEV_BASE_URL = "https://dev-api.modis.com";
    public static final String STAGING_BASE_URL = "https://staging-api.modis.com";
    public static final String PROD_BASE_URL = "https://api.modis.com";
    
    // ==================== MODIS APP SPECIFIC CONSTANTS ====================
    
    // Screen Names
    public static final String LOADING_SCREEN = "LoadingScreen";
    public static final String LOGIN_SCREEN = "LoginScreen";
    public static final String SIGNUP_SCREEN = "SignupScreen";
    public static final String HOME_SCREEN = "HomeScreen";
    public static final String TAKE_SCREEN = "TakeScreen";
    public static final String SEND_PHOTO_SCREEN = "SendPhotoScreen";
    public static final String FRIENDS_SCREEN = "FriendsScreen";
    public static final String PROFILE_SCREEN = "ProfileScreen";
    public static final String MESSAGE_SCREEN = "MessageScreen";
    public static final String CONVERSATION_SCREEN = "ConversationScreen";
    public static final String ALL_IMAGES_SCREEN = "AllImagesScreen";
    
    // Navigation Flow
    public static final String INITIAL_ROUTE = "LoadingScreen";
    
    // User Roles
    public static final String ROLE_USER = "user";
    public static final String ROLE_ADMIN = "admin";
    
    // Friend Request States
    public static final String FRIEND_STATUS_PENDING = "pending";
    public static final String FRIEND_STATUS_ACCEPTED = "accepted";
    public static final String FRIEND_STATUS_DECLINED = "declined";
    
    // Message Types
    public static final String MESSAGE_TYPE_TEXT = "text";
    public static final String MESSAGE_TYPE_IMAGE = "image";
    public static final String MESSAGE_TYPE_EMOJI = "emoji";
    
    // Image Upload
    public static final int MAX_IMAGE_SIZE_MB = 10;
    public static final String[] SUPPORTED_IMAGE_FORMATS = {"jpg", "jpeg", "png", "gif"};
    
    // Feed Configuration
    public static final int FEED_PAGE_SIZE = 20;
    public static final int MAX_FEED_SCROLL_ATTEMPTS = 5;
    
    // Search Configuration
    public static final int MIN_SEARCH_QUERY_LENGTH = 2;
    public static final int SEARCH_DEBOUNCE_MS = 500;
    
    // Camera Configuration
    public static final String CAMERA_PERMISSION = "android.permission.CAMERA";
    public static final String STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
    public static final String LOCATION_PERMISSION = "android.permission.ACCESS_FINE_LOCATION";
    
    // Notification Types
    public static final String NOTIFICATION_FRIEND_REQUEST = "friend_request";
    public static final String NOTIFICATION_MESSAGE = "message";
    public static final String NOTIFICATION_PHOTO_RECEIVED = "photo_received";
    
    // Theme Configuration
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String THEME_SYSTEM = "system";
    
    // ==================== VALIDATION CONSTANTS ====================
    
    // Username validation
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 20;
    public static final String USERNAME_PATTERN = "^[a-zA-Z0-9_]+$";
    
    // Password validation
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 50;
    
    // Email validation
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    // Phone validation
    public static final String PHONE_PATTERN = "^[0-9]{10,15}$";
    
    // ==================== ERROR MESSAGES ====================
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid username or password";
    public static final String ERROR_USERNAME_TAKEN = "Username is already taken";
    public static final String ERROR_EMAIL_TAKEN = "Email is already registered";
    public static final String ERROR_NETWORK_ERROR = "Network connection error";
    public static final String ERROR_SERVER_ERROR = "Server error occurred";
    public static final String ERROR_PERMISSION_DENIED = "Permission denied";
    public static final String ERROR_CAMERA_NOT_AVAILABLE = "Camera not available";
    
    // ==================== SUCCESS MESSAGES ====================
    public static final String SUCCESS_LOGIN = "Login successful";
    public static final String SUCCESS_SIGNUP = "Account created successfully";
    public static final String SUCCESS_PHOTO_SENT = "Photo sent successfully";
    public static final String SUCCESS_FRIEND_REQUEST_SENT = "Friend request sent";
    public static final String SUCCESS_FRIEND_REQUEST_ACCEPTED = "Friend request accepted";
    public static final String SUCCESS_MESSAGE_SENT = "Message sent";
    
    // ==================== TEST DATA CONSTANTS ====================
    
    // Test Users
    public static final String TEST_USER_USERNAME = "testuser";
    public static final String TEST_USER_PASSWORD = "testpass123";
    public static final String TEST_USER_EMAIL = "testuser@example.com";
    public static final String TEST_USER_PHONE = "1234567890";
    public static final String TEST_USER_FULLNAME = "Test User";
    
    public static final String TEST_ADMIN_USERNAME = "testadmin";
    public static final String TEST_ADMIN_PASSWORD = "adminpass123";
    
    // Test Messages
    public static final String TEST_MESSAGE_TEXT = "Hello, this is a test message!";
    public static final String TEST_MESSAGE_LONG = "This is a very long test message that should test the message display and wrapping functionality in the chat interface.";
    
    // Test Search Queries
    public static final String TEST_SEARCH_QUERY = "test";
    public static final String TEST_SEARCH_NO_RESULTS = "xyzabc123";
    
    // ==================== PLATFORM SPECIFIC CONSTANTS ====================
    
    // Android specific
    public static final String ANDROID_SETTINGS_PACKAGE = "com.android.settings";
    public static final String ANDROID_CAMERA_PACKAGE = "com.android.camera2";
    public static final String ANDROID_GALLERY_PACKAGE = "com.google.android.apps.photos";
    
    // iOS specific
    public static final String IOS_SETTINGS_BUNDLE = "com.apple.Preferences";
    public static final String IOS_CAMERA_BUNDLE = "com.apple.camera";
    public static final String IOS_PHOTOS_BUNDLE = "com.apple.mobileslideshow";
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Get base URL based on environment
     * @param environment Environment name
     * @return Base URL for the environment
     */
    public static String getBaseUrl(String environment) {
        switch (environment.toLowerCase()) {
            case "dev":
            case "development":
                return DEV_BASE_URL;
            case "staging":
            case "stage":
                return STAGING_BASE_URL;
            case "prod":
            case "production":
                return PROD_BASE_URL;
            default:
                return DEV_BASE_URL;
        }
    }
    
    /**
     * Get app package based on platform
     * @param platform Platform name (android/ios)
     * @return App package/bundle ID
     */
    public static String getAppPackage(String platform) {
        return platform.toLowerCase().equals("ios") ? APP_BUNDLE_ID : APP_PACKAGE;
    }
    
    /**
     * Get platform-specific settings package
     * @param platform Platform name (android/ios)
     * @return Settings package/bundle ID
     */
    public static String getSettingsPackage(String platform) {
        return platform.toLowerCase().equals("ios") ? IOS_SETTINGS_BUNDLE : ANDROID_SETTINGS_PACKAGE;
    }
    
    /**
     * Check if environment is production
     * @param environment Environment name
     * @return true if production, false otherwise
     */
    public static boolean isProduction(String environment) {
        return "prod".equalsIgnoreCase(environment) || "production".equalsIgnoreCase(environment);
    }
    
    /**
     * Get timeout based on operation type
     * @param operationType Type of operation (element, page, network, etc.)
     * @return Timeout in seconds
     */
    public static int getTimeout(String operationType) {
        switch (operationType.toLowerCase()) {
            case "element":
                return ELEMENT_WAIT_TIMEOUT;
            case "page":
                return PAGE_LOAD_TIMEOUT;
            case "network":
                return NETWORK_WAIT;
            case "short":
                return SHORT_WAIT;
            case "long":
                return LONG_WAIT;
            default:
                return EXPLICIT_WAIT;
        }
    }
    
    // Private constructor to prevent instantiation
    private AppConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}