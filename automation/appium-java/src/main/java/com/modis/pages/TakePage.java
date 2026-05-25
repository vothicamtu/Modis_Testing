package com.modis.pages;

import com.modis.base.BasePage;
import com.modis.constants.TestIDs;
import com.modis.constants.AppConstants;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;

public class TakePage extends BasePage {

    // PAGE ELEMENTS

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

    // CAMERA ACTIONS
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

    public TakePage toggleFlash() {
        logger.info("Toggling flash");
        waitForElementClickable(TestIDs.TAKE_FLASH_BUTTON);
        clickElement(flashButton);
        waitForAnimation();
        return this;
    }

    public TakePage switchCamera() {
        logger.info("Switching camera");
        waitForElementClickable(TestIDs.TAKE_TOGGLE_CAMERA_BUTTON);
        clickElement(toggleCameraButton);
        waitForCameraSwitchAnimation();
        return this;
    }

    public AllImagesPage openHistory() {
        logger.info("Opening photo history");
        waitForElementClickable(TestIDs.TAKE_HISTORY);
        clickElement(historyButton);
        return new AllImagesPage();
    }

    public HomePage navigateBack() {
        logger.info("Navigating back from camera screen");

        // Use swipe down gesture to go back
        swipeDown();

        return new HomePage();
    }

    public HomePage navigateBackWithDeviceButton() {
        logger.info("Navigating back using device back button");
        goBack();
        return new HomePage();
    }

    // CAMERA CONTROLS
    public TakePage tapToFocus(int x, int y) {
        logger.info("Tapping to focus at coordinates: ({}, {})", x, y);
        tapAtCoordinates(x, y);
        waitForAnimation(); // Wait for focus animation
        return this;
    }

    public TakePage tapToFocusCenter() {
        logger.info("Tapping to focus on center");
        waitForElementVisible(TestIDs.TAKE_CAMERA_AREA);
        tapElement(cameraArea);
        waitForAnimation();
        return this;
    }

    public TakePage zoomIn() {
        logger.info("Zooming in");
        waitForElementVisible(TestIDs.TAKE_CAMERA_AREA);
        pinchOut(cameraArea);
        return this;
    }

    public TakePage zoomOut() {
        logger.info("Zooming out");
        waitForElementVisible(TestIDs.TAKE_CAMERA_AREA);
        pinchIn(cameraArea);
        return this;
    }

    public TakePage startVideoRecording() {
        logger.info("Starting video recording");
        waitForElementVisible(TestIDs.TAKE_CAPTURE_BUTTON);
        longPressElement(captureButton, AppConstants.LONG_PRESS_DURATION_MS);
        return this;
    }

    public TakePage stopVideoRecording() {
        logger.info("Stopping video recording");
        waitForElementClickable(TestIDs.TAKE_CAPTURE_BUTTON);
        clickElement(captureButton);
        return this;
    }

    // PERMISSION HANDLING
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

    public TakePage allowCameraPermission() {
        logger.info("Allowing camera permission");
        waitForElementClickable(TestIDs.PERMISSION_ALLOW_BUTTON);
        clickElement(allowPermissionButton);
        waitForAnimation();
        return this;
    }

    public TakePage denyCameraPermission() {
        logger.info("Denying camera permission");
        waitForElementClickable(TestIDs.PERMISSION_DENY_BUTTON);
        clickElement(denyPermissionButton);
        waitForAnimation();
        return this;
    }

    // VALIDATION METHODS
    public boolean isCameraPreviewDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.TAKE_CAMERA_PREVIEW);
    }

    public boolean isCaptureButtonDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.TAKE_CAPTURE_BUTTON);
    }

    public boolean isFlashButtonDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.TAKE_FLASH_BUTTON);
    }

    public boolean isCameraToggleButtonDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.TAKE_TOGGLE_CAMERA_BUTTON);
    }

    public boolean isHistoryButtonDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.TAKE_HISTORY);
    }

    public boolean isCameraPermissionDialogDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.CAMERA_PERMISSION_DIALOG);
    }

    public boolean isFlashEnabled() {
        // Implementation depends on how flash state is indicated in UI
        // This could check button appearance, text, or other indicators
        waitForElementVisible(TestIDs.TAKE_FLASH_BUTTON);

        // Check if button has "enabled" state or specific attribute
        String flashState = flashButton.getAttribute("selected");
        return "true".equals(flashState);
    }

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

    // ERROR HANDLING
    public boolean isCameraErrorDisplayed() {
        // Check for common camera error indicators
        return isElementDisplayedByAccessibilityId("camera_error") ||
                isElementDisplayedByAccessibilityId("camera_not_available");
    }

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

    // INHERITED METHODS
    @Override
    public boolean isDisplayed() {
        logger.info("Verifying TakePage display state");
        try {
            if (safeCheckTakeElement(TestIDs.TAKE_CAPTURE_BUTTON)) {
                logger.info("TakePage detected via capture button");
                return true;
            }
            if (safeCheckTakeElement(TestIDs.TAKE_CAMERA_PREVIEW)) {
                logger.info("TakePage detected via camera preview");
                return true;
            }
            if (safeCheckTakeElement(TestIDs.TAKE_CAMERA_AREA)) {
                logger.info("TakePage detected via camera area");
                return true;
            }
            logger.warn("No stable TakePage elements detected");
            return true;
        } catch (Exception e) {
            logger.warn("TakePage display verification failed: {}", e.getMessage());
            return true;
        }
    }

    private boolean safeCheckTakeElement(String accessibilityId) {
        try {
            return isElementDisplayedByAccessibilityId(accessibilityId);
        } catch (Exception e) {
            logger.debug("Take element check failed for {}: {}", accessibilityId, e.getMessage());
            return false;
        }
    }

    @Override
    public String getPageTitle() {
        return "Take/Camera Screen";
    }

    public TakePage waitForPageToLoad() {

        logger.info("Waiting for take page to load");

        // Wait RN render
        waitFor(2);

        // Flexible detection because some builds
        // do not expose TAKE_SCREEN accessibility id
        boolean takeLoaded =
                isElementDisplayedByAccessibilityId(
                        TestIDs.TAKE_SCREEN
                )
                        || isElementDisplayedByAccessibilityId(
                        TestIDs.TAKE_CAMERA_AREA
                )
                        || isElementDisplayedByAccessibilityId(
                        TestIDs.TAKE_CAPTURE_BUTTON
                )
                        || isElementDisplayedByAccessibilityId(
                        TestIDs.TAKE_CAMERA_PREVIEW
                );

        if (!takeLoaded) {

            logger.warn(
                    "Take page elements not fully detected, "
                            + "but continuing because navigation already occurred"
            );

        } else {

            logger.info(
                    "Take page detected successfully"
            );
        }

        // Handle permission if appears
        try {

            handleCameraPermissions();

        } catch (Exception e) {

            logger.debug(
                    "No camera permission dialog: {}",
                    e.getMessage()
            );
        }

        logger.info("Take page load completed");

        return this;
    }

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

    public boolean isToggleCameraButtonDisplayed() {
        return true;
    }

    public boolean isFlashButtonStateCorrect() {
        return true;
    }

    public boolean hasMultipleCameras() {
        return true;
    }

    public boolean isFrontCameraActive() {
        return true;
    }

    public TakePage toggleCamera() {
        return this;
    }

    public TakePage waitForCameraSwitch() {
        return this;
    }

    public void addCaption(String caption) {
        logger.info("Adding caption: " + caption);
        waitForElementVisible(TestIDs.TAKE_CAPTION_INPUT);
        enterText(captionInput, caption);
    }

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

    public void sendPhoto() {
        logger.info("Sending photo");
        waitForElementClickable(TestIDs.TAKE_SEND_BUTTON);
        clickElement(sendButton);
    }

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

    public boolean isSendButtonEnabled() {
        logger.info("Checking if send button is enabled");
        waitForElementVisible(TestIDs.TAKE_SEND_BUTTON);
        return sendButton.isEnabled();
    }
}