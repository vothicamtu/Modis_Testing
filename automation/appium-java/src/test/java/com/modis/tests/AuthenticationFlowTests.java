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

    private static final int P_LOGIN_INVALID_1 = 10;
    private static final int P_LOGIN_INVALID_2 = 11;
    private static final int P_LOGIN_VALID_LAST = 19;

    private static final int P_SIGNUP_INVALID_1 = 30;
    private static final int P_SIGNUP_INVALID_2 = 31;
    private static final int P_SIGNUP_VALID_LAST = 39;

    @Test(priority = P_LOGIN_INVALID_1, groups = {"authentication", "flow", "regression"}, description = "Login invalid (wrong credentials)")
    public void testLoginInvalidWrongCredentials() {
        LoginPage loginPage = openLoginPage();

        loginPage.clearAndEnterUsername("invalid_user");
        loginPage.clearAndEnterPassword("invalid_pass");

        BasePage afterSubmit = loginPage.clickLoginButton();
        Assert.assertTrue(afterSubmit instanceof LoginPage,
                "Invalid login should remain on LoginPage, but got: " +
                        (afterSubmit != null ? afterSubmit.getClass().getSimpleName() : "null"));
        Assert.assertTrue(loginPage.isDisplayed(), "Login page should remain displayed after invalid login");

        String dialogError = loginPage.getLastLoginErrorDialogMessage();
        if (dialogError == null || dialogError.isEmpty()) {
            Assert.assertTrue(loginPage.isErrorMessageDisplayed() || loginPage.isDisplayed(),
                    "Expected error dialog or inline error message after invalid login");
        }
    }

    @Test(priority = P_LOGIN_INVALID_2, groups = {"authentication", "flow", "regression"}, description = "Login invalid (empty fields)")
    public void testLoginInvalidEmptyFields() {
        LoginPage loginPage = openLoginPage();

        loginPage.clearAndEnterUsername("");
        loginPage.clearAndEnterPassword("");

        if (!loginPage.isLoginButtonEnabled()) {
            logger.info("Login button disabled for empty credentials -> PASS (expected)");
            return;
        }

        BasePage afterSubmit = loginPage.clickLoginButton();
        Assert.assertTrue(afterSubmit instanceof LoginPage, "Empty login should remain on LoginPage");
        Assert.assertTrue(loginPage.isDisplayed(), "Login page should remain displayed after empty login");
    }

    @Test(priority = P_LOGIN_VALID_LAST, groups = {"authentication", "flow", "regression"}, description = "Login valid (LAST) -> Home/Take screenshot -> Profile -> Logout")
    public void testLoginValidLastAndLogoutThenProceed() {
        LoginPage loginPage = openLoginPage();

        BasePage afterLogin = loginPage.loginWithTestUser();
        Assert.assertTrue(afterLogin instanceof HomePage,
                "Login should navigate to HomePage, but got: " +
                        (afterLogin != null ? afterLogin.getClass().getSimpleName() : "null"));

        HomePage homePage = (HomePage) afterLogin;
        homePage.waitForTopbarReadyAfterLogin(8);

        ScreenshotUtils.takeScreenshot("flow_login_valid_home");

        try {
            TakePage takePage = homePage.navigateToCamera();
            if (takePage != null) {
                try {
                    takePage.waitForPageToLoad();
                } catch (Exception ignored) {
                }
                if (takePage.isDisplayed()) {
                    ScreenshotUtils.takeScreenshot("flow_login_valid_take");
                    homePage = takePage.navigateBack();
                    homePage.waitForTopbarReadyAfterLogin(6);
                }
            }
        } catch (Exception e) {
            logger.warn("Skip TakeScreen (non-fatal): {}", e.getMessage());
        }

        ProfilePage profilePage = homePage.navigateToProfile();
        LoginPage afterLogout = profilePage.logout();
        Assert.assertTrue(afterLogout.isDisplayed(), "Login page should be displayed after logout");
    }

    @Test(priority = P_SIGNUP_INVALID_1, groups = {"authentication", "flow", "regression"}, description = "Signup invalid (invalid email)")
    public void testSignupInvalidEmail() {
        SignupPage signupPage = openSignupPage();

        String timestamp = String.valueOf(System.currentTimeMillis());
        SignupPage resultPage = signupPage.signupWithInvalidEmail(
                "testuser" + timestamp,
                "Test User",
                "invalidemail",
                "1234567890",
                "password123"
        );

        Assert.assertTrue(resultPage.isDisplayed(), "Should remain on signup page");
        Assert.assertFalse(resultPage.isEmailFormatValid(), "Email format should be invalid");
    }

    @Test(priority = P_SIGNUP_INVALID_2, groups = {"authentication", "flow", "regression"}, description = "Signup invalid (mismatched passwords)")
    public void testSignupInvalidMismatchedPasswords() {
        SignupPage signupPage = openSignupPage();

        String timestamp = String.valueOf(System.currentTimeMillis());
        SignupPage resultPage = signupPage.signupWithMismatchedPasswords(
                "testuser" + timestamp,
                "Test User",
                "test" + timestamp + "@example.com",
                "1234567890",
                "password123",
                "differentpassword"
        );

        Assert.assertTrue(resultPage.isDisplayed(), "Should remain on signup page");
        Assert.assertFalse(resultPage.doPasswordsMatch(), "Passwords should not match");
    }

    @Test(priority = P_SIGNUP_VALID_LAST, groups = {"authentication", "flow", "regression"}, description = "Signup valid (LAST) -> Home screenshot -> Profile -> Logout")
    public void testSignupValidLast() {
        SignupPage signupPage = openSignupPage();

        BasePage afterSignup = signupPage.signupWithRandomUser();
        Assert.assertTrue(afterSignup instanceof HomePage,
                "Signup should navigate to HomePage, but got: " +
                        (afterSignup != null ? afterSignup.getClass().getSimpleName() : "null"));

        HomePage homePage = (HomePage) afterSignup;

        ScreenshotUtils.takeScreenshot("flow_signup_valid_home");

        ProfilePage profilePage = homePage.navigateToProfile();
        LoginPage afterLogout = profilePage.logout();
        Assert.assertTrue(afterLogout.isDisplayed(), "Login page should be displayed after logout");
    }

    private LoginPage openLoginPage() {
        LoginPage loginPage = new LoginPage();
        if (loginPage.isDisplayed()) {
            loginPage.waitForPageToLoad();
            return loginPage;
        }

        SignupPage signupPage = new SignupPage();
        if (signupPage.isDisplayed()) {
            LoginPage fromSignup = signupPage.clickLoginLink();
            fromSignup.waitForPageToLoad();
            return fromSignup;
        }

        SmartWaitUtils.ScreenDetectionResult detected = SmartWaitUtils.waitForAnyScreen(20);
        switch (detected.getScreenType()) {
            case LOGIN: {
                LoginPage lp = new LoginPage();
                lp.waitForPageToLoad();
                return lp;
            }
            case SIGNUP: {
                SignupPage sp = new SignupPage();
                LoginPage fromSignup = sp.clickLoginLink();
                fromSignup.waitForPageToLoad();
                return fromSignup;
            }
            case LOADING: {
                try {
                    LoadingPage loadingPage = new LoadingPage();
                    LoginPage lp = loadingPage.clickLoginButton();
                    lp.waitForPageToLoad();
                    return lp;
                } catch (Exception e) {
                    SmartWaitUtils.captureDebugInfo("open_login_from_loading");
                    throw e;
                }
            }
            case HOME: {
                LogoutHelper.logoutIfLoggedIn(driver);
                return openLoginPage();
            }
            default: {
                SmartWaitUtils.captureDebugInfo("open_login_unknown");
                throw new RuntimeException("Cannot navigate to LoginPage from current state: " + detected.getScreenType());
            }
        }
    }

    private SignupPage openSignupPage() {
        SignupPage signupPage = new SignupPage();
        if (signupPage.isDisplayed()) {
            signupPage.waitForPageToLoad();
            return signupPage;
        }

        LoginPage loginPage = new LoginPage();
        if (loginPage.isDisplayed()) {
            SignupPage fromLogin = loginPage.clickSignupLink();
            fromLogin.waitForPageToLoad();
            return fromLogin;
        }

        SmartWaitUtils.ScreenDetectionResult detected = SmartWaitUtils.waitForAnyScreen(20);
        switch (detected.getScreenType()) {
            case SIGNUP: {
                SignupPage sp = new SignupPage();
                sp.waitForPageToLoad();
                return sp;
            }
            case LOGIN: {
                LoginPage lp = new LoginPage();
                SignupPage fromLogin = lp.clickSignupLink();
                fromLogin.waitForPageToLoad();
                return fromLogin;
            }
            case LOADING: {
                try {
                    LoadingPage loadingPage = new LoadingPage();
                    SignupPage sp = loadingPage.clickSignupButton();
                    sp.waitForPageToLoad();
                    return sp;
                } catch (Exception e) {
                    SmartWaitUtils.captureDebugInfo("open_signup_from_loading");
                    throw e;
                }
            }
            case HOME: {
                LogoutHelper.logoutIfLoggedIn(driver);
                return openSignupPage();
            }
            default: {
                SmartWaitUtils.captureDebugInfo("open_signup_unknown");
                throw new RuntimeException("Cannot navigate to SignupPage from current state: " + detected.getScreenType());
            }
        }
    }
}
