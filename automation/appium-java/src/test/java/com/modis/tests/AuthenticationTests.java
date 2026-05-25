package com.modis.tests;

import com.modis.base.BasePage;
import com.modis.base.BaseTest;
import com.modis.pages.LoadingPage;
import com.modis.pages.LoginPage;
import com.modis.pages.SignupPage;
import com.modis.utils.LoggerUtil;
import com.modis.utils.ScreenshotUtils;

import org.slf4j.Logger;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class AuthenticationTests extends BaseTest {

    private static final Logger logger =
            LoggerUtil.getLogger(AuthenticationTests.class);

    // LOGIN TESTS

    @Test( groups = {"authentication", "regression"},description = "Login submit must stay disabled until both fields are filled")
    public void testLoginButtonDisabledWhenFieldsMissing() {

        LoginPage loginPage = openLoginPage();

        logger.info("Clearing all login fields");
        loginPage.clearAllFields();

        logger.info("Verifying login page still displayed");
        Assert.assertTrue(
                loginPage.isDisplayed(),
                "Login page should remain visible"
        );

        logger.info("Verifying login button is disabled");
        Assert.assertFalse(
                loginPage.isLoginButtonEnabled(),
                "Login button should be disabled when username and password are empty"
        );
    }

    @Test(groups = {"authentication", "regression"},description = "Login invalid username should show the auth dialog and stop the flow")
    public void testLoginShowsDialogForUnknownUsername() {

        LoginPage loginPage = openLoginPage();

        String username =
                "missing_user_" + System.currentTimeMillis();

        logger.info("Entering invalid username: {}", username);

        loginPage.enterUsername(username);
        loginPage.enterPassword("Password123");

        logger.info("Submitting login form");

        BasePage submitResult =
                loginPage.clickLoginButton();

        String dialogMessageRaw =
                loginPage.getAuthDialogMessage();

        logger.info("Verifying still on LoginPage");
        Assert.assertTrue(
                submitResult instanceof LoginPage,
                "Invalid login should stay on the login screen"
        );

        logger.info("Verifying auth dialog is visible");
        Assert.assertTrue(
                loginPage.isAuthDialogVisible(),
                "Expected the auth dialog to appear for an invalid login"
        );

        logger.info("Verifying auth dialog message");
        Assert.assertFalse(
                dialogMessageRaw == null || dialogMessageRaw.isBlank(),
                "Expected an auth dialog message for the invalid username flow"
        );

        Assert.assertEquals(
                dialogMessageRaw,
                "Tài khoản không tồn tại!",
                "Expected the invalid username dialog to show account-not-found message"
        );

        logger.info("Dismissing auth dialog");

        loginPage.dismissLoginErrorDialog();

        Assert.assertFalse(
                loginPage.isAuthDialogVisible(),
                "Dialog should close after OK"
        );
    }

    @Test(groups = {"authentication", "regression"}, description = "Login wrong password should show the auth dialog and stop the flow")
    public void testLoginShowsDialogForWrongPassword() {

        LoginPage loginPage = openLoginPage();

        logger.info("Entering valid username with wrong password");

        loginPage.enterUsername("testuser");
        loginPage.enterPassword("wrong_password");

        BasePage submitResult =
                loginPage.clickLoginButton();

        String dialogMessageRaw =
                loginPage.getAuthDialogMessage();

        Assert.assertTrue(
                submitResult instanceof LoginPage,
                "Invalid password should keep the user on the login screen"
        );

        Assert.assertTrue(
                loginPage.isAuthDialogVisible(),
                "Expected the auth dialog to appear for a wrong password"
        );

        Assert.assertFalse(
                dialogMessageRaw == null || dialogMessageRaw.isBlank(),
                "Expected an auth dialog message for the wrong-password flow"
        );

        Assert.assertEquals(
                dialogMessageRaw,
                "Tài khoản không tồn tại!",
                "Expected the exact Vietnamese wrong-password dialog text"
        );

        loginPage.dismissLoginErrorDialog();

        Assert.assertFalse(
                loginPage.isAuthDialogVisible(),
                "Dialog should close after OK"
        );
    }

    // SIGNUP TESTS

    @Test( groups = {"authentication", "regression"}, description = "Signup submit must stay disabled until all required fields are filled")
    public void testSignupButtonDisabledWhenRequiredFieldsMissing() {

        SignupPage signupPage = openSignupPage();

        logger.info("Entering partial signup form");

        signupPage.enterUsername("signup_blocker");
        signupPage.enterPassword("Password123");

        logger.info("Verifying signup button remains disabled");

        Assert.assertFalse(
                signupPage.isSignupButtonEnabled(),
                "Signup button should remain disabled until all required fields are filled"
        );
    }

    @Test(groups = {"authentication", "regression"}, description = "Signup mismatched passwords should show the auth dialog and stop the flow")
    public void testSignupShowsDialogWhenPasswordsMismatch() {

        SignupPage signupPage = openSignupPage();

        String timestamp =
                String.valueOf(System.currentTimeMillis());

        logger.info("Entering signup data with mismatched passwords");

        signupPage.enterUsername("signup_mismatch_" + timestamp);
        signupPage.enterFullname("Signup Mismatch");
        signupPage.enterEmail("signup_mismatch_" + timestamp + "@example.com");
        signupPage.enterPhone("1234567890");
        signupPage.enterPassword("Password123");
        signupPage.enterConfirmPassword("DifferentPassword");

        BasePage submitResult =
                signupPage.clickSignupButton();

        String dialogMessageRaw =
                signupPage.getAuthDialogMessage();

        Assert.assertTrue(
                submitResult instanceof SignupPage,
                "Mismatched signup should stay on signup screen"
        );

        Assert.assertTrue(
                signupPage.isAuthDialogVisible(),
                "Expected the auth dialog to appear for invalid signup input"
        );

        Assert.assertFalse(
                dialogMessageRaw == null || dialogMessageRaw.isBlank(),
                "Expected an auth dialog message for the mismatched password flow"
        );

        logger.info("Dismiss signup error dialog");

        signupPage.dismissSignupErrorDialog();

        Assert.assertFalse(
                signupPage.isAuthDialogVisible(),
                "Dialog should close after OK"
        );
    }

    // PASS / FAIL LOGGER

    @AfterMethod(alwaysRun = true)
    public void logAuthenticationResult(ITestResult result) {

        String testName =
                result.getMethod().getMethodName();

        String description =
                result.getMethod().getDescription();

        long duration =
                result.getEndMillis() - result.getStartMillis();

        if (result.getStatus() == ITestResult.SUCCESS) {

            logger.info("");
            
            logger.info("AUTH TEST PASSED");
            logger.info("Test Case : {}", testName);
            logger.info("Description : {}", description);
            logger.info("Reason : {}", getPassReason(testName));
            logger.info("Duration : {} ms", duration);
            
            logger.info("");

            ScreenshotUtils.takeScreenshot(
                    "PASS_" + testName
            );
        } else if (result.getStatus() == ITestResult.FAILURE) {

            logger.error("");
            
            logger.error("AUTH TEST FAILED");
            logger.error("Test Case : {}", testName);
            logger.error("Description : {}", description);
            logger.error("Error : {}",
                    result.getThrowable() != null
                            ? result.getThrowable().getMessage()
                            : "Unknown Error");
            logger.error("Duration : {} ms", duration);
            
            logger.error("");
        }
    }

    private String getPassReason(String testName) {

        switch (testName) {

            case "testLoginButtonDisabledWhenFieldsMissing":
                return "System correctly prevented login with empty credentials";

            case "testLoginShowsDialogForUnknownUsername":
                return "System correctly blocked login for unknown username";

            case "testLoginShowsDialogForWrongPassword":
                return "System correctly blocked login for wrong password";

            case "testSignupButtonDisabledWhenRequiredFieldsMissing":
                return "System correctly prevented incomplete signup submission";

            case "testSignupShowsDialogWhenPasswordsMismatch":
                return "System correctly validated password confirmation mismatch";

            default:
                return "All assertions passed successfully";
        }
    }

    // NAVIGATION HELPERS

    private LoginPage openLoginPage() {

        logger.info("Opening Login Page");

        LoadingPage loadingPage = new LoadingPage();

        loadingPage.waitForPageToLoad();
        loadingPage.waitForLoginSignupButtonsVisible();

        LoginPage loginPage =
                loadingPage.clickLoginButton();

        loginPage.waitForPageToLoad();

        return loginPage;
    }

    private SignupPage openSignupPage() {

        logger.info("Opening Signup Page");

        LoadingPage loadingPage =
                new LoadingPage();

        loadingPage.waitForPageToLoad();
        loadingPage.waitForLoginSignupButtonsVisible();

        SignupPage signupPage =
                loadingPage.clickSignupButton();

        signupPage.waitForPageToLoad();

        return signupPage;
    }
}