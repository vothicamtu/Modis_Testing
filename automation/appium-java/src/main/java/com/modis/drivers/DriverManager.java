package com.modis.drivers;

import com.modis.constants.AppConstants;
import com.modis.utils.ConfigReader;
import com.modis.utils.LoggerUtil;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Driver Manager for Appium WebDriver instances
 * Handles driver creation, configuration, and lifecycle management
 */
public class DriverManager {
    
    private static final Logger logger = LoggerUtil.getLogger(DriverManager.class);
    private static final ThreadLocal<AppiumDriver> driver = new ThreadLocal<>();
    
    /**
     * Get the current driver instance for the thread
     * @return AppiumDriver instance
     */
    public static AppiumDriver getDriver() {
        return driver.get();
    }
    
    /**
     * Set driver instance for the current thread
     * @param appiumDriver The driver instance to set
     */
    public static void setDriver(AppiumDriver appiumDriver) {
        driver.set(appiumDriver);
    }
    
    /**
     * Create and initialize Appium driver based on platform
     * @param platform The platform (android/ios)
     * @return AppiumDriver instance
     */
    public static AppiumDriver createDriver(String platform) {
        logger.info("Creating driver for platform: {}", platform);
        
        AppiumDriver appiumDriver;
        DesiredCapabilities capabilities = new DesiredCapabilities();
        
        try {
            switch (platform.toLowerCase()) {
                case "android":
                    capabilities = getAndroidCapabilities();
                    appiumDriver = new AndroidDriver(getAppiumServerURL(), capabilities);
                    break;
                case "ios":
                    capabilities = getIOSCapabilities();
                    appiumDriver = new IOSDriver(getAppiumServerURL(), capabilities);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported platform: " + platform);
            }
            
            configureDriver(appiumDriver);
            setDriver(appiumDriver);
            
            logger.info("Driver created successfully for platform: {}", platform);
            return appiumDriver;
            
        } catch (Exception e) {
            logger.error("Failed to create driver for platform: {}", platform, e);
            throw new RuntimeException("Driver creation failed", e);
        }
    }
    
    /**
     * Get Android capabilities
     * @return DesiredCapabilities for Android
     */
    private static DesiredCapabilities getAndroidCapabilities() {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        
        // Platform capabilities
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("automationName", "UiAutomator2");
        capabilities.setCapability("deviceName", ConfigReader.getProperty("android.deviceName", AppConstants.DEFAULT_DEVICE_NAME));
        capabilities.setCapability("platformVersion", ConfigReader.getProperty("android.platformVersion", AppConstants.DEFAULT_PLATFORM_VERSION));
        
        // App capabilities
        String appPath = ConfigReader.getProperty("android.appPath");
        if (appPath != null && !appPath.isEmpty()) {
            capabilities.setCapability("app", appPath);
        } else {
            capabilities.setCapability("appPackage", ConfigReader.getProperty("android.appPackage", AppConstants.APP_PACKAGE));
            capabilities.setCapability("appActivity", ConfigReader.getProperty("android.appActivity", AppConstants.APP_ACTIVITY));
        }
        
        // Performance and behavior capabilities
        capabilities.setCapability("noReset", ConfigReader.getBooleanProperty("android.noReset", false));
        capabilities.setCapability("fullReset", ConfigReader.getBooleanProperty("android.fullReset", false));
        capabilities.setCapability("autoGrantPermissions", ConfigReader.getBooleanProperty("android.autoGrantPermissions", true));
        capabilities.setCapability("autoAcceptAlerts", ConfigReader.getBooleanProperty("android.autoAcceptAlerts", true));
        capabilities.setCapability("autoDismissAlerts", ConfigReader.getBooleanProperty("android.autoDismissAlerts", true));
        
        // Network and performance
        capabilities.setCapability("networkSpeed", ConfigReader.getProperty("android.networkSpeed", "full"));
        capabilities.setCapability("gpsEnabled", ConfigReader.getBooleanProperty("android.gpsEnabled", true));
        
        // Timeouts
        capabilities.setCapability("newCommandTimeout", ConfigReader.getIntProperty("android.newCommandTimeout", 300));
        capabilities.setCapability("androidInstallTimeout", ConfigReader.getIntProperty("android.installTimeout", 90000));
        
        // UiAutomator2 specific
        capabilities.setCapability("uiautomator2ServerLaunchTimeout", ConfigReader.getIntProperty("android.serverLaunchTimeout", 60000));
        capabilities.setCapability("uiautomator2ServerInstallTimeout", ConfigReader.getIntProperty("android.serverInstallTimeout", 30000));
        
        // Additional Android capabilities
        capabilities.setCapability("skipUnlock", ConfigReader.getBooleanProperty("android.skipUnlock", true));
        capabilities.setCapability("unlockType", ConfigReader.getProperty("android.unlockType", "pin"));
        capabilities.setCapability("unlockKey", ConfigReader.getProperty("android.unlockKey", "1234"));
        
        // Logging
        capabilities.setCapability("enablePerformanceLogging", ConfigReader.getBooleanProperty("android.enablePerformanceLogging", false));
        
        logger.info("Android capabilities configured: {}", capabilities.asMap());
        return capabilities;
    }
    
    /**
     * Get iOS capabilities
     * @return DesiredCapabilities for iOS
     */
    private static DesiredCapabilities getIOSCapabilities() {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        
        // Platform capabilities
        capabilities.setCapability("platformName", "iOS");
        capabilities.setCapability("automationName", "XCUITest");
        capabilities.setCapability("deviceName", ConfigReader.getProperty("ios.deviceName", "iPhone Simulator"));
        capabilities.setCapability("platformVersion", ConfigReader.getProperty("ios.platformVersion", "15.0"));
        
        // App capabilities
        String appPath = ConfigReader.getProperty("ios.appPath");
        if (appPath != null && !appPath.isEmpty()) {
            capabilities.setCapability("app", appPath);
        } else {
            capabilities.setCapability("bundleId", ConfigReader.getProperty("ios.bundleId", AppConstants.APP_PACKAGE));
        }
        
        // Performance and behavior capabilities
        capabilities.setCapability("noReset", ConfigReader.getBooleanProperty("ios.noReset", false));
        capabilities.setCapability("fullReset", ConfigReader.getBooleanProperty("ios.fullReset", false));
        capabilities.setCapability("autoAcceptAlerts", ConfigReader.getBooleanProperty("ios.autoAcceptAlerts", true));
        capabilities.setCapability("autoDismissAlerts", ConfigReader.getBooleanProperty("ios.autoDismissAlerts", true));
        
        // XCUITest specific
        capabilities.setCapability("wdaLaunchTimeout", ConfigReader.getIntProperty("ios.wdaLaunchTimeout", 60000));
        capabilities.setCapability("wdaConnectionTimeout", ConfigReader.getIntProperty("ios.wdaConnectionTimeout", 60000));
        
        // Timeouts
        capabilities.setCapability("newCommandTimeout", ConfigReader.getIntProperty("ios.newCommandTimeout", 300));
        
        logger.info("iOS capabilities configured: {}", capabilities.asMap());
        return capabilities;
    }
    
    /**
     * Configure driver with timeouts and settings
     * @param appiumDriver The driver to configure
     */
    private static void configureDriver(AppiumDriver appiumDriver) {
        // Set implicit wait
        appiumDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(AppConstants.IMPLICIT_WAIT));
        
        // Set page load timeout
        appiumDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(AppConstants.PAGE_LOAD_TIMEOUT));
        
        logger.info("Driver configured with timeouts - Implicit: {}s, Page Load: {}s", 
                   AppConstants.IMPLICIT_WAIT, AppConstants.PAGE_LOAD_TIMEOUT);
    }
    
    /**
     * Get Appium server URL
     * @return URL of Appium server
     */
    private static URL getAppiumServerURL() {
        try {
            String serverUrl = ConfigReader.getProperty("appium.serverUrl", AppConstants.APPIUM_SERVER_URL);
            String serverPath = ConfigReader.getProperty("appium.serverPath", AppConstants.APPIUM_SERVER_PATH);
            
            if (!serverUrl.endsWith("/") && !serverPath.startsWith("/")) {
                serverUrl += "/";
            }
            
            URL url = new URL(serverUrl + serverPath.substring(1));
            logger.info("Appium server URL: {}", url);
            return url;
            
        } catch (MalformedURLException e) {
            logger.error("Invalid Appium server URL", e);
            throw new RuntimeException("Invalid Appium server URL", e);
        }
    }
    
    /**
     * Quit the current driver and remove from ThreadLocal
     */
    public static void quitDriver() {
        AppiumDriver currentDriver = getDriver();
        if (currentDriver != null) {
            try {
                logger.info("Quitting driver");
                currentDriver.quit();
            } catch (Exception e) {
                logger.warn("Error while quitting driver", e);
            } finally {
                driver.remove();
                logger.info("Driver removed from ThreadLocal");
            }
        }
    }
    
    /**
     * Check if driver is initialized
     * @return true if driver exists, false otherwise
     */
    public static boolean isDriverInitialized() {
        return getDriver() != null;
    }
    
    /**
     * Get current platform name
     * @return Platform name (Android/iOS)
     */
    public static String getCurrentPlatform() {
        AppiumDriver currentDriver = getDriver();
        if (currentDriver != null) {
            return currentDriver.getCapabilities().getCapability("platformName").toString();
        }
        return "Unknown";
    }
    
    /**
     * Get current device name
     * @return Device name
     */
    public static String getCurrentDeviceName() {
        AppiumDriver currentDriver = getDriver();
        if (currentDriver != null) {
            return currentDriver.getCapabilities().getCapability("deviceName").toString();
        }
        return "Unknown";
    }
    
    /**
     * Restart the app
     */
    public static void restartApp() {
        AppiumDriver currentDriver = getDriver();
        if (currentDriver != null) {
            logger.info("Restarting app");
            try {
                if (currentDriver instanceof AndroidDriver) {
                    currentDriver.executeScript("mobile: terminateApp", java.util.Map.of("appId", AppConstants.APP_PACKAGE));
                    currentDriver.executeScript("mobile: activateApp", java.util.Map.of("appId", AppConstants.APP_PACKAGE));
                } else if (currentDriver instanceof IOSDriver) {
                    currentDriver.executeScript("mobile: terminateApp", java.util.Map.of("bundleId", AppConstants.APP_BUNDLE_ID));
                    currentDriver.executeScript("mobile: activateApp", java.util.Map.of("bundleId", AppConstants.APP_BUNDLE_ID));
                }
                logger.info("App restarted successfully");
            } catch (Exception e) {
                logger.warn("Failed to restart app", e);
            }
        }
    }
    
    /**
     * Put app in background for specified duration
     * @param duration Duration in seconds
     */
    public static void backgroundApp(int duration) {
        AppiumDriver currentDriver = getDriver();
        if (currentDriver != null) {
            logger.info("Putting app in background for {} seconds", duration);
            try {
                if (currentDriver instanceof AndroidDriver) {
                    currentDriver.executeScript("mobile: backgroundApp", java.util.Map.of("seconds", duration));
                } else if (currentDriver instanceof IOSDriver) {
                    currentDriver.executeScript("mobile: backgroundApp", java.util.Map.of("seconds", duration));
                }
                logger.info("App brought back to foreground");
            } catch (Exception e) {
                logger.warn("Failed to background app", e);
            }
        }
    }
    
    /**
     * Reset app to initial state
     */
    public static void resetApp() {
        AppiumDriver currentDriver = getDriver();
        if (currentDriver != null) {
            logger.info("Resetting app");
            try {
                if (currentDriver instanceof AndroidDriver) {
                    currentDriver.executeScript("mobile: terminateApp", java.util.Map.of("appId", AppConstants.APP_PACKAGE));
                    currentDriver.executeScript("mobile: clearApp", java.util.Map.of("appId", AppConstants.APP_PACKAGE));
                    currentDriver.executeScript("mobile: activateApp", java.util.Map.of("appId", AppConstants.APP_PACKAGE));
                } else if (currentDriver instanceof IOSDriver) {
                    currentDriver.executeScript("mobile: terminateApp", java.util.Map.of("bundleId", AppConstants.APP_BUNDLE_ID));
                    currentDriver.executeScript("mobile: clearApp", java.util.Map.of("bundleId", AppConstants.APP_BUNDLE_ID));
                    currentDriver.executeScript("mobile: activateApp", java.util.Map.of("bundleId", AppConstants.APP_BUNDLE_ID));
                }
                logger.info("App reset completed");
            } catch (Exception e) {
                logger.warn("Failed to reset app", e);
            }
        }
    }
}