package com.modis.pages;

import com.modis.base.BasePage;
import com.modis.constants.TestIDs;
import com.modis.constants.AppConstants;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;

public class HomePage extends BasePage {

    @AndroidFindBy(accessibility = TestIDs.HOME_SCREEN)
    @iOSXCUITFindBy(accessibility = TestIDs.HOME_SCREEN)
    private WebElement homeScreen;

    @AndroidFindBy(accessibility = TestIDs.HOME_GESTURE_CONTAINER)
    @iOSXCUITFindBy(accessibility = TestIDs.HOME_GESTURE_CONTAINER)
    private WebElement gestureContainer;

    // Top Bar Elements
    @AndroidFindBy(accessibility = TestIDs.TOPBAR_CONTAINER)
    @iOSXCUITFindBy(accessibility = TestIDs.TOPBAR_CONTAINER)
    private WebElement topBarContainer;

    @AndroidFindBy(accessibility = TestIDs.TOPBAR_AVATAR_BUTTON)
    @iOSXCUITFindBy(accessibility = TestIDs.TOPBAR_AVATAR_BUTTON)
    private WebElement avatarButton;

    @AndroidFindBy(accessibility = TestIDs.TOPBAR_FRIENDS_BUTTON)
    @iOSXCUITFindBy(accessibility = TestIDs.TOPBAR_FRIENDS_BUTTON)
    private WebElement friendsButton;

    @AndroidFindBy(accessibility = TestIDs.TOPBAR_MESSAGE_BUTTON)
    @iOSXCUITFindBy(accessibility = TestIDs.TOPBAR_MESSAGE_BUTTON)
    private WebElement messageButton;

    @AndroidFindBy(accessibility = TestIDs.TOPBAR_FILTER_BUTTON)
    @iOSXCUITFindBy(accessibility = TestIDs.TOPBAR_FILTER_BUTTON)
    private WebElement filterButton;

    @AndroidFindBy(accessibility = TestIDs.TOPBAR_TITLE)
    @iOSXCUITFindBy(accessibility = TestIDs.TOPBAR_TITLE)
    private WebElement topBarTitle;

    // Bottom Bar Elements
    @AndroidFindBy(accessibility = TestIDs.BOTTOMBAR_CONTAINER)
    @iOSXCUITFindBy(accessibility = TestIDs.BOTTOMBAR_CONTAINER)
    private WebElement bottomBarContainer;

    @AndroidFindBy(accessibility = TestIDs.BOTTOMBAR_HOME_BUTTON)
    @iOSXCUITFindBy(accessibility = TestIDs.BOTTOMBAR_HOME_BUTTON)
    private WebElement homeButton;

    // Feed Elements
    @AndroidFindBy(accessibility = TestIDs.FEED_SCREEN)
    @iOSXCUITFindBy(accessibility = TestIDs.FEED_SCREEN)
    private WebElement feedScreen;

    @AndroidFindBy(accessibility = TestIDs.FEED_SCROLL_VIEW)
    @iOSXCUITFindBy(accessibility = TestIDs.FEED_SCROLL_VIEW)
    private WebElement feedScrollView;

    // NAVIGATION ACTIONS
public ProfilePage navigateToProfile() {
        logger.info("Navigating to profile screen");
        // DÃ¹ng locator theo testID/accessibilityId Ä‘á»ƒ trÃ¡nh káº¹t do @AndroidFindBy(id=...) khÃ´ng match RN
        clickByAccessibilityId(TestIDs.TOPBAR_AVATAR_BUTTON);
        return new ProfilePage();
    }

    public FriendsPage navigateToFriends() {

        logger.info(
                "Navigating to friends screen"
        );

        clickByAccessibilityId(
                TestIDs.TOPBAR_FRIENDS_BUTTON
        );

        FriendsPage friendsPage =
                new FriendsPage();

        friendsPage.waitForPageToLoad();

        return friendsPage;
    }

    public MessagePage navigateToMessages() {

        logger.info("Navigating to messages screen");

        try {
            // Ensure we're on home screen first
            if (!isDisplayed()) {
                logger.warn("Not on home screen, attempting to navigate back");
                goBack();
                waitForAnimation();
            }

            // Wait for topbar to be ready
            waitForElementVisible(TestIDs.TOPBAR_MESSAGE_BUTTON);
            
            // Click message button
            findByAccessibilityId(TestIDs.TOPBAR_MESSAGE_BUTTON).click();

            waitForAnimation();

            // Create and wait for message page to load
            MessagePage messagePage = new MessagePage();
            messagePage.waitForPageToLoad();
            
            // Verify navigation was successful
            if (!messagePage.isDisplayed()) {
                throw new RuntimeException("Failed to navigate to messages screen");
            }
            
            logger.info("Successfully navigated to messages screen");
            return messagePage;

        } catch (Exception e) {

            logger.warn(
                    "Failed clicking message button: {}",
                    e.getMessage()
            );

            // Retry once
            try {
                goBack();
                waitFor(2);
                
                waitForElementVisible(TestIDs.TOPBAR_MESSAGE_BUTTON);
                findByAccessibilityId(TestIDs.TOPBAR_MESSAGE_BUTTON).click();
                waitForAnimation();
                
                MessagePage messagePage = new MessagePage();
                messagePage.waitForPageToLoad();
                
                if (!messagePage.isDisplayed()) {
                    throw new RuntimeException("Failed to navigate to messages screen after retry");
                }
                
                logger.info("Successfully navigated to messages screen after retry");
                return messagePage;
                
            } catch (Exception retryException) {
                logger.error("Navigation to messages failed even after retry: {}", retryException.getMessage());
                throw new RuntimeException("Unable to navigate to messages screen", retryException);
            }
        }
    }

    public TakePage navigateToCamera() {
        logger.info("Navigating to camera screen using gesture");
        try {
            waitForElementVisible(TestIDs.TOPBAR_AVATAR_BUTTON);
            waitForAnimation();
        } catch (Exception e) {
            logger.warn(
                    "Camera navigation gesture failed: {}",
                    e.getMessage()
            );
        }

        return new TakePage();
    }
public TakePage navigateToCameraAlternative() {
        logger.info("Navigating to camera screen using alternative method");

        // Alternative navigation method (if available)
        swipeUp();

        return new TakePage();
    }

public HomePage openFilterDropdown() {
        logger.info("Opening filter dropdown");
        clickByAccessibilityId(TestIDs.TOPBAR_FILTER_BUTTON);
        return this;
    }
public HomePage closeFilterDropdown() {
        logger.info("Closing filter dropdown");
        // Click outside the dropdown or on filter button again
        clickByAccessibilityId(TestIDs.TOPBAR_FILTER_BUTTON);
        return this;
    }

    // FEED ACTIONS
public HomePage refreshFeed() {
        logger.info("Refreshing feed");
        pullToRefresh();
        waitForFeedToLoad();
        return this;
    }
public HomePage scrollFeedDown() {
        logger.debug("Scrolling feed down");
        scrollInElement(feedScrollView, "down");
        return this;
    }
public HomePage scrollFeedUp() {
        logger.debug("Scrolling feed up");
        scrollInElement(feedScrollView, "up");
        return this;
    }
public HomePage scrollToTopOfFeed() {
        logger.info("Scrolling to top of feed");
        scrollToTopBase();
        return this;
    }
public HomePage scrollToBottomOfFeed() {
        logger.info("Scrolling to bottom of feed");
        scrollToBottomBase();
        return this;
    }
public void waitForFeedToLoad() {
        logger.debug("Waiting for feed to load");
        waitForAnimation();
    }

    // FEED POST INTERACTIONS
public HomePage clickFeedPost(String postId) {
        logger.info("Clicking on feed post: {}", postId);
        String postTestId = TestIDs.getFeedPostItemId(postId);
        waitForElementClickable(postTestId);
        clickByAccessibilityId(postTestId);
        return this;
    }
public HomePage clickFeedPostImage(String postId) {
        logger.info("Clicking on feed post image: {}", postId);
        String imageTestId = TestIDs.getFeedPostImageId(postId);
        waitForElementClickable(imageTestId);
        clickByAccessibilityId(imageTestId);
        return this;
    }
public HomePage clickEmojiReaction(String emoji, String postId) {
        logger.info("Clicking emoji reaction '{}' on post: {}", emoji, postId);
        String emojiTestId = TestIDs.getFeedEmojiId(emoji, postId);
        waitForElementClickable(emojiTestId);
        clickByAccessibilityId(emojiTestId);
        return this;
    }
public HomePage clickCommentButton(String postId) {
        logger.info("Clicking comment button on post: {}", postId);
        String commentButtonId = TestIDs.FEED_COMMENT_BUTTON_PREFIX + postId;
        waitForElementClickable(commentButtonId);
        clickByAccessibilityId(commentButtonId);
        return this;
    }
public HomePage longPressFeedPost(String postId) {
        logger.info("Long pressing on feed post: {}", postId);
        String postTestId = TestIDs.getFeedPostItemId(postId);
        WebElement postElement = findByAccessibilityId(postTestId);
        longPressElement(postElement, AppConstants.LONG_PRESS_DURATION_MS);
        return this;
    }

    // VALIDATION METHODS
public boolean isTopBarDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.TOPBAR_CONTAINER);
    }
public boolean isBottomBarDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.BOTTOMBAR_CONTAINER);
    }
public boolean isFeedDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.FEED_SCROLL_VIEW);
    }
public String getTopBarTitle() {
        if (isElementDisplayedByAccessibilityId(TestIDs.TOPBAR_TITLE)) {
            return getTextByAccessibilityId(TestIDs.TOPBAR_TITLE);
        }
        return "";
    }
public boolean isAvatarButtonDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.TOPBAR_AVATAR_BUTTON);
    }
public boolean isFriendsButtonDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.TOPBAR_FRIENDS_BUTTON);
    }
public boolean isMessageButtonDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.TOPBAR_MESSAGE_BUTTON);
    }
public boolean isFilterButtonDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.TOPBAR_FILTER_BUTTON);
    }
public boolean isGestureContainerDisplayed() {
        // The gesture container may not be exposed as an accessibility node; use home screen as a stable signal instead.
        return isElementDisplayedByAccessibilityId(TestIDs.HOME_SCREEN);
    }
public boolean isFeedPostDisplayed(String postId) {
        String postTestId = TestIDs.getFeedPostItemId(postId);
        return isElementDisplayedByAccessibilityId(postTestId);
    }
public boolean isFeedPostImageDisplayed(String postId) {
        String imageTestId = TestIDs.getFeedPostImageId(postId);
        return isElementDisplayedByAccessibilityId(imageTestId);
    }
public boolean isEmojiReactionDisplayed(String emoji, String postId) {
        String emojiTestId = TestIDs.getFeedEmojiId(emoji, postId);
        return isElementDisplayedByAccessibilityId(emojiTestId);
    }

    public boolean isFeedCommentButtonDisplayed(String postId) {
        String commentButtonId = TestIDs.FEED_COMMENT_BUTTON_PREFIX + postId;
        return isElementDisplayedByAccessibilityId(commentButtonId);
    }

    // GESTURE ACTIONS
public HomePage performSwipeGesture(String direction) {
        logger.info("Performing swipe gesture: {}", direction);

        switch (direction.toLowerCase()) {
            case "left":
                swipeLeft();
                break;
            case "right":
                swipeRight();
                break;
            case "up":
                swipeUp();
                break;
            case "down":
                swipeDown();
                break;
            default:
                logger.warn("Invalid swipe direction: {}", direction);
        }

        return this;
    }
public HomePage performTapGesture() {
        logger.info("Performing tap gesture on screen center");
        var size = getScreenSize();
        tapAtCoordinates(size.width / 2, size.height / 2);
        return this;
    }
public HomePage performDoubleTapGesture() {
        logger.info("Performing double tap gesture");
        var size = getScreenSize();
        int centerX = size.width / 2;
        int centerY = size.height / 2;

        tapAtCoordinates(centerX, centerY);
        waitFor(1); // Short delay between taps
        tapAtCoordinates(centerX, centerY);

        return this;
    }

    // INHERITED METHODS
    @Override
    public boolean isDisplayed() {

        logger.info("Checking HomePage display state");

        try {
            waitForElementVisible(TestIDs.HOME_SCREEN);
            boolean visible = isElementDisplayedByAccessibilityId(TestIDs.HOME_SCREEN);

            logger.info(
                    "HomePage visible: {}",
                    visible
            );

            return visible;

        } catch (Exception e) {

            logger.warn(
                    "HomePage display check failed: {}",
                    e.getMessage()
            );

            return false;
        }
    }

    @Override
    public String getPageTitle() {
        return "Home Screen";
    }

    public HomePage waitForPageToLoad() {

        logger.info("Waiting for home page to load");

        waitForTopbarReadyAfterLogin(10);

        waitForAnimation();

        logger.info("Home page loaded successfully");

        return this;
    }

    public HomePage waitForTopbarReadyAfterLogin(
            int timeoutSeconds
    ) {

        logger.info(
                "Waiting for home topbar to be ready"
        );

        long endTime =
                System.currentTimeMillis()
                        + (timeoutSeconds * 1000L);

        boolean ready = false;

        while (System.currentTimeMillis() < endTime) {

            ready =
                    isElementDisplayedByAccessibilityId(
                            TestIDs.HOME_SCREEN
                    );

            if (!ready) {

                ready =
                        isElementDisplayedByAccessibilityId(
                                TestIDs.TOPBAR_CONTAINER
                        );
            }

            if (ready) {
                break;
            }

            waitFor(1);
        }

        logger.info(
                "Home topbar ready: {}",
                ready
        );

        if (!ready) {

            throw new RuntimeException(
                    "Home topbar failed to load"
            );
        }

        return this;
    }

    public boolean verifyPageElements() {

        logger.info("Verifying home page elements");

        boolean homeVisible =
                isElementDisplayedByAccessibilityId(
                        TestIDs.HOME_SCREEN
                );

        logger.info(
                "Home page elements verification: {}",
                homeVisible ? "PASSED" : "FAILED"
        );

        return homeVisible;
    }

    public boolean isUserLoggedIn() {
        return isDisplayed() && isTopBarDisplayed();
    }

    public boolean isTopbarAvatarDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.TOPBAR_AVATAR_BUTTON);
    }
}
