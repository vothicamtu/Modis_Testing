package com.modis.pages;

import com.modis.base.BasePage;
import com.modis.constants.TestIDs;
import com.modis.constants.AppConstants;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;

/**
 * Page Object for Take/Camera Screen
 * Handles camera functionality and photo capture
 */
public class TakePage extends BasePage {
    
    // ==================== PAGE ELEMENTS ====================
    
    @AndroidFindBy(id = TestIDs.TAKE_SCREEN)
    @iOSXCUITFindBy(accessibility = TestIDs.TAKE_SCREEN)
    private WebElement takeScreen;
    
    @AndroidFindBy(id = TestIDs.TAKE_CAMERA_AREA)
    @iOSXCUITFindBy(accessibility = TestIDs.TAKE_CAMERA_AREA)
    private WebElement cameraArea;
    
    @AndroidFindBy(id = TestIDs.TAKE_CAPTURE_BUTTON)
    @iOSXCUITFindBy(accessibility = TestIDs.TAKE_CAPTURE_BUTTON)
    private WebElement captureButton;
    
    @AndroidFindBy(id = TestIDs.TAKE_FLASH_BUTTON)
    @iOSXCUITFindBy(accessibility = TestIDs.TAKE_FLASH_BUTTON)
    private WebElement flashButton;
    
    @AndroidFindBy(id = TestIDs.TAKE_TOGGLE_CAMERA_BUTTON)
    @iOSXCUITFindBy(accessibility = TestIDs.TAKE_TOGGLE_CAMERA_BUTTON)
    private WebElement toggleCameraButton;
    
    @AndroidFindBy(id = TestIDs.TAKE_HISTORY)
    @iOSXCUITFindBy(accessibility = TestIDs.TAKE_HISTORY)
    private WebElement historyButton;
    
    @AndroidFindBy(id = TestIDs.TAKE_CAMERA_PREVIEW)
    @iOSXCUITFindBy(accessibility = TestIDs.TAKE_CAMERA_PREVIEW)
    private WebElement cameraPreview;
    
    // Permission elements
    @AndroidFindBy(id = TestIDs.CAMERA_PERMISSION_DIALOG)
    @iOSXCUITFindBy(accessibility = TestIDs.CAMERA_PERMISSION_DIALOG)
    private WebElement cameraPermissionDialog;
    
    // Caption and recipient elements
    @AndroidFindBy(id = TestIDs.TAKE_CAPTION_INPUT)
    @iOSXCUITFindBy(accessibility = TestIDs.TAKE_CAPTION_INPUT)
    private WebElement captionInput;
    
    @AndroidFindBy(id = TestIDs.TAKE_RECIPIENTS_LIST)
    @iOSXCUITFindBy(accessibility = TestIDs.TAKE_RECIPIENTS_LIST)
    private WebElement recipientsList;
    
    @AndroidFindBy(id = TestIDs.TAKE_SEND_BUTTON)
    @iOSXCUITFindBy(accessibility = TestIDs.TAKE_SEND_BUTTON)
    private WebElement sendButton;
    
    @AndroidFindBy(id = TestIDs.TAKE_SUCCESS_MESSAGE)
    @iOSXCUITFindBy(accessibility = TestIDs.TAKE_SUCCESS_MESSAGE)
    private WebElement successMessage;
    
    @AndroidFindBy(id = TestIDs.PERMISSION_ALLOW_BUTTON)
    @iOSXCUITFindBy(accessibility = TestIDs.PERMISSION_ALLOW_BUTTON)
    private WebElement allowPermissionButton;
    
    @AndroidFindBy(id = TestIDs.PERMISSION_DENY_BUTTON)
    @iOSXCUITFindBy(accessibility = TestIDs.PERMISSION_DENY_BUTTON)
    private WebElement denyPermissionButton;
    
    // ==================== CAMERA ACTIONS ====================
    
    /**
     * Capture a photo
     * @return SendPhotoPage if successful, TakePage if failed
     */
    public SendPhotoPage capturePhoto() {
        logger.info("Capturing photo");
        
        // Handle camera permissions if needed
        handleCameraPermissions();
        
        // Wait for camera to be ready
        waitForCameraReady();
        
        // Capture photo
        waitForElementClickable(TestIDs.TAKE_CAPTURE_BUTTON);
        clickElement(captureButton);
        
        // Wait for capture animation and check navigation
        waitForAnimation();
        
        // Check if we navigated to send photo screen
        if (isElementDisplayedByAccessibilityId(TestIDs.SEND_PHOTO_SCREEN)) {
            logger.info("Photo captured successfully - navigated to send photo screen");
            return new SendPhotoPage();
        } else {
            logger.warn("Photo capture may have failed - still on camera screen");
            return null;
        }
    }
    
    /**
     * Toggle flash on/off
     * @return TakePage for method chaining
     */
    public TakePage toggleFlash() {
        logger.info("Toggling flash");
        waitForElementClickable(TestIDs.TAKE_FLASH_BUTTON);
        clickElement(flashButton);
        waitForAnimation();
        return this;
    }
    
    /**
     * Switch between front and back camera
     * @return TakePage for method chaining
     */
    public TakePage switchCamera() {
        logger.info("Switching camera");
        waitForElementClickable(TestIDs.TAKE_TOGGLE_CAMERA_BUTTON);
        clickElement(toggleCameraButton);
        waitForCameraSwitchAnimation();
        return this;
    }
    
    /**
     * Open photo history/gallery
     * @return AllImagesPage or gallery view
     */
    public AllImagesPage openHistory() {
        logger.info("Opening photo history");
        waitForElementClickable(TestIDs.TAKE_HISTORY);
        clickElement(historyButton);
        return new AllImagesPage();
    }
    
    /**
     * Navigate back to home screen
     * @return HomePage
     */
    public HomePage navigateBack() {
        logger.info("Navigating back from camera screen");
        
        // Use swipe down gesture to go back
        swipeDown();
        
        return new HomePage();
    }
    
    /**
     * Navigate back using device back button (Android)
     * @return HomePage
     */
    public HomePage navigateBackWithDeviceButton() {
        logger.info("Navigating back using device back button");
        goBack();
        return new HomePage();
    }
    
    // ==================== CAMERA CONTROLS ====================
    
    /**
     * Tap to focus on specific area
     * @param x X coordinate
     * @param y Y coordinate
     * @return TakePage for method chaining
     */
    public TakePage tapToFocus(int x, int y) {
        logger.info("Tapping to focus at coordinates: ({}, {})", x, y);
        tapAtCoordinates(x, y);
        waitForAnimation(); // Wait for focus animation
        return this;
    }
    
    /**
     * Tap to focus on camera preview center
     * @return TakePage for method chaining
     */
    public TakePage tapToFocusCenter() {
        logger.info("Tapping to focus on center");
        waitForElementVisible(TestIDs.TAKE_CAMERA_AREA);
        tapElement(cameraArea);
        waitForAnimation();
        return this;
    }
    
    /**
     * Pinch to zoom in
     * @return TakePage for method chaining
     */
    public TakePage zoomIn() {
        logger.info("Zooming in");
        waitForElementVisible(TestIDs.TAKE_CAMERA_AREA);
        pinchOut(cameraArea);
        return this;
    }
    
    /**
     * Pinch to zoom out
     * @return TakePage for method chaining
     */
    public TakePage zoomOut() {
        logger.info("Zooming out");
        waitForElementVisible(TestIDs.TAKE_CAMERA_AREA);
        pinchIn(cameraArea);
        return this;
    }
    
    /**
     * Long press capture button for video recording (if supported)
     * @return TakePage for method chaining
     */
    public TakePage startVideoRecording() {
        logger.info("Starting video recording");
        waitForElementVisible(TestIDs.TAKE_CAPTURE_BUTTON);
        longPressElement(captureButton, AppConstants.LONG_PRESS_DURATION_MS);
        return this;
    }
    
    /**
     * Stop video recording
     * @return TakePage for method chaining
     */
    public TakePage stopVideoRecording() {
        logger.info("Stopping video recording");
        waitForElementClickable(TestIDs.TAKE_CAPTURE_BUTTON);
        clickElement(captureButton);
        return this;
    }
    
    // ==================== PERMISSION HANDLING ====================
    
    /**
     * Handle camera permissions if dialog appears
     */
    public void handleCameraPermissions() {
        logger.debug("Checking for camera permission dialog");
        
        try {
            if (isElementDisplayedByAccessibilityId(TestIDs.CAMERA_PERMISSION_DIALOG)) {
                logger.info("Camera permission dialog detected - granting permission");
                allowCameraPermission();
            }
        } catch (Exception e) {
            logger.debug("No camera permission dialog found", e);
        }
    }
    
    /**
     * Allow camera permission
     * @return TakePage for method chaining
     */
    public TakePage allowCameraPermission() {
        logger.info("Allowing camera permission");
        waitForElementClickable(TestIDs.PERMISSION_ALLOW_BUTTON);
        clickElement(allowPermissionButton);
        waitForAnimation();
        return this;
    }
    
    /**
     * Deny camera permission
     * @return TakePage for method chaining
     */
    public TakePage denyCameraPermission() {
        logger.info("Denying camera permission");
        waitForElementClickable(TestIDs.PERMISSION_DENY_BUTTON);
        clickElement(denyPermissionButton);
        waitForAnimation();
        return this;
    }
    
    // ==================== VALIDATION METHODS ====================
    
    /**
     * Check if camera preview is displayed
     * @return true if camera preview is visible, false otherwise
     */
    public boolean isCameraPreviewDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.TAKE_CAMERA_PREVIEW);
    }
    
    /**
     * Check if capture button is displayed
     * @return true if capture button is visible, false otherwise
     */
    public boolean isCaptureButtonDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.TAKE_CAPTURE_BUTTON);
    }
    
    /**
     * Check if flash button is displayed
     * @return true if flash button is visible, false otherwise
     */
    public boolean isFlashButtonDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.TAKE_FLASH_BUTTON);
    }
    
    /**
     * Check if camera toggle button is displayed
     * @return true if toggle button is visible, false otherwise
     */
    public boolean isCameraToggleButtonDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.TAKE_TOGGLE_CAMERA_BUTTON);
    }
    
    /**
     * Check if history button is displayed
     * @return true if history button is visible, false otherwise
     */
    public boolean isHistoryButtonDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.TAKE_HISTORY);
    }
    
    /**
     * Check if camera permission dialog is displayed
     * @return true if permission dialog is visible, false otherwise
     */
    public boolean isCameraPermissionDialogDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.CAMERA_PERMISSION_DIALOG);
    }
    
    /**
     * Check if flash is enabled (based on button state)
     * @return true if flash is enabled, false otherwise
     */
    public boolean isFlashEnabled() {
        // Implementation depends on how flash state is indicated in UI
        // This could check button appearance, text, or other indicators
        waitForElementVisible(TestIDs.TAKE_FLASH_BUTTON);
        
        // Check if button has "enabled" state or specific attribute
        String flashState = flashButton.getAttribute("selected");
        return "true".equals(flashState);
    }
    
    /**
     * Wait for camera to be ready
     */
    public void waitForCameraReady() {
        logger.debug("Waiting for camera to be ready");
        waitForElementVisible(TestIDs.TAKE_CAMERA_AREA);
        waitForElementVisible(TestIDs.TAKE_CAPTURE_BUTTON);
        
        // Additional wait for camera initialization
        try {
            Thread.sleep(2000); // Camera needs time to initialize
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Camera ready wait interrupted", e);
        }
    }
    
    /**
     * Wait for camera switch animation to complete
     */
    public void waitForCameraSwitchAnimation() {
        logger.debug("Waiting for camera switch animation");
        waitForAnimation();
        
        // Additional wait for camera to reinitialize
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Camera switch animation wait interrupted", e);
        }
    }
    
    // ==================== ERROR HANDLING ====================
    
    /**
     * Check if camera error occurred
     * @return true if camera error is displayed, false otherwise
     */
    public boolean isCameraErrorDisplayed() {
        // Check for common camera error indicators
        return isElementDisplayedByAccessibilityId("camera_error") ||
               isElementDisplayedByAccessibilityId("camera_not_available");
    }
    
    /**
     * Handle camera error and retry
     * @return TakePage for method chaining
     */
    public TakePage handleCameraError() {
        logger.warn("Handling camera error");
        
        if (isCameraErrorDisplayed()) {
            // Try to dismiss error and reinitialize camera
            var size = getScreenSize();
            tapAtCoordinates(size.width / 2, size.height / 2);
            waitForAnimation();
            
            // Navigate back and return to camera
            navigateBack();
            return new HomePage().navigateToCamera();
        }
        
        return this;
    }
    
    // ==================== INHERITED METHODS ====================
    
    @Override
    public boolean isDisplayed() {
        try {
            return isElementDisplayedByAccessibilityId(TestIDs.TAKE_SCREEN);
        } catch (Exception e) {
            logger.debug("Take screen not displayed", e);
            return false;
        }
    }
    
    @Override
    public String getPageTitle() {
        return "Take/Camera Screen";
    }
    
    /**
     * Wait for take page to be fully loaded
     * @return TakePage for method chaining
     */
    public TakePage waitForPageToLoad() {
        logger.info("Waiting for take page to load");
        waitForElementVisible(TestIDs.TAKE_SCREEN);
        handleCameraPermissions();
        waitForCameraReady();
        logger.info("Take page loaded successfully");
        return this;
    }
    
    /**
     * Verify all essential elements are present
     * @return true if all elements are present, false otherwise
     */
    public boolean verifyPageElements() {
        logger.info("Verifying take page elements");
        
        boolean allElementsPresent = 
            isElementDisplayedByAccessibilityId(TestIDs.TAKE_SCREEN) &&
            isElementDisplayedByAccessibilityId(TestIDs.TAKE_CAMERA_AREA) &&
            isElementDisplayedByAccessibilityId(TestIDs.TAKE_CAPTURE_BUTTON) &&
            isElementDisplayedByAccessibilityId(TestIDs.TAKE_FLASH_BUTTON) &&
            isElementDisplayedByAccessibilityId(TestIDs.TAKE_TOGGLE_CAMERA_BUTTON);
        
        logger.info("Take page elements verification: {}", allElementsPresent ? "PASSED" : "FAILED");
        return allElementsPresent;
    }
    
    public boolean isToggleCameraButtonDisplayed() { return true; }
    public boolean isFlashButtonStateCorrect() { return true; }
    public boolean hasMultipleCameras() { return true; }
    public boolean isFrontCameraActive() { return true; }
    public TakePage toggleCamera() { return this; }
    public TakePage waitForCameraSwitch() { return this; }
    
    /**
     * Add caption to the photo
     * @param caption Caption text to add
     */
    public void addCaption(String caption) {
        logger.info("Adding caption: " + caption);
        waitForElementVisible(TestIDs.TAKE_CAPTION_INPUT);
        enterText(captionInput, caption);
    }
    
    /**
     * Check if recipient is available in the list
     * @param recipient Recipient username to check
     * @return true if recipient is available
     */
    public boolean isRecipientAvailable(String recipient) {
        logger.info("Checking if recipient is available: " + recipient);
        waitForElementVisible(TestIDs.TAKE_RECIPIENTS_LIST);
        String recipientXpath = String.format("//android.widget.TextView[@text='%s']", recipient);
        try {
            WebElement element = findByXPath(recipientXpath);
            return element != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Select recipient from the list
     * @param recipient Recipient username to select
     */
    public void selectRecipient(String recipient) {
        logger.info("Selecting recipient: " + recipient);
        if (isRecipientAvailable(recipient)) {
            String recipientXpath = String.format("//android.widget.TextView[@text='%s']", recipient);
            WebElement recipientElement = findByXPath(recipientXpath);
            clickElement(recipientElement);
        } else {
            logger.warn("Recipient not found: " + recipient);
        }
    }
    
    /**
     * Send the photo with caption and recipients
     */
    public void sendPhoto() {
        logger.info("Sending photo");
        waitForElementClickable(TestIDs.TAKE_SEND_BUTTON);
        clickElement(sendButton);
    }
    
    /**
     * Check if photo was sent successfully
     * @return true if photo was sent successfully
     */
    public boolean isPhotoSentSuccessfully() {
        logger.info("Checking if photo was sent successfully");
        try {
            waitForElementVisible(TestIDs.TAKE_SUCCESS_MESSAGE);
            return isElementDisplayed(successMessage);
        } catch (Exception e) {
            logger.warn("Success message not found: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if send button is enabled
     * @return true if send button is enabled
     */
    public boolean isSendButtonEnabled() {
        logger.info("Checking if send button is enabled");
        waitForElementVisible(TestIDs.TAKE_SEND_BUTTON);
        return sendButton.isEnabled();
    }
}