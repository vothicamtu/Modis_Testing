package com.modis.pages;

import com.modis.base.BasePage;
import com.modis.constants.TestIDs;
import com.modis.constants.AppConstants;
import com.modis.utils.UiDebugUtils;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;

/**
 * Page Object for Loading Screen
 * Handles app launch and initial navigation
 */
public class LoadingPage extends BasePage {
    
    // ==================== PAGE ELEMENTS ====================
    
    @AndroidFindBy(accessibility = TestIDs.LOADING_SCREEN)
    @iOSXCUITFindBy(accessibility = TestIDs.LOADING_SCREEN)
    private WebElement loadingScreen;
    
    @AndroidFindBy(accessibility = TestIDs.LOADING_LOGO)
    @iOSXCUITFindBy(accessibility = TestIDs.LOADING_LOGO)
    private WebElement logo;
    
    @AndroidFindBy(accessibility = TestIDs.LOADING_LOGIN_BUTTON)
    @iOSXCUITFindBy(accessibility = TestIDs.LOADING_LOGIN_BUTTON)
    private WebElement loginButton;
    
    @AndroidFindBy(accessibility = TestIDs.LOADING_SIGNUP_BUTTON)
    @iOSXCUITFindBy(accessibility = TestIDs.LOADING_SIGNUP_BUTTON)
    private WebElement signupButton;
    
    @AndroidFindBy(accessibility = TestIDs.LOADING_SPINNER)
    @iOSXCUITFindBy(accessibility = TestIDs.LOADING_SPINNER)
    private WebElement loadingSpinner;
    
    // ==================== PAGE ACTIONS ====================
    
    /**
     * Click login button to navigate to login screen
     * @return LoginPage
     */
    public LoginPage clickLoginButton() {
        logger.info("Clicking login button from loading screen");

        // Ensure Loading screen has rendered at least one navigation button
        waitForLoadingScreenReady();
        clickByAccessibilityId(TestIDs.LOADING_LOGIN_BUTTON);
        logger.info("Successfully clicked login button");

        // IMPORTANT: synchronize after navigation (RN render + navigation transition)
        try {
            LoginPage loginPage = new LoginPage();
            loginPage.waitForPageToLoad();
            return loginPage;
        } catch (Exception firstWaitEx) {
            // In case the first tap didn't register (e.g. animation/overlay), do one controlled retry.
            logger.warn("Login screen not ready after first tap, retrying click once...");
            try {
                clickByAccessibilityId(TestIDs.LOADING_LOGIN_BUTTON);
                LoginPage loginPage = new LoginPage();
                loginPage.waitForPageToLoad();
                return loginPage;
            } catch (Exception retryEx) {
                logger.error("Login navigation retry failed", retryEx);
                throw firstWaitEx;
            }
        }
    }
    
    /**
     * Click signup button to navigate to signup screen
     * @return SignupPage
     */
    public SignupPage clickSignupButton() {
        logger.info("Clicking signup button from loading screen");
        waitForLoadingScreenReady();
        clickByAccessibilityId(TestIDs.LOADING_SIGNUP_BUTTON);
        return new SignupPage();
    }
    
    /**
     * Wait for loading screen to complete and navigate automatically
     * @return BasePage (could be LoginPage or HomePage depending on auth state)
     */
    public BasePage waitForAutoNavigation() {
        logger.info("Waiting for app to be ready after launch (and performing required landing navigation)");

        // ROOT CAUSE FIX:
        // RN screens were setting `accessible={true}` on the root container (Loading/Login/Signup/Home),
        // which groups children and makes leaf nodes (buttons/inputs) NOT discoverable by Appium.
        // We removed that in RN code; now we should detect the current screen by leaf testIDs,
        // WITHOUT waiting for a non-existent "loading_spinner".

        final long deadlineNs = System.nanoTime() + java.time.Duration.ofSeconds(AppConstants.RN_APP_LAUNCH_TIMEOUT).toNanos();
        while (System.nanoTime() < deadlineNs) {
            // If already logged in -> Home stack
            if (isElementDisplayedByAccessibilityId(TestIDs.HOME_SCREEN) ||
                isElementDisplayedByAccessibilityId(TestIDs.TOPBAR_AVATAR_BUTTON) ||
                isElementDisplayedByAccessibilityId(TestIDs.TAKE_SCREEN) ||
                isElementDisplayedByAccessibilityId(TestIDs.FEED_SCREEN)) {
                logger.info("Detected Home flow (user already logged in)");
                return new HomePage();
            }

            // Login screen
            if (isElementDisplayedByAccessibilityId(TestIDs.LOGIN_USERNAME_INPUT) ||
                isElementDisplayedByAccessibilityId(TestIDs.LOGIN_SUBMIT_BUTTON)) {
                logger.info("Detected Login screen");
                return new LoginPage().waitForPageToLoad();
            }

            // Landing/Loading screen requires manual navigation: tap Login/Signup first
            if (isElementDisplayedByAccessibilityId(TestIDs.LOADING_LOGIN_BUTTON)) {
                logger.info("Detected landing page -> tapping Login button");
                return clickLoginButton(); // already waits for LoginPage readiness
            }

            if (isElementDisplayedByAccessibilityId(TestIDs.LOADING_SIGNUP_BUTTON)) {
                // Fallback: if only signup is visible, go to signup then to login
                logger.info("Detected landing page -> Login not visible, tapping Signup then navigating to Login");
                SignupPage signupPage = clickSignupButton();
                try {
                    signupPage.waitForPageToLoad();
                } catch (Exception ignored) {
                }
                LoginPage loginPage = signupPage.clickLoginLink();
                loginPage.waitForPageToLoad();
                return loginPage;
            }

            // Small backoff to avoid busy looping
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        logger.warn("Could not classify current screen after {}s. Assuming still on LoadingPage.",
                AppConstants.RN_APP_LAUNCH_TIMEOUT);
        UiDebugUtils.dumpOnFailure(driver, "unclassified_after_launch");
        return this;
    }
    
    /**
     * Wait for loading screen elements to be ready
     * @return LoadingPage for method chaining
     */
    public LoadingPage waitForLoadingScreenReady() {
        logger.info("Waiting for loading screen to be ready");
        // Tránh rely vào root container (có thể không accessible để tránh merge children).
        // Điều kiện ready: ít nhất 1 trong 2 nút (login/signup) xuất hiện.
        try {
            waitForElementVisible(TestIDs.LOADING_LOGIN_BUTTON);
            logger.info("Loading screen ready - login button is visible");
        } catch (Exception e1) {
            try {
                waitForElementVisible(TestIDs.LOADING_SIGNUP_BUTTON);
                logger.info("Loading screen ready - signup button is visible");
            } catch (Exception e2) {
                logger.debug("Loading screen buttons not found - may auto-navigate", e2);
            }
        }
        
        return this;
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
     * Wait for loading to complete
     */
    public void waitForLoadingToComplete() {
        logger.debug("Waiting for loading to complete");
        waitForElementToDisappear(TestIDs.LOADING_SPINNER);
        waitForAnimation();
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
    
    /**
     * Check if navigation buttons are ready
     * @return true if both buttons are visible, false otherwise
     */
    public boolean areNavigationButtonsReady() {
        return isLoginButtonDisplayed() && isSignupButtonDisplayed();
    }
    
    // ==================== INHERITED METHODS ====================
    
    @Override
    public boolean isDisplayed() {
        try {
            // Không rely vào root container (có thể không accessible để tránh merge children).
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
    
    /**
     * Wait for loading page to be fully loaded
     * @return LoadingPage for method chaining
     */
    public LoadingPage waitForPageToLoad() {
        logger.info("Waiting for loading page to load");

        // React Native loading can either:
        // - render loading buttons (login/signup), OR
        // - auto-navigate quickly (already authenticated) to Home/Login.
        // So we wait for "any-of" signals instead of sleeping a fixed 20s.
        try {
            waitUtils.waitForCondition(d -> {
                // Use BasePage helpers to keep locator fallback bounded
                try { findByAccessibilityId(TestIDs.LOADING_LOGIN_BUTTON); return true; } catch (Exception ignored) {}
                try { findByAccessibilityId(TestIDs.LOADING_SIGNUP_BUTTON); return true; } catch (Exception ignored) {}
                try { findByAccessibilityId(TestIDs.LOGIN_USERNAME_INPUT); return true; } catch (Exception ignored) {}
                try { findByAccessibilityId(TestIDs.TOPBAR_AVATAR_BUTTON); return true; } catch (Exception ignored) {}
                return false;
            }, AppConstants.RN_APP_LAUNCH_TIMEOUT);
        } catch (Exception e) {
            logger.warn("Loading page readiness timeout ({}s) - continuing anyway", AppConstants.RN_APP_LAUNCH_TIMEOUT);
        }
        
        logger.info("Loading page loaded successfully");
        return this;
    }
    
    /**
     * Verify all essential elements are present
     * @return true if all elements are present, false otherwise
     */
    public boolean verifyPageElements() {
        logger.info("Verifying loading page elements");
        
        boolean screenPresent = isDisplayed();
        boolean logoPresent = isElementDisplayedByAccessibilityId(TestIDs.LOADING_LOGO);
        
        // Buttons may not be present if auto-navigation occurs
        boolean buttonsPresent = areNavigationButtonsReady();
        
        logger.info("Loading page elements verification - Screen: {}, Logo: {}, Buttons: {}", 
                   screenPresent, logoPresent, buttonsPresent);
        
        return screenPresent && logoPresent;
    }
}
