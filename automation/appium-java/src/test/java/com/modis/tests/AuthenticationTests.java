package com.modis.tests;

import com.modis.base.BaseTest;
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
    
    @Test(priority = 1, description = "Verify successful login with valid credentials")
    public void testValidLogin() {
        logger.info("Starting valid login test");
        
        LoadingPage loadingPage = new LoadingPage();
        loadingPage.waitForPageToLoad();
        
        LoginPage loginPage = loadingPage.clickLoginButton();
        loginPage.waitForPageToLoad();
        
        HomePage homePage = (HomePage) loginPage.login(
            AppConstants.TEST_USER_USERNAME, 
            AppConstants.TEST_USER_PASSWORD
        );
        
        Assert.assertTrue(homePage.isDisplayed(), "Home page should be displayed after successful login");
        Assert.assertTrue(homePage.verifyPageElements(), "Home page elements should be present");
        Assert.assertTrue(homePage.isUserLoggedIn(), "User should be logged in");
        
        logger.info("Valid login test completed successfully");
    }
    
    @Test(priority = 2, description = "Verify login failure with invalid credentials")
    public void testInvalidLogin() {
        logger.info("Starting invalid login test");
        
        LoadingPage loadingPage = new LoadingPage();
        LoginPage loginPage = loadingPage.clickLoginButton();
        
        LoginPage resultPage = (LoginPage) loginPage.loginWithInvalidCredentials(
            "invaliduser", "invalidpass"
        );
        
        Assert.assertTrue(resultPage.isDisplayed(), "Should remain on login page after invalid login");
        Assert.assertTrue(resultPage.isErrorMessageDisplayed(), "Error message should be displayed");
        
        String errorMessage = resultPage.getErrorMessage();
        Assert.assertFalse(errorMessage.isEmpty(), "Error message should not be empty");
        
        logger.info("Invalid login test completed successfully");
    }
    
    @Test(priority = 3, description = "Verify login with empty credentials")
    public void testEmptyCredentialsLogin() {
        logger.info("Starting empty credentials login test");
        
        LoadingPage loadingPage = new LoadingPage();
        LoginPage loginPage = loadingPage.clickLoginButton();
        
        LoginPage resultPage = loginPage.loginWithEmptyCredentials();
        
        Assert.assertTrue(resultPage.isDisplayed(), "Should remain on login page");
        Assert.assertFalse(resultPage.isLoginButtonEnabled(), "Login button should be disabled for empty credentials");
        
        logger.info("Empty credentials login test completed successfully");
    }
    
    @Test(priority = 4, description = "Verify login with username only")
    public void testUsernameOnlyLogin() {
        logger.info("Starting username only login test");
        
        LoadingPage loadingPage = new LoadingPage();
        LoginPage loginPage = loadingPage.clickLoginButton();
        
        LoginPage resultPage = loginPage.loginWithUsernameOnly(AppConstants.TEST_USER_USERNAME);
        
        Assert.assertTrue(resultPage.isDisplayed(), "Should remain on login page");
        Assert.assertTrue(resultPage.isPasswordEmpty(), "Password field should be empty");
        
        logger.info("Username only login test completed successfully");
    }
    
    @Test(priority = 5, description = "Verify login with password only")
    public void testPasswordOnlyLogin() {
        logger.info("Starting password only login test");
        
        LoadingPage loadingPage = new LoadingPage();
        LoginPage loginPage = loadingPage.clickLoginButton();
        
        LoginPage resultPage = loginPage.loginWithPasswordOnly(AppConstants.TEST_USER_PASSWORD);
        
        Assert.assertTrue(resultPage.isDisplayed(), "Should remain on login page");
        Assert.assertTrue(resultPage.isUsernameEmpty(), "Username field should be empty");
        
        logger.info("Password only login test completed successfully");
    }
    
    // ==================== SIGNUP TESTS ====================
    
    @Test(priority = 6, description = "Verify successful signup with valid information")
    public void testValidSignup() {
        logger.info("Starting valid signup test");
        
        LoadingPage loadingPage = new LoadingPage();
        SignupPage signupPage = loadingPage.clickSignupButton();
        signupPage.waitForPageToLoad();
        
        HomePage homePage = (HomePage) signupPage.signupWithRandomUser();
        
        Assert.assertTrue(homePage.isDisplayed(), "Home page should be displayed after successful signup");
        Assert.assertTrue(homePage.verifyPageElements(), "Home page elements should be present");
        
        logger.info("Valid signup test completed successfully");
    }
    
    @Test(priority = 7, description = "Verify signup with empty fields")
    public void testEmptyFieldsSignup() {
        logger.info("Starting empty fields signup test");
        
        LoadingPage loadingPage = new LoadingPage();
        SignupPage signupPage = loadingPage.clickSignupButton();
        
        SignupPage resultPage = signupPage.signupWithEmptyFields();
        
        Assert.assertTrue(resultPage.isDisplayed(), "Should remain on signup page");
        Assert.assertFalse(resultPage.isSignupButtonEnabled(), "Signup button should be disabled for empty fields");
        
        logger.info("Empty fields signup test completed successfully");
    }
    
    @Test(priority = 8, description = "Verify signup with mismatched passwords")
    public void testMismatchedPasswordsSignup() {
        logger.info("Starting mismatched passwords signup test");
        
        LoadingPage loadingPage = new LoadingPage();
        SignupPage signupPage = loadingPage.clickSignupButton();
        
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
    
    @Test(priority = 9, description = "Verify signup with invalid email format")
    public void testInvalidEmailSignup() {
        logger.info("Starting invalid email signup test");
        
        LoadingPage loadingPage = new LoadingPage();
        SignupPage signupPage = loadingPage.clickSignupButton();
        
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
    
    @Test(priority = 10, description = "Verify signup with existing username")
    public void testExistingUsernameSignup() {
        logger.info("Starting existing username signup test");
        
        LoadingPage loadingPage = new LoadingPage();
        SignupPage signupPage = loadingPage.clickSignupButton();
        
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
    
    @Test(priority = 11, description = "Verify navigation from login to signup")
    public void testLoginToSignupNavigation() {
        logger.info("Starting login to signup navigation test");
        
        LoadingPage loadingPage = new LoadingPage();
        LoginPage loginPage = loadingPage.clickLoginButton();
        
        SignupPage signupPage = loginPage.clickSignupLink();
        
        Assert.assertTrue(signupPage.isDisplayed(), "Signup page should be displayed");
        Assert.assertTrue(signupPage.verifyPageElements(), "Signup page elements should be present");
        
        logger.info("Login to signup navigation test completed successfully");
    }
    
    @Test(priority = 12, description = "Verify navigation from signup to login")
    public void testSignupToLoginNavigation() {
        logger.info("Starting signup to login navigation test");
        
        LoadingPage loadingPage = new LoadingPage();
        SignupPage signupPage = loadingPage.clickSignupButton();
        
        LoginPage loginPage = signupPage.clickLoginLink();
        
        Assert.assertTrue(loginPage.isDisplayed(), "Login page should be displayed");
        Assert.assertTrue(loginPage.verifyPageElements(), "Login page elements should be present");
        
        logger.info("Signup to login navigation test completed successfully");
    }
    
    // ==================== LOGOUT TESTS ====================
    
    @Test(priority = 13, description = "Verify successful logout", dependsOnMethods = {"testValidLogin"})
    public void testSuccessfulLogout() {
        logger.info("Starting successful logout test");
        
        // First login
        LoadingPage loadingPage = new LoadingPage();
        LoginPage loginPage = loadingPage.clickLoginButton();
        HomePage homePage = (HomePage) loginPage.loginWithTestUser();
        
        // Navigate to profile and logout
        ProfilePage profilePage = homePage.navigateToProfile();
        LoginPage resultPage = profilePage.logout();
        
        Assert.assertTrue(resultPage.isDisplayed(), "Login page should be displayed after logout");
        Assert.assertTrue(resultPage.verifyPageElements(), "Login page elements should be present");
        
        logger.info("Successful logout test completed successfully");
    }
    
    @Test(priority = 14, description = "Verify logout cancellation")
    public void testLogoutCancellation() {
        logger.info("Starting logout cancellation test");
        
        // First login
        LoadingPage loadingPage = new LoadingPage();
        LoginPage loginPage = loadingPage.clickLoginButton();
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
    
    @Test(priority = 15, description = "Verify username field validation")
    public void testUsernameValidation() {
        logger.info("Starting username validation test");
        
        LoadingPage loadingPage = new LoadingPage();
        SignupPage signupPage = loadingPage.clickSignupButton();
        
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
    
    @Test(priority = 16, description = "Verify password field validation")
    public void testPasswordValidation() {
        logger.info("Starting password validation test");
        
        LoadingPage loadingPage = new LoadingPage();
        SignupPage signupPage = loadingPage.clickSignupButton();
        
        // Test minimum length
        signupPage.enterPassword("12345");
        Assert.assertFalse(signupPage.isPasswordValid(), "Password should be invalid for length < 6");
        
        // Test valid password
        signupPage.enterPassword("validpass123");
        Assert.assertTrue(signupPage.isPasswordValid(), "Password should be valid");
        
        logger.info("Password validation test completed successfully");
    }
    
    @Test(priority = 17, description = "Verify email field validation")
    public void testEmailValidation() {
        logger.info("Starting email validation test");
        
        LoadingPage loadingPage = new LoadingPage();
        SignupPage signupPage = loadingPage.clickSignupButton();
        
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
    
    @Test(priority = 18, description = "Verify phone field validation")
    public void testPhoneValidation() {
        logger.info("Starting phone validation test");
        
        LoadingPage loadingPage = new LoadingPage();
        SignupPage signupPage = loadingPage.clickSignupButton();
        
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
    
    @Test(priority = 19, description = "Verify login page UI elements")
    public void testLoginPageElements() {
        logger.info("Starting login page elements test");
        
        LoadingPage loadingPage = new LoadingPage();
        LoginPage loginPage = loadingPage.clickLoginButton();
        
        Assert.assertTrue(loginPage.verifyPageElements(), "All login page elements should be present");
        Assert.assertTrue(loginPage.isSignupLinkDisplayed(), "Signup link should be displayed");
        Assert.assertFalse(loginPage.getTitleText().isEmpty(), "Title text should not be empty");
        
        logger.info("Login page elements test completed successfully");
    }
    
    @Test(priority = 20, description = "Verify signup page UI elements")
    public void testSignupPageElements() {
        logger.info("Starting signup page elements test");
        
        LoadingPage loadingPage = new LoadingPage();
        SignupPage signupPage = loadingPage.clickSignupButton();
        
        Assert.assertTrue(signupPage.verifyPageElements(), "All signup page elements should be present");
        Assert.assertTrue(signupPage.areAllRequiredFieldsFilled() == false, "Required fields should be empty initially");
        Assert.assertFalse(signupPage.getTitleText().isEmpty(), "Title text should not be empty");
        
        logger.info("Signup page elements test completed successfully");
    }
}