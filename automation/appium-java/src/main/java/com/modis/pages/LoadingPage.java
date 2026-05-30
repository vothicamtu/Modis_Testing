package com.modis.pages;

import com.modis.base.BasePage;
import com.modis.constants.TestIDs;
import com.modis.drivers.DriverManager;
import com.modis.utils.SmartWaitUtils;
import io.appium.java_client.AppiumBy;

public class LoadingPage extends BasePage {

    public LoadingPage waitForPageToLoad() {
        logger.info("Checking loading page readiness");

        if (isLoginScreenVisible() || isSignupScreenVisible()) {
            logger.info("Already on auth screen; skipping loading wait");
            return this;
        }

        try {
            waitForLoadingScreenVisible();
            waitForLoginSignupButtonsVisible();
            return this;
        } catch (RuntimeException firstFailure) {
            logger.warn("Loading page wait failed on first attempt; checking whether the session needs recovery", firstFailure);
            if (DriverManager.recoverFromUiAutomator2Crash()) {
                waitForLoadingScreenVisible();
                waitForLoginSignupButtonsVisible();
                logger.info("Loading page is ready after recovery");
                return this;
            }
            throw firstFailure;
        }
    }

    public BasePage waitForAutoNavigation() {
        logger.info("Waiting for app initial LoadingScreen -> Login/Signup buttons");
        waitForLoadingScreenVisible();
        waitForLoginSignupButtonsVisible();
        return this;
    }

    public LoadingPage waitForLoadingScreenVisible() {
        logger.info("Waiting for LoadingScreen visible");
        waitForEitherVisible(TestIDs.LOADING_LOGIN_BUTTON, TestIDs.LOADING_SIGNUP_BUTTON, 15);
        return this;
    }

    public LoadingPage waitForLoginSignupButtonsVisible() {
        logger.info("Waiting for Login/Signup buttons visible on LoadingScreen");
        waitForVisible(TestIDs.LOADING_LOGIN_BUTTON, 10);
        waitForVisible(TestIDs.LOADING_SIGNUP_BUTTON, 10);
        return this;
    }

    private void waitForEitherVisible(String idA, String idB, int timeoutSeconds) {
        try {
            waitUtils.waitForCondition(d -> isPresentByAccessibilityOrId(idA) || isPresentByAccessibilityOrId(idB), timeoutSeconds);
        } catch (Exception e) {
            throw new RuntimeException("Neither element visible: " + idA + " | " + idB);
        }
    }

    private void waitForVisible(String accessibilityId, int timeoutSeconds) {
        try {
            waitUtils.waitForCondition(d -> isPresentByAccessibilityOrId(accessibilityId), timeoutSeconds);
        } catch (Exception e) {
            throw new RuntimeException("Element not visible: " + accessibilityId);
        }
    }

    private boolean isPresentByAccessibilityOrId(String id) {
        if (SmartWaitUtils.isElementPresent(AppiumBy.accessibilityId(id))) return true;
        if (SmartWaitUtils.isElementPresent(AppiumBy.id(id))) return true;
        if (DriverManager.getCurrentPlatform().equalsIgnoreCase("android")) {
            try {
                String uiSelector = String.format("new UiSelector().resourceIdMatches(\".*:id/%s\")", id);
                return SmartWaitUtils.isElementPresent(AppiumBy.androidUIAutomator(uiSelector));
            } catch (Exception ignored) {
                return false;
            }
        }
        return false;
    }

    private boolean isLoadingScreenVisible() {
        return isPresentByAccessibilityOrId(TestIDs.LOADING_LOGIN_BUTTON)
                || isPresentByAccessibilityOrId(TestIDs.LOADING_SIGNUP_BUTTON);
    }

    private boolean isLoginScreenVisible() {
        return isPresentByAccessibilityOrId(TestIDs.LOGIN_USERNAME_INPUT)
                || isPresentByAccessibilityOrId(TestIDs.LOGIN_SUBMIT_BUTTON);
    }

    private boolean isSignupScreenVisible() {
        return isPresentByAccessibilityOrId(TestIDs.SIGNUP_USERNAME_INPUT)
                || isPresentByAccessibilityOrId(TestIDs.SIGNUP_SUBMIT_BUTTON);
    }

    @Deprecated
    public LoginPage clickLoginButton() {
        logger.info("Clicking login button on loading page");

        if (isLoginScreenVisible()) {
            logger.info("Already on login screen, skipping loading button click");
            LoginPage loginPage = new LoginPage();
            loginPage.waitForPageToLoad();
            return loginPage;
        }

        try {
            if (!isLoadingScreenVisible()) {
                logger.warn("Loading login button is not visible; falling back to login screen detection");
                LoginPage loginPage = new LoginPage();
                loginPage.waitForPageToLoad();
                return loginPage;
            }

            clickByAccessibilityId(TestIDs.LOADING_LOGIN_BUTTON);

            SmartWaitUtils.ScreenDetectionResult screen = SmartWaitUtils.waitForAnyScreen(10);
            if (screen.getScreenType() == SmartWaitUtils.ScreenType.LOGIN) {
                LoginPage loginPage = new LoginPage();
                loginPage.waitForPageToLoad();
                return loginPage;
            }

            if (screen.getScreenType() == SmartWaitUtils.ScreenType.LOADING) {
                logger.warn("Login button tap did not advance to LOGIN screen; retrying once");
                clickByAccessibilityId(TestIDs.LOADING_LOGIN_BUTTON);
                screen = SmartWaitUtils.waitForAnyScreen(10);
            }

            if (screen.getScreenType() == SmartWaitUtils.ScreenType.LOGIN) {
                LoginPage loginPage = new LoginPage();
                loginPage.waitForPageToLoad();
                return loginPage;
            }

            logger.warn("Login button tap did not reach LOGIN screen, current screen: {}", screen.getScreenType());
            LoginPage loginPage = new LoginPage();
            loginPage.waitForPageToLoad();
            return loginPage;
        } catch (Exception e) {
            logger.error("Failed to click login button", e);
            throw new RuntimeException("Could not navigate to login page from loading screen", e);
        }
    }

    @Deprecated
    public SignupPage clickSignupButton() {
        logger.info("Clicking signup button on loading page");

        try {
            clickByAccessibilityId(TestIDs.LOADING_SIGNUP_BUTTON);

            SmartWaitUtils.ScreenDetectionResult screen = SmartWaitUtils.waitForAnyScreen(10);
            if (screen.getScreenType() == SmartWaitUtils.ScreenType.SIGNUP) {
                SignupPage signupPage = new SignupPage();
                signupPage.waitForPageToLoad();
                return signupPage;
            }

            if (screen.getScreenType() == SmartWaitUtils.ScreenType.LOADING) {
                logger.warn("Signup button tap did not advance to SIGNUP screen; retrying once");
                clickByAccessibilityId(TestIDs.LOADING_SIGNUP_BUTTON);
                screen = SmartWaitUtils.waitForAnyScreen(10);
            }

            if (screen.getScreenType() == SmartWaitUtils.ScreenType.SIGNUP) {
                SignupPage signupPage = new SignupPage();
                signupPage.waitForPageToLoad();
                return signupPage;
            }

            logger.warn("Signup button tap did not reach SIGNUP screen, current screen: {}", screen.getScreenType());
            SignupPage signupPage = new SignupPage();
            signupPage.waitForPageToLoad();
            return signupPage;
        } catch (Exception e) {
            logger.error("Failed to click signup button", e);
            throw new RuntimeException("Could not navigate to signup page from loading screen", e);
        }
    }

    public boolean isLoading() {
        return isElementDisplayedByAccessibilityId(TestIDs.LOADING_SPINNER);
    }

    public boolean isLogoDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.LOADING_LOGO);
    }

    public boolean isLoginButtonDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.LOADING_LOGIN_BUTTON);
    }

    public boolean isSignupButtonDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.LOADING_SIGNUP_BUTTON);
    }

    @Override
    public boolean isDisplayed() {
        try {
            return isLoginButtonDisplayed() || isSignupButtonDisplayed();
        } catch (Exception e) {
            logger.debug("Loading screen not displayed", e);
            return false;
        }
    }

    @Override
    public String getPageTitle() {
        return "Loading Screen";
    }
}
