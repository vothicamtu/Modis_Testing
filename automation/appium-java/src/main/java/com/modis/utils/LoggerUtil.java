package com.modis.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggerUtil {
    private static final String LOG_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    public static final String TEST_CLASS_KEY = "testClass";
    public static final String TEST_METHOD_KEY = "testMethod";
    public static final String PLATFORM_KEY = "platform";
    public static final String DEVICE_KEY = "device";
    public static final String SESSION_ID_KEY = "sessionId";

    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    public static Logger getLogger(String name) {
        return LoggerFactory.getLogger(name);
    }

    public static void setTestContext(String testClass, String testMethod) {
        MDC.put(TEST_CLASS_KEY, testClass);
        MDC.put(TEST_METHOD_KEY, testMethod);
    }

    public static void setPlatformContext(String platform, String deviceName) {
        MDC.put(PLATFORM_KEY, platform);
        MDC.put(DEVICE_KEY, deviceName);
    }

    public static void setSessionId(String sessionId) {
        MDC.put(SESSION_ID_KEY, sessionId);
    }

    public static void clearTestContext() {
        MDC.remove(TEST_CLASS_KEY);
        MDC.remove(TEST_METHOD_KEY);
    }

    public static void clearPlatformContext() {
        MDC.remove(PLATFORM_KEY);
        MDC.remove(DEVICE_KEY);
    }

    public static void clearSessionId() {
        MDC.remove(SESSION_ID_KEY);
    }

    public static void clearAllContext() {
        MDC.clear();
    }

    public static void logTestStart(Logger logger, String testClass, String testMethod) {
        setTestContext(testClass, testMethod);
        logger.info("=== TEST START: {}.{} ===", testClass, testMethod);
        logger.info("Test started at: {}", DATE_FORMAT.format(new Date()));
    }

    public static void logTestEnd(Logger logger, String testClass, String testMethod, String result, long durationMs) {
        logger.info("=== TEST END: {}.{} - {} ===", testClass, testMethod, result);
        logger.info("Test duration: {}ms", durationMs);
        logger.info("Test ended at: {}", DATE_FORMAT.format(new Date()));
        clearTestContext();
    }

    public static void logStep(Logger logger, String stepName, String description) {
        logger.info("STEP: {} - {}", stepName, description);
    }

    public static void logStep(Logger logger, String stepName, String description, Object... parameters) {
        logger.info("STEP: {} - {}", stepName, String.format(description, parameters));
    }

    public static void logAssertion(Logger logger, String assertion, Object expected, Object actual, boolean passed) {
        if (passed) {
            logger.info("ASSERTION PASSED: {} | Expected: {} | Actual: {}", assertion, expected, actual);
        } else {
            logger.error("ASSERTION FAILED: {} | Expected: {} | Actual: {}", assertion, expected, actual);
        }
    }

    public static void logElementInteraction(Logger logger, String action, String element, String value) {
        if (value != null && !value.isEmpty()) {
            logger.info("ELEMENT ACTION: {} on '{}' with value '{}'", action, element, value);
        } else {
            logger.info("ELEMENT ACTION: {} on '{}'", action, element);
        }
    }

    public static void logNavigation(Logger logger, String from, String to) {
        logger.info("NAVIGATION: {} -> {}", from, to);
    }

    public static void logApiCall(Logger logger, String method, String url, int statusCode, long responseTime) {
        logger.info("API CALL: {} {} | Status: {} | Time: {}ms", method, url, statusCode, responseTime);
    }

    public static void logPerformance(Logger logger, String operation, long durationMs) {
        logger.info("PERFORMANCE: {} completed in {}ms", operation, durationMs);
    }

    public static void logDeviceInfo(Logger logger, String platform, String deviceName, String platformVersion, String appVersion) {
        logger.info("DEVICE INFO: Platform={}, Device={}, Version={}, App={}",
                platform, deviceName, platformVersion, appVersion != null ? appVersion : "N/A");
        setPlatformContext(platform, deviceName);
    }

    public static void logScreenshot(Logger logger, String screenshotPath, String reason) {
        logger.info("SCREENSHOT: {} | Reason: {} | Path: {}",
                screenshotPath != null ? "Captured" : "Failed", reason, screenshotPath);
    }

    public static void logException(Logger logger, String operation, Exception exception) {
        logger.error("EXCEPTION in {}: {} | Message: {}",
                operation, exception.getClass().getSimpleName(), exception.getMessage());
        logger.debug("Exception stack trace:", exception);
    }

    public static void logRetry(Logger logger, String operation, int attempt, int maxAttempts, String reason) {
        logger.warn("RETRY: {} | Attempt {}/{} | Reason: {}", operation, attempt, maxAttempts, reason);
    }

    public static void logConfigLoading(Logger logger, String configFile, boolean success) {
        if (success) {
            logger.info("CONFIG: Successfully loaded {}", configFile);
        } else {
            logger.warn("CONFIG: Failed to load {}", configFile);
        }
    }

    public static void logDriverEvent(Logger logger, String event, String platform, String sessionId) {
        if (sessionId != null && !sessionId.isEmpty()) {
            logger.info("DRIVER {}: Platform={} | SessionId={}", event, platform, sessionId);
            setSessionId(sessionId);
        } else {
            logger.info("DRIVER {}: Platform={}", event, platform);
        }
    }

    public static void logTestData(Logger logger, String dataType, String dataSource, int recordCount) {
        logger.info("TEST DATA: Type={} | Source={} | Records={}", dataType, dataSource, recordCount);
    }

    public static void logEnvironment(Logger logger, String environment, String baseUrl, String additionalInfo) {
        logger.info("ENVIRONMENT: {} | BaseURL={} | Info={}", environment, baseUrl, additionalInfo);
    }

    public static String formatMessage(String message, Object... parameters) {
        String formattedMessage = parameters.length > 0 ? String.format(message, parameters) : message;
        return String.format("[%s] %s", DATE_FORMAT.format(new Date()), formattedMessage);
    }

    public static void logSeparator(Logger logger, String title) {
        String separator = "=".repeat(50);
        logger.info(separator);
        if (title != null && !title.isEmpty()) {
            logger.info("  {}", title);
            logger.info(separator);
        }
    }

    public static void logSuiteStart(Logger logger, String suiteName, int testCount) {
        logSeparator(logger, "TEST SUITE START");
        logger.info("Suite: {} | Tests: {}", suiteName, testCount);
        logger.info("Started at: {}", DATE_FORMAT.format(new Date()));
    }

    public static void logSuiteEnd(Logger logger, String suiteName, int passed, int failed, int skipped, long durationMs) {
        logSeparator(logger, "TEST SUITE END");
        logger.info("Suite: {} | Passed: {} | Failed: {} | Skipped: {}", suiteName, passed, failed, skipped);
        logger.info("Duration: {}ms | Ended at: {}", durationMs, DATE_FORMAT.format(new Date()));
    }
}