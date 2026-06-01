package com.modis.pages;

import com.modis.base.BasePage;
import com.modis.constants.TestIDs;
import com.modis.constants.AppConstants;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

public class TakePage extends BasePage {

    // PAGE ELEMENTS

    @AndroidFindBy(accessibility = TestIDs.TAKE_SCREEN)
    private WebElement takeScreen;

    @AndroidFindBy(accessibility = TestIDs.TAKE_CAMERA_AREA)
    private WebElement cameraArea;

    @AndroidFindBy(accessibility = TestIDs.TAKE_CAPTURE_BUTTON)
    private WebElement captureButton;

    @AndroidFindBy(accessibility = TestIDs.TAKE_FLASH_BUTTON)
    private WebElement flashButton;

    @AndroidFindBy(accessibility = TestIDs.TAKE_TOGGLE_CAMERA_BUTTON)
    private WebElement toggleCameraButton;

    @AndroidFindBy(accessibility = TestIDs.TAKE_HISTORY)
    private WebElement historyButton;

    // CAMERA ACTIONS
    public SendPhotoPage capturePhoto() {
        logger.info("Capturing photo");

        // Handle camera permissions if needed
        handleCameraPermissions();
        waitForCameraReady();
        findByAccessibilityId(TestIDs.TAKE_CAPTURE_BUTTON).click();

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

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
        clickByAccessibilityId(TestIDs.TAKE_HISTORY);
        waitForAnimation();
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
        waitForElementVisible(TestIDs.TAKE_SCREEN);
        waitForCameraReady();
        tapElement(cameraArea);
        waitForAnimation();
        return this;
    }

    public TakePage zoomIn() {
        logger.info("Zooming in");
        waitForElementVisible(TestIDs.TAKE_SCREEN);
        waitForCameraReady();
        pinchOut(cameraArea);
        return this;
    }

    public TakePage zoomOut() {
        logger.info("Zooming out");
        waitForElementVisible(TestIDs.TAKE_SCREEN);
        waitForCameraReady();
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
            WebElement allowButton = driver.findElement(
                    AppiumBy.id("com.android.permissioncontroller:id/permission_allow_button")
            );

            if (allowButton != null && allowButton.isDisplayed()) {
                clickElement(allowButton);
                waitForAnimation();
            }
        } catch (Exception e) {
            logger.debug("No camera permission dialog found", e);
        }
    }

    public boolean isCaptureButtonDisplayed() {

        try {

            return isElementDisplayedByAccessibilityId(
                    TestIDs.TAKE_CAPTURE_BUTTON
            );

        } catch (Exception e) {

            logger.warn(
                    "Capture button check failed: {}",
                    e.getMessage()
            );

            return false;
        }
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

    public boolean isFlashEnabled() {
        waitForElementVisible(TestIDs.TAKE_FLASH_BUTTON);
        String flashState = flashButton.getAttribute("selected");
        logger.info("Flash selected attribute = {}", flashState);
        return "true".equals(flashState);
    }

    public void waitForCameraReady() {

        logger.info("Waiting for camera initialization");

        long endTime =
                System.currentTimeMillis() + 20000;

        while (System.currentTimeMillis() < endTime) {

            boolean cameraReady =
                    isElementDisplayedByAccessibilityId(
                            TestIDs.TAKE_CAMERA_AREA
                    );

            boolean captureReady =
                    isElementDisplayedByAccessibilityId(
                            TestIDs.TAKE_CAPTURE_BUTTON
                    );

            if (cameraReady && captureReady) {

                logger.info("Camera ready");

                waitFor(2);

                return;
            }

            waitFor(1);
        }

        throw new RuntimeException(
                "Camera did not become ready"
        );
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

    @Override
    public boolean isDisplayed() {

        try {

            waitForPageToLoad();

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    @Override
    public String getPageTitle() {
        return "Take/Camera Screen";
    }

    public TakePage waitForPageToLoad() {

        logger.info("Waiting for camera screen");

        handleCameraPermissions();

        long endTime = System.currentTimeMillis() + 20000;

        while (System.currentTimeMillis() < endTime) {

            if (
                    isElementDisplayedByAccessibilityId(TestIDs.TAKE_CAPTURE_BUTTON)
                            ||
                            isElementDisplayedByAccessibilityId(TestIDs.TAKE_CAMERA_AREA)
            ) {

                logger.info("Camera ready");
                return this;
            }

            waitFor(1);
        }

        throw new RuntimeException(
                "Camera screen did not become ready within 20 seconds"
        );
    }

    public boolean verifyPageElements() {

        logger.info("Verifying take page elements");

        try {

            handleCameraPermissions();

            waitForCameraReady();

            boolean captureVisible =
                    isElementDisplayedByAccessibilityId(
                            TestIDs.TAKE_CAPTURE_BUTTON
                    );

            boolean cameraVisible =
                    isElementDisplayedByAccessibilityId(
                            TestIDs.TAKE_CAMERA_AREA
                    );

            boolean result =
                    captureVisible || cameraVisible;

            logger.info(
                    "Take page verification result: {}",
                    result ? "PASSED" : "FAILED"
            );

            return result;

        } catch (Exception e) {

            logger.warn(
                    "Take page verification failed: {}",
                    e.getMessage()
            );

            return false;
        }
    }

    public boolean isToggleCameraButtonDisplayed() {

        return isElementDisplayedByAccessibilityId(
                TestIDs.TAKE_TOGGLE_CAMERA_BUTTON
        );
    }

    public boolean isFlashButtonStateCorrect() {

        return isFlashButtonDisplayed();
    }

    public boolean hasMultipleCameras() {

        return isToggleCameraButtonDisplayed();
    }

    public boolean isFrontCameraActive() {

        try {

            String selected =
                    toggleCameraButton.getAttribute(
                            "selected"
                    );

            return "true".equals(selected);

        } catch (Exception e) {

            logger.warn(
                    "Failed to determine front camera state: {}",
                    e.getMessage()
            );

            return false;
        }
    }

    public TakePage toggleCamera() {

        return switchCamera();
    }

    public TakePage waitForCameraSwitch() {

        waitForCameraSwitchAnimation();

        return this;
    }

}
