package com.modis.pages;

import com.modis.base.BasePage;
import com.modis.constants.TestIDs;
import com.modis.constants.AppConstants;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;

import java.util.List;
public class SendPhotoPage extends BasePage {

    // ==================== PAGE ELEMENTS ====================

    @AndroidFindBy(id = TestIDs.SEND_PHOTO_SCREEN)
    @iOSXCUITFindBy(accessibility = TestIDs.SEND_PHOTO_SCREEN)
    private WebElement sendPhotoScreen;

    @AndroidFindBy(id = TestIDs.SEND_PHOTO_PREVIEW_IMAGE)
    @iOSXCUITFindBy(accessibility = TestIDs.SEND_PHOTO_PREVIEW_IMAGE)
    private WebElement previewImage;

    @AndroidFindBy(id = TestIDs.SEND_PHOTO_CAPTION_INPUT)
    @iOSXCUITFindBy(accessibility = TestIDs.SEND_PHOTO_CAPTION_INPUT)
    private WebElement captionInput;

    @AndroidFindBy(id = TestIDs.SEND_PHOTO_SEND_BUTTON)
    @iOSXCUITFindBy(accessibility = TestIDs.SEND_PHOTO_SEND_BUTTON)
    private WebElement sendButton;

    @AndroidFindBy(id = TestIDs.SEND_PHOTO_CLOSE_BUTTON)
    @iOSXCUITFindBy(accessibility = TestIDs.SEND_PHOTO_CLOSE_BUTTON)
    private WebElement closeButton;

    @AndroidFindBy(id = TestIDs.SEND_PHOTO_SELECT_ALL)
    @iOSXCUITFindBy(accessibility = TestIDs.SEND_PHOTO_SELECT_ALL)
    private WebElement selectAllButton;

    @AndroidFindBy(id = TestIDs.SEND_PHOTO_FRIENDS_LIST)
    @iOSXCUITFindBy(accessibility = TestIDs.SEND_PHOTO_FRIENDS_LIST)
    private WebElement friendsList;

    // Loading and status elements
    @AndroidFindBy(id = TestIDs.LOADING_SPINNER)
    @iOSXCUITFindBy(accessibility = TestIDs.LOADING_SPINNER)
    private WebElement loadingSpinner;

    @AndroidFindBy(id = TestIDs.TOAST_CONTAINER)
    @iOSXCUITFindBy(accessibility = TestIDs.TOAST_CONTAINER)
    private WebElement toastMessage;

    // ==================== PHOTO ACTIONS ====================
public SendPhotoPage addCaption(String caption) {
        logger.info("Adding caption: {}", caption);
        waitForElementVisible(TestIDs.SEND_PHOTO_CAPTION_INPUT);
        enterText(captionInput, caption);
        return this;
    }
public SendPhotoPage clearCaption() {
        logger.debug("Clearing caption");
        waitForElementVisible(TestIDs.SEND_PHOTO_CAPTION_INPUT);
        captionInput.clear();
        return this;
    }
public SendPhotoPage selectFriend(String userId) {
        logger.info("Selecting friend: {}", userId);
        String friendTestId = TestIDs.getSendPhotoFriendId(userId);

        // Scroll to find friend if not visible
        WebElement friendElement = scrollToElementByAccessibilityId(friendTestId);
        if (friendElement != null) {
            clickElement(friendElement);
        } else {
            logger.warn("Friend with ID {} not found in list", userId);
        }

        return this;
    }
public SendPhotoPage selectFriends(String[] userIds) {
        logger.info("Selecting {} friends", userIds.length);

        for (String userId : userIds) {
            selectFriend(userId);
        }

        return this;
    }
public SendPhotoPage selectAllFriends() {
        logger.info("Selecting all friends");
        waitForElementClickable(TestIDs.SEND_PHOTO_SELECT_ALL);
        clickElement(selectAllButton);
        return this;
    }
public SendPhotoPage deselectFriend(String userId) {
        logger.info("Deselecting friend: {}", userId);
        String friendTestId = TestIDs.getSendPhotoFriendId(userId);

        WebElement friendElement = scrollToElementByAccessibilityId(friendTestId);
        if (friendElement != null) {
            clickElement(friendElement); // Toggle selection
        }

        return this;
    }
public HomePage sendPhoto() {
        logger.info("Sending photo");
        waitForElementClickable(TestIDs.SEND_PHOTO_SEND_BUTTON);
        clickElement(sendButton);

        // Wait for sending to complete
        waitForSendingToComplete();

        return new HomePage();
    }
public BasePage sendPhotoWithCaptionToFriends(String caption, String[] userIds) {
        logger.info("Sending photo with caption to {} friends", userIds.length);

        if (caption != null && !caption.trim().isEmpty()) {
            addCaption(caption);
        }

        selectFriends(userIds);

        return sendPhoto();
    }

    public HomePage sendPhotoTo(String friendId) {

        logger.info(
                "Sending photo to friend: {}",
                friendId
        );

        selectFriend(friendId);

        return sendPhoto();
    }

    public boolean hasFriends() {
        return getVisibleFriends() != null
                && !getVisibleFriends().isEmpty();
    }

    public String getFirstFriendId() {

        List<WebElement> friends =
                getVisibleFriends();

        if (friends == null || friends.isEmpty()) {

            return null;
        }

        return friends.get(0)
                .getAttribute("content-desc");
    }

    public TakePage navigateBack() {

        logger.info("Navigating back from send photo screen");

        waitForElementClickable(
                TestIDs.SEND_PHOTO_CLOSE_BUTTON
        );

        clickByAccessibilityId(
                TestIDs.SEND_PHOTO_CLOSE_BUTTON
        );

        waitForAnimation();

        TakePage takePage = new TakePage();

        takePage.waitForPageToLoad();

        return takePage;
    }

    public BasePage sendPhotoToAllFriends(String caption) {
        logger.info("Sending photo to all friends with caption");

        if (caption != null && !caption.trim().isEmpty()) {
            addCaption(caption);
        }

        selectAllFriends();

        return sendPhoto();
    }
public BasePage closeSendPhoto() {
        logger.info("Closing send photo screen");
        waitForElementClickable(TestIDs.SEND_PHOTO_CLOSE_BUTTON);
        clickElement(closeButton);

        // Could navigate back to camera or home
        if (isElementDisplayedByAccessibilityId(TestIDs.TAKE_SCREEN)) {
            return new TakePage();
        } else {
            return new HomePage();
        }
    }

    // ==================== FRIEND LIST ACTIONS ====================
public SendPhotoPage scrollFriendsList(String direction) {
        logger.debug("Scrolling friends list: {}", direction);
        waitForElementVisible(TestIDs.SEND_PHOTO_FRIENDS_LIST);
        scrollInElement(friendsList, direction);
        return this;
    }
public boolean findFriendInList(String userId) {
        logger.info("Searching for friend in list: {}", userId);
        String friendTestId = TestIDs.getSendPhotoFriendId(userId);

        WebElement friendElement = scrollToElementByAccessibilityId(friendTestId);
        return friendElement != null;
    }
public List<WebElement> getVisibleFriends() {
        waitForElementVisible(TestIDs.SEND_PHOTO_FRIENDS_LIST);
        return findElementsByAccessibilityId(TestIDs.SEND_PHOTO_FRIEND_PREFIX + "*");
    }
public int getSelectedFriendsCount() {
        // Implementation depends on how selection is indicated in UI
        // This could check for selected state attributes or visual indicators
        List<WebElement> friends = getVisibleFriends();
        int selectedCount = 0;

        for (WebElement friend : friends) {
            String selected = friend.getAttribute("selected");
            if ("true".equals(selected)) {
                selectedCount++;
            }
        }

        logger.debug("Selected friends count: {}", selectedCount);
        return selectedCount;
    }

    // ==================== VALIDATION METHODS ====================
public boolean isPhotoPreviewDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.SEND_PHOTO_PREVIEW_IMAGE);
    }
public boolean isCaptionInputDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.SEND_PHOTO_CAPTION_INPUT);
    }
public boolean isFriendsListDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.SEND_PHOTO_FRIENDS_LIST);
    }
public boolean isSendButtonEnabled() {
        waitForElementVisible(TestIDs.SEND_PHOTO_SEND_BUTTON);
        return isElementEnabled(sendButton);
    }
public String getCaptionText() {
        waitForElementVisible(TestIDs.SEND_PHOTO_CAPTION_INPUT);
        return captionInput.getAttribute("text");
    }
public boolean isLoading() {
        return isElementDisplayedByAccessibilityId(TestIDs.LOADING_SPINNER);
    }
public void waitForSendingToComplete() {
        logger.debug("Waiting for photo sending to complete");
        waitForElementToDisappear(TestIDs.LOADING_SPINNER);
        waitForAnimation();
    }
public boolean isSuccessToastDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.TOAST_SUCCESS);
    }
public boolean isErrorToastDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.TOAST_ERROR);
    }
public String getToastMessage() {
        if (isElementDisplayedByAccessibilityId(TestIDs.TOAST_MESSAGE)) {
            return getTextByAccessibilityId(TestIDs.TOAST_MESSAGE);
        }
        return "";
    }
public boolean isFriendSelected(String userId) {
        String friendTestId = TestIDs.getSendPhotoFriendId(userId);

        try {
            WebElement friendElement = findByAccessibilityId(friendTestId);
            String selected = friendElement.getAttribute("selected");
            return "true".equals(selected);
        } catch (Exception e) {
            logger.debug("Friend {} not found or not selected", userId);
            return false;
        }
    }

    // ==================== IMAGE ACTIONS ====================
public SendPhotoPage tapPhotoPreview() {
        logger.info("Tapping photo preview");
        waitForElementClickable(TestIDs.SEND_PHOTO_PREVIEW_IMAGE);
        clickElement(previewImage);
        return this;
    }
public SendPhotoPage longPressPhotoPreview() {
        logger.info("Long pressing photo preview");
        waitForElementVisible(TestIDs.SEND_PHOTO_PREVIEW_IMAGE);
        longPressElement(previewImage, AppConstants.LONG_PRESS_DURATION_MS);
        return this;
    }

    // ==================== NEGATIVE TEST METHODS ====================
public SendPhotoPage sendPhotoWithoutFriends() {
        logger.info("Attempting to send photo without selecting friends");
        waitForElementClickable(TestIDs.SEND_PHOTO_SEND_BUTTON);
        clickElement(sendButton);
        return this;
    }
public SendPhotoPage sendPhotoWithLongCaption(String longCaption) {
        logger.info("Sending photo with long caption (length: {})", longCaption.length());
        addCaption(longCaption);
        selectAllFriends();

        waitForElementClickable(TestIDs.SEND_PHOTO_SEND_BUTTON);
        clickElement(sendButton);
        return this;
    }

    // ==================== INHERITED METHODS ====================

    @Override
    public boolean isDisplayed() {
        try {
            return isElementDisplayedByAccessibilityId(TestIDs.SEND_PHOTO_SCREEN);
        } catch (Exception e) {
            logger.debug("Send photo screen not displayed", e);
            return false;
        }
    }

    @Override
    public String getPageTitle() {
        return "Send Photo Screen";
    }
public SendPhotoPage waitForPageToLoad() {
        logger.info("Waiting for send photo page to load");
        waitForElementVisible(TestIDs.SEND_PHOTO_SCREEN);
        waitForElementVisible(TestIDs.SEND_PHOTO_PREVIEW_IMAGE);
        waitForElementVisible(TestIDs.SEND_PHOTO_FRIENDS_LIST);
        logger.info("Send photo page loaded successfully");
        return this;
    }
public boolean verifyPageElements() {
        logger.info("Verifying send photo page elements");

        boolean allElementsPresent =
                isElementDisplayedByAccessibilityId(TestIDs.SEND_PHOTO_SCREEN) &&
                        isElementDisplayedByAccessibilityId(TestIDs.SEND_PHOTO_PREVIEW_IMAGE) &&
                        isElementDisplayedByAccessibilityId(TestIDs.SEND_PHOTO_CAPTION_INPUT) &&
                        isElementDisplayedByAccessibilityId(TestIDs.SEND_PHOTO_SEND_BUTTON) &&
                        isElementDisplayedByAccessibilityId(TestIDs.SEND_PHOTO_CLOSE_BUTTON) &&
                        isElementDisplayedByAccessibilityId(TestIDs.SEND_PHOTO_FRIENDS_LIST);

        logger.info("Send photo page elements verification: {}", allElementsPresent ? "PASSED" : "FAILED");
        return allElementsPresent;
    }

    public boolean isCapturedPhotoDisplayed() {

        return isPhotoPreviewDisplayed();
    }

    public boolean isSendButtonDisplayed() {

        return isElementDisplayedByAccessibilityId(
                TestIDs.SEND_PHOTO_SEND_BUTTON
        );
    }

    public boolean isCloseButtonDisplayed() {

        return isElementDisplayedByAccessibilityId(
                TestIDs.SEND_PHOTO_CLOSE_BUTTON
        );
    }

    public SendPhotoPage enterCaption(String caption) {

        return addCaption(caption);
    }

    public boolean isSelectAllButtonDisplayed() {

        return isElementDisplayedByAccessibilityId(
                TestIDs.SEND_PHOTO_SELECT_ALL
        );
    }

    public boolean areAllFriendsSelected() {

        List<WebElement> friends =
                getVisibleFriends();

        if (friends == null || friends.isEmpty()) {

            return false;
        }

        for (WebElement friend : friends) {

            String selected =
                    friend.getAttribute("selected");

            if (!"true".equals(selected)) {

                return false;
            }
        }

        return true;
    }

    public SendPhotoPage deselectAllFriends() {

        List<WebElement> friends =
                getVisibleFriends();

        if (friends == null) {

            return this;
        }

        for (WebElement friend : friends) {

            String selected =
                    friend.getAttribute("selected");

            if ("true".equals(selected)) {

                clickElement(friend);
            }
        }

        return this;
    }

    public boolean areAnyFriendsSelected() {
        return getSelectedFriendsCount() > 0;
    }
}