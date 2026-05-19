package com.modis.utils;

import com.modis.drivers.DriverManager;
import io.appium.java_client.AppiumDriver;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for taking and managing screenshots
 * Provides methods for capturing screenshots during test execution
 */
public class ScreenshotUtils {
    
    private static final Logger logger = LoggerUtil.getLogger(ScreenshotUtils.class);
    private static final String SCREENSHOT_DIR = "screenshots";
    private static final String SCREENSHOT_FORMAT = ".png";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    
    static {
        // Create screenshots directory if it doesn't exist
        createScreenshotDirectory();
    }
    
    /**
     * Take screenshot with custom name
     * @param screenshotName Name for the screenshot file
     * @return Path to the screenshot file
     */
    public static String takeScreenshot(String screenshotName) {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            if (driver == null) {
                logger.warn("Driver is null, cannot take screenshot");
                return null;
            }
            
            // Clean screenshot name
            String cleanName = cleanFileName(screenshotName);
            String timestamp = DATE_FORMAT.format(new Date());
            String fileName = String.format("%s_%s%s", cleanName, timestamp, SCREENSHOT_FORMAT);
            
            // Take screenshot
            File sourceFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File destFile = new File(SCREENSHOT_DIR, fileName);
            
            FileUtils.copyFile(sourceFile, destFile);
            
            String absolutePath = destFile.getAbsolutePath();
            logger.info("Screenshot saved: {}", absolutePath);
            return absolutePath;
            
        } catch (Exception e) {
            logger.error("Failed to take screenshot: {}", screenshotName, e);
            return null;
        }
    }
    
    /**
     * Take screenshot with automatic naming based on test method
     * @return Path to the screenshot file
     */
    public static String takeScreenshot() {
        String methodName = getCurrentTestMethodName();
        return takeScreenshot(methodName);
    }
    
    /**
     * Take screenshot for test failure
     * @param testClassName Test class name
     * @param testMethodName Test method name
     * @return Path to the screenshot file
     */
    public static String takeFailureScreenshot(String testClassName, String testMethodName) {
        String screenshotName = String.format("%s_%s_FAILED", testClassName, testMethodName);
        return takeScreenshot(screenshotName);
    }
    
    /**
     * Take screenshot for test step
     * @param testMethodName Test method name
     * @param stepName Step name
     * @return Path to the screenshot file
     */
    public static String takeStepScreenshot(String testMethodName, String stepName) {
        String screenshotName = String.format("%s_%s", testMethodName, stepName);
        return takeScreenshot(screenshotName);
    }
    
    /**
     * Take screenshot with device info in filename
     * @param screenshotName Base screenshot name
     * @return Path to the screenshot file
     */
    public static String takeScreenshotWithDeviceInfo(String screenshotName) {
        try {
            String platform = DriverManager.getCurrentPlatform();
            String deviceName = DriverManager.getCurrentDeviceName();
            String enhancedName = String.format("%s_%s_%s", screenshotName, platform, cleanFileName(deviceName));
            return takeScreenshot(enhancedName);
        } catch (Exception e) {
            logger.warn("Failed to get device info, taking regular screenshot", e);
            return takeScreenshot(screenshotName);
        }
    }
    
    /**
     * Take screenshot as byte array (useful for reporting frameworks)
     * @return Screenshot as byte array
     */
    public static byte[] takeScreenshotAsBytes() {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            if (driver == null) {
                logger.warn("Driver is null, cannot take screenshot");
                return new byte[0];
            }
            
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            logger.debug("Screenshot taken as byte array, size: {} bytes", screenshot.length);
            return screenshot;
            
        } catch (Exception e) {
            logger.error("Failed to take screenshot as bytes", e);
            return new byte[0];
        }
    }
    
    /**
     * Take screenshot as base64 string (useful for HTML reports)
     * @return Screenshot as base64 string
     */
    public static String takeScreenshotAsBase64() {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            if (driver == null) {
                logger.warn("Driver is null, cannot take screenshot");
                return "";
            }
            
            String base64Screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
            logger.debug("Screenshot taken as base64, length: {} characters", base64Screenshot.length());
            return base64Screenshot;
            
        } catch (Exception e) {
            logger.error("Failed to take screenshot as base64", e);
            return "";
        }
    }
    
    /**
     * Take multiple screenshots with delay
     * @param baseName Base name for screenshots
     * @param count Number of screenshots to take
     * @param delayMs Delay between screenshots in milliseconds
     * @return Array of screenshot paths
     */
    public static String[] takeMultipleScreenshots(String baseName, int count, int delayMs) {
        String[] screenshots = new String[count];
        
        for (int i = 0; i < count; i++) {
            String screenshotName = String.format("%s_%d", baseName, i + 1);
            screenshots[i] = takeScreenshot(screenshotName);
            
            if (i < count - 1) { // Don't wait after the last screenshot
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Screenshot delay interrupted", e);
                    break;
                }
            }
        }
        
        logger.info("Took {} screenshots with base name: {}", count, baseName);
        return screenshots;
    }
    
    /**
     * Take screenshot before and after an action
     * @param actionName Name of the action
     * @param action Runnable action to execute
     * @return Array containing [beforeScreenshot, afterScreenshot] paths
     */
    public static String[] takeBeforeAfterScreenshots(String actionName, Runnable action) {
        String beforeScreenshot = takeScreenshot(actionName + "_BEFORE");
        
        try {
            action.run();
        } catch (Exception e) {
            logger.error("Action failed during before/after screenshot capture", e);
            throw e;
        }
        
        String afterScreenshot = takeScreenshot(actionName + "_AFTER");
        
        return new String[]{beforeScreenshot, afterScreenshot};
    }
    
    /**
     * Clean up old screenshots (older than specified days)
     * @param daysOld Number of days old to consider for cleanup
     * @return Number of files deleted
     */
    public static int cleanupOldScreenshots(int daysOld) {
        try {
            File screenshotDir = new File(SCREENSHOT_DIR);
            if (!screenshotDir.exists()) {
                return 0;
            }
            
            long cutoffTime = System.currentTimeMillis() - (daysOld * 24L * 60L * 60L * 1000L);
            int deletedCount = 0;
            
            File[] files = screenshotDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(SCREENSHOT_FORMAT) && file.lastModified() < cutoffTime) {
                        if (file.delete()) {
                            deletedCount++;
                            logger.debug("Deleted old screenshot: {}", file.getName());
                        }
                    }
                }
            }
            
            logger.info("Cleaned up {} old screenshots (older than {} days)", deletedCount, daysOld);
            return deletedCount;
            
        } catch (Exception e) {
            logger.error("Failed to cleanup old screenshots", e);
            return 0;
        }
    }
    
    /**
     * Get screenshot directory path
     * @return Screenshot directory path
     */
    public static String getScreenshotDirectory() {
        return new File(SCREENSHOT_DIR).getAbsolutePath();
    }
    
    /**
     * Check if screenshot directory exists and is writable
     * @return true if directory is ready, false otherwise
     */
    public static boolean isScreenshotDirectoryReady() {
        File dir = new File(SCREENSHOT_DIR);
        return dir.exists() && dir.isDirectory() && dir.canWrite();
    }
    
    /**
     * Get screenshot file size in bytes
     * @param screenshotPath Path to screenshot file
     * @return File size in bytes, -1 if file doesn't exist
     */
    public static long getScreenshotFileSize(String screenshotPath) {
        try {
            File file = new File(screenshotPath);
            return file.exists() ? file.length() : -1;
        } catch (Exception e) {
            logger.debug("Failed to get screenshot file size", e);
            return -1;
        }
    }
    
    /**
     * Copy screenshot to custom location
     * @param sourcePath Source screenshot path
     * @param destinationPath Destination path
     * @return true if copy successful, false otherwise
     */
    public static boolean copyScreenshot(String sourcePath, String destinationPath) {
        try {
            File sourceFile = new File(sourcePath);
            File destFile = new File(destinationPath);
            
            if (!sourceFile.exists()) {
                logger.warn("Source screenshot file does not exist: {}", sourcePath);
                return false;
            }
            
            // Create destination directory if it doesn't exist
            File destDir = destFile.getParentFile();
            if (destDir != null && !destDir.exists()) {
                destDir.mkdirs();
            }
            
            FileUtils.copyFile(sourceFile, destFile);
            logger.info("Screenshot copied from {} to {}", sourcePath, destinationPath);
            return true;
            
        } catch (IOException e) {
            logger.error("Failed to copy screenshot from {} to {}", sourcePath, destinationPath, e);
            return false;
        }
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    /**
     * Create screenshot directory if it doesn't exist
     */
    private static void createScreenshotDirectory() {
        try {
            File dir = new File(SCREENSHOT_DIR);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (created) {
                    logger.info("Created screenshot directory: {}", dir.getAbsolutePath());
                } else {
                    logger.warn("Failed to create screenshot directory: {}", dir.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            logger.error("Error creating screenshot directory", e);
        }
    }
    
    /**
     * Clean filename by removing invalid characters
     * @param fileName Original filename
     * @return Cleaned filename
     */
    private static String cleanFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "screenshot";
        }
        
        // Remove invalid characters for filenames
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_")
                      .replaceAll("_{2,}", "_") // Replace multiple underscores with single
                      .trim();
    }
    
    /**
     * Get current test method name from stack trace
     * @return Current test method name
     */
    private static String getCurrentTestMethodName() {
        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (StackTraceElement element : stackTrace) {
                String className = element.getClassName();
                String methodName = element.getMethodName();
                
                // Look for test methods (typically contain "test" or are in test packages)
                if (className.contains("test") || methodName.contains("test") || 
                    className.contains("Test") || methodName.startsWith("test")) {
                    return methodName;
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to get current test method name", e);
        }
        
        return "unknown_test";
    }
    
    /**
     * Generate unique filename with timestamp
     * @param baseName Base name for the file
     * @return Unique filename with timestamp
     */
    private static String generateUniqueFileName(String baseName) {
        String timestamp = DATE_FORMAT.format(new Date());
        return String.format("%s_%s", baseName, timestamp);
    }
    
    /**
     * Validate screenshot file
     * @param filePath Path to screenshot file
     * @return true if valid, false otherwise
     */
    private static boolean isValidScreenshotFile(String filePath) {
        try {
            File file = new File(filePath);
            return file.exists() && file.isFile() && file.length() > 0 && 
                   file.getName().toLowerCase().endsWith(SCREENSHOT_FORMAT.toLowerCase());
        } catch (Exception e) {
            return false;
        }
    }
}