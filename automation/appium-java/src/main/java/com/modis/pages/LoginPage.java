package com.modis.pages;

import com.modis.base.BasePage;
import com.modis.constants.TestIDs;
import com.modis.constants.AppConstants;
import com.modis.utils.TestDataManager;
import com.modis.utils.UiDebugUtils;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;

import java.util.Map;

/**
 * Page Object for Login Screen
 * Handles login functionality and related interactions
 */
public class LoginPage extends BasePage {
    
    // ==================== PAGE ELEMENTS ====================
    // NOTE:
    // Do not rely on the root container testID/accessibilityId (login_screen) for React Native.
    // In the current RN code, the root LinearGradient sets testID/accessibilityLabel but does NOT set accessible={true},
    // so it may not be exposed as an accessibility node on Android -> AppiumBy.accessibilityId("login_screen") can fail.
    // Use stable, interactive child elements as the screen "ready" signal instead (inputs / buttons).
    
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
        // Dùng helper theo accessibilityId/testID để tránh phụ thuộc @AndroidFindBy(id=...) (RN có thể không map vào resource-id)
        enterTextByAccessibilityId(TestIDs.LOGIN_USERNAME_INPUT, username);
        return this;
    }
    
    /**
     * Enter password
     * @param password Password to enter
     * @return LoginPage for method chaining
     */
    public LoginPage enterPassword(String password) {
        logger.info("Entering password");
        enterTextByAccessibilityId(TestIDs.LOGIN_PASSWORD_INPUT, password);
        return this;
    }
    
    /**
     * Click login button
     * @return HomePage if login successful, LoginPage if failed
     */
    public BasePage clickLoginButton() {
        logger.info("Clicking login button");
        waitForElementClickable(TestIDs.LOGIN_SUBMIT_BUTTON);
        clickByAccessibilityId(TestIDs.LOGIN_SUBMIT_BUTTON);

        // Wait until:
        // - Home screen appears (login OK), OR
        // - login button becomes enabled again (request finished but still on login -> invalid creds)
        try {
            waitUtils.waitForCondition(d -> {
                // Success signals
                if (isElementDisplayedByAccessibilityId(TestIDs.HOME_SCREEN) ||
                    isElementDisplayedByAccessibilityId(TestIDs.TOPBAR_AVATAR_BUTTON) ||
                    isElementDisplayedByAccessibilityId(TestIDs.TAKE_SCREEN) ||
                    isElementDisplayedByAccessibilityId(TestIDs.FEED_SCREEN)) {
                    return true;
                }

                // Failure/finished signal: login button enabled again
                try {
                    WebElement btn = findByAccessibilityId(TestIDs.LOGIN_SUBMIT_BUTTON);
                    return btn.isEnabled();
                } catch (Exception ignored) {
                    return false;
                }
            }, AppConstants.PAGE_LOAD_TIMEOUT);
        } catch (Exception e) {
            UiDebugUtils.dumpOnFailure(driver, "login_outcome_timeout");
            throw e;
        }

        if (isElementDisplayedByAccessibilityId(TestIDs.HOME_SCREEN) ||
            isElementDisplayedByAccessibilityId(TestIDs.TOPBAR_AVATAR_BUTTON) ||
            isElementDisplayedByAccessibilityId(TestIDs.TAKE_SCREEN) ||
            isElementDisplayedByAccessibilityId(TestIDs.FEED_SCREEN)) {
            logger.info("Login successful - navigated to home flow");
            return new HomePage();
        }

        logger.warn("Login finished but still on login screen (likely invalid credentials)");
        return this;
    }
    
    /**
     * Click signup link to navigate to signup screen
     * @return SignupPage
     */
    public SignupPage clickSignupLink() {
        logger.info("Clicking signup link");
        clickByAccessibilityId(TestIDs.LOGIN_SIGNUP_LINK);
        return new SignupPage();
    }
    
    /**
     * Click forgot password link
     * @return LoginPage for method chaining
     */
    public LoginPage clickForgotPasswordLink() {
        logger.info("Clicking forgot password link");
        clickByAccessibilityId(TestIDs.LOGIN_FORGOT_PASSWORD);
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
        Map<String, String> user = TestDataManager.getRealLoginUser();
        return login(user.get("username"), user.get("password"));
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
        waitUtils.waitForElementToBeVisible(usernameInput);
        return usernameInput.getAttribute("text");
    }
    
    /**
     * Get password field value
     * @return Password field value (masked)
     */
    public String getPasswordValue() {
        waitUtils.waitForElementToBeVisible(passwordInput);
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
        waitUtils.waitForElementToBeVisible(loginButton);
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
        waitUtils.waitForElementToBeVisible(titleText);
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
        waitUtils.waitForElementToBeVisible(usernameInput);
        usernameInput.clear();
        return this;
    }
    
    /**
     * Clear password field
     * @return LoginPage for method chaining
     */
    public LoginPage clearPassword() {
        logger.debug("Clearing password field");
        waitUtils.waitForElementToBeVisible(passwordInput);
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
        waitUtils.waitForElementToBeVisible(usernameInput);
        return usernameInput.equals(driver.switchTo().activeElement());
    }
    
    /**
     * Check if password field has focus
     * @return true if focused, false otherwise
     */
    public boolean isPasswordFocused() {
        waitUtils.waitForElementToBeVisible(passwordInput);
        return passwordInput.equals(driver.switchTo().activeElement());
    }
    
    /**
     * Tap on username field to focus
     * @return LoginPage for method chaining
     */
    public LoginPage focusUsernameField() {
        logger.debug("Focusing username field");
        waitUtils.waitForElementToBeClickable(usernameInput);
        clickElement(usernameInput);
        return this;
    }
    
    /**
     * Tap on password field to focus
     * @return LoginPage for method chaining
     */
    public LoginPage focusPasswordField() {
        logger.debug("Focusing password field");
        waitUtils.waitForElementToBeClickable(passwordInput);
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
            // Prefer stable child elements; these are on TextInput/TouchableOpacity with accessible={true}
            return isElementDisplayedByAccessibilityId(TestIDs.LOGIN_USERNAME_INPUT) ||
                   isElementDisplayedByAccessibilityId(TestIDs.LOGIN_SUBMIT_BUTTON);
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
        // Avoid waiting on login_screen root. Wait for critical interactive elements instead.
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
            isElementDisplayedByAccessibilityId(TestIDs.LOGIN_USERNAME_INPUT) &&
            isElementDisplayedByAccessibilityId(TestIDs.LOGIN_PASSWORD_INPUT) &&
            isElementDisplayedByAccessibilityId(TestIDs.LOGIN_SUBMIT_BUTTON) &&
            isElementDisplayedByAccessibilityId(TestIDs.LOGIN_SIGNUP_LINK);
        
        logger.info("Login page elements verification: {}", allElementsPresent ? "PASSED" : "FAILED");
        return allElementsPresent;
    }
}
