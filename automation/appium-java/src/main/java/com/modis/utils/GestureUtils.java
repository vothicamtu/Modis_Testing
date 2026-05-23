package com.modis.utils;

import com.modis.constants.AppConstants;
import com.modis.drivers.DriverManager;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for handling gestures and touch actions
 * Provides methods for swipe, scroll, tap, pinch, and other mobile gestures
 */
public class GestureUtils {
    
    private static final Logger logger = LoggerUtil.getLogger(GestureUtils.class);
    private final AppiumDriver driver;
    private volatile Dimension cachedScreenSize;
    
    // Gesture constants
    private static final double SWIPE_START_PERCENTAGE = 0.8;
    private static final double SWIPE_END_PERCENTAGE = 0.2;
    private static final double SWIPE_ANCHOR_PERCENTAGE = 0.5;
    private static final int SWIPE_DURATION_MS = 1000;
    private static final int TAP_DURATION_MS = 100;
    private static final int SCROLL_DURATION_MS = 800;
    private static final int MAX_SCROLL_ATTEMPTS = 15;
    
    public GestureUtils(AppiumDriver driver) {
        this.driver = driver;
        // IMPORTANT:
        // Do NOT call driver.manage().window().getSize() in constructor.
        // On some real devices / UiAutomator2 states, this command can hang / "socket hang up",
        // causing PageObject construction to freeze the whole test.
    }

    /**
     * Get screen size with a SAFE strategy:
     * 1) Prefer capabilities (deviceScreenSize / viewportRect) - zero extra Appium calls
     * 2) Fallback to driver.manage().window().getSize() only if needed
     */
    public Dimension getScreenSize() {
        if (cachedScreenSize != null) {
            return cachedScreenSize;
        }

        // 1) Try capabilities first (fast, stable)
        try {
            Object cap = driver.getCapabilities().getCapability("appium:deviceScreenSize");
            if (cap == null) cap = driver.getCapabilities().getCapability("deviceScreenSize");
            if (cap instanceof String) {
                String s = (String) cap;
                String trimmed = s.trim();
                if (trimmed.matches("\\d+x\\d+")) {
                    String[] parts = trimmed.split("x");
                    cachedScreenSize = new Dimension(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                    logger.debug("Screen size from capabilities deviceScreenSize: {}x{}", cachedScreenSize.width, cachedScreenSize.height);
                    return cachedScreenSize;
                }
            }
        } catch (Exception e) {
            logger.debug("Could not parse deviceScreenSize capability", e);
        }

        // 2) Try viewportRect (width/height) next
        try {
            Object viewportRect = driver.getCapabilities().getCapability("appium:viewportRect");
            if (viewportRect instanceof java.util.Map) {
                java.util.Map map = (java.util.Map) viewportRect;
                Object w = map.get("width");
                Object h = map.get("height");
                if (w != null && h != null) {
                    cachedScreenSize = new Dimension(Integer.parseInt(w.toString()), Integer.parseInt(h.toString()));
                    logger.debug("Screen size from capabilities viewportRect: {}x{}", cachedScreenSize.width, cachedScreenSize.height);
                    return cachedScreenSize;
                }
            }
        } catch (Exception e) {
            logger.debug("Could not parse viewportRect capability", e);
        }

        // 3) LAST resort - call Appium (can hang in some conditions, so keep as final fallback)
        try {
            cachedScreenSize = driver.manage().window().getSize();
            logger.debug("Screen size from driver.manage().window().getSize(): {}x{}", cachedScreenSize.width, cachedScreenSize.height);
            return cachedScreenSize;
        } catch (Exception e) {
            logger.warn("Failed to get screen size, using safe default 1080x1920", e);
            cachedScreenSize = new Dimension(1080, 1920);
            return cachedScreenSize;
        }
    }
    
    // ==================== BASIC GESTURES ====================
    
    /**
     * Tap on element
     * @param element Element to tap
     */
    public void tapElement(WebElement element) {
        try {
            Point location = element.getLocation();
            Dimension size = element.getSize();
            int centerX = location.x + size.width / 2;
            int centerY = location.y + size.height / 2;
            
            tapAtCoordinates(centerX, centerY);
            logger.debug("Tapped element at coordinates: ({}, {})", centerX, centerY);
        } catch (Exception e) {
            logger.error("Failed to tap element", e);
            throw new RuntimeException("Tap gesture failed", e);
        }
    }
    
    /**
     * Tap at specific coordinates
     * @param x X coordinate
     * @param y Y coordinate
     */
    public void tapAtCoordinates(int x, int y) {
        try {
            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            Sequence tap = new Sequence(finger, 1);
            
            tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y));
            tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
            
            driver.perform(Collections.singletonList(tap));
            logger.debug("Tapped at coordinates: ({}, {})", x, y);
        } catch (Exception e) {
            logger.error("Failed to tap at coordinates: ({}, {})", x, y, e);
            throw new RuntimeException("Tap at coordinates failed", e);
        }
    }
    
    /**
     * Long press on element
     * @param element Element to long press
     * @param durationMs Duration in milliseconds
     */
    public void longPressElement(WebElement element, int durationMs) {
        try {
            Point location = element.getLocation();
            Dimension size = element.getSize();
            int centerX = location.x + size.width / 2;
            int centerY = location.y + size.height / 2;
            
            longPressAtCoordinates(centerX, centerY, durationMs);
            logger.debug("Long pressed element for {}ms at coordinates: ({}, {})", durationMs, centerX, centerY);
        } catch (Exception e) {
            logger.error("Failed to long press element", e);
            throw new RuntimeException("Long press gesture failed", e);
        }
    }
    
    /**
     * Long press at specific coordinates
     * @param x X coordinate
     * @param y Y coordinate
     * @param durationMs Duration in milliseconds
     */
    public void longPressAtCoordinates(int x, int y, int durationMs) {
        try {
            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            Sequence longPress = new Sequence(finger, 1);
            
            longPress.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y));
            longPress.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            // Use Thread.sleep instead of createPause for compatibility
            try {
                Thread.sleep(durationMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            longPress.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
            
            driver.perform(Collections.singletonList(longPress));
            logger.debug("Long pressed at coordinates: ({}, {}) for {}ms", x, y, durationMs);
        } catch (Exception e) {
            logger.error("Failed to long press at coordinates: ({}, {})", x, y, e);
            throw new RuntimeException("Long press at coordinates failed", e);
        }
    }
    
    // ==================== SWIPE GESTURES ====================
    
    /**
     * Swipe left on screen
     */
    public void swipeLeft() {
        Dimension screenSize = getScreenSize();
        int startX = (int) (screenSize.width * SWIPE_START_PERCENTAGE);
        int endX = (int) (screenSize.width * SWIPE_END_PERCENTAGE);
        int y = (int) (screenSize.height * SWIPE_ANCHOR_PERCENTAGE);
        
        swipe(startX, y, endX, y, SWIPE_DURATION_MS);
        logger.info("Swiped left from ({}, {}) to ({}, {})", startX, y, endX, y);
    }
    
    /**
     * Swipe right on screen
     */
    public void swipeRight() {
        Dimension screenSize = getScreenSize();
        int startX = (int) (screenSize.width * SWIPE_END_PERCENTAGE);
        int endX = (int) (screenSize.width * SWIPE_START_PERCENTAGE);
        int y = (int) (screenSize.height * SWIPE_ANCHOR_PERCENTAGE);
        
        swipe(startX, y, endX, y, SWIPE_DURATION_MS);
        logger.info("Swiped right from ({}, {}) to ({}, {})", startX, y, endX, y);
    }
    
    /**
     * Swipe up on screen
     */
    public void swipeUp() {
        Dimension screenSize = getScreenSize();
        int x = (int) (screenSize.width * SWIPE_ANCHOR_PERCENTAGE);
        int startY = (int) (screenSize.height * SWIPE_START_PERCENTAGE);
        int endY = (int) (screenSize.height * SWIPE_END_PERCENTAGE);
        
        swipe(x, startY, x, endY, SWIPE_DURATION_MS);
        logger.info("Swiped up from ({}, {}) to ({}, {})", x, startY, x, endY);
    }
    
    /**
     * Swipe down on screen
     */
    public void swipeDown() {
        Dimension screenSize = getScreenSize();
        int x = (int) (screenSize.width * SWIPE_ANCHOR_PERCENTAGE);
        int startY = (int) (screenSize.height * SWIPE_END_PERCENTAGE);
        int endY = (int) (screenSize.height * SWIPE_START_PERCENTAGE);
        
        swipe(x, startY, x, endY, SWIPE_DURATION_MS);
        logger.info("Swiped down from ({}, {}) to ({}, {})", x, startY, x, endY);
    }
    
    /**
     * Swipe on specific element
     * @param element Element to swipe on
     * @param direction Direction (left, right, up, down)
     */
    public void swipeOnElement(WebElement element, String direction) {
        Point location = element.getLocation();
        Dimension size = element.getSize();
        
        int centerX = location.x + size.width / 2;
        int centerY = location.y + size.height / 2;
        int startX, startY, endX, endY;
        
        switch (direction.toLowerCase()) {
            case "left":
                startX = location.x + (int) (size.width * 0.8);
                endX = location.x + (int) (size.width * 0.2);
                startY = endY = centerY;
                break;
            case "right":
                startX = location.x + (int) (size.width * 0.2);
                endX = location.x + (int) (size.width * 0.8);
                startY = endY = centerY;
                break;
            case "up":
                startY = location.y + (int) (size.height * 0.8);
                endY = location.y + (int) (size.height * 0.2);
                startX = endX = centerX;
                break;
            case "down":
                startY = location.y + (int) (size.height * 0.2);
                endY = location.y + (int) (size.height * 0.8);
                startX = endX = centerX;
                break;
            default:
                throw new IllegalArgumentException("Invalid swipe direction: " + direction);
        }
        
        swipe(startX, startY, endX, endY, SWIPE_DURATION_MS);
        logger.info("Swiped {} on element from ({}, {}) to ({}, {})", direction, startX, startY, endX, endY);
    }
    
    /**
     * Generic swipe method
     * @param startX Start X coordinate
     * @param startY Start Y coordinate
     * @param endX End X coordinate
     * @param endY End Y coordinate
     * @param durationMs Duration in milliseconds
     */
    public void swipe(int startX, int startY, int endX, int endY, int durationMs) {
        try {
            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            Sequence swipe = new Sequence(finger, 1);
            
            swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
            swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            swipe.addAction(finger.createPointerMove(Duration.ofMillis(durationMs), PointerInput.Origin.viewport(), endX, endY));
            swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
            
            driver.perform(Collections.singletonList(swipe));
            logger.debug("Swiped from ({}, {}) to ({}, {}) in {}ms", startX, startY, endX, endY, durationMs);
        } catch (Exception e) {
            logger.error("Failed to swipe from ({}, {}) to ({}, {})", startX, startY, endX, endY, e);
            throw new RuntimeException("Swipe gesture failed", e);
        }
    }
    
    // ==================== SCROLL GESTURES ====================
    
    /**
     * Scroll to element by text
     * @param text Text to scroll to
     * @return WebElement if found, null otherwise
     */
    public WebElement scrollToElementByText(String text) {
        logger.info("Scrolling to element with text: {}", text);
        
        for (int attempt = 0; attempt < MAX_SCROLL_ATTEMPTS; attempt++) {
            try {
                List<WebElement> elements = driver.findElements(AppiumBy.xpath(
                        String.format("//*[@text='%s' or @content-desc='%s']", text, text)));
                if (elements != null && !elements.isEmpty() && elements.get(0).isDisplayed()) {
                    logger.info("Found element with text '{}' after {} scroll attempts", text, attempt);
                    return elements.get(0);
                }
            } catch (Exception e) {
                logger.debug("Element with text '{}' not found, attempt {}", text, attempt + 1);
            }
            
            // Scroll down to find element
            scrollDown();
        }
        
        logger.warn("Element with text '{}' not found after {} scroll attempts", text, MAX_SCROLL_ATTEMPTS);
        return null;
    }
    
    /**
     * Scroll to element by accessibility ID
     * @param accessibilityId Accessibility ID to scroll to
     * @return WebElement if found, null otherwise
     */
    public WebElement scrollToElementByAccessibilityId(String accessibilityId) {
        logger.info("Scrolling to element with accessibility ID: {}", accessibilityId);
        
        for (int attempt = 0; attempt < MAX_SCROLL_ATTEMPTS; attempt++) {
            try {
                List<WebElement> elements = DriverManager.safelyFindElements(AppiumBy.accessibilityId(accessibilityId));

                boolean isAndroid = DriverManager.getCurrentPlatform().equalsIgnoreCase("android");
                if (isAndroid && (elements == null || elements.isEmpty())) {
                    elements = DriverManager.safelyFindElements(AppiumBy.id(accessibilityId));
                }

                if (isAndroid && (elements == null || elements.isEmpty())) {
                    String uiSelector = String.format("new UiSelector().resourceIdMatches(\".*:id/%s\")", accessibilityId);
                    elements = DriverManager.safelyFindElements(AppiumBy.androidUIAutomator(uiSelector));
                }

                if (elements != null && !elements.isEmpty()) {
                    for (WebElement el : elements) {
                        try {
                            if (el != null && el.isDisplayed()) {
                                logger.info("Found element with ID/Accessibility ID '{}' after {} scroll attempts", accessibilityId, attempt);
                                return el;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            } catch (Exception e) {
                logger.debug("Element with ID/Accessibility ID '{}' not found, attempt {}", accessibilityId, attempt + 1);
            }
            
            scrollDown();
        }
        
        logger.warn("Element with ID/Accessibility ID '{}' not found after {} scroll attempts", accessibilityId, MAX_SCROLL_ATTEMPTS);
        return null;
    }
    
    /**
     * Scroll down on screen
     */
    public void scrollDown() {
        Dimension screenSize = getScreenSize();
        int x = screenSize.width / 2;
        int startY = (int) (screenSize.height * 0.7);
        int endY = (int) (screenSize.height * 0.3);
        
        swipe(x, startY, x, endY, SCROLL_DURATION_MS);
        logger.debug("Scrolled down");
    }
    
    /**
     * Scroll up on screen
     */
    public void scrollUp() {
        Dimension screenSize = getScreenSize();
        int x = screenSize.width / 2;
        int startY = (int) (screenSize.height * 0.3);
        int endY = (int) (screenSize.height * 0.7);
        
        swipe(x, startY, x, endY, SCROLL_DURATION_MS);
        logger.debug("Scrolled up");
    }
    
    /**
     * Scroll to top of screen
     */
    public void scrollToTop() {
        logger.info("Scrolling to top");
        for (int i = 0; i < 5; i++) {
            scrollUp();
        }
    }
    
    /**
     * Scroll to bottom of screen
     */
    public void scrollToBottom() {
        logger.info("Scrolling to bottom");
        for (int i = 0; i < 10; i++) {
            scrollDown();
        }
    }
    
    /**
     * Scroll within a specific element
     * @param scrollableElement The scrollable container element
     * @param direction Direction to scroll (up/down)
     */
    public void scrollInElement(WebElement scrollableElement, String direction) {
        Point location = scrollableElement.getLocation();
        Dimension size = scrollableElement.getSize();
        
        int centerX = location.x + size.width / 2;
        int startY, endY;
        
        if ("down".equalsIgnoreCase(direction)) {
            startY = location.y + (int) (size.height * 0.7);
            endY = location.y + (int) (size.height * 0.3);
        } else {
            startY = location.y + (int) (size.height * 0.3);
            endY = location.y + (int) (size.height * 0.7);
        }
        
        swipe(centerX, startY, centerX, endY, SCROLL_DURATION_MS);
        logger.debug("Scrolled {} in element", direction);
    }
    
    // ==================== PULL TO REFRESH ====================
    
    /**
     * Pull to refresh gesture
     */
    public void pullToRefresh() {
        logger.info("Performing pull to refresh");
        Dimension screenSize = getScreenSize();
        int x = screenSize.width / 2;
        int startY = (int) (screenSize.height * 0.2);
        int endY = (int) (screenSize.height * 0.6);
        
        swipe(x, startY, x, endY, SWIPE_DURATION_MS);
        
        // Wait for refresh animation
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    // ==================== PINCH AND ZOOM ====================
    
    /**
     * Pinch to zoom out
     * @param element Element to pinch on
     */
    public void pinchOut(WebElement element) {
        Point location = element.getLocation();
        Dimension size = element.getSize();
        
        int centerX = location.x + size.width / 2;
        int centerY = location.y + size.height / 2;
        
        pinchOut(centerX, centerY);
    }
    
    /**
     * Pinch to zoom out at coordinates
     * @param centerX Center X coordinate
     * @param centerY Center Y coordinate
     */
    public void pinchOut(int centerX, int centerY) {
        try {
            PointerInput finger1 = new PointerInput(PointerInput.Kind.TOUCH, "finger1");
            PointerInput finger2 = new PointerInput(PointerInput.Kind.TOUCH, "finger2");
            
            Sequence pinch1 = new Sequence(finger1, 1);
            Sequence pinch2 = new Sequence(finger2, 1);
            
            int offset = 50;
            
            // Finger 1 moves from center-offset to center-offset*3
            pinch1.addAction(finger1.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), centerX - offset, centerY));
            pinch1.addAction(finger1.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            pinch1.addAction(finger1.createPointerMove(Duration.ofMillis(1000), PointerInput.Origin.viewport(), centerX - offset * 3, centerY));
            pinch1.addAction(finger1.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
            
            // Finger 2 moves from center+offset to center+offset*3
            pinch2.addAction(finger2.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), centerX + offset, centerY));
            pinch2.addAction(finger2.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            pinch2.addAction(finger2.createPointerMove(Duration.ofMillis(1000), PointerInput.Origin.viewport(), centerX + offset * 3, centerY));
            pinch2.addAction(finger2.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
            
            driver.perform(Arrays.asList(pinch1, pinch2));
            logger.info("Performed pinch out gesture at ({}, {})", centerX, centerY);
        } catch (Exception e) {
            logger.error("Failed to perform pinch out gesture", e);
            throw new RuntimeException("Pinch out gesture failed", e);
        }
    }
    
    /**
     * Pinch to zoom in
     * @param element Element to pinch on
     */
    public void pinchIn(WebElement element) {
        Point location = element.getLocation();
        Dimension size = element.getSize();
        
        int centerX = location.x + size.width / 2;
        int centerY = location.y + size.height / 2;
        
        pinchIn(centerX, centerY);
    }
    
    /**
     * Pinch to zoom in at coordinates
     * @param centerX Center X coordinate
     * @param centerY Center Y coordinate
     */
    public void pinchIn(int centerX, int centerY) {
        try {
            PointerInput finger1 = new PointerInput(PointerInput.Kind.TOUCH, "finger1");
            PointerInput finger2 = new PointerInput(PointerInput.Kind.TOUCH, "finger2");
            
            Sequence pinch1 = new Sequence(finger1, 1);
            Sequence pinch2 = new Sequence(finger2, 1);
            
            int offset = 150;
            
            // Finger 1 moves from center-offset*3 to center-offset
            pinch1.addAction(finger1.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), centerX - offset * 3, centerY));
            pinch1.addAction(finger1.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            pinch1.addAction(finger1.createPointerMove(Duration.ofMillis(1000), PointerInput.Origin.viewport(), centerX - offset, centerY));
            pinch1.addAction(finger1.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
            
            // Finger 2 moves from center+offset*3 to center+offset
            pinch2.addAction(finger2.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), centerX + offset * 3, centerY));
            pinch2.addAction(finger2.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            pinch2.addAction(finger2.createPointerMove(Duration.ofMillis(1000), PointerInput.Origin.viewport(), centerX + offset, centerY));
            pinch2.addAction(finger2.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
            
            driver.perform(Arrays.asList(pinch1, pinch2));
            logger.info("Performed pinch in gesture at ({}, {})", centerX, centerY);
        } catch (Exception e) {
            logger.error("Failed to perform pinch in gesture", e);
            throw new RuntimeException("Pinch in gesture failed", e);
        }
    }
    
    // ==================== DRAG AND DROP ====================
    
    /**
     * Drag element to another element
     * @param sourceElement Source element to drag
     * @param targetElement Target element to drop on
     */
    public void dragAndDrop(WebElement sourceElement, WebElement targetElement) {
        Point sourceLocation = sourceElement.getLocation();
        Dimension sourceSize = sourceElement.getSize();
        Point targetLocation = targetElement.getLocation();
        Dimension targetSize = targetElement.getSize();
        
        int sourceX = sourceLocation.x + sourceSize.width / 2;
        int sourceY = sourceLocation.y + sourceSize.height / 2;
        int targetX = targetLocation.x + targetSize.width / 2;
        int targetY = targetLocation.y + targetSize.height / 2;
        
        dragAndDrop(sourceX, sourceY, targetX, targetY);
        logger.info("Dragged element from ({}, {}) to ({}, {})", sourceX, sourceY, targetX, targetY);
    }
    
    /**
     * Drag from coordinates to coordinates
     * @param startX Start X coordinate
     * @param startY Start Y coordinate
     * @param endX End X coordinate
     * @param endY End Y coordinate
     */
    public void dragAndDrop(int startX, int startY, int endX, int endY) {
        try {
            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            Sequence dragDrop = new Sequence(finger, 1);
            
            dragDrop.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
            dragDrop.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            // Use Thread.sleep instead of createPause for compatibility
            try {
                Thread.sleep(500); // Hold before drag
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            dragDrop.addAction(finger.createPointerMove(Duration.ofMillis(1000), PointerInput.Origin.viewport(), endX, endY));
            dragDrop.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
            
            driver.perform(Collections.singletonList(dragDrop));
            logger.debug("Performed drag and drop from ({}, {}) to ({}, {})", startX, startY, endX, endY);
        } catch (Exception e) {
            logger.error("Failed to perform drag and drop", e);
            throw new RuntimeException("Drag and drop gesture failed", e);
        }
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Get screen center coordinates
     * @return Point representing screen center
     */
    public Point getScreenCenter() {
        Dimension screenSize = getScreenSize();
        return new Point(screenSize.width / 2, screenSize.height / 2);
    }
    
    /**
     * Get element center coordinates
     * @param element Element to get center of
     * @return Point representing element center
     */
    public Point getElementCenter(WebElement element) {
        Point location = element.getLocation();
        Dimension size = element.getSize();
        return new Point(location.x + size.width / 2, location.y + size.height / 2);
    }
    
    /**
     * Check if coordinates are within screen bounds
     * @param x X coordinate
     * @param y Y coordinate
     * @return true if within bounds, false otherwise
     */
    public boolean isWithinScreenBounds(int x, int y) {
        Dimension screenSize = getScreenSize();
        return x >= 0 && x <= screenSize.width && y >= 0 && y <= screenSize.height;
    }
    
    /**
     * Wait for gesture animation to complete
     */
    public void waitForGestureAnimation() {
        try {
            Thread.sleep(AppConstants.ANIMATION_WAIT * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Gesture animation wait interrupted", e);
        }
    }
}
