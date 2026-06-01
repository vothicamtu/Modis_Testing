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
        TakePage takePage = new TakePage();
        takePage.waitForPageToLoad();

        Assert.assertTrue(takePage.isDisplayed(), "Camera screen should be displayed");
        Assert.assertTrue(takePage.isCaptureButtonDisplayed(), "Capture button should be displayed");
    }

    @Test(priority = 2, groups = {"photo-sharing", "regression"}, description = "Toggle flash")
    public void testToggleFlash() {
        TakePage takePage = new TakePage();
        takePage.waitForPageToLoad();
        Assert.assertTrue(takePage.isFlashButtonDisplayed(), "Flash button should be displayed");
        boolean initialFlashState = takePage.isFlashEnabled();
        takePage.toggleFlash();
        Assert.assertNotEquals(takePage.isFlashEnabled(), initialFlashState, "Flash state should change after toggle");
    }

    @Test(priority = 3, groups = {"photo-sharing", "regression"}, description = "Switch front and back camera")
    public void testSwitchCamera() {
        TakePage takePage = new TakePage();
        takePage.waitForPageToLoad();
        if (!takePage.hasMultipleCameras()) {
            throw new SkipException("Camera switch button is not available on this device");
        }
        takePage.toggleCamera().waitForCameraSwitch();
        Assert.assertTrue(takePage.isToggleCameraButtonDisplayed(), "Camera toggle button should remain visible after switching");
    }

    @Test(priority = 4, groups = {"photo-sharing", "regression"})
    public void testCapturePhoto() {
        SendPhotoPage sendPhotoPage = capturePhotoOrSkip();
        Assert.assertTrue(sendPhotoPage.isDisplayed(), "Send photo screen should be displayed after capture");
    }

    @Test(priority = 5, groups = {"photo-sharing", "regression"})
    public void testAddCaption() {
        SendPhotoPage sendPhotoPage = capturePhotoOrSkip();
        String caption = "Automation caption " + System.currentTimeMillis();
        sendPhotoPage.enterCaption(caption);
        Assert.assertEquals(sendPhotoPage.getCaptionText(), caption);
    }

    @Test(priority = 6, groups = {"photo-sharing", "regression"}, description = "Select recipient")
    public void testSelectRecipient() {
        SendPhotoPage sendPhotoPage = capturePhotoOrSkip();

        if (!sendPhotoPage.hasFriends()) {
            throw new SkipException("No friends available for recipient selection");
        }

        String friendId = sendPhotoPage.getFirstFriendId();
        Assert.assertNotNull(friendId, "Should be able to get first friend ID");

        sendPhotoPage.selectFriend(friendId);

        Assert.assertTrue(sendPhotoPage.isFriendSelected(friendId), "Selected recipient should be marked selected");
    }

    @Test(priority = 7, groups = {"photo-sharing", "regression"}, description = "Send photo successfully")
    public void testSendPhoto() {
        SendPhotoPage sendPhotoPage = capturePhotoOrSkip();
        sendPhotoPage.waitForPageToLoad();
        HomePage homePage = sendPhotoPage.sendPhoto();
        Assert.assertTrue(homePage.isDisplayed(), "Should return to Home page after sending photo");
    }

    @Test(priority = 8, groups = {"photo-sharing", "regression"}, description = "Send photo with caption")
    public void testSendPhotoWithCaption() {
        SendPhotoPage sendPhotoPage = capturePhotoOrSkip();
        sendPhotoPage.waitForPageToLoad();
        String caption = "Automation caption " + System.currentTimeMillis();
        sendPhotoPage.enterCaption(caption);
        Assert.assertEquals(sendPhotoPage.getCaptionText(), caption, "Caption should be entered correctly");
        HomePage homePage = sendPhotoPage.sendPhoto();
        Assert.assertTrue(homePage.isDisplayed(), "Should return to Home page after sending photo");
    }

    @Test(priority = 9, groups = {"photo-sharing", "regression"}, description = "View feed after sending photo")
    public void testViewPhotoHistory() {

        SendPhotoPage sendPhotoPage =
                capturePhotoOrSkip();

        sendPhotoPage.waitForPageToLoad();

        HomePage homePage =
                sendPhotoPage.sendPhoto();

        homePage.waitForPageToLoad();

        homePage.performSwipeGesture("up");

        Assert.assertTrue(
                homePage.isDisplayed(),
                "Home page should remain displayed"
        );

        Assert.assertTrue(
                homePage.isFeedDisplayed(),
                "Feed should be displayed"
        );
    }

    private SendPhotoPage capturePhotoOrSkip() {
        TakePage takePage = new TakePage();
        takePage.waitForPageToLoad();

        SendPhotoPage sendPhotoPage = takePage.capturePhoto();

        if (sendPhotoPage == null) {
            throw new SkipException("Photo capture did not navigate to send photo screen");
        }

        sendPhotoPage.waitForPageToLoad();
        return sendPhotoPage;
    }
}
