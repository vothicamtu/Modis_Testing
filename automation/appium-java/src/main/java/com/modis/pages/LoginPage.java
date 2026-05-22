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
    
    // ==================== ERROR DIALOG ELEMENTS ====================
    
    // Error dialog elements
    @AndroidFindBy(accessibility = TestIDs.ERROR_DIALOG)
    @iOSXCUITFindBy(accessibility = TestIDs.ERROR_DIALOG)
    private WebElement errorDialog;
    
    @AndroidFindBy(accessibility = TestIDs.ERROR_DIALOG_OK_BUTTON)
    @iOSXCUITFindBy(accessibility = TestIDs.ERROR_DIALOG_OK_BUTTON)
    private WebElement errorDialogOkButton;
    
    @AndroidFindBy(accessibility = TestIDs.LOGIN_ERROR_DIALOG)
    @iOSXCUITFindBy(accessibility = TestIDs.LOGIN_ERROR_DIALOG)
    private WebElement loginErrorDialog;
    
    @AndroidFindBy(accessibility = TestIDs.LOGIN_ERROR_OK_BUTTON)
    @iOSXCUITFindBy(accessibility = TestIDs.LOGIN_ERROR_OK_BUTTON)
    private WebElement loginErrorOkButton;
    
    // ==================== ERROR DIALOG HANDLING ====================
    
    /**
     * Check if login error dialog is displayed
     * @return true if error dialog is visible
     */
    public boolean isLoginErrorDialogDisplayed() {
        logger.debug("Checking if login error dialog is displayed");
        
        // Try multiple possible selectors for error dialog
        try {
            return isElementDisplayedByAccessibilityId(TestIDs.LOGIN_ERROR_DIALOG) ||
                   isElementDisplayedByAccessibilityId(TestIDs.ERROR_DIALOG) ||
                   isElementDisplayedByAccessibilityId(TestIDs.ALERT_DIALOG) ||
                   checkForErrorTextPrivate("Thông báo") ||
                   checkForErrorTextPrivate("Tài khoản hoặc mật khẩu không chính xác") ||
                   checkForErrorTextPrivate("Mật khẩu không chính xác!") ||
                   checkForErrorTextPrivate("Tên đăng nhập không chính xác!") ||
                   checkForErrorTextPrivate("Vui lòng nhập đầy đủ thông tin!") ||
                   checkForErrorTextPrivate("Đăng nhập thất bại");
        } catch (Exception e) {
            logger.debug("Error checking for login error dialog: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Helper method to check for error text using XPath
     * @param text Text to search for
     * @return true if text is found
     */
    private boolean checkForErrorTextPrivate(String text) {
        try {
            String xpath = String.format("//*[@text='%s' or @content-desc='%s' or contains(@text,'%s')]", text, text, text);
            WebElement element = findByXPath(xpath);
            return element != null && isElementDisplayed(element);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Public method to check for error text (for test access)
     * @param text Text to search for
     * @return true if text is found
     */
    public boolean checkForErrorText(String text) {
        return checkForErrorTextPrivate(text);
    }
    
    /**
     * Get error dialog message text
     * @return Error message text
     */
    public String getErrorDialogMessage() {
        logger.info("Getting error dialog message");
        
        if (isLoginErrorDialogDisplayed()) {
            // Try to find error message text with different possible messages
            try {
                // Check for specific Vietnamese error messages
                String[] possibleMessages = {
                    "Tài khoản hoặc mật khẩu không chính xác",
                    "Mật khẩu không chính xác!",
                    "Tên đăng nhập không chính xác!",
                    "Vui lòng nhập đầy đủ thông tin!",
                    "Đăng nhập thất bại",
                    "Thông tin đăng nhập không hợp lệ",
                    "Sai tên đăng nhập hoặc mật khẩu"
                };
                
                for (String message : possibleMessages) {
                    if (checkForErrorTextPrivate(message)) {
                        return message;
                    }
                }
                
                // Try to get text from dialog container using XPath
                String[] xpathSelectors = {
                    "//android.widget.TextView[contains(@text,'mật khẩu')]",
                    "//android.widget.TextView[contains(@text,'đăng nhập')]", 
                    "//android.widget.TextView[contains(@text,'không chính xác')]",
                    "//android.widget.TextView[contains(@text,'thất bại')]",
                    "//android.widget.TextView[contains(@text,'không hợp lệ')]"
                };
                
                for (String xpath : xpathSelectors) {
                    try {
                        WebElement messageElement = findByXPath(xpath);
                        if (messageElement != null) {
                            return getText(messageElement);
                        }
                    } catch (Exception e) {
                        // Continue to next selector
                    }
                }
                
            } catch (Exception e) {
                logger.warn("Could not get error dialog message: " + e.getMessage());
            }
        }
        
        return "Login error occurred";
    }
    
    /**
     * Dismiss login error dialog by clicking OK
     * @return LoginPage for method chaining
     */
    public LoginPage dismissLoginErrorDialog() {
        logger.info("Dismissing login error dialog");
        
        if (isLoginErrorDialogDisplayed()) {
            try {
                // Strategy 1: Try accessibility IDs first
                if (isElementDisplayedByAccessibilityId(TestIDs.LOGIN_ERROR_OK_BUTTON)) {
                    logger.info("Found OK button by LOGIN_ERROR_OK_BUTTON ID");
                    clickByAccessibilityId(TestIDs.LOGIN_ERROR_OK_BUTTON);
                } else if (isElementDisplayedByAccessibilityId(TestIDs.ERROR_DIALOG_OK_BUTTON)) {
                    logger.info("Found OK button by ERROR_DIALOG_OK_BUTTON ID");
                    clickByAccessibilityId(TestIDs.ERROR_DIALOG_OK_BUTTON);
                } else if (isElementDisplayedByAccessibilityId(TestIDs.ALERT_OK_BUTTON)) {
                    logger.info("Found OK button by ALERT_OK_BUTTON ID");
                    clickByAccessibilityId(TestIDs.ALERT_OK_BUTTON);
                } else {
                    // Strategy 2: Try finding by text content
                    logger.info("Trying to find OK button by text");
                    boolean okButtonFound = false;
                    
                    // Try different text variations
                    String[] okTexts = {"OK", "ok", "Ok", "Đồng ý", "Xác nhận"};
                    for (String okText : okTexts) {
                        try {
                            String xpath = String.format("//*[@text='%s' or @content-desc='%s']", okText, okText);
                            WebElement okButton = findByXPath(xpath);
                            if (okButton != null && isElementDisplayed(okButton)) {
                                logger.info("Found OK button by text: {}", okText);
                                clickElement(okButton);
                                okButtonFound = true;
                                break;
                            }
                        } catch (Exception e) {
                            logger.debug("Could not find OK button by text '{}': {}", okText, e.getMessage());
                        }
                    }
                    
                    if (!okButtonFound) {
                        // Strategy 3: Try finding any button in dialog
                        logger.info("Trying to find any button in dialog");
                        String[] buttonXpaths = {
                            "//android.widget.Button[contains(@text,'OK') or contains(@content-desc,'OK')]",
                            "//android.widget.Button",
                            "//*[@class='android.widget.Button']",
                            "//*[contains(@text,'OK')]",
                            "//*[contains(@content-desc,'OK')]"
                        };
                        
                        for (String xpath : buttonXpaths) {
                            try {
                                WebElement button = findByXPath(xpath);
                                if (button != null && isElementDisplayed(button)) {
                                    logger.info("Found button by xpath: {}", xpath);
                                    clickElement(button);
                                    okButtonFound = true;
                                    break;
                                }
                            } catch (Exception e) {
                                logger.debug("Could not find button by xpath '{}': {}", xpath, e.getMessage());
                            }
                        }
                    }
                    
                    if (!okButtonFound) {
                        // Strategy 4: Try tapping at common OK button positions
                        logger.warn("Could not find OK button, trying to tap at common positions");
                        org.openqa.selenium.Dimension screenSize = getScreenSize();
                        int centerX = screenSize.width / 2;
                        int bottomY = (int) (screenSize.height * 0.7); // 70% down from top
                        
                        tapAtCoordinates(centerX, bottomY);
                        logger.info("Tapped at coordinates ({}, {}) for OK button", centerX, bottomY);
                    }
                }
                
                // Wait for dialog to disappear
                waitForErrorDialogToDisappear();
                
            } catch (Exception e) {
                logger.error("Failed to dismiss login error dialog: " + e.getMessage());
                // Final fallback: try tapping at center-bottom of screen
                try {
                    org.openqa.selenium.Dimension screenSize = getScreenSize();
                    int centerX = screenSize.width / 2;
                    int bottomY = (int) (screenSize.height * 0.65);
                    tapAtCoordinates(centerX, bottomY);
                    logger.info("Fallback: tapped at center-bottom ({}, {})", centerX, bottomY);
                    waitForErrorDialogToDisappear();
                } catch (Exception fallbackEx) {
                    logger.error("Fallback tap also failed: " + fallbackEx.getMessage());
                }
            }
        } else {
            logger.info("No error dialog to dismiss");
        }
        
        return this;
    }
    
    /**
     * Helper method to click OK button using text search
     */
    private void clickOkButton() {
        try {
            String xpath = "//*[@text='OK' or @content-desc='OK' or contains(@text,'OK')]";
            WebElement okButton = findByXPath(xpath);
            clickElement(okButton);
        } catch (Exception e) {
            logger.warn("Could not find OK button by text: " + e.getMessage());
        }
    }
    
    /**
     * Wait for error dialog to disappear
     */
    private void waitForErrorDialogToDisappear() {
        logger.debug("Waiting for error dialog to disappear");
        
        try {
            // Wait up to 10 seconds for dialog to disappear
            waitUtils.waitForCondition(driver -> !isLoginErrorDialogDisplayed(), 10);
            logger.info("Error dialog successfully disappeared");
        } catch (Exception e) {
            logger.warn("Error dialog may still be visible after waiting: " + e.getMessage());
            // Try one more time with a different approach
            try {
                Thread.sleep(2000); // Wait 2 more seconds
                if (isLoginErrorDialogDisplayed()) {
                    logger.warn("Error dialog still visible after additional wait");
                } else {
                    logger.info("Error dialog disappeared after additional wait");
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Handle login error dialog if it appears
     * @return true if error dialog was handled, false if no dialog appeared
     */
    public boolean handleLoginErrorDialogIfPresent() {
        logger.debug("Checking for and handling login error dialog");
        
        if (isLoginErrorDialogDisplayed()) {
            String errorMessage = getErrorDialogMessage();
            logger.info("Login error dialog appeared with message: {}", errorMessage);
            dismissLoginErrorDialog();
            return true;
        }
        
        return false;
    }
    
    /**
     * Login with credentials and handle error dialog if it appears
     * @param username Username
     * @param password Password
     * @return HomePage if successful, LoginPage if failed
     */
    public BasePage loginWithErrorHandling(String username, String password) {
        logger.info("Attempting login with error handling for user: {}", username);
        
        // Clear existing text and enter credentials
        clearAndEnterUsername(username);
        clearAndEnterPassword(password);
        
        // Click login button
        clickLoginButton();
        
        // Wait a moment for response
        waitFor(2);
        
        // Check for error dialog
        if (handleLoginErrorDialogIfPresent()) {
            logger.info("Login failed - error dialog was displayed and dismissed");
            return this; // Stay on login page
        }
        
        // Check if we navigated to home screen
        if (isElementDisplayedByAccessibilityId(TestIDs.HOME_SCREEN)) {
            logger.info("Login successful - navigated to home screen");
            return new HomePage();
        }
        
        // If still on login screen without error dialog, something else happened
        logger.warn("Login attempt completed but result is unclear");
        return this;
    }
    
    /**
     * Clear username field and enter new username
     * @param username Username to enter
     * @return LoginPage for method chaining
     */
    public LoginPage clearAndEnterUsername(String username) {
        logger.info("Clearing and entering username: {}", username);
        waitForElementVisible(TestIDs.LOGIN_USERNAME_INPUT);
        WebElement usernameField = findByAccessibilityId(TestIDs.LOGIN_USERNAME_INPUT);
        usernameField.clear();
        enterTextByAccessibilityId(TestIDs.LOGIN_USERNAME_INPUT, username);
        return this;
    }
    
    /**
     * Clear password field and enter new password
     * @param password Password to enter
     * @return LoginPage for method chaining
     */
    public LoginPage clearAndEnterPassword(String password) {
        logger.info("Clearing and entering password");
        waitForElementVisible(TestIDs.LOGIN_PASSWORD_INPUT);
        WebElement passwordField = findByAccessibilityId(TestIDs.LOGIN_PASSWORD_INPUT);
        passwordField.clear();
        enterTextByAccessibilityId(TestIDs.LOGIN_PASSWORD_INPUT, password);
        return this;
    }
    
    /**
     * Test invalid login credentials
     * @param invalidUsername Invalid username
     * @param invalidPassword Invalid password
     * @return LoginPage (should stay on login page after error)
     */
    public LoginPage testInvalidLogin(String invalidUsername, String invalidPassword) {
        logger.info("Testing invalid login with username: {}", invalidUsername);
        
        clearAndEnterUsername(invalidUsername);
        clearAndEnterPassword(invalidPassword);
        clickLoginButton();
        
        // Wait for and handle error dialog
        waitFor(2);
        handleLoginErrorDialogIfPresent();
        
        return this;
    }
    
    /**
     * Verify error dialog appears for invalid credentials
     * @param invalidUsername Invalid username
     * @param invalidPassword Invalid password
     * @return true if error dialog appeared as expected
     */
    public boolean verifyInvalidLoginShowsError(String invalidUsername, String invalidPassword) {
        logger.info("Verifying invalid login shows error dialog");
        
        clearAndEnterUsername(invalidUsername);
        clearAndEnterPassword(invalidPassword);
        clickLoginButton();
        
        // Wait for error dialog to appear
        waitFor(3);
        
        boolean errorDialogAppeared = isLoginErrorDialogDisplayed();
        
        if (errorDialogAppeared) {
            String errorMessage = getErrorDialogMessage();
            logger.info("Error dialog appeared with message: {}", errorMessage);
            dismissLoginErrorDialog();
        } else {
            logger.warn("Expected error dialog did not appear for invalid credentials");
        }
        
        return errorDialogAppeared;
    }
    
    /**
     * Check if username input is displayed
     * @return true if username input is visible
     */
    public boolean isUsernameInputDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.LOGIN_USERNAME_INPUT);
    }
    
    /**
     * Check if password input is displayed
     * @return true if password input is visible
     */
    public boolean isPasswordInputDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.LOGIN_PASSWORD_INPUT);
    }
    
    /**
     * Check if login button is displayed
     * @return true if login button is visible
     */
    public boolean isLoginButtonDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.LOGIN_SUBMIT_BUTTON);
    }
    
    /**
     * Get username text from input field
     * @return Username text
     */
    public String getUsernameText() {
        return getUsernameValue();
    }
    
    /**
     * Public method to tap at coordinates (for test access)
     * @param x X coordinate
     * @param y Y coordinate
     */
    public void tapAtCoordinates(int x, int y) {
        super.tapAtCoordinates(x, y);
    }
    
    /**
     * Public method to go back (for test access)
     */
    public void goBack() {
        super.goBack();
    }
}