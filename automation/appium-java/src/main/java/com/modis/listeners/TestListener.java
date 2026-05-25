package com.modis.listeners;

import com.modis.utils.LoggerUtil;
import com.modis.utils.ScreenshotUtils;
import org.slf4j.Logger;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {
    
    private static final Logger logger = LoggerUtil.getLogger(TestListener.class);
    
    @Override
    public void onTestStart(ITestResult result) {
        String testClass = result.getTestClass().getName();
        String testMethod = result.getMethod().getMethodName();
        
        LoggerUtil.logTestStart(logger, testClass, testMethod);
        
        // Set test context for logging
        LoggerUtil.setTestContext(testClass, testMethod);
        
        logger.info("Test parameters: {}", java.util.Arrays.toString(result.getParameters()));
    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
        String testClass = result.getTestClass().getName();
        String testMethod = result.getMethod().getMethodName();
        long duration = result.getEndMillis() - result.getStartMillis();
        
        LoggerUtil.logTestEnd(logger, testClass, testMethod, "PASS", duration);
        
        // Take screenshot on success if configured
        if (shouldTakeScreenshotOnSuccess()) {
            String screenshotName = String.format("%s_%s_PASSED", testClass, testMethod);
            ScreenshotUtils.takeScreenshot(screenshotName);
        }
        
        // Clear test context
        LoggerUtil.clearTestContext();
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        String testClass = result.getTestClass().getName();
        String testMethod = result.getMethod().getMethodName();
        long duration = result.getEndMillis() - result.getStartMillis();
        
        LoggerUtil.logTestEnd(logger, testClass, testMethod, "FAIL", duration);
        
        // Log failure details
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            LoggerUtil.logException(logger, testMethod, new Exception(throwable));
        }
        
        // Take screenshot on failure
        String screenshotName = String.format("%s_%s_FAILED", testClass, testMethod);
        String screenshotPath = ScreenshotUtils.takeFailureScreenshot(testClass, testMethod);
        
        if (screenshotPath != null) {
            logger.info("Failure screenshot saved: {}", screenshotPath);
            
            // Attach screenshot to TestNG result for reporting
            System.setProperty("screenshot.path", screenshotPath);
        }
        
        // Clear test context
        LoggerUtil.clearTestContext();
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        String testClass = result.getTestClass().getName();
        String testMethod = result.getMethod().getMethodName();
        long duration = result.getEndMillis() - result.getStartMillis();
        
        LoggerUtil.logTestEnd(logger, testClass, testMethod, "SKIP", duration);
        
        // Log skip reason
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            logger.warn("Test skipped reason: {}", throwable.getMessage());
        }
        
        // Clear test context
        LoggerUtil.clearTestContext();
    }
    
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        String testClass = result.getTestClass().getName();
        String testMethod = result.getMethod().getMethodName();
        
        logger.warn("Test failed but within success percentage: {}.{}", testClass, testMethod);
        
        // Handle as partial success
        onTestSuccess(result);
    }

    private boolean shouldTakeScreenshotOnSuccess() {
        String takeScreenshotOnPass = System.getProperty("screenshot.onPass", "true");
        return Boolean.parseBoolean(takeScreenshotOnPass);
    }
}