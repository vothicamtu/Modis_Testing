package com.modis.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for logging configuration and management
 * Provides centralized logging functionality with context support
 */
public class LoggerUtil {
    
    private static final String LOG_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    // MDC keys for contextual logging
    public static final String TEST_CLASS_KEY = "testClass";
    public static final String TEST_METHOD_KEY = "testMethod";
    public static final String PLATFORM_KEY = "platform";
    public static final String DEVICE_KEY = "device";
    public static final String SESSION_ID_KEY = "sessionId";
    
    /**
     * Get logger for specified class
     * @param clazz Class to get logger for
     * @return Logger instance
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
    
    /**
     * Get logger for specified name
     * @param name Logger name
     * @return Logger instance
     */
    public static Logger getLogger(String name) {
        return LoggerFactory.getLogger(name);
    }
    
    /**
     * Set test context in MDC for contextual logging
     * @param testClass Test class name
     * @param testMethod Test method name
     */
    public static void setTestContext(String testClass, String testMethod) {
        MDC.put(TEST_CLASS_KEY, testClass);
        MDC.put(TEST_METHOD_KEY, testMethod);
    }
    
    /**
     * Set platform context in MDC
     * @param platform Platform name (Android/iOS)
     * @param deviceName Device name
     */
    public static void setPlatformContext(String platform, String deviceName) {
        MDC.put(PLATFORM_KEY, platform);
        MDC.put(DEVICE_KEY, deviceName);
    }
    
    /**
     * Set session ID in MDC
     * @param sessionId Appium session ID
     */
    public static void setSessionId(String sessionId) {
        MDC.put(SESSION_ID_KEY, sessionId);
    }
    
    /**
     * Clear test context from MDC
     */
    public static void clearTestContext() {
        MDC.remove(TEST_CLASS_KEY);
        MDC.remove(TEST_METHOD_KEY);
    }
    
    /**
     * Clear platform context from MDC
     */
    public static void clearPlatformContext() {
        MDC.remove(PLATFORM_KEY);
        MDC.remove(DEVICE_KEY);
    }
    
    /**
     * Clear session ID from MDC
     */
    public static void clearSessionId() {
        MDC.remove(SESSION_ID_KEY);
    }
    
    /**
     * Clear all MDC context
     */
    public static void clearAllContext() {
        MDC.clear();
    }
    
    /**
     * Log test start with context
     * @param logger Logger instance
     * @param testClass Test class name
     * @param testMethod Test method name
     */
    public static void logTestStart(Logger logger, String testClass, String testMethod) {
        setTestContext(testClass, testMethod);
        logger.info("=== TEST START: {}.{} ===", testClass, testMethod);
        logger.info("Test started at: {}", DATE_FORMAT.format(new Date()));
    }
    
    /**
     * Log test end with result
     * @param logger Logger instance
     * @param testClass Test class name
     * @param testMethod Test method name
     * @param result Test result (PASS/FAIL/SKIP)
     * @param durationMs Test duration in milliseconds
     */
    public static void logTestEnd(Logger logger, String testClass, String testMethod, String result, long durationMs) {
        logger.info("=== TEST END: {}.{} - {} ===", testClass, testMethod, result);
        logger.info("Test duration: {}ms", durationMs);
        logger.info("Test ended at: {}", DATE_FORMAT.format(new Date()));
        clearTestContext();
    }
    
    /**
     * Log step execution
     * @param logger Logger instance
     * @param stepName Step name
     * @param description Step description
     */
    public static void logStep(Logger logger, String stepName, String description) {
        logger.info("STEP: {} - {}", stepName, description);
    }
    
    /**
     * Log step execution with parameters
     * @param logger Logger instance
     * @param stepName Step name
     * @param description Step description
     * @param parameters Step parameters
     */
    public static void logStep(Logger logger, String stepName, String description, Object... parameters) {
        logger.info("STEP: {} - {}", stepName, String.format(description, parameters));
    }
    
    /**
     * Log assertion
     * @param logger Logger instance
     * @param assertion Assertion description
     * @param expected Expected value
     * @param actual Actual value
     * @param passed Whether assertion passed
     */
    public static void logAssertion(Logger logger, String assertion, Object expected, Object actual, boolean passed) {
        if (passed) {
            logger.info("ASSERTION PASSED: {} | Expected: {} | Actual: {}", assertion, expected, actual);
        } else {
            logger.error("ASSERTION FAILED: {} | Expected: {} | Actual: {}", assertion, expected, actual);
        }
    }
    
    /**
     * Log element interaction
     * @param logger Logger instance
     * @param action Action performed
     * @param element Element identifier
     * @param value Value used (optional)
     */
    public static void logElementInteraction(Logger logger, String action, String element, String value) {
        if (value != null && !value.isEmpty()) {
            logger.info("ELEMENT ACTION: {} on '{}' with value '{}'", action, element, value);
        } else {
            logger.info("ELEMENT ACTION: {} on '{}'", action, element);
        }
    }
    
    /**
     * Log navigation
     * @param logger Logger instance
     * @param from Source screen/page
     * @param to Destination screen/page
     */
    public static void logNavigation(Logger logger, String from, String to) {
        logger.info("NAVIGATION: {} -> {}", from, to);
    }
    
    /**
     * Log API call
     * @param logger Logger instance
     * @param method HTTP method
     * @param url API URL
     * @param statusCode Response status code
     * @param responseTime Response time in milliseconds
     */
    public static void logApiCall(Logger logger, String method, String url, int statusCode, long responseTime) {
        logger.info("API CALL: {} {} | Status: {} | Time: {}ms", method, url, statusCode, responseTime);
    }
    
    /**
     * Log performance metrics
     * @param logger Logger instance
     * @param operation Operation name
     * @param durationMs Duration in milliseconds
     */
    public static void logPerformance(Logger logger, String operation, long durationMs) {
        logger.info("PERFORMANCE: {} completed in {}ms", operation, durationMs);
    }
    
    /**
     * Log device information
     * @param logger Logger instance
     * @param platform Platform name
     * @param deviceName Device name
     * @param platformVersion Platform version
     * @param appVersion App version (optional)
     */
    public static void logDeviceInfo(Logger logger, String platform, String deviceName, String platformVersion, String appVersion) {
        logger.info("DEVICE INFO: Platform={}, Device={}, Version={}, App={}", 
                   platform, deviceName, platformVersion, appVersion != null ? appVersion : "N/A");
        setPlatformContext(platform, deviceName);
    }
    
    /**
     * Log screenshot capture
     * @param logger Logger instance
     * @param screenshotPath Path to screenshot file
     * @param reason Reason for taking screenshot
     */
    public static void logScreenshot(Logger logger, String screenshotPath, String reason) {
        logger.info("SCREENSHOT: {} | Reason: {} | Path: {}", 
                   screenshotPath != null ? "Captured" : "Failed", reason, screenshotPath);
    }
    
    /**
     * Log exception with context
     * @param logger Logger instance
     * @param operation Operation that failed
     * @param exception Exception that occurred
     */
    public static void logException(Logger logger, String operation, Exception exception) {
        logger.error("EXCEPTION in {}: {} | Message: {}", 
                    operation, exception.getClass().getSimpleName(), exception.getMessage());
        logger.debug("Exception stack trace:", exception);
    }
    
    /**
     * Log retry attempt
     * @param logger Logger instance
     * @param operation Operation being retried
     * @param attempt Current attempt number
     * @param maxAttempts Maximum attempts
     * @param reason Reason for retry
     */
    public static void logRetry(Logger logger, String operation, int attempt, int maxAttempts, String reason) {
        logger.warn("RETRY: {} | Attempt {}/{} | Reason: {}", operation, attempt, maxAttempts, reason);
    }
    
    /**
     * Log configuration loading
     * @param logger Logger instance
     * @param configFile Configuration file name
     * @param success Whether loading was successful
     */
    public static void logConfigLoading(Logger logger, String configFile, boolean success) {
        if (success) {
            logger.info("CONFIG: Successfully loaded {}", configFile);
        } else {
            logger.warn("CONFIG: Failed to load {}", configFile);
        }
    }
    
    /**
     * Log driver lifecycle events
     * @param logger Logger instance
     * @param event Event type (CREATE, START, STOP, QUIT)
     * @param platform Platform name
     * @param sessionId Session ID (optional)
     */
    public static void logDriverEvent(Logger logger, String event, String platform, String sessionId) {
        if (sessionId != null && !sessionId.isEmpty()) {
            logger.info("DRIVER {}: Platform={} | SessionId={}", event, platform, sessionId);
            setSessionId(sessionId);
        } else {
            logger.info("DRIVER {}: Platform={}", event, platform);
        }
    }
    
    /**
     * Log test data usage
     * @param logger Logger instance
     * @param dataType Type of test data
     * @param dataSource Source of test data
     * @param recordCount Number of records
     */
    public static void logTestData(Logger logger, String dataType, String dataSource, int recordCount) {
        logger.info("TEST DATA: Type={} | Source={} | Records={}", dataType, dataSource, recordCount);
    }
    
    /**
     * Log environment information
     * @param logger Logger instance
     * @param environment Environment name
     * @param baseUrl Base URL
     * @param additionalInfo Additional environment info
     */
    public static void logEnvironment(Logger logger, String environment, String baseUrl, String additionalInfo) {
        logger.info("ENVIRONMENT: {} | BaseURL={} | Info={}", environment, baseUrl, additionalInfo);
    }
    
    /**
     * Create formatted log message with timestamp
     * @param message Log message
     * @param parameters Message parameters
     * @return Formatted message with timestamp
     */
    public static String formatMessage(String message, Object... parameters) {
        String formattedMessage = parameters.length > 0 ? String.format(message, parameters) : message;
        return String.format("[%s] %s", DATE_FORMAT.format(new Date()), formattedMessage);
    }
    
    /**
     * Log separator for better readability
     * @param logger Logger instance
     * @param title Section title
     */
    public static void logSeparator(Logger logger, String title) {
        String separator = "=".repeat(50);
        logger.info(separator);
        if (title != null && !title.isEmpty()) {
            logger.info("  {}", title);
            logger.info(separator);
        }
    }
    
    /**
     * Log test suite start
     * @param logger Logger instance
     * @param suiteName Test suite name
     * @param testCount Number of tests in suite
     */
    public static void logSuiteStart(Logger logger, String suiteName, int testCount) {
        logSeparator(logger, "TEST SUITE START");
        logger.info("Suite: {} | Tests: {}", suiteName, testCount);
        logger.info("Started at: {}", DATE_FORMAT.format(new Date()));
    }
    
    /**
     * Log test suite end
     * @param logger Logger instance
     * @param suiteName Test suite name
     * @param passed Number of passed tests
     * @param failed Number of failed tests
     * @param skipped Number of skipped tests
     * @param durationMs Suite duration in milliseconds
     */
    public static void logSuiteEnd(Logger logger, String suiteName, int passed, int failed, int skipped, long durationMs) {
        logSeparator(logger, "TEST SUITE END");
        logger.info("Suite: {} | Passed: {} | Failed: {} | Skipped: {}", suiteName, passed, failed, skipped);
        logger.info("Duration: {}ms | Ended at: {}", durationMs, DATE_FORMAT.format(new Date()));
    }
}