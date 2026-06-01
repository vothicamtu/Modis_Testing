package com.modis.pages;

import com.modis.base.BasePage;
import com.modis.constants.TestIDs;
import com.modis.constants.AppConstants;
import com.modis.drivers.DriverManager;
import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;
import io.appium.java_client.AppiumBy;

import java.util.List;
import java.util.ArrayList;

public class SendPhotoPage extends BasePage {

    @AndroidFindBy(accessibility = TestIDs.SEND_PHOTO_SCREEN)
    private WebElement sendPhotoScreen;

    @AndroidFindBy(accessibility = TestIDs.SEND_PHOTO_PREVIEW_IMAGE)
    private WebElement previewImage;

    @AndroidFindBy(accessibility = TestIDs.SEND_PHOTO_CAPTION_INPUT)
    private WebElement captionInput;

    @AndroidFindBy(accessibility = TestIDs.SEND_PHOTO_SEND_BUTTON)
    private WebElement sendButton;

    @AndroidFindBy(accessibility = TestIDs.SEND_PHOTO_CLOSE_BUTTON)
    private WebElement closeButton;

    @AndroidFindBy(accessibility = TestIDs.SEND_PHOTO_SELECT_ALL)
    private WebElement selectAllButton;

    @AndroidFindBy(accessibility = TestIDs.SEND_PHOTO_FRIENDS_LIST)
    private WebElement friendsList;

    // Loading and status elements
    @AndroidFindBy(accessibility = TestIDs.LOADING_SPINNER)
    private WebElement loadingSpinner;

    @AndroidFindBy(accessibility = TestIDs.TOAST_CONTAINER)
    private WebElement toastMessage;

    public SendPhotoPage addCaption(String caption) {
        logger.info("Adding caption: {}", caption);
        
        // Ensure we have a valid driver session before proceeding
        try {
            if (driver == null) {
                throw new RuntimeException("Driver is null, cannot add caption");
            }
            
            String sessionId = driver.getSessionId().toString();
            if (sessionId == null || sessionId.isEmpty() || "null".equals(sessionId)) {
                throw new RuntimeException("Driver session ID is null or empty");
            }
        } catch (Exception e) {
            logger.error("Driver session validation failed: {}", e.getMessage());
            throw new RuntimeException("Cannot add caption due to invalid driver session", e);
        }
        
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

        waitForElementVisible(TestIDs.SEND_PHOTO_SEND_BUTTON);
        findByAccessibilityId(TestIDs.SEND_PHOTO_SEND_BUTTON).click();

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
        try {
            // Ensure we have a valid driver session
            if (driver == null) {
                logger.warn("Driver is null, cannot check friends");
                return false;
            }
            
            // Check if driver session is still valid
            try {
                String sessionId = driver.getSessionId().toString();
                if (sessionId == null || sessionId.isEmpty() || "null".equals(sessionId)) {
                    logger.warn("Driver session ID is null or empty");
                    return false;
                }
            } catch (Exception e) {
                logger.warn("Driver session is invalid: {}", e.getMessage());
                return false;
            }
            
            // Wait for friends list to be visible with retry logic
            for (int attempt = 1; attempt <= 2; attempt++) {
                try {
                    waitForElementVisible(TestIDs.SEND_PHOTO_FRIENDS_LIST);
                    List<WebElement> friends = getVisibleFriends();
                    boolean hasFriends = friends != null && !friends.isEmpty();
                    logger.info("Friends check result: {} friends found", hasFriends ? friends.size() : 0);
                    return hasFriends;
                } catch (Exception e) {
                    logger.warn("Failed to check friends on attempt {}: {}", attempt, e.getMessage());
                    if (attempt == 2) {
                        return false;
                    }
                    // Try to recover from UiAutomator2 crash
                    if (!DriverManager.recoverFromUiAutomator2Crash()) {
                        return false;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            logger.warn("Failed to check friends availability: {}", e.getMessage());
            return false;
        }
    }

    public String getFirstFriendId() {

        List<WebElement> friends =
                getVisibleFriends();

        for (WebElement friend : friends) {

            String contentDesc =
                    friend.getAttribute(
                            "content-desc"
                    );

            System.out.println(
                    "FOUND = " + contentDesc
            );

            if (contentDesc == null) {
                continue;
            }

            if (!contentDesc.startsWith(
                    "send_photo_friend_")) {
                continue;
            }

            String friendId =
                    contentDesc.replace(
                            "send_photo_friend_",
                            ""
                    );

            if (friendId.equals("list")
                    || friendId.equals("flatlist")
                    || friendId.equals("all")) {
                continue;
            }

            return friendId;
        }

        return null;
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
        try {
            // Ensure we have a valid driver session
            if (driver == null) {
                logger.warn("Driver is null, cannot get visible friends");
                return new ArrayList<>();
            }
            
            // Check if driver session is still valid
            try {
                String sessionId = driver.getSessionId().toString();
                if (sessionId == null || sessionId.isEmpty() || "null".equals(sessionId)) {
                    logger.warn("Driver session ID is null or empty");
                    return new ArrayList<>();
                }
            } catch (Exception e) {
                logger.warn("Driver session is invalid: {}", e.getMessage());
                return new ArrayList<>();
            }

            waitForElementVisible(TestIDs.SEND_PHOTO_FRIENDS_LIST);

            return driver.findElements(
                    AppiumBy.xpath(
                            "//*[starts-with(@content-desc,'send_photo_friend_')]"
                    )
            );
        } catch (Exception e) {
            logger.warn("Failed to get visible friends: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public int getSelectedFriendsCount() {
        try {
            // Ensure we have a valid driver session
            if (driver == null) {
                logger.warn("Driver is null, cannot get selected friends count");
                return 0;
            }
            
            // Check if driver session is still valid
            try {
                String sessionId = driver.getSessionId().toString();
                if (sessionId == null || sessionId.isEmpty() || "null".equals(sessionId)) {
                    logger.warn("Driver session ID is null or empty");
                    return 0;
                }
            } catch (Exception e) {
                logger.warn("Driver session is invalid: {}", e.getMessage());
                return 0;
            }
            
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
        } catch (Exception e) {
            logger.warn("Failed to get selected friends count: {}", e.getMessage());
            return 0;
        }
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

    public boolean isAllFriendsSelected() {
        try {
            // Ensure we have a valid driver session
            if (driver == null) {
                logger.warn("Driver is null, cannot check all friends selection");
                return false;
            }
            
            // Check if driver session is still valid
            try {
                String sessionId = driver.getSessionId().toString();
                if (sessionId == null || sessionId.isEmpty() || "null".equals(sessionId)) {
                    logger.warn("Driver session ID is null or empty");
                    return false;
                }
            } catch (Exception e) {
                logger.warn("Driver session is invalid: {}", e.getMessage());
                return false;
            }
            
            WebElement selectAllElement = findByAccessibilityId(TestIDs.SEND_PHOTO_SELECT_ALL);
            if (selectAllElement == null) {
                logger.debug("All friends button not found");
                return false;
            }
            
            // Check multiple attributes that might indicate selection
            String selected = selectAllElement.getAttribute("selected");
            String checked = selectAllElement.getAttribute("checked");
            String accessibilityState = selectAllElement.getAttribute("accessibilityState");
            
            logger.debug("All friends selection attributes: selected='{}', checked='{}', accessibilityState='{}'", 
                selected, checked, accessibilityState);
            
            // Check if any of the selection indicators are true
            boolean isSelected = "true".equals(selected) || "true".equals(checked);
            
            // Also check accessibilityState for React Native components
            if (!isSelected && accessibilityState != null) {
                isSelected = accessibilityState.contains("selected") && accessibilityState.contains("true");
            }
            
            logger.debug("All friends final selection state: {}", isSelected);
            return isSelected;
        } catch (Exception e) {
            logger.debug("All friends button not found or selection state unavailable: {}", e.getMessage());
            return false;
        }
    }

    public boolean isFriendSelected(String userId) {
        String friendTestId = TestIDs.getSendPhotoFriendId(userId);

        try {
            if (driver == null) {
                logger.warn("Driver is null, cannot check friend selection");
                return false;
            }
            
            try {
                String sessionId = driver.getSessionId().toString();
                if (sessionId == null || sessionId.isEmpty() || "null".equals(sessionId)) {
                    logger.warn("Driver session ID is null or empty");
                    return false;
                }
            } catch (Exception e) {
                logger.warn("Driver session is invalid: {}", e.getMessage());
                return false;
            }
            
            WebElement friendElement = findByAccessibilityId(friendTestId);
            if (friendElement == null) {
                logger.debug("Friend element {} not found", friendTestId);
                return false;
            }
            
            String selected = friendElement.getAttribute("selected");
            String checked = friendElement.getAttribute("checked");
            String accessibilityState = friendElement.getAttribute("accessibilityState");
            
            logger.debug("Friend {} selection attributes: selected='{}', checked='{}', accessibilityState='{}'", 
                userId, selected, checked, accessibilityState);
            
            // Check if any of the selection indicators are true
            boolean isSelected = "true".equals(selected) || "true".equals(checked);
            
            // Also check accessibilityState for React Native components
            if (!isSelected && accessibilityState != null) {
                isSelected = accessibilityState.contains("selected") && accessibilityState.contains("true");
            }
            
            logger.debug("Friend {} final selection state: {}", userId, isSelected);
            return isSelected;
        } catch (Exception e) {
            logger.debug("Friend {} not found or selection state unavailable: {}", userId, e.getMessage());
            return false;
        }
    }

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

        try {
            waitForElementVisible(TestIDs.SEND_PHOTO_SCREEN);
            waitForAnimation();
            logger.info("Send photo page loaded successfully");
            return this;
        } catch (Exception e) {
            logger.error("Failed to load send photo page: {}", e.getMessage());
            throw new RuntimeException("Send photo page did not load properly", e);
        }
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
        try {
            // Ensure we have a valid driver session
            if (driver == null) {
                logger.warn("Driver is null, cannot check friend selection");
                return false;
            }
            
            // Check if driver session is still valid
            try {
                String sessionId = driver.getSessionId().toString();
                if (sessionId == null || sessionId.isEmpty() || "null".equals(sessionId)) {
                    logger.warn("Driver session ID is null or empty");
                    return false;
                }
            } catch (Exception e) {
                logger.warn("Driver session is invalid: {}", e.getMessage());
                return false;
            }
            
            return getSelectedFriendsCount() > 0;
        } catch (Exception e) {
            logger.warn("Failed to check if any friends are selected: {}", e.getMessage());
            return false;
        }
    }
}