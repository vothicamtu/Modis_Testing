package com.modis.tests;

import com.modis.base.BaseTest;
import com.modis.constants.AppConstants;
import com.modis.pages.*;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for Profile functionality
 * Covers profile viewing, editing, settings, and account management
 */
public class ProfileTests extends BaseTest {
    
    private HomePage homePage;
    
    @BeforeMethod(groups = {"profile", "regression"})
    public void loginBeforeTest() {
        logger.info("Logging in before profile test");
        
        LoadingPage loadingPage = new LoadingPage();
        LoginPage loginPage = loadingPage.clickLoginButton();
        homePage = (HomePage) loginPage.loginWithTestUser();
        
        Assert.assertTrue(homePage.isDisplayed(), "Should be logged in before profile tests");
    }
    
    // ==================== NAVIGATION TESTS ====================
    
    @Test(priority = 1, groups = {"profile", "smoke"}, 
          description = "Verify navigation to profile screen")
    public void testNavigateToProfile() {
        logger.info("Starting navigate to profile test");
        
        ProfilePage profilePage = homePage.navigateToProfile();
        
        Assert.assertTrue(profilePage.isDisplayed(), "Profile page should be displayed");
        Assert.assertTrue(profilePage.verifyPageElements(), "Profile page elements should be present");
        
        logger.info("Navigate to profile test completed successfully");
    }
    
    @Test(priority = 2, groups = {"profile", "navigation"}, 
          description = "Verify back navigation from profile screen")
    public void testBackNavigationFromProfile() {
        logger.info("Starting back navigation from profile test");
        
        ProfilePage profilePage = homePage.navigateToProfile();
        HomePage returnedHomePage = profilePage.navigateBack();
        
        Assert.assertTrue(returnedHomePage.isDisplayed(), 
            "Should return to home page after back navigation");
        Assert.assertTrue(returnedHomePage.verifyPageElements(), 
            "Home page elements should be present after back navigation");
        
        logger.info("Back navigation from profile test completed successfully");
    }
    
    // ==================== PROFILE DISPLAY TESTS ====================
    
    @Test(priority = 3, groups = {"profile", "regression"}, 
          description = "Verify profile information display")
    public void testProfileInformationDisplay() {
        logger.info("Starting profile information display test");
        
        ProfilePage profilePage = homePage.navigateToProfile();
        
        // Verify avatar is displayed
        Assert.assertTrue(profilePage.isAvatarDisplayed(), 
            "Profile avatar should be displayed");
        
        // Verify username is displayed
        Assert.assertTrue(profilePage.isUsernameDisplayed(), 
            "Username should be displayed");
        Assert.assertFalse(profilePage.getUsername().isEmpty(), 
            "Username should not be empty");
        
        // Verify profile menu items are displayed
        Assert.assertTrue(profilePage.areProfileMenuItemsDisplayed(), 
            "Profile menu items should be displayed");
        
        logger.info("Profile information display test completed successfully");
    }
    
    @Test(priority = 4, groups = {"profile", "regression"}, 
          description = "Verify profile menu items")
    public void testProfileMenuItems() {
        logger.info("Starting profile menu items test");
        
        ProfilePage profilePage = homePage.navigateToProfile();
        
        // Verify edit username item
        Assert.assertTrue(profilePage.isEditUsernameItemDisplayed(), 
            "Edit username item should be displayed");
        
        // Verify edit phone item
        Assert.assertTrue(profilePage.isEditPhoneItemDisplayed(), 
            "Edit phone item should be displayed");
        
        // Verify edit email item
        Assert.assertTrue(profilePage.isEditEmailItemDisplayed(), 
            "Edit email item should be displayed");
        
        // Verify change password item
        Assert.assertTrue(profilePage.isChangePasswordItemDisplayed(), 
            "Change password item should be displayed");
        
        // Verify theme toggle
        Assert.assertTrue(profilePage.isThemeToggleDisplayed(), 
            "Theme toggle should be displayed");
        
        // Verify logout button
        Assert.assertTrue(profilePage.isLogoutButtonDisplayed(), 
            "Logout button should be displayed");
        
        logger.info("Profile menu items test completed successfully");
    }
    
    // ==================== PROFILE EDITING TESTS ====================
    
    @Test(priority = 5, groups = {"profile", "regression"}, 
          description = "Verify edit username functionality")
    public void testEditUsername() {
        logger.info("Starting edit username test");
        
        ProfilePage profilePage = homePage.navigateToProfile();
        String originalUsername = profilePage.getUsername();
        
        // Click edit username
        profilePage.clickEditUsernameItem();
        
        if (profilePage.isUsernameEditDialogDisplayed()) {
            String newUsername = "testuser" + System.currentTimeMillis();
            
            profilePage.enterNewUsername(newUsername);
            profilePage.confirmUsernameEdit();
            
            // Wait for update to complete
            profilePage.waitForUsernameUpdate();
            
            // Verify username was updated (or check for validation message)
            if (profilePage.isUsernameUpdateSuccessful()) {
                Assert.assertEquals(profilePage.getUsername(), newUsername, 
                    "Username should be updated to new value");
                
                // Revert back to original username for cleanup
                profilePage.clickEditUsernameItem();
                profilePage.enterNewUsername(originalUsername);
                profilePage.confirmUsernameEdit();
            } else {
                logger.info("Username update was not successful - may be due to validation rules");
            }
        } else {
            logger.info("Username edit dialog not displayed - feature may not be available");
        }
        
        logger.info("Edit username test completed successfully");
    }
    
    @Test(priority = 6, groups = {"profile", "regression"}, 
          description = "Verify edit phone functionality")
    public void testEditPhone() {
        logger.info("Starting edit phone test");
        
        ProfilePage profilePage = homePage.navigateToProfile();
        
        // Click edit phone
        profilePage.clickEditPhoneItem();
        
        if (profilePage.isPhoneEditDialogDisplayed()) {
            String newPhone = "1234567890";
            
            profilePage.enterNewPhone(newPhone);
            profilePage.confirmPhoneEdit();
            
            // Wait for update to complete
            profilePage.waitForPhoneUpdate();
            
            // Verify phone was updated or check for validation message
            if (profilePage.isPhoneUpdateSuccessful()) {
                logger.info("Phone number updated successfully");
            } else {
                logger.info("Phone update was not successful - may be due to validation rules");
            }
        } else {
            logger.info("Phone edit dialog not displayed - feature may not be available");
        }
        
        logger.info("Edit phone test completed successfully");
    }
    
    @Test(priority = 7, groups = {"profile", "regression"}, 
          description = "Verify edit email functionality")
    public void testEditEmail() {
        logger.info("Starting edit email test");
        
        ProfilePage profilePage = homePage.navigateToProfile();
        
        // Click edit email
        profilePage.clickEditEmailItem();
        
        if (profilePage.isEmailEditDialogDisplayed()) {
            String newEmail = "test" + System.currentTimeMillis() + "@example.com";
            
            profilePage.enterNewEmail(newEmail);
            profilePage.confirmEmailEdit();
            
            // Wait for update to complete
            profilePage.waitForEmailUpdate();
            
            // Verify email was updated or check for validation message
            if (profilePage.isEmailUpdateSuccessful()) {
                logger.info("Email updated successfully");
            } else {
                logger.info("Email update was not successful - may be due to validation rules");
            }
        } else {
            logger.info("Email edit dialog not displayed - feature may not be available");
        }
        
        logger.info("Edit email test completed successfully");
    }
    
    @Test(priority = 8, groups = {"profile", "regression"}, 
          description = "Verify change password functionality")
    public void testChangePassword() {
        logger.info("Starting change password test");
        
        ProfilePage profilePage = homePage.navigateToProfile();
        
        // Click change password
        profilePage.clickChangePasswordItem();
        
        if (profilePage.isChangePasswordDialogDisplayed()) {
            String currentPassword = AppConstants.TEST_USER_PASSWORD;
            String newPassword = "newpassword123";
            
            profilePage.enterCurrentPassword(currentPassword);
            profilePage.enterNewPassword(newPassword);
            profilePage.enterConfirmPassword(newPassword);
            profilePage.confirmPasswordChange();
            
            // Wait for update to complete
            profilePage.waitForPasswordUpdate();
            
            // Verify password change result
            if (profilePage.isPasswordUpdateSuccessful()) {
                logger.info("Password changed successfully");
                
                // Revert password back for other tests
                profilePage.clickChangePasswordItem();
                profilePage.enterCurrentPassword(newPassword);
                profilePage.enterNewPassword(currentPassword);
                profilePage.enterConfirmPassword(currentPassword);
                profilePage.confirmPasswordChange();
            } else {
                logger.info("Password change was not successful");
            }
        } else {
            logger.info("Change password dialog not displayed - feature may not be available");
        }
        
        logger.info("Change password test completed successfully");
    }
    
    // ==================== THEME TOGGLE TESTS ====================
    
    @Test(priority = 9, groups = {"profile", "regression"}, 
          description = "Verify theme toggle functionality")
    public void testThemeToggle() {
        logger.info("Starting theme toggle test");
        
        ProfilePage profilePage = homePage.navigateToProfile();
        
        // Get current theme state
        boolean initialThemeState = profilePage.isThemeToggleEnabled();
        
        // Toggle theme
        profilePage.toggleTheme();
        
        // Wait for theme change to apply
        profilePage.waitForThemeChange();
        
        // Verify theme state changed
        boolean newThemeState = profilePage.isThemeToggleEnabled();
        Assert.assertNotEquals(newThemeState, initialThemeState, 
            "Theme state should change after toggle");
        
        // Verify UI reflects theme change
        Assert.assertTrue(profilePage.isThemeChangeApplied(), 
            "UI should reflect theme change");
        
        // Toggle back to original state
        profilePage.toggleTheme();
        profilePage.waitForThemeChange();
        
        // Verify theme reverted
        boolean finalThemeState = profilePage.isThemeToggleEnabled();
        Assert.assertEquals(finalThemeState, initialThemeState, 
            "Theme should revert to original state");
        
        logger.info("Theme toggle test completed successfully");
    }
    
    // ==================== LOGOUT TESTS ====================
    
    @Test(priority = 10, groups = {"profile", "regression"}, 
          description = "Verify logout functionality")
    public void testLogout() {
        logger.info("Starting logout test");
        
        ProfilePage profilePage = homePage.navigateToProfile();
        
        // Perform logout
        LoginPage loginPage = profilePage.logout();
        
        // Verify logout was successful
        Assert.assertTrue(loginPage.isDisplayed(), 
            "Login page should be displayed after logout");
        Assert.assertTrue(loginPage.verifyPageElements(), 
            "Login page elements should be present after logout");
        
        // Verify user is actually logged out by trying to access protected content
        // This would typically involve checking if we can access home without login
        
        logger.info("Logout test completed successfully");
    }
    
    @Test(priority = 11, groups = {"profile", "regression"}, 
          description = "Verify logout confirmation dialog")
    public void testLogoutConfirmationDialog() {
        logger.info("Starting logout confirmation dialog test");
        
        ProfilePage profilePage = homePage.navigateToProfile();
        
        // Click logout button
        profilePage.clickLogoutButton();
        
        if (profilePage.isLogoutConfirmationDialogDisplayed()) {
            // Test cancel logout
            profilePage.cancelLogout();
            
            // Verify we're still on profile page
            Assert.assertTrue(profilePage.isDisplayed(), 
                "Should remain on profile page after canceling logout");
            
            // Test confirm logout
            profilePage.clickLogoutButton();
            profilePage.confirmLogout();
            
            // Verify logout was successful
            LoadingPage loadingPage = new LoadingPage();
            Assert.assertTrue(loadingPage.isDisplayed() || 
                            new LoginPage().isDisplayed(), 
                "Should be on loading or login page after logout");
        } else {
            logger.info("Logout confirmation dialog not displayed - direct logout may be implemented");
        }
        
        logger.info("Logout confirmation dialog test completed successfully");
    }
    
    // ==================== SHARE APP TESTS ====================
    
    @Test(priority = 12, groups = {"profile", "regression"}, 
          description = "Verify share app functionality")
    public void testShareApp() {
        logger.info("Starting share app test");
        
        ProfilePage profilePage = homePage.navigateToProfile();
        
        if (profilePage.isShareAppItemDisplayed()) {
            profilePage.clickShareAppItem();
            
            // Verify share dialog or action sheet appears
            if (profilePage.isShareDialogDisplayed()) {
                Assert.assertTrue(profilePage.areShareOptionsDisplayed(), 
                    "Share options should be displayed");
                
                // Close share dialog
                profilePage.closeShareDialog();
                
                // Verify we're back to profile page
                Assert.assertTrue(profilePage.isDisplayed(), 
                    "Should return to profile page after closing share dialog");
            } else {
                logger.info("Share dialog not displayed - may open external app directly");
            }
        } else {
            logger.info("Share app item not displayed - feature may not be available");
        }
        
        logger.info("Share app test completed successfully");
    }
    
    // ==================== DELETE ACCOUNT TESTS ====================
    
    @Test(priority = 13, groups = {"profile", "regression"}, 
          description = "Verify delete account option display")
    public void testDeleteAccountDisplay() {
        logger.info("Starting delete account display test");
        
        ProfilePage profilePage = homePage.navigateToProfile();
        
        if (profilePage.isDeleteAccountItemDisplayed()) {
            Assert.assertTrue(profilePage.isDeleteAccountItemDisplayed(), 
                "Delete account item should be displayed");
            
            // Note: We don't actually test deletion to avoid destroying test account
            logger.info("Delete account option is available");
        } else {
            logger.info("Delete account item not displayed - feature may not be available");
        }
        
        logger.info("Delete account display test completed successfully");
    }
    
    @Test(priority = 14, groups = {"profile", "regression"}, 
          description = "Verify delete account confirmation flow")
    public void testDeleteAccountConfirmationFlow() {
        logger.info("Starting delete account confirmation flow test");
        
        ProfilePage profilePage = homePage.navigateToProfile();
        
        if (profilePage.isDeleteAccountItemDisplayed()) {
            profilePage.clickDeleteAccountItem();
            
            if (profilePage.isDeleteAccountConfirmationDisplayed()) {
                // Test cancel deletion
                profilePage.cancelAccountDeletion();
                
                // Verify we're still on profile page
                Assert.assertTrue(profilePage.isDisplayed(), 
                    "Should remain on profile page after canceling account deletion");
                
                // Note: We don't test actual deletion to preserve test account
                logger.info("Delete account confirmation flow works correctly");
            } else {
                logger.info("Delete account confirmation not displayed");
            }
        } else {
            logger.info("Delete account item not available for testing");
        }
        
        logger.info("Delete account confirmation flow test completed successfully");
    }
    
    // ==================== AVATAR TESTS ====================
    
    @Test(priority = 15, groups = {"profile", "regression"}, 
          description = "Verify avatar display and interaction")
    public void testAvatarDisplayAndInteraction() {
        logger.info("Starting avatar display and interaction test");
        
        ProfilePage profilePage = homePage.navigateToProfile();
        
        // Verify avatar is displayed
        Assert.assertTrue(profilePage.isAvatarDisplayed(), 
            "Profile avatar should be displayed");
        
        // Test avatar click (if clickable)
        if (profilePage.isAvatarClickable()) {
            profilePage.clickAvatar();
            
            if (profilePage.isAvatarOptionsDisplayed()) {
                Assert.assertTrue(profilePage.areAvatarOptionsDisplayed(), 
                    "Avatar options should be displayed after clicking avatar");
                
                // Close avatar options
                profilePage.closeAvatarOptions();
                
                // Verify we're back to normal profile view
                Assert.assertTrue(profilePage.isDisplayed(), 
                    "Should return to normal profile view after closing avatar options");
            } else {
                logger.info("Avatar options not displayed - avatar may not be editable");
            }
        } else {
            logger.info("Avatar is not clickable - no interaction available");
        }
        
        logger.info("Avatar display and interaction test completed successfully");
    }
    
    // ==================== VALIDATION TESTS ====================
    
    @Test(priority = 16, groups = {"profile", "regression"}, 
          description = "Verify profile edit validation")
    public void testProfileEditValidation() {
        logger.info("Starting profile edit validation test");
        
        ProfilePage profilePage = homePage.navigateToProfile();
        
        // Test username validation
        profilePage.clickEditUsernameItem();
        if (profilePage.isUsernameEditDialogDisplayed()) {
            // Test empty username
            profilePage.enterNewUsername("");
            Assert.assertFalse(profilePage.isUsernameEditConfirmEnabled(), 
                "Confirm button should be disabled for empty username");
            
            // Test invalid username (too short)
            profilePage.enterNewUsername("ab");
            Assert.assertFalse(profilePage.isUsernameEditConfirmEnabled(), 
                "Confirm button should be disabled for too short username");
            
            // Test valid username
            profilePage.enterNewUsername("validusername");
            Assert.assertTrue(profilePage.isUsernameEditConfirmEnabled(), 
                "Confirm button should be enabled for valid username");
            
            profilePage.cancelUsernameEdit();
        }
        
        // Test email validation
        profilePage.clickEditEmailItem();
        if (profilePage.isEmailEditDialogDisplayed()) {
            // Test invalid email format
            profilePage.enterNewEmail("invalidemail");
            Assert.assertFalse(profilePage.isEmailEditConfirmEnabled(), 
                "Confirm button should be disabled for invalid email format");
            
            // Test valid email
            profilePage.enterNewEmail("valid@example.com");
            Assert.assertTrue(profilePage.isEmailEditConfirmEnabled(), 
                "Confirm button should be enabled for valid email");
            
            profilePage.cancelEmailEdit();
        }
        
        logger.info("Profile edit validation test completed successfully");
    }
    
    // ==================== ERROR HANDLING TESTS ====================
    
    @Test(priority = 17, groups = {"profile", "regression"}, 
          description = "Verify network error handling in profile")
    public void testNetworkErrorHandling() {
        logger.info("Starting network error handling test");
        
        ProfilePage profilePage = homePage.navigateToProfile();
        
        // Simulate network issue
        backgroundApp(3);
        
        // Try to perform network-dependent actions
        profilePage.clickEditUsernameItem();
        
        // Verify app remains stable
        Assert.assertTrue(profilePage.isDisplayed(), 
            "Profile page should remain stable during network issues");
        
        // Try theme toggle (may be local or require network)
        profilePage.toggleTheme();
        
        // Verify app doesn't crash
        Assert.assertTrue(profilePage.isDisplayed(), 
            "Profile page should remain stable after theme toggle during network issues");
        
        logger.info("Network error handling test completed successfully");
    }
}