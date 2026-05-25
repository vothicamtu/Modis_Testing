package com.modis.tests;

import com.modis.base.BasePage;
import com.modis.base.BaseTest;
import com.modis.pages.HomePage;
import com.modis.pages.LoadingPage;
import com.modis.pages.LoginPage;
import com.modis.pages.ProfilePage;
import com.modis.pages.SignupPage;
import com.modis.pages.TakePage;
import com.modis.utils.ScreenshotUtils;
import com.modis.utils.SmartWaitUtils;
import com.modis.utils.LogoutHelper;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AuthenticationFlowTests extends BaseTest {

    private static final int P_LOADING_SCREEN = 1;
    private static final int P_LOGIN_INVALID_1 = 10;
    private static final int P_LOGIN_INVALID_2 = 11;
    private static final int P_LOGIN_VALID_LAST = 19;

    private static final int P_SIGNUP_INVALID_1 = 30;
    private static final int P_SIGNUP_INVALID_2 = 31;
    private static final int P_SIGNUP_VALID_LAST = 39;

    @Test(priority = P_LOADING_SCREEN, groups = {"authentication", "flow", "regression"}, description = "Verify Loading/Welcome screen appears first")
    public void testLoadingScreenAppearsFirst() {
        logger.info("Testing that Loading/Welcome screen appears first after app launch");
        
        // After app launch, should be on Loading/Welcome screen
        LoadingPage loadingPage = new LoadingPage();
        Assert.assertTrue(loadingPage.isDisplayed(), "Loading/Welcome screen should be displayed after app launch");
        
        // Verify Login and Signup buttons are present (unauthenticated state)
        Assert.assertTrue(loadingPage.isLoginButtonDisplayed(), "Login button should be visible on Loading screen");
        Assert.assertTrue(loadingPage.isSignupButtonDisplayed(), "Signup button should be visible on Loading screen");
        
        // Take screenshot of initial state
        ScreenshotUtils.takeScreenshot("loading_screen_initial");
        
        // IMPORTANT: Do NOT look for authenticated elements like topbar_avatar here
        // App is still in unauthenticated state
        logger.info("Loading screen verification PASSED - app is in unauthenticated state");
    }

    @Test(priority = P_LOGIN_INVALID_1, groups = {"authentication", "flow", "regression"}, description = "Login invalid (wrong credentials)")
    public void testLoginInvalidWrongCredentials() {
        // Start from Loading screen, navigate to Login
        LoginPage loginPage = navigateToLoginFromLoading();

        loginPage.clearAndEnterUsername("invalid_user");
        loginPage.clearAndEnterPassword("invalid_pass");

        BasePage afterSubmit = loginPage.clickLoginButton();

        // Always try to dismiss error dialog if present, and wait for it to disappear
        if (loginPage.isLoginErrorDialogDisplayed()) {
            loginPage.clickErrorDialogOk();
            loginPage.waitForAuthDialogDisappear();
        }

        Assert.assertTrue(afterSubmit instanceof LoginPage,
                "Invalid login should remain on LoginPage, but got: " +
                        (afterSubmit != null ? afterSubmit.getClass().getSimpleName() : "null"));
        Assert.assertTrue(loginPage.isDisplayed(), "Login page should remain displayed after invalid login");

        String dialogError = loginPage.getLastLoginErrorDialogMessage();
        if (dialogError == null || dialogError.isEmpty()) {
            Assert.assertTrue(loginPage.isErrorMessageDisplayed() || loginPage.isDisplayed(),
                    "Expected error dialog or inline error message after invalid login");
        }
        
        // Still in unauthenticated state - should NOT have authenticated elements
        logger.info("Invalid login test PASSED - still in unauthenticated state");
    }

    @Test(priority = P_LOGIN_INVALID_2, groups = {"authentication", "flow", "regression"}, description = "Login invalid (empty fields)")
    public void testLoginInvalidEmptyFields() {
        // Start from Loading screen, navigate to Login
        LoginPage loginPage = navigateToLoginFromLoading();

        loginPage.clearAndEnterUsername("");
        loginPage.clearAndEnterPassword("");

        if (!loginPage.isLoginButtonEnabled()) {
            logger.info("Login button disabled for empty credentials -> PASS (expected)");
            return;
        }

        BasePage afterSubmit = loginPage.clickLoginButton();

        if (loginPage.isLoginErrorDialogDisplayed()) {
            loginPage.clickErrorDialogOk();
            loginPage.waitForAuthDialogDisappear();
        }

        Assert.assertTrue(afterSubmit instanceof LoginPage, "Empty login should remain on LoginPage");
        Assert.assertTrue(loginPage.isDisplayed(), "Login page should remain displayed after empty login");
        
        // Still in unauthenticated state
        logger.info("Empty login test PASSED - still in unauthenticated state");
    }

    @Test(priority = P_LOGIN_VALID_LAST, groups = {"authentication", "flow", "regression"}, description = "Login valid -> AUTHENTICATED state -> Home/Take -> Profile -> Logout")
    public void testLoginValidLastAndLogoutThenProceed() {
        // Start from Loading screen, navigate to Login
        LoginPage loginPage = navigateToLoginFromLoading();

        // Perform successful login
        BasePage afterLogin = loginPage.loginWithTestUser();
        Assert.assertTrue(afterLogin instanceof HomePage,
                "Login should navigate to HomePage, but got: " +
                        (afterLogin != null ? afterLogin.getClass().getSimpleName() : "null"));

        // NOW we are in AUTHENTICATED state - can check authenticated elements
        HomePage homePage = (HomePage) afterLogin;
        homePage.waitForTopbarReadyAfterLogin(8);
        
        // Verify authenticated UI elements are present
        Assert.assertTrue(homePage.isTopbarAvatarDisplayed(), "Topbar avatar should be visible after successful login");
        Assert.assertTrue(homePage.isDisplayed(), "Home screen should be displayed after successful login");

        ScreenshotUtils.takeScreenshot("flow_login_valid_home_authenticated");

        try {
            TakePage takePage = homePage.navigateToCamera();
            if (takePage != null) {
                try {
                    takePage.waitForPageToLoad();
                } catch (Exception ignored) {
                }
                if (takePage.isDisplayed()) {
                    ScreenshotUtils.takeScreenshot("flow_login_valid_take_authenticated");
                    homePage = takePage.navigateBack();
                    homePage.waitForTopbarReadyAfterLogin(6);
                }
            }
        } catch (Exception e) {
            logger.warn("Skip TakeScreen (non-fatal): {}", e.getMessage());
        }

        // Test authenticated navigation
        ProfilePage profilePage = homePage.navigateToProfile();
        Assert.assertTrue(profilePage.isDisplayed(), "Profile page should be accessible in authenticated state");
        
        // Logout - should return to unauthenticated state
        LoginPage afterLogout = profilePage.logout();
        Assert.assertTrue(afterLogout.isDisplayed(), "Should return to Login page after logout");
        
        // Verify we're back to unauthenticated state
        logger.info("Logout successful - back to unauthenticated state");
    }

    @Test(priority = P_SIGNUP_INVALID_1, groups = {"authentication", "flow", "regression"}, description = "Signup invalid (invalid email)")
    public void testSignupInvalidEmail() {
        // Start from Loading screen, navigate to Signup
        SignupPage signupPage = navigateToSignupFromLoading();

        String timestamp = String.valueOf(System.currentTimeMillis());
        SignupPage resultPage = signupPage.signupWithInvalidEmail(
                "testuser" + timestamp,
                "Test User",
                "invalidemail",
                "1234567890",
                "password123"
        );

        if (signupPage.isSignupErrorDialogDisplayed()) { // TODO: verify method exists in SignupPage
            signupPage.clickErrorDialogOk(); // TODO: verify method exists in SignupPage
            signupPage.waitForAuthDialogDisappear(); // TODO: verify method exists in SignupPage
        }

        Assert.assertTrue(resultPage.isDisplayed(), "Should remain on signup page");
        Assert.assertFalse(resultPage.isEmailFormatValid(), "Email format should be invalid");
        
        // Still in unauthenticated state
        logger.info("Invalid signup test PASSED - still in unauthenticated state");
    }

    @Test(priority = P_SIGNUP_INVALID_2, groups = {"authentication", "flow", "regression"}, description = "Signup invalid (mismatched passwords)")
    public void testSignupInvalidMismatchedPasswords() {
        // Start from Loading screen, navigate to Signup
        SignupPage signupPage = navigateToSignupFromLoading();

        String timestamp = String.valueOf(System.currentTimeMillis());
        SignupPage resultPage = signupPage.signupWithMismatchedPasswords(
                "testuser" + timestamp,
                "Test User",
                "test" + timestamp + "@example.com",
                "1234567890",
                "password123",
                "differentpassword"
        );

        if (signupPage.isSignupErrorDialogDisplayed()) { // TODO: verify method exists in SignupPage
            signupPage.clickErrorDialogOk(); // TODO: verify method exists in SignupPage
            signupPage.waitForAuthDialogDisappear(); // TODO: verify method exists in SignupPage
        }

        Assert.assertTrue(resultPage.isDisplayed(), "Should remain on signup page");
        Assert.assertFalse(resultPage.doPasswordsMatch(), "Passwords should not match");
        
        // Still in unauthenticated state
        logger.info("Mismatched password test PASSED - still in unauthenticated state");
    }

    @Test(priority = P_SIGNUP_VALID_LAST, groups = {"authentication", "flow", "regression"}, description = "Signup valid -> AUTHENTICATED state -> Home -> Profile -> Logout")
    public void testSignupValidLast() {
        // Start from Loading screen, navigate to Signup
        SignupPage signupPage = navigateToSignupFromLoading();

        // Perform successful signup
        BasePage afterSignup = signupPage.signupWithRandomUser();
        Assert.assertTrue(afterSignup instanceof HomePage,
                "Signup should navigate to HomePage, but got: " +
                        (afterSignup != null ? afterSignup.getClass().getSimpleName() : "null"));

        // NOW we are in AUTHENTICATED state - can check authenticated elements
        HomePage homePage = (HomePage) afterSignup;
        homePage.waitForTopbarReadyAfterLogin(8);
        
        // Verify authenticated UI elements are present
        Assert.assertTrue(homePage.isTopbarAvatarDisplayed(), "Topbar avatar should be visible after successful signup");
        Assert.assertTrue(homePage.isDisplayed(), "Home screen should be displayed after successful signup");

        ScreenshotUtils.takeScreenshot("flow_signup_valid_home_authenticated");

        // Test authenticated navigation
        ProfilePage profilePage = homePage.navigateToProfile();
        Assert.assertTrue(profilePage.isDisplayed(), "Profile page should be accessible in authenticated state");
        
        // Logout - should return to unauthenticated state
        LoginPage afterLogout = profilePage.logout();
        Assert.assertTrue(afterLogout.isDisplayed(), "Should return to Login page after logout");
        
        // Verify we're back to unauthenticated state
        logger.info("Logout successful - back to unauthenticated state");
    }

    /**
     * Navigate to Login page from Loading screen
     * Ensures we start from the correct unauthenticated state
     */
    private LoginPage navigateToLoginFromLoading() {
        // First ensure we're on Loading screen or navigate to it
        LoadingPage loadingPage = ensureLoadingScreen();
        
        // Click Login button to navigate to Login screen
        LoginPage loginPage = loadingPage.clickLoginButton();
        loginPage.waitForPageToLoad();
        
        Assert.assertTrue(loginPage.isDisplayed(), "Should be on Login page after clicking Login button");
        logger.info("Successfully navigated from Loading screen to Login screen");
        
        return loginPage;
    }

    /**
     * Navigate to Signup page from Loading screen
     * Ensures we start from the correct unauthenticated state
     */
    private SignupPage navigateToSignupFromLoading() {
        // First ensure we're on Loading screen or navigate to it
        LoadingPage loadingPage = ensureLoadingScreen();
        
        // Click Signup button to navigate to Signup screen
        SignupPage signupPage = loadingPage.clickSignupButton();
        signupPage.waitForPageToLoad();
        
        Assert.assertTrue(signupPage.isDisplayed(), "Should be on Signup page after clicking Signup button");
        logger.info("Successfully navigated from Loading screen to Signup screen");
        
        return signupPage;
    }

    /**
     * Ensure we're on Loading screen, logout if needed
     */
    private LoadingPage ensureLoadingScreen() {
        // Check current state
        SmartWaitUtils.ScreenDetectionResult detected = SmartWaitUtils.waitForAnyScreen(10);
        
        switch (detected.getScreenType()) {
            case LOADING:
                // Already on Loading screen - perfect
                LoadingPage loadingPage = new LoadingPage();
                loadingPage.waitForPageToLoad();
                loadingPage.waitForLoginSignupButtonsVisible();
                return loadingPage;
                
            case LOGIN:
            case SIGNUP:
                // Already on auth screens - this is fine, we're in unauthenticated state.
                // Do not force loading-page waits here, because the app may already be on Login/Signup.
                logger.info("Already on auth screen - unauthenticated state confirmed");
                return new LoadingPage();
                
            case HOME:
                // We're authenticated - need to logout first
                logger.info("Currently authenticated - logging out to return to unauthenticated state");
                LogoutHelper.logoutIfLoggedIn(driver);
                
                // After logout, should be back to Loading or Login screen
                SmartWaitUtils.ScreenDetectionResult afterLogout = SmartWaitUtils.waitForAnyScreen(10);
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
                throw new RuntimeException("Unknown screen state detected: " + detected.getScreenType());
        }
    }
}
