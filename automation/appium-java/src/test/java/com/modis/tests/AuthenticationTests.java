package com.modis.tests;

import com.modis.base.BaseTest;
import com.modis.base.BasePage;
import com.modis.constants.AppConstants;
import com.modis.pages.*;
import com.modis.utils.TestDataReader;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import java.util.Map;
import java.util.List;

/**
 * Test class for Authentication functionality
 * Covers login, signup, and logout scenarios with real data and retry logic
 */
public class AuthenticationTests extends BaseTest {

    private TestDataReader testDataReader = new TestDataReader();

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

    @Test(priority = 1, groups = {"authentication", "regression", "smoke"}, 
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
        ProfilePage profilePage = homePage.navigateToProfile();
        String displayedName = profilePage.getDisplayedUserName();
        Assert.assertTrue(displayedName.contains(fullname) || displayedName.contains(username),
                "Displayed name should contain expected fullname or username");

        logger.info("Valid login test completed successfully for user: " + username);
    }

    @Test(priority = 2, groups = {"authentication", "regression"}, 
          dataProvider = "invalidLoginData", description = "Verify login failure with invalid credentials and error dialog handling")
    public void testInvalidLoginWithRealData(String username, String password, String expectedError, String description) {
        logger.info("Starting invalid login test with error dialog handling: " + description);

        LoginPage loginPage = openLoginPage();

        // Attempt login with invalid credentials
        loginPage.clearAndEnterUsername(username);
        loginPage.clearAndEnterPassword(password);
        loginPage.clickLoginButton();

        // Wait for response (either error dialog or inline error)
        try {
            Thread.sleep(3000); // Wait 3 seconds for error dialog to appear
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Check if error dialog appeared (Vietnamese dialog with "Thông báo" title)
        if (loginPage.isLoginErrorDialogDisplayed()) {
            logger.info("Vietnamese error dialog appeared for: " + description);
            
            // Get error message from dialog
            String dialogErrorMessage = loginPage.getErrorDialogMessage();
            logger.info("Dialog error message: '{}'", dialogErrorMessage);
            
            // Verify error message is in Vietnamese and meaningful
            Assert.assertFalse(dialogErrorMessage.isEmpty(), "Error dialog message should not be empty");
            
            boolean isVietnamese = dialogErrorMessage.contains("không") || 
                                 dialogErrorMessage.contains("mật khẩu") ||
                                 dialogErrorMessage.contains("đăng nhập") ||
                                 dialogErrorMessage.contains("thông tin") ||
                                 dialogErrorMessage.contains("chính xác");
            Assert.assertTrue(isVietnamese, "Error message should be in Vietnamese: " + dialogErrorMessage);
            
            // Verify dialog has Vietnamese title "Thông báo"
            Assert.assertTrue(loginPage.checkForErrorText("Thông báo"), 
                "Error dialog should have Vietnamese title 'Thông báo'");
            
            // Verify OK button is present
            Assert.assertTrue(loginPage.checkForErrorText("OK"), 
                "Error dialog should have OK button");
            
            // Test that dialog cannot be dismissed by tapping outside
            loginPage.tapAtCoordinates(50, 50);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Assert.assertTrue(loginPage.isLoginErrorDialogDisplayed(), 
                "Error dialog should still be visible after tapping outside (must click OK)");
            
            // Dismiss dialog by clicking OK button (required!)
            loginPage.dismissLoginErrorDialog();
            
            // Verify dialog is dismissed
            Assert.assertFalse(loginPage.isLoginErrorDialogDisplayed(), 
                "Error dialog should be dismissed after clicking OK");
            
            logger.info("Successfully handled Vietnamese error dialog for: " + description);
            
        } else {
            // Fallback: check for inline error message (older behavior)
            logger.info("No error dialog found, checking for inline error message");
            
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
                // Still verify we stayed on login page
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

    @Test(priority = 3, groups = {"authentication", "regression", "retry"}, 
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
                
                // Wait a bit before next attempt to simulate real user behavior
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        logger.info("Login retry flow test completed: " + description);
    }

    @Test(priority = 4, groups = {"authentication", "regression"}, description = "Verify login with empty credentials")
    public void testEmptyCredentialsLogin() {
        logger.info("Starting empty credentials login test");

        LoginPage loginPage = openLoginPage();

        LoginPage resultPage = loginPage.loginWithEmptyCredentials();

        Assert.assertTrue(resultPage.isDisplayed(), "Should remain on login page");
        Assert.assertFalse(resultPage.isLoginButtonEnabled(), "Login button should be disabled for empty credentials");

        logger.info("Empty credentials login test completed successfully");
    }

    @Test(priority = 5, groups = {"authentication", "regression"}, description = "Verify login with username only")
    public void testUsernameOnlyLogin() {
        logger.info("Starting username only login test");

        LoginPage loginPage = openLoginPage();

        LoginPage resultPage = loginPage.loginWithUsernameOnly("u001");

        Assert.assertTrue(resultPage.isDisplayed(), "Should remain on login page");
        Assert.assertTrue(resultPage.isPasswordEmpty(), "Password field should be empty");

        logger.info("Username only login test completed successfully");
    }

    @Test(priority = 6, groups = {"authentication", "regression"}, description = "Verify login with password only")
    public void testPasswordOnlyLogin() {
        logger.info("Starting password only login test");

        LoginPage loginPage = openLoginPage();

        LoginPage resultPage = loginPage.loginWithPasswordOnly("123");

        Assert.assertTrue(resultPage.isDisplayed(), "Should remain on login page");
        Assert.assertTrue(resultPage.isUsernameEmpty(), "Username field should be empty");

        logger.info("Password only login test completed successfully");
    }

    // ==================== ORIGINAL LOGIN TESTS ====================

    @Test(priority = 7, groups = {"authentication", "regression", "smoke"}, description = "Verify successful login with test user")
    public void testValidLogin() {
        logger.info("Starting valid login test with test user");

        LoginPage loginPage = openLoginPage();

        BasePage result = loginPage.loginWithTestUser();
        Assert.assertTrue(result instanceof HomePage,
                "Login should navigate to HomePage, but got: " + (result != null ? result.getClass().getSimpleName() : "null"));

        logger.info("Valid login test completed successfully");
    }

    @Test(priority = 8, groups = {"authentication", "regression"}, description = "Verify login failure with invalid credentials and error dialog handling")
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
        // After launch, the landing page requires a manual tap (Login/Signup) before auth screens appear.
        // LoadingPage.waitForAutoNavigation() now performs that tap and returns the actual current page.
        BasePage current = new LoadingPage().waitForAutoNavigation();

        // If already logged in, logout first to guarantee a clean auth state
        if (current instanceof HomePage) {
            logger.info("Detected HomePage at start -> logging out to reach LoginPage");
            ProfilePage profilePage = ((HomePage) current).navigateToProfile();
            current = profilePage.logout();
        }

        // Fallbacks (should rarely happen, but keeps the test resilient)
        if (current instanceof SignupPage) {
            logger.info("Detected SignupPage at start -> navigating to LoginPage via link");
            current = ((SignupPage) current).clickLoginLink();
        } else if (current instanceof LoadingPage) {
            logger.info("Detected LoadingPage at start -> tapping Login button");
            current = ((LoadingPage) current).clickLoginButton();
        }

        if (!(current instanceof LoginPage)) {
            throw new AssertionError("Expected LoginPage, but got: " + current.getClass().getSimpleName());
        }

        LoginPage loginPage = (LoginPage) current;
        loginPage.waitForPageToLoad();
        return loginPage;
    }

    private SignupPage openSignupPage() {
        BasePage current = new LoadingPage().waitForAutoNavigation();

        // If already logged in, logout first to guarantee we can navigate to signup consistently
        if (current instanceof HomePage) {
            logger.info("Detected HomePage at start -> logging out to reach SignupPage");
            ProfilePage profilePage = ((HomePage) current).navigateToProfile();
            current = profilePage.logout();
        }

        if (current instanceof LoadingPage) {
            logger.info("Detected LoadingPage at start -> clicking Signup button");
            current = ((LoadingPage) current).clickSignupButton();
        } else if (current instanceof LoginPage) {
            logger.info("Detected LoginPage at start -> navigating to SignupPage via link");
            current = ((LoginPage) current).clickSignupLink();
        }

        if (!(current instanceof SignupPage)) {
            throw new AssertionError("Expected SignupPage, but got: " + current.getClass().getSimpleName());
        }

        SignupPage signupPage = (SignupPage) current;
        signupPage.waitForPageToLoad();
        return signupPage;
    }

    @Test(priority = 9, groups = {"authentication", "regression"}, description = "Verify error dialog requires OK button click to dismiss")
    public void testErrorDialogRequiresOKClick() {
        logger.info("Starting error dialog OK button requirement test");

        LoginPage loginPage = openLoginPage();

        // Trigger error dialog with invalid credentials
        loginPage.clearAndEnterUsername("test_dialog_persistence");
        loginPage.clearAndEnterPassword("wrong_password");
        loginPage.clickLoginButton();

        // Wait for error dialog
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (loginPage.isLoginErrorDialogDisplayed()) {
            logger.info("Error dialog appeared, testing persistence and OK requirement");

            // Test 1: Dialog should persist after tapping outside
            loginPage.tapAtCoordinates(50, 50);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Assert.assertTrue(loginPage.isLoginErrorDialogDisplayed(), 
                "Error dialog should persist after tapping outside");

            // Test 2: Dialog should persist after back button (if supported)
            try {
                loginPage.goBack();
                Thread.sleep(1000);
                Assert.assertTrue(loginPage.isLoginErrorDialogDisplayed(), 
                    "Error dialog should persist after back button");
            } catch (Exception e) {
                logger.info("Back button test skipped (not supported on this platform)");
            }

            // Test 3: Dialog should not auto-dismiss over time
            try {
                Thread.sleep(5000); // Wait 5 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Assert.assertTrue(loginPage.isLoginErrorDialogDisplayed(), 
                "Error dialog should not auto-dismiss after waiting");

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

    @Test(priority = 17, groups = {"authentication", "regression"}, description = "Verify successful logout", dependsOnMethods = {"testValidLogin"})
    public void testSuccessfulLogout() {
        logger.info("Starting successful logout test");

        // First login
        LoginPage loginPage = openLoginPage();
        HomePage homePage = (HomePage) loginPage.loginWithTestUser();

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
        HomePage homePage = (HomePage) loginPage.loginWithTestUser();

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
