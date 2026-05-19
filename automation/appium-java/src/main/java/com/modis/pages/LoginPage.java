package com.modis.pages;

import com.modis.base.BasePage;
import com.modis.constants.TestIDs;
import com.modis.constants.AppConstants;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;

/**
 * Page Object for Login Screen
 * Handles login functionality and related interactions
 */
public class LoginPage extends BasePage {
    
    // ==================== PAGE ELEMENTS ====================
    
    @AndroidFindBy(accessibility = TestIDs.LOGIN_SCREEN)
    @iOSXCUITFindBy(accessibility = TestIDs.LOGIN_SCREEN)
    private WebElement loginScreen;
    
    @AndroidFindBy(accessibility = TestIDs.LOGIN_TITLE_TEXT)
    @iOSXCUITFindBy(accessibility = TestIDs.LOGIN_TITLE_TEXT)
    private WebElement titleText;
    
    @AndroidFindBy(accessibility = TestIDs.LOGIN_USERNAME_INPUT)
    @iOSXCUITFindBy(accessibility = TestIDs.LOGIN_USERNAME_INPUT)
    private WebElement usernameInput;
    
    @AndroidFindBy(accessibility = TestIDs.LOGIN_PASSWORD_INPUT)
    @iOSXCUITFindBy(accessibility = TestIDs.LOGIN_PASSWORD_INPUT)
    private WebElement passwordInput;
    
    @AndroidFindBy(accessibility = TestIDs.LOGIN_SUBMIT_BUTTON)
    @iOSXCUITFindBy(accessibility = TestIDs.LOGIN_SUBMIT_BUTTON)
    private WebElement loginButton;
    
    @AndroidFindBy(accessibility = TestIDs.LOGIN_SIGNUP_LINK)
    @iOSXCUITFindBy(accessibility = TestIDs.LOGIN_SIGNUP_LINK)
    private WebElement signupLink;
    
    @AndroidFindBy(accessibility = TestIDs.LOGIN_FORGOT_PASSWORD)
    @iOSXCUITFindBy(accessibility = TestIDs.LOGIN_FORGOT_PASSWORD)
    private WebElement forgotPasswordLink;
    
    // Error and validation elements
    @AndroidFindBy(accessibility = TestIDs.VALIDATION_ERROR_TEXT)
    @iOSXCUITFindBy(accessibility = TestIDs.VALIDATION_ERROR_TEXT)
    private WebElement errorMessage;
    
    @AndroidFindBy(accessibility = TestIDs.LOADING_SPINNER)
    @iOSXCUITFindBy(accessibility = TestIDs.LOADING_SPINNER)
    private WebElement loadingSpinner;
    
    // ==================== PAGE ACTIONS ====================
    
    /**
     * Enter username
     * @param username Username to enter
     * @return LoginPage for method chaining
     */
    public LoginPage enterUsername(String username) {
        logger.info("Entering username: {}", username);
        waitForElementVisible(TestIDs.LOGIN_USERNAME_INPUT);
        enterText(usernameInput, username);
        return this;
    }
    
    /**
     * Enter password
     * @param password Password to enter
     * @return LoginPage for method chaining
     */
    public LoginPage enterPassword(String password) {
        logger.info("Entering password");
        waitForElementVisible(TestIDs.LOGIN_PASSWORD_INPUT);
        enterText(passwordInput, password);
        return this;
    }
    
    /**
     * Click login button
     * @return HomePage if login successful, LoginPage if failed
     */
    public BasePage clickLoginButton() {
        logger.info("Clicking login button");
        waitForElementClickable(TestIDs.LOGIN_SUBMIT_BUTTON);
        clickElement(loginButton);
        
        // Wait for either success (home screen) or error message
        try {
            // Wait for loading to complete
            waitForLoadingToComplete();
            
            // Check if we're on home screen (successful login)
            if (isElementDisplayedByAccessibilityId(TestIDs.HOME_SCREEN)) {
                logger.info("Login successful - navigated to home screen");
                return new HomePage();
            } else {
                logger.warn("Login failed - still on login screen");
                return this;
            }
        } catch (Exception e) {
            logger.error("Error during login process", e);
            return this;
        }
    }
    
    /**
     * Click signup link to navigate to signup screen
     * @return SignupPage
     */
    public SignupPage clickSignupLink() {
        logger.info("Clicking signup link");
        waitForElementClickable(TestIDs.LOGIN_SIGNUP_LINK);
        clickElement(signupLink);
        return new SignupPage();
    }
    
    /**
     * Click forgot password link
     * @return LoginPage for method chaining
     */
    public LoginPage clickForgotPasswordLink() {
        logger.info("Clicking forgot password link");
        waitForElementClickable(TestIDs.LOGIN_FORGOT_PASSWORD);
        clickElement(forgotPasswordLink);
        return this;
    }
    
    /**
     * Perform complete login with credentials
     * @param username Username
     * @param password Password
     * @return HomePage if successful, LoginPage if failed
     */
    public BasePage login(String username, String password) {
        logger.info("Performing login with username: {}", username);
        
        enterUsername(username);
        enterPassword(password);
        return clickLoginButton();
    }
    
    /**
     * Perform login with test user credentials
     * @return HomePage if successful, LoginPage if failed
     */
    public BasePage loginWithTestUser() {
        return login(AppConstants.TEST_USER_USERNAME, AppConstants.TEST_USER_PASSWORD);
    }
    
    /**
     * Perform login with admin credentials
     * @return HomePage if successful, LoginPage if failed
     */
    public BasePage loginWithAdmin() {
        return login(AppConstants.TEST_ADMIN_USERNAME, AppConstants.TEST_ADMIN_PASSWORD);
    }
    
    // ==================== VALIDATION METHODS ====================
    
    /**
     * Get username field value
     * @return Username field value
     */
    public String getUsernameValue() {
        waitForElementVisible(TestIDs.LOGIN_USERNAME_INPUT);
        return usernameInput.getAttribute("text");
    }
    
    /**
     * Get password field value
     * @return Password field value (masked)
     */
    public String getPasswordValue() {
        waitForElementVisible(TestIDs.LOGIN_PASSWORD_INPUT);
        return passwordInput.getAttribute("text");
    }
    
    /**
     * Check if username field is empty
     * @return true if empty, false otherwise
     */
    public boolean isUsernameEmpty() {
        String username = getUsernameValue();
        return username == null || username.trim().isEmpty();
    }
    
    /**
     * Check if password field is empty
     * @return true if empty, false otherwise
     */
    public boolean isPasswordEmpty() {
        String password = getPasswordValue();
        return password == null || password.trim().isEmpty();
    }
    
    /**
     * Check if login button is enabled
     * @return true if enabled, false otherwise
     */
    public boolean isLoginButtonEnabled() {
        waitForElementVisible(TestIDs.LOGIN_SUBMIT_BUTTON);
        return isElementEnabled(loginButton);
    }
    
    /**
     * Check if error message is displayed
     * @return true if error message is visible, false otherwise
     */
    public boolean isErrorMessageDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.VALIDATION_ERROR_TEXT);
    }
    
    /**
     * Get error message text
     * @return Error message text or empty string if not displayed
     */
    public String getErrorMessage() {
        if (isErrorMessageDisplayed()) {
            return getText(errorMessage);
        }
        return "";
    }
    
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
        logger.debug("Waiting for login loading to complete");
        waitForElementToDisappear(TestIDs.LOADING_SPINNER);
        waitForAnimation();
    }
    
    /**
     * Get page title text
     * @return Page title text
     */
    public String getTitleText() {
        waitForElementVisible(TestIDs.LOGIN_TITLE_TEXT);
        return getText(titleText);
    }
    
    /**
     * Check if signup link is displayed
     * @return true if signup link is visible, false otherwise
     */
    public boolean isSignupLinkDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.LOGIN_SIGNUP_LINK);
    }
    
    /**
     * Check if forgot password link is displayed
     * @return true if forgot password link is visible, false otherwise
     */
    public boolean isForgotPasswordLinkDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.LOGIN_FORGOT_PASSWORD);
    }
    
    // ==================== FIELD VALIDATION METHODS ====================
    
    /**
     * Clear username field
     * @return LoginPage for method chaining
     */
    public LoginPage clearUsername() {
        logger.debug("Clearing username field");
        waitForElementVisible(TestIDs.LOGIN_USERNAME_INPUT);
        usernameInput.clear();
        return this;
    }
    
    /**
     * Clear password field
     * @return LoginPage for method chaining
     */
    public LoginPage clearPassword() {
        logger.debug("Clearing password field");
        waitForElementVisible(TestIDs.LOGIN_PASSWORD_INPUT);
        passwordInput.clear();
        return this;
    }
    
    /**
     * Clear all fields
     * @return LoginPage for method chaining
     */
    public LoginPage clearAllFields() {
        clearUsername();
        clearPassword();
        return this;
    }
    
    /**
     * Check if username field has focus
     * @return true if focused, false otherwise
     */
    public boolean isUsernameFocused() {
        waitForElementVisible(TestIDs.LOGIN_USERNAME_INPUT);
        return usernameInput.equals(driver.switchTo().activeElement());
    }
    
    /**
     * Check if password field has focus
     * @return true if focused, false otherwise
     */
    public boolean isPasswordFocused() {
        waitForElementVisible(TestIDs.LOGIN_PASSWORD_INPUT);
        return passwordInput.equals(driver.switchTo().activeElement());
    }
    
    /**
     * Tap on username field to focus
     * @return LoginPage for method chaining
     */
    public LoginPage focusUsernameField() {
        logger.debug("Focusing username field");
        waitForElementClickable(TestIDs.LOGIN_USERNAME_INPUT);
        clickElement(usernameInput);
        return this;
    }
    
    /**
     * Tap on password field to focus
     * @return LoginPage for method chaining
     */
    public LoginPage focusPasswordField() {
        logger.debug("Focusing password field");
        waitForElementClickable(TestIDs.LOGIN_PASSWORD_INPUT);
        clickElement(passwordInput);
        return this;
    }
    
    // ==================== NEGATIVE TEST METHODS ====================
    
    /**
     * Attempt login with empty credentials
     * @return LoginPage (should stay on login page)
     */
    public LoginPage loginWithEmptyCredentials() {
        logger.info("Attempting login with empty credentials");
        clearAllFields();
        clickLoginButton();
        return this;
    }
    
    /**
     * Attempt login with invalid credentials
     * @param username Invalid username
     * @param password Invalid password
     * @return LoginPage (should stay on login page)
     */
    public LoginPage loginWithInvalidCredentials(String username, String password) {
        logger.info("Attempting login with invalid credentials");
        return (LoginPage) login(username, password);
    }
    
    /**
     * Attempt login with only username
     * @param username Username
     * @return LoginPage (should stay on login page)
     */
    public LoginPage loginWithUsernameOnly(String username) {
        logger.info("Attempting login with username only");
        clearAllFields();
        enterUsername(username);
        clickLoginButton();
        return this;
    }
    
    /**
     * Attempt login with only password
     * @param password Password
     * @return LoginPage (should stay on login page)
     */
    public LoginPage loginWithPasswordOnly(String password) {
        logger.info("Attempting login with password only");
        clearAllFields();
        enterPassword(password);
        clickLoginButton();
        return this;
    }
    
    // ==================== INHERITED METHODS ====================
    
    @Override
    public boolean isDisplayed() {
        try {
            return isElementDisplayedByAccessibilityId(TestIDs.LOGIN_SCREEN);
        } catch (Exception e) {
            logger.debug("Login screen not displayed", e);
            return false;
        }
    }
    
    @Override
    public String getPageTitle() {
        return "Login Screen";
    }
    
    /**
     * Wait for login page to be fully loaded
     * @return LoginPage for method chaining
     */
    public LoginPage waitForPageToLoad() {
        logger.info("Waiting for login page to load");
        waitForElementVisible(TestIDs.LOGIN_SCREEN);
        waitForElementVisible(TestIDs.LOGIN_USERNAME_INPUT);
        waitForElementVisible(TestIDs.LOGIN_PASSWORD_INPUT);
        waitForElementVisible(TestIDs.LOGIN_SUBMIT_BUTTON);
        logger.info("Login page loaded successfully");
        return this;
    }
    
    /**
     * Verify all essential elements are present
     * @return true if all elements are present, false otherwise
     */
    public boolean verifyPageElements() {
        logger.info("Verifying login page elements");
        
        boolean allElementsPresent = 
            isElementDisplayedByAccessibilityId(TestIDs.LOGIN_SCREEN) &&
            isElementDisplayedByAccessibilityId(TestIDs.LOGIN_USERNAME_INPUT) &&
            isElementDisplayedByAccessibilityId(TestIDs.LOGIN_PASSWORD_INPUT) &&
            isElementDisplayedByAccessibilityId(TestIDs.LOGIN_SUBMIT_BUTTON) &&
            isElementDisplayedByAccessibilityId(TestIDs.LOGIN_SIGNUP_LINK);
        
        logger.info("Login page elements verification: {}", allElementsPresent ? "PASSED" : "FAILED");
        return allElementsPresent;
    }
}