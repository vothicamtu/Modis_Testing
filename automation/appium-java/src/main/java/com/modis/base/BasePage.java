package com.modis.base;

import com.modis.constants.AppConstants;
import com.modis.constants.TestIDs;
import com.modis.drivers.DriverManager;
import com.modis.utils.GestureUtils;
import com.modis.utils.LoggerUtil;
import com.modis.utils.ScreenshotUtils;
import com.modis.utils.SmartWaitUtils;
import com.modis.utils.UiDebugUtils;
import com.modis.utils.WaitUtils;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BasePage {

    protected final Logger logger = LoggerUtil.getLogger(this.getClass());
    protected final AppiumDriver driver;
    protected final WaitUtils waitUtils;
    protected final GestureUtils gestureUtils;

    public BasePage() {
        this.driver = DriverManager.getDriver();
        this.waitUtils = new WaitUtils(driver);
        this.gestureUtils = new GestureUtils();
        PageFactory.initElements(new AppiumFieldDecorator(driver, Duration.ofSeconds(AppConstants.ELEMENT_WAIT_TIMEOUT)), this);
        logger.info("Initialized page: {}", this.getClass().getSimpleName());
    }

    protected org.openqa.selenium.Dimension getScreenSize() {
        return gestureUtils.getScreenSize();
    }

    protected WebElement findByAccessibilityId(
            String accessibilityId
    ) {

        logger.debug(
                "Finding element by accessibility ID: {}",
                accessibilityId
        );

        final long deadlineNs =
                System.nanoTime()
                        + Duration.ofSeconds(
                        AppConstants.ELEMENT_WAIT_TIMEOUT
                ).toNanos();

        // accessibility id
        WebElement element =
                tryWaitVisible(
                        AppiumBy.accessibilityId(
                                accessibilityId
                        ),
                        deadlineNs,
                        5
                );

        if (element != null) {
            return element;
        }

        // Android resource-id
        element =
                tryWaitVisible(
                        AppiumBy.id(accessibilityId),
                        deadlineNs,
                        3
                );

        if (element != null) {
            return element;
        }

        logger.error(
                "Element '{}' not found",
                accessibilityId
        );

        throw new RuntimeException(
                "Element not found: "
                        + accessibilityId
        );
    }

    private WebElement tryWaitVisible(By locator, long deadlineNs, int maxPerStrategySeconds) {
        long remainingNs = deadlineNs - System.nanoTime();
        if (remainingNs <= 0) return null;

        int remainingSec = (int) Math.ceil(remainingNs / 1_000_000_000.0);
        int timeoutSec = Math.max(1, Math.min(maxPerStrategySeconds, remainingSec));

        try {
            return pollForVisible(locator, timeoutSec);
        } catch (Exception e) {
            logger.debug("Locator not visible within {}s: {} ({})", timeoutSec, locator, e.getMessage());
            return null;
        }
    }

    private WebElement pollForVisible(By locator, int timeoutSec) {
        long endNs = System.nanoTime() + Duration.ofSeconds(timeoutSec).toNanos();
        RuntimeException last = null;
        while (System.nanoTime() < endNs) {
            try {
                WebElement el = DriverManager.safelyFindElement(locator);
                if (el != null && el.isDisplayed()) {
                    return el;
                }
            } catch (RuntimeException e) {
                last = e;
            }

            try {
                Thread.sleep(250);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        if (last != null) {
            throw last;
        }
        return null;
    }

    protected List<WebElement> findElementsByAccessibilityId(
            String accessibilityId
    ) {

        logger.debug(
                "Finding elements by accessibility ID: {}",
                accessibilityId
        );

        try {

            List<WebElement> elements =
                    DriverManager.safelyFindElements(
                            AppiumBy.accessibilityId(
                                    accessibilityId
                            )
                    );

            if (
                    elements != null
                            &&
                            !elements.isEmpty()
            ) {

                return elements;
            }

            if (
                    DriverManager.getCurrentPlatform()
                            .equalsIgnoreCase("android")
            ) {

                return DriverManager.safelyFindElements(
                        AppiumBy.id(accessibilityId)
                );
            }

            return java.util.Collections.emptyList();

        } catch (Exception e) {

            logger.warn(
                    "Failed to find elements by accessibility ID: {}",
                    e.getMessage()
            );

            return java.util.Collections.emptyList();
        }
    }

    protected WebElement findByXPath(String xpath) {
        logger.debug("Finding element by XPath: {}", xpath);
        return waitUtils.waitForElementToBeVisible(By.xpath(xpath));
    }

    protected WebElement findById(String id) {
        logger.debug("Finding element by ID: {}", id);
        return waitUtils.waitForElementToBeVisible(By.id(id));
    }

    protected WebElement findByClassName(String className) {
        logger.debug("Finding element by class name: {}", className);
        return waitUtils.waitForElementToBeVisible(By.className(className));
    }

    protected WebElement findByText(String text) {
        logger.debug("Finding element by text: {}", text);
        String xpath = String.format("//*[@text='%s' or @content-desc='%s']", text, text);
        return waitUtils.waitForElementToBeVisible(By.xpath(xpath));
    }

    protected WebElement findByPartialText(String partialText) {
        logger.debug("Finding element by partial text: {}", partialText);
        String xpath = String.format("//*[contains(@text,'%s') or contains(@content-desc,'%s')]", partialText, partialText);
        return waitUtils.waitForElementToBeVisible(By.xpath(xpath));
    }

    // ELEMENT INTERACTION METHODS
    protected void clickElement(WebElement element) {

        if (element == null) {
            throw new RuntimeException(
                    "Click target is null"
            );
        }

        try {

            element.click();

            logger.debug(
                    "Clicked element successfully"
            );

        } catch (Exception firstException) {

            logger.warn(
                    "Standard click failed, retrying..."
            );

            waitFor(1);

            try {

                element.click();

            } catch (Exception secondException) {

                logger.warn(
                        "Retry click failed, trying tap gesture"
                );

                gestureUtils.tapElement(element);
            }
        }
    }

    protected void clickByAccessibilityId(String accessibilityId) {
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                WebElement element = findByAccessibilityId(accessibilityId);
                clickElement(element);
                logger.info("Clicked element with accessibility ID: {}", accessibilityId);
                return;
            } catch (Exception e) {
                logger.warn("Failed to click {} on attempt {}: {}", accessibilityId, attempt, e.getMessage());
                if (attempt == 2) {
                    throw e;
                }
                if (!DriverManager.recoverFromUiAutomator2Crash()) {
                    throw e;
                }
            }
        }
    }

    protected void enterText(WebElement element, String text) {
        if (element == null) {
            throw new RuntimeException("Text input element is null");
        }

        try {
            element.clear();
            element.sendKeys(text);
            logger.debug("Entered text: {}", text);
        } catch (Exception e) {
            logger.error("Failed to enter text: {}", text, e);
            throw new RuntimeException("Text input failed", e);
        }
    }

    protected void enterTextByAccessibilityId(String accessibilityId, String text) {
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                WebElement element = findByAccessibilityId(accessibilityId);
                enterText(element, text);
                logger.info("Entered text '{}' into element with accessibility ID: {}", text, accessibilityId);
                return;
            } catch (Exception e) {
                logger.warn("Failed to enter text into {} on attempt {}: {}", accessibilityId, attempt, e.getMessage());
                if (attempt == 2) {
                    throw e;
                }
                if (!DriverManager.recoverFromUiAutomator2Crash()) {
                    throw e;
                }
            }
        }
    }

    protected void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected void scrollDownSlightly() {

        Dimension size =
                driver.manage().window().getSize();

        int startX = size.width / 2;

        int startY =
                (int) (size.height * 0.70);

        int endY =
                (int) (size.height * 0.55);

        try {

            Map<String, Object> params =
                    new HashMap<>();

            params.put("startX", startX);
            params.put("startY", startY);
            params.put("endX", startX);
            params.put("endY", endY);
            params.put("speed", 600);

            driver.executeScript(
                    "mobile: dragGesture",
                    params
            );

            logger.info(
                    "Performed slight downward scroll adjustment"
            );

        } catch (Exception e) {

            logger.error(
                    "Failed to perform slight scroll",
                    e
            );

            throw new RuntimeException(
                    "Scroll gesture failed",
                    e
            );
        }
    }

    protected String getText(WebElement element) {
        waitUtils.waitForElementToBeVisible(element);
        String text = element.getText();
        if (text == null || text.isEmpty()) {
            text = element.getAttribute("content-desc");
        }
        if (text == null || text.isEmpty()) {
            text = element.getAttribute("text");
        }
        logger.debug("Retrieved text: {}", text);
        return text != null ? text : "";
    }

    protected String getTextByAccessibilityId(String accessibilityId) {
        WebElement element = findByAccessibilityId(accessibilityId);
        return getText(element);
    }

    protected boolean isElementDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            logger.debug("Element not displayed", e);
            return false;
        }
    }

    protected boolean isElementDisplayedByAccessibilityId(
            String accessibilityId
    ) {

        try {

            WebElement element =
                    DriverManager.safelyFindElement(
                            AppiumBy.accessibilityId(
                                    accessibilityId
                            )
                    );

            return element != null
                    &&
                    element.isDisplayed();

        } catch (Exception ignored) {
        }

        try {

            WebElement element =
                    DriverManager.safelyFindElement(
                            AppiumBy.id(accessibilityId)
                    );

            return element != null
                    &&
                    element.isDisplayed();

        } catch (Exception ignored) {

            return false;
        }
    }

    private boolean isAnyDisplayed(List<WebElement> els) {
        if (els == null || els.isEmpty()) return false;
        for (WebElement el : els) {
            if (el != null && isElementDisplayed(el)) return true;
        }
        return false;
    }

    protected boolean isElementEnabled(WebElement element) {
        try {
            waitUtils.waitForElementToBeVisible(element);
            return element.isEnabled();
        } catch (Exception e) {
            logger.debug("Element not enabled", e);
            return false;
        }
    }

    // SCROLL GESTURES
    protected WebElement scrollToElementByText(String text) {
        logger.info("Scrolling to element with text: {}", text);
        return gestureUtils.scrollToElementByText(text);
    }

    protected WebElement scrollToElementByAccessibilityId(String accessibilityId) {
        logger.info("Scrolling to element with accessibility ID: {}", accessibilityId);
        return gestureUtils.scrollToElementByAccessibilityId(accessibilityId);
    }

    protected void scrollDownBase() {
        gestureUtils.scrollDown();
        logger.info("Scrolled down");
    }

    protected void scrollUpBase() {
        gestureUtils.scrollUp();
        logger.info("Scrolled up");
    }

    protected void scrollToTopBase() {
        gestureUtils.scrollToTop();
        logger.info("Scrolled to top");
    }

    protected void scrollToBottomBase() {
        gestureUtils.scrollToBottom();
        logger.info("Scrolled to bottom");
    }

    protected void scrollInElement(WebElement scrollableElement, String direction) {
        gestureUtils.scrollInElement(scrollableElement, direction);
        logger.debug("Scrolled {} in element", direction);
    }

    protected void pullToRefresh() {
        gestureUtils.pullToRefresh();
        logger.info("Performed pull to refresh");
    }

    // GESTURE METHODS

    protected void swipeLeft() {
        gestureUtils.swipeLeft();
        logger.info("Performed swipe left gesture");
    }

    protected void swipeRight() {
        gestureUtils.swipeRight();
        logger.info("Performed swipe right gesture");
    }

    protected void swipeUp() {
        gestureUtils.swipeUp();
        logger.info("Performed swipe up gesture");
    }


    protected void swipeDown() {
        gestureUtils.swipeDown();
        logger.info("Performed swipe down gesture");
    }

    protected void swipeOnElement(WebElement element, String direction) {
        gestureUtils.swipeOnElement(element, direction);
        logger.info("Performed swipe {} on element", direction);
    }

    protected void longPressElement(WebElement element, int durationMs) {
        gestureUtils.longPressElement(element, durationMs);
        logger.info("Long pressed element for {}ms", durationMs);
    }

    protected void tapAtCoordinates(int x, int y) {
        gestureUtils.tapAtCoordinates(x, y);
        logger.debug("Tapped at coordinates: ({}, {})", x, y);
    }

    protected void tapElement(WebElement element) {
        gestureUtils.tapElement(element);
        logger.debug("Tapped element");
    }

    protected void pinchOut(WebElement element) {
        gestureUtils.pinchOut(element);
        logger.info("Performed pinch out on element");
    }

    protected void pinchIn(WebElement element) {
        gestureUtils.pinchIn(element);
        logger.info("Performed pinch in on element");
    }

    // WAIT METHODS
    protected WebElement waitForElementVisible(String accessibilityId) {
        return findByAccessibilityId(accessibilityId);
    }

    protected WebElement waitForElementClickable(
            String accessibilityId
    ) {

        final long deadlineNs =
                System.nanoTime()
                        + Duration.ofSeconds(
                        AppConstants.ELEMENT_WAIT_TIMEOUT
                ).toNanos();

        WebElement element =
                tryWaitClickable(
                        AppiumBy.accessibilityId(
                                accessibilityId
                        ),
                        deadlineNs,
                        5
                );

        if (element != null) {
            return element;
        }

        element =
                tryWaitClickable(
                        AppiumBy.id(accessibilityId),
                        deadlineNs,
                        3
                );

        if (element != null) {
            return element;
        }

        element =
                tryWaitClickable(
                        By.xpath(
                                String.format(
                                        "//*[@content-desc='%s' or contains(@resource-id,'%s')]",
                                        accessibilityId,
                                        accessibilityId
                                )
                        ),
                        deadlineNs,
                        2
                );

        if (element != null) {
            return element;
        }

        throw new RuntimeException(
                "Element not clickable: "
                        + accessibilityId
        );
    }

    private WebElement tryWaitClickable(By locator, long deadlineNs, int maxPerStrategySeconds) {
        long remainingNs = deadlineNs - System.nanoTime();
        if (remainingNs <= 0) return null;
        int remainingSec = (int) Math.ceil(remainingNs / 1_000_000_000.0);
        int timeoutSec = Math.max(1, Math.min(maxPerStrategySeconds, remainingSec));
        try {
            return waitUtils.waitForElementToBeClickable(locator, timeoutSec);
        } catch (Exception e) {
            logger.debug("Locator not clickable within {}s: {} ({})", timeoutSec, locator, e.getMessage());
            return null;
        }
    }


    protected void waitForElementToDisappear(String accessibilityId) {
        waitUtils.waitForElementToDisappear(AppiumBy.accessibilityId(accessibilityId));
        if (DriverManager.getCurrentPlatform().equalsIgnoreCase("android")) {
            waitUtils.waitForElementToDisappear(AppiumBy.id(accessibilityId));
        }
    }

    protected void waitForTextInElement(String accessibilityId, String text) {
        try {
            waitUtils.waitForTextToBePresentInElement(AppiumBy.accessibilityId(accessibilityId), text);
        } catch (Exception e) {
            if (DriverManager.getCurrentPlatform().equalsIgnoreCase("android")) {
                logger.debug("Wait for text in accessibility ID failed, trying native ID (resource-id): {}", accessibilityId);
                waitUtils.waitForTextToBePresentInElement(AppiumBy.id(accessibilityId), text);
            } else {
                throw e;
            }
        }
    }

    // UTILITY METHODS
    protected void waitForAnimation() {
        waitUtils.waitForAnimation();
        logger.debug("Animation wait completed");
    }

    protected void takeScreenshot(String screenshotName) {
        ScreenshotUtils.takeScreenshot(screenshotName);
        logger.info("Screenshot taken: {}", screenshotName);
    }

    protected void waitFor(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
            logger.debug("Waited for {} seconds", seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Wait interrupted", e);
        }
    }

    protected void hideKeyboard() {
        try {
            driver.executeScript("mobile: hideKeyboard");
            logger.debug("Keyboard hidden using mobile: hideKeyboard");
        } catch (Exception e) {
            logger.debug("Keyboard not visible or failed to hide", e);
        }
    }

    protected boolean isElementEnabledByAccessibilityId(String accessibilityId) {
        try {
            return isElementEnabled(findByAccessibilityId(accessibilityId));
        } catch (Exception e) {
            logger.debug("Element {} is not enabled or not found", accessibilityId, e);
            return false;
        }
    }

    protected boolean isAuthDialogVisible() {
        return SmartWaitUtils.isAuthDialogVisible();
    }

    protected String getAuthDialogMessage() {
        return SmartWaitUtils.getAuthDialogMessage();
    }

    protected String getAuthDialogTitle() {
        return SmartWaitUtils.getAuthDialogTitle();
    }

    protected boolean waitForAuthDialog(int timeoutSeconds) {
        return SmartWaitUtils.waitForAuthDialog(timeoutSeconds);
    }

    protected void dismissAuthDialog() {
        SmartWaitUtils.dismissAuthDialog();
    }

    protected void waitForAuthDialogDisappear() {
        try {
            waitUtils.waitForCondition(d -> !isAuthDialogVisible(), 5);
        } catch (Exception ignored) {
        }
    }

    protected String getCurrentPackage() {

        try {

            return driver.getSessionId() != null
                    ? "ACTIVE_SESSION"
                    : "";

        } catch (Exception e) {

            logger.debug(
                    "Failed to check session",
                    e
            );

            return "";
        }
    }

    protected String getCurrentActivity() {
        try {
            if (DriverManager.getCurrentPlatform().equalsIgnoreCase("android")) {
                if (driver instanceof AndroidDriver) {
                    return ((AndroidDriver) driver).currentActivity();
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to get current activity", e);
        }
        return "";
    }

    protected boolean isPageLoaded(String keyElementAccessibilityId) {
        try {
            waitForElementVisible(keyElementAccessibilityId);
            logger.info("Page loaded successfully - key element found: {}", keyElementAccessibilityId);
            return true;
        } catch (Exception e) {
            logger.warn("Page not loaded - key element not found: {}", keyElementAccessibilityId);
            return false;
        }
    }

    protected void refreshPage() {
        logger.info("Refreshing page");
        gestureUtils.pullToRefresh();
    }

    protected void goBack() {
        try {
            if (DriverManager.getCurrentPlatform().equalsIgnoreCase("android")) {
                driver.navigate().back();
                logger.info("Navigated back using device back button");
            }
        } catch (Exception e) {
            logger.warn("Failed to navigate back", e);
        }
    }

    // ANDROID SCROLL ENHANCEMENT METHODS
    protected void ensureElementScrolledIntoViewAndClickable(String accessibilityId) {
        logger.info("Ensuring element '{}' is scrolled into view and clickable", accessibilityId);

        try {
            // Dismiss keyboard if present (Android specific issue)
            if (DriverManager.getCurrentPlatform().equalsIgnoreCase("android")) {
                try {
                    ((AndroidDriver) driver).hideKeyboard();
                    logger.debug("Dismissed keyboard before scrolling");
                    waitForAnimation(); // Wait for keyboard dismiss animation
                } catch (Exception e) {
                    logger.debug("No keyboard to dismiss or dismiss failed: {}", e.getMessage());
                }
            }

            //  Try to find element without scrolling first
            if (isElementDisplayedAndClickable(accessibilityId)) {
                logger.debug("Element '{}' already visible and clickable", accessibilityId);
                return;
            }

            // Scroll to element using GestureUtils
            WebElement element = gestureUtils.scrollToElementByAccessibilityId(accessibilityId);

            if (element == null) {
                logger.warn("Could not scroll to element '{}' - will try direct click", accessibilityId);
                return;
            }

// Wait cho RN render + scroll animation settle
            gestureUtils.waitForGestureAnimation();
            waitFor(1);

// Verify element clickable
            int retries = 3;
            while (retries > 0 && !isElementDisplayedAndClickable(accessibilityId)) {
                logger.debug("Element '{}' not yet clickable, waiting... (retries left: {})", accessibilityId, retries);
                waitFor(1);
                retries--;
            }

            if (!isElementDisplayedAndClickable(accessibilityId)) {
                logger.warn("Element '{}' still not clickable after scrolling and waiting - will try direct click", accessibilityId);
                // Take screenshot for debugging but don't throw exception
                ScreenshotUtils.takeScreenshot("element_not_clickable_" + accessibilityId);
            } else {
                logger.info("Element '{}' is now ready for interaction", accessibilityId);
            }

        } catch (Exception e) {
            logger.error("Failed to ensure element '{}' is scrolled into view: {} - will try direct click", accessibilityId, e.getMessage());
            ScreenshotUtils.takeScreenshot("scroll_failed_" + accessibilityId);
        }
    }

    private boolean isElementDisplayedAndClickable(
            String accessibilityId
    ) {

        try {

            List<WebElement> elements =
                    DriverManager.safelyFindElements(
                            AppiumBy.accessibilityId(
                                    accessibilityId
                            )
                    );

            if (
                    (elements == null || elements.isEmpty())
                            &&
                            DriverManager.getCurrentPlatform()
                                    .equalsIgnoreCase("android")
            ) {

                elements =
                        DriverManager.safelyFindElements(
                                AppiumBy.id(accessibilityId)
                        );
            }

            if (
                    elements != null
                            &&
                            !elements.isEmpty()
            ) {

                WebElement element =
                        elements.get(0);

                return element.isDisplayed()
                        &&
                        element.isEnabled();
            }

            return false;

        } catch (Exception e) {

            logger.debug(
                    "Error checking element visibility/clickability: {}",
                    e.getMessage()
            );

            return false;
        }
    }

    protected void scrollDownUntilVisible(String accessibilityId) {
        logger.info(
                "Scrolling until element visible: {}",
                accessibilityId
        );

        org.openqa.selenium.Dimension size =
                getScreenSize();

        int startX = size.width / 2;
        int startY = (int) (size.height * 0.85);
        int endY = (int) (size.height * 0.25);
        for (int i = 0; i < 5; i++) {

            // CHECK TRƯỚC
            if (isElementDisplayedByAccessibilityId(
                    accessibilityId
            )) {

                logger.info(
                        "Element '{}' visible after {} scrolls",
                        accessibilityId,
                        i
                );

                return;
            }
            logger.info(
                    "Swipe up attempt {}",
                    i + 1
            );

            // SWIPE UP
            gestureUtils.swipe(
                    startX,
                    startY,
                    startX,
                    endY,
                    250
            );

            // WAIT RN RENDER
            gestureUtils.waitForGestureAnimation();

            waitFor(1);
        }

        ScreenshotUtils.takeScreenshot(
                "scroll_failed_" + accessibilityId
        );

        throw new RuntimeException(
                "Could not find element after scrolling: "
                        + accessibilityId
        );
    }

    protected List<WebElement> findElementsByXPath(
            String xpath
    ) {

        logger.debug(
                "Finding elements by XPath: {}",
                xpath
        );

        try {

            return DriverManager.safelyFindElements(
                    By.xpath(xpath)
            );

        } catch (Exception e) {

            logger.warn(
                    "Failed to find elements by XPath: {}",
                    e.getMessage()
            );

            return java.util.Collections.emptyList();
        }
    }

    public List<WebElement> findElementsByAccessibilityIdPrefix(
            String prefix
    ) {

        try {

            return driver.findElements(
                    AppiumBy.androidUIAutomator(
                            "new UiSelector().descriptionContains(\""
                                    + prefix +
                                    "\")"
                    )
            );

        } catch (Exception e) {

            logger.warn(
                    "Failed finding elements by accessibility prefix {}: {}",
                    prefix,
                    e.getMessage()
            );

            return List.of();
        }
    }

    public abstract boolean isDisplayed();

    public abstract String getPageTitle();
}
