package com.modis.tests;

import com.modis.base.BasePage;
import com.modis.base.BaseTest;

import com.modis.pages.HomePage;
import com.modis.pages.LoadingPage;
import com.modis.pages.LoginPage;
import com.modis.pages.SignupPage;
import com.modis.pages.TakePage;

import com.modis.utils.LoggerUtil;
import com.modis.utils.ScreenshotUtils;
import com.modis.utils.TestDataReader;

import org.slf4j.Logger;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;

public class AuthenticationTests extends BaseTest {

    private static final Logger logger = LoggerUtil.getLogger(AuthenticationTests.class);
    private final TestDataReader testDataReader = new TestDataReader();


    // LOGIN TESTS
    @Test(groups = {"authentication", "regression"}, description = "Login submit must stay disabled until both fields are filled")
    public void testLoginButtonDisabledWhenFieldsMissing() {
        LoginPage loginPage = openLoginPage();
        logger.info("Clearing all login fields");
        loginPage.clearAllFields();
        Assert.assertTrue(loginPage.isDisplayed(), "Login page should remain visible");
        Assert.assertFalse(loginPage.isLoginButtonEnabled(), "Login button should be disabled when username and password are empty");
    }

    @Test(groups = {"authentication", "regression"}, description = "Login invalid username should show auth dialog")
    public void testLoginShowsDialogForUnknownUsername() {
        LoginPage loginPage = openLoginPage();
        List<Map<String, Object>> invalidCredentials = testDataReader.getInvalidLoginCredentials();
        Map<String, Object> invalidUser = invalidCredentials.get(0);
        String username = (String) invalidUser.get("username");
        String password = (String) invalidUser.get("password");
        logger.info("Using invalid username from JSON: {}", username);

        loginPage.enterUsername(username);
        loginPage.enterPassword(password);

        BasePage submitResult = loginPage.clickLoginButton();

        String dialogMessage = loginPage.getAuthDialogMessage();
        Assert.assertTrue(submitResult instanceof LoginPage, "Invalid login should stay on LoginPage");
        Assert.assertTrue(loginPage.isAuthDialogVisible(), "Auth dialog should appear");
        Assert.assertFalse(dialogMessage == null || dialogMessage.isBlank(), "Dialog message should not be empty");

        loginPage.dismissLoginErrorDialog();
        Assert.assertFalse(loginPage.isAuthDialogVisible(), "Dialog should close after dismiss");

        ScreenshotUtils.takeScreenshot("INVALID_USERNAME_DIALOG");
    }

    @Test(groups = {"authentication", "regression"}, description = "Login wrong password should show auth dialog")
    public void testLoginShowsDialogForWrongPassword() {
        LoginPage loginPage = openLoginPage();
        List<Map<String, Object>> invalidCredentials = testDataReader.getInvalidLoginCredentials();
        Map<String, Object> invalidPasswordUser = invalidCredentials.get(1);
        String username = (String) invalidPasswordUser.get("username");
        String password = (String) invalidPasswordUser.get("password");
        logger.info("Using invalid password test data for user: {}", username);

        loginPage.enterUsername(username);
        loginPage.enterPassword(password);

        BasePage submitResult = loginPage.clickLoginButton();

        String dialogMessage = loginPage.getAuthDialogMessage();
        Assert.assertTrue(submitResult instanceof LoginPage, "Invalid password should stay on LoginPage");
        Assert.assertTrue(loginPage.isAuthDialogVisible(), "Auth dialog should appear");
        Assert.assertFalse(dialogMessage == null || dialogMessage.isBlank(), "Dialog message should not be empty");

        loginPage.dismissLoginErrorDialog();

        Assert.assertFalse(loginPage.isAuthDialogVisible(), "Dialog should close after dismiss");

        ScreenshotUtils.takeScreenshot("WRONG_PASSWORD_DIALOG");
    }

    @Test(groups = {"authentication", "regression"}, description = "Empty credentials should show validation error")
    public void testLoginWithEmptyCredentials() {
        LoginPage loginPage = openLoginPage();
        List<Map<String, Object>> invalidCredentials = testDataReader.getInvalidLoginCredentials();
        Map<String, Object> emptyData = invalidCredentials.get(4);
        String username = (String) emptyData.get("username");
        String password = (String) emptyData.get("password");
        logger.info("Testing empty credentials validation");

        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
        Assert.assertFalse(loginPage.isLoginButtonEnabled(), "Login button should stay disabled");

        ScreenshotUtils.takeScreenshot("EMPTY_CREDENTIALS_VALIDATION");
    }

    @Test(groups = {"authentication", "regression"}, description = "Empty username should show validation error")
    public void testLoginWithEmptyUsername() {
        LoginPage loginPage = openLoginPage();
        List<Map<String, Object>> invalidCredentials = testDataReader.getInvalidLoginCredentials();
        Map<String, Object> emptyUsername = invalidCredentials.get(5);
        String username = (String) emptyUsername.get("username");
        String password = (String) emptyUsername.get("password");
        logger.info("Testing empty username validation");
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
        Assert.assertFalse(loginPage.isLoginButtonEnabled(), "Login button should remain disabled");

        ScreenshotUtils.takeScreenshot("EMPTY_USERNAME_VALIDATION");
    }

    @Test(groups = {"authentication", "regression"}, description = "Empty password should show validation error")
    public void testLoginWithEmptyPassword() {
        LoginPage loginPage = openLoginPage();
        List<Map<String, Object>> invalidCredentials = testDataReader.getInvalidLoginCredentials();
        Map<String, Object> emptyPassword = invalidCredentials.get(6);
        String username = (String) emptyPassword.get("username");
        String password = (String) emptyPassword.get("password");
        logger.info("Testing empty password validation");

        loginPage.enterUsername(username);
        loginPage.enterPassword(password);

        Assert.assertFalse(loginPage.isLoginButtonEnabled(), "Login button should remain disabled");
        ScreenshotUtils.takeScreenshot("EMPTY_PASSWORD_VALIDATION");
    }

    @Test(groups = {"authentication", "smoke", "regression"}, description = "Valid login should navigate to Home and Take screen")
    public void testLoginSuccessfullyNavigateToHomeAndTake() {
        LoginPage loginPage = openLoginPage();
        List<Map<String, Object>> validCredentials = testDataReader.getValidLoginCredentials();
        Map<String, Object> validUser = validCredentials.get(0);

        String username = (String) validUser.get("username");
        String password = (String) validUser.get("password");

        logger.info("Logging in with valid JSON user: {}", username);
        BasePage afterLogin = loginPage.login(username, password);

        Assert.assertTrue(afterLogin instanceof HomePage, "Login should navigate to HomePage");

        HomePage homePage = (HomePage) afterLogin;
        logger.info("Waiting for authenticated Home screen");
        homePage.waitForTopbarReadyAfterLogin(8);
        Assert.assertTrue(homePage.isDisplayed(), "Home page should display");
        logger.info("Home page detected successfully after login");
        ScreenshotUtils.takeScreenshot("LOGIN_SUCCESS_HOME");
        logger.info("Navigating to Take screen");
        TakePage takePage = homePage.navigateToCamera();
        Assert.assertNotNull(takePage, "TakePage should not be null");
        takePage.waitForPageToLoad();
        Assert.assertTrue(takePage.isDisplayed(), "Take screen should display");
        ScreenshotUtils.takeScreenshot("LOGIN_SUCCESS_TAKE");
        logger.info("Authentication login flow completed successfully");
    }


    // SIGNUP TESTS
    @Test(groups = {"authentication", "regression"}, description = "Signup submit must stay disabled until all required fields are filled")
    public void testSignupButtonDisabledWhenRequiredFieldsMissing() {
        SignupPage signupPage = openSignupPage();
        logger.info("Entering partial signup form");
        signupPage.enterUsername("signup_blocker");
        signupPage.enterPassword("Password123");
        Assert.assertFalse(signupPage.isSignupButtonEnabled(), "Signup button should remain disabled");
    }

    @Test(groups = {"authentication", "regression"}, description = "Signup mismatched passwords should show auth dialog")
    public void testSignupShowsDialogWhenPasswordsMismatch() {

        SignupPage signupPage = openSignupPage();
        String timestamp = String.valueOf(System.currentTimeMillis());

        signupPage.enterUsername("signup_mismatch_" + timestamp);
        signupPage.enterFullname("Signup Mismatch");
        signupPage.enterEmail("signup_mismatch_" + timestamp + "@example.com");
        signupPage.enterPhone("1234567890");
        signupPage.enterPassword("Password123");
        signupPage.enterConfirmPassword("DifferentPassword");

        BasePage submitResult = signupPage.clickSignupButton();

        // Verify vẫn ở signup page
        Assert.assertTrue(submitResult instanceof SignupPage, "Signup should stay on SignupPage"
        );

        // Verify dialog hiện
        Assert.assertTrue(signupPage.isAuthDialogVisible(), "Auth dialog should display");
        // Verify exact dialog message
        String dialogMessage = signupPage.getAuthDialogMessage();

        Assert.assertEquals(Normalizer.normalize(dialogMessage.trim(), Normalizer.Form.NFC), Normalizer.normalize("Mật khẩu không khớp", Normalizer.Form.NFC), "Wrong signup mismatch dialog message");

        // Close dialog
        signupPage.dismissSignupErrorDialog();

        // Verify dialog đóng
        Assert.assertFalse(signupPage.isAuthDialogVisible(), "Dialog should close");
        ScreenshotUtils.takeScreenshot("SIGNUP_PASSWORD_MISMATCH");
    }

    @Test(groups = {"authentication", "smoke", "regression"}, description = "Valid signup should show success dialog")
    public void testSignupSuccessfully() {
        SignupPage signupPage = openSignupPage();
        logger.info("Creating random user account");
        BasePage resultPage = signupPage.signupWithRandomUser();
        Assert.assertTrue(resultPage instanceof SignupPage, "Signup should remain on SignupPage");

        String dialogMessage = signupPage.getAuthDialogMessage();
        logger.info("Signup dialog captured: {}", dialogMessage);
        Assert.assertNotNull(dialogMessage, "Signup success dialog should appear");
        Assert.assertTrue(dialogMessage.contains("Đăng ký thành công"), "Expected signup success message");
        ScreenshotUtils.takeScreenshot("SIGNUP_SUCCESS_DIALOG");
        logger.info("Authentication signup flow completed successfully");
    }


    // PASS / FAIL LOGGER
    @AfterMethod(alwaysRun = true)
    public void logAuthenticationResult(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();
        long duration = result.getEndMillis() - result.getStartMillis();

        if (result.getStatus() == ITestResult.SUCCESS) {
            logger.info("");
            logger.info("AUTH TEST PASSED");
            logger.info("Test Case : {}", testName);
            logger.info("Description : {}", description);
            logger.info("Reason : {}", getPassReason(testName));
            logger.info("Duration : {} ms", duration);
            logger.info("");
            String screenshotPath = ScreenshotUtils.takeScreenshot("PASS_" + testName);

            logger.info("PASS screenshot saved: {}", screenshotPath);
        } else if (result.getStatus() == ITestResult.FAILURE) {
            logger.error("");
            logger.error("AUTH TEST FAILED");
            logger.error("Test Case : {}", testName);
            logger.error("Description : {}", description);
            logger.error("Error : {}", result.getThrowable() != null
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
                return "System correctly blocked unknown username";
            case "testLoginShowsDialogForWrongPassword":
                return "System correctly blocked wrong password";
            case "testLoginWithEmptyCredentials":
                return "System correctly validated empty credentials";
            case "testLoginWithEmptyUsername":
                return "System correctly validated empty username";
            case "testLoginWithEmptyPassword":
                return "System correctly validated empty password";
            case "testLoginSuccessfullyNavigateToHomeAndTake":
                return "System successfully authenticated user and navigated to Home and Take screens";
            case "testSignupButtonDisabledWhenRequiredFieldsMissing":
                return "System correctly prevented incomplete signup";
            case "testSignupShowsDialogWhenPasswordsMismatch":
                return "System correctly validated mismatched passwords";
            case "testSignupSuccessfully":
                return "System successfully created account and displayed signup success dialog";
            default:
                return "All assertions passed";
        }
    }

    // HELPERS
    private LoginPage openLoginPage() {
        logger.info("Opening Login page");
        LoadingPage loadingPage = new LoadingPage();
        loadingPage.waitForPageToLoad();
        LoginPage loginPage = loadingPage.clickLoginButton();
        loginPage.waitForPageToLoad();
        return loginPage;
    }

    private SignupPage openSignupPage() {
        logger.info("Opening Signup page");
        LoadingPage loadingPage = new LoadingPage();
        loadingPage.waitForPageToLoad();
        SignupPage signupPage = loadingPage.clickSignupButton();
        signupPage.waitForPageToLoad();
        return signupPage;
    }
}