package com.modis.pages;

import com.modis.base.BasePage;
import com.modis.constants.TestIDs;
import com.modis.constants.AppConstants;
import com.modis.drivers.DriverManager;
import com.modis.utils.TestDataManager;
import com.modis.utils.UiDebugUtils;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.By;
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

    // Lưu thông tin lỗi lần đăng nhập gần nhất để Test có thể assert mà không cần giữ popup trên màn hình
    private String lastLoginErrorDialogMessage = "";

    private enum LoginSubmitOutcome {
        SUCCESS,
        FAILURE_DIALOG,
        FAILURE_NO_DIALOG,
        TIMEOUT
    }
    
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
     * Click nút Login và xử lý theo đúng trạng thái thực tế của app.
     *
     * Mục tiêu:
     * - Không được "assume" login thành công và đi tìm element của HomePage khi popup lỗi đang che màn hình
     * - Ưu tiên detect 2 nhánh rõ ràng: SUCCESS hoặc FAILURE (dialog / inline)
     * - Timeout ngắn để tránh treo 60s do findByAccessibilityId() bị gọi sai màn hình
     */
    public BasePage clickLoginButton() {
        logger.info("Clicking login button");
        lastLoginErrorDialogMessage = "";

        waitForElementClickable(TestIDs.LOGIN_SUBMIT_BUTTON);
        clickByAccessibilityId(TestIDs.LOGIN_SUBMIT_BUTTON);

        LoginSubmitOutcome outcome = waitForLoginSubmitOutcome(12);

        if (outcome == LoginSubmitOutcome.FAILURE_DIALOG) {
            // Popup lỗi đang che màn hình -> MUST dismiss trước, và KHÔNG được đi tìm element HomePage
            lastLoginErrorDialogMessage = getErrorDialogMessage();
            clickErrorDialogOk();
            logger.info("Login failed (dialog) - staying on login screen");
            return this;
        }

        if (outcome == LoginSubmitOutcome.SUCCESS) {
            logger.info("Login successful - navigated to home");
            HomePage homePage = new HomePage();
            homePage.waitForTopbarReadyAfterLogin(8);
            return homePage;
        }

        // FAILURE_NO_DIALOG hoặc TIMEOUT: không thấy Home, không thấy dialog -> coi như fail và giữ ở LoginPage
        logger.info("Login failed (no dialog/timeout) - staying on login screen");
        return this;
    }

    /**
     * Check nhanh HomePage đã hiển thị chưa (không gọi findByAccessibilityId để tránh timeout dài).
     */
    public boolean isHomePageDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.TOPBAR_AVATAR_BUTTON) ||
                isElementDisplayedByAccessibilityId(TestIDs.TOPBAR_CONTAINER) ||
                isElementDisplayedByAccessibilityId(TestIDs.FEED_SCREEN) ||
                isElementDisplayedByAccessibilityId(TestIDs.HOME_SCREEN);
    }

    private boolean isLoginScreenDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.LOGIN_USERNAME_INPUT) &&
                isElementDisplayedByAccessibilityId(TestIDs.LOGIN_SUBMIT_BUTTON);
    }

    /**
     * LoginSpinner là tín hiệu "đang xử lý". Nếu spinner còn hiện thì chưa nên kết luận fail/success.
     */
    private boolean isLoginProcessing() {
        try {
            java.util.List<WebElement> spinners = DriverManager.safelyFindElements(AppiumBy.accessibilityId(TestIDs.LOADING_SPINNER));
            if (spinners == null || spinners.isEmpty()) {
                spinners = DriverManager.safelyFindElements(AppiumBy.id(TestIDs.LOADING_SPINNER));
            }
            return spinners != null && !spinners.isEmpty() && spinners.get(0).isDisplayed();
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Chờ kết quả sau khi bấm Login theo 3 nhánh:
     * - SUCCESS: HomePage indicator xuất hiện
     * - FAILURE_DIALOG: popup/dialog lỗi xuất hiện (cần bấm OK)
     * - FAILURE_NO_DIALOG: form enable lại nhưng không có Home, không có dialog (ví dụ lỗi inline)
     */
    private LoginSubmitOutcome waitForLoginSubmitOutcome(int timeoutSeconds) {
        final long[] spinnerGoneSinceNs = new long[]{-1};
        final long startNs = System.nanoTime();

        try {
            LoginSubmitOutcome outcome = waitUtils.waitForCondition(d -> {
                if (isHomePageDisplayed()) {
                    return LoginSubmitOutcome.SUCCESS;
                }

                if (isLoginErrorDialogDisplayed()) {
                    return LoginSubmitOutcome.FAILURE_DIALOG;
                }

                boolean onLogin = isLoginScreenDisplayed();
                if (!onLogin) return null;

                java.util.List<WebElement> btns = DriverManager.safelyFindElements(AppiumBy.accessibilityId(TestIDs.LOGIN_SUBMIT_BUTTON));
                if ((btns == null || btns.isEmpty())) {
                    btns = DriverManager.safelyFindElements(AppiumBy.id(TestIDs.LOGIN_SUBMIT_BUTTON));
                }

                boolean enabled = false;
                if (btns != null && !btns.isEmpty()) {
                    try {
                        WebElement btn = btns.get(0);
                        enabled = btn != null && btn.isDisplayed() && btn.isEnabled();
                    } catch (Exception ignored) {
                        enabled = false;
                    }
                }

                if (!enabled) return null;

                long elapsedMs = java.time.Duration.ofNanos(System.nanoTime() - startNs).toMillis();
                if (elapsedMs < 1200) return null;

                boolean spinnerVisible = isLoginProcessing();
                if (spinnerVisible) {
                    spinnerGoneSinceNs[0] = -1;
                    return null;
                }

                if (spinnerGoneSinceNs[0] < 0) {
                    spinnerGoneSinceNs[0] = System.nanoTime();
                    return null;
                }

                long spinnerGoneMs = java.time.Duration.ofNanos(System.nanoTime() - spinnerGoneSinceNs[0]).toMillis();
                if (spinnerGoneMs < 600) return null;

                if (!isHomePageDisplayed() && !isLoginErrorDialogDisplayed()) {
                    return LoginSubmitOutcome.FAILURE_NO_DIALOG;
                }

                return null;
            }, timeoutSeconds);

            return outcome != null ? outcome : LoginSubmitOutcome.TIMEOUT;
        } catch (Exception e) {
            return LoginSubmitOutcome.TIMEOUT;
        }
    }

    /**
     * Getter để Test đọc message lỗi gần nhất (đặc biệt hữu ích khi clickLoginButton() auto dismiss dialog).
     */
    public String getLastLoginErrorDialogMessage() {
        return lastLoginErrorDialogMessage != null ? lastLoginErrorDialogMessage : "";
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
        Map<String, String> user = TestDataManager.getTestDataLoginUser();
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
     * SIMPLIFIED: Check if login error dialog is displayed - chỉ dùng accessibility ID chính xác
     */
    public boolean isLoginErrorDialogDisplayed() {
        logger.debug("Checking if login error dialog is displayed");
        
        try {
            if (!driver.findElements(AppiumBy.accessibilityId(TestIDs.LOGIN_ERROR_DIALOG)).isEmpty()) {
                return true;
            }
            if (!driver.findElements(AppiumBy.accessibilityId(TestIDs.ERROR_DIALOG)).isEmpty()) {
                return true;
            }
            if (!driver.findElements(AppiumBy.accessibilityId(TestIDs.ALERT_DIALOG)).isEmpty()) {
                return true;
            }

            return !driver.findElements(By.id("android:id/button1")).isEmpty();
        } catch (Exception e) {
            logger.debug("Error checking for login error dialog: " + e.getMessage());
            return false;
        }
    }

    public String getErrorDialogMessage() {
        try {
            if (isElementDisplayedByAccessibilityId(TestIDs.ERROR_DIALOG_MESSAGE)) {
                return getTextByAccessibilityId(TestIDs.ERROR_DIALOG_MESSAGE);
            }
        } catch (Exception ignored) {}

        try {
            if (isElementDisplayedByAccessibilityId(TestIDs.ERROR_DIALOG_TITLE)) {
                return getTextByAccessibilityId(TestIDs.ERROR_DIALOG_TITLE);
            }
        } catch (Exception ignored) {}

        return "";
    }
    
    // ==================== REMOVED COMPLEX METHODS ====================
    // Đã xóa: checkForErrorTextPrivate, getErrorDialogMessage, clickOkButton, waitForErrorDialogToDisappear
    // Lý do: Quá phức tạp, dùng nhiều xpath, gây chậm và không ổn định
    
    /**
     * SIMPLIFIED: Handle login error dialog if present
     */
    public boolean handleLoginErrorDialogIfPresent() {
        if (isLoginErrorDialogDisplayed()) {
            logger.info("Login error dialog detected - dismissing");
            dismissLoginErrorDialog();
            return true;
        }
        return false;
    }
    
    /**
     * SIMPLIFIED: Dismiss login error dialog - chỉ dùng accessibility ID
     */
    public LoginPage dismissLoginErrorDialog() {
        logger.info("Dismissing login error dialog");
        
        if (isLoginErrorDialogDisplayed()) {
            try {
                // Chỉ thử accessibility ID, không dùng xpath phức tạp
                if (isElementDisplayedByAccessibilityId(TestIDs.LOGIN_ERROR_OK_BUTTON)) {
                    clickByAccessibilityId(TestIDs.LOGIN_ERROR_OK_BUTTON);
                    logger.info("Dismissed using LOGIN_ERROR_OK_BUTTON");
                } else if (isElementDisplayedByAccessibilityId(TestIDs.ERROR_DIALOG_OK_BUTTON)) {
                    clickByAccessibilityId(TestIDs.ERROR_DIALOG_OK_BUTTON);
                    logger.info("Dismissed using ERROR_DIALOG_OK_BUTTON");
                } else if (isElementDisplayedByAccessibilityId(TestIDs.ALERT_OK_BUTTON)) {
                    clickByAccessibilityId(TestIDs.ALERT_OK_BUTTON);
                    logger.info("Dismissed using ALERT_OK_BUTTON");
                } else {
                    // Fallback: thử standard Android OK button
                    java.util.List<WebElement> okButtons = DriverManager.safelyFindElements(By.id("android:id/button1"));
                    if (okButtons != null && !okButtons.isEmpty() && okButtons.get(0).isDisplayed()) {
                        clickElement(okButtons.get(0));
                        logger.info("Dismissed using standard android OK button");
                    }
                }

                // Đợi popup biến mất nhanh (không dùng sleep dài để tránh chậm + flake)
                try {
                    waitUtils.waitForCondition(d -> !isLoginErrorDialogDisplayed(), 3);
                } catch (Exception ignored) {}
            } catch (Exception e) {
                logger.error("Failed to dismiss login error dialog: " + e.getMessage());
            }
        }
        
        return this;
    }

    /**
     * Helper mới theo yêu cầu: click OK của dialog lỗi.
     * - Nếu dialog không tồn tại thì không làm gì
     * - Nếu tồn tại thì dismiss bằng đúng nút OK (không tìm Home element)
     */
    public void clickErrorDialogOk() {
        dismissLoginErrorDialog();
    }
    
    /**
     * SIMPLIFIED: Login with error handling
     */
    public BasePage loginWithErrorHandling(String username, String password) {
        logger.info("Login with auto error handling - Username: {}", username);
        
        enterUsername(username);
        enterPassword(password);
        BasePage result = clickLoginButton(); // Auto-dismiss included
        
        return result;
    }
    
    /**
     * SIMPLIFIED: Verify invalid login shows error
     */
    public boolean verifyInvalidLoginShowsError(String username, String password) {
        logger.info("Verifying invalid login shows error - Username: {}", username);
        
        clearAllFields();
        enterUsername(username);
        enterPassword(password);
        clickByAccessibilityId(TestIDs.LOGIN_SUBMIT_BUTTON);

        // Wait for response
        try {
            waitUtils.waitForCondition(d ->
                    isLoginErrorDialogDisplayed() ||
                            isErrorMessageDisplayed() ||
                            isElementDisplayedByAccessibilityId(TestIDs.LOGIN_SUBMIT_BUTTON),
                    3);
        } catch (Exception ignored) {}

        // Check and handle error dialog
        return handleLoginErrorDialogIfPresent();
    }
    
    /**
     * Clear and enter username (helper method)
     * @param username Username to enter
     * @return LoginPage for method chaining
     */
    public LoginPage clearAndEnterUsername(String username) {
        clearUsername();
        enterUsername(username);
        return this;
    }
    
    /**
     * Clear and enter password (helper method)
     * @param password Password to enter
     * @return LoginPage for method chaining
     */
    public LoginPage clearAndEnterPassword(String password) {
        clearPassword();
        enterPassword(password);
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
