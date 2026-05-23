package com.modis.utils;

import com.modis.constants.TestIDs;
import com.modis.drivers.DriverManager;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Smart waiting utilities optimized for React Native apps
 * Provides robust element waiting with UiAutomator2 crash protection
 */
public class SmartWaitUtils {
    
    private static final Logger logger = LoggerUtil.getLogger(SmartWaitUtils.class);
    
    // Optimized timeouts for React Native
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final int QUICK_TIMEOUT_SECONDS = 5;
    private static final int POLLING_INTERVAL_MS = 500;
    
    /**
     * SIMPLIFIED: Wait for any screen với timeout ngắn và logic đơn giản
     */
    public static ScreenDetectionResult waitForAnyScreen(int timeoutSeconds) {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) {
            logger.error("No driver available for screen detection");
            return new ScreenDetectionResult(ScreenType.UNKNOWN, null);
        }

        logger.info("Waiting for any screen to appear (timeout: {}s)", timeoutSeconds);
        
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            wait.pollingEvery(Duration.ofMillis(250));

            return wait.until(d -> {
                if (isPresentByIdOrAccessibility(TestIDs.TOPBAR_AVATAR_BUTTON) ||
                        isPresentByIdOrAccessibility(TestIDs.HOME_SCREEN)) {
                    logger.info("Screen detected: HOME");
                    return new ScreenDetectionResult(ScreenType.HOME, null);
                }

                if (isPresentByIdOrAccessibility(TestIDs.LOGIN_USERNAME_INPUT)) {
                    logger.info("Screen detected: LOGIN");
                    return new ScreenDetectionResult(ScreenType.LOGIN, null);
                }

                if (isPresentByIdOrAccessibility(TestIDs.SIGNUP_USERNAME_INPUT)) {
                    logger.info("Screen detected: SIGNUP");
                    return new ScreenDetectionResult(ScreenType.SIGNUP, null);
                }

                if (isPresentByIdOrAccessibility(TestIDs.LOADING_LOGIN_BUTTON) ||
                        isPresentByIdOrAccessibility(TestIDs.LOADING_SIGNUP_BUTTON)) {
                    logger.info("Screen detected: LOADING");
                    return new ScreenDetectionResult(ScreenType.LOADING, null);
                }

                return null;
            });
        } catch (TimeoutException e) {
        } catch (Exception e) {
            logger.warn("Screen detection failed: {}", e.getMessage());
        }
        
        logger.warn("No screen detected after {}s", timeoutSeconds);
        return new ScreenDetectionResult(ScreenType.UNKNOWN, null);
    }
    
    /**
     * Wait for specific element with UiAutomator2 crash protection
     */
    public static WebElement waitForElement(By locator, int timeoutSeconds) {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) {
            logger.error("No driver available for element waiting");
            return null;
        }

        logger.debug("Waiting for element: {} (timeout: {}s)", locator, timeoutSeconds);
        
        try {
            // Check UiAutomator2 health before waiting
            if (!DriverManager.isUiAutomator2Healthy()) {
                logger.warn("UiAutomator2 not healthy, attempting recovery before element wait");
                if (!DriverManager.recoverFromUiAutomator2Crash()) {
                    logger.error("Failed to recover UiAutomator2 for element waiting");
                    return null;
                }
                driver = DriverManager.getDriver(); // Get new driver after recovery
            }
            
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            wait.pollingEvery(Duration.ofMillis(POLLING_INTERVAL_MS));
            
            return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            
        } catch (Exception e) {
            logger.debug("Element not found within {}s: {} - {}", timeoutSeconds, locator, e.getMessage());
            
            // Check if this looks like a UiAutomator2 crash
            String errorMessage = e.getMessage() != null ? e.getMessage() : "";
            if (errorMessage.contains("instrumentation process is not running") ||
                errorMessage.contains("Could not proxy command") ||
                errorMessage.contains("socket hang up")) {
                
                logger.error("UiAutomator2 crash detected during element wait, attempting recovery");
                if (DriverManager.recoverFromUiAutomator2Crash()) {
                    logger.info("Retrying element wait after UiAutomator2 recovery");
                    try {
                        driver = DriverManager.getDriver();
                        WebDriverWait retryWait = new WebDriverWait(driver, Duration.ofSeconds(Math.min(timeoutSeconds, 10)));
                        return retryWait.until(ExpectedConditions.presenceOfElementLocated(locator));
                    } catch (Exception retryException) {
                        logger.error("Element wait failed even after UiAutomator2 recovery", retryException);
                    }
                }
            }
            
            return null;
        }
    }
    
    /**
     * Wait for element to be clickable with crash protection
     */
    public static WebElement waitForClickableElement(By locator, int timeoutSeconds) {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) {
            return null;
        }

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            wait.pollingEvery(Duration.ofMillis(POLLING_INTERVAL_MS));
            
            return wait.until(ExpectedConditions.elementToBeClickable(locator));
            
        } catch (Exception e) {
            logger.debug("Clickable element not found within {}s: {}", timeoutSeconds, locator);
            return null;
        }
    }
    
    /**
     * SIMPLIFIED: Quick element check - chỉ dùng 1 giây timeout
     */
    public static boolean isElementPresent(By locator) {
        try {
            List<WebElement> elements = DriverManager.safelyFindElements(locator);
            if (elements == null || elements.isEmpty()) return false;
            for (WebElement element : elements) {
                try {
                    if (element != null && element.isDisplayed()) return true;
                } catch (Exception ignored) {}
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isPresentByIdOrAccessibility(String id) {
        if (id == null || id.isEmpty()) return false;
        if (isElementPresent(AppiumBy.accessibilityId(id))) return true;
        if (isElementPresent(AppiumBy.id(id))) return true;
        if (DriverManager.getCurrentPlatform().equalsIgnoreCase("android")) {
            try {
                String uiSelector = String.format("new UiSelector().resourceIdMatches(\".*:id/%s\")", id);
                return isElementPresent(AppiumBy.androidUIAutomator(uiSelector));
            } catch (Exception ignored) {
                return false;
            }
        }
        return false;
    }
    
    /**
     * Identify screen type from found element
     */
    private static ScreenType identifyScreenFromElement(WebElement element) {
        try {
            String accessibilityId = element.getAttribute("content-desc");
            if (accessibilityId == null) {
                accessibilityId = element.getAttribute("name"); // iOS fallback
            }
            
            if (accessibilityId != null) {
                // Home screen elements
                if (accessibilityId.equals(TestIDs.HOME_SCREEN) ||
                    accessibilityId.equals(TestIDs.TOPBAR_AVATAR_BUTTON) ||
                    accessibilityId.equals(TestIDs.TAKE_SCREEN) ||
                    accessibilityId.equals(TestIDs.FEED_SCREEN)) {
                    return ScreenType.HOME;
                }
                
                // Login screen elements
                if (accessibilityId.equals(TestIDs.LOGIN_USERNAME_INPUT) ||
                    accessibilityId.equals(TestIDs.LOGIN_SUBMIT_BUTTON)) {
                    return ScreenType.LOGIN;
                }
                
                // Signup screen elements
                if (accessibilityId.equals(TestIDs.SIGNUP_USERNAME_INPUT) ||
                    accessibilityId.equals(TestIDs.SIGNUP_SUBMIT_BUTTON)) {
                    return ScreenType.SIGNUP;
                }
                
                // Loading screen elements
                if (accessibilityId.equals(TestIDs.LOADING_LOGIN_BUTTON) ||
                    accessibilityId.equals(TestIDs.LOADING_SIGNUP_BUTTON)) {
                    return ScreenType.LOADING;
                }
            }
            
        } catch (Exception e) {
            logger.debug("Could not identify screen from element: {}", e.getMessage());
        }
        
        return ScreenType.UNKNOWN;
    }
    
    /**
     * Get element description for logging
     */
    private static String getElementDescription(WebElement element) {
        try {
            String accessibilityId = element.getAttribute("content-desc");
            if (accessibilityId != null && !accessibilityId.isEmpty()) {
                return "accessibilityId=" + accessibilityId;
            }
            
            String resourceId = element.getAttribute("resource-id");
            if (resourceId != null && !resourceId.isEmpty()) {
                return "resource-id=" + resourceId;
            }
            
            return element.getTagName();
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    /**
     * Capture debug information when things go wrong
     */
    public static void captureDebugInfo(String reason) {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) {
            logger.warn("Cannot capture debug info - no driver available");
            return;
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String debugDir = "target/debug-captures";
        
        try {
            // Create debug directory
            Path debugPath = Paths.get(debugDir);
            Files.createDirectories(debugPath);
            
            // Capture screenshot
            if (driver instanceof TakesScreenshot) {
                try {
                    File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                    Path screenshotPath = debugPath.resolve(String.format("%s_%s_screenshot.png", timestamp, reason));
                    Files.copy(screenshot.toPath(), screenshotPath);
                    logger.info("Screenshot captured: {}", screenshotPath);
                } catch (Exception e) {
                    logger.warn("Failed to capture screenshot: {}", e.getMessage());
                }
            }
            
            // Capture page source
            try {
                String pageSource = driver.getPageSource();
                Path pageSourcePath = debugPath.resolve(String.format("%s_%s_page_source.xml", timestamp, reason));
                Files.write(pageSourcePath, pageSource.getBytes());
                logger.info("Page source captured: {}", pageSourcePath);
            } catch (Exception e) {
                logger.warn("Failed to capture page source: {}", e.getMessage());
            }
            
            // Log current activity (Android only)
            if (driver instanceof AndroidDriver) {
                try {
                    AndroidDriver androidDriver = (AndroidDriver) driver;
                    String currentActivity = androidDriver.currentActivity();
                    String currentPackage = androidDriver.getCurrentPackage();
                    logger.info("Current activity: {}, package: {}", currentActivity, currentPackage);
                } catch (Exception e) {
                    logger.warn("Failed to get current activity: {}", e.getMessage());
                }
            }
            
        } catch (IOException e) {
            logger.error("Failed to create debug directory: {}", e.getMessage());
        }
    }
    
    /**
     * Screen detection result
     */
    public static class ScreenDetectionResult {
        private final ScreenType screenType;
        private final WebElement element;
        
        public ScreenDetectionResult(ScreenType screenType, WebElement element) {
            this.screenType = screenType;
            this.element = element;
        }
        
        public ScreenType getScreenType() {
            return screenType;
        }
        
        public WebElement getElement() {
            return element;
        }
        
        public boolean isFound() {
            return screenType != ScreenType.UNKNOWN && element != null;
        }
    }
    
    /**
     * SIMPLIFIED: Auto-dismiss error dialogs với strategy đơn giản
     * Chỉ dùng accessibility ID chính xác, không dùng xpath phức tạp
     */
    public static boolean autoDismissErrorDialogs() {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) {
            return false;
        }

        logger.debug("Checking for error dialogs to auto-dismiss");
        
        try {
            // STRATEGY 1: Chỉ check accessibility ID chính xác
            if (isElementPresent(AppiumBy.accessibilityId(TestIDs.ERROR_DIALOG_OK_BUTTON))) {
                By ok = AppiumBy.accessibilityId(TestIDs.ERROR_DIALOG_OK_BUTTON);
                driver.findElements(ok).get(0).click();
                logger.info("Dismissed dialog using ERROR_DIALOG_OK_BUTTON");
                waitForGone(driver, ok, 2);
                return true;
            }
            
            if (isElementPresent(AppiumBy.accessibilityId(TestIDs.LOGIN_ERROR_OK_BUTTON))) {
                By ok = AppiumBy.accessibilityId(TestIDs.LOGIN_ERROR_OK_BUTTON);
                driver.findElements(ok).get(0).click();
                logger.info("Dismissed dialog using LOGIN_ERROR_OK_BUTTON");
                waitForGone(driver, ok, 2);
                return true;
            }
            
            if (isElementPresent(AppiumBy.accessibilityId(TestIDs.ALERT_OK_BUTTON))) {
                By ok = AppiumBy.accessibilityId(TestIDs.ALERT_OK_BUTTON);
                driver.findElements(ok).get(0).click();
                logger.info("Dismissed dialog using ALERT_OK_BUTTON");
                waitForGone(driver, ok, 2);
                return true;
            }
            
            // STRATEGY 2: Chỉ check resource-id chuẩn (không xpath)
            try {
                By ok = By.id("android:id/button1");
                List<WebElement> okButtons = driver.findElements(ok);
                if (okButtons != null && !okButtons.isEmpty() && okButtons.get(0).isDisplayed()) {
                    okButtons.get(0).click();
                    logger.info("Dismissed dialog using standard android OK button");
                    waitForGone(driver, ok, 2);
                    return true;
                }
            } catch (Exception ignored) {}
            
            return false; // No dialog found
            
        } catch (Exception e) {
            logger.debug("Error during auto-dismiss dialog check: {}", e.getMessage());
            return false;
        }
    }

    private static void waitForGone(AppiumDriver driver, By locator, int timeoutSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            wait.pollingEvery(Duration.ofMillis(250));
            wait.until(d -> d.findElements(locator).isEmpty());
        } catch (Exception ignored) {
        }
    }
    
    /**
     * Screen types for navigation
     */
    public enum ScreenType {
        HOME,
        LOGIN,
        SIGNUP,
        LOADING,
        UNKNOWN
    }
}
