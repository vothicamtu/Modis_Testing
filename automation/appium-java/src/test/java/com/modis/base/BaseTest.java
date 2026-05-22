package com.modis.base;

import com.modis.constants.AppConstants;
import com.modis.drivers.DriverManager;
import com.modis.listeners.TestListener;
import com.modis.utils.ConfigReader;
import com.modis.utils.LoggerUtil;
import com.modis.utils.ScreenshotUtils;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import java.time.Duration;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Base Test class for all test classes
 * Handles driver setup, teardown, and common test utilities
 */
@Listeners({TestListener.class})
public abstract class BaseTest {

    protected final Logger logger = LoggerUtil.getLogger(this.getClass());
    protected AppiumDriver driver;

    // Test configuration parameters
    @Parameters({"platform", "deviceName", "platformVersion"})
    @BeforeClass(alwaysRun = true)
    public void setUpClass(@Optional("android") String platform,
                           @Optional("Android Emulator") String deviceName,
                           @Optional("11.0") String platformVersion) {

        logger.info("=== Starting Test Class: {} ===", this.getClass().getSimpleName());
        logger.info("Platform: {}, Device: {}, Version: {}", platform, deviceName, platformVersion);

        // Set system properties for configuration
        System.setProperty("platform", platform);
        System.setProperty("deviceName", deviceName);
        System.setProperty("platformVersion", platformVersion);

        // Initialize configuration
        ConfigReader.loadConfiguration();

        // Create driver
        try {
            driver = initializeDriver(platform);
            logger.info("Driver created successfully for class: {}", this.getClass().getSimpleName());
        } catch (Exception e) {
            logger.error("Failed to create driver for class: {}", this.getClass().getSimpleName(), e);
            throw new RuntimeException("Driver creation failed", e);
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void setUpMethod(Method method) {
        logger.info("--- Starting Test Method: {} ---", method.getName());

        // Ensure driver is available
        if (!DriverManager.isSessionAlive()) {
            logger.warn("Driver not initialized, creating new driver");
            String platform = System.getProperty("platform", "android");
            driver = initializeDriver(platform);
        }

        // Check UiAutomator2 health before each test method
        if ("android".equalsIgnoreCase(System.getProperty("platform", "android")) && !DriverManager.isUiAutomator2Healthy()) {
            logger.warn("UiAutomator2 not healthy before test method, attempting recovery...");
            if (!DriverManager.recoverFromUiAutomator2Crash()) {
                throw new RuntimeException("Failed to recover UiAutomator2 before test method: " + method.getName());
            }
            // Update driver reference after recovery
            driver = DriverManager.getDriver();
        }

        // Ensure app is launched and in foreground
        ensureAppIsLaunched();

        // Reset app to clean state for each test if needed
        if (shouldResetAppBeforeTest()) {
            resetAppToCleanState();
        }

        // Wait for app to be ready
        waitForAppToBeReady();
    }

    /**
     * Ensure Modis app is launched and in foreground with timeout protection
     */
    private void ensureAppIsLaunched() {
        if (driver instanceof AndroidDriver) {
            AndroidDriver androidDriver = (AndroidDriver) driver;
            String targetPackage = "com.modis";

            try {
                String currentPackage = androidDriver.getCurrentPackage();
                logger.info("Current package: {}", currentPackage);

                if (!targetPackage.equals(currentPackage)) {
                    logger.info("App not in foreground, activating: {}", targetPackage);
                    // Keep this minimal. Direct adb commands from the test JVM are a common source of flakiness
                    // (wrong target device, adb server contention, extra latency).
                    androidDriver.activateApp(targetPackage);
                    // Wait until app is actually back in foreground (bounded)
                    new WebDriverWait(androidDriver, Duration.ofSeconds(10)).until(d -> {
                        try {
                            return targetPackage.equals(androidDriver.getCurrentPackage());
                        } catch (Exception e) {
                            return false;
                        }
                    });

                } else {
                    logger.info("App already in foreground: {}", targetPackage);
                }

            } catch (Exception e) {
                logger.error("Failed to ensure app is launched", e);
                throw new RuntimeException("App launch verification failed", e);
            }
        }
    }

    /**
     * Initialize Appium Driver with UiAutomator2Options for Appium 2.x
     * @param platform The target platform (android/ios)
     * @return AppiumDriver instance
     */
    private AppiumDriver initializeDriver(String platform) {
        try {
            AppiumDriver appiumDriver = DriverManager.createDriver(platform);
            this.driver = appiumDriver;
            return appiumDriver;
        } catch (Exception e) {
            logger.error("Failed to initialize driver for platform: {}", platform, e);
            throw new RuntimeException("Driver initialization failed", e);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownMethod(ITestResult result) {
        String methodName = result.getMethod().getMethodName();

        if (result.getStatus() == ITestResult.FAILURE) {
            logger.error("Test method failed: {}", methodName);
            handleTestFailure(result);
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            logger.info("Test method passed: {}", methodName);
        } else if (result.getStatus() == ITestResult.SKIP) {
            logger.warn("Test method skipped: {}", methodName);
        }

        // Cleanup after test method
        performMethodCleanup();

        logger.info("--- Finished Test Method: {} ---", methodName);
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        logger.info("=== Finishing Test Class: {} ===", this.getClass().getSimpleName());

        // Quit driver
        if (DriverManager.isDriverInitialized()) {
            DriverManager.quitDriver();
            logger.info("Driver quit successfully for class: {}", this.getClass().getSimpleName());
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Determine if app should be reset before each test
     * Override in subclasses if different behavior is needed
     * @return true if app should be reset, false otherwise
     */
    protected boolean shouldResetAppBeforeTest() {
        // Default MUST be false.
        // Restarting (terminate/activate) before every test method is a top source of flakiness for RN + UiAutomator2
        // and has been observed to destabilize the session (socket hang up).
        return ConfigReader.getBooleanProperty("test.resetAppBeforeTest", false);
    }

    /**
     * Reset app to clean state
     */
    protected void resetAppToCleanState() {
        try {
            logger.info("Resetting app to clean state");

            // App restarts are extremely expensive and often destabilize UiAutomator2 on real devices.
            // Keep this behind explicit opt-in flags only.
            boolean useAppRestart = ConfigReader.getBooleanProperty("test.useAppRestart", false);
            boolean useFullReset = ConfigReader.getBooleanProperty("test.useFullAppReset", false);

            if (useFullReset) {
                DriverManager.resetApp(); // terminate + clear + activate
            } else if (useAppRestart) {
                DriverManager.restartApp(); // terminate + activate
            } else {
                logger.info("App reset skipped (test.useAppRestart=false and test.useFullAppReset=false)");
                return;
            }

            logger.info("App reset completed");
        } catch (Exception e) {
            logger.warn("Failed to reset app, continuing with test", e);
        }
    }

    /**
     * Wait for app to be ready after launch/reset
     */
    protected void waitForAppToBeReady() {
        try {
            // Additional platform-specific wait logic can be added here
            if (DriverManager.getCurrentPlatform().equalsIgnoreCase("android")) {
                waitForAndroidAppReady();
            } else if (DriverManager.getCurrentPlatform().equalsIgnoreCase("ios")) {
                waitForIOSAppReady();
            }

            logger.debug("App is ready for testing");
        } catch (Exception e) {
            logger.warn("Wait for app ready failed (non-fatal)", e);
        }
    }

    /**
     * Android-specific app ready checks with React Native support
     * Fixed to prevent infinite waiting and stuck at loading screen
     */
    private void waitForAndroidAppReady() {
        if (driver instanceof AndroidDriver) {
            AndroidDriver androidDriver = (AndroidDriver) driver;
            String targetPackage = "com.modis";
            logger.info("Waiting for Android app package '{}' to be in foreground", targetPackage);

            // Step 1: Wait for app package to be in foreground with timeout protection
            WebDriverWait packageWait = new WebDriverWait(androidDriver, Duration.ofSeconds(AppConstants.RN_APP_LAUNCH_TIMEOUT));
            try {
                packageWait.until(d -> {
                    try {
                        String currentPackage = androidDriver.getCurrentPackage();
                        logger.debug("Current package in foreground: {}", currentPackage);
                        return targetPackage.equals(currentPackage);
                    } catch (Exception e) {
                        logger.debug("Error getting current package: {}", e.getMessage());
                        return false;
                    }
                });
                logger.info("Android app '{}' successfully verified in foreground", targetPackage);
            } catch (TimeoutException e) {
                logger.warn("App package '{}' not in foreground after {}s, attempting to activate", targetPackage, AppConstants.RN_APP_LAUNCH_TIMEOUT);
                try {
                    androidDriver.activateApp(targetPackage);
                    logger.info("Manually activated app '{}'", targetPackage);
                    // Wait until the package is really foreground (bounded, no fixed sleep)
                    new WebDriverWait(androidDriver, Duration.ofSeconds(10)).until(d -> {
                        try {
                            return targetPackage.equals(androidDriver.getCurrentPackage());
                        } catch (Exception ex) {
                            return false;
                        }
                    });
                } catch (Exception ex) {
                    logger.error("Failed to manually activate app '{}'", targetPackage, ex);
                    throw new RuntimeException("App activation failed: " + targetPackage, ex);
                }
            }

            // Step 2: Wait for React Native bridge with retry mechanism
            logger.info("Waiting for React Native bridge to be ready...");
            waitForReactNativeBridge();

            // Step 3: Wait for main activity to be ready
            waitForMainActivity();
        }
    }

    /**
     * Wait for React Native bridge to be ready with retry mechanism
     * Prevents infinite waiting at loading screen
     */
    private void waitForReactNativeBridge() {
        AndroidDriver androidDriver = (AndroidDriver) driver;
        try {
            long timeoutSec = AppConstants.RN_BRIDGE_WAIT;
            logger.info("Waiting for React Native bridge readiness (activity-based) up to {}s...", timeoutSec);
            new WebDriverWait(androidDriver, Duration.ofSeconds(timeoutSec))
                    .pollingEvery(Duration.ofMillis(500))
                    .until(d -> {
                        try {
                            String currentActivity = androidDriver.currentActivity();
                            logger.debug("Current activity: {}", currentActivity);
                            return currentActivity != null
                                    && currentActivity.contains("MainActivity")
                                    && !currentActivity.contains("Splash")
                                    && !currentActivity.contains("Loading");
                        } catch (Exception e) {
                            return false;
                        }
                    });
            logger.info("✓ React Native bridge ready (MainActivity confirmed)");
        } catch (TimeoutException te) {
            logger.warn("React Native bridge check timed out; continuing (non-fatal)");
        } catch (Exception e) {
            logger.debug("Error while waiting for React Native bridge: {}", e.getMessage());
        }
    }

    /**
     * Wait for main activity to be ready
     */
    private void waitForMainActivity() {
        try {
            AndroidDriver androidDriver = (AndroidDriver) driver;
            WebDriverWait activityWait = new WebDriverWait(androidDriver, Duration.ofSeconds(10));

            activityWait.until(d -> {
                try {
                    String currentActivity = androidDriver.currentActivity();
                    logger.debug("Current activity: {}", currentActivity);

                    // We're good if we're on MainActivity and not on splash/loading
                    return currentActivity != null &&
                            currentActivity.contains("MainActivity") &&
                            !currentActivity.contains("Splash") &&
                            !currentActivity.contains("Loading");
                } catch (Exception e) {
                    return false;
                }
            });

            logger.info("Main activity is ready");

        } catch (TimeoutException e) {
            logger.warn("Main activity wait timeout, but continuing anyway");
        } catch (Exception e) {
            logger.debug("Error waiting for main activity: {}", e.getMessage());
        }
    }

    /**
     * iOS-specific app ready checks
     */
    private void waitForIOSAppReady() {
        // iOS-specific checks can be added here
    }

    /**
     * Handle test failure - take screenshot and log details
     * @param result The test result
     */
    protected void handleTestFailure(ITestResult result) {
        try {
            // Step 1: Take screenshot ONLY if UiAutomator2 is healthy
            if (DriverManager.isDriverInitialized() && DriverManager.isUiAutomator2Healthy()) {
                try {
                    String screenshotName = String.format("%s_%s_FAILED",
                            this.getClass().getSimpleName(),
                            result.getMethod().getMethodName());
                    ScreenshotUtils.takeScreenshot(screenshotName);
                    logger.info("✓ Failure screenshot captured");
                } catch (Exception screenshotEx) {
                    logger.error("Failed to take failure screenshot (non-critical): {}",
                            screenshotEx.getMessage());
                    // Continue - don't let screenshot fail stop test
                }
            } else {
                logger.warn("UiAutomator2 not healthy or driver null - skipping screenshot");
            }

            // Step 2: Log failure details
            Throwable throwable = result.getThrowable();
            if (throwable != null) {
                logger.error("Test failure reason: {}", throwable.getMessage());
                logger.debug("Stack trace: ", throwable);
            }

            // Step 3: Log app state with QUICK method (no expensive operations)
            logCurrentAppStateQuick();

        } catch (Exception e) {
            logger.error("Failed to handle test failure properly", e);
        }
    }

    protected void logCurrentAppStateQuick() {
        try {
            if (DriverManager.isDriverInitialized()) {
                logger.info("Current platform: {}", DriverManager.getCurrentPlatform());
                logger.info("Current device: {}", DriverManager.getCurrentDeviceName());

                // For Android, log current activity ONLY (fast operation)
                if (driver instanceof AndroidDriver) {
                    try {
                        AndroidDriver androidDriver = (AndroidDriver) driver;
                        String currentActivity = androidDriver.currentActivity();
                        logger.info("Current activity: {}", currentActivity);
                    } catch (Exception e) {
                        logger.debug("Could not get current activity: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to log quick app state", e);
        }
    }

    /**
     * Perform cleanup after each test method
     */
    protected void performMethodCleanup() {
        try {
            // Hide keyboard if shown
            if (DriverManager.isDriverInitialized()) {
                try {
                    driver.executeScript("mobile: hideKeyboard");
                } catch (Exception e) {
                    logger.debug("Keyboard might not be present or cannot be hidden", e);
                }
            }

            // Navigate to home screen if configured
            if (ConfigReader.getBooleanProperty("test.navigateToHomeAfterTest", false)) {
                navigateToHomeScreen();
            }

        } catch (Exception e) {
            logger.debug("Method cleanup failed", e);
        }
    }

    /**
     * Navigate to home screen
     */
    protected void navigateToHomeScreen() {
        // This method should be overridden by subclasses with app-specific navigation
        logger.debug("Navigate to home screen - override in subclass for app-specific implementation");
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Get current test method name
     * @return Current test method name
     */
    protected String getCurrentTestMethodName() {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }

    /**
     * Skip test with reason
     * @param reason The reason for skipping
     */
    protected void skipTest(String reason) {
        logger.warn("Skipping test: {}", reason);
        throw new org.testng.SkipException(reason);
    }

    /**
     * Retry test method
     * @param maxRetries Maximum number of retries
     * @param testAction The test action to retry
     */
    protected void retryTest(int maxRetries, Runnable testAction) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                logger.info("Test attempt {} of {}", attempt, maxRetries);
                testAction.run();
                return; // Success
            } catch (Exception e) {
                lastException = e;
                logger.warn("Test attempt {} failed: {}", attempt, e.getMessage());

                if (attempt < maxRetries) {
                    // Reset app before retry
                    resetAppToCleanState();
                    waitForAppToBeReady();
                }
            }
        }

        // All retries failed
        logger.error("All {} test attempts failed", maxRetries);
        throw new RuntimeException("Test failed after " + maxRetries + " attempts", lastException);
    }

    /**
     * Put app in background and bring back
     * @param seconds Duration in background
     */
    protected void backgroundApp(int seconds) {
        logger.info("Putting app in background for {} seconds", seconds);
        DriverManager.backgroundApp(seconds);
    }

    /**
     * Check if running on CI/CD environment
     * @return true if running on CI, false otherwise
     */
    protected boolean isRunningOnCI() {
        return System.getenv("CI") != null ||
                System.getenv("JENKINS_URL") != null ||
                System.getenv("GITHUB_ACTIONS") != null;
    }

    /**
     * Get test data based on test method name
     * Override in subclasses for specific test data handling
     * @param key The data key
     * @return Test data value
     */
    protected String getTestData(String key) {
        // Default implementation - override in subclasses
        return ConfigReader.getProperty("testdata." + key, "");
    }

    /**
     * Soft assertion helper - log assertion but don't fail immediately
     * @param condition The condition to check
     * @param message The assertion message
     */
    protected void softAssert(boolean condition, String message) {
        if (!condition) {
            logger.error("Soft assertion failed: {}", message);
            // Could integrate with TestNG SoftAssert here
        } else {
            logger.debug("Soft assertion passed: {}", message);
        }
    }
}
