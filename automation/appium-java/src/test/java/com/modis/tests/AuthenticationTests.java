package com.modis.tests;

import com.modis.base.BaseTest;
import com.modis.base.BasePage;
import com.modis.constants.AppConstants;
import com.modis.pages.*;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for Authentication functionality
 * Covers login, signup, and logout scenarios
 */
public class AuthenticationTests extends BaseTest {

    // ==================== LOGIN TESTS ====================

    private LoginPage openLoginPage() {
        BasePage current = new LoadingPage().waitForAutoNavigation();

        // If already logged in, logout first to guarantee a clean auth state
        if (current instanceof HomePage) {
            logger.info("Detected HomePage at start -> logging out to reach LoginPage");
            ProfilePage profilePage = ((HomePage) current).navigateToProfile();
            current = profilePage.logout();
        }

        if (current instanceof LoadingPage) {
            logger.info("Detected LoadingPage at start -> clicking Login button");
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

    @Test(priority = 1, groups = {"authentication", "regression", "smoke"}, description = "Verify successful login with valid credentials")
    public void testValidLogin() {
        logger.info("Starting valid login test");

        LoginPage loginPage = openLoginPage();

        // 3. Login bằng PageObject (đã có multi-strategy theo testID/accessibilityId)
        BasePage result = loginPage.loginWithTestUser();
        Assert.assertTrue(result instanceof HomePage,
                "Login should navigate to HomePage, but got: " + (result != null ? result.getClass().getSimpleName() : "null"));

        logger.info("Valid login test completed successfully");
    }

    @Test(priority = 2, groups = {"authentication", "regression"}, description = "Verify login failure with invalid credentials")
    public void testInvalidLogin() {
        logger.info("Starting invalid login test");

        LoginPage loginPage = openLoginPage();

        LoginPage resultPage = (LoginPage) loginPage.loginWithInvalidCredentials(
                "invaliduser", "invalidpass"
        );

        Assert.assertTrue(resultPage.isDisplayed(), "Should remain on login page after invalid login");
        Assert.assertTrue(resultPage.isErrorMessageDisplayed(), "Error message should be displayed");

        String errorMessage = resultPage.getErrorMessage();
        Assert.assertFalse(errorMessage.isEmpty(), "Error message should not be empty");

        logger.info("Invalid login test completed successfully");
    }

    @Test(priority = 3, groups = {"authentication", "regression"}, description = "Verify login with empty credentials")
    public void testEmptyCredentialsLogin() {
        logger.info("Starting empty credentials login test");

        LoginPage loginPage = openLoginPage();

        LoginPage resultPage = loginPage.loginWithEmptyCredentials();

        Assert.assertTrue(resultPage.isDisplayed(), "Should remain on login page");
        Assert.assertFalse(resultPage.isLoginButtonEnabled(), "Login button should be disabled for empty credentials");

        logger.info("Empty credentials login test completed successfully");
    }

    @Test(priority = 4, groups = {"authentication", "regression"}, description = "Verify login with username only")
    public void testUsernameOnlyLogin() {
        logger.info("Starting username only login test");

        LoginPage loginPage = openLoginPage();

        LoginPage resultPage = loginPage.loginWithUsernameOnly(AppConstants.TEST_USER_USERNAME);

        Assert.assertTrue(resultPage.isDisplayed(), "Should remain on login page");
        Assert.assertTrue(resultPage.isPasswordEmpty(), "Password field should be empty");

        logger.info("Username only login test completed successfully");
    }

    @Test(priority = 5, groups = {"authentication", "regression"}, description = "Verify login with password only")
    public void testPasswordOnlyLogin() {
        logger.info("Starting password only login test");

        LoginPage loginPage = openLoginPage();

        LoginPage resultPage = loginPage.loginWithPasswordOnly(AppConstants.TEST_USER_PASSWORD);

        Assert.assertTrue(resultPage.isDisplayed(), "Should remain on login page");
        Assert.assertTrue(resultPage.isUsernameEmpty(), "Username field should be empty");

        logger.info("Password only login test completed successfully");
    }

    // ==================== SIGNUP TESTS ====================

    @Test(priority = 6, groups = {"authentication", "regression", "smoke"}, description = "Verify successful signup with valid information")
    public void testValidSignup() {
        logger.info("Starting valid signup test");

        SignupPage signupPage = openSignupPage();
        signupPage.waitForPageToLoad();

        HomePage homePage = (HomePage) signupPage.signupWithRandomUser();

        Assert.assertTrue(homePage.isDisplayed(), "Home page should be displayed after successful signup");
        Assert.assertTrue(homePage.verifyPageElements(), "Home page elements should be present");

        logger.info("Valid signup test completed successfully");
    }

    @Test(priority = 7, groups = {"authentication", "regression"}, description = "Verify signup with empty fields")
    public void testEmptyFieldsSignup() {
        logger.info("Starting empty fields signup test");

        SignupPage signupPage = openSignupPage();

        SignupPage resultPage = signupPage.signupWithEmptyFields();

        Assert.assertTrue(resultPage.isDisplayed(), "Should remain on signup page");
        Assert.assertFalse(resultPage.isSignupButtonEnabled(), "Signup button should be disabled for empty fields");

        logger.info("Empty fields signup test completed successfully");
    }

    @Test(priority = 8, groups = {"authentication", "regression"}, description = "Verify signup with mismatched passwords")
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

    @Test(priority = 9, groups = {"authentication", "regression"}, description = "Verify signup with invalid email format")
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

    @Test(priority = 10, groups = {"authentication", "regression"}, description = "Verify signup with existing username")
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

    @Test(priority = 11, groups = {"authentication", "navigation", "regression"}, description = "Verify navigation from login to signup")
    public void testLoginToSignupNavigation() {
        logger.info("Starting login to signup navigation test");

        LoginPage loginPage = openLoginPage();

        SignupPage signupPage = loginPage.clickSignupLink();

        Assert.assertTrue(signupPage.isDisplayed(), "Signup page should be displayed");
        Assert.assertTrue(signupPage.verifyPageElements(), "Signup page elements should be present");

        logger.info("Login to signup navigation test completed successfully");
    }

    @Test(priority = 12, groups = {"authentication", "navigation", "regression"}, description = "Verify navigation from signup to login")
    public void testSignupToLoginNavigation() {
        logger.info("Starting signup to login navigation test");

        SignupPage signupPage = openSignupPage();

        LoginPage loginPage = signupPage.clickLoginLink();

        Assert.assertTrue(loginPage.isDisplayed(), "Login page should be displayed");
        Assert.assertTrue(loginPage.verifyPageElements(), "Login page elements should be present");

        logger.info("Signup to login navigation test completed successfully");
    }

    // ==================== LOGOUT TESTS ====================

    @Test(priority = 13, groups = {"authentication", "regression"}, description = "Verify successful logout", dependsOnMethods = {"testValidLogin"})
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

    @Test(priority = 14, groups = {"authentication", "regression"}, description = "Verify logout cancellation")
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

    @Test(priority = 15, groups = {"authentication", "regression"}, description = "Verify username field validation")
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

    @Test(priority = 16, groups = {"authentication", "regression"}, description = "Verify password field validation")
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

    @Test(priority = 17, groups = {"authentication", "regression"}, description = "Verify email field validation")
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

    @Test(priority = 18, groups = {"authentication", "regression"}, description = "Verify phone field validation")
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

    @Test(priority = 19, groups = {"authentication", "regression"}, description = "Verify login page UI elements")
    public void testLoginPageElements() {
        logger.info("Starting login page elements test");

        LoginPage loginPage = openLoginPage();

        Assert.assertTrue(loginPage.verifyPageElements(), "All login page elements should be present");
        Assert.assertTrue(loginPage.isSignupLinkDisplayed(), "Signup link should be displayed");
        Assert.assertFalse(loginPage.getTitleText().isEmpty(), "Title text should not be empty");

        logger.info("Login page elements test completed successfully");
    }

    @Test(priority = 20, groups = {"authentication", "regression"}, description = "Verify signup page UI elements")
    public void testSignupPageElements() {
        logger.info("Starting signup page elements test");

        SignupPage signupPage = openSignupPage();

        Assert.assertTrue(signupPage.verifyPageElements(), "All signup page elements should be present");
        Assert.assertTrue(signupPage.areAllRequiredFieldsFilled() == false, "Required fields should be empty initially");
        Assert.assertFalse(signupPage.getTitleText().isEmpty(), "Title text should not be empty");

        logger.info("Signup page elements test completed successfully");
    }
}
