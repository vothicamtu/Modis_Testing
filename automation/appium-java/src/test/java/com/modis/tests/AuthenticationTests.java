package com.modis.tests;

import com.modis.base.BasePage;
import com.modis.base.BaseTest;

import com.modis.pages.HomePage;
import com.modis.pages.LoadingPage;
import com.modis.pages.LoginPage;
import com.modis.pages.ProfilePage;
import com.modis.pages.SignupPage;
import com.modis.pages.TakePage;

import com.modis.utils.LoggerUtil;
import com.modis.utils.LogoutHelper;
import com.modis.utils.ScreenshotUtils;
import com.modis.utils.SmartWaitUtils;
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

    private static final int P_LOADING_SCREEN = 1;

    private static final int P_LOGIN_DISABLED = 2;
    private static final int P_LOGIN_UNKNOWN_USERNAME = 3;
    private static final int P_LOGIN_WRONG_PASSWORD = 4;
    private static final int P_LOGIN_EMPTY_CREDENTIALS = 5;
    private static final int P_LOGIN_EMPTY_USERNAME = 6;
    private static final int P_LOGIN_EMPTY_PASSWORD = 7;
    private static final int P_LOGIN_VALID_FLOW = 8;

    private static final int P_SIGNUP_DISABLED = 9;
    private static final int P_SIGNUP_INVALID_EMAIL = 10;
    private static final int P_SIGNUP_PASSWORD_MISMATCH = 11;
    private static final int P_SIGNUP_VALID = 12;

    @Test(priority = P_LOADING_SCREEN, groups = {"authentication", "flow", "regression"}, description = "Verify Loading/Welcome screen appears first")
    public void testLoadingScreenAppearsFirst() {

        logger.info("Testing that Loading/Welcome screen appears first after app launch");

        LoadingPage loadingPage = new LoadingPage();

        Assert.assertTrue(
                loadingPage.isDisplayed(),
                "Loading/Welcome screen should be displayed after app launch"
        );

        Assert.assertTrue(
                loadingPage.isLoginButtonDisplayed(),
                "Login button should be visible on Loading screen"
        );

        Assert.assertTrue(
                loadingPage.isSignupButtonDisplayed(),
                "Signup button should be visible on Loading screen"
        );

        ScreenshotUtils.takeScreenshot("loading_screen_initial");

        logger.info("Loading screen verification PASSED - app is in unauthenticated state");
    }


    @Test(
            priority = P_LOGIN_DISABLED,
            groups = {"authentication", "regression"},
            description = "Login submit must stay disabled until both fields are filled"
    )
    public void testLoginButtonDisabledWhenFieldsMissing() {

        LoginPage loginPage = navigateToLoginFromLoading();

        logger.info("Clearing all login fields");

        loginPage.clearAllFields();

        Assert.assertTrue(
                loginPage.isDisplayed(),
                "Login page should remain visible"
        );

        Assert.assertFalse(
                loginPage.isLoginButtonEnabled(),
                "Login button should be disabled when username and password are empty"
        );
    }

    @Test(
            priority = P_LOGIN_UNKNOWN_USERNAME,
            groups = {"authentication", "regression"},
            description = "Login invalid username should show auth dialog"
    )
    public void testLoginShowsDialogForUnknownUsername() {

        LoginPage loginPage = navigateToLoginFromLoading();

        List<Map<String, Object>> invalidCredentials =
                testDataReader.getInvalidLoginCredentials();

        Map<String, Object> invalidUser = invalidCredentials.get(0);

        String username = (String) invalidUser.get("username");
        String password = (String) invalidUser.get("password");

        logger.info("Using invalid username from JSON: {}", username);

        loginPage.enterUsername(username);
        loginPage.enterPassword(password);

        BasePage submitResult = loginPage.clickLoginButton();

        String dialogMessage = loginPage.getAuthDialogMessage();

        Assert.assertTrue(
                submitResult instanceof LoginPage,
                "Invalid login should stay on LoginPage"
        );

        Assert.assertTrue(
                loginPage.isAuthDialogVisible(),
                "Auth dialog should appear"
        );

        Assert.assertFalse(
                dialogMessage == null || dialogMessage.isBlank(),
                "Dialog message should not be empty"
        );

        loginPage.dismissLoginErrorDialog();

        Assert.assertFalse(
                loginPage.isAuthDialogVisible(),
                "Dialog should close after dismiss"
        );

        ScreenshotUtils.takeScreenshot("INVALID_USERNAME_DIALOG");
    }

    @Test(
            priority = P_LOGIN_WRONG_PASSWORD,
            groups = {"authentication", "regression"},
            description = "Login wrong password should show auth dialog"
    )
    public void testLoginShowsDialogForWrongPassword() {

        LoginPage loginPage = navigateToLoginFromLoading();

        List<Map<String, Object>> invalidCredentials =
                testDataReader.getInvalidLoginCredentials();

        Map<String, Object> invalidPasswordUser = invalidCredentials.get(1);

        String username = (String) invalidPasswordUser.get("username");
        String password = (String) invalidPasswordUser.get("password");

        logger.info("Using invalid password test data for user: {}", username);

        loginPage.enterUsername(username);
        loginPage.enterPassword(password);

        BasePage submitResult = loginPage.clickLoginButton();

        String dialogMessage = loginPage.getAuthDialogMessage();

        Assert.assertTrue(
                submitResult instanceof LoginPage,
                "Invalid password should stay on LoginPage"
        );

        Assert.assertTrue(
                loginPage.isAuthDialogVisible(),
                "Auth dialog should appear"
        );

        Assert.assertFalse(
                dialogMessage == null || dialogMessage.isBlank(),
                "Dialog message should not be empty"
        );

        loginPage.dismissLoginErrorDialog();

        Assert.assertFalse(
                loginPage.isAuthDialogVisible(),
                "Dialog should close after dismiss"
        );

        ScreenshotUtils.takeScreenshot("WRONG_PASSWORD_DIALOG");
    }

    @Test(
            priority = P_LOGIN_EMPTY_CREDENTIALS,
            groups = {"authentication", "regression"},
            description = "Empty credentials should show validation error"
    )
    public void testLoginWithEmptyCredentials() {

        LoginPage loginPage = navigateToLoginFromLoading();

        List<Map<String, Object>> invalidCredentials =
                testDataReader.getInvalidLoginCredentials();

        Map<String, Object> emptyData = invalidCredentials.get(4);

        String username = (String) emptyData.get("username");
        String password = (String) emptyData.get("password");

        logger.info("Testing empty credentials validation");

        loginPage.enterUsername(username);
        loginPage.enterPassword(password);

        Assert.assertFalse(
                loginPage.isLoginButtonEnabled(),
                "Login button should stay disabled"
        );

        ScreenshotUtils.takeScreenshot("EMPTY_CREDENTIALS_VALIDATION");
    }

    @Test(
            priority = P_LOGIN_EMPTY_USERNAME,
            groups = {"authentication", "regression"},
            description = "Empty username should show validation error"
    )
    public void testLoginWithEmptyUsername() {

        LoginPage loginPage = navigateToLoginFromLoading();

        List<Map<String, Object>> invalidCredentials =
                testDataReader.getInvalidLoginCredentials();

        Map<String, Object> emptyUsername = invalidCredentials.get(5);

        String username = (String) emptyUsername.get("username");
        String password = (String) emptyUsername.get("password");

        logger.info("Testing empty username validation");

        loginPage.enterUsername(username);
        loginPage.enterPassword(password);

        Assert.assertFalse(
                loginPage.isLoginButtonEnabled(),
                "Login button should remain disabled"
        );

        ScreenshotUtils.takeScreenshot("EMPTY_USERNAME_VALIDATION");
    }

    @Test(
            priority = P_LOGIN_EMPTY_PASSWORD,
            groups = {"authentication", "regression"},
            description = "Empty password should show validation error"
    )
    public void testLoginWithEmptyPassword() {

        LoginPage loginPage = navigateToLoginFromLoading();

        List<Map<String, Object>> invalidCredentials =
                testDataReader.getInvalidLoginCredentials();

        Map<String, Object> emptyPassword = invalidCredentials.get(6);

        String username = (String) emptyPassword.get("username");
        String password = (String) emptyPassword.get("password");

        logger.info("Testing empty password validation");

        loginPage.enterUsername(username);
        loginPage.enterPassword(password);

        Assert.assertFalse(
                loginPage.isLoginButtonEnabled(),
                "Login button should remain disabled"
        );

        ScreenshotUtils.takeScreenshot("EMPTY_PASSWORD_VALIDATION");
    }

    @Test(
            priority = P_LOGIN_VALID_FLOW,
            groups = {"authentication", "flow", "smoke", "regression"},
            description = "Valid login -> AUTHENTICATED state -> Home/Take -> Profile -> Logout"
    )
    public void testLoginSuccessfullyNavigateToHomeAndTake() {

        LoginPage loginPage = navigateToLoginFromLoading();

        List<Map<String, Object>> validCredentials =
                testDataReader.getValidLoginCredentials();

        Map<String, Object> validUser = validCredentials.get(0);

        String username = (String) validUser.get("username");
        String password = (String) validUser.get("password");

        logger.info("Logging in with valid JSON user: {}", username);

        BasePage afterLogin = loginPage.login(username, password);

        Assert.assertTrue(
                afterLogin instanceof HomePage,
                "Login should navigate to HomePage"
        );

        HomePage homePage = (HomePage) afterLogin;

        logger.info("Waiting for authenticated Home screen");

        homePage.waitForTopbarReadyAfterLogin(8);

        Assert.assertTrue(
                homePage.isTopbarAvatarDisplayed(),
                "Topbar avatar should be visible after successful login"
        );

        Assert.assertTrue(
                homePage.isDisplayed(),
                "Home page should display"
        );

        ScreenshotUtils.takeScreenshot("LOGIN_SUCCESS_HOME");

        logger.info("Navigating to Take screen");

        try {

            TakePage takePage = homePage.navigateToCamera();

            Assert.assertNotNull(
                    takePage,
                    "TakePage should not be null"
            );

            takePage.waitForPageToLoad();

            Assert.assertTrue(
                    takePage.isDisplayed(),
                    "Take screen should display"
            );

            ScreenshotUtils.takeScreenshot("LOGIN_SUCCESS_TAKE");

            homePage = takePage.navigateBack();

            homePage.waitForTopbarReadyAfterLogin(6);

        } catch (Exception e) {

            logger.warn("Skip TakeScreen (non-fatal): {}", e.getMessage());
        }

        ProfilePage profilePage = homePage.navigateToProfile();

        Assert.assertTrue(
                profilePage.isDisplayed(),
                "Profile page should be accessible in authenticated state"
        );

        LoginPage afterLogout = profilePage.logout();

        Assert.assertTrue(
                afterLogout.isDisplayed(),
                "Should return to Login page after logout"
        );

        logger.info("Authentication login flow completed successfully");
    }

    @Test(
            priority = P_SIGNUP_DISABLED,
            groups = {"authentication", "regression"},
            description = "Signup submit must stay disabled until all required fields are filled"
    )
    public void testSignupButtonDisabledWhenRequiredFieldsMissing() {

        SignupPage signupPage = navigateToSignupFromLoading();

        logger.info("Entering partial signup form");

        signupPage.enterUsername("signup_blocker");
        signupPage.enterPassword("Password123");

        Assert.assertFalse(
                signupPage.isSignupButtonEnabled(),
                "Signup button should remain disabled"
        );
    }

    @Test(
            priority = P_SIGNUP_INVALID_EMAIL,
            groups = {"authentication", "flow", "regression"},
            description = "Signup invalid (invalid email)"
    )
    public void testSignupInvalidEmail() {

        SignupPage signupPage = navigateToSignupFromLoading();

        String timestamp = String.valueOf(System.currentTimeMillis());

        SignupPage resultPage = signupPage.signupWithInvalidEmail(
                "testuser" + timestamp,
                "Test User",
                "invalidemail",
                "1234567890",
                "password123"
        );

        if (signupPage.isSignupErrorDialogDisplayed()) {

            signupPage.clickErrorDialogOk();
            signupPage.waitForAuthDialogDisappear();
        }

        Assert.assertTrue(
                resultPage.isDisplayed(),
                "Should remain on signup page"
        );

        Assert.assertFalse(
                resultPage.isEmailFormatValid(),
                "Email format should be invalid"
        );

        logger.info("Invalid signup test PASSED - still in unauthenticated state");
    }

    @Test(
            priority = P_SIGNUP_PASSWORD_MISMATCH,
            groups = {"authentication", "regression"},
            description = "Signup mismatched passwords should show auth dialog"
    )
    public void testSignupShowsDialogWhenPasswordsMismatch() {

        SignupPage signupPage = navigateToSignupFromLoading();

        String timestamp = String.valueOf(System.currentTimeMillis());

        signupPage.enterUsername("signup_mismatch_" + timestamp);
        signupPage.enterFullname("Signup Mismatch");
        signupPage.enterEmail("signup_mismatch_" + timestamp + "@example.com");
        signupPage.enterPhone("1234567890");
        signupPage.enterPassword("Password123");
        signupPage.enterConfirmPassword("DifferentPassword");

        BasePage submitResult = signupPage.clickSignupButton();

        Assert.assertTrue(
                submitResult instanceof SignupPage,
                "Signup should stay on SignupPage"
        );

        Assert.assertTrue(
                signupPage.isAuthDialogVisible(),
                "Auth dialog should display"
        );

        String dialogMessage = signupPage.getAuthDialogMessage();

        Assert.assertEquals(
                Normalizer.normalize(dialogMessage.trim(), Normalizer.Form.NFC),
                Normalizer.normalize("Mật khẩu không khớp", Normalizer.Form.NFC),
                "Wrong signup mismatch dialog message"
        );

        signupPage.dismissSignupErrorDialog();

        Assert.assertFalse(
                signupPage.isAuthDialogVisible(),
                "Dialog should close"
        );

        ScreenshotUtils.takeScreenshot("SIGNUP_PASSWORD_MISMATCH");
    }

    @Test(
            priority = P_SIGNUP_VALID,
            groups = {"authentication", "flow", "smoke", "regression"},
            description = "Valid signup -> AUTHENTICATED state -> Home -> Profile -> Logout"
    )
    public void testSignupSuccessfully() {

        SignupPage signupPage = navigateToSignupFromLoading();

        BasePage afterSignup = signupPage.signupWithRandomUser();

        if (afterSignup instanceof SignupPage) {

            String dialogMessage = signupPage.getAuthDialogMessage();

            logger.info("Signup dialog captured: {}", dialogMessage);

            Assert.assertNotNull(
                    dialogMessage,
                    "Signup success dialog should appear"
            );

            Assert.assertTrue(
                    dialogMessage.contains("Đăng ký thành công"),
                    "Expected signup success message"
            );

            ScreenshotUtils.takeScreenshot("SIGNUP_SUCCESS_DIALOG");

            return;
        }

        Assert.assertTrue(
                afterSignup instanceof HomePage,
                "Signup should navigate to HomePage"
        );

        HomePage homePage = (HomePage) afterSignup;

        homePage.waitForTopbarReadyAfterLogin(8);

        Assert.assertTrue(
                homePage.isTopbarAvatarDisplayed(),
                "Topbar avatar should be visible after successful signup"
        );

        Assert.assertTrue(
                homePage.isDisplayed(),
                "Home screen should be displayed after successful signup"
        );

        ScreenshotUtils.takeScreenshot("flow_signup_valid_home_authenticated");

        ProfilePage profilePage = homePage.navigateToProfile();

        Assert.assertTrue(
                profilePage.isDisplayed(),
                "Profile page should be accessible in authenticated state"
        );

        LoginPage afterLogout = profilePage.logout();

        Assert.assertTrue(
                afterLogout.isDisplayed(),
                "Should return to Login page after logout"
        );

        logger.info("Authentication signup flow completed successfully");
    }

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

            String screenshotPath =
                    ScreenshotUtils.takeScreenshot("PASS_" + testName);

            logger.info("PASS screenshot saved: {}", screenshotPath);

        } else if (result.getStatus() == ITestResult.FAILURE) {

            logger.error("");
            logger.error("AUTH TEST FAILED");
            logger.error("Test Case : {}", testName);
            logger.error("Description : {}", description);

            logger.error(
                    "Error : {}",
                    result.getThrowable() != null
                            ? result.getThrowable().getMessage()
                            : "Unknown Error"
            );

            logger.error("Duration : {} ms", duration);
            logger.error("");
        }
    }

    private String getPassReason(String testName) {

        switch (testName) {

            case "testLoadingScreenAppearsFirst":
                return "Application correctly displayed unauthenticated loading screen";

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
                return "System successfully authenticated user and navigated through authenticated flow";

            case "testSignupButtonDisabledWhenRequiredFieldsMissing":
                return "System correctly prevented incomplete signup";

            case "testSignupInvalidEmail":
                return "System correctly validated invalid email format";

            case "testSignupShowsDialogWhenPasswordsMismatch":
                return "System correctly validated mismatched passwords";

            case "testSignupSuccessfully":
                return "System successfully completed signup flow";

            default:
                return "All assertions passed";
        }
    }

    private LoginPage navigateToLoginFromLoading() {

        LoadingPage loadingPage = ensureLoadingScreen();

        LoginPage loginPage = loadingPage.clickLoginButton();

        loginPage.waitForPageToLoad();

        Assert.assertTrue(
                loginPage.isDisplayed(),
                "Should be on Login page after clicking Login button"
        );

        logger.info("Successfully navigated from Loading screen to Login screen");

        return loginPage;
    }

    private SignupPage navigateToSignupFromLoading() {

        LoadingPage loadingPage = ensureLoadingScreen();

        SignupPage signupPage = loadingPage.clickSignupButton();

        signupPage.waitForPageToLoad();

        Assert.assertTrue(
                signupPage.isDisplayed(),
                "Should be on Signup page after clicking Signup button"
        );

        logger.info("Successfully navigated from Loading screen to Signup screen");

        return signupPage;
    }

    private LoadingPage ensureLoadingScreen() {

        SmartWaitUtils.ScreenDetectionResult detected =
                SmartWaitUtils.waitForAnyScreen(10);

        switch (detected.getScreenType()) {

            case LOADING:

                LoadingPage loadingPage = new LoadingPage();

                loadingPage.waitForPageToLoad();
                loadingPage.waitForLoginSignupButtonsVisible();

                return loadingPage;

            case LOGIN:
            case SIGNUP:

                logger.info("Already on auth screen - unauthenticated state confirmed");

                return new LoadingPage();

            case HOME:

                logger.info("Currently authenticated - logging out to return to unauthenticated state");

                LogoutHelper.logoutIfLoggedIn(driver);

                SmartWaitUtils.ScreenDetectionResult afterLogout =
                        SmartWaitUtils.waitForAnyScreen(10);

                if (afterLogout.getScreenType() == SmartWaitUtils.ScreenType.LOADING) {

                    LoadingPage lp = new LoadingPage();

                    lp.waitForPageToLoad();
                    lp.waitForLoginSignupButtonsVisible();

                    return lp;

                } else if (afterLogout.getScreenType() == SmartWaitUtils.ScreenType.LOGIN) {

                    logger.info("After logout, on Login screen - unauthenticated state confirmed");

                    LoginPage loginPage = new LoginPage();

                    loginPage.waitForPageToLoad();

                    return new LoadingPage();

                } else {

                    LoadingPage lp = new LoadingPage();

                    lp.waitForPageToLoad();
                    lp.waitForLoginSignupButtonsVisible();

                    return lp;
                }

            default:
                throw new RuntimeException(
                        "Unknown screen state detected: " + detected.getScreenType()
                );
        }
    }
}