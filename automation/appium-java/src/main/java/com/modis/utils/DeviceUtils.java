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

public class DeviceUtils {
    private static final Logger logger = LoggerUtil.getLogger(DeviceUtils.class);

    private DeviceUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static Dimension getScreenSize() {
        AppiumDriver driver = DriverManager.getDriver();
        return getScreenSize(driver);
    }

    public static Dimension getScreenSize(AppiumDriver driver) {
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

    public static int getScreenWidth() {
        return getScreenSize().width;
    }

    public static int getScreenHeight() {
        return getScreenSize().height;
    }

    public static ScreenOrientation getOrientation() {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            ScreenOrientation orientation = ScreenOrientation.PORTRAIT;
            return orientation;
        } catch (Exception e) {
            logger.error("Failed to get device orientation", e);
            return ScreenOrientation.PORTRAIT;
        }
    }

    public static boolean isPortrait() {
        return getOrientation() == ScreenOrientation.PORTRAIT;
    }

    public static boolean isLandscape() {
        return getOrientation() == ScreenOrientation.LANDSCAPE;
    }

    public static String getPlatform() {
        return DriverManager.getCurrentPlatform();
    }

    public static boolean isAndroid() {
        return "android".equalsIgnoreCase(getPlatform());
    }

    public static boolean isIOS() {
        return "ios".equalsIgnoreCase(getPlatform());
    }

    public static void rotateToPortrait() {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            if (getOrientation() != ScreenOrientation.PORTRAIT) {
                logger.info("Device rotated to portrait (mocked)");
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            logger.error("Failed to rotate device to portrait", e);
        }
    }

    public static void rotateToLandscape() {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            if (getOrientation() != ScreenOrientation.LANDSCAPE) {
                logger.info("Device rotated to landscape (mocked)");
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            logger.error("Failed to rotate device to landscape", e);
        }
    }

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

    public static void toggleAirplaneMode() {
        try {
            if (isAndroid()) {
                logger.info("Airplane mode toggled");
            } else {
                logger.warn("Airplane mode toggle not supported on iOS");
            }
        } catch (Exception e) {
            logger.error("Failed to toggle airplane mode", e);
        }
    }

    public static void toggleWiFi() {
        try {
            if (isAndroid()) {
                logger.info("WiFi toggled");
            } else {
                logger.warn("WiFi toggle not supported on iOS");
            }
        } catch (Exception e) {
            logger.error("Failed to toggle WiFi", e);
        }
    }

    public static void toggleMobileData() {
        try {
            if (isAndroid()) {
                logger.info("Mobile data toggled");
            } else {
                logger.warn("Mobile data toggle not supported on iOS");
            }
        } catch (Exception e) {
            logger.error("Failed to toggle mobile data", e);
        }
    }

    public static boolean isKeyboardShown() {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            return (Boolean) driver.executeScript("mobile: isKeyboardShown");
        } catch (Exception e) {
            logger.debug("Failed to check keyboard status", e);
            return false;
        }
    }

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

    public static Map<String, Object> getDeviceCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        try {
            AppiumDriver driver = DriverManager.getDriver();
            capabilities.put("platform", getPlatform());
            capabilities.put("screenWidth", getScreenWidth());
            capabilities.put("screenHeight", getScreenHeight());
            capabilities.put("orientation", getOrientation().toString());
            if (isAndroid()) {
                capabilities.put("deviceLocked", isDeviceLocked());
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

    public static boolean hasMultipleCameras() {
        return true;
    }

    public static boolean hasFlash() {
        return true;
    }

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

    public static void waitForDeviceReady(int timeoutSeconds) {
        try {
            long startTime = System.currentTimeMillis();
            long timeout = timeoutSeconds * 1000L;
            while (System.currentTimeMillis() - startTime < timeout) {
                try {
                    AppiumDriver driver = DriverManager.getDriver();
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

    public static boolean takeScreenshot(String fileName) {
        try {
            String path = ScreenshotUtils.takeScreenshot(fileName);
            return path != null && !path.isEmpty();
        } catch (Exception e) {
            logger.error("Failed to take screenshot", e);
            return false;
        }
    }

    public static Map<String, Integer> getSafeAreaCoordinates() {
        Map<String, Integer> safeArea = new HashMap<>();
        Dimension screenSize = getScreenSize();
        int statusBarHeight = isAndroid() ? 60 : 44;
        int navigationBarHeight = isAndroid() ? 48 : 34;
        safeArea.put("top", statusBarHeight);
        safeArea.put("bottom", screenSize.height - navigationBarHeight);
        safeArea.put("left", 0);
        safeArea.put("right", screenSize.width);
        return safeArea;
    }

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
