package com.modis.utils;

import org.slf4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for reading configuration properties
 * Supports multiple configuration sources with priority order
 */
public class ConfigReader {
    
    private static final Logger logger = LoggerUtil.getLogger(ConfigReader.class);
    private static Properties properties;
    private static boolean isLoaded = false;
    
    // Configuration file paths in priority order
    private static final String[] CONFIG_PATHS = {
        "src/test/resources/config/test.properties",
        "src/test/resources/config/android.properties", 
        "src/test/resources/config/ios.properties",
        "src/test/resources/application.properties",
        "config.properties"
    };
    
    // Environment-specific configuration
    private static final String ENV_CONFIG_PREFIX = "src/test/resources/config/";
    private static final String ENV_CONFIG_SUFFIX = ".properties";
    
    /**
     * Load configuration from properties files
     */
    public static synchronized void loadConfiguration() {
        if (isLoaded) {
            return;
        }
        
        properties = new Properties();
        
        // Load default configurations
        loadDefaultConfigurations();
        
        // Load environment-specific configuration
        loadEnvironmentConfiguration();
        
        // Load system properties (highest priority)
        loadSystemProperties();
        
        isLoaded = true;
        logger.info("Configuration loaded successfully");
    }
    
    /**
     * Get property value
     * @param key Property key
     * @return Property value or null if not found
     */
    public static String getProperty(String key) {
        ensureConfigurationLoaded();
        String value = properties.getProperty(key);
        logger.debug("Retrieved property: {} = {}", key, value);
        return value;
    }
    
    /**
     * Get property value with default fallback
     * @param key Property key
     * @param defaultValue Default value if property not found
     * @return Property value or default value
     */
    public static String getProperty(String key, String defaultValue) {
        ensureConfigurationLoaded();
        String value = properties.getProperty(key, defaultValue);
        logger.debug("Retrieved property: {} = {} (default: {})", key, value, defaultValue);
        return value;
    }
    
    /**
     * Get boolean property value
     * @param key Property key
     * @param defaultValue Default value if property not found
     * @return Boolean property value
     */
    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        
        try {
            return Boolean.parseBoolean(value.trim());
        } catch (Exception e) {
            logger.warn("Invalid boolean value for property {}: {}, using default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Get integer property value
     * @param key Property key
     * @param defaultValue Default value if property not found
     * @return Integer property value
     */
    public static int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer value for property {}: {}, using default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Get long property value
     * @param key Property key
     * @param defaultValue Default value if property not found
     * @return Long property value
     */
    public static long getLongProperty(String key, long defaultValue) {
        String value = getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            logger.warn("Invalid long value for property {}: {}, using default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Get double property value
     * @param key Property key
     * @param defaultValue Default value if property not found
     * @return Double property value
     */
    public static double getDoubleProperty(String key, double defaultValue) {
        String value = getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            logger.warn("Invalid double value for property {}: {}, using default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Get array property value (comma-separated)
     * @param key Property key
     * @param defaultValue Default array if property not found
     * @return String array
     */
    public static String[] getArrayProperty(String key, String[] defaultValue) {
        String value = getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        
        try {
            return value.split(",");
        } catch (Exception e) {
            logger.warn("Invalid array value for property {}: {}, using default", key, value);
            return defaultValue;
        }
    }
    
    /**
     * Set property value (runtime override)
     * @param key Property key
     * @param value Property value
     */
    public static void setProperty(String key, String value) {
        ensureConfigurationLoaded();
        properties.setProperty(key, value);
        logger.debug("Set property: {} = {}", key, value);
    }
    
    /**
     * Check if property exists
     * @param key Property key
     * @return true if property exists, false otherwise
     */
    public static boolean hasProperty(String key) {
        ensureConfigurationLoaded();
        return properties.containsKey(key);
    }
    
    /**
     * Get all properties
     * @return Properties object
     */
    public static Properties getAllProperties() {
        ensureConfigurationLoaded();
        return new Properties(properties);
    }
    
    /**
     * Reload configuration (useful for dynamic config changes)
     */
    public static synchronized void reloadConfiguration() {
        isLoaded = false;
        properties = null;
        loadConfiguration();
        logger.info("Configuration reloaded");
    }
    
    /**
     * Get configuration summary for logging
     * @return Configuration summary string
     */
    public static String getConfigurationSummary() {
        ensureConfigurationLoaded();
        StringBuilder summary = new StringBuilder();
        summary.append("Configuration Summary:\n");
        
        // Key configuration properties to display
        String[] keyProperties = {
            "platform", "deviceName", "platformVersion",
            "appium.serverUrl", "android.appPackage", "ios.bundleId",
            "test.environment", "test.timeout", "test.retryCount"
        };
        
        for (String key : keyProperties) {
            String value = properties.getProperty(key);
            if (value != null) {
                // Mask sensitive values
                if (key.toLowerCase().contains("password") || key.toLowerCase().contains("secret")) {
                    value = "***";
                }
                summary.append(String.format("  %s = %s\n", key, value));
            }
        }
        
        return summary.toString();
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    /**
     * Ensure configuration is loaded
     */
    private static void ensureConfigurationLoaded() {
        if (!isLoaded) {
            loadConfiguration();
        }
    }
    
    /**
     * Load default configuration files
     */
    private static void loadDefaultConfigurations() {
        for (String configPath : CONFIG_PATHS) {
            loadPropertiesFile(configPath, false);
        }
    }
    
    /**
     * Load environment-specific configuration
     */
    private static void loadEnvironmentConfiguration() {
        String environment = System.getProperty("test.environment", 
                                               System.getenv("TEST_ENVIRONMENT"));
        
        if (environment != null && !environment.trim().isEmpty()) {
            String envConfigPath = ENV_CONFIG_PREFIX + environment.toLowerCase() + ENV_CONFIG_SUFFIX;
            loadPropertiesFile(envConfigPath, false);
            logger.info("Loaded environment-specific configuration: {}", environment);
        }
    }
    
    /**
     * Load system properties (highest priority)
     */
    private static void loadSystemProperties() {
        // Override with system properties
        System.getProperties().forEach((key, value) -> {
            if (key instanceof String && value instanceof String) {
                properties.setProperty((String) key, (String) value);
            }
        });
        
        // Override with environment variables (convert to property format)
        System.getenv().forEach((key, value) -> {
            // Convert environment variable names to property format
            String propertyKey = key.toLowerCase().replace('_', '.');
            properties.setProperty(propertyKey, value);
        });
    }
    
    /**
     * Load properties from file
     * @param filePath Path to properties file
     * @param required Whether the file is required
     */
    private static void loadPropertiesFile(String filePath, boolean required) {
        try (InputStream inputStream = getInputStream(filePath)) {
            if (inputStream != null) {
                Properties fileProperties = new Properties();
                fileProperties.load(inputStream);
                
                // Merge properties (later files override earlier ones)
                fileProperties.forEach((key, value) -> properties.setProperty((String) key, (String) value));
                
                logger.debug("Loaded configuration file: {}", filePath);
            } else if (required) {
                logger.error("Required configuration file not found: {}", filePath);
                throw new RuntimeException("Required configuration file not found: " + filePath);
            } else {
                logger.debug("Optional configuration file not found: {}", filePath);
            }
        } catch (IOException e) {
            if (required) {
                logger.error("Failed to load required configuration file: {}", filePath, e);
                throw new RuntimeException("Failed to load configuration file: " + filePath, e);
            } else {
                logger.debug("Failed to load optional configuration file: {}", filePath, e);
            }
        }
    }
    
    /**
     * Get input stream for configuration file
     * @param filePath Path to configuration file
     * @return InputStream or null if file not found
     */
    private static InputStream getInputStream(String filePath) {
        try {
            // Try as file path first
            java.io.File file = new java.io.File(filePath);
            if (file.exists()) {
                return new FileInputStream(file);
            }
            
            // Try as classpath resource
            InputStream resourceStream = ConfigReader.class.getClassLoader().getResourceAsStream(filePath);
            if (resourceStream != null) {
                return resourceStream;
            }
            
            // Try without leading path
            String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
            return ConfigReader.class.getClassLoader().getResourceAsStream(fileName);
            
        } catch (Exception e) {
            logger.debug("Failed to get input stream for: {}", filePath, e);
            return null;
        }
    }
    
    /**
     * Validate configuration after loading
     */
    private static void validateConfiguration() {
        // Add validation logic for required properties
        String[] requiredProperties = {
            "platform"
        };
        
        for (String required : requiredProperties) {
            if (!hasProperty(required) || getProperty(required).trim().isEmpty()) {
                logger.warn("Required property not set: {}", required);
            }
        }
    }
    
    /**
     * Get property with environment variable fallback
     * @param key Property key
     * @param envKey Environment variable key
     * @param defaultValue Default value
     * @return Property value
     */
    public static String getPropertyWithEnvFallback(String key, String envKey, String defaultValue) {
        String value = getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            value = System.getenv(envKey);
        }
        return value != null ? value : defaultValue;
    }
    
    /**
     * Get platform-specific property
     * @param baseKey Base property key
     * @param platform Platform name (android/ios)
     * @param defaultValue Default value
     * @return Platform-specific property value
     */
    public static String getPlatformProperty(String baseKey, String platform, String defaultValue) {
        // Try platform-specific property first
        String platformKey = platform.toLowerCase() + "." + baseKey;
        String value = getProperty(platformKey);
        
        if (value == null || value.trim().isEmpty()) {
            // Fall back to generic property
            value = getProperty(baseKey, defaultValue);
        }
        
        return value;
    }
}