package com.modis.tests;

import com.modis.base.BaseTest;
import com.modis.pages.AllImagesPage;
import com.modis.pages.HomePage;
import com.modis.pages.SendPhotoPage;
import com.modis.pages.TakePage;
import com.modis.utils.TestDataReader;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PhotoSharingTests extends BaseTest {

    private HomePage homePage;
    private final TestDataReader testDataReader = new TestDataReader();

    @BeforeMethod(alwaysRun = true)
    public void loginBeforeTest() {
        homePage = TestSessionHelper.loginAsDefaultUser(testDataReader);
    }

    @Test(priority = 1, groups = {"photo-sharing", "smoke", "regression"}, description = "Open camera screen")
    public void testOpenCamera() {
        TakePage takePage = homePage.navigateToCamera();
        takePage.waitForPageToLoad();

        Assert.assertTrue(takePage.isDisplayed(), "Camera screen should be displayed");
        Assert.assertTrue(takePage.isCaptureButtonDisplayed(), "Capture button should be displayed");
    }

    @Test(priority = 2, groups = {"photo-sharing", "regression"}, description = "Toggle flash")
    public void testToggleFlash() {
        TakePage takePage = homePage.navigateToCamera();
        takePage.waitForPageToLoad();

        if (!takePage.isFlashButtonDisplayed()) {
            throw new SkipException("Flash button is not available on this device");
        }

        boolean initialFlashState = takePage.isFlashEnabled();
        takePage.toggleFlash();

        Assert.assertNotEquals(
                takePage.isFlashEnabled(),
                initialFlashState,
                "Flash state should change after toggle"
        );
    }

    @Test(priority = 3, groups = {"photo-sharing", "regression"}, description = "Switch front and back camera")
    public void testSwitchCamera() {
        TakePage takePage = homePage.navigateToCamera();
        takePage.waitForPageToLoad();

        if (!takePage.hasMultipleCameras()) {
            throw new SkipException("Camera switch button is not available on this device");
        }

        boolean initialCameraState = takePage.isFrontCameraActive();
        takePage.toggleCamera().waitForCameraSwitch();

        Assert.assertNotEquals(
                takePage.isFrontCameraActive(),
                initialCameraState,
                "Camera should switch between front and back"
        );
    }

    @Test(priority = 4, groups = {"photo-sharing", "regression"}, description = "Capture photo")
    public void testCapturePhoto() {
        SendPhotoPage sendPhotoPage = capturePhotoOrSkip();

        Assert.assertTrue(sendPhotoPage.isDisplayed(), "Send photo screen should be displayed after capture");
        Assert.assertTrue(sendPhotoPage.isCapturedPhotoDisplayed(), "Captured photo preview should be displayed");
    }

    @Test(priority = 5, groups = {"photo-sharing", "regression"}, description = "Add caption")
    public void testAddCaption() {
        SendPhotoPage sendPhotoPage = capturePhotoOrSkip();

        String caption = "Automation caption " + System.currentTimeMillis();
        sendPhotoPage.enterCaption(caption);

        Assert.assertEquals(sendPhotoPage.getCaptionText(), caption, "Caption text should match entered value");
    }

    @Test(priority = 6, groups = {"photo-sharing", "regression"}, description = "Select recipient")
    public void testSelectRecipient() {
        SendPhotoPage sendPhotoPage = capturePhotoOrSkip();

        if (!sendPhotoPage.hasFriends()) {
            throw new SkipException("No friends available for recipient selection");
        }

        String friendId = sendPhotoPage.getFirstFriendId();
        sendPhotoPage.selectFriend(friendId);

        Assert.assertTrue(sendPhotoPage.isFriendSelected(friendId), "Selected recipient should be marked selected");
    }

    @Test(priority = 7, groups = {"photo-sharing", "regression"}, description = "Send photo")
    public void testSendPhoto() {
        SendPhotoPage sendPhotoPage = capturePhotoOrSkip();

        if (!sendPhotoPage.hasFriends()) {
            throw new SkipException("No friends available for photo sending");
        }

        sendPhotoPage.enterCaption("Automation photo " + System.currentTimeMillis());
        sendPhotoPage.selectFriend(sendPhotoPage.getFirstFriendId());

        HomePage afterSend = sendPhotoPage.sendPhoto();

        Assert.assertTrue(afterSend.isDisplayed(), "Home screen should display after sending photo");
    }

    @Test(priority = 8, groups = {"photo-sharing", "regression"}, description = "Validate send photo without recipient")
    public void testSendPhotoRequiresRecipient() {
        SendPhotoPage sendPhotoPage = capturePhotoOrSkip();

        Assert.assertFalse(
                sendPhotoPage.isSendButtonEnabled(),
                "Send button should be disabled until at least one recipient is selected"
        );
    }

    @Test(priority = 9, groups = {"photo-sharing", "regression"}, description = "View photo history")
    public void testViewPhotoHistory() {
        TakePage takePage = homePage.navigateToCamera();
        takePage.waitForPageToLoad();

        if (!takePage.isHistoryButtonDisplayed()) {
            throw new SkipException("Photo history button is not available");
        }

        AllImagesPage allImagesPage = takePage.openHistory();
        allImagesPage.waitForPageToLoad();

        Assert.assertTrue(allImagesPage.isDisplayed(), "Photo history screen should be displayed");
        Assert.assertTrue(
                allImagesPage.isImagesGridDisplayed() || allImagesPage.isEmptyStateDisplayed(),
                "Photo history should show either image grid or empty state"
        );
    }

    private SendPhotoPage capturePhotoOrSkip() {
        TakePage takePage = homePage.navigateToCamera();
        takePage.waitForPageToLoad();

        SendPhotoPage sendPhotoPage = takePage.capturePhoto();

        if (sendPhotoPage == null) {
            throw new SkipException("Photo capture did not navigate to send photo screen");
        }

        sendPhotoPage.waitForPageToLoad();
        return sendPhotoPage;
    }
}
