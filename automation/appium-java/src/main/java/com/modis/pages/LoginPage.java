package com.modis.pages;

import com.modis.base.BasePage;
import com.modis.constants.TestIDs;
import org.openqa.selenium.WebElement;

public class LoginPage extends BasePage {

    private WebElement usernameInput;
    private WebElement passwordInput;
    private WebElement loginButton;
    private WebElement titleText;
    private WebElement signupLink;
    private WebElement forgotPasswordLink;
    private WebElement loadingSpinner;

    private String lastLoginErrorDialogMessage = "";

    public LoginPage enterUsername(String username) {
        logger.info("Entering username: {}", username);
        enterTextByAccessibilityId(TestIDs.LOGIN_USERNAME_INPUT, username);
        return this;
    }

    public LoginPage enterPassword(String password) {
        logger.info("Entering password");
        enterTextByAccessibilityId(TestIDs.LOGIN_PASSWORD_INPUT, password);
        return this;
    }

    public BasePage clickLoginButton() {
        logger.info("Clicking login button");
        lastLoginErrorDialogMessage = "";

        if (!isLoginButtonEnabled()) {
            logger.info("Login button is disabled; stopping submit without clicking");
            return this;
        }

        hideKeyboard();

        // WAIT CHO RN UPDATE UI
        waitFor(1);

        // ENSURE BUTTON VISIBLE + CLICKABLE
        ensureElementScrolledIntoViewAndClickable(TestIDs.LOGIN_SUBMIT_BUTTON);

        // RETRY CLICK
        for (int i = 0; i < 3; i++) {
            try {
                clickByAccessibilityId(TestIDs.LOGIN_SUBMIT_BUTTON);
                break;
            } catch (Exception e) {
                logger.warn("Retry click login button: {}", i + 1);
                waitFor(1);
            }
        }

        if (waitForAuthDialog(8)) {
            lastLoginErrorDialogMessage = getAuthDialogMessage();
            logger.info("Login dialog captured: {}", lastLoginErrorDialogMessage);
            return this;
        }

        if (isHomePageDisplayed()) {
            HomePage homePage = new HomePage();
            homePage.waitForTopbarReadyAfterLogin(8);
            return homePage;
        }

        return this;
    }

    public boolean isHomePageDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.TOPBAR_AVATAR_BUTTON)
                || isElementDisplayedByAccessibilityId(TestIDs.TOPBAR_CONTAINER)
                || isElementDisplayedByAccessibilityId(TestIDs.FEED_SCREEN)
                || isElementDisplayedByAccessibilityId(TestIDs.HOME_SCREEN);
    }

    public String getLastLoginErrorDialogMessage() {
        return lastLoginErrorDialogMessage;
    }

    public boolean isAuthDialogVisible() {
        return super.isAuthDialogVisible();
    }

    public String getAuthDialogMessage() {
        return super.getAuthDialogMessage();
    }

    public boolean isLoginErrorDialogDisplayed() {
        return isAuthDialogVisible();
    }

    public void dismissLoginErrorDialog() {
        if (!isLoginErrorDialogDisplayed()) {
            return;
        }

        dismissAuthDialog();
        waitForAuthDialogDisappear();
    }

    public void clickErrorDialogOk() {
        dismissLoginErrorDialog();
    }

    public void waitForAuthDialogDisappear() {
        super.waitForAuthDialogDisappear();
    }

    public boolean isLoginButtonEnabled() {
        return isElementEnabledByAccessibilityId(TestIDs.LOGIN_SUBMIT_BUTTON);
    }

    public String getUsernameValue() {
        return getTextByAccessibilityId(TestIDs.LOGIN_USERNAME_INPUT);
    }

    public String getPasswordValue() {
        return getTextByAccessibilityId(TestIDs.LOGIN_PASSWORD_INPUT);
    }

    public boolean isUsernameEmpty() {
        return getUsernameValue().trim().isEmpty();
    }

    public boolean isPasswordEmpty() {
        return getPasswordValue().trim().isEmpty();
    }

    public String getTitleText() {
        return getTextByAccessibilityId(TestIDs.LOGIN_TITLE_TEXT);
    }

    public boolean isSignupLinkDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.LOGIN_SIGNUP_LINK);
    }

    public boolean isForgotPasswordLinkDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.LOGIN_FORGOT_PASSWORD);
    }

    public boolean isLoading() {
        return isElementDisplayedByAccessibilityId(TestIDs.LOADING_SPINNER);
    }

    public LoginPage clearUsername() {
        clearFieldByAccessibilityId(TestIDs.LOGIN_USERNAME_INPUT);
        return this;
    }

    public LoginPage clearPassword() {
        clearFieldByAccessibilityId(TestIDs.LOGIN_PASSWORD_INPUT);
        return this;
    }

    public LoginPage clearAllFields() {
        clearUsername();
        clearPassword();
        return this;
    }

    public BasePage login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        return clickLoginButton();
    }

    public BasePage loginWithTestUser() {
        return login("testuser", "testpass123");
    }

    public LoginPage loginWithEmptyCredentials() {
        clearAllFields();
        return this;
    }

    public LoginPage loginWithUsernameOnly(String username) {
        clearAllFields();
        enterUsername(username);
        return this;
    }

    public LoginPage loginWithPasswordOnly(String password) {
        clearAllFields();
        enterPassword(password);
        return this;
    }

    public LoginPage clearAndEnterUsername(String username) {
        clearUsername();
        return enterUsername(username);
    }

    public LoginPage clearAndEnterPassword(String password) {
        clearPassword();
        return enterPassword(password);
    }

    public boolean isErrorMessageDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.VALIDATION_ERROR_TEXT);
    }

    public String getErrorMessage() {
        return getTextByAccessibilityId(TestIDs.VALIDATION_ERROR_TEXT);
    }

    public SignupPage clickSignupLink() {
        waitForElementClickable(TestIDs.LOGIN_SIGNUP_LINK);
        clickByAccessibilityId(TestIDs.LOGIN_SIGNUP_LINK);
        return new SignupPage();
    }

    public LoginPage clickForgotPasswordLink() {
        waitForElementClickable(TestIDs.LOGIN_FORGOT_PASSWORD);
        clickByAccessibilityId(TestIDs.LOGIN_FORGOT_PASSWORD);
        return this;
    }

    public LoginPage waitForPageToLoad() {
        waitForElementVisible(TestIDs.LOGIN_USERNAME_INPUT);
        waitForElementVisible(TestIDs.LOGIN_PASSWORD_INPUT);
        waitForElementVisible(TestIDs.LOGIN_SUBMIT_BUTTON);
        return this;
    }

    public boolean verifyPageElements() {
        return isElementDisplayedByAccessibilityId(TestIDs.LOGIN_USERNAME_INPUT)
                && isElementDisplayedByAccessibilityId(TestIDs.LOGIN_PASSWORD_INPUT)
                && isElementDisplayedByAccessibilityId(TestIDs.LOGIN_SUBMIT_BUTTON)
                && isElementDisplayedByAccessibilityId(TestIDs.LOGIN_SIGNUP_LINK);
    }

    @Override
    public boolean isDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.LOGIN_USERNAME_INPUT)
                || isElementDisplayedByAccessibilityId(TestIDs.LOGIN_SUBMIT_BUTTON);
    }

    @Override
    public String getPageTitle() {
        return "Login Screen";
    }

    private void clearFieldByAccessibilityId(String accessibilityId) {
        waitForElementVisible(accessibilityId);
        WebElement element = findByAccessibilityId(accessibilityId);
        element.clear();
    }
}
