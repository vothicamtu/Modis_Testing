package com.modis.tests;

import com.modis.base.BaseTest;
import com.modis.constants.AppConstants;
import com.modis.pages.*;
import com.modis.base.BasePage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for Navigation functionality
 * Covers screen navigation, gestures, and user flows
 */
public class NavigationTests extends BaseTest {
    
    private HomePage homePage;
    
    @BeforeMethod(alwaysRun = true)
    public void loginBeforeEachTest() {
        logger.info("Logging in before navigation test");

        // Nếu app đã login sẵn (do noReset=true), chỉ cần init HomePage và chạy tiếp
        try {
            HomePage currentHome = new HomePage();
            if (currentHome.isDisplayed()) {
                logger.info("Already on Home screen - skipping login flow");
                homePage = currentHome;
                return;
            }
        } catch (Exception ignore) {
            // ignore - sẽ thử theo luồng login bên dưới
        }

        // Nếu đang ở Login screen thì login luôn, nếu không thì đi từ Loading screen
        LoginPage loginPage;
        try {
            LoginPage currentLogin = new LoginPage();
            if (currentLogin.isDisplayed()) {
                logger.info("Detected Login screen - proceeding to login");
                loginPage = currentLogin;
            } else {
                LoadingPage loadingPage = new LoadingPage();
                logger.info("Detected Loading/initial screen - navigating to Login screen");
                loginPage = loadingPage.clickLoginButton();
            }
        } catch (Exception e) {
            logger.warn("Could not detect current screen reliably, fallback to LoadingPage -> LoginPage. Reason: {}", e.getMessage());
            LoadingPage loadingPage = new LoadingPage();
            loginPage = loadingPage.clickLoginButton();
        }

        BasePage afterLogin = loginPage.loginWithTestUser();
        Assert.assertTrue(afterLogin instanceof HomePage,
                "Login should navigate to HomePage, but got: " + (afterLogin != null ? afterLogin.getClass().getSimpleName() : "null"));

        homePage = (HomePage) afterLogin;
        Assert.assertTrue(homePage.isDisplayed(), "Should be on home page before navigation tests");
    }
    
    // ==================== BASIC NAVIGATION TESTS ====================
    
    @Test(priority = 1, groups = {"navigation", "regression", "smoke"}, description = "Verify navigation to Profile screen")
    public void testNavigateToProfile() {
        logger.info("Starting navigate to profile test");
        
        ProfilePage profilePage = homePage.navigateToProfile();
        
        Assert.assertTrue(profilePage.isDisplayed(), "Profile page should be displayed");
        Assert.assertTrue(profilePage.verifyPageElements(), "Profile page elements should be present");
        Assert.assertFalse(profilePage.getDisplayedUsername().isEmpty(), "Username should be displayed");
        
        logger.info("Navigate to profile test completed successfully");
    }
    
    @Test(priority = 2, groups = {"navigation", "regression"}, description = "Verify navigation to Friends screen")
    public void testNavigateToFriends() {
        logger.info("Starting navigate to friends test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        
        Assert.assertTrue(friendsPage.isDisplayed(), "Friends page should be displayed");
        Assert.assertTrue(friendsPage.verifyPageElements(), "Friends page elements should be present");
        Assert.assertTrue(friendsPage.isSearchInputDisplayed(), "Search input should be displayed");
        
        logger.info("Navigate to friends test completed successfully");
    }
    
    @Test(priority = 3, groups = {"navigation", "regression"}, description = "Verify navigation to Messages screen")
    public void testNavigateToMessages() {
        logger.info("Starting navigate to messages test");
        
        MessagePage messagePage = homePage.navigateToMessages();
        
        Assert.assertTrue(messagePage.isDisplayed(), "Message page should be displayed");
        Assert.assertTrue(messagePage.verifyPageElements(), "Message page elements should be present");
        
        logger.info("Navigate to messages test completed successfully");
    }
    
    @Test(priority = 4, groups = {"navigation", "regression", "smoke"}, description = "Verify navigation to Camera screen using gesture")
    public void testNavigateToCamera() {
        logger.info("Starting navigate to camera test");
        
        TakePage takePage = homePage.navigateToCamera();
        
        Assert.assertTrue(takePage.isDisplayed(), "Take/Camera page should be displayed");
        Assert.assertTrue(takePage.verifyPageElements(), "Camera page elements should be present");
        Assert.assertTrue(takePage.isCaptureButtonDisplayed(), "Capture button should be displayed");
        
        logger.info("Navigate to camera test completed successfully");
    }
    
    // ==================== BACK NAVIGATION TESTS ====================
    
    @Test(priority = 5, groups = {"navigation", "regression"}, description = "Verify back navigation from Profile")
    public void testBackNavigationFromProfile() {
        logger.info("Starting back navigation from profile test");
        
        ProfilePage profilePage = homePage.navigateToProfile();
        HomePage returnedHomePage = profilePage.navigateBack();
        
        Assert.assertTrue(returnedHomePage.isDisplayed(), "Should return to home page");
        Assert.assertTrue(returnedHomePage.verifyPageElements(), "Home page elements should be present");
        
        logger.info("Back navigation from profile test completed successfully");
    }
    
    @Test(priority = 6, groups = {"navigation", "regression"}, description = "Verify back navigation from Friends")
    public void testBackNavigationFromFriends() {
        logger.info("Starting back navigation from friends test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        HomePage returnedHomePage = friendsPage.navigateBack();
        
        Assert.assertTrue(returnedHomePage.isDisplayed(), "Should return to home page");
        Assert.assertTrue(returnedHomePage.verifyPageElements(), "Home page elements should be present");
        
        logger.info("Back navigation from friends test completed successfully");
    }
    
    @Test(priority = 7, groups = {"navigation", "regression"}, description = "Verify back navigation from Messages")
    public void testBackNavigationFromMessages() {
        logger.info("Starting back navigation from messages test");
        
        MessagePage messagePage = homePage.navigateToMessages();
        HomePage returnedHomePage = messagePage.navigateBack();
        
        Assert.assertTrue(returnedHomePage.isDisplayed(), "Should return to home page");
        Assert.assertTrue(returnedHomePage.verifyPageElements(), "Home page elements should be present");
        
        logger.info("Back navigation from messages test completed successfully");
    }
    
    @Test(priority = 8, groups = {"navigation", "regression"}, description = "Verify back navigation from Camera")
    public void testBackNavigationFromCamera() {
        logger.info("Starting back navigation from camera test");
        
        TakePage takePage = homePage.navigateToCamera();
        HomePage returnedHomePage = takePage.navigateBack();
        
        Assert.assertTrue(returnedHomePage.isDisplayed(), "Should return to home page");
        Assert.assertTrue(returnedHomePage.verifyPageElements(), "Home page elements should be present");
        
        logger.info("Back navigation from camera test completed successfully");
    }
    
    // ==================== DEEP NAVIGATION TESTS ====================
    
    @Test(priority = 9, groups = {"navigation", "regression"}, description = "Verify navigation to conversation from messages")
    public void testNavigateToConversation() {
        logger.info("Starting navigate to conversation test");
        
        MessagePage messagePage = homePage.navigateToMessages();
        
        // Check if there are conversations available
        if (messagePage.getVisibleConversationsCount() > 0) {
            ConversationPage conversationPage = messagePage.openConversation("test_conversation_1");
            
            Assert.assertTrue(conversationPage.isDisplayed(), "Conversation page should be displayed");
            Assert.assertTrue(conversationPage.verifyPageElements(), "Conversation page elements should be present");
        } else {
            logger.info("No conversations available for testing");
        }
        
        logger.info("Navigate to conversation test completed successfully");
    }
    
    @Test(priority = 10, groups = {"navigation", "regression"}, description = "Verify navigation to all images from camera")
    public void testNavigateToAllImages() {
        logger.info("Starting navigate to all images test");
        
        TakePage takePage = homePage.navigateToCamera();
        AllImagesPage allImagesPage = takePage.openHistory();
        
        Assert.assertTrue(allImagesPage.isDisplayed(), "All images page should be displayed");
        Assert.assertTrue(allImagesPage.verifyPageElements(), "All images page elements should be present");
        
        logger.info("Navigate to all images test completed successfully");
    }
    
    // ==================== GESTURE NAVIGATION TESTS ====================
    
    @Test(priority = 11, groups = {"navigation", "regression"}, description = "Verify swipe gestures on home screen")
    public void testSwipeGestures() {
        logger.info("Starting swipe gestures test");
        
        // Test different swipe directions
        homePage.performSwipeGesture("left");
        Assert.assertTrue(homePage.isDisplayed(), "Should remain on home page after left swipe");
        
        homePage.performSwipeGesture("right");
        Assert.assertTrue(homePage.isDisplayed(), "Should remain on home page after right swipe");
        
        homePage.performSwipeGesture("down");
        Assert.assertTrue(homePage.isDisplayed(), "Should remain on home page after down swipe");
        
        logger.info("Swipe gestures test completed successfully");
    }
    
    @Test(priority = 12, groups = {"navigation", "regression"}, description = "Verify tap gestures")
    public void testTapGestures() {
        logger.info("Starting tap gestures test");
        
        homePage.performTapGesture();
        Assert.assertTrue(homePage.isDisplayed(), "Should remain on home page after tap");
        
        homePage.performDoubleTapGesture();
        Assert.assertTrue(homePage.isDisplayed(), "Should remain on home page after double tap");
        
        logger.info("Tap gestures test completed successfully");
    }
    
    // ==================== NAVIGATION FLOW TESTS ====================
    
    @Test(priority = 13, groups = {"navigation", "regression"}, description = "Verify complete camera flow navigation")
    public void testCompleteCameraFlow() {
        logger.info("Starting complete camera flow test");
        
        // Navigate to camera
        TakePage takePage = homePage.navigateToCamera();
        Assert.assertTrue(takePage.isDisplayed(), "Camera page should be displayed");
        
        // Capture photo (this should navigate to send photo)
        BasePage resultPage = takePage.capturePhoto();
        
        if (resultPage instanceof SendPhotoPage) {
            SendPhotoPage sendPhotoPage = (SendPhotoPage) resultPage;
            Assert.assertTrue(sendPhotoPage.isDisplayed(), "Send photo page should be displayed");
            
            // Close send photo to return to camera or home
            BasePage closedPage = sendPhotoPage.closeSendPhoto();
            Assert.assertTrue(closedPage.isDisplayed(), "Should navigate back after closing send photo");
        }
        
        logger.info("Complete camera flow test completed successfully");
    }
    
    @Test(priority = 14, groups = {"navigation", "regression"}, description = "Verify complete messaging flow navigation")
    public void testCompleteMessagingFlow() {
        logger.info("Starting complete messaging flow test");
        
        // Navigate to messages
        MessagePage messagePage = homePage.navigateToMessages();
        Assert.assertTrue(messagePage.isDisplayed(), "Message page should be displayed");
        
        // If conversations exist, open one
        if (messagePage.getVisibleConversationsCount() > 0) {
            ConversationPage conversationPage = messagePage.openConversation("test_conversation_1");
            Assert.assertTrue(conversationPage.isDisplayed(), "Conversation page should be displayed");
            
            // Navigate back to messages
            MessagePage returnedMessagePage = conversationPage.navigateBack();
            Assert.assertTrue(returnedMessagePage.isDisplayed(), "Should return to message page");
            
            // Navigate back to home
            HomePage returnedHomePage = returnedMessagePage.navigateBack();
            Assert.assertTrue(returnedHomePage.isDisplayed(), "Should return to home page");
        }
        
        logger.info("Complete messaging flow test completed successfully");
    }
    
    @Test(priority = 15, groups = {"navigation", "regression"}, description = "Verify complete friends flow navigation")
    public void testCompleteFriendsFlow() {
        logger.info("Starting complete friends flow test");
        
        // Navigate to friends
        FriendsPage friendsPage = homePage.navigateToFriends();
        Assert.assertTrue(friendsPage.isDisplayed(), "Friends page should be displayed");
        
        // Test tab navigation
        friendsPage.switchToRequestsTab();
        Assert.assertTrue(friendsPage.isTabActive("requests"), "Requests tab should be active");
        
        friendsPage.switchToSentRequestsTab();
        Assert.assertTrue(friendsPage.isTabActive("sent"), "Sent requests tab should be active");
        
        friendsPage.switchToFriendsTab();
        Assert.assertTrue(friendsPage.isTabActive("friends"), "Friends tab should be active");
        
        // Navigate back to home
        HomePage returnedHomePage = friendsPage.navigateBack();
        Assert.assertTrue(returnedHomePage.isDisplayed(), "Should return to home page");
        
        logger.info("Complete friends flow test completed successfully");
    }
    
    // ==================== NAVIGATION STATE TESTS ====================
    
    @Test(priority = 16, groups = {"navigation", "regression"}, description = "Verify navigation state persistence")
    public void testNavigationStatePersistence() {
        logger.info("Starting navigation state persistence test");
        
        // Navigate to profile and check state
        ProfilePage profilePage = homePage.navigateToProfile();
        String username = profilePage.getDisplayedUsername();
        
        // Navigate back and forth
        HomePage returnedHomePage = profilePage.navigateBack();
        ProfilePage profilePageAgain = returnedHomePage.navigateToProfile();
        
        // Verify state is maintained
        String usernameAgain = profilePageAgain.getDisplayedUsername();
        Assert.assertEquals(usernameAgain, username, "Username should be consistent across navigation");
        
        logger.info("Navigation state persistence test completed successfully");
    }
    
    @Test(priority = 17, groups = {"navigation", "regression"}, description = "Verify rapid navigation handling")
    public void testRapidNavigation() {
        logger.info("Starting rapid navigation test");
        
        // Perform rapid navigation between screens
        for (int i = 0; i < 3; i++) {
            ProfilePage profilePage = homePage.navigateToProfile();
            Assert.assertTrue(profilePage.isDisplayed(), "Profile page should handle rapid navigation");
            
            homePage = profilePage.navigateBack();
            Assert.assertTrue(homePage.isDisplayed(), "Home page should handle rapid navigation");
        }
        
        logger.info("Rapid navigation test completed successfully");
    }
    
    // ==================== ERROR HANDLING TESTS ====================
    
    @Test(priority = 18, groups = {"navigation", "regression"}, description = "Verify navigation error recovery")
    public void testNavigationErrorRecovery() {
        logger.info("Starting navigation error recovery test");
        
        // Test navigation when network is poor or app is under stress
        // This is a placeholder for error scenarios
        
        ProfilePage profilePage = homePage.navigateToProfile();
        Assert.assertTrue(profilePage.isDisplayed(), "Should handle navigation even under stress");
        
        HomePage recoveredHomePage = profilePage.navigateBack();
        Assert.assertTrue(recoveredHomePage.isDisplayed(), "Should recover from navigation issues");
        
        logger.info("Navigation error recovery test completed successfully");
    }
    
    // ==================== ACCESSIBILITY NAVIGATION TESTS ====================
    
    @Test(priority = 19, groups = {"navigation", "regression"}, description = "Verify accessibility navigation")
    public void testAccessibilityNavigation() {
        logger.info("Starting accessibility navigation test");
        
        // Verify all navigation elements have proper accessibility IDs
        Assert.assertTrue(homePage.isAvatarButtonDisplayed(), "Avatar button should be accessible");
        Assert.assertTrue(homePage.isFriendsButtonDisplayed(), "Friends button should be accessible");
        Assert.assertTrue(homePage.isMessageButtonDisplayed(), "Message button should be accessible");
        
        logger.info("Accessibility navigation test completed successfully");
    }
    
    @Test(priority = 20, groups = {"navigation", "regression"}, description = "Verify navigation performance")
    public void testNavigationPerformance() {
        logger.info("Starting navigation performance test");
        
        long startTime = System.currentTimeMillis();
        
        // Measure navigation time
        ProfilePage profilePage = homePage.navigateToProfile();
        long navigationTime = System.currentTimeMillis() - startTime;
        
        Assert.assertTrue(profilePage.isDisplayed(), "Navigation should complete successfully");
        Assert.assertTrue(navigationTime < AppConstants.MAX_SCREEN_LOAD_TIME_MS, 
            "Navigation should complete within acceptable time: " + navigationTime + "ms");
        
        logger.info("Navigation completed in {}ms", navigationTime);
        logger.info("Navigation performance test completed successfully");
    }
}
