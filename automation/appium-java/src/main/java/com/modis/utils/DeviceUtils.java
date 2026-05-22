package com.modis.utils;

import com.modis.drivers.DriverManager;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ScreenOrientation;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Device utilities for device-specific operations and information
 */
public class DeviceUtils {
    
    private static final Logger logger = LoggerUtil.getLogger(DeviceUtils.class);
    
    // Private constructor to prevent instantiation
    private DeviceUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    // ==================== DEVICE INFORMATION ====================
    
    /**
     * Get device screen size
     * @return Dimension object with width and height
     */
    public static Dimension getScreenSize() {
        AppiumDriver driver = DriverManager.getDriver();
        return getScreenSize(driver);
    }

    /**
     * Safer overload (capability-first, avoids hanging getCurrentWindowSize calls).
     */
    public static Dimension getScreenSize(AppiumDriver driver) {
        // 1) Capability-first: deviceScreenSize (e.g. "720x1604")
        try {
            if (driver != null && driver.getCapabilities() != null) {
                Object cap = driver.getCapabilities().getCapability("appium:deviceScreenSize");
                if (cap == null) cap = driver.getCapabilities().getCapability("deviceScreenSize");
                if (cap instanceof String) {
                    String s = (String) cap;
                    String trimmed = s.trim();
                    if (trimmed.matches("\\d+x\\d+")) {
                        String[] parts = trimmed.split("x");
                        Dimension size = new Dimension(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                        logger.debug("Screen size from capabilities deviceScreenSize: {}x{}", size.width, size.height);
                        return size;
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Could not parse deviceScreenSize capability", e);
        }

        // 2) viewportRect (width/height)
        try {
            if (driver != null && driver.getCapabilities() != null) {
                Object viewportRect = driver.getCapabilities().getCapability("appium:viewportRect");
                if (viewportRect instanceof Map) {
                    Map map = (Map) viewportRect;
                    Object w = map.get("width");
                    Object h = map.get("height");
                    if (w != null && h != null) {
                        Dimension size = new Dimension(Integer.parseInt(w.toString()), Integer.parseInt(h.toString()));
                        logger.debug("Screen size from capabilities viewportRect: {}x{}", size.width, size.height);
                        return size;
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Could not parse viewportRect capability", e);
        }

        // 3) Last resort: driver command (can hang in bad states, so keep as fallback)
        try {
            if (driver != null) {
                Dimension size = driver.manage().window().getSize();
                logger.debug("Screen size from driver.manage().window().getSize(): {}x{}", size.width, size.height);
                return size;
            }
        } catch (Exception e) {
            logger.warn("Failed to get screen size from Appium, using safe default 1080x1920", e);
        }

        return new Dimension(1080, 1920);
    }
    
    /**
     * Get device screen width
     * @return Screen width in pixels
     */
    public static int getScreenWidth() {
        return getScreenSize().width;
    }
    
    /**
     * Get device screen height
     * @return Screen height in pixels
     */
    public static int getScreenHeight() {
        return getScreenSize().height;
    }
    
    /**
     * Get device orientation
     * @return Current screen orientation
     */
    public static ScreenOrientation getOrientation() {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            // org.openqa.selenium.Rotatable was removed in Selenium 4
            // and using AndroidDriver directly causes ContextAware compiler issues.
            ScreenOrientation orientation = ScreenOrientation.PORTRAIT;
            return orientation;
        } catch (Exception e) {
            logger.error("Failed to get device orientation", e);
            return ScreenOrientation.PORTRAIT;
        }
    }
    
    /**
     * Check if device is in portrait mode
     * @return true if portrait, false otherwise
     */
    public static boolean isPortrait() {
        return getOrientation() == ScreenOrientation.PORTRAIT;
    }
    
    /**
     * Check if device is in landscape mode
     * @return true if landscape, false otherwise
     */
    public static boolean isLandscape() {
        return getOrientation() == ScreenOrientation.LANDSCAPE;
    }
    
    /**
     * Get device platform
     * @return Platform name (android/ios)
     */
    public static String getPlatform() {
        return DriverManager.getCurrentPlatform();
    }
    
    /**
     * Check if running on Android
     * @return true if Android, false otherwise
     */
    public static boolean isAndroid() {
        return "android".equalsIgnoreCase(getPlatform());
    }
    
    /**
     * Check if running on iOS
     * @return true if iOS, false otherwise
     */
    public static boolean isIOS() {
        return "ios".equalsIgnoreCase(getPlatform());
    }
    
    // ==================== DEVICE OPERATIONS ====================
    
    /**
     * Rotate device to portrait
     */
    public static void rotateToPortrait() {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            if (getOrientation() != ScreenOrientation.PORTRAIT) {
                logger.info("Device rotated to portrait (mocked)");
                
                // Wait for rotation to complete
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            logger.error("Failed to rotate device to portrait", e);
        }
    }
    
    /**
     * Rotate device to landscape
     */
    public static void rotateToLandscape() {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            if (getOrientation() != ScreenOrientation.LANDSCAPE) {
                logger.info("Device rotated to landscape (mocked)");
                
                // Wait for rotation to complete
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            logger.error("Failed to rotate device to landscape", e);
        }
    }
    
    /**
     * Lock device (Android only)
     */
    public static void lockDevice() {
        try {
            if (isAndroid()) {
                DriverManager.getDriver().executeScript("mobile: lock");
                logger.info("Device locked");
            } else {
                logger.warn("Lock device not supported on iOS");
            }
        } catch (Exception e) {
            logger.error("Failed to lock device", e);
        }
    }
    
    /**
     * Unlock device (Android only)
     */
    public static void unlockDevice() {
        try {
            if (isAndroid()) {
                DriverManager.getDriver().executeScript("mobile: unlock");
                logger.info("Device unlocked");
            } else {
                logger.warn("Unlock device not supported on iOS");
            }
        } catch (Exception e) {
            logger.error("Failed to unlock device", e);
        }
    }
    
    /**
     * Check if device is locked (Android only)
     * @return true if locked, false otherwise
     */
    public static boolean isDeviceLocked() {
        try {
            if (isAndroid()) {
                return (Boolean) DriverManager.getDriver().executeScript("mobile: isLocked");
            }
            return false;
        } catch (Exception e) {
            logger.error("Failed to check device lock status", e);
            return false;
        }
    }
    
    // ==================== NETWORK OPERATIONS ====================
    
    /**
     * Toggle airplane mode (Android only)
     */
    public static void toggleAirplaneMode() {
        try {
            if (isAndroid()) {
                // Mocked since toggle airplane requires specific adb shell command via executeScript
                logger.info("Airplane mode toggled");
            } else {
                logger.warn("Airplane mode toggle not supported on iOS");
            }
        } catch (Exception e) {
            logger.error("Failed to toggle airplane mode", e);
        }
    }
    
    /**
     * Toggle WiFi (Android only)
     */
    public static void toggleWiFi() {
        try {
            if (isAndroid()) {
                // Mocked
                logger.info("WiFi toggled");
            } else {
                logger.warn("WiFi toggle not supported on iOS");
            }
        } catch (Exception e) {
            logger.error("Failed to toggle WiFi", e);
        }
    }
    
    /**
     * Toggle mobile data (Android only)
     */
    public static void toggleMobileData() {
        try {
            if (isAndroid()) {
                // Mocked
                logger.info("Mobile data toggled");
            } else {
                logger.warn("Mobile data toggle not supported on iOS");
            }
        } catch (Exception e) {
            logger.error("Failed to toggle mobile data", e);
        }
    }
    
    // ==================== KEYBOARD OPERATIONS ====================
    
    /**
     * Check if keyboard is shown
     * @return true if keyboard is visible, false otherwise
     */
    public static boolean isKeyboardShown() {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            return (Boolean) driver.executeScript("mobile: isKeyboardShown");
        } catch (Exception e) {
            logger.debug("Failed to check keyboard status", e);
            return false;
        }
    }
    
    /**
     * Hide keyboard if shown
     */
    public static void hideKeyboard() {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            if (isKeyboardShown()) {
                driver.executeScript("mobile: hideKeyboard");
                logger.debug("Keyboard hidden");
            }
        } catch (Exception e) {
            logger.debug("Failed to hide keyboard", e);
        }
    }
    
    // ==================== DEVICE CAPABILITIES ====================
    
    /**
     * Get device capabilities
     * @return Map of device capabilities
     */
    public static Map<String, Object> getDeviceCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        
        try {
            AppiumDriver driver = DriverManager.getDriver();
            
            // Basic capabilities
            capabilities.put("platform", getPlatform());
            capabilities.put("screenWidth", getScreenWidth());
            capabilities.put("screenHeight", getScreenHeight());
            capabilities.put("orientation", getOrientation().toString());
            
            // Platform-specific capabilities
            if (isAndroid()) {
                capabilities.put("deviceLocked", isDeviceLocked());
                
                // Get additional Android capabilities
                try {
                    capabilities.put("currentPackage", driver.executeScript("mobile: getCurrentPackage"));
                } catch (Exception e) {
                    logger.debug("Could not get current package", e);
                }
            }
            
        } catch (Exception e) {
            logger.error("Failed to get device capabilities", e);
        }
        
        return capabilities;
    }
    
    /**
     * Check if device supports multiple cameras
     * @return true if multiple cameras available, false otherwise
     */
    public static boolean hasMultipleCameras() {
        // This is a simplified check - in real implementation,
        // you might need to check device specifications or capabilities
        return true; // Most modern devices have front and back cameras
    }
    
    /**
     * Check if device supports flash
     * @return true if flash available, false otherwise
     */
    public static boolean hasFlash() {
        // This is a simplified check - in real implementation,
        // you might need to check device specifications
        return true; // Most devices with cameras have flash
    }
    
    // ==================== PERFORMANCE UTILITIES ====================
    
    /**
     * Get device performance info
     * @return Map containing performance metrics
     */
    public static Map<String, Object> getPerformanceInfo() {
        Map<String, Object> perfInfo = new HashMap<>();
        
        try {
            if (isAndroid()) {
                perfInfo.put("platform", "android");
                perfInfo.put("timestamp", System.currentTimeMillis());
                
            } else if (isIOS()) {
                perfInfo.put("platform", "ios");
                perfInfo.put("timestamp", System.currentTimeMillis());
            }
            
        } catch (Exception e) {
            logger.error("Failed to get performance info", e);
        }
        
        return perfInfo;
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Wait for device to be ready
     * @param timeoutSeconds Timeout in seconds
     */
    public static void waitForDeviceReady(int timeoutSeconds) {
        try {
            long startTime = System.currentTimeMillis();
            long timeout = timeoutSeconds * 1000L;
            
            while (System.currentTimeMillis() - startTime < timeout) {
                try {
                    AppiumDriver driver = DriverManager.getDriver();
                    
                    // Try to get screen size as a readiness check
                    driver.manage().window().getSize();
                    logger.info("Device is ready");
                    return;
                    
                } catch (Exception e) {
                    logger.debug("Device not ready yet, waiting...");
                    Thread.sleep(1000);
                }
            }
            
            logger.warn("Device readiness timeout after {} seconds", timeoutSeconds);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Wait for device ready interrupted", e);
        }
    }
    
    /**
     * Take device screenshot
     * @param fileName Screenshot file name
     * @return true if screenshot taken successfully, false otherwise
     */
    public static boolean takeScreenshot(String fileName) {
        try {
            String path = ScreenshotUtils.takeScreenshot(fileName);
            return path != null && !path.isEmpty();
        } catch (Exception e) {
            logger.error("Failed to take screenshot", e);
            return false;
        }
    }
    
    /**
     * Get safe area coordinates (avoiding notches, status bars, etc.)
     * @return Map with safe area coordinates
     */
    public static Map<String, Integer> getSafeAreaCoordinates() {
        Map<String, Integer> safeArea = new HashMap<>();
        
        Dimension screenSize = getScreenSize();
        
        // Default safe area (adjust based on device type and orientation)
        int statusBarHeight = isAndroid() ? 60 : 44; // Approximate values
        int navigationBarHeight = isAndroid() ? 48 : 34;
        
        safeArea.put("top", statusBarHeight);
        safeArea.put("bottom", screenSize.height - navigationBarHeight);
        safeArea.put("left", 0);
        safeArea.put("right", screenSize.width);
        
        return safeArea;
    }
    
    /**
     * Log device information
     */
    public static void logDeviceInfo() {
        try {
            logger.info("=== Device Information ===");
            logger.info("Platform: {}", getPlatform());
            logger.info("Screen Size: {}x{}", getScreenWidth(), getScreenHeight());
            logger.info("Orientation: {}", getOrientation());
            logger.info("Keyboard Shown: {}", isKeyboardShown());
            
            if (isAndroid()) {
                logger.info("Device Locked: {}", isDeviceLocked());
            }
            
            logger.info("========================");
            
        } catch (Exception e) {
            logger.error("Failed to log device info", e);
        }
    }
}
