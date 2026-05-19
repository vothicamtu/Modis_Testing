package com.modis.base;

import com.modis.constants.AppConstants;
import com.modis.drivers.DriverManager;
import com.modis.utils.GestureUtils;
import com.modis.utils.LoggerUtil;
import com.modis.utils.ScreenshotUtils;
import com.modis.utils.WaitUtils;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.List;

/**
 * Base Page class for all page objects
 * Contains common methods and utilities for page interactions
 */
public abstract class BasePage {
    
    protected final Logger logger = LoggerUtil.getLogger(this.getClass());
    protected final AppiumDriver driver;
    protected final WaitUtils waitUtils;
    protected final GestureUtils gestureUtils;
    protected final Dimension screenSize;
    
    public BasePage() {
        this.driver = DriverManager.getDriver();
        this.waitUtils = new WaitUtils(driver);
        this.gestureUtils = new GestureUtils(driver);
        this.screenSize = driver.manage().window().getSize();
        PageFactory.initElements(new AppiumFieldDecorator(driver, Duration.ofSeconds(AppConstants.ELEMENT_WAIT_TIMEOUT)), this);
        logger.info("Initialized page: {}", this.getClass().getSimpleName());
    }
    
    // ==================== ELEMENT FINDING METHODS ====================
    
    /**
     * Find element by accessibility ID (preferred method)
     * @param accessibilityId The accessibility ID
     * @return WebElement
     */
    protected WebElement findByAccessibilityId(String accessibilityId) {
        logger.debug("Finding element by accessibility ID: {}", accessibilityId);
        return waitUtils.waitForElementToBeVisible(AppiumBy.accessibilityId(accessibilityId));
    }
    
    /**
     * Find elements by accessibility ID
     * @param accessibilityId The accessibility ID
     * @return List of WebElements
     */
    protected List<WebElement> findElementsByAccessibilityId(String accessibilityId) {
        logger.debug("Finding elements by accessibility ID: {}", accessibilityId);
        return waitUtils.waitForElementsToBeVisible(AppiumBy.accessibilityId(accessibilityId));
    }
    
    /**
     * Find element by XPath (use sparingly)
     * @param xpath The XPath expression
     * @return WebElement
     */
    protected WebElement findByXPath(String xpath) {
        logger.debug("Finding element by XPath: {}", xpath);
        return waitUtils.waitForElementToBeVisible(By.xpath(xpath));
    }
    
    /**
     * Find element by ID
     * @param id The element ID
     * @return WebElement
     */
    protected WebElement findById(String id) {
        logger.debug("Finding element by ID: {}", id);
        return waitUtils.waitForElementToBeVisible(By.id(id));
    }
    
    /**
     * Find element by class name
     * @param className The class name
     * @return WebElement
     */
    protected WebElement findByClassName(String className) {
        logger.debug("Finding element by class name: {}", className);
        return waitUtils.waitForElementToBeVisible(By.className(className));
    }
    
    /**
     * Find element by text content
     * @param text The text to search for
     * @return WebElement
     */
    protected WebElement findByText(String text) {
        logger.debug("Finding element by text: {}", text);
        String xpath = String.format("//*[@text='%s' or @content-desc='%s']", text, text);
        return waitUtils.waitForElementToBeVisible(By.xpath(xpath));
    }
    
    /**
     * Find element by partial text content
     * @param partialText The partial text to search for
     * @return WebElement
     */
    protected WebElement findByPartialText(String partialText) {
        logger.debug("Finding element by partial text: {}", partialText);
        String xpath = String.format("//*[contains(@text,'%s') or contains(@content-desc,'%s')]", partialText, partialText);
        return waitUtils.waitForElementToBeVisible(By.xpath(xpath));
    }
    
    // ==================== ELEMENT INTERACTION METHODS ====================
    
    /**
     * Click on element with retry mechanism
     * @param element The element to click
     */
    protected void clickElement(WebElement element) {
        try {
            waitUtils.waitForElementToBeClickable(element);
            element.click();
            logger.debug("Clicked element successfully");
        } catch (Exception e) {
            logger.warn("Standard click failed, trying tap gesture", e);
            gestureUtils.tapElement(element);
        }
    }
    
    /**
     * Click on element by accessibility ID
     * @param accessibilityId The accessibility ID
     */
    protected void clickByAccessibilityId(String accessibilityId) {
        WebElement element = findByAccessibilityId(accessibilityId);
        clickElement(element);
        logger.info("Clicked element with accessibility ID: {}", accessibilityId);
    }
    
    /**
     * Enter text into input field
     * @param element The input element
     * @param text The text to enter
     */
    protected void enterText(WebElement element, String text) {
        try {
            waitUtils.waitForElementToBeVisible(element);
            element.clear();
            element.sendKeys(text);
            logger.debug("Entered text: {}", text);
        } catch (Exception e) {
            logger.error("Failed to enter text: {}", text, e);
            throw new RuntimeException("Text input failed", e);
        }
    }
    
    /**
     * Enter text by accessibility ID
     * @param accessibilityId The accessibility ID
     * @param text The text to enter
     */
    protected void enterTextByAccessibilityId(String accessibilityId, String text) {
        WebElement element = findByAccessibilityId(accessibilityId);
        enterText(element, text);
        logger.info("Entered text '{}' into element with accessibility ID: {}", text, accessibilityId);
    }
    
    /**
     * Get text from element
     * @param element The element
     * @return Text content
     */
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
    
    /**
     * Get text by accessibility ID
     * @param accessibilityId The accessibility ID
     * @return Text content
     */
    protected String getTextByAccessibilityId(String accessibilityId) {
        WebElement element = findByAccessibilityId(accessibilityId);
        return getText(element);
    }
    
    /**
     * Check if element is displayed
     * @param element The element
     * @return true if displayed, false otherwise
     */
    protected boolean isElementDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            logger.debug("Element not displayed", e);
            return false;
        }
    }
    
    /**
     * Check if element exists by accessibility ID
     * @param accessibilityId The accessibility ID
     * @return true if exists, false otherwise
     */
    protected boolean isElementDisplayedByAccessibilityId(String accessibilityId) {
        try {
            WebElement element = waitUtils.waitForElementToBePresent(AppiumBy.accessibilityId(accessibilityId), 5);
            return isElementDisplayed(element);
        } catch (Exception e) {
            logger.debug("Element with accessibility ID '{}' not displayed", accessibilityId);
            return false;
        }
    }
    
    /**
     * Check if element is enabled
     * @param element The element
     * @return true if enabled, false otherwise
     */
    protected boolean isElementEnabled(WebElement element) {
        try {
            waitUtils.waitForElementToBeVisible(element);
            return element.isEnabled();
        } catch (Exception e) {
            logger.debug("Element not enabled", e);
            return false;
        }
    }
    
    // ==================== SCROLL GESTURES ====================
    
    /**
     * Scroll to element by text
     * @param text Text to scroll to
     * @return WebElement if found
     */
    protected WebElement scrollToElementByText(String text) {
        logger.info("Scrolling to element with text: {}", text);
        return gestureUtils.scrollToElementByText(text);
    }
    
    /**
     * Scroll to element by accessibility ID
     * @param accessibilityId The accessibility ID
     * @return WebElement if found
     */
    protected WebElement scrollToElementByAccessibilityId(String accessibilityId) {
        logger.info("Scrolling to element with accessibility ID: {}", accessibilityId);
        return gestureUtils.scrollToElementByAccessibilityId(accessibilityId);
    }
    
    /**
     * Scroll down on screen
     */
    protected void scrollDownBase() {
        gestureUtils.scrollDown();
        logger.info("Scrolled down");
    }
    
    /**
     * Scroll up on screen
     */
    protected void scrollUpBase() {
        gestureUtils.scrollUp();
        logger.info("Scrolled up");
    }
    
    /**
     * Scroll to top of screen
     */
    protected void scrollToTopBase() {
        gestureUtils.scrollToTop();
        logger.info("Scrolled to top");
    }
    
    /**
     * Scroll to bottom of screen
     */
    protected void scrollToBottomBase() {
        gestureUtils.scrollToBottom();
        logger.info("Scrolled to bottom");
    }
    
    /**
     * Scroll within a specific element
     * @param scrollableElement The scrollable container element
     * @param direction Direction to scroll (up/down)
     */
    protected void scrollInElement(WebElement scrollableElement, String direction) {
        gestureUtils.scrollInElement(scrollableElement, direction);
        logger.debug("Scrolled {} in element", direction);
    }
    
    /**
     * Pull to refresh gesture
     */
    protected void pullToRefresh() {
        gestureUtils.pullToRefresh();
        logger.info("Performed pull to refresh");
    }
    
    // ==================== GESTURE METHODS ====================
    
    /**
     * Swipe left on the screen
     */
    protected void swipeLeft() {
        gestureUtils.swipeLeft();
        logger.info("Performed swipe left gesture");
    }
    
    /**
     * Swipe right on the screen
     */
    protected void swipeRight() {
        gestureUtils.swipeRight();
        logger.info("Performed swipe right gesture");
    }
    
    /**
     * Swipe up on the screen
     */
    protected void swipeUp() {
        gestureUtils.swipeUp();
        logger.info("Performed swipe up gesture");
    }
    
    /**
     * Swipe down on the screen
     */
    protected void swipeDown() {
        gestureUtils.swipeDown();
        logger.info("Performed swipe down gesture");
    }
    
    /**
     * Swipe on specific element
     * @param element Element to swipe on
     * @param direction Direction (left, right, up, down)
     */
    protected void swipeOnElement(WebElement element, String direction) {
        gestureUtils.swipeOnElement(element, direction);
        logger.info("Performed swipe {} on element", direction);
    }
    
    /**
     * Long press on element
     * @param element Element to long press
     * @param durationMs Duration in milliseconds
     */
    protected void longPressElement(WebElement element, int durationMs) {
        gestureUtils.longPressElement(element, durationMs);
        logger.info("Long pressed element for {}ms", durationMs);
    }
    
    /**
     * Tap at specific coordinates
     * @param x X coordinate
     * @param y Y coordinate
     */
    protected void tapAtCoordinates(int x, int y) {
        gestureUtils.tapAtCoordinates(x, y);
        logger.debug("Tapped at coordinates: ({}, {})", x, y);
    }
    
    /**
     * Tap on element
     * @param element Element to tap
     */
    protected void tapElement(WebElement element) {
        gestureUtils.tapElement(element);
        logger.debug("Tapped element");
    }
    
    /**
     * Pinch out (zoom in) on element
     * @param element Element to pinch on
     */
    protected void pinchOut(WebElement element) {
        gestureUtils.pinchOut(element);
        logger.info("Performed pinch out on element");
    }
    
    /**
     * Pinch in (zoom out) on element
     * @param element Element to pinch on
     */
    protected void pinchIn(WebElement element) {
        gestureUtils.pinchIn(element);
        logger.info("Performed pinch in on element");
    }
    
    // ==================== WAIT METHODS ====================
    
    /**
     * Wait for element to be visible
     * @param accessibilityId The accessibility ID
     * @return WebElement
     */
    protected WebElement waitForElementVisible(String accessibilityId) {
        return waitUtils.waitForElementToBeVisible(AppiumBy.accessibilityId(accessibilityId));
    }
    
    /**
     * Wait for element to be clickable
     * @param accessibilityId The accessibility ID
     * @return WebElement
     */
    protected WebElement waitForElementClickable(String accessibilityId) {
        return waitUtils.waitForElementToBeClickable(AppiumBy.accessibilityId(accessibilityId));
    }
    
    /**
     * Wait for element to disappear
     * @param accessibilityId The accessibility ID
     */
    protected void waitForElementToDisappear(String accessibilityId) {
        waitUtils.waitForElementToDisappear(AppiumBy.accessibilityId(accessibilityId));
    }
    
    /**
     * Wait for text to be present in element
     * @param accessibilityId The accessibility ID
     * @param text The expected text
     */
    protected void waitForTextInElement(String accessibilityId, String text) {
        waitUtils.waitForTextToBePresentInElement(AppiumBy.accessibilityId(accessibilityId), text);
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Wait for animation to complete
     */
    protected void waitForAnimation() {
        waitUtils.waitForAnimation();
        logger.debug("Animation wait completed");
    }
    
    /**
     * Take screenshot
     * @param screenshotName The name for the screenshot
     */
    protected void takeScreenshot(String screenshotName) {
        ScreenshotUtils.takeScreenshot(screenshotName);
        logger.info("Screenshot taken: {}", screenshotName);
    }
    
    /**
     * Wait for specified duration
     * @param seconds Duration in seconds
     */
    protected void waitFor(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
            logger.debug("Waited for {} seconds", seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Wait interrupted", e);
        }
    }
    
    /**
     * Hide keyboard if visible
     */
    protected void hideKeyboard() {
        try {
            driver.executeScript("mobile: hideKeyboard");
            logger.debug("Keyboard hidden using mobile: hideKeyboard");
        } catch (Exception e) {
            logger.debug("Keyboard not visible or failed to hide", e);
        }
    }
    
    /**
     * Get current activity (Android only)
     * @return Current activity name
     */
    protected String getCurrentActivity() {
        try {
            if (DriverManager.getCurrentPlatform().equalsIgnoreCase("android")) {
                return (String) driver.executeScript("mobile: getCurrentPackage");
            }
        } catch (Exception e) {
            logger.debug("Failed to get current activity", e);
        }
        return "";
    }
    
    /**
     * Verify page is loaded by checking key element
     * @param keyElementAccessibilityId The accessibility ID of key element
     * @return true if page is loaded, false otherwise
     */
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
    
    /**
     * Refresh the current page/screen
     */
    protected void refreshPage() {
        logger.info("Refreshing page");
        gestureUtils.pullToRefresh();
    }
    
    /**
     * Go back using device back button (Android)
     */
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
    
    /**
     * Abstract method to verify page is displayed
     * Must be implemented by each page class
     * @return true if page is displayed, false otherwise
     */
    public abstract boolean isDisplayed();
    
    /**
     * Abstract method to get page title/identifier
     * Must be implemented by each page class
     * @return Page title or identifier
     */
    public abstract String getPageTitle();
}