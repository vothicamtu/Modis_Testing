package com.modis.utils;

import com.modis.constants.AppConstants;
import com.modis.drivers.DriverManager;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;

import java.util.Map;
import java.util.HashMap;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;

import java.util.List;

public class GestureUtils {

    private static final Logger logger = LoggerUtil.getLogger(GestureUtils.class);

    private AppiumDriver getDriver() {
        return DriverManager.getDriver();
    }

    private volatile Dimension cachedScreenSize;

    // Gesture constants
    private static final double SWIPE_START_PERCENTAGE = 0.8;
    private static final double SWIPE_END_PERCENTAGE = 0.2;
    private static final double SWIPE_ANCHOR_PERCENTAGE = 0.5;
    private static final int SWIPE_DURATION_MS = 1000;
    private static final int SCROLL_DURATION_MS = 800;
    private static final int MAX_SCROLL_ATTEMPTS = 15;

    public GestureUtils() {
    }

    public Dimension getScreenSize() {
        if (cachedScreenSize != null) {
            return cachedScreenSize;
        }

        // 1) Try capabilities first (fast, stable)
        try {
            Object cap = getDriver().getCapabilities().getCapability("appium:deviceScreenSize");
            if (cap == null) cap = getDriver().getCapabilities().getCapability("deviceScreenSize");
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
            Object viewportRect = getDriver().getCapabilities().getCapability("appium:viewportRect");
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
            cachedScreenSize = getDriver().manage().window().getSize();
            logger.debug("Screen size from getDriver().manage().window().getSize(): {}x{}", cachedScreenSize.width, cachedScreenSize.height);
            return cachedScreenSize;
        } catch (Exception e) {
            logger.warn("Failed to get screen size, using safe default 1080x1920", e);
            cachedScreenSize = new Dimension(1080, 1920);
            return cachedScreenSize;
        }
    }

    // ==================== BASIC GESTURES ====================
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

    public void tapAtCoordinates(
            int x,
            int y
    ) {

        try {

            Map<String, Object> params =
                    new HashMap<>();

            params.put("x", x);
            params.put("y", y);

            getDriver().executeScript(
                    "mobile: clickGesture",
                    params
            );

            logger.debug(
                    "Tapped at coordinates: ({}, {})",
                    x,
                    y
            );

        } catch (Exception e) {

            logger.error(
                    "Failed to tap at coordinates: ({}, {})",
                    x,
                    y,
                    e
            );

            throw new RuntimeException(
                    "Tap at coordinates failed",
                    e
            );
        }
    }

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

    public void longPressAtCoordinates(
            int x,
            int y,
            int durationMs
    ) {

        try {

            Map<String, Object> params =
                    new HashMap<>();

            params.put("x", x);
            params.put("y", y);
            params.put("duration", durationMs);

            getDriver().executeScript(
                    "mobile: longClickGesture",
                    params
            );

            logger.debug(
                    "Long pressed at coordinates: ({}, {}) for {}ms",
                    x,
                    y,
                    durationMs
            );

        } catch (Exception e) {

            logger.error(
                    "Failed to long press at coordinates: ({}, {})",
                    x,
                    y,
                    e
            );

            throw new RuntimeException(
                    "Long press failed",
                    e
            );
        }
    }

    public void swipeLeft() {
        Dimension screenSize = getScreenSize();
        int startX = (int) (screenSize.width * SWIPE_START_PERCENTAGE);
        int endX = (int) (screenSize.width * SWIPE_END_PERCENTAGE);
        int y = (int) (screenSize.height * SWIPE_ANCHOR_PERCENTAGE);

        swipe(startX, y, endX, y, SWIPE_DURATION_MS);
        logger.info("Swiped left from ({}, {}) to ({}, {})", startX, y, endX, y);
    }

    public void swipeRight() {
        Dimension screenSize = getScreenSize();
        int startX = (int) (screenSize.width * SWIPE_END_PERCENTAGE);
        int endX = (int) (screenSize.width * SWIPE_START_PERCENTAGE);
        int y = (int) (screenSize.height * SWIPE_ANCHOR_PERCENTAGE);

        swipe(startX, y, endX, y, SWIPE_DURATION_MS);
        logger.info("Swiped right from ({}, {}) to ({}, {})", startX, y, endX, y);
    }

    public void swipeUp() {
        Dimension screenSize = getScreenSize();
        int x = (int) (screenSize.width * SWIPE_ANCHOR_PERCENTAGE);
        int startY = (int) (screenSize.height * SWIPE_START_PERCENTAGE);
        int endY = (int) (screenSize.height * SWIPE_END_PERCENTAGE);

        swipe(x, startY, x, endY, SWIPE_DURATION_MS);
        logger.info("Swiped up from ({}, {}) to ({}, {})", x, startY, x, endY);
    }

    public void swipeDown() {
        Dimension screenSize = getScreenSize();
        int x = (int) (screenSize.width * SWIPE_ANCHOR_PERCENTAGE);
        int startY = (int) (screenSize.height * SWIPE_END_PERCENTAGE);
        int endY = (int) (screenSize.height * SWIPE_START_PERCENTAGE);

        swipe(x, startY, x, endY, SWIPE_DURATION_MS);
        logger.info("Swiped down from ({}, {}) to ({}, {})", x, startY, x, endY);
    }

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

    public void swipe(int startX, int startY, int endX, int endY, int durationMs) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("startX", startX);
            params.put("startY", startY);
            params.put("endX", endX);
            params.put("endY", endY);
            params.put("speed", Math.max(100, (int) (Math.hypot(endX - startX, endY - startY) / (durationMs / 1000.0))));

            getDriver().executeScript("mobile: dragGesture", params);

            logger.debug("Swiped from ({}, {}) to ({}, {})", startX, startY, endX, endY);

        } catch (Exception e) {
            logger.error("Failed to swipe from ({}, {}) to ({}, {})", startX, startY, endX, endY, e);
            throw new RuntimeException("Swipe gesture failed", e);
        }
    }

    public WebElement scrollToElementByText(String text) {
        logger.info("Scrolling to element with text: {}", text);

        for (int attempt = 0; attempt < MAX_SCROLL_ATTEMPTS; attempt++) {
            try {
                List<WebElement> elements =
                        DriverManager.safelyFindElements(
                                AppiumBy.accessibilityId(text)
                        );

                if (
                        (elements == null || elements.isEmpty())
                                &&
                                DriverManager.getCurrentPlatform()
                                        .equalsIgnoreCase("android")
                ) {

                    elements =
                            DriverManager.safelyFindElements(
                                    AppiumBy.androidUIAutomator(
                                            String.format(
                                                    "new UiSelector().text(\"%s\")",
                                                    text
                                            )
                                    )
                            );
                }
                if (elements != null && !elements.isEmpty() && elements.get(0).isDisplayed()) {
                    logger.info("Found element with text '{}' after {} scroll attempts", text, attempt);
                    return elements.get(0);
                }
            } catch (Exception e) {
                logger.debug("Element with text '{}' not found, attempt {}", text, attempt + 1);
            }

            // Scroll down to find element
            scrollDown();
            waitForGestureAnimation();
        }

        logger.warn("Element with text '{}' not found after {} scroll attempts", text, MAX_SCROLL_ATTEMPTS);
        return null;
    }

    public WebElement scrollToElementByAccessibilityId(String accessibilityId) {
        logger.info("Scrolling to element with accessibility ID: {}", accessibilityId);

        for (int attempt = 0; attempt < MAX_SCROLL_ATTEMPTS; attempt++) {
            try {
                List<WebElement> elements = DriverManager.safelyFindElements(AppiumBy.accessibilityId(accessibilityId));

                boolean isAndroid = DriverManager.getCurrentPlatform().equalsIgnoreCase("android");
                if (isAndroid && (elements == null || elements.isEmpty())) {
                    elements = DriverManager.safelyFindElements(AppiumBy.id(accessibilityId));
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
            waitForGestureAnimation();
        }

        logger.warn("Element with ID/Accessibility ID '{}' not found after {} scroll attempts", accessibilityId, MAX_SCROLL_ATTEMPTS);
        return null;
    }

    public void scrollDown() {
        Dimension screenSize = getScreenSize();
        int x = screenSize.width / 2;
        int startY = (int) (screenSize.height * 0.75);
        int endY   = (int) (screenSize.height * 0.25);

        swipe(x, startY, x, endY, SCROLL_DURATION_MS);
        logger.debug("Scrolled down");
    }

    public void scrollUp() {
        Dimension screenSize = getScreenSize();
        int x = screenSize.width / 2;
        int startY = (int) (screenSize.height * 0.25);
        int endY   = (int) (screenSize.height * 0.75);

        swipe(x, startY, x, endY, SCROLL_DURATION_MS);
        logger.debug("Scrolled up");
    }

    public void scrollToTop() {

        logger.info("Scrolling to top");

        for (int i = 0; i < 5; i++) {

            scrollUp();

            waitForGestureAnimation();
        }
    }

    public void scrollToBottom() {

        logger.info("Scrolling to bottom");

        for (int i = 0; i < 8; i++) {

            scrollDown();

            waitForGestureAnimation();
        }
    }

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
    public void pinchOut(WebElement element) {
        logger.warn(
                "Pinch out temporarily disabled for Android 15 stability"
        );
    }

    public void pinchOut(int centerX, int centerY) {
        logger.warn(
                "Pinch out temporarily disabled for Android 15 stability"
        );
    }

    public void pinchIn(WebElement element) {
        logger.warn(
                "Pinch in temporarily disabled for Android 15 stability"
        );
    }

    public void pinchIn(int centerX, int centerY) {
        logger.warn(
                "Pinch in temporarily disabled for Android 15 stability"
        );
    }

    // ==================== DRAG AND DROP ====================

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

    public void dragAndDrop(
            int startX,
            int startY,
            int endX,
            int endY
    ) {

        try {

            Map<String, Object> params =
                    new HashMap<>();

            params.put("startX", startX);
            params.put("startY", startY);
            params.put("endX", endX);
            params.put("endY", endY);
            params.put("speed", 500);

            getDriver().executeScript(
                    "mobile: dragGesture",
                    params
            );

            logger.debug(
                    "Dragged from ({}, {}) to ({}, {})",
                    startX,
                    startY,
                    endX,
                    endY
            );

        } catch (Exception e) {

            logger.error(
                    "Failed to drag and drop",
                    e
            );

            throw new RuntimeException(
                    "Drag and drop failed",
                    e
            );
        }
    }

    public Point getScreenCenter() {
        Dimension screenSize = getScreenSize();
        return new Point(screenSize.width / 2, screenSize.height / 2);
    }

    public Point getElementCenter(WebElement element) {
        Point location = element.getLocation();
        Dimension size = element.getSize();
        return new Point(location.x + size.width / 2, location.y + size.height / 2);
    }

    public boolean isWithinScreenBounds(int x, int y) {
        Dimension screenSize = getScreenSize();
        return x >= 0 && x <= screenSize.width && y >= 0 && y <= screenSize.height;
    }

    public void waitForGestureAnimation() {
        try {
            Thread.sleep(AppConstants.ANIMATION_WAIT * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Gesture animation wait interrupted", e);
        }
    }

    public void scrollElementIntoCenter(WebElement element) {
        if (element == null) {
            return;
        }

        try {
            if (!element.isDisplayed()) {
                return;
            }
        } catch (Exception ignored) {
            return;
        }
        try {
            Point location = element.getLocation();
            Dimension size = element.getSize();
            Dimension screen = getScreenSize();

            int elementCenterY = location.y + (size.height / 2);
            int screenCenterY = screen.height / 2;

            int delta = elementCenterY - screenCenterY;

            if (Math.abs(delta) < 250) {
                return;
            }

            int startX = screen.width / 2;
            int startY = elementCenterY;
            int endY = screenCenterY;

            endY = Math.max(200, Math.min(endY, screen.height - 200));
            swipe(startX, startY, startX, endY, 250);
            logger.debug("Scrolled element into center");

        } catch (Exception e) {
            logger.debug("scrollElementIntoCenter failed: {}", e.getMessage());
        }
    }
}
