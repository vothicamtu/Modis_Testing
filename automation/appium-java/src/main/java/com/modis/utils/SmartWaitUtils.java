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
import java.util.List;

public class SmartWaitUtils {
    private static final Logger logger = LoggerUtil.getLogger(SmartWaitUtils.class);
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final int POLLING_INTERVAL_MS = 500;

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

    public static WebElement waitForElement(By locator, int timeoutSeconds) {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) {
            logger.error("No driver available for element waiting");
            return null;
        }
        logger.debug("Waiting for element: {} (timeout: {}s)", locator, timeoutSeconds);
        try {
            if (!DriverManager.isUiAutomator2Healthy()) {
                logger.warn("UiAutomator2 not healthy, attempting recovery before element wait");
                if (!DriverManager.recoverFromUiAutomator2Crash()) {
                    logger.error("Failed to recover UiAutomator2 for element waiting");
                    return null;
                }
                driver = DriverManager.getDriver();
            }
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            wait.pollingEvery(Duration.ofMillis(POLLING_INTERVAL_MS));
            return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (Exception e) {
            logger.debug("Element not found within {}s: {} - {}", timeoutSeconds, locator, e.getMessage());
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

    public static boolean isElementPresent(By locator) {
        try {
            List<WebElement> elements = DriverManager.safelyFindElements(locator);
            if (elements == null || elements.isEmpty()) return false;
            for (WebElement element : elements) {
                try {
                    if (element != null && element.isDisplayed()) return true;
                } catch (Exception ignored) {
                }
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

    public static void captureDebugInfo(String reason) {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) {
            logger.warn("Cannot capture debug info - no driver available");
            return;
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String debugDir = "target/debug-captures";
        try {
            Path debugPath = Paths.get(debugDir);
            Files.createDirectories(debugPath);
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
            try {
                String pageSource = driver.getPageSource();
                Path pageSourcePath = debugPath.resolve(String.format("%s_%s_page_source.xml", timestamp, reason));
                Files.write(pageSourcePath, pageSource.getBytes());
                logger.info("Page source captured: {}", pageSourcePath);
            } catch (Exception e) {
                logger.warn("Failed to capture page source: {}", e.getMessage());
            }
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

    public static boolean autoDismissErrorDialogs() {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) {
            return false;
        }
        logger.debug("Checking for error dialogs to auto-dismiss");
        try {
            if (isElementPresent(AppiumBy.accessibilityId(TestIDs.AUTH_DIALOG_OK_BUTTON))) {
                By ok = AppiumBy.accessibilityId(TestIDs.AUTH_DIALOG_OK_BUTTON);
                driver.findElements(ok).get(0).click();
                logger.info("Dismissed dialog using AUTH_DIALOG_OK_BUTTON");
                waitForGone(driver, ok, 2);
                return true;
            }
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
            try {
                By ok = By.id("android:id/button1");
                List<WebElement> okButtons = driver.findElements(ok);
                if (okButtons != null && !okButtons.isEmpty() && okButtons.get(0).isDisplayed()) {
                    okButtons.get(0).click();
                    logger.info("Dismissed dialog using standard android OK button");
                    waitForGone(driver, ok, 2);
                    return true;
                }
            } catch (Exception ignored) {
            }
            return false;
        } catch (Exception e) {
            logger.debug("Error during auto-dismiss dialog check: {}", e.getMessage());
            return false;
        }
    }

    public static boolean isAuthDialogVisible() {
        return isElementPresent(AppiumBy.accessibilityId(TestIDs.AUTH_DIALOG_CONTAINER))
                || isElementPresent(AppiumBy.accessibilityId(TestIDs.AUTH_DIALOG_TITLE))
                || isElementPresent(AppiumBy.accessibilityId(TestIDs.AUTH_DIALOG_MESSAGE))
                || isElementPresent(AppiumBy.accessibilityId(TestIDs.ERROR_DIALOG))
                || isElementPresent(AppiumBy.accessibilityId(TestIDs.LOGIN_ERROR_DIALOG))
                || isElementPresent(AppiumBy.accessibilityId(TestIDs.ALERT_DIALOG));
    }

    public static String getAuthDialogTitle() {
        String title = getTextByLocator(AppiumBy.accessibilityId(TestIDs.AUTH_DIALOG_TITLE));
        if (!title.isEmpty()) {
            return title;
        }
        return getTextByLocator(AppiumBy.accessibilityId(TestIDs.ERROR_DIALOG_TITLE));
    }

    public static String getAuthDialogMessage() {
        String message = getTextByLocator(AppiumBy.accessibilityId(TestIDs.AUTH_DIALOG_MESSAGE));
        if (!message.isEmpty()) {
            return message;
        }
        message = getTextByLocator(AppiumBy.accessibilityId(TestIDs.ERROR_DIALOG_MESSAGE));
        if (!message.isEmpty()) {
            return message;
        }
        return getTextByLocator(AppiumBy.accessibilityId(TestIDs.ERROR_DIALOG));
    }

    public static boolean waitForAuthDialog(int timeoutSeconds) {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) {
            return false;
        }
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            wait.pollingEvery(Duration.ofMillis(POLLING_INTERVAL_MS));
            return wait.until(d -> isAuthDialogVisible());
        } catch (Exception e) {
            logger.debug("Auth dialog not detected within {}s: {}", timeoutSeconds, e.getMessage());
            return false;
        }
    }

    public static boolean dismissAuthDialog() {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) {
            return false;
        }
        if (!isAuthDialogVisible()) {
            return false;
        }
        try {
            if (isElementPresent(AppiumBy.accessibilityId(TestIDs.AUTH_DIALOG_OK_BUTTON))) {
                clickFirst(AppiumBy.accessibilityId(TestIDs.AUTH_DIALOG_OK_BUTTON));
                waitForGone(driver, AppiumBy.accessibilityId(TestIDs.AUTH_DIALOG_CONTAINER), 5);
                return true;
            }
            if (isElementPresent(AppiumBy.accessibilityId(TestIDs.ERROR_DIALOG_OK_BUTTON))) {
                clickFirst(AppiumBy.accessibilityId(TestIDs.ERROR_DIALOG_OK_BUTTON));
                waitForGone(driver, AppiumBy.accessibilityId(TestIDs.ERROR_DIALOG), 5);
                return true;
            }
            if (isElementPresent(AppiumBy.accessibilityId(TestIDs.LOGIN_ERROR_OK_BUTTON))) {
                clickFirst(AppiumBy.accessibilityId(TestIDs.LOGIN_ERROR_OK_BUTTON));
                waitForGone(driver, AppiumBy.accessibilityId(TestIDs.LOGIN_ERROR_DIALOG), 5);
                return true;
            }
            if (isElementPresent(AppiumBy.accessibilityId(TestIDs.ALERT_OK_BUTTON))) {
                clickFirst(AppiumBy.accessibilityId(TestIDs.ALERT_OK_BUTTON));
                waitForGone(driver, AppiumBy.accessibilityId(TestIDs.ALERT_DIALOG), 5);
                return true;
            }
            List<WebElement> okButtons = driver.findElements(By.id("android:id/button1"));
            if (okButtons != null && !okButtons.isEmpty() && okButtons.get(0).isDisplayed()) {
                okButtons.get(0).click();
                waitForGone(driver, By.id("android:id/button1"), 5);
                return true;
            }
        } catch (Exception e) {
            logger.debug("Failed to dismiss auth dialog: {}", e.getMessage());
        }
        return false;
    }

    private static void clickFirst(By locator) {
        List<WebElement> elements = DriverManager.safelyFindElements(locator);
        if (elements != null && !elements.isEmpty()) {
            elements.get(0).click();
        }
    }

    private static String getTextByLocator(By locator) {
        try {
            List<WebElement> elements = DriverManager.safelyFindElements(locator);
            if (elements == null || elements.isEmpty()) {
                return "";
            }
            WebElement element = elements.get(0);
            String text = element.getText();
            if (text == null || text.isEmpty()) {
                text = element.getAttribute("content-desc");
            }
            if (text == null || text.isEmpty()) {
                text = element.getAttribute("text");
            }
            return text == null ? "" : text;
        } catch (Exception e) {
            return "";
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

    public enum ScreenType {
        HOME,
        LOGIN,
        SIGNUP,
        LOADING,
        UNKNOWN
    }
}
