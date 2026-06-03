package com.modis.pages;

import com.modis.base.BasePage;
import com.modis.constants.TestIDs;
import com.modis.drivers.DriverManager;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.Map;

public class SignupPage extends BasePage {

    private WebElement signupScreen;
    private WebElement titleText;
    private WebElement usernameInput;
    private WebElement fullnameInput;
    private WebElement emailInput;
    private WebElement phoneInput;
    private WebElement passwordInput;
    private WebElement confirmPasswordInput;
    private WebElement signupButton;
    private WebElement loginLink;
    private WebElement backButton;

    private String lastSignupErrorDialogMessage = "";

    public SignupPage enterUsername(String username) {
        logger.info("Entering signup username: {}", username);
        enterTextByAccessibilityId(TestIDs.SIGNUP_USERNAME_INPUT, username);
        return this;
    }

    public SignupPage enterFullname(String fullname) {
        logger.info("Entering signup fullname: {}", fullname);
        enterTextByAccessibilityId(TestIDs.SIGNUP_FULLNAME_INPUT, fullname);
        return this;
    }

    public SignupPage enterEmail(String email) {
        logger.info("Entering signup email: {}", email);
        enterTextByAccessibilityId(TestIDs.SIGNUP_EMAIL_INPUT, email);
        return this;
    }

    public SignupPage enterPhone(String phone) {
        logger.info("Entering signup phone: {}", phone);
        enterTextByAccessibilityId(TestIDs.SIGNUP_PHONE_INPUT, phone);
        return this;
    }

    public SignupPage enterPassword(String password) {
        logger.info("Entering signup password");
        enterTextByAccessibilityId(TestIDs.SIGNUP_PASSWORD_INPUT, password);
        return this;
    }

    public SignupPage enterConfirmPassword(String confirmPassword) {
        logger.info("Entering signup confirm password");

        enterTextByAccessibilityId(
                TestIDs.SIGNUP_CONFIRM_PASSWORD_INPUT,
                confirmPassword
        );

        return this;
    }

    public BasePage clickSignupButton() {

        logger.info("Clicking signup button");

        lastSignupErrorDialogMessage = "";

        hideKeyboard();

        waitFor(1);

        // SCROLL XUỐNG TỚI KHI THẤY NÚT
        WebElement signupTarget =
                ensureSignupButtonVisible();

        clickElement(signupTarget);

        if (waitForAuthDialog(8)) {

            lastSignupErrorDialogMessage =
                    getAuthDialogMessage();

            logger.info(
                    "Signup dialog captured: {}",
                    lastSignupErrorDialogMessage
            );

            return this;
        }

        if (isHomePageDisplayed()) {
            return new HomePage();
        }

        return this;
    }

    private WebElement ensureSignupButtonVisible() {
        WebElement visibleSignupButton =
                findVisibleSignupButton();

        if (visibleSignupButton != null) {
            return visibleSignupButton;
        }

        visibleSignupButton =
                scrollSignupButtonIntoViewWithGesture();

        if (visibleSignupButton != null) {
            return visibleSignupButton;
        }

        throw new RuntimeException(
                "Signup button is not visible on signup screen"
        );
    }

    private WebElement scrollSignupButtonIntoViewWithGesture() {
        AppiumDriver currentDriver =
                DriverManager.getDriver();

        if (currentDriver == null) {
            return null;
        }

        Dimension size;

        try {
            size = currentDriver.manage().window().getSize();
        } catch (Exception e) {
            logger.warn(
                    "Could not read screen size before signup scroll: {}",
                    e.getMessage()
            );

            return null;
        }

        int centerX = size.width / 2;
        int startY = (int) (size.height * 0.78);
        int endY = (int) (size.height * 0.32);

        for (int attempt = 1; attempt <= 4; attempt++) {
            try {
                Map<String, Object> dragParams =
                        new HashMap<>();

                dragParams.put("startX", centerX);
                dragParams.put("startY", startY);
                dragParams.put("endX", centerX);
                dragParams.put("endY", endY);
                dragParams.put("speed", 700);

                currentDriver.executeScript(
                        "mobile: dragGesture",
                        dragParams
                );

                waitFor(1);

                WebElement visibleSignupButton =
                        findVisibleSignupButton();

                if (visibleSignupButton != null) {
                    logger.info(
                            "Signup button visible after dragGesture attempt {}",
                            attempt
                    );

                    return visibleSignupButton;
                }
            } catch (Exception e) {
                logger.warn(
                        "Signup dragGesture attempt {} failed: {}",
                        attempt,
                        e.getMessage()
                );
            }
        }

        return null;
    }

    private WebElement findVisibleSignupButton() {
        AppiumDriver currentDriver =
                DriverManager.getDriver();

        if (currentDriver == null) {
            return null;
        }

        try {
            for (WebElement element : currentDriver.findElements(AppiumBy.accessibilityId(TestIDs.SIGNUP_SUBMIT_BUTTON))) {
                if (element != null && element.isDisplayed()) {
                    return element;
                }
            }
        } catch (Exception e) {
            logger.debug(
                    "Signup button accessibility lookup failed: {}",
                    e.getMessage()
            );
        }

        try {
            for (WebElement element : currentDriver.findElements(AppiumBy.id(TestIDs.SIGNUP_SUBMIT_BUTTON))) {
                if (element != null && element.isDisplayed()) {
                    return element;
                }
            }
        } catch (Exception e) {
            logger.debug(
                    "Signup button resource-id lookup failed: {}",
                    e.getMessage()
            );
        }

        return null;
    }

    public boolean isHomePageDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.TOPBAR_AVATAR_BUTTON)
                || isElementDisplayedByAccessibilityId(TestIDs.TOPBAR_CONTAINER)
                || isElementDisplayedByAccessibilityId(TestIDs.FEED_SCREEN)
                || isElementDisplayedByAccessibilityId(TestIDs.HOME_SCREEN);
    }

    public boolean isSignupButtonEnabled() {
        return isElementEnabledByAccessibilityId(TestIDs.SIGNUP_SUBMIT_BUTTON);
    }

    public String getLastSignupErrorDialogMessage() {
        return lastSignupErrorDialogMessage;
    }

    public boolean isAuthDialogVisible() {
        return super.isAuthDialogVisible();
    }

    public String getAuthDialogMessage() {
        return super.getAuthDialogMessage();
    }

    public boolean isSignupErrorDialogDisplayed() {
        return isAuthDialogVisible();
    }

    public void dismissSignupErrorDialog() {
        if (!isSignupErrorDialogDisplayed()) {
            return;
        }

        dismissAuthDialog();
        waitForAuthDialogDisappear();
    }

    public void clickErrorDialogOk() {
        dismissSignupErrorDialog();
    }

    public void waitForAuthDialogDisappear() {
        super.waitForAuthDialogDisappear();
    }

    public String getUsernameValue() {
        return getTextByAccessibilityId(TestIDs.SIGNUP_USERNAME_INPUT);
    }

    public String getFullnameValue() {
        return getTextByAccessibilityId(TestIDs.SIGNUP_FULLNAME_INPUT);
    }

    public String getEmailValue() {
        return getTextByAccessibilityId(TestIDs.SIGNUP_EMAIL_INPUT);
    }

    public String getPhoneValue() {
        return getTextByAccessibilityId(TestIDs.SIGNUP_PHONE_INPUT);
    }

    public boolean areAllRequiredFieldsFilled() {
        return !getUsernameValue().trim().isEmpty()
                && !getFullnameValue().trim().isEmpty()
                && !getEmailValue().trim().isEmpty()
                && !getPhoneValue().trim().isEmpty()
                && !getTextByAccessibilityId(TestIDs.SIGNUP_PASSWORD_INPUT).trim().isEmpty()
                && !getTextByAccessibilityId(TestIDs.SIGNUP_CONFIRM_PASSWORD_INPUT).trim().isEmpty();
    }

    public boolean doPasswordsMatch() {
        String password = getTextByAccessibilityId(TestIDs.SIGNUP_PASSWORD_INPUT);
        String confirmPassword = getTextByAccessibilityId(TestIDs.SIGNUP_CONFIRM_PASSWORD_INPUT);
        return password.equals(confirmPassword);
    }

    public boolean isEmailFormatValid() {
        return getEmailValue().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    public boolean isPhoneFormatValid() {
        return getPhoneValue().matches("^[0-9]{10,15}$");
    }

    public boolean isUsernameFormatValid() {
        String username = getUsernameValue();
        return username.length() >= 3 && username.length() <= 20 && username.matches("^[a-zA-Z0-9_]+$");
    }

    public boolean isPasswordValid() {
        String password = getTextByAccessibilityId(TestIDs.SIGNUP_PASSWORD_INPUT);
        return password.length() >= 6 && password.length() <= 50;
    }

    public SignupPage clearAllFields() {
        clearFieldByAccessibilityId(TestIDs.SIGNUP_USERNAME_INPUT);
        clearFieldByAccessibilityId(TestIDs.SIGNUP_FULLNAME_INPUT);
        clearFieldByAccessibilityId(TestIDs.SIGNUP_EMAIL_INPUT);
        clearFieldByAccessibilityId(TestIDs.SIGNUP_PHONE_INPUT);
        clearFieldByAccessibilityId(TestIDs.SIGNUP_PASSWORD_INPUT);
        clearFieldByAccessibilityId(TestIDs.SIGNUP_CONFIRM_PASSWORD_INPUT);
        return this;
    }

    public SignupPage signupWithEmptyFields() {
        clearAllFields();
        return this;
    }

    public SignupPage signupWithMismatchedPasswords(String username, String fullname, String email, String phone, String password, String confirmPassword) {
        enterUsername(username);
        enterFullname(fullname);
        enterEmail(email);
        enterPhone(phone);
        enterPassword(password);
        enterConfirmPassword(confirmPassword);
        return this;
    }

    public SignupPage signupWithInvalidEmail(String username, String fullname, String invalidEmail, String phone, String password) {
        enterUsername(username);
        enterFullname(fullname);
        enterEmail(invalidEmail);
        enterPhone(phone);
        enterPassword(password);
        enterConfirmPassword(password);
        return this;
    }

    public BasePage signup(String username, String fullname, String email, String phone, String password, String confirmPassword) {
        enterUsername(username);
        enterFullname(fullname);
        enterEmail(email);
        enterPhone(phone);
        enterPassword(password);
        enterConfirmPassword(confirmPassword);
        return clickSignupButton();
    }

    public BasePage signupWithTestUser() {
        return signup(
                "testuser",
                "Test User",
                "testuser@example.com",
                "1234567890",
                "testpass123",
                "testpass123");
    }

    public BasePage signupWithRandomUser() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        return signup(
                "testuser" + timestamp,
                "Test User " + timestamp,
                "testuser" + timestamp + "@example.com",
                "123456789" + timestamp.substring(timestamp.length() - 1),
                "testpass123",
                "testpass123");
    }

    public boolean isErrorMessageDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.VALIDATION_ERROR_TEXT);
    }

    public String getErrorMessage() {
        return getTextByAccessibilityId(TestIDs.VALIDATION_ERROR_TEXT);
    }

    public LoginPage clickLoginLink() {
        waitForElementClickable(TestIDs.SIGNUP_LOGIN_LINK);
        clickByAccessibilityId(TestIDs.SIGNUP_LOGIN_LINK);
        return new LoginPage();
    }

    public BasePage clickBackButton() {
        waitForElementClickable(TestIDs.SIGNUP_BACK_BUTTON);
        clickByAccessibilityId(TestIDs.SIGNUP_BACK_BUTTON);
        if (isElementDisplayedByAccessibilityId(TestIDs.LOGIN_SCREEN)) {
            return new LoginPage();
        }
        return new LoadingPage();
    }

    public SignupPage waitForPageToLoad() {
        waitForElementVisible(TestIDs.SIGNUP_SCREEN);
        waitForElementVisible(TestIDs.SIGNUP_USERNAME_INPUT);

        // KHÔNG wait submit button vì nằm ngoài viewport
        return this;
    }

    public boolean verifyPageElements() {
        return isElementDisplayedByAccessibilityId(TestIDs.SIGNUP_SCREEN)
                && isElementDisplayedByAccessibilityId(TestIDs.SIGNUP_USERNAME_INPUT)
                && isElementDisplayedByAccessibilityId(TestIDs.SIGNUP_FULLNAME_INPUT)
                && isElementDisplayedByAccessibilityId(TestIDs.SIGNUP_EMAIL_INPUT)
                && isElementDisplayedByAccessibilityId(TestIDs.SIGNUP_PHONE_INPUT)
                && isElementDisplayedByAccessibilityId(TestIDs.SIGNUP_PASSWORD_INPUT)
                && isElementDisplayedByAccessibilityId(TestIDs.SIGNUP_CONFIRM_PASSWORD_INPUT);
    }

    @Override
    public boolean isDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.SIGNUP_SCREEN)
                || isElementDisplayedByAccessibilityId(TestIDs.SIGNUP_USERNAME_INPUT);
    }

    @Override
    public String getPageTitle() {
        return "Signup Screen";
    }

    private void clearFieldByAccessibilityId(String accessibilityId) {
        waitForElementVisible(accessibilityId);
        WebElement element = findByAccessibilityId(accessibilityId);
        element.clear();
    }
}
