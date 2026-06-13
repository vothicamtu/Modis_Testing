package com.modis.listeners;

import com.aventstack.extentreports.*;
import com.modis.utils.ExtentManager;
import com.modis.utils.LoggerUtil;
import com.modis.utils.ScreenshotUtils;
import org.slf4j.Logger;
import org.testng.*;

public class TestListener implements ITestListener {
    private static final Logger logger =
            LoggerUtil.getLogger(TestListener.class);
    private static final ExtentReports extent =
            ExtentManager.getInstance();
    private static final ThreadLocal<ExtentTest> test =
            new ThreadLocal<>();

    @Override
    public void onTestStart(ITestResult result) {
        String testClass =
                result.getTestClass().getName();
        String testMethod =
                result.getMethod().getMethodName();
        LoggerUtil.logTestStart(
                logger,
                testClass,
                testMethod
        );
        LoggerUtil.setTestContext(
                testClass,
                testMethod
        );
        logger.info(
                "Test parameters: {}",
                java.util.Arrays.toString(
                        result.getParameters()
                )
        );
        ExtentTest extentTest =
                extent.createTest(
                        testMethod
                );
        extentTest.info(
                "Test Started: " +
                        testClass +
                        "." +
                        testMethod
        );
        test.set(extentTest);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testClass =
                result.getTestClass().getName();
        String testMethod =
                result.getMethod().getMethodName();
        long duration =
                result.getEndMillis() -
                        result.getStartMillis();
        LoggerUtil.logTestEnd(
                logger,
                testClass,
                testMethod,
                "PASS",
                duration
        );
        test.get().pass(
                "Test Passed in " +
                        duration +
                        " ms"
        );
        if (shouldTakeScreenshotOnSuccess()) {
            String screenshotName =
                    String.format(
                            "%s_%s_PASSED",
                            testClass,
                            testMethod
                    );
            String screenshotPath =
                    ScreenshotUtils.takeScreenshot(
                            screenshotName
                    );
            try {
                if (screenshotPath != null) {
                    test.get()
                            .addScreenCaptureFromPath(
                                    screenshotPath
                            );
                }
            } catch (Exception e) {
                logger.warn(
                        "Could not attach screenshot"
                );
            }
        }
        LoggerUtil.clearTestContext();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testClass = result.getTestClass().getName();
        String testMethod = result.getMethod().getMethodName();
        long duration = result.getEndMillis() - result.getStartMillis();
        LoggerUtil.logTestEnd(logger, testClass, testMethod, "FAIL", duration);
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            LoggerUtil.logException(logger, testMethod, new Exception(throwable));
            test.get().fail(throwable);
        }
        String screenshotPath = ScreenshotUtils.takeFailureScreenshot(testClass, testMethod);
        try {
            if (screenshotPath != null) {
                logger.info("Failure screenshot saved: {}", screenshotPath);
                test.get().addScreenCaptureFromPath(screenshotPath);
            }
        } catch (Exception e) {
            logger.warn("Could not attach failure screenshot");
        }
        LoggerUtil.clearTestContext();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testClass =
                result.getTestClass().getName();
        String testMethod =
                result.getMethod().getMethodName();
        long duration =
                result.getEndMillis() -
                        result.getStartMillis();
        LoggerUtil.logTestEnd(
                logger,
                testClass,
                testMethod,
                "SKIP",
                duration
        );
        Throwable throwable =
                result.getThrowable();
        if (throwable != null) {
            logger.warn(
                    "Test skipped reason: {}",
                    throwable.getMessage()
            );
            test.get().skip(
                    throwable.getMessage()
            );
        } else {
            test.get().skip(
                    "Test Skipped"
            );
        }
        LoggerUtil.clearTestContext();
    }

    @Override
    public void onFinish(ITestContext context) {
        logger.info(
                "Generating Extent Report..."
        );
        extent.flush();
        logger.info(
                "Extent Report Generated Successfully"
        );
    }

    private boolean shouldTakeScreenshotOnSuccess() {
        String takeScreenshotOnPass =
                System.getProperty(
                        "screenshot.onPass",
                        "true"
                );
        return Boolean.parseBoolean(
                takeScreenshotOnPass
        );
    }
}