package com.modis.utils;

import com.modis.constants.AppConstants;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.List;

/**
 * Utility class for handling waits and element synchronization
 * Provides explicit wait methods for various element states
 */
public class WaitUtils {
    
    private static final Logger logger = LoggerUtil.getLogger(WaitUtils.class);
    private final WebDriverWait wait;
    private final WebDriverWait shortWait;
    private final WebDriverWait longWait;
    private final AppiumDriver driver;
    
    public WaitUtils(AppiumDriver driver) {
        this.driver = driver;
        this.wait = buildWait(Duration.ofSeconds(AppConstants.EXPLICIT_WAIT));
        this.shortWait = buildWait(Duration.ofSeconds(AppConstants.SHORT_WAIT));
        this.longWait = buildWait(Duration.ofSeconds(AppConstants.LONG_WAIT));
    }

    private WebDriverWait buildWait(Duration timeout) {
        WebDriverWait w = new WebDriverWait(driver, timeout);
        // React Native often re-renders and can trigger StaleElementReferenceException mid-wait.
        // Ignoring it keeps the wait resilient without increasing overall timeout.
        w.ignoring(StaleElementReferenceException.class);
        return w;
    }
    
    // ==================== ELEMENT PRESENCE WAITS ====================
    
    /**
     * Wait for element to be present in DOM
     * @param locator Element locator
     * @return WebElement
     */
    public WebElement waitForElementToBePresent(By locator) {
        return waitForElementToBePresent(locator, AppConstants.EXPLICIT_WAIT);
    }
    
    /**
     * Wait for element to be present in DOM with custom timeout
     * @param locator Element locator
     * @param timeoutSeconds Custom timeout in seconds
     * @return WebElement
     */
    public WebElement waitForElementToBePresent(By locator, int timeoutSeconds) {
        try {
            logger.debug("Waiting for element to be present: {}", locator);
            WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            WebElement element = customWait.until(ExpectedConditions.presenceOfElementLocated(locator));
            logger.debug("Element found: {}", locator);
            return element;
        } catch (TimeoutException e) {
            logger.error("Element not found within {} seconds: {}", timeoutSeconds, locator);
            throw new TimeoutException("Element not present: " + locator, e);
        }
    }
    
    /**
     * Wait for elements to be present in DOM
     * @param locator Element locator
     * @return List of WebElements
     */
    public List<WebElement> waitForElementsToBePresent(By locator) {
        try {
            logger.debug("Waiting for elements to be present: {}", locator);
            List<WebElement> elements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
            logger.debug("Found {} elements: {}", elements.size(), locator);
            return elements;
        } catch (TimeoutException e) {
            logger.error("Elements not found: {}", locator);
            throw new TimeoutException("Elements not present: " + locator, e);
        }
    }
    
    // ==================== ELEMENT VISIBILITY WAITS ====================
    
    /**
     * Wait for element to be visible
     * @param locator Element locator
     * @return WebElement
     */
    public WebElement waitForElementToBeVisible(By locator) {
        return waitForElementToBeVisible(locator, AppConstants.EXPLICIT_WAIT);
    }

    /**
     * Wait for element to be visible with custom timeout (seconds).
     * Use this for multi-strategy locators to keep overall time bounded.
     */
    public WebElement waitForElementToBeVisible(By locator, int timeoutSeconds) {
        try {
            logger.debug("Waiting for element to be visible ({}s): {}", timeoutSeconds, locator);
            WebDriverWait customWait = buildWait(Duration.ofSeconds(timeoutSeconds));
            WebElement element = customWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            logger.debug("Element is visible: {}", locator);
            return element;
        } catch (TimeoutException e) {
            logger.error("Element not visible within {} seconds: {}", timeoutSeconds, locator);
            throw new TimeoutException("Element not visible: " + locator, e);
        }
    }
    
    /**
     * Wait for element to be visible with short timeout
     * @param locator Element locator
     * @return WebElement
     */
    public WebElement waitForElementToBeVisibleShort(By locator) {
        try {
            logger.debug("Waiting for element to be visible (short): {}", locator);
            return shortWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            logger.debug("Element not visible within short timeout: {}", locator);
            throw new TimeoutException("Element not visible (short wait): " + locator, e);
        }
    }
    
    /**
     * Wait for element to be visible with long timeout
     * @param locator Element locator
     * @return WebElement
     */
    public WebElement waitForElementToBeVisibleLong(By locator) {
        try {
            logger.debug("Waiting for element to be visible (long): {}", locator);
            return longWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            logger.error("Element not visible within long timeout: {}", locator);
            throw new TimeoutException("Element not visible (long wait): " + locator, e);
        }
    }
    
    /**
     * Wait for element to be visible
     * @param element WebElement
     * @return WebElement
     */
    public WebElement waitForElementToBeVisible(WebElement element) {
        try {
            logger.debug("Waiting for element to be visible");
            return wait.until(ExpectedConditions.visibilityOf(element));
        } catch (TimeoutException e) {
            logger.error("Element not visible");
            throw new TimeoutException("Element not visible", e);
        }
    }
    
    /**
     * Wait for elements to be visible
     * @param locator Element locator
     * @return List of WebElements
     */
    public List<WebElement> waitForElementsToBeVisible(By locator) {
        try {
            logger.debug("Waiting for elements to be visible: {}", locator);
            List<WebElement> elements = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
            logger.debug("Found {} visible elements: {}", elements.size(), locator);
            return elements;
        } catch (TimeoutException e) {
            logger.error("Elements not visible: {}", locator);
            throw new TimeoutException("Elements not visible: " + locator, e);
        }
    }
    
    // ==================== ELEMENT CLICKABILITY WAITS ====================
    
    /**
     * Wait for element to be clickable
     * @param locator Element locator
     * @return WebElement
     */
    public WebElement waitForElementToBeClickable(By locator) {
        return waitForElementToBeClickable(locator, AppConstants.EXPLICIT_WAIT);
    }

    public WebElement waitForElementToBeClickable(By locator, int timeoutSeconds) {
        try {
            logger.debug("Waiting for element to be clickable ({}s): {}", timeoutSeconds, locator);
            WebDriverWait customWait = buildWait(Duration.ofSeconds(timeoutSeconds));
            WebElement element = customWait.until(ExpectedConditions.elementToBeClickable(locator));
            logger.debug("Element is clickable: {}", locator);
            return element;
        } catch (TimeoutException e) {
            logger.error("Element not clickable within {} seconds: {}", timeoutSeconds, locator);
            throw new TimeoutException("Element not clickable: " + locator, e);
        }
    }
    
    /**
     * Wait for element to be clickable
     * @param element WebElement
     * @return WebElement
     */
    public WebElement waitForElementToBeClickable(WebElement element) {
        try {
            logger.debug("Waiting for element to be clickable");
            return wait.until(ExpectedConditions.elementToBeClickable(element));
        } catch (TimeoutException e) {
            logger.error("Element not clickable");
            throw new TimeoutException("Element not clickable", e);
        }
    }
    
    // ==================== ELEMENT DISAPPEARANCE WAITS ====================
    
    /**
     * Wait for element to disappear/become invisible
     * @param locator Element locator
     */
    public void waitForElementToDisappear(By locator) {
        try {
            logger.debug("Waiting for element to disappear: {}", locator);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
            logger.debug("Element disappeared: {}", locator);
        } catch (TimeoutException e) {
            logger.warn("Element still visible after timeout: {}", locator);
            // Don't throw exception for disappearance waits as element might already be gone
        }
    }
    
    /**
     * Wait for element to disappear with short timeout
     * @param locator Element locator
     */
    public void waitForElementToDisappearShort(By locator) {
        try {
            logger.debug("Waiting for element to disappear (short): {}", locator);
            shortWait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            logger.debug("Element still visible after short timeout: {}", locator);
        }
    }
    
    /**
     * Wait for element to disappear
     * @param element WebElement
     */
    public void waitForElementToDisappear(WebElement element) {
        try {
            logger.debug("Waiting for element to disappear");
            wait.until(ExpectedConditions.invisibilityOf(element));
        } catch (TimeoutException e) {
            logger.debug("Element still visible after timeout");
        }
    }
    
    // ==================== TEXT CONTENT WAITS ====================
    
    /**
     * Wait for text to be present in element
     * @param locator Element locator
     * @param text Expected text
     */
    public void waitForTextToBePresentInElement(By locator, String text) {
        try {
            logger.debug("Waiting for text '{}' in element: {}", text, locator);
            wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
            logger.debug("Text '{}' found in element: {}", text, locator);
        } catch (TimeoutException e) {
            logger.error("Text '{}' not found in element: {}", text, locator);
            throw new TimeoutException("Text not present in element: " + text, e);
        }
    }
    
    /**
     * Wait for text to be present in element value
     * @param locator Element locator
     * @param text Expected text
     */
    public void waitForTextToBePresentInElementValue(By locator, String text) {
        try {
            logger.debug("Waiting for text '{}' in element value: {}", text, locator);
            wait.until(ExpectedConditions.textToBePresentInElementValue(locator, text));
            logger.debug("Text '{}' found in element value: {}", text, locator);
        } catch (TimeoutException e) {
            logger.error("Text '{}' not found in element value: {}", text, locator);
            throw new TimeoutException("Text not present in element value: " + text, e);
        }
    }
    
    /**
     * Wait for element to contain specific text
     * @param element WebElement
     * @param text Expected text
     */
    public void waitForTextToBePresentInElement(WebElement element, String text) {
        try {
            logger.debug("Waiting for text '{}' in element", text);
            wait.until(ExpectedConditions.textToBePresentInElement(element, text));
        } catch (TimeoutException e) {
            logger.error("Text '{}' not found in element", text);
            throw new TimeoutException("Text not present in element: " + text, e);
        }
    }
    
    // ==================== ATTRIBUTE WAITS ====================
    
    /**
     * Wait for element attribute to contain specific value
     * @param locator Element locator
     * @param attribute Attribute name
     * @param value Expected value
     */
    public void waitForAttributeToContain(By locator, String attribute, String value) {
        try {
            logger.debug("Waiting for attribute '{}' to contain '{}' in element: {}", attribute, value, locator);
            wait.until(ExpectedConditions.attributeContains(locator, attribute, value));
            logger.debug("Attribute '{}' contains '{}' in element: {}", attribute, value, locator);
        } catch (TimeoutException e) {
            logger.error("Attribute '{}' does not contain '{}' in element: {}", attribute, value, locator);
            throw new TimeoutException("Attribute does not contain expected value", e);
        }
    }
    
    /**
     * Wait for element attribute to be specific value
     * @param locator Element locator
     * @param attribute Attribute name
     * @param value Expected value
     */
    public void waitForAttributeToBe(By locator, String attribute, String value) {
        try {
            logger.debug("Waiting for attribute '{}' to be '{}' in element: {}", attribute, value, locator);
            wait.until(ExpectedConditions.attributeToBe(locator, attribute, value));
            logger.debug("Attribute '{}' is '{}' in element: {}", attribute, value, locator);
        } catch (TimeoutException e) {
            logger.error("Attribute '{}' is not '{}' in element: {}", attribute, value, locator);
            throw new TimeoutException("Attribute is not expected value", e);
        }
    }
    
    // ==================== SELECTION STATE WAITS ====================
    
    /**
     * Wait for element to be selected
     * @param locator Element locator
     */
    public void waitForElementToBeSelected(By locator) {
        try {
            logger.debug("Waiting for element to be selected: {}", locator);
            wait.until(ExpectedConditions.elementToBeSelected(locator));
            logger.debug("Element is selected: {}", locator);
        } catch (TimeoutException e) {
            logger.error("Element not selected: {}", locator);
            throw new TimeoutException("Element not selected: " + locator, e);
        }
    }
    
    /**
     * Wait for element selection state
     * @param locator Element locator
     * @param selected Expected selection state
     */
    public void waitForElementSelectionStateToBe(By locator, boolean selected) {
        try {
            logger.debug("Waiting for element selection state to be {}: {}", selected, locator);
            wait.until(ExpectedConditions.elementSelectionStateToBe(locator, selected));
            logger.debug("Element selection state is {}: {}", selected, locator);
        } catch (TimeoutException e) {
            logger.error("Element selection state is not {}: {}", selected, locator);
            throw new TimeoutException("Element selection state not as expected", e);
        }
    }
    
    // ==================== FRAME AND ALERT WAITS ====================
    
    /**
     * Wait for alert to be present
     */
    public void waitForAlertToBePresent() {
        try {
            logger.debug("Waiting for alert to be present");
            wait.until(ExpectedConditions.alertIsPresent());
            logger.debug("Alert is present");
        } catch (TimeoutException e) {
            logger.error("Alert not present");
            throw new TimeoutException("Alert not present", e);
        }
    }
    
    // ==================== CUSTOM CONDITIONS ====================
    
    /**
     * Wait for page to load (no loading indicators visible)
     */
    public void waitForPageToLoad() {
        try {
            logger.debug("Waiting for page to load");
            // Wait for common loading indicators to disappear
            waitForElementToDisappearShort(By.className("loading"));
            waitForElementToDisappearShort(By.className("spinner"));
            waitForElementToDisappearShort(By.xpath("//*[contains(@class,'loading') or contains(@class,'spinner')]"));
            logger.debug("Page loaded");
        } catch (Exception e) {
            logger.debug("Page load wait completed (no loading indicators found)");
        }
    }
    
    /**
     * Wait for network request to complete (mobile specific)
     */
    public void waitForNetworkIdle() {
        // Prefer explicit waits for UI signals.
        // This is a last-resort tiny backoff (configurable) to avoid flakiness on slow RN devices.
        int seconds = ConfigReader.getIntProperty("network.wait", 0);
        if (seconds <= 0) return;
        safeSleepSeconds(seconds, "network idle");
    }
    
    /**
     * Wait for animation to complete
     */
    public void waitForAnimation() {
        int seconds = ConfigReader.getIntProperty("animation.wait", 0);
        if (seconds <= 0) return;
        safeSleepSeconds(seconds, "animation");
    }

    private void safeSleepSeconds(int seconds, String reason) {
        try {
            logger.debug("Sleeping {}s for {}", seconds, reason);
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Sleep interrupted ({})", reason, e);
        }
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Check if element exists without waiting
     * @param locator Element locator
     * @return true if element exists, false otherwise
     */
    public boolean isElementPresent(By locator) {
        try {
            List<WebElement> elements = driver.findElements(locator);
            return elements != null && !elements.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if element is visible without waiting
     * @param locator Element locator
     * @return true if element is visible, false otherwise
     */
    public boolean isElementVisible(By locator) {
        try {
            List<WebElement> elements = driver.findElements(locator);
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
    
    /**
     * Wait with custom condition and timeout
     * @param condition Custom expected condition
     * @param timeoutSeconds Timeout in seconds
     * @param <T> Return type
     * @return Result of condition
     */
    public <T> T waitForCondition(org.openqa.selenium.support.ui.ExpectedCondition<T> condition, int timeoutSeconds) {
        try {
            WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            customWait.pollingEvery(Duration.ofMillis(250));
            return customWait.until(condition);
        } catch (TimeoutException e) {
            logger.debug("Custom condition not met within {} seconds", timeoutSeconds);
            throw new TimeoutException("Custom condition timeout", e);
        }
    }
    
    /**
     * Fluent wait for element with polling
     * @param locator Element locator
     * @param timeoutSeconds Timeout in seconds
     * @param pollingSeconds Polling interval in seconds
     * @return WebElement
     */
    public WebElement fluentWaitForElement(By locator, int timeoutSeconds, int pollingSeconds) {
        try {
            logger.debug("Fluent wait for element: {}", locator);
            org.openqa.selenium.support.ui.FluentWait<AppiumDriver> fluentWait = 
                new org.openqa.selenium.support.ui.FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(timeoutSeconds))
                    .pollingEvery(Duration.ofSeconds(pollingSeconds))
                    .ignoring(org.openqa.selenium.NoSuchElementException.class);
            
            return fluentWait.until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (TimeoutException e) {
            logger.error("Fluent wait timeout for element: {}", locator);
            throw new TimeoutException("Fluent wait timeout: " + locator, e);
        }
    }
}
