package com.modis.pages;

import com.modis.base.BasePage;
import com.modis.constants.TestIDs;
import com.modis.drivers.DriverManager;
import com.modis.utils.SmartWaitUtils;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.WebElement;

/**
 * Loading Page - Handles app launch and initial navigation
 * Optimized for React Native apps with UiAutomator2 stability
 */
public class LoadingPage extends BasePage {

    /**
     * Wait for loading page to be ready with quick timeout
     * @return LoadingPage for method chaining
     */
    public LoadingPage waitForPageToLoad() {
        logger.info("Checking loading page readiness");

        waitForLoadingScreenVisible();
        waitForLoginSignupButtonsVisible();
        return this;
    }

    /**
     * SIMPLIFIED: Wait for app auto-navigation với timeout ngắn
     */
    public BasePage waitForAutoNavigation() {
        logger.info("Waiting for app initial LoadingScreen -> Login/Signup buttons");
        waitForLoadingScreenVisible();
        waitForLoginSignupButtonsVisible();
        return this;
    }

    /**
     * SIMPLIFIED: Handle loading screen navigation - chỉ thử 1 lần
     */
    private BasePage handleLoadingScreenNavigation() {
        try {
            // Chỉ thử click login button 1 lần, không retry
            if (SmartWaitUtils.isElementPresent(AppiumBy.accessibilityId(TestIDs.LOADING_LOGIN_BUTTON))) {
                logger.info("Clicking loading login button");
                clickByAccessibilityId(TestIDs.LOADING_LOGIN_BUTTON);
                LoginPage loginPage = new LoginPage();
                loginPage.waitForPageToLoad();
                return loginPage;
            }
            
            logger.warn("No loading login button found - assuming LoginPage");
            return new LoginPage();
            
        } catch (Exception e) {
            logger.error("Loading screen navigation failed: {}", e.getMessage());
            return new LoginPage();
        }
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

    /**
     * Click login button on loading screen
     * @return LoginPage
     * @deprecated Use waitForAutoNavigation() instead for better reliability
     */
    @Deprecated
    public LoginPage clickLoginButton() {
        logger.info("Clicking login button on loading page");
        
        try {
            WebElement button = DriverManager.safelyFindElement(AppiumBy.accessibilityId(TestIDs.LOADING_LOGIN_BUTTON));
            if (button == null) {
                button = DriverManager.safelyFindElement(AppiumBy.id(TestIDs.LOADING_LOGIN_BUTTON));
            }
            if (button == null) {
                throw new RuntimeException("Loading login button not found: " + TestIDs.LOADING_LOGIN_BUTTON);
            }
            button.click();
            LoginPage loginPage = new LoginPage();
            loginPage.waitForPageToLoad();
            return loginPage;
        } catch (Exception e) {
            logger.error("Failed to click login button", e);
            throw new RuntimeException("Could not navigate to login page from loading screen", e);
        }
    }

    /**
     * Click signup button on loading screen
     * @return SignupPage
     * @deprecated Use waitForAutoNavigation() instead for better reliability
     */
    @Deprecated
    public SignupPage clickSignupButton() {
        logger.info("Clicking signup button on loading page");
        
        try {
            WebElement button = DriverManager.safelyFindElement(AppiumBy.accessibilityId(TestIDs.LOADING_SIGNUP_BUTTON));
            if (button == null) {
                button = DriverManager.safelyFindElement(AppiumBy.id(TestIDs.LOADING_SIGNUP_BUTTON));
            }
            if (button == null) {
                throw new RuntimeException("Loading signup button not found: " + TestIDs.LOADING_SIGNUP_BUTTON);
            }
            button.click();
            SignupPage signupPage = new SignupPage();
            signupPage.waitForPageToLoad();
            return signupPage;
        } catch (Exception e) {
            logger.error("Failed to click signup button", e);
            throw new RuntimeException("Could not navigate to signup page from loading screen", e);
        }
    }

    // ==================== VALIDATION METHODS ====================
    
    /**
     * Check if loading spinner is displayed
     * @return true if loading, false otherwise
     */
    public boolean isLoading() {
        return isElementDisplayedByAccessibilityId(TestIDs.LOADING_SPINNER);
    }
    
    /**
     * Check if logo is displayed
     * @return true if logo is visible, false otherwise
     */
    public boolean isLogoDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.LOADING_LOGO);
    }
    
    /**
     * Check if login button is displayed
     * @return true if login button is visible, false otherwise
     */
    public boolean isLoginButtonDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.LOADING_LOGIN_BUTTON);
    }
    
    /**
     * Check if signup button is displayed
     * @return true if signup button is visible, false otherwise
     */
    public boolean isSignupButtonDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.LOADING_SIGNUP_BUTTON);
    }
    
    // ==================== INHERITED METHODS ====================
    
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
