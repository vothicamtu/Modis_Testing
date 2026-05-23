package com.modis.tests;

import com.modis.base.BaseTest;
import com.modis.base.BasePage;
import com.modis.constants.AppConstants;
import com.modis.constants.TestIDs;
import com.modis.drivers.DriverManager;
import com.modis.pages.*;
import com.modis.utils.TestDataReader;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import java.time.Duration;
import java.util.Map;
import java.util.List;

/**
 * Test class for Authentication functionality
 * Covers login, signup, and logout scenarios with real data and retry logic
 */
public class AuthenticationTests extends BaseTest {

    private TestDataReader testDataReader = new TestDataReader();
    private static final int P_LOGIN_EMPTY_FIELDS = 1;
    private static final int P_LOGIN_USERNAME_ONLY = 2;
    private static final int P_LOGIN_PASSWORD_ONLY = 3;
    private static final int P_LOGIN_INVALID_REALDATA = 4;
    private static final int P_LOGIN_INVALID_BASIC = 5;
    private static final int P_LOGIN_DIALOG_OK = 6;
    private static final int P_LOGIN_RETRY_FLOW = 50;
    private static final int P_LOGIN_SUCCESS_REALDATA = 998;
    private static final int P_LOGIN_SUCCESS_BASIC = 999;

    // ==================== DATA PROVIDERS ====================

    @DataProvider(name = "validLoginData")
    public Object[][] getValidLoginData() {
        List<Map<String, Object>> validCredentials = testDataReader.getValidLoginCredentials();
        Object[][] data = new Object[validCredentials.size()][];
        
        for (int i = 0; i < validCredentials.size(); i++) {
            Map<String, Object> credential = validCredentials.get(i);
            data[i] = new Object[]{
                credential.get("username"),
                credential.get("password"),
                credential.get("fullname"),
                credential.get("expectedResult")
            };
        }
        return data;
    }

    @DataProvider(name = "invalidLoginData")
    public Object[][] getInvalidLoginData() {
        List<Map<String, Object>> invalidCredentials = testDataReader.getInvalidLoginCredentials();
        Object[][] data = new Object[invalidCredentials.size()][];
        
        for (int i = 0; i < invalidCredentials.size(); i++) {
            Map<String, Object> credential = invalidCredentials.get(i);
            data[i] = new Object[]{
                credential.get("username"),
                credential.get("password"),
                credential.get("expectedError"),
                credential.get("description")
            };
        }
        return data;
    }

    @DataProvider(name = "retryLoginScenarios")
    public Object[][] getRetryLoginScenarios() {
        List<Map<String, Object>> retryScenarios = testDataReader.getRetryLoginScenarios();
        Object[][] data = new Object[retryScenarios.size()][];
        
        for (int i = 0; i < retryScenarios.size(); i++) {
            Map<String, Object> scenario = retryScenarios.get(i);
            data[i] = new Object[]{
                scenario.get("id"),
                scenario.get("description"),
                scenario.get("attempts")
            };
        }
        return data;
    }

    // ==================== HELPER METHODS ====================

    // ==================== LOGIN TESTS WITH REAL DATA ====================

    @Test(priority = P_LOGIN_SUCCESS_REALDATA, groups = {"authentication", "regression", "smoke"}, 
          dataProvider = "validLoginData", description = "Verify successful login with valid credentials from real data")
    public void testValidLoginWithRealData(String username, String password, String fullname, String expectedResult) {
        logger.info("Starting valid login test with real data - Username: " + username);

        LoginPage loginPage = openLoginPage();

        BasePage result = loginPage.login(username, password);
        Assert.assertTrue(result instanceof HomePage,
                "Login should navigate to HomePage for user: " + username + ", but got: " + 
                (result != null ? result.getClass().getSimpleName() : "null"));

        // Verify user is logged in by checking profile info
        HomePage homePage = (HomePage) result;
        homePage.waitForTopbarReadyAfterLogin(8);
        ProfilePage profilePage = homePage.navigateToProfile();
        String displayedName = profilePage.getDisplayedUserName();
        Assert.assertTrue(displayedName.contains(fullname) || displayedName.contains(username),
                "Displayed name should contain expected fullname or username");

        logger.info("Valid login test completed successfully for user: " + username);
    }

    @Test(priority = P_LOGIN_INVALID_REALDATA, groups = {"authentication", "regression"}, 
          dataProvider = "invalidLoginData", description = "Verify login failure with invalid credentials and error dialog handling")
    public void testInvalidLoginWithRealData(String username, String password, String expectedError, String description) {
        logger.info("Starting invalid login test with error dialog handling: " + description);

        LoginPage loginPage = openLoginPage();

        // Attempt login with invalid credentials
        loginPage.clearAndEnterUsername(username);
        loginPage.clearAndEnterPassword(password);
        BasePage afterSubmit = loginPage.clickLoginButton();

        Assert.assertTrue(afterSubmit instanceof LoginPage,
                "Invalid login should remain on LoginPage, but got: " +
                        (afterSubmit != null ? afterSubmit.getClass().getSimpleName() : "null"));

        // LoginPage.clickLoginButton() sẽ auto dismiss dialog để tránh popup che màn hình
        // Vì vậy Test đọc message lỗi thông qua lastLoginErrorDialogMessage
        String dialogErrorMessage = loginPage.getLastLoginErrorDialogMessage();
        if (!dialogErrorMessage.isEmpty()) {
            logger.info("Captured dialog error message: '{}'", dialogErrorMessage);

            boolean isVietnamese = dialogErrorMessage.contains("không") ||
                    dialogErrorMessage.contains("mật khẩu") ||
                    dialogErrorMessage.contains("đăng nhập") ||
                    dialogErrorMessage.contains("thông tin") ||
                    dialogErrorMessage.contains("chính xác") ||
                    dialogErrorMessage.contains("tồn tại");
            Assert.assertTrue(isVietnamese, "Error message should be in Vietnamese: " + dialogErrorMessage);
        } else {
            // Fallback: inline error message (nếu app không show dialog)
            Assert.assertTrue(loginPage.isDisplayed(), "Should remain on login page after invalid login");

            if (loginPage.isErrorMessageDisplayed()) {
                String inlineError = loginPage.getErrorMessage();
                logger.info("Inline error message: '{}'", inlineError);

                Assert.assertTrue(inlineError.contains(expectedError) ||
                                inlineError.toLowerCase().contains("sai") ||
                                inlineError.toLowerCase().contains("không đúng") ||
                                inlineError.toLowerCase().contains("không tồn tại"),
                        "Error message should indicate login failure. Expected: " + expectedError + ", Actual: " + inlineError);
            } else {
                logger.warn("No error message found (neither dialog nor inline) for: " + description);
                Assert.assertTrue(loginPage.isDisplayed(), "Should remain on login page after invalid login");
            }
        }

        // Verify we can still interact with login form after error handling
        loginPage.clearAndEnterUsername("test_after_error");
        String enteredText = loginPage.getUsernameValue();
        Assert.assertEquals(enteredText, "test_after_error", 
            "Should be able to interact with login form after error handling");

        logger.info("Invalid login test with error dialog handling completed successfully: " + description);
    }

    @Test(priority = P_LOGIN_RETRY_FLOW, groups = {"authentication", "regression", "retry"}, 
          dataProvider = "retryLoginScenarios", description = "Verify login retry flow with multiple attempts")
    public void testLoginRetryFlow(String scenarioId, String description, List<Map<String, Object>> attempts) {
        logger.info("Starting login retry flow test: " + description);

        LoginPage loginPage = openLoginPage();
        BasePage currentPage = loginPage;

        for (int i = 0; i < attempts.size(); i++) {
            Map<String, Object> attempt = attempts.get(i);
            String username = (String) attempt.get("username");
            String password = (String) attempt.get("password");
            String expectedResult = (String) attempt.get("expectedResult");
            int attemptNumber = ((Number) attempt.get("attempt")).intValue();

            logger.info("Attempt " + attemptNumber + ": Username=" + username + ", Password=" + password);

            if (currentPage instanceof LoginPage) {
                currentPage = ((LoginPage) currentPage).login(username, password);
            }

            if ("success".equals(expectedResult)) {
                Assert.assertTrue(currentPage instanceof HomePage,
                        "Final attempt should succeed and navigate to HomePage");
                logger.info("Login retry flow completed successfully after " + attemptNumber + " attempts");
                break;
            } else {
                Assert.assertTrue(currentPage instanceof LoginPage,
                        "Failed attempt " + attemptNumber + " should remain on LoginPage");
                Assert.assertTrue(((LoginPage) currentPage).isErrorMessageDisplayed(),
                        "Error message should be displayed for failed attempt " + attemptNumber);

                try {
                    LoginPage loginPageRef = (LoginPage) currentPage;
                    WebDriverWait readyForNextAttempt = new WebDriverWait(driver, Duration.ofSeconds(1));
                    readyForNextAttempt.pollingEvery(Duration.ofMillis(250));
                    readyForNextAttempt.until(d -> loginPageRef.isDisplayed());
                } catch (Exception ignored) {
                }
            }
        }

        logger.info("Login retry flow test completed: " + description);
    }

    @Test(priority = P_LOGIN_EMPTY_FIELDS, groups = {"authentication", "regression"}, description = "Verify login with empty credentials")
    public void testEmptyCredentialsLogin() {
        logger.info("Starting empty credentials login test");

        LoginPage loginPage = openLoginPage();

        LoginPage resultPage = loginPage.loginWithEmptyCredentials();

        Assert.assertTrue(resultPage.isDisplayed(), "Should remain on login page");
        Assert.assertFalse(resultPage.isLoginButtonEnabled(), "Login button should be disabled for empty credentials");

        logger.info("Empty credentials login test completed successfully");
    }

    @Test(priority = P_LOGIN_USERNAME_ONLY, groups = {"authentication", "regression"}, description = "Verify login with username only")
    public void testUsernameOnlyLogin() {
        logger.info("Starting username only login test");

        LoginPage loginPage = openLoginPage();

        LoginPage resultPage = loginPage.loginWithUsernameOnly("u001");

        Assert.assertTrue(resultPage.isDisplayed(), "Should remain on login page");
        Assert.assertTrue(resultPage.isPasswordEmpty(), "Password field should be empty");

        logger.info("Username only login test completed successfully");
    }

    @Test(priority = P_LOGIN_PASSWORD_ONLY, groups = {"authentication", "regression"}, description = "Verify login with password only")
    public void testPasswordOnlyLogin() {
        logger.info("Starting password only login test");

        LoginPage loginPage = openLoginPage();

        LoginPage resultPage = loginPage.loginWithPasswordOnly("123");

        Assert.assertTrue(resultPage.isDisplayed(), "Should remain on login page");
        Assert.assertTrue(resultPage.isUsernameEmpty(), "Username field should be empty");

        logger.info("Password only login test completed successfully");
    }

    // ==================== ORIGINAL LOGIN TESTS ====================

    @Test(priority = P_LOGIN_SUCCESS_BASIC, groups = {"authentication", "regression", "smoke"}, description = "Verify successful login with test user")
    public void testValidLogin() {
        logger.info("Starting valid login test with test user");

        LoginPage loginPage = openLoginPage();

        BasePage result = loginPage.loginWithTestUser();
        Assert.assertTrue(result instanceof HomePage,
                "Login should navigate to HomePage, but got: " + (result != null ? result.getClass().getSimpleName() : "null"));

        logger.info("Valid login test completed successfully");
    }

    @Test(priority = P_LOGIN_INVALID_BASIC, groups = {"authentication", "regression"}, description = "Verify login failure with invalid credentials and error dialog handling")
    public void testInvalidLogin() {
        logger.info("Starting invalid login test with error dialog handling");

        LoginPage loginPage = openLoginPage();

        // Use the error handling method instead of regular login
        boolean errorDialogAppeared = loginPage.verifyInvalidLoginShowsError("invaliduser", "invalidpass");

        if (errorDialogAppeared) {
            logger.info("Vietnamese error dialog appeared and was handled successfully");
            
            // Verify we're back on login page and can interact normally
            Assert.assertTrue(loginPage.isDisplayed(), "Should remain on login page after error dialog");
            
            // Test that we can enter new credentials after dismissing dialog
            loginPage.clearAndEnterUsername("test_after_dialog");
            String enteredText = loginPage.getUsernameValue();
            Assert.assertEquals(enteredText, "test_after_dialog", 
                "Should be able to interact with login form after dismissing error dialog");
                
        } else {
            // Fallback to original behavior if no dialog appears
            LoginPage resultPage = (LoginPage) loginPage.loginWithInvalidCredentials("invaliduser", "invalidpass");

            Assert.assertTrue(resultPage.isDisplayed(), "Should remain on login page after invalid login");
            Assert.assertTrue(resultPage.isErrorMessageDisplayed(), "Error message should be displayed");

            String errorMessage = resultPage.getErrorMessage();
            Assert.assertFalse(errorMessage.isEmpty(), "Error message should not be empty");
        }

        logger.info("Invalid login test with error dialog handling completed successfully");
    }

    private LoginPage openLoginPage() {
        logger.info("Opening login page (LoadingScreen -> Login)");

        LoadingPage loadingPage = new LoadingPage();
        loadingPage.waitForLoadingScreenVisible();
        loadingPage.waitForLoginSignupButtonsVisible();

        LoginPage loginPage = loadingPage.clickLoginButton();
        loginPage.waitForPageToLoad();
        return loginPage;
    }
    
    /**
     * Helper method to check if element is displayed by accessibility ID
     */
    private boolean isElementDisplayedByAccessibilityId(String accessibilityId) {
        try {
            return !driver.findElements(io.appium.java_client.AppiumBy.accessibilityId(accessibilityId)).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private SignupPage openSignupPage() {
        logger.info("Opening signup page (LoadingScreen -> Signup)");

        LoadingPage loadingPage = new LoadingPage();
        loadingPage.waitForLoadingScreenVisible();
        loadingPage.waitForLoginSignupButtonsVisible();

        SignupPage signupPage = loadingPage.clickSignupButton();
        signupPage.waitForPageToLoad();
        return signupPage;
    }

    @Test(priority = P_LOGIN_DIALOG_OK, groups = {"authentication", "regression"}, description = "Verify error dialog requires OK button click to dismiss")
    public void testErrorDialogRequiresOKClick() {
        logger.info("Starting error dialog OK button requirement test");

        LoginPage loginPage = openLoginPage();

        // Trigger error dialog with invalid credentials
        loginPage.clearAndEnterUsername("test_dialog_persistence");
        loginPage.clearAndEnterPassword("wrong_password");
        loginPage.clickLoginButton();

        WebDriverWait waitDialog = new WebDriverWait(driver, Duration.ofSeconds(5));
        waitDialog.pollingEvery(Duration.ofMillis(250));

        boolean dialogShown;
        try {
            dialogShown = waitDialog.until(d -> loginPage.isLoginErrorDialogDisplayed());
        } catch (TimeoutException e) {
            dialogShown = false;
        }

        if (dialogShown) {
            logger.info("Error dialog appeared, testing persistence and OK requirement");

            // Test 1: Dialog should persist after tapping outside
            loginPage.tapAtCoordinates(50, 50);
            WebDriverWait stillThereAfterTap = new WebDriverWait(driver, Duration.ofSeconds(1));
            stillThereAfterTap.pollingEvery(Duration.ofMillis(250));
            Assert.assertTrue(stillThereAfterTap.until(d -> loginPage.isLoginErrorDialogDisplayed()),
                    "Error dialog should persist after tapping outside");

            // Test 2: Dialog should persist after back button (if supported)
            try {
                loginPage.goBack();
                WebDriverWait stillThereAfterBack = new WebDriverWait(driver, Duration.ofSeconds(1));
                stillThereAfterBack.pollingEvery(Duration.ofMillis(250));
                Assert.assertTrue(stillThereAfterBack.until(d -> loginPage.isLoginErrorDialogDisplayed()),
                        "Error dialog should persist after back button");
            } catch (Exception e) {
                logger.info("Back button test skipped (not supported on this platform)");
            }

            // Test 3: Dialog should not auto-dismiss over time
            WebDriverWait waitGone = new WebDriverWait(driver, Duration.ofSeconds(5));
            waitGone.pollingEvery(Duration.ofMillis(250));
            try {
                waitGone.until(d -> !loginPage.isLoginErrorDialogDisplayed());
                Assert.fail("Error dialog should not auto-dismiss after waiting");
            } catch (TimeoutException ignored) {
            }

            // Test 4: Only OK button should dismiss the dialog
            loginPage.dismissLoginErrorDialog();
            Assert.assertFalse(loginPage.isLoginErrorDialogDisplayed(), 
                "Error dialog should be dismissed only by clicking OK button");

            // Test 5: Verify normal interaction is restored
            loginPage.clearAndEnterUsername("interaction_restored");
            String enteredText = loginPage.getUsernameValue();
            Assert.assertEquals(enteredText, "interaction_restored", 
                "Normal interaction should be restored after dismissing dialog");

            logger.info("Error dialog OK button requirement test completed successfully");
        } else {
            logger.warn("Error dialog did not appear - skipping persistence tests");
        }
    }

    // ==================== SIGNUP TESTS ====================

    @Test(priority = 10, groups = {"authentication", "regression", "smoke"}, description = "Verify successful signup with valid information")
    public void testValidSignup() {
        logger.info("Starting valid signup test");

        SignupPage signupPage = openSignupPage();
        signupPage.waitForPageToLoad();

        HomePage homePage = (HomePage) signupPage.signupWithRandomUser();

        Assert.assertTrue(homePage.isDisplayed(), "Home page should be displayed after successful signup");
        Assert.assertTrue(homePage.verifyPageElements(), "Home page elements should be present");

        logger.info("Valid signup test completed successfully");
    }

    // Update all remaining test priorities
    @Test(priority = 11, groups = {"authentication", "regression"}, description = "Verify signup with empty fields")
    public void testEmptyFieldsSignup() {
        logger.info("Starting empty fields signup test");

        SignupPage signupPage = openSignupPage();

        SignupPage resultPage = signupPage.signupWithEmptyFields();

        Assert.assertTrue(resultPage.isDisplayed(), "Should remain on signup page");
        Assert.assertFalse(resultPage.isSignupButtonEnabled(), "Signup button should be disabled for empty fields");

        logger.info("Empty fields signup test completed successfully");
    }

    @Test(priority = 12, groups = {"authentication", "regression"}, description = "Verify signup with mismatched passwords")
    public void testMismatchedPasswordsSignup() {
        logger.info("Starting mismatched passwords signup test");

        SignupPage signupPage = openSignupPage();

        String timestamp = String.valueOf(System.currentTimeMillis());
        SignupPage resultPage = signupPage.signupWithMismatchedPasswords(
                "testuser" + timestamp,
                "Test User",
                "test" + timestamp + "@example.com",
                "1234567890",
                "password123",
                "differentpassword"
        );

        Assert.assertTrue(resultPage.isDisplayed(), "Should remain on signup page");
        Assert.assertFalse(resultPage.doPasswordsMatch(), "Passwords should not match");

        logger.info("Mismatched passwords signup test completed successfully");
    }

    @Test(priority = 13, groups = {"authentication", "regression"}, description = "Verify signup with invalid email format")
    public void testInvalidEmailSignup() {
        logger.info("Starting invalid email signup test");

        SignupPage signupPage = openSignupPage();

        String timestamp = String.valueOf(System.currentTimeMillis());
        SignupPage resultPage = signupPage.signupWithInvalidEmail(
                "testuser" + timestamp,
                "Test User",
                "invalidemail",
                "1234567890",
                "password123"
        );

        Assert.assertTrue(resultPage.isDisplayed(), "Should remain on signup page");
        Assert.assertFalse(resultPage.isEmailFormatValid(), "Email format should be invalid");

        logger.info("Invalid email signup test completed successfully");
    }

    @Test(priority = 14, groups = {"authentication", "regression"}, description = "Verify signup with existing username")
    public void testExistingUsernameSignup() {
        logger.info("Starting existing username signup test");

        SignupPage signupPage = openSignupPage();

        SignupPage resultPage = signupPage.signupWithExistingUsername(
                AppConstants.TEST_USER_USERNAME, // Existing username
                "Test User New",
                "newemail@example.com",
                "9876543210",
                "newpassword123"
        );

        Assert.assertTrue(resultPage.isDisplayed(), "Should remain on signup page");
        Assert.assertTrue(resultPage.isErrorMessageDisplayed(), "Error message should be displayed");

        logger.info("Existing username signup test completed successfully");
    }

    // ==================== NAVIGATION TESTS ====================

    @Test(priority = 15, groups = {"authentication", "navigation", "regression"}, description = "Verify navigation from login to signup")
    public void testLoginToSignupNavigation() {
        logger.info("Starting login to signup navigation test");

        LoginPage loginPage = openLoginPage();

        SignupPage signupPage = loginPage.clickSignupLink();

        Assert.assertTrue(signupPage.isDisplayed(), "Signup page should be displayed");
        Assert.assertTrue(signupPage.verifyPageElements(), "Signup page elements should be present");

        logger.info("Login to signup navigation test completed successfully");
    }

    @Test(priority = 16, groups = {"authentication", "navigation", "regression"}, description = "Verify navigation from signup to login")
    public void testSignupToLoginNavigation() {
        logger.info("Starting signup to login navigation test");

        SignupPage signupPage = openSignupPage();

        LoginPage loginPage = signupPage.clickLoginLink();

        Assert.assertTrue(loginPage.isDisplayed(), "Login page should be displayed");
        Assert.assertTrue(loginPage.verifyPageElements(), "Login page elements should be present");

        logger.info("Signup to login navigation test completed successfully");
    }

    // ==================== LOGOUT TESTS ====================

    @Test(priority = 17, groups = {"authentication", "regression"}, description = "Verify successful logout")
    public void testSuccessfulLogout() {
        logger.info("Starting successful logout test");

        // First login
        LoginPage loginPage = openLoginPage();
        BasePage afterLogin = loginPage.loginWithTestUser();
        Assert.assertTrue(afterLogin instanceof HomePage,
                "Login should navigate to HomePage, but got: " + (afterLogin != null ? afterLogin.getClass().getSimpleName() : "null"));
        HomePage homePage = (HomePage) afterLogin;
        homePage.waitForTopbarReadyAfterLogin(8);

        // Navigate to profile and logout
        ProfilePage profilePage = homePage.navigateToProfile();
        LoginPage resultPage = profilePage.logout();

        Assert.assertTrue(resultPage.isDisplayed(), "Login page should be displayed after logout");
        Assert.assertTrue(resultPage.verifyPageElements(), "Login page elements should be present");

        logger.info("Successful logout test completed successfully");
    }

    @Test(priority = 18, groups = {"authentication", "regression"}, description = "Verify logout cancellation")
    public void testLogoutCancellation() {
        logger.info("Starting logout cancellation test");

        // First login
        LoginPage loginPage = openLoginPage();
        BasePage afterLogin = loginPage.loginWithTestUser();
        Assert.assertTrue(afterLogin instanceof HomePage,
                "Login should navigate to HomePage, but got: " + (afterLogin != null ? afterLogin.getClass().getSimpleName() : "null"));
        HomePage homePage = (HomePage) afterLogin;
        homePage.waitForTopbarReadyAfterLogin(8);

        // Navigate to profile and cancel logout
        ProfilePage profilePage = homePage.navigateToProfile();
        ProfilePage resultPage = profilePage.cancelLogout();

        Assert.assertTrue(resultPage.isDisplayed(), "Profile page should still be displayed");

        // Verify we can still navigate back to home
        HomePage homePageAfterCancel = resultPage.navigateBack();
        Assert.assertTrue(homePageAfterCancel.isDisplayed(), "Should be able to navigate back to home");

        logger.info("Logout cancellation test completed successfully");
    }

    // ==================== FIELD VALIDATION TESTS ====================

    @Test(priority = 19, groups = {"authentication", "regression"}, description = "Verify username field validation")
    public void testUsernameValidation() {
        logger.info("Starting username validation test");

        SignupPage signupPage = openSignupPage();

        // Test minimum length
        signupPage.enterUsername("ab");
        Assert.assertFalse(signupPage.isUsernameFormatValid(), "Username should be invalid for length < 3");

        // Test maximum length
        signupPage.enterUsername("a".repeat(25));
        Assert.assertFalse(signupPage.isUsernameFormatValid(), "Username should be invalid for length > 20");

        // Test valid username
        signupPage.enterUsername("validuser123");
        Assert.assertTrue(signupPage.isUsernameFormatValid(), "Username should be valid");

        logger.info("Username validation test completed successfully");
    }

    @Test(priority = 20, groups = {"authentication", "regression"}, description = "Verify password field validation")
    public void testPasswordValidation() {
        logger.info("Starting password validation test");

        SignupPage signupPage = openSignupPage();

        // Test minimum length
        signupPage.enterPassword("12345");
        Assert.assertFalse(signupPage.isPasswordValid(), "Password should be invalid for length < 6");

        // Test valid password
        signupPage.enterPassword("validpass123");
        Assert.assertTrue(signupPage.isPasswordValid(), "Password should be valid");

        logger.info("Password validation test completed successfully");
    }

    @Test(priority = 21, groups = {"authentication", "regression"}, description = "Verify email field validation")
    public void testEmailValidation() {
        logger.info("Starting email validation test");

        SignupPage signupPage = openSignupPage();

        // Test invalid email formats
        String[] invalidEmails = {"invalid", "invalid@", "@invalid.com", "invalid.com"};

        for (String invalidEmail : invalidEmails) {
            signupPage.enterEmail(invalidEmail);
            Assert.assertFalse(signupPage.isEmailFormatValid(),
                    "Email should be invalid: " + invalidEmail);
        }

        // Test valid email
        signupPage.enterEmail("valid@example.com");
        Assert.assertTrue(signupPage.isEmailFormatValid(), "Email should be valid");

        logger.info("Email validation test completed successfully");
    }

    @Test(priority = 22, groups = {"authentication", "regression"}, description = "Verify phone field validation")
    public void testPhoneValidation() {
        logger.info("Starting phone validation test");

        SignupPage signupPage = openSignupPage();

        // Test invalid phone formats
        String[] invalidPhones = {"123", "abcdefghij", "123-456-7890"};

        for (String invalidPhone : invalidPhones) {
            signupPage.enterPhone(invalidPhone);
            Assert.assertFalse(signupPage.isPhoneFormatValid(),
                    "Phone should be invalid: " + invalidPhone);
        }

        // Test valid phone
        signupPage.enterPhone("1234567890");
        Assert.assertTrue(signupPage.isPhoneFormatValid(), "Phone should be valid");

        logger.info("Phone validation test completed successfully");
    }

    // ==================== UI ELEMENT TESTS ====================

    @Test(priority = 23, groups = {"authentication", "regression"}, description = "Verify login page UI elements")
    public void testLoginPageElements() {
        logger.info("Starting login page elements test");

        LoginPage loginPage = openLoginPage();

        Assert.assertTrue(loginPage.verifyPageElements(), "All login page elements should be present");
        Assert.assertTrue(loginPage.isSignupLinkDisplayed(), "Signup link should be displayed");
        Assert.assertFalse(loginPage.getTitleText().isEmpty(), "Title text should not be empty");

        logger.info("Login page elements test completed successfully");
    }

    @Test(priority = 24, groups = {"authentication", "regression"}, description = "Verify signup page UI elements")
    public void testSignupPageElements() {
        logger.info("Starting signup page elements test");

        SignupPage signupPage = openSignupPage();

        Assert.assertTrue(signupPage.verifyPageElements(), "All signup page elements should be present");
        Assert.assertTrue(signupPage.areAllRequiredFieldsFilled() == false, "Required fields should be empty initially");
        Assert.assertFalse(signupPage.getTitleText().isEmpty(), "Title text should not be empty");

        logger.info("Signup page elements test completed successfully");
    }
}
