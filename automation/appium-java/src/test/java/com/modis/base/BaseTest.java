package com.modis.base;

import com.modis.constants.AppConstants;
import com.modis.drivers.DriverManager;
import com.modis.listeners.TestListener;
import com.modis.utils.ConfigReader;
import com.modis.utils.LoggerUtil;
import com.modis.utils.LogoutHelper;
import com.modis.utils.ScreenshotUtils;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * BaseTest theo chiến lược Android 11 real device:
 * - @BeforeClass: tạo driver 1 lần duy nhất cho cả class
 * - @BeforeMethod: nếu driver null -> tạo mới; nếu có -> terminate + activate (không clear data)
 *   rồi logout nếu đang login
 * - @AfterMethod: screenshot khi fail
 * - @AfterClass: quit driver
 */
@Listeners({TestListener.class})
public abstract class BaseTest {

    protected final Logger logger = LoggerUtil.getLogger(this.getClass());
    protected AppiumDriver driver;

    @Parameters({"platform", "deviceName", "platformVersion"})
    @BeforeClass(alwaysRun = true)
    public void setUpClass(@Optional("android") String platform,
                           @Optional("Android") String deviceName,
                           @Optional("11.0") String platformVersion) {

        logger.info("=== Starting Test Class: {} ===", this.getClass().getSimpleName());
        logger.info("Platform: {}, Device: {}, Version: {}", platform, deviceName, platformVersion);

        System.setProperty("platform", platform);
        System.setProperty("deviceName", deviceName);
        System.setProperty("platformVersion", platformVersion);

        ConfigReader.loadConfiguration();

        // Tạo driver 1 lần duy nhất cho class
        driver = initializeDriver(platform);
    }

    @BeforeMethod(alwaysRun = true)
    public void setUpMethod(Method method) {
        logger.info("--- Starting Test Method: {} ---", method.getName());

        String platform = System.getProperty("platform", "android");
        AppiumDriver current = DriverManager.getDriver();
        if (current != null) {
            driver = current;
        }

        // SIMPLIFIED: Chỉ tạo driver mới nếu null, không restart app mỗi test
        if (driver == null || !DriverManager.isSessionAlive()) {
            logger.info("Creating new driver for test method");
            driver = initializeDriver(platform);
        } else {
            logger.info("Reusing existing driver session");
            relaunchApp();
        }

        // SIMPLIFIED: Chỉ logout nếu cần, không force restart
        try {
            LogoutHelper.logoutIfLoggedIn(driver);
        } catch (Exception e) {
            logger.warn("Logout helper failed (non-fatal): {}", e.getMessage());
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownMethod(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            try {
                ScreenshotUtils.takeFailureScreenshot(this.getClass().getSimpleName(),
                        result.getMethod().getMethodName());
            } catch (Exception e) {
                logger.warn("Không thể chụp screenshot khi fail (non-fatal): {}", e.getMessage());
            }
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            long durationMs = Math.max(0, result.getEndMillis() - result.getStartMillis());
            String platform = System.getProperty("platform", "unknown");
            String paramDeviceName = System.getProperty("deviceName", "unknown");

            String capDeviceName = "unknown";
            try {
                AppiumDriver d = DriverManager.getDriver();
                if (d != null && d.getCapabilities() != null) {
                    Object cap = d.getCapabilities().getCapability("appium:deviceName");
                    if (cap == null) cap = d.getCapabilities().getCapability("deviceName");
                    if (cap != null) capDeviceName = String.valueOf(cap);
                }
            } catch (Exception ignored) {
            }

            String deviceName = String.format("param=%s,cap=%s", paramDeviceName, capDeviceName);
            logger.info("PASS | {}ms | {} | {} | {}.{}",
                    durationMs,
                    platform,
                    deviceName,
                    result.getTestClass().getName(),
                    result.getMethod().getMethodName());
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        logger.info("=== Finishing Test Class: {} ===", this.getClass().getSimpleName());
        try {
            DriverManager.quitDriver();
        } catch (Exception e) {
            logger.warn("Quit driver failed (non-fatal): {}", e.getMessage());
        } finally {
            driver = null;
        }
    }

    private AppiumDriver initializeDriver(String platform) {
        try {
            AppiumDriver created = DriverManager.createDriver(platform);
            this.driver = created;
            return created;
        } catch (Exception e) {
            logger.error("Driver initialization failed for platform: {}", platform, e);
            throw new RuntimeException("Driver initialization failed", e);
        }
    }

    /**
     * Terminate + Activate để về màn hình đầu (không clear data).
     * Nếu relaunch fail hoặc UiAutomator2 không khỏe -> attempt recovery/recreate.
     */
    protected void relaunchApp() {
        if (driver == null) return;

        String platform = System.getProperty("platform", "android");

        // Nếu Android và UiAutomator2 đã die -> recover trước khi relaunch
        if ("android".equalsIgnoreCase(platform) && !DriverManager.isUiAutomator2Healthy()) {
            logger.warn("UiAutomator2 không khỏe trước relaunch -> attempt recover");
            boolean ok = DriverManager.recoverFromUiAutomator2Crash();
            driver = DriverManager.getDriver();
            if (!ok || driver == null) {
                logger.warn("Recover thất bại -> tạo driver mới");
                driver = initializeDriver(platform);
                return;
            }
        }

        try {
            if (driver instanceof AndroidDriver) {
                AndroidDriver android = (AndroidDriver) driver;
                logger.info("Relaunch app (Android): terminate + activate {}", AppConstants.APP_PACKAGE);
                android.terminateApp(AppConstants.APP_PACKAGE);
                android.activateApp(AppConstants.APP_PACKAGE);

                // Wait bounded cho app lên foreground
                new WebDriverWait(android, Duration.ofSeconds(10)).until(d -> {
                    try {
                        return AppConstants.APP_PACKAGE.equals(android.getCurrentPackage());
                    } catch (Exception e) {
                        return false;
                    }
                });
            } else if (driver instanceof IOSDriver) {
                IOSDriver ios = (IOSDriver) driver;
                logger.info("Relaunch app (iOS): terminate + activate {}", AppConstants.APP_BUNDLE_ID);
                ios.terminateApp(AppConstants.APP_BUNDLE_ID);
                ios.activateApp(AppConstants.APP_BUNDLE_ID);
            } else {
                // Nếu driver type khác, vẫn cố gắng không crash (best-effort)
                logger.warn("Driver type không xác định cho relaunch, skip");
            }
        } catch (Exception e) {
            logger.warn("Relaunch app failed -> recreate driver (non-fatal): {}", e.getMessage());
            driver = initializeDriver(platform);
        }
    }

    /**
     * Đưa app xuống background rồi đưa lên lại (một số test case đang dùng).
     */
    protected void backgroundApp(int seconds) {
        try {
            DriverManager.backgroundApp(seconds);
        } catch (Exception e) {
            logger.warn("backgroundApp failed (non-fatal): {}", e.getMessage());
        }
    }
}
