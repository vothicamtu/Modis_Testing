package com.modis.tests;

import com.modis.base.BaseTest;
import com.modis.base.BasePage;
import com.modis.constants.AppConstants;
import com.modis.pages.*;
import com.modis.utils.TestDataReader;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import java.util.Map;
import java.util.List;

/**
 * Test class for Camera functionality with real data
 * Covers camera operations, photo capture, and photo sending using actual photo data
 */
public class CameraTests extends BaseTest {
    
    private HomePage homePage;
    private TestDataReader testDataReader = new TestDataReader();
    
    // ==================== DATA PROVIDERS ====================
    
    @DataProvider(name = "testPhotosData")
    public Object[][] getTestPhotosData() {
        List<Map<String, Object>> photos = testDataReader.getTestPhotos();
        Object[][] data = new Object[Math.min(photos.size(), 3)][];
        
        for (int i = 0; i < Math.min(photos.size(), 3); i++) {
            Map<String, Object> photo = photos.get(i);
            data[i] = new Object[]{
                photo.get("id"),
                photo.get("senderUsername"),
                photo.get("caption"),
                photo.get("recipientUsernames")
            };
        }
        return data;
    }
    
    @DataProvider(name = "photoScenariosData")
    public Object[][] getPhotoScenariosData() {
        Map<String, Map<String, Object>> scenarios = testDataReader.getPhotoTestScenarios();
        Object[][] data = new Object[scenarios.size()][];
        
        int i = 0;
        for (Map.Entry<String, Map<String, Object>> entry : scenarios.entrySet()) {
            Map<String, Object> scenario = entry.getValue();
            data[i] = new Object[]{
                entry.getKey(),
                scenario.get("description"),
                scenario.get("caption"),
                scenario.get("recipients"),
                scenario.get("expectedResult")
            };
            i++;
        }
        return data;
    }
    
    @BeforeMethod(groups = {"camera", "regression"})
    public void loginBeforeTest() {
        logger.info("Logging in before camera test with real user data");
        
        // Use real user data for login
        Map<String, Object> testUser = testDataReader.getRandomValidUser();
        String username = (String) testUser.get("username");
        String password = (String) testUser.get("password");
        
        LoadingPage loadingPage = new LoadingPage();
        LoginPage loginPage = loadingPage.clickLoginButton();
        BasePage afterLogin = loginPage.login(username, password);
        Assert.assertTrue(afterLogin instanceof HomePage,
                "Login should navigate to HomePage, but got: " + (afterLogin != null ? afterLogin.getClass().getSimpleName() : "null"));
        homePage = (HomePage) afterLogin;
        homePage.waitForTopbarReadyAfterLogin(8);
        
        Assert.assertTrue(homePage.isDisplayed(), "Should be logged in before camera tests");
        logger.info("Logged in successfully with user: " + username);
    }
    
    // ==================== PHOTO DATA TESTS ====================
    
    @Test(priority = 1, groups = {"camera", "regression", "data"}, 
          dataProvider = "testPhotosData", description = "Verify photo data from real database")
    public void testPhotosWithRealData(String photoId, String senderUsername, String caption, List<String> recipients) {
        logger.info("Testing photo data - Sender: " + senderUsername + " Caption: " + caption);
        
        TakePage takePage = homePage.navigateToCamera();
        
        // Test photo capture with real caption
        if (caption != null && !caption.trim().isEmpty()) {
            // Simulate taking a photo
            takePage.capturePhoto();
            
            // Add caption from real data
            takePage.addCaption(caption);
            
            // Select recipients from real data
            if (recipients != null && !recipients.isEmpty()) {
                for (String recipient : recipients) {
                    if (takePage.isRecipientAvailable(recipient)) {
                        takePage.selectRecipient(recipient);
                    }
                }
            }
            
            // Send photo
            takePage.sendPhoto();
            
            // Verify photo was sent successfully
            Assert.assertTrue(takePage.isPhotoSentSuccessfully(), 
                "Photo should be sent successfully with real data");
        }
        
        logger.info("Photo data test completed for sender: " + senderUsername);
    }
    
    @Test(priority = 2, groups = {"camera", "regression", "data"}, 
          dataProvider = "photoScenariosData", description = "Verify photo scenarios with real data")
    public void testPhotoScenariosWithRealData(String scenarioName, String description, String caption, 
                                             List<String> recipients, String expectedResult) {
        logger.info("Testing photo scenario: " + scenarioName + " - " + description);
        
        TakePage takePage = homePage.navigateToCamera();
        
        // Execute scenario based on real data
        takePage.capturePhoto();
        
        if (caption != null && !caption.trim().isEmpty()) {
            takePage.addCaption(caption);
        }
        
        if (recipients != null && !recipients.isEmpty()) {
            for (String recipient : recipients) {
                if (takePage.isRecipientAvailable(recipient)) {
                    takePage.selectRecipient(recipient);
                }
            }
        }
        
        // Verify expected result
        if ("photo_sent_successfully".equals(expectedResult)) {
            takePage.sendPhoto();
            Assert.assertTrue(takePage.isPhotoSentSuccessfully(), 
                "Photo should be sent successfully for scenario: " + scenarioName);
        } else if ("send_button_disabled".equals(expectedResult)) {
            Assert.assertFalse(takePage.isSendButtonEnabled(), 
                "Send button should be disabled for scenario: " + scenarioName);
        }
        
        logger.info("Photo scenario test completed: " + scenarioName);
    }
    
    // ==================== NAVIGATION TESTS ====================
    
    @Test(priority = 3, groups = {"camera", "smoke"}, 
          description = "Verify navigation to camera screen")
    public void testNavigateToCamera() {
        logger.info("Starting navigate to camera test");
        
        TakePage takePage = homePage.navigateToCamera();
        
        Assert.assertTrue(takePage.isDisplayed(), "Camera page should be displayed");
        Assert.assertTrue(takePage.verifyPageElements(), "Camera page elements should be present");
        
        logger.info("Navigate to camera test completed successfully");
    }
    
    @Test(priority = 4, groups = {"camera", "navigation"}, 
          description = "Verify back navigation from camera screen")
    public void testBackNavigationFromCamera() {
        logger.info("Starting back navigation from camera test");
        
        TakePage takePage = homePage.navigateToCamera();
        HomePage returnedHomePage = takePage.navigateBack();
        
        Assert.assertTrue(returnedHomePage.isDisplayed(), 
            "Should return to home page after back navigation");
        Assert.assertTrue(returnedHomePage.verifyPageElements(), 
            "Home page elements should be present after back navigation");
        
        logger.info("Back navigation from camera test completed successfully");
    }
    
    // ==================== CAMERA UI TESTS ====================
    
    @Test(priority = 3, groups = {"camera", "regression"}, 
          description = "Verify camera UI elements")
    public void testCameraUIElements() {
        logger.info("Starting camera UI elements test");
        
        TakePage takePage = homePage.navigateToCamera();
        
        // Verify camera preview is displayed
        Assert.assertTrue(takePage.isCameraPreviewDisplayed(), 
            "Camera preview should be displayed");
        
        // Verify capture button is displayed
        Assert.assertTrue(takePage.isCaptureButtonDisplayed(), 
            "Capture button should be displayed");
        
        // Verify flash button is displayed
        Assert.assertTrue(takePage.isFlashButtonDisplayed(), 
            "Flash button should be displayed");
        
        // Verify camera toggle button is displayed
        Assert.assertTrue(takePage.isToggleCameraButtonDisplayed(), 
            "Camera toggle button should be displayed");
        
        // Verify history button is displayed (if available)
        if (takePage.isHistoryButtonDisplayed()) {
            logger.info("History button is available");
        }
        
        logger.info("Camera UI elements test completed successfully");
    }
    
    @Test(priority = 4, groups = {"camera", "regression"}, 
          description = "Verify camera permissions")
    public void testCameraPermissions() {
        logger.info("Starting camera permissions test");
        
        TakePage takePage = homePage.navigateToCamera();
        
        // Check if camera permission dialog appears
        if (takePage.isCameraPermissionDialogDisplayed()) {
            takePage.allowCameraPermission();
            
            // Verify camera preview is now available
            Assert.assertTrue(takePage.isCameraPreviewDisplayed(), 
                "Camera preview should be displayed after granting permission");
        } else {
            // Permission already granted
            Assert.assertTrue(takePage.isCameraPreviewDisplayed(), 
                "Camera preview should be displayed when permission is already granted");
        }
        
        logger.info("Camera permissions test completed successfully");
    }
    
    // ==================== FLASH FUNCTIONALITY TESTS ====================
    
    @Test(priority = 5, groups = {"camera", "regression"}, 
          description = "Verify flash toggle functionality")
    public void testFlashToggle() {
        logger.info("Starting flash toggle test");
        
        TakePage takePage = homePage.navigateToCamera();
        
        // Get initial flash state
        boolean initialFlashState = takePage.isFlashEnabled();
        
        // Toggle flash
        takePage.toggleFlash();
        
        // Verify flash state changed
        boolean newFlashState = takePage.isFlashEnabled();
        Assert.assertNotEquals(newFlashState, initialFlashState, 
            "Flash state should change after toggle");
        
        // Verify flash button UI reflects the change
        Assert.assertTrue(takePage.isFlashButtonStateCorrect(), 
            "Flash button UI should reflect current flash state");
        
        // Toggle back to original state
        takePage.toggleFlash();
        boolean finalFlashState = takePage.isFlashEnabled();
        Assert.assertEquals(finalFlashState, initialFlashState, 
            "Flash should return to original state after second toggle");
        
        logger.info("Flash toggle test completed successfully");
    }
    
    // ==================== CAMERA SWITCHING TESTS ====================
    
    @Test(priority = 6, groups = {"camera", "regression"}, 
          description = "Verify camera switching functionality")
    public void testCameraSwitching() {
        logger.info("Starting camera switching test");
        
        TakePage takePage = homePage.navigateToCamera();
        
        // Check if device has multiple cameras
        if (takePage.hasMultipleCameras()) {
            // Get initial camera state (front/back)
            boolean initialCameraState = takePage.isFrontCameraActive();
            
            // Switch camera
            takePage.toggleCamera();
            
            // Wait for camera switch to complete
            takePage.waitForCameraSwitch();
            
            // Verify camera switched
            boolean newCameraState = takePage.isFrontCameraActive();
            Assert.assertNotEquals(newCameraState, initialCameraState, 
                "Camera should switch between front and back");
            
            // Verify camera preview is still working
            Assert.assertTrue(takePage.isCameraPreviewDisplayed(), 
                "Camera preview should still be displayed after switching");
            
            // Switch back to original camera
            takePage.toggleCamera();
            takePage.waitForCameraSwitch();
            
            boolean finalCameraState = takePage.isFrontCameraActive();
            Assert.assertEquals(finalCameraState, initialCameraState, 
                "Camera should return to original state");
        } else {
            logger.info("Device has only one camera - skipping camera switch test");
        }
        
        logger.info("Camera switching test completed successfully");
    }
    
    // ==================== PHOTO CAPTURE TESTS ====================
    
    @Test(priority = 7, groups = {"camera", "regression"}, 
          description = "Verify photo capture functionality")
    public void testPhotoCapture() {
        logger.info("Starting photo capture test");
        
        TakePage takePage = homePage.navigateToCamera();
        
        // Ensure camera is ready
        Assert.assertTrue(takePage.isCameraPreviewDisplayed(), 
            "Camera preview should be displayed before capture");
        
        // Capture photo
        SendPhotoPage sendPhotoPage = takePage.capturePhoto();
        
        // Verify navigation to send photo screen
        Assert.assertTrue(sendPhotoPage.isDisplayed(), 
            "Send photo page should be displayed after capture");
        Assert.assertTrue(sendPhotoPage.verifyPageElements(), 
            "Send photo page elements should be present");
        
        // Verify captured photo is displayed
        Assert.assertTrue(sendPhotoPage.isCapturedPhotoDisplayed(), 
            "Captured photo should be displayed on send photo page");
        
        logger.info("Photo capture test completed successfully");
    }
    
    @Test(priority = 8, groups = {"camera", "regression"}, 
          description = "Verify multiple photo captures")
    public void testMultiplePhotoCaptures() {
        logger.info("Starting multiple photo captures test");
        
        TakePage takePage = homePage.navigateToCamera();
        
        // Capture first photo
        SendPhotoPage sendPhotoPage1 = takePage.capturePhoto();
        Assert.assertTrue(sendPhotoPage1.isDisplayed(), 
            "Send photo page should be displayed after first capture");
        
        // Go back to camera
        TakePage returnedTakePage = sendPhotoPage1.navigateBack();
        Assert.assertTrue(returnedTakePage.isDisplayed(), 
            "Should return to camera page");
        
        // Capture second photo
        SendPhotoPage sendPhotoPage2 = returnedTakePage.capturePhoto();
        Assert.assertTrue(sendPhotoPage2.isDisplayed(), 
            "Send photo page should be displayed after second capture");
        
        // Verify both captures worked
        Assert.assertTrue(sendPhotoPage2.isCapturedPhotoDisplayed(), 
            "Second captured photo should be displayed");
        
        logger.info("Multiple photo captures test completed successfully");
    }
    
    @Test(priority = 9, groups = {"camera", "regression"}, 
          description = "Verify photo capture with flash")
    public void testPhotoCaptureWithFlash() {
        logger.info("Starting photo capture with flash test");
        
        TakePage takePage = homePage.navigateToCamera();
        
        // Enable flash
        if (!takePage.isFlashEnabled()) {
            takePage.toggleFlash();
        }
        
        Assert.assertTrue(takePage.isFlashEnabled(), 
            "Flash should be enabled before capture");
        
        // Capture photo with flash
        SendPhotoPage sendPhotoPage = takePage.capturePhoto();
        
        // Verify capture was successful
        Assert.assertTrue(sendPhotoPage.isDisplayed(), 
            "Send photo page should be displayed after flash capture");
        Assert.assertTrue(sendPhotoPage.isCapturedPhotoDisplayed(), 
            "Photo captured with flash should be displayed");
        
        logger.info("Photo capture with flash test completed successfully");
    }
    
    @Test(priority = 10, groups = {"camera", "regression"}, 
          description = "Verify photo capture with front camera")
    public void testPhotoCaptureWithFrontCamera() {
        logger.info("Starting photo capture with front camera test");
        
        TakePage takePage = homePage.navigateToCamera();
        
        // Switch to front camera if available
        if (takePage.hasMultipleCameras() && !takePage.isFrontCameraActive()) {
            takePage.toggleCamera();
            takePage.waitForCameraSwitch();
        }
        
        if (takePage.isFrontCameraActive()) {
            // Capture photo with front camera
            SendPhotoPage sendPhotoPage = takePage.capturePhoto();
            
            // Verify capture was successful
            Assert.assertTrue(sendPhotoPage.isDisplayed(), 
                "Send photo page should be displayed after front camera capture");
            Assert.assertTrue(sendPhotoPage.isCapturedPhotoDisplayed(), 
                "Photo captured with front camera should be displayed");
        } else {
            logger.info("Front camera not available - skipping front camera capture test");
        }
        
        logger.info("Photo capture with front camera test completed successfully");
    }
    
    // ==================== CAMERA HISTORY TESTS ====================
    
    @Test(priority = 11, groups = {"camera", "regression"}, 
          description = "Verify camera history functionality")
    public void testCameraHistory() {
        logger.info("Starting camera history test");
        
        TakePage takePage = homePage.navigateToCamera();
        
        if (takePage.isHistoryButtonDisplayed()) {
            AllImagesPage allImagesPage = takePage.openHistory();
            
            // Verify navigation to all images page
            Assert.assertTrue(allImagesPage.isDisplayed(), 
                "All images page should be displayed after opening history");
            Assert.assertTrue(allImagesPage.verifyPageElements(), 
                "All images page elements should be present");
            
            // Check if images are displayed
            if (allImagesPage.hasImages()) {
                Assert.assertTrue(allImagesPage.areImagesDisplayed(), 
                    "Images should be displayed in history");
                Assert.assertTrue(allImagesPage.getImageCount() > 0, 
                    "Should have at least one image in history");
            } else {
                Assert.assertTrue(allImagesPage.isEmptyStateDisplayed(), 
                    "Empty state should be displayed when no images exist");
            }
            
            // Navigate back to camera
            TakePage returnedTakePage = allImagesPage.navigateBack();
            Assert.assertTrue(returnedTakePage.isDisplayed(), 
                "Should return to camera page from history");
        } else {
            logger.info("History button not available - skipping history test");
        }
        
        logger.info("Camera history test completed successfully");
    }
    
    // ==================== SEND PHOTO TESTS ====================
    
    @Test(priority = 12, groups = {"camera", "regression"}, 
          description = "Verify send photo screen elements")
    public void testSendPhotoScreenElements() {
        logger.info("Starting send photo screen elements test");
        
        TakePage takePage = homePage.navigateToCamera();
        SendPhotoPage sendPhotoPage = takePage.capturePhoto();
        
        // Verify photo preview
        Assert.assertTrue(sendPhotoPage.isCapturedPhotoDisplayed(), 
            "Captured photo should be displayed");
        
        // Verify caption input
        Assert.assertTrue(sendPhotoPage.isCaptionInputDisplayed(), 
            "Caption input should be displayed");
        
        // Verify friends list
        Assert.assertTrue(sendPhotoPage.isFriendsListDisplayed(), 
            "Friends list should be displayed");
        
        // Verify send button
        Assert.assertTrue(sendPhotoPage.isSendButtonDisplayed(), 
            "Send button should be displayed");
        
        // Verify close button
        Assert.assertTrue(sendPhotoPage.isCloseButtonDisplayed(), 
            "Close button should be displayed");
        
        logger.info("Send photo screen elements test completed successfully");
    }
    
    @Test(priority = 13, groups = {"camera", "regression"}, 
          description = "Verify photo caption functionality")
    public void testPhotoCaptionFunctionality() {
        logger.info("Starting photo caption functionality test");
        
        TakePage takePage = homePage.navigateToCamera();
        SendPhotoPage sendPhotoPage = takePage.capturePhoto();
        
        // Test caption input
        String testCaption = "Test photo caption " + System.currentTimeMillis();
        sendPhotoPage.enterCaption(testCaption);
        
        // Verify caption was entered
        Assert.assertEquals(sendPhotoPage.getCaptionText(), testCaption, 
            "Caption text should match entered text");
        
        // Test caption clearing
        sendPhotoPage.clearCaption();
        Assert.assertTrue(sendPhotoPage.getCaptionText().isEmpty(), 
            "Caption should be empty after clearing");
        
        // Test long caption
        String longCaption = "This is a very long caption that exceeds normal length. ".repeat(5);
        sendPhotoPage.enterCaption(longCaption);
        
        // Verify long caption handling (may be truncated or allowed)
        String actualCaption = sendPhotoPage.getCaptionText();
        Assert.assertFalse(actualCaption.isEmpty(), 
            "Long caption should be handled appropriately");
        
        logger.info("Photo caption functionality test completed successfully");
    }
    
    @Test(priority = 14, groups = {"camera", "regression"}, 
          description = "Verify friend selection for photo sending")
    public void testFriendSelectionForPhoto() {
        logger.info("Starting friend selection for photo test");
        
        TakePage takePage = homePage.navigateToCamera();
        SendPhotoPage sendPhotoPage = takePage.capturePhoto();
        
        if (sendPhotoPage.hasFriends()) {
            // Test selecting individual friends
            String firstFriendId = sendPhotoPage.getFirstFriendId();
            sendPhotoPage.selectFriend(firstFriendId);
            
            Assert.assertTrue(sendPhotoPage.isFriendSelected(firstFriendId), 
                "Friend should be selected after clicking");
            
            // Test deselecting friend
            sendPhotoPage.deselectFriend(firstFriendId);
            Assert.assertFalse(sendPhotoPage.isFriendSelected(firstFriendId), 
                "Friend should be deselected after clicking again");
            
            // Test select all functionality (if available)
            if (sendPhotoPage.isSelectAllButtonDisplayed()) {
                sendPhotoPage.selectAllFriends();
                Assert.assertTrue(sendPhotoPage.areAllFriendsSelected(), 
                    "All friends should be selected after select all");
                
                sendPhotoPage.deselectAllFriends();
                Assert.assertFalse(sendPhotoPage.areAnyFriendsSelected(), 
                    "No friends should be selected after deselect all");
            }
        } else {
            logger.info("No friends available for photo sending test");
        }
        
        logger.info("Friend selection for photo test completed successfully");
    }
    
    @Test(priority = 15, groups = {"camera", "regression"}, 
          description = "Verify photo sending functionality")
    public void testPhotoSending() {
        logger.info("Starting photo sending test");
        
        TakePage takePage = homePage.navigateToCamera();
        SendPhotoPage sendPhotoPage = takePage.capturePhoto();
        
        if (sendPhotoPage.hasFriends()) {
            // Add caption
            String caption = "Test photo " + System.currentTimeMillis();
            sendPhotoPage.enterCaption(caption);
            
            // Select at least one friend
            String firstFriendId = sendPhotoPage.getFirstFriendId();
            sendPhotoPage.selectFriend(firstFriendId);
            
            // Send photo
            HomePage homePage = sendPhotoPage.sendPhoto();
            
            // Verify return to home page
            Assert.assertTrue(homePage.isDisplayed(), 
                "Should return to home page after sending photo");
            
            // Verify photo was sent (may appear in feed or show success message)
            if (homePage.isPhotoSentSuccessMessageDisplayed()) {
                logger.info("Photo sent successfully with success message");
            } else {
                logger.info("Photo sent - no explicit success message displayed");
            }
        } else {
            logger.info("No friends available for photo sending test");
        }
        
        logger.info("Photo sending test completed successfully");
    }
    
    @Test(priority = 16, groups = {"camera", "regression"}, 
          description = "Verify photo sending validation")
    public void testPhotoSendingValidation() {
        logger.info("Starting photo sending validation test");
        
        TakePage takePage = homePage.navigateToCamera();
        SendPhotoPage sendPhotoPage = takePage.capturePhoto();
        
        // Test sending without selecting friends
        Assert.assertFalse(sendPhotoPage.isSendButtonEnabled(), 
            "Send button should be disabled when no friends are selected");
        
        if (sendPhotoPage.hasFriends()) {
            // Select a friend
            String firstFriendId = sendPhotoPage.getFirstFriendId();
            sendPhotoPage.selectFriend(firstFriendId);
            
            // Now send button should be enabled
            Assert.assertTrue(sendPhotoPage.isSendButtonEnabled(), 
                "Send button should be enabled when friends are selected");
        }
        
        logger.info("Photo sending validation test completed successfully");
    }
    
    // ==================== ERROR HANDLING TESTS ====================
    
    @Test(priority = 17, groups = {"camera", "regression"}, 
          description = "Verify camera error handling")
    public void testCameraErrorHandling() {
        logger.info("Starting camera error handling test");
        
        TakePage takePage = homePage.navigateToCamera();
        
        // Simulate app backgrounding during camera use
        backgroundApp(2);
        
        // Verify camera recovers properly
        Assert.assertTrue(takePage.isDisplayed(), 
            "Camera page should remain stable after backgrounding");
        
        if (takePage.isCameraPreviewDisplayed()) {
            logger.info("Camera preview recovered successfully");
        } else {
            logger.info("Camera preview may need reinitialization after backgrounding");
        }
        
        // Test capture after potential error
        try {
            SendPhotoPage sendPhotoPage = takePage.capturePhoto();
            Assert.assertTrue(sendPhotoPage.isDisplayed(), 
                "Photo capture should work after recovery");
        } catch (Exception e) {
            logger.warn("Photo capture failed after backgrounding: {}", e.getMessage());
        }
        
        logger.info("Camera error handling test completed successfully");
    }
    
    @Test(priority = 18, groups = {"camera", "regression"}, 
          description = "Verify camera performance")
    public void testCameraPerformance() {
        logger.info("Starting camera performance test");
        
        TakePage takePage = homePage.navigateToCamera();
        
        // Measure camera initialization time
        long startTime = System.currentTimeMillis();
        Assert.assertTrue(takePage.isCameraPreviewDisplayed(), 
            "Camera preview should be displayed");
        long initTime = System.currentTimeMillis() - startTime;
        
        logger.info("Camera initialization time: {} ms", initTime);
        
        // Test rapid captures (if supported)
        for (int i = 0; i < 3; i++) {
            startTime = System.currentTimeMillis();
            SendPhotoPage sendPhotoPage = takePage.capturePhoto();
            long captureTime = System.currentTimeMillis() - startTime;
            
            logger.info("Photo capture {} time: {} ms", i + 1, captureTime);
            
            Assert.assertTrue(sendPhotoPage.isDisplayed(), 
                "Send photo page should be displayed for capture " + (i + 1));
            
            // Return to camera for next capture
            if (i < 2) {
                takePage = sendPhotoPage.navigateBack();
            }
        }
        
        logger.info("Camera performance test completed successfully");
    }
}
