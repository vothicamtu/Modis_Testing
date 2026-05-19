package com.modis.pages;

import com.modis.base.BasePage;
import com.modis.constants.TestIDs;
import com.modis.constants.AppConstants;
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
        waitForElementClickable(TestIDs.LOADING_LOGIN_BUTTON);
        clickElement(loginButton);
        return new LoginPage();
    }
    
    /**
     * Click signup button to navigate to signup screen
     * @return SignupPage
     */
    public SignupPage clickSignupButton() {
        logger.info("Clicking signup button from loading screen");
        waitForElementClickable(TestIDs.LOADING_SIGNUP_BUTTON);
        clickElement(signupButton);
        return new SignupPage();
    }
    
    /**
     * Wait for loading screen to complete and navigate automatically
     * @return BasePage (could be LoginPage or HomePage depending on auth state)
     */
    public BasePage waitForAutoNavigation() {
        logger.info("Waiting for automatic navigation from loading screen");
        
        // Wait for loading to complete
        waitForLoadingToComplete();
        
        // Check which screen we navigated to
        if (isElementDisplayedByAccessibilityId(TestIDs.HOME_SCREEN)) {
            logger.info("Auto-navigated to home screen (user already logged in)");
            return new HomePage();
        } else if (isElementDisplayedByAccessibilityId(TestIDs.LOGIN_SCREEN)) {
            logger.info("Auto-navigated to login screen");
            return new LoginPage();
        } else {
            logger.info("Still on loading screen - manual navigation required");
            return this;
        }
    }
    
    /**
     * Wait for loading screen elements to be ready
     * @return LoadingPage for method chaining
     */
    public LoadingPage waitForLoadingScreenReady() {
        logger.info("Waiting for loading screen to be ready");
        waitForElementVisible(TestIDs.LOADING_SCREEN);
        
        // Wait for either buttons to appear or auto-navigation to occur
        try {
            waitForElementVisible(TestIDs.LOADING_LOGIN_BUTTON);
            waitForElementVisible(TestIDs.LOADING_SIGNUP_BUTTON);
            logger.info("Loading screen buttons are ready");
        } catch (Exception e) {
            logger.debug("Loading screen buttons not found - may auto-navigate", e);
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
            return isElementDisplayedByAccessibilityId(TestIDs.LOADING_SCREEN);
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
        waitForElementVisible(TestIDs.LOADING_SCREEN);
        
        // Wait for app launch to complete
        try {
            Thread.sleep(AppConstants.MAX_APP_LAUNCH_TIME_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("App launch wait interrupted", e);
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
        
        boolean screenPresent = isElementDisplayedByAccessibilityId(TestIDs.LOADING_SCREEN);
        boolean logoPresent = isElementDisplayedByAccessibilityId(TestIDs.LOADING_LOGO);
        
        // Buttons may not be present if auto-navigation occurs
        boolean buttonsPresent = areNavigationButtonsReady();
        
        logger.info("Loading page elements verification - Screen: {}, Logo: {}, Buttons: {}", 
                   screenPresent, logoPresent, buttonsPresent);
        
        return screenPresent && logoPresent;
    }
}