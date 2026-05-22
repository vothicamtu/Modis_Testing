package com.modis.drivers;

import com.modis.constants.AppConstants;
import com.modis.utils.ConfigReader;
import com.modis.utils.LoggerUtil;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import org.openqa.selenium.By;
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
        for (URL url : urls) {
            try {
                logger.info("Attempting to create AndroidDriver at: {}", url);
                return new AndroidDriver(url, options);
            } catch (Exception e) {
                last = e;
                logger.warn("Failed to create AndroidDriver at {}: {}", url, e.getMessage());
            }
        }
        logger.error("APPIUM SERVER CONNECTION FAILED (Android). Tried URLs: {}", urls);
        throw new RuntimeException("Appium server not accessible for Android. Please verify serverUrl/serverPath.", last);
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
        
        // Platform capabilities
        options.setPlatformName("Android");
        options.setAutomationName("UiAutomator2");
        options.setDeviceName(ConfigReader.getProperty("android.deviceName", AppConstants.DEFAULT_DEVICE_NAME));
        String platformVersion = ConfigReader.getProperty("android.platformVersion", "").trim();
        if (!platformVersion.isEmpty()) {
            options.setPlatformVersion(platformVersion);
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
        
        // Performance and behavior capabilities
        options.setNoReset(ConfigReader.getBooleanProperty("android.noReset", false));
        options.setFullReset(ConfigReader.getBooleanProperty("android.fullReset", false));
        
        // Set other capabilities via generic setCapability to ensure maximum compatibility
        options.setCapability("autoGrantPermissions", ConfigReader.getBooleanProperty("android.autoGrantPermissions", true));
        options.setCapability("autoAcceptAlerts", ConfigReader.getBooleanProperty("android.autoAcceptAlerts", true));
        options.setCapability("autoDismissAlerts", ConfigReader.getBooleanProperty("android.autoDismissAlerts", true));
        
        // Network and performance
        options.setCapability("networkSpeed", ConfigReader.getProperty("android.networkSpeed", "full"));
        options.setCapability("gpsEnabled", ConfigReader.getBooleanProperty("android.gpsEnabled", true));
        
        // Timeouts - Increased for React Native apps and UiAutomator2 stability
        options.setNewCommandTimeout(Duration.ofSeconds(ConfigReader.getIntProperty("android.newCommandTimeout", 600))); // Increased to 10 minutes
        options.setCapability("androidInstallTimeout", ConfigReader.getIntProperty("android.androidInstallTimeout", 90000));
        
        // UiAutomator2 specific - Enhanced for stability
        options.setCapability("uiautomator2ServerLaunchTimeout", ConfigReader.getIntProperty("android.uiautomator2ServerLaunchTimeout", 120000)); // 2 minutes
        options.setCapability("uiautomator2ServerInstallTimeout", ConfigReader.getIntProperty("android.uiautomator2ServerInstallTimeout", 60000)); // 1 minute
        // IMPORTANT:
        // uiautomator2ServerReadTimeout is in milliseconds. Keeping it too high makes "findElement" look like it hangs forever.
        options.setCapability("uiautomator2ServerReadTimeout", ConfigReader.getIntProperty("android.uiautomator2ServerReadTimeout", 30000)); // 30s
        options.setCapability("adbExecTimeout", ConfigReader.getIntProperty("android.adbExecTimeout", 120000));
        
        // Element finding timeouts - Critical for UiAutomator2 stability
        // NOTE: These are milliseconds in UiAutomator2. Our config uses ms values.
        options.setCapability("waitForIdleTimeout", ConfigReader.getIntProperty("android.waitForIdleTimeout", 15000));
        options.setCapability("waitForSelectorTimeout", ConfigReader.getIntProperty("android.waitForSelectorTimeout", 5000));
        options.setCapability("actionAcknowledgmentTimeout", ConfigReader.getIntProperty("android.actionAcknowledgmentTimeout", 5000));
        options.setCapability("scrollAcknowledgmentTimeout", ConfigReader.getIntProperty("android.scrollAcknowledgmentTimeout", 500));
        
        // Additional Android capabilities
        options.setCapability("skipUnlock", ConfigReader.getBooleanProperty("android.skipUnlock", true));
        options.setCapability("unlockType", ConfigReader.getProperty("android.unlockType", "pin"));
        options.setCapability("unlockKey", ConfigReader.getProperty("android.unlockKey", "1234"));
        
        // UiAutomator2 stability improvements
        options.setCapability("ignoreUnimportantViews", ConfigReader.getBooleanProperty("android.ignoreUnimportantViews", false)); // Keep all views for React Native
        options.setCapability("disableAndroidWatchers", ConfigReader.getBooleanProperty("android.disableAndroidWatchers", false));
        // Re-installing UiAutomator2 server on every session is a common crash source. Prefer re-use.
        options.setCapability("skipServerInstallation", ConfigReader.getBooleanProperty("android.skipServerInstallation", true));

        // Optional: disable Android window animations to reduce RN transition flakiness
        if (ConfigReader.hasProperty("android.disableWindowAnimation")) {
            options.setCapability("disableWindowAnimation", ConfigReader.getBooleanProperty("android.disableWindowAnimation", true));
        }
        
        // Logging
        options.setCapability("enablePerformanceLogging", ConfigReader.getBooleanProperty("android.enablePerformanceLogging", false));
        
        logger.info("Android options configured with UiAutomator2 stability improvements: {}", options.asMap());
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
    }
    
    /**
     * Generate a robust list of Appium server URLs to try.
     * Supports Appium 2.x ("/") and Appium 1.x ("/wd/hub") based on config.
     */
    private static List<URL> getAppiumServerURLs() {
        try {
            String baseUrl = ConfigReader.getProperty("appium.serverUrl", "http://127.0.0.1:4723").trim();
            String serverPath = ConfigReader.getProperty("appium.serverPath", "").trim();

            LinkedHashSet<String> candidates = new LinkedHashSet<>();

            if (!baseUrl.isEmpty()) {
                candidates.add(baseUrl);

                // serverPath is typically "/wd/hub" for Appium 1.x, empty for Appium 2.x.
                if (!serverPath.isEmpty()) {
                    String normalized = baseUrl.endsWith("/")
                            ? baseUrl.substring(0, baseUrl.length() - 1) + serverPath
                            : baseUrl + serverPath;
                    candidates.add(normalized);
                }

                // If user put "/wd/hub" into baseUrl, also try the base root (Appium 2.x)
                if (baseUrl.contains("/wd/hub")) {
                    candidates.add(baseUrl.replace("/wd/hub", ""));
                }
            }

            // Hard fallbacks (helpful when config is missing/mis-set)
            candidates.add("http://127.0.0.1:4723");
            candidates.add("http://127.0.0.1:4723/wd/hub");

            List<URL> urls = new ArrayList<>();
            for (String s : candidates) {
                try {
                    urls.add(new URL(s));
                } catch (MalformedURLException ignored) {
                    logger.warn("Skipping invalid Appium URL candidate: {}", s);
                }
            }

            logger.info("Appium server URL candidates: {}", urls);
            return urls;
        } catch (Exception e) {
            logger.error("Failed to build Appium server URL candidates", e);
            throw new RuntimeException("Failed to build Appium server URL candidates", e);
        }
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
            
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("instrumentation process is not running")) {
                logger.error("UiAutomator2 instrumentation crashed during element finding: {}", locator);
                
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
                logger.error("Element finding failed: {}", locator, e);
                return null;
            }
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
                    currentDriver.executeScript("mobile: activateApp", java.util.Map.of("appId", AppConstants.APP_PACKAGE));
                } else if (currentDriver instanceof IOSDriver) {
                    currentDriver.executeScript("mobile: terminateApp", java.util.Map.of("bundleId", AppConstants.APP_BUNDLE_ID));
                    currentDriver.executeScript("mobile: activateApp", java.util.Map.of("bundleId", AppConstants.APP_BUNDLE_ID));
                }
                logger.info("App restarted successfully");
            } catch (Exception e) {
                logger.warn("Failed to restart app", e);
            }
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
