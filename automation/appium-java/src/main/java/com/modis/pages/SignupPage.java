package com.modis.pages;

import com.modis.base.BasePage;
import com.modis.constants.TestIDs;
import com.modis.constants.AppConstants;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;

/**
 * Page Object for Signup Screen
 * Handles user registration functionality and related interactions
 */
public class SignupPage extends BasePage {
    
    // ==================== PAGE ELEMENTS ====================
    
    @AndroidFindBy(accessibility = TestIDs.SIGNUP_SCREEN)
    @iOSXCUITFindBy(accessibility = TestIDs.SIGNUP_SCREEN)
    private WebElement signupScreen;
    
    @AndroidFindBy(accessibility = TestIDs.SIGNUP_TITLE_TEXT)
    @iOSXCUITFindBy(accessibility = TestIDs.SIGNUP_TITLE_TEXT)
    private WebElement titleText;
    
    @AndroidFindBy(accessibility = TestIDs.SIGNUP_USERNAME_INPUT)
    @iOSXCUITFindBy(accessibility = TestIDs.SIGNUP_USERNAME_INPUT)
    private WebElement usernameInput;
    
    @AndroidFindBy(accessibility = TestIDs.SIGNUP_FULLNAME_INPUT)
    @iOSXCUITFindBy(accessibility = TestIDs.SIGNUP_FULLNAME_INPUT)
    private WebElement fullnameInput;
    
    @AndroidFindBy(accessibility = TestIDs.SIGNUP_EMAIL_INPUT)
    @iOSXCUITFindBy(accessibility = TestIDs.SIGNUP_EMAIL_INPUT)
    private WebElement emailInput;
    
    @AndroidFindBy(accessibility = TestIDs.SIGNUP_PHONE_INPUT)
    @iOSXCUITFindBy(accessibility = TestIDs.SIGNUP_PHONE_INPUT)
    private WebElement phoneInput;
    
    @AndroidFindBy(accessibility = TestIDs.SIGNUP_PASSWORD_INPUT)
    @iOSXCUITFindBy(accessibility = TestIDs.SIGNUP_PASSWORD_INPUT)
    private WebElement passwordInput;
    
    @AndroidFindBy(accessibility = TestIDs.SIGNUP_CONFIRM_PASSWORD_INPUT)
    @iOSXCUITFindBy(accessibility = TestIDs.SIGNUP_CONFIRM_PASSWORD_INPUT)
    private WebElement confirmPasswordInput;
    
    @AndroidFindBy(accessibility = TestIDs.SIGNUP_SUBMIT_BUTTON)
    @iOSXCUITFindBy(accessibility = TestIDs.SIGNUP_SUBMIT_BUTTON)
    private WebElement signupButton;
    
    @AndroidFindBy(accessibility = TestIDs.SIGNUP_LOGIN_LINK)
    @iOSXCUITFindBy(accessibility = TestIDs.SIGNUP_LOGIN_LINK)
    private WebElement loginLink;
    
    @AndroidFindBy(accessibility = TestIDs.SIGNUP_BACK_BUTTON)
    @iOSXCUITFindBy(accessibility = TestIDs.SIGNUP_BACK_BUTTON)
    private WebElement backButton;
    
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
     * @return SignupPage for method chaining
     */
    public SignupPage enterUsername(String username) {
        logger.info("Entering username: {}", username);
        enterTextByAccessibilityId(TestIDs.SIGNUP_USERNAME_INPUT, username);
        return this;
    }
    
    /**
     * Enter full name
     * @param fullname Full name to enter
     * @return SignupPage for method chaining
     */
    public SignupPage enterFullname(String fullname) {
        logger.info("Entering full name: {}", fullname);
        enterTextByAccessibilityId(TestIDs.SIGNUP_FULLNAME_INPUT, fullname);
        return this;
    }
    
    /**
     * Enter email
     * @param email Email to enter
     * @return SignupPage for method chaining
     */
    public SignupPage enterEmail(String email) {
        logger.info("Entering email: {}", email);
        enterTextByAccessibilityId(TestIDs.SIGNUP_EMAIL_INPUT, email);
        return this;
    }
    
    /**
     * Enter phone number
     * @param phone Phone number to enter
     * @return SignupPage for method chaining
     */
    public SignupPage enterPhone(String phone) {
        logger.info("Entering phone: {}", phone);
        enterTextByAccessibilityId(TestIDs.SIGNUP_PHONE_INPUT, phone);
        return this;
    }
    
    /**
     * Enter password
     * @param password Password to enter
     * @return SignupPage for method chaining
     */
    public SignupPage enterPassword(String password) {
        logger.info("Entering password");
        enterTextByAccessibilityId(TestIDs.SIGNUP_PASSWORD_INPUT, password);
        return this;
    }
    
    /**
     * Enter confirm password
     * @param confirmPassword Confirm password to enter
     * @return SignupPage for method chaining
     */
    public SignupPage enterConfirmPassword(String confirmPassword) {
        logger.info("Entering confirm password");
        enterTextByAccessibilityId(TestIDs.SIGNUP_CONFIRM_PASSWORD_INPUT, confirmPassword);
        return this;
    }
    
    /**
     * Click signup button
     * @return HomePage if signup successful, SignupPage if failed
     */
    public BasePage clickSignupButton() {
        logger.info("Clicking signup button");
        waitForElementClickable(TestIDs.SIGNUP_SUBMIT_BUTTON);
        clickElement(signupButton);
        
        // Wait for either success (home screen) or error message
        try {
            // Wait for loading to complete
            waitForLoadingToComplete();
            
            // Check if we're on home screen (successful signup)
            if (isElementDisplayedByAccessibilityId(TestIDs.HOME_SCREEN)) {
                logger.info("Signup successful - navigated to home screen");
                return new HomePage();
            } else {
                logger.warn("Signup failed - still on signup screen");
                return this;
            }
        } catch (Exception e) {
            logger.error("Error during signup process", e);
            return this;
        }
    }
    
    /**
     * Click login link to navigate to login screen
     * @return LoginPage
     */
    public LoginPage clickLoginLink() {
        logger.info("Clicking login link");
        waitForElementClickable(TestIDs.SIGNUP_LOGIN_LINK);
        clickElement(loginLink);
        return new LoginPage();
    }
    
    /**
     * Click back button to navigate to previous screen
     * @return LoginPage or LoadingPage depending on navigation
     */
    public BasePage clickBackButton() {
        logger.info("Clicking back button");
        waitForElementClickable(TestIDs.SIGNUP_BACK_BUTTON);
        clickElement(backButton);
        
        // Could navigate to login or loading screen
        if (isElementDisplayedByAccessibilityId(TestIDs.LOGIN_SCREEN)) {
            return new LoginPage();
        } else {
            return new LoadingPage();
        }
    }
    
    /**
     * Perform complete signup with all required information
     * @param username Username
     * @param fullname Full name
     * @param email Email
     * @param phone Phone number
     * @param password Password
     * @param confirmPassword Confirm password
     * @return HomePage if successful, SignupPage if failed
     */
    public BasePage signup(String username, String fullname, String email, String phone, String password, String confirmPassword) {
        logger.info("Performing signup with username: {}", username);
        
        enterUsername(username);
        enterFullname(fullname);
        enterEmail(email);
        enterPhone(phone);
        enterPassword(password);
        enterConfirmPassword(confirmPassword);
        
        return clickSignupButton();
    }
    
    /**
     * Perform signup with test user data
     * @return HomePage if successful, SignupPage if failed
     */
    public BasePage signupWithTestUser() {
        return signup(
            AppConstants.TEST_USER_USERNAME,
            AppConstants.TEST_USER_FULLNAME,
            AppConstants.TEST_USER_EMAIL,
            AppConstants.TEST_USER_PHONE,
            AppConstants.TEST_USER_PASSWORD,
            AppConstants.TEST_USER_PASSWORD
        );
    }
    
    /**
     * Perform signup with random test data
     * @return HomePage if successful, SignupPage if failed
     */
    public BasePage signupWithRandomUser() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        return signup(
            "testuser" + timestamp,
            "Test User " + timestamp,
            "testuser" + timestamp + "@example.com",
            "123456789" + timestamp.substring(timestamp.length() - 1),
            "testpass123",
            "testpass123"
        );
    }
    
    // ==================== VALIDATION METHODS ====================
    
    /**
     * Get username field value
     * @return Username field value
     */
    public String getUsernameValue() {
        waitForElementVisible(TestIDs.SIGNUP_USERNAME_INPUT);
        return usernameInput.getAttribute("text");
    }
    
    /**
     * Get fullname field value
     * @return Fullname field value
     */
    public String getFullnameValue() {
        waitForElementVisible(TestIDs.SIGNUP_FULLNAME_INPUT);
        return fullnameInput.getAttribute("text");
    }
    
    /**
     * Get email field value
     * @return Email field value
     */
    public String getEmailValue() {
        waitForElementVisible(TestIDs.SIGNUP_EMAIL_INPUT);
        return emailInput.getAttribute("text");
    }
    
    /**
     * Get phone field value
     * @return Phone field value
     */
    public String getPhoneValue() {
        waitForElementVisible(TestIDs.SIGNUP_PHONE_INPUT);
        return phoneInput.getAttribute("text");
    }
    
    /**
     * Check if signup button is enabled
     * @return true if enabled, false otherwise
     */
    public boolean isSignupButtonEnabled() {
        waitForElementVisible(TestIDs.SIGNUP_SUBMIT_BUTTON);
        return isElementEnabled(signupButton);
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
        logger.debug("Waiting for signup loading to complete");
        waitForElementToDisappear(TestIDs.LOADING_SPINNER);
        waitForAnimation();
    }
    
    /**
     * Get page title text
     * @return Page title text
     */
    public String getTitleText() {
        waitForElementVisible(TestIDs.SIGNUP_TITLE_TEXT);
        return getText(titleText);
    }
    
    // ==================== FIELD VALIDATION METHODS ====================
    
    /**
     * Clear all input fields
     * @return SignupPage for method chaining
     */
    public SignupPage clearAllFields() {
        logger.debug("Clearing all signup fields");
        
        waitForElementVisible(TestIDs.SIGNUP_USERNAME_INPUT);
        usernameInput.clear();
        
        waitForElementVisible(TestIDs.SIGNUP_FULLNAME_INPUT);
        fullnameInput.clear();
        
        waitForElementVisible(TestIDs.SIGNUP_EMAIL_INPUT);
        emailInput.clear();
        
        waitForElementVisible(TestIDs.SIGNUP_PHONE_INPUT);
        phoneInput.clear();
        
        waitForElementVisible(TestIDs.SIGNUP_PASSWORD_INPUT);
        passwordInput.clear();
        
        waitForElementVisible(TestIDs.SIGNUP_CONFIRM_PASSWORD_INPUT);
        confirmPasswordInput.clear();
        
        return this;
    }
    
    /**
     * Check if all required fields are filled
     * @return true if all required fields have values, false otherwise
     */
    public boolean areAllRequiredFieldsFilled() {
        return !getUsernameValue().trim().isEmpty() &&
               !getFullnameValue().trim().isEmpty() &&
               !getEmailValue().trim().isEmpty() &&
               !getPhoneValue().trim().isEmpty() &&
               !passwordInput.getAttribute("text").trim().isEmpty() &&
               !confirmPasswordInput.getAttribute("text").trim().isEmpty();
    }
    
    /**
     * Check if passwords match
     * @return true if passwords match, false otherwise
     */
    public boolean doPasswordsMatch() {
        String password = passwordInput.getAttribute("text");
        String confirmPassword = confirmPasswordInput.getAttribute("text");
        return password != null && password.equals(confirmPassword);
    }
    
    /**
     * Validate email format
     * @return true if email format is valid, false otherwise
     */
    public boolean isEmailFormatValid() {
        String email = getEmailValue();
        return email != null && email.matches(AppConstants.EMAIL_PATTERN);
    }
    
    /**
     * Validate phone format
     * @return true if phone format is valid, false otherwise
     */
    public boolean isPhoneFormatValid() {
        String phone = getPhoneValue();
        return phone != null && phone.matches(AppConstants.PHONE_PATTERN);
    }
    
    /**
     * Validate username format
     * @return true if username format is valid, false otherwise
     */
    public boolean isUsernameFormatValid() {
        String username = getUsernameValue();
        return username != null && 
               username.length() >= AppConstants.MIN_USERNAME_LENGTH &&
               username.length() <= AppConstants.MAX_USERNAME_LENGTH &&
               username.matches(AppConstants.USERNAME_PATTERN);
    }
    
    /**
     * Validate password strength
     * @return true if password meets requirements, false otherwise
     */
    public boolean isPasswordValid() {
        String password = passwordInput.getAttribute("text");
        return password != null && 
               password.length() >= AppConstants.MIN_PASSWORD_LENGTH &&
               password.length() <= AppConstants.MAX_PASSWORD_LENGTH;
    }
    
    // ==================== NEGATIVE TEST METHODS ====================
    
    /**
     * Attempt signup with empty fields
     * @return SignupPage (should stay on signup page)
     */
    public SignupPage signupWithEmptyFields() {
        logger.info("Attempting signup with empty fields");
        clearAllFields();
        clickSignupButton();
        return this;
    }
    
    /**
     * Attempt signup with mismatched passwords
     * @param username Username
     * @param fullname Full name
     * @param email Email
     * @param phone Phone
     * @param password Password
     * @param wrongConfirmPassword Wrong confirm password
     * @return SignupPage (should stay on signup page)
     */
    public SignupPage signupWithMismatchedPasswords(String username, String fullname, String email, String phone, String password, String wrongConfirmPassword) {
        logger.info("Attempting signup with mismatched passwords");
        return (SignupPage) signup(username, fullname, email, phone, password, wrongConfirmPassword);
    }
    
    /**
     * Attempt signup with invalid email
     * @param username Username
     * @param fullname Full name
     * @param invalidEmail Invalid email
     * @param phone Phone
     * @param password Password
     * @return SignupPage (should stay on signup page)
     */
    public SignupPage signupWithInvalidEmail(String username, String fullname, String invalidEmail, String phone, String password) {
        logger.info("Attempting signup with invalid email: {}", invalidEmail);
        return (SignupPage) signup(username, fullname, invalidEmail, phone, password, password);
    }
    
    /**
     * Attempt signup with existing username
     * @param existingUsername Existing username
     * @param fullname Full name
     * @param email Email
     * @param phone Phone
     * @param password Password
     * @return SignupPage (should stay on signup page)
     */
    public SignupPage signupWithExistingUsername(String existingUsername, String fullname, String email, String phone, String password) {
        logger.info("Attempting signup with existing username: {}", existingUsername);
        return (SignupPage) signup(existingUsername, fullname, email, phone, password, password);
    }
    
    // ==================== SCROLL AND NAVIGATION ====================
    
    /**
     * Scroll to make all fields visible (for smaller screens)
     * @return SignupPage for method chaining
     */
    public SignupPage scrollToViewAllFields() {
        logger.debug("Scrolling to view all signup fields");
        
        // Scroll down to ensure all fields are visible
        if (!isElementDisplayedByAccessibilityId(TestIDs.SIGNUP_SUBMIT_BUTTON)) {
            scrollDownBase();
        }
        
        return this;
    }
    
    /**
     * Navigate through fields using tab/next
     * @return SignupPage for method chaining
     */
    public SignupPage navigateToNextField() {
        logger.debug("Navigating to next field");
        hideKeyboard();
        return this;
    }
    
    // ==================== INHERITED METHODS ====================
    
    @Override
    public boolean isDisplayed() {
        try {
            return isElementDisplayedByAccessibilityId(TestIDs.SIGNUP_SCREEN);
        } catch (Exception e) {
            logger.debug("Signup screen not displayed", e);
            return false;
        }
    }
    
    @Override
    public String getPageTitle() {
        return "Signup Screen";
    }
    
    /**
     * Wait for signup page to be fully loaded
     * @return SignupPage for method chaining
     */
    public SignupPage waitForPageToLoad() {
        logger.info("Waiting for signup page to load");
        waitForElementVisible(TestIDs.SIGNUP_SCREEN);
        waitForElementVisible(TestIDs.SIGNUP_USERNAME_INPUT);
        waitForElementVisible(TestIDs.SIGNUP_SUBMIT_BUTTON);
        logger.info("Signup page loaded successfully");
        return this;
    }
    
    /**
     * Verify all essential elements are present
     * @return true if all elements are present, false otherwise
     */
    public boolean verifyPageElements() {
        logger.info("Verifying signup page elements");
        
        boolean allElementsPresent = 
            isElementDisplayedByAccessibilityId(TestIDs.SIGNUP_SCREEN) &&
            isElementDisplayedByAccessibilityId(TestIDs.SIGNUP_USERNAME_INPUT) &&
            isElementDisplayedByAccessibilityId(TestIDs.SIGNUP_FULLNAME_INPUT) &&
            isElementDisplayedByAccessibilityId(TestIDs.SIGNUP_EMAIL_INPUT) &&
            isElementDisplayedByAccessibilityId(TestIDs.SIGNUP_PHONE_INPUT) &&
            isElementDisplayedByAccessibilityId(TestIDs.SIGNUP_PASSWORD_INPUT) &&
            isElementDisplayedByAccessibilityId(TestIDs.SIGNUP_CONFIRM_PASSWORD_INPUT) &&
            isElementDisplayedByAccessibilityId(TestIDs.SIGNUP_SUBMIT_BUTTON) &&
            isElementDisplayedByAccessibilityId(TestIDs.SIGNUP_LOGIN_LINK);
        
        logger.info("Signup page elements verification: {}", allElementsPresent ? "PASSED" : "FAILED");
        return allElementsPresent;
    }
}
