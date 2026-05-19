package com.modis.base;

import com.modis.constants.AppConstants;
import com.modis.drivers.DriverManager;
import com.modis.listeners.TestListener;
import com.modis.utils.ConfigReader;
import com.modis.utils.LoggerUtil;
import com.modis.utils.ScreenshotUtils;
import io.appium.java_client.AppiumDriver;
import org.slf4j.Logger;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.lang.reflect.Method;

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
            driver = DriverManager.createDriver(platform);
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
        if (!DriverManager.isDriverInitialized()) {
            logger.warn("Driver not initialized, creating new driver");
            String platform = System.getProperty("platform", "android");
            driver = DriverManager.createDriver(platform);
        }
        
        // Reset app to clean state for each test
        if (shouldResetAppBeforeTest()) {
            resetAppToCleanState();
        }
        
        // Wait for app to be ready
        waitForAppToBeReady();
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
        return ConfigReader.getBooleanProperty("test.resetAppBeforeTest", true);
    }
    
    /**
     * Reset app to clean state
     */
    protected void resetAppToCleanState() {
        try {
            logger.info("Resetting app to clean state");
            
            // Option 1: Restart app (faster)
            if (ConfigReader.getBooleanProperty("test.useAppRestart", true)) {
                DriverManager.restartApp();
            } 
            // Option 2: Reset app (slower but more thorough)
            else {
                DriverManager.resetApp();
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
            // Wait for app launch
            Thread.sleep(AppConstants.ANIMATION_WAIT * 1000L);
            
            // Additional platform-specific wait logic can be added here
            if (DriverManager.getCurrentPlatform().equalsIgnoreCase("android")) {
                waitForAndroidAppReady();
            } else if (DriverManager.getCurrentPlatform().equalsIgnoreCase("ios")) {
                waitForIOSAppReady();
            }
            
            logger.debug("App is ready for testing");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Wait for app ready interrupted", e);
        }
    }
    
    /**
     * Android-specific app ready checks
     */
    private void waitForAndroidAppReady() {
        // Check if app is in foreground
        // Additional Android-specific checks can be added here
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
            // Take screenshot
            String screenshotName = String.format("%s_%s_FAILED", 
                                                this.getClass().getSimpleName(), 
                                                result.getMethod().getMethodName());
            ScreenshotUtils.takeScreenshot(screenshotName);
            
            // Log failure details
            Throwable throwable = result.getThrowable();
            if (throwable != null) {
                logger.error("Test failure reason: {}", throwable.getMessage());
                logger.error("Stack trace: ", throwable);
            }
            
            // Log current app state
            logCurrentAppState();
            
        } catch (Exception e) {
            logger.error("Failed to handle test failure", e);
        }
    }
    
    /**
     * Log current app state for debugging
     */
    protected void logCurrentAppState() {
        try {
            if (DriverManager.isDriverInitialized()) {
                logger.info("Current platform: {}", DriverManager.getCurrentPlatform());
                logger.info("Current device: {}", DriverManager.getCurrentDeviceName());
                
                // Log page source for debugging (be careful with large outputs)
                if (ConfigReader.getBooleanProperty("test.logPageSourceOnFailure", false)) {
                    String pageSource = driver.getPageSource();
                    logger.debug("Page source: {}", pageSource);
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to log app state", e);
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