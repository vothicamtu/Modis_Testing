package com.modis.drivers;

import com.modis.constants.AppConstants;
import com.modis.utils.ConfigReader;
import com.modis.utils.LoggerUtil;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.HasSettings;
import io.appium.java_client.Setting;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Driver Manager for Appium WebDriver instances
 * Handles driver creation, configuration, and lifecycle management
 */
public class DriverManager {

    private static final Logger logger = LoggerUtil.getLogger(DriverManager.class);
    private static final ThreadLocal<AppiumDriver> driver = new ThreadLocal<>();

    /**
     * Get the current driver instance for the thread
     * @return AppiumDriver instance
     */
    public static AppiumDriver getDriver() {
        return driver.get();
    }

    /**
     * Set driver instance for the current thread
     * @param appiumDriver The driver instance to set
     */
    public static void setDriver(AppiumDriver appiumDriver) {
        driver.set(appiumDriver);
    }

    /**
     * Create and initialize Appium driver based on platform
     * @param platform The platform (android/ios)
     * @return AppiumDriver instance
     */
    public static AppiumDriver createDriver(String platform) {
        logger.info("Creating driver for platform: {}", platform);

        AppiumDriver appiumDriver;

        try {
            List<URL> serverUrls = getAppiumServerURLs();

            switch (platform.toLowerCase()) {
                case "android": {
                    UiAutomator2Options androidOptions = getAndroidOptions();
                    appiumDriver = createWithUrlFallbackAndroid(serverUrls, androidOptions);
                    break;
                }
                case "ios": {
                    XCUITestOptions iosOptions = getIOSOptions();
                    appiumDriver = createWithUrlFallbackIOS(serverUrls, iosOptions);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unsupported platform: " + platform);
            }

            configureDriver(appiumDriver);
            setDriver(appiumDriver);

            logger.info("Driver created successfully for platform: {}", platform);
            return appiumDriver;

        } catch (Exception e) {
            logger.error("Failed to create driver for platform: {}", platform, e);
            throw new RuntimeException("Driver creation failed", e);
        }
    }

    private static AppiumDriver createWithUrlFallbackAndroid(List<URL> urls, UiAutomator2Options options) {
        Exception last = null;
        boolean retriedWithNoReset = false;
        for (URL url : urls) {
            try {
                logger.info("Attempting to create AndroidDriver at: {}", url);
                return new AndroidDriver(url, options);
            } catch (Exception e) {
                last = e;
                if (!retriedWithNoReset && isPmClearPermissionError(e)) {
                    retriedWithNoReset = true;
                    try {
                        logger.warn("Detected pm clear permission failure; retrying AndroidDriver creation with noReset=true");
                        options.setNoReset(true);
                        options.setFullReset(false);
                        options.setCapability("fastReset", false);
                        return new AndroidDriver(url, options);
                    } catch (Exception retryException) {
                        last = retryException;
                        logger.warn("Retry with noReset=true failed at {}: {}: {}", url, retryException.getClass().getSimpleName(), retryException.getMessage());
                    }
                }
                logger.warn("Failed to create AndroidDriver at {}: {}: {}", url, e.getClass().getSimpleName(), e.getMessage());
            }
        }
        logger.error("ANDROID SESSION CREATION FAILED. Tried URLs: {}", urls);
        throw new RuntimeException("AndroidDriver session creation failed. See root cause in suppressed exception/logs.", last);
    }

    private static boolean isPmClearPermissionError(Exception e) {
        String msg = String.valueOf(e.getMessage());
        return msg.contains("pm clear") && msg.contains("CLEAR_APP_USER_DATA");
    }

    private static AppiumDriver createWithUrlFallbackIOS(List<URL> urls, XCUITestOptions options) {
        Exception last = null;
        for (URL url : urls) {
            try {
                logger.info("Attempting to create IOSDriver at: {}", url);
                return new IOSDriver(url, options);
            } catch (Exception e) {
                last = e;
                logger.warn("Failed to create IOSDriver at {}: {}", url, e.getMessage());
            }
        }
        logger.error("APPIUM SERVER CONNECTION FAILED (iOS). Tried URLs: {}", urls);
        throw new RuntimeException("Appium server not accessible for iOS. Please verify serverUrl/serverPath.", last);
    }

    /**
     * Get Android options with UiAutomator2 stability improvements
     * @return UiAutomator2Options for Android
     */
    private static UiAutomator2Options getAndroidOptions() {
        UiAutomator2Options options = new UiAutomator2Options();

        // Platform capabilities - REMOVED hardcoded platformVersion
        options.setPlatformName("Android");
        options.setAutomationName("UiAutomator2");
        options.setDeviceName(ConfigReader.getProperty("android.deviceName", AppConstants.DEFAULT_DEVICE_NAME));
        
        // FIXED: Only set platformVersion if explicitly provided in config
        String platformVersion = ConfigReader.getProperty("android.platformVersion", "").trim();
        if (!platformVersion.isEmpty()) {
            options.setPlatformVersion(platformVersion);
            logger.info("Using configured platformVersion: {}", platformVersion);
        } else {
            logger.info("No platformVersion specified - letting Appium auto-detect");
        }
        
        String udid = ConfigReader.getProperty("android.udid", "").trim();
        if (!udid.isEmpty()) {
            options.setUdid(udid);
        }

        // App capabilities
        String appPath = ConfigReader.getProperty("android.appPath");
        if (appPath != null && !appPath.isEmpty()) {
            options.setApp(appPath);
        } else {
            options.setAppPackage(ConfigReader.getProperty("android.appPackage", AppConstants.APP_PACKAGE));
            options.setAppActivity(ConfigReader.getProperty("android.appActivity", AppConstants.APP_ACTIVITY));
        }

        // FIXED: App reset strategy for stability
        // noReset=true prevents SecurityException on Android 11+ real devices
        boolean noReset = ConfigReader.getBooleanProperty("android.noReset", true);
        boolean fullReset = ConfigReader.getBooleanProperty("android.fullReset", false);
        
        logger.info("Setting app reset capabilities: noReset={}, fullReset={}", noReset, fullReset);
        options.setNoReset(noReset);
        options.setFullReset(fullReset);
        options.setCapability("fastReset", false);

        // Set other capabilities via generic setCapability to ensure maximum compatibility
        options.setCapability("autoGrantPermissions", ConfigReader.getBooleanProperty("android.autoGrantPermissions", true));
        options.setCapability("autoAcceptAlerts", ConfigReader.getBooleanProperty("android.autoAcceptAlerts", true));
        options.setCapability("autoDismissAlerts", ConfigReader.getBooleanProperty("android.autoDismissAlerts", true));

        // Network and performance
        options.setCapability("networkSpeed", ConfigReader.getProperty("android.networkSpeed", "full"));
        options.setCapability("gpsEnabled", ConfigReader.getBooleanProperty("android.gpsEnabled", true));

        // Timeouts - Optimized for stability
        options.setNewCommandTimeout(Duration.ofSeconds(ConfigReader.getIntProperty("android.newCommandTimeout", 300))); // 5 minutes
        options.setCapability("androidInstallTimeout", ConfigReader.getIntProperty("android.androidInstallTimeout", 90000));

        // UiAutomator2 specific - Enhanced for stability
        options.setCapability("uiautomator2ServerLaunchTimeout", ConfigReader.getIntProperty("android.uiautomator2ServerLaunchTimeout", 60000)); // 1 minute
        options.setCapability("uiautomator2ServerInstallTimeout", ConfigReader.getIntProperty("android.uiautomator2ServerInstallTimeout", 60000)); // 1 minute
        options.setCapability("uiautomator2ServerReadTimeout", ConfigReader.getIntProperty("android.uiautomator2ServerReadTimeout", 15000));
        options.setCapability("adbExecTimeout", ConfigReader.getIntProperty("android.adbExecTimeout", 60000));

        // Element finding timeouts - Optimized for React Native
        options.setCapability("waitForIdleTimeout", 0); // Critical: disable idle wait for RN
        options.setCapability("waitForSelectorTimeout", ConfigReader.getIntProperty("android.waitForSelectorTimeout", 3000)); // Reduced to 3s
        options.setCapability("actionAcknowledgmentTimeout", ConfigReader.getIntProperty("android.actionAcknowledgmentTimeout", 3000)); // Reduced to 3s
        options.setCapability("scrollAcknowledgmentTimeout", ConfigReader.getIntProperty("android.scrollAcknowledgmentTimeout", 500));

        // Additional Android capabilities
        options.setCapability("skipUnlock", ConfigReader.getBooleanProperty("android.skipUnlock", true));
        options.setCapability("unlockType", ConfigReader.getProperty("android.unlockType", "pin"));
        options.setCapability("unlockKey", ConfigReader.getProperty("android.unlockKey", "1234"));

        // FIXED: UiAutomator2 stability improvements for debugging
        // Turn OFF ignoreUnimportantViews when debugging accessibility issues
        boolean debugMode = ConfigReader.getBooleanProperty("android.debugMode", false);
        options.setCapability("ignoreUnimportantViews", !debugMode); // false when debugging
        options.setCapability("disableAndroidWatchers", true);
        options.setCapability("skipServerInstallation", false);

        // Optional: disable Android window animations to reduce RN transition flakiness
        options.setCapability("disableWindowAnimation",
                ConfigReader.getBooleanProperty("android.disableWindowAnimation", true));

        // Logging
        options.setCapability("enablePerformanceLogging", ConfigReader.getBooleanProperty("android.enablePerformanceLogging", false));

        logger.info("Android options configured with stability improvements: debugMode={}, ignoreUnimportantViews={}", 
                   debugMode, !debugMode);
        return options;
    }

    /**
     * Get iOS options
     * @return XCUITestOptions for iOS
     */
    private static XCUITestOptions getIOSOptions() {
        XCUITestOptions options = new XCUITestOptions();

        // Platform capabilities
        options.setPlatformName("iOS");
        options.setAutomationName("XCUITest");
        options.setDeviceName(ConfigReader.getProperty("ios.deviceName", "iPhone Simulator"));
        options.setPlatformVersion(ConfigReader.getProperty("ios.platformVersion", "15.0"));

        // App capabilities
        String appPath = ConfigReader.getProperty("ios.appPath");
        if (appPath != null && !appPath.isEmpty()) {
            options.setApp(appPath);
        } else {
            options.setBundleId(ConfigReader.getProperty("ios.bundleId", AppConstants.APP_PACKAGE));
        }

        // Performance and behavior capabilities
        options.setNoReset(ConfigReader.getBooleanProperty("ios.noReset", false));
        options.setFullReset(ConfigReader.getBooleanProperty("ios.fullReset", false));

        options.setCapability("autoAcceptAlerts", ConfigReader.getBooleanProperty("ios.autoAcceptAlerts", true));
        options.setCapability("autoDismissAlerts", ConfigReader.getBooleanProperty("ios.autoDismissAlerts", true));

        // XCUITest specific
        options.setCapability("wdaLaunchTimeout", ConfigReader.getIntProperty("ios.wdaLaunchTimeout", 60000));
        options.setCapability("wdaConnectionTimeout", ConfigReader.getIntProperty("ios.wdaConnectionTimeout", 60000));

        // Timeouts
        options.setNewCommandTimeout(Duration.ofSeconds(ConfigReader.getIntProperty("ios.newCommandTimeout", 300)));

        logger.info("iOS options configured: {}", options.asMap());
        return options;
    }

    /**
     * Configure driver with timeouts and settings
     * @param appiumDriver The driver to configure
     */
    private static void configureDriver(AppiumDriver appiumDriver) {
        // BEST PRACTICE: implicit wait should be 0 to avoid compounding with explicit waits
        appiumDriver.manage().timeouts().implicitlyWait(Duration.ZERO);
        logger.info("Driver configured - implicitWait=0 (explicit waits are used for stability)");

        // Enforce critical UiAutomator2 settings at runtime as well (in case the server ignored caps).
        // This is especially important for RN apps with continuous animations.
        if (appiumDriver instanceof AndroidDriver) {
            try {
                HasSettings settings = (HasSettings) appiumDriver;
                settings.setSetting(Setting.WAIT_FOR_IDLE_TIMEOUT, 0);
                boolean debugMode = ConfigReader.getBooleanProperty("android.debugMode", false);
                boolean ignoreUnimportantViews = ConfigReader.getBooleanProperty("android.ignoreUnimportantViews", !debugMode);
                settings.setSetting(Setting.IGNORE_UNIMPORTANT_VIEWS, ignoreUnimportantViews);
                logger.info("Applied Android UiAutomator2 settings: waitForIdleTimeout=0, ignoreUnimportantViews={}", ignoreUnimportantViews);
            } catch (Exception e) {
                logger.warn("Could not apply Android UiAutomator2 settings at runtime (non-fatal): {}", e.getMessage());
            }
        }
    }

    /**
     * Generate a robust list of Appium server URLs to try.
     * Appium 2/3 use the root ("/") endpoint and MUST NOT use "/wd/hub".
     *
     * Misconfiguration prevention:
     * - If serverUrl contains a non-root path (e.g. "/wd/hub"), fail fast with a clear message.
     * - appium.serverPath is deprecated (legacy Appium 1.x). It is ignored, and "/wd/hub" will be rejected.
     */
    private static List<URL> getAppiumServerURLs() {
        try {
            // Support specifying multiple endpoints explicitly (recommended for CI / multiple hosts).
            // Example: appium.serverUrls=http://127.0.0.1:4723,http://localhost:4723
            String rawList = ConfigReader.getProperty("appium.serverUrls", "").trim();
            String baseUrl = ConfigReader.getProperty("appium.serverUrl", "http://127.0.0.1:4723").trim();

            // Legacy property (Appium 1.x). We keep it only to detect & block misconfiguration.
            String serverPath = ConfigReader.getProperty("appium.serverPath", "").trim();
            if (!serverPath.isEmpty()) {
                String normalized = serverPath.startsWith("/") ? serverPath : "/" + serverPath;
                if ("/wd/hub".equalsIgnoreCase(normalized) || "/wd/hub/".equalsIgnoreCase(normalized)) {
                    throw new IllegalArgumentException(
                            "Invalid Appium serverPath '/wd/hub' detected. Appium 2/3 MUST NOT use /wd/hub. " +
                                    "Fix: set appium.serverPath empty and set appium.serverUrl like 'http://127.0.0.1:4723'.");
                }
                logger.warn("Ignoring deprecated property appium.serverPath='{}' (Appium 2/3 use root endpoint '/')", serverPath);
            }

            LinkedHashSet<String> candidates = new LinkedHashSet<>();

            // 1) User-provided list or single URL
            if (!rawList.isEmpty()) {
                for (String part : rawList.split(",")) {
                    String s = part.trim();
                    if (!s.isEmpty()) {
                        candidates.add(normalizeAndValidateAppiumRootUrl(s));
                    }
                }
            } else if (!baseUrl.isEmpty()) {
                candidates.add(normalizeAndValidateAppiumRootUrl(baseUrl));
            }

            // 2) Safe fallbacks (root endpoint only)
            candidates.add("http://127.0.0.1:4723");
            candidates.add("http://localhost:4723");

            List<URL> urls = new ArrayList<>();
            for (String s : candidates) {
                try {
                    urls.add(new URL(s));
                } catch (MalformedURLException ignored) {
                    logger.warn("Skipping invalid Appium URL candidate: {}", s);
                }
            }

            logger.info("Appium server URL candidates (Appium 2/3 root endpoint only): {}", urls);
            return urls;
        } catch (Exception e) {
            logger.error("Failed to build Appium server URL candidates", e);
            throw new RuntimeException("Failed to build Appium server URL candidates", e);
        }
    }

    /**
     * Normalize serverUrl and enforce Appium 2/3 root endpoint:
     * - Reject "/wd/hub" and any non-root path (Appium 3 returns 404 for /wd/hub).
     * - Normalize to "scheme://host:port" (no trailing slash).
     */
    private static String normalizeAndValidateAppiumRootUrl(String rawUrl) throws MalformedURLException {
        String trimmed = rawUrl.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Appium serverUrl is empty");
        }

        URL u = new URL(trimmed);
        String path = u.getPath() == null ? "" : u.getPath().trim();

        // Accept only "" or "/" as path. Anything else is a misconfig for Appium 2/3 server URL.
        if (!path.isEmpty() && !"/".equals(path)) {
            throw new IllegalArgumentException(
                    "Invalid Appium serverUrl path '" + path + "' in '" + rawUrl + "'. " +
                            "Appium 2/3 MUST use the root endpoint (e.g. 'http://127.0.0.1:4723'). " +
                            "Remove '/wd/hub' or any extra path.");
        }

        // Normalize: no trailing slash, no path.
        String portPart = u.getPort() > 0 ? ":" + u.getPort() : "";
        return u.getProtocol() + "://" + u.getHost() + portPart;
    }

    /**
     * Quit the current driver and remove from ThreadLocal
     */
    public static void quitDriver() {
        AppiumDriver currentDriver = getDriver();
        if (currentDriver != null) {
            try {
                logger.info("Quitting driver");
                currentDriver.quit();
            } catch (Exception e) {
                logger.warn("Error while quitting driver", e);
            } finally {
                driver.remove();
                logger.info("Driver removed from ThreadLocal");
            }
        }
    }

    /**
     * Check if driver is initialized and UiAutomator2 is healthy
     * @return true if driver exists and UiAutomator2 is responsive, false otherwise
     */
    public static boolean isDriverInitialized() {
        AppiumDriver currentDriver = getDriver();
        if (currentDriver == null) {
            return false;
        }

        // Check UiAutomator2 health
        return isUiAutomator2Healthy();
    }

    /**
     * Ultra-light session check (no extra Appium calls).
     * Useful to avoid doing heavy health checks too frequently.
     */
    public static boolean isSessionAlive() {
        AppiumDriver currentDriver = getDriver();
        try {
            return currentDriver != null && currentDriver.getSessionId() != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check UiAutomator2 server health
     * @return true if UiAutomator2 is responsive, false if crashed
     */
    public static boolean isUiAutomator2Healthy() {
        AppiumDriver currentDriver = getDriver();
        if (currentDriver == null) {
            return false;
        }

        try {
            // Simple health check - try to get current package
            if (currentDriver instanceof AndroidDriver) {
                AndroidDriver androidDriver = (AndroidDriver) currentDriver;
                String currentPackage = androidDriver.getCurrentPackage();
                logger.debug("UiAutomator2 health check passed - current package: {}", currentPackage);
                return currentPackage != null;
            }
            return true;
        } catch (Exception e) {
            logger.warn("UiAutomator2 health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Recover from UiAutomator2 crash by recreating session
     * @return true if recovery successful, false otherwise
     */

    public static boolean recoverFromUiAutomator2Crash() {
        logger.warn(" Attempting to recover from UiAutomator2 crash...");

        try {
            AppiumDriver currentDriver = getDriver();

            // Step 1: Validate we actually have a driver to recover from
            if (currentDriver == null) {
                logger.warn("No driver to recover from - creating new driver");
                String platform = System.getProperty("platform", "android");
                AppiumDriver newDriver = createDriver(platform);
                return newDriver != null && isUiAutomator2Healthy();
            }

            // Step 2: Attempt graceful shutdown
            try {
                logger.info("Attempting graceful driver shutdown...");
                currentDriver.quit();
                logger.info("✓ Driver quit successfully");
            } catch (Exception quitEx) {
                logger.debug("Quit failed (expected if crashed): {}", quitEx.getMessage());
            }

            // Step 3: Store platform before cleanup
            String platform = "android";
            try {
                Object platformCap = currentDriver.getCapabilities().getCapability("platformName");
                if (platformCap != null) {
                    platform = platformCap.toString().toLowerCase();
                }
            } catch (Exception e) {
                logger.debug("Could not get platform capability, using default: android");
            }

            // IMPROVED: Longer cleanup wait (from 3s to 8s for thorough cleanup)
            long cleanupWaitMs = ConfigReader.getLongProperty("android.recovery.cleanupWaitMs", AppConstants.UIAUTOMATOR2_RECOVERY_WAIT_MS);
            logger.info("Waiting {}ms for system cleanup and resource release...", cleanupWaitMs);
            Thread.sleep(cleanupWaitMs);

            // Step 4: Clear ThreadLocal reference
            driver.remove();
            logger.info("✓ Cleared ThreadLocal driver reference");

            // Step 5: Create new driver with retry logic
            logger.info("Creating new driver for platform: {}", platform);
            int retries = AppConstants.UIAUTOMATOR2_RECOVERY_MAX_ATTEMPTS;

            for (int i = 1; i <= retries; i++) {
                try {
                    logger.info("Driver creation attempt {}/{}", i, retries);
                    AppiumDriver newDriver = createDriver(platform);

                    if (newDriver != null) {
                        // Validate the new driver is healthy
                        if (isUiAutomator2Healthy()) {
                            logger.info(" Successfully recovered from UiAutomator2 crash!");
                            logger.info("✓ New driver is healthy and responsive");
                            return true;
                        } else {
                            logger.warn("New driver created but UiAutomator2 health check failed");
                            try {
                                newDriver.quit();
                            } catch (Exception e) {
                                logger.debug("Error quitting unhealthy driver: {}", e.getMessage());
                            }
                        }
                    }
                } catch (Exception createEx) {
                    logger.warn("Driver creation attempt {} failed: {}", i, createEx.getMessage());

                    if (i < retries) {
                        // Exponential backoff: 5s, 10s, 15s
                        long waitTime = 5000L * i;
                        logger.info("Waiting {}ms before retry attempt {}...", waitTime, i + 1);
                        Thread.sleep(waitTime);
                    }
                }
            }

            logger.error(" Failed to recover from UiAutomator2 crash after {} retry attempts", retries);
            return false;

        } catch (Exception e) {
            logger.error("Unexpected exception during UiAutomator2 crash recovery", e);
            return false;
        }
    }
    /**
     * Safe element finding with UiAutomator2 crash protection
     * @param locator Element locator
     * @return WebElement or null if not found or UiAutomator2 crashed
     */
    public static WebElement safelyFindElement(By locator) {
        AppiumDriver currentDriver = getDriver();
        if (currentDriver == null) {
            logger.error("No driver available for safe element finding");
            return null;
        }

        try {
            // Check UiAutomator2 health before finding element
            if (!isUiAutomator2Healthy()) {
                logger.warn("UiAutomator2 not healthy, attempting recovery...");
                if (!recoverFromUiAutomator2Crash()) {
                    logger.error("Failed to recover UiAutomator2, cannot find element");
                    return null;
                }
                currentDriver = getDriver(); // Get new driver after recovery
            }

            return currentDriver.findElement(locator);

        } catch (NoSuchElementException e) {
            return null;
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "";

            // Treat transport/proxy errors as UiAutomator2 crash/unresponsive.
            // Common symptoms:
            // - "instrumentation process is not running"
            // - "Could not proxy command to the remote server"
            // - "socket hang up"
            // - "timeout of 30000ms exceeded"
            boolean likelyUiA2Crash =
                    errorMessage.contains("instrumentation process is not running") ||
                            errorMessage.contains("Could not proxy command") ||
                            errorMessage.contains("socket hang up") ||
                            errorMessage.contains("timeout of") ||
                            errorMessage.contains("ECONNRESET") ||
                            errorMessage.contains("ECONNREFUSED");

            if (likelyUiA2Crash) {
                logger.error("UiAutomator2 likely crashed/unresponsive during element finding: {} | error: {}", locator, errorMessage);

                // Attempt recovery
                if (recoverFromUiAutomator2Crash()) {
                    logger.info("Retrying element finding after UiAutomator2 recovery...");
                    try {
                        currentDriver = getDriver();
                        return currentDriver.findElement(locator);
                    } catch (Exception retryException) {
                        logger.error("Element finding failed even after UiAutomator2 recovery", retryException);
                        return null;
                    }
                } else {
                    logger.error("Could not recover from UiAutomator2 crash");
                    return null;
                }
            } else {
                logger.debug("Element finding failed: {} ({})", locator, errorMessage);
                return null;
            }
        }
    }

    public static List<WebElement> safelyFindElements(By locator) {
        AppiumDriver currentDriver = getDriver();
        if (currentDriver == null) {
            logger.error("No driver available for safe elements finding");
            return java.util.Collections.emptyList();
        }

        try {
            if (!isUiAutomator2Healthy()) {
                logger.warn("UiAutomator2 not healthy, attempting recovery...");
                if (!recoverFromUiAutomator2Crash()) {
                    logger.error("Failed to recover UiAutomator2, cannot find elements");
                    return java.util.Collections.emptyList();
                }
                currentDriver = getDriver();
            }

            List<WebElement> elements = currentDriver.findElements(locator);
            return elements != null ? elements : java.util.Collections.emptyList();

        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "";

            boolean likelyUiA2Crash =
                    errorMessage.contains("instrumentation process is not running") ||
                            errorMessage.contains("Could not proxy command") ||
                            errorMessage.contains("socket hang up") ||
                            errorMessage.contains("timeout of") ||
                            errorMessage.contains("ECONNRESET") ||
                            errorMessage.contains("ECONNREFUSED");

            if (likelyUiA2Crash) {
                logger.error("UiAutomator2 likely crashed/unresponsive during elements finding: {} | error: {}", locator, errorMessage);
                if (recoverFromUiAutomator2Crash()) {
                    try {
                        currentDriver = getDriver();
                        List<WebElement> elements = currentDriver.findElements(locator);
                        return elements != null ? elements : java.util.Collections.emptyList();
                    } catch (Exception retryException) {
                        logger.error("Elements finding failed even after UiAutomator2 recovery", retryException);
                        return java.util.Collections.emptyList();
                    }
                }
            }

            logger.debug("Elements finding failed: {} ({})", locator, errorMessage);
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Get current platform name
     * @return Platform name (Android/iOS)
     */
    public static String getCurrentPlatform() {
        AppiumDriver currentDriver = getDriver();
        if (currentDriver != null) {
            return currentDriver.getCapabilities().getCapability("platformName").toString();
        }
        return "Unknown";
    }

    /**
     * Get current device name
     * @return Device name
     */
    public static String getCurrentDeviceName() {
        AppiumDriver currentDriver = getDriver();
        if (currentDriver != null) {
            return currentDriver.getCapabilities().getCapability("deviceName").toString();
        }
        return "Unknown";
    }

    /**
     * Restart the app
     */
    public static void restartApp() {
        AppiumDriver currentDriver = getDriver();
        if (currentDriver != null) {
            logger.info("Restarting app");
            try {
                if (currentDriver instanceof AndroidDriver) {
                    currentDriver.executeScript("mobile: terminateApp", java.util.Map.of("appId", AppConstants.APP_PACKAGE));
                    Thread.sleep(2000); // Wait for app to fully terminate
                    currentDriver.executeScript("mobile: activateApp", java.util.Map.of("appId", AppConstants.APP_PACKAGE));
                } else if (currentDriver instanceof IOSDriver) {
                    currentDriver.executeScript("mobile: terminateApp", java.util.Map.of("bundleId", AppConstants.APP_BUNDLE_ID));
                    Thread.sleep(2000); // Wait for app to fully terminate
                    currentDriver.executeScript("mobile: activateApp", java.util.Map.of("bundleId", AppConstants.APP_BUNDLE_ID));
                }
                logger.info("App restarted successfully");
            } catch (Exception e) {
                logger.warn("Failed to restart app", e);
            }
        }
    }

    /**
     * Clear app data and restart (for clean state)
     */
    public static void clearAppDataAndRestart() {
        AppiumDriver currentDriver = getDriver();
        if (currentDriver != null) {
            logger.info("Clearing app data and restarting");
            try {
                if (currentDriver instanceof AndroidDriver) {
                    // Terminate app first
                    currentDriver.executeScript("mobile: terminateApp", java.util.Map.of("appId", AppConstants.APP_PACKAGE));
                    Thread.sleep(1000);
                    
                    // Clear app data
                    currentDriver.executeScript("mobile: clearApp", java.util.Map.of("appId", AppConstants.APP_PACKAGE));
                    Thread.sleep(2000); // Wait for clear to complete
                    
                    // Restart app
                    currentDriver.executeScript("mobile: activateApp", java.util.Map.of("appId", AppConstants.APP_PACKAGE));
                    logger.info("App data cleared and app restarted successfully");
                } else {
                    // For iOS, just restart (no clearApp equivalent)
                    restartApp();
                }
            } catch (Exception e) {
                logger.warn("Failed to clear app data and restart: {}", e.getMessage());
                // Fallback to simple restart
                restartApp();
            }
        }
    }

    /**
     * Check if app is stuck on splash/loading screen
     */
    public static boolean isAppStuckOnSplash() {
        AppiumDriver currentDriver = getDriver();
        if (currentDriver == null) {
            return false;
        }

        try {
            if (currentDriver instanceof AndroidDriver) {
                AndroidDriver androidDriver = (AndroidDriver) currentDriver;
                String currentActivity = androidDriver.getCurrentPackage();
                String currentActivityName = androidDriver.currentActivity();
                
                // Check if stuck on splash activity or no activity detected
                boolean stuckOnSplash = currentActivity == null || 
                                      currentActivityName == null ||
                                      currentActivityName.contains("SplashActivity") ||
                                      currentActivityName.contains("LaunchActivity");
                
                if (stuckOnSplash) {
                    logger.warn("App appears stuck on splash screen - activity: {}, package: {}", 
                              currentActivityName, currentActivity);
                }
                
                return stuckOnSplash;
            }
            return false;
        } catch (Exception e) {
            logger.debug("Could not check splash screen status: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Recover from app stuck on splash screen
     */
    public static boolean recoverFromSplashScreen() {
        logger.warn("Attempting to recover from splash screen...");
        
        try {
            // Try clearing app data and restarting
            clearAppDataAndRestart();
            
            // Wait for app to load
            Thread.sleep(5000);
            
            // Check if recovery was successful
            if (!isAppStuckOnSplash()) {
                logger.info("Successfully recovered from splash screen");
                return true;
            }
            
            // If still stuck, try force restart
            logger.warn("Still stuck after clear data, trying force restart...");
            restartApp();
            Thread.sleep(5000);
            
            return !isAppStuckOnSplash();
            
        } catch (Exception e) {
            logger.error("Failed to recover from splash screen", e);
            return false;
        }
    }

    /**
     * Put app in background for specified duration
     * @param duration Duration in seconds
     */
    public static void backgroundApp(int duration) {
        AppiumDriver currentDriver = getDriver();
        if (currentDriver != null) {
            logger.info("Putting app in background for {} seconds", duration);
            try {
                if (currentDriver instanceof AndroidDriver) {
                    currentDriver.executeScript("mobile: backgroundApp", java.util.Map.of("seconds", duration));
                } else if (currentDriver instanceof IOSDriver) {
                    currentDriver.executeScript("mobile: backgroundApp", java.util.Map.of("seconds", duration));
                }
                logger.info("App brought back to foreground");
            } catch (Exception e) {
                logger.warn("Failed to background app", e);
            }
        }
    }

    /**
     * Reset app to initial state
     */
    public static void resetApp() {
        AppiumDriver currentDriver = getDriver();
        if (currentDriver != null) {
            logger.info("Resetting app");
            try {
                if (currentDriver instanceof AndroidDriver) {
                    currentDriver.executeScript("mobile: terminateApp", java.util.Map.of("appId", AppConstants.APP_PACKAGE));
                    currentDriver.executeScript("mobile: clearApp", java.util.Map.of("appId", AppConstants.APP_PACKAGE));
                    currentDriver.executeScript("mobile: activateApp", java.util.Map.of("appId", AppConstants.APP_PACKAGE));
                } else if (currentDriver instanceof IOSDriver) {
                    currentDriver.executeScript("mobile: terminateApp", java.util.Map.of("bundleId", AppConstants.APP_BUNDLE_ID));
                    currentDriver.executeScript("mobile: clearApp", java.util.Map.of("bundleId", AppConstants.APP_BUNDLE_ID));
                    currentDriver.executeScript("mobile: activateApp", java.util.Map.of("bundleId", AppConstants.APP_BUNDLE_ID));
                }
                logger.info("App reset completed");
            } catch (Exception e) {
                logger.warn("Failed to reset app", e);
            }
        }
    }
}
