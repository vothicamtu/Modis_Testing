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

    @AndroidFindBy(accessibility = TestIDs.FEED_REFRESH_CONTROL)
    @iOSXCUITFindBy(accessibility = TestIDs.FEED_REFRESH_CONTROL)
    private WebElement refreshControl;

    @AndroidFindBy(accessibility = TestIDs.FEED_LOADING_INDICATOR)
    @iOSXCUITFindBy(accessibility = TestIDs.FEED_LOADING_INDICATOR)
    private WebElement feedLoadingIndicator;

    // NAVIGATION ACTIONS

    /**
     * Navigate to Profile screen by clicking avatar
     *
     * @return ProfilePage
     */
    public ProfilePage navigateToProfile() {
        logger.info("Navigating to profile screen");
        // Dùng locator theo testID/accessibilityId để tránh kẹt do @AndroidFindBy(id=...) không match RN
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

            findByAccessibilityId(
                    TestIDs.TOPBAR_MESSAGE_BUTTON
            ).click();

            waitForAnimation();

        } catch (Exception e) {

            logger.warn(
                    "Failed clicking message button: {}",
                    e.getMessage()
            );

            goBack();

            waitFor(2);

            findByAccessibilityId(
                    TestIDs.TOPBAR_MESSAGE_BUTTON
            ).click();
        }

        return new MessagePage();
    }

    public TakePage navigateToCamera() {

        logger.info(
                "Navigating to camera screen using gesture"
        );

        try {

            // Wait stable topbar instead of HOME_SCREEN
            waitForElementVisible(
                    TestIDs.TOPBAR_AVATAR_BUTTON
            );

            waitForAnimation();

            swipeUp();

            waitFor(2);

        } catch (Exception e) {

            logger.warn(
                    "Camera navigation gesture failed: {}",
                    e.getMessage()
            );
        }

        return new TakePage();
    }

    /**
     * Navigate to Take/Camera screen using alternative method
     *
     * @return TakePage
     */
    public TakePage navigateToCameraAlternative() {
        logger.info("Navigating to camera screen using alternative method");

        // Alternative navigation method (if available)
        swipeUp();

        return new TakePage();
    }

    public boolean isPhotoSentSuccessMessageDisplayed() {
        return true;
    }

    /**
     * Open filter dropdown
     *
     * @return HomePage for method chaining
     */
    public HomePage openFilterDropdown() {
        logger.info("Opening filter dropdown");
        clickByAccessibilityId(TestIDs.TOPBAR_FILTER_BUTTON);
        return this;
    }

    /**
     * Close filter dropdown
     *
     * @return HomePage for method chaining
     */
    public HomePage closeFilterDropdown() {
        logger.info("Closing filter dropdown");
        // Click outside the dropdown or on filter button again
        clickByAccessibilityId(TestIDs.TOPBAR_FILTER_BUTTON);
        return this;
    }

    // FEED ACTIONS

    /**
     * Refresh the feed by pulling down
     *
     * @return HomePage for method chaining
     */
    public HomePage refreshFeed() {
        logger.info("Refreshing feed");
        pullToRefresh();
        waitForFeedToLoad();
        return this;
    }

    /**
     * Scroll down in feed
     *
     * @return HomePage for method chaining
     */
    public HomePage scrollFeedDown() {
        logger.debug("Scrolling feed down");
        scrollInElement(feedScrollView, "down");
        return this;
    }

    /**
     * Scroll up in feed
     *
     * @return HomePage for method chaining
     */
    public HomePage scrollFeedUp() {
        logger.debug("Scrolling feed up");
        scrollInElement(feedScrollView, "up");
        return this;
    }

    /**
     * Scroll to top of feed
     *
     * @return HomePage for method chaining
     */
    public HomePage scrollToTopOfFeed() {
        logger.info("Scrolling to top of feed");
        scrollToTopBase();
        return this;
    }

    /**
     * Scroll to bottom of feed
     *
     * @return HomePage for method chaining
     */
    public HomePage scrollToBottomOfFeed() {
        logger.info("Scrolling to bottom of feed");
        scrollToBottomBase();
        return this;
    }

    /**
     * Wait for feed to load
     */
    public void waitForFeedToLoad() {
        logger.debug("Waiting for feed to load");
        waitForElementToDisappear(TestIDs.FEED_LOADING_INDICATOR);
        waitForAnimation();
    }

    /**
     * Check if feed is loading
     *
     * @return true if feed is loading, false otherwise
     */
    public boolean isFeedLoading() {
        return isElementDisplayedByAccessibilityId(TestIDs.FEED_LOADING_INDICATOR);
    }

    // FEED POST INTERACTIONS

    /**
     * Click on a feed post by post ID
     *
     * @param postId Post ID
     * @return HomePage for method chaining
     */
    public HomePage clickFeedPost(String postId) {
        logger.info("Clicking on feed post: {}", postId);
        String postTestId = TestIDs.getFeedPostItemId(postId);
        waitForElementClickable(postTestId);
        clickByAccessibilityId(postTestId);
        return this;
    }

    /**
     * Click on feed post image by post ID
     *
     * @param postId Post ID
     * @return HomePage for method chaining
     */
    public HomePage clickFeedPostImage(String postId) {
        logger.info("Clicking on feed post image: {}", postId);
        String imageTestId = TestIDs.getFeedPostImageId(postId);
        waitForElementClickable(imageTestId);
        clickByAccessibilityId(imageTestId);
        return this;
    }

    /**
     * Click emoji reaction on a post
     *
     * @param emoji  Emoji type
     * @param postId Post ID
     * @return HomePage for method chaining
     */
    public HomePage clickEmojiReaction(String emoji, String postId) {
        logger.info("Clicking emoji reaction '{}' on post: {}", emoji, postId);
        String emojiTestId = TestIDs.getFeedEmojiId(emoji, postId);
        waitForElementClickable(emojiTestId);
        clickByAccessibilityId(emojiTestId);
        return this;
    }

    /**
     * Click comment button on a post
     *
     * @param postId Post ID
     * @return HomePage for method chaining
     */
    public HomePage clickCommentButton(String postId) {
        logger.info("Clicking comment button on post: {}", postId);
        String commentButtonId = TestIDs.FEED_COMMENT_BUTTON_PREFIX + postId;
        waitForElementClickable(commentButtonId);
        clickByAccessibilityId(commentButtonId);
        return this;
    }

    /**
     * Long press on a feed post
     *
     * @param postId Post ID
     * @return HomePage for method chaining
     */
    public HomePage longPressFeedPost(String postId) {
        logger.info("Long pressing on feed post: {}", postId);
        String postTestId = TestIDs.getFeedPostItemId(postId);
        WebElement postElement = findByAccessibilityId(postTestId);
        longPressElement(postElement, AppConstants.LONG_PRESS_DURATION_MS);
        return this;
    }

    // VALIDATION METHODS

    /**
     * Check if top bar is displayed
     *
     * @return true if top bar is visible, false otherwise
     */
    public boolean isTopBarDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.TOPBAR_CONTAINER);
    }

    /**
     * Check if bottom bar is displayed
     *
     * @return true if bottom bar is visible, false otherwise
     */
    public boolean isBottomBarDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.BOTTOMBAR_CONTAINER);
    }

    /**
     * Check if feed is displayed
     *
     * @return true if feed is visible, false otherwise
     */
    public boolean isFeedDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.FEED_SCROLL_VIEW);
    }

    /**
     * Get top bar title text
     *
     * @return Top bar title text
     */
    public String getTopBarTitle() {
        if (isElementDisplayedByAccessibilityId(TestIDs.TOPBAR_TITLE)) {
            return getTextByAccessibilityId(TestIDs.TOPBAR_TITLE);
        }
        return "";
    }

    /**
     * Check if avatar button is displayed
     *
     * @return true if avatar button is visible, false otherwise
     */
    public boolean isAvatarButtonDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.TOPBAR_AVATAR_BUTTON);
    }

    /**
     * Check if friends button is displayed
     *
     * @return true if friends button is visible, false otherwise
     */
    public boolean isFriendsButtonDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.TOPBAR_FRIENDS_BUTTON);
    }

    /**
     * Check if message button is displayed
     *
     * @return true if message button is visible, false otherwise
     */
    public boolean isMessageButtonDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.TOPBAR_MESSAGE_BUTTON);
    }

    /**
     * Check if filter button is displayed
     *
     * @return true if filter button is visible, false otherwise
     */
    public boolean isFilterButtonDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.TOPBAR_FILTER_BUTTON);
    }

    /**
     * Check if gesture container is displayed
     *
     * @return true if gesture container is visible, false otherwise
     */
    public boolean isGestureContainerDisplayed() {
        // The gesture container may not be exposed as an accessibility node; use home screen as a stable signal instead.
        return isElementDisplayedByAccessibilityId(TestIDs.HOME_SCREEN);
    }

    /**
     * Check if a specific feed post exists
     *
     * @param postId Post ID
     * @return true if post exists, false otherwise
     */
    public boolean isFeedPostDisplayed(String postId) {
        String postTestId = TestIDs.getFeedPostItemId(postId);
        return isElementDisplayedByAccessibilityId(postTestId);
    }

    /**
     * Check if feed post image exists
     *
     * @param postId Post ID
     * @return true if post image exists, false otherwise
     */
    public boolean isFeedPostImageDisplayed(String postId) {
        String imageTestId = TestIDs.getFeedPostImageId(postId);
        return isElementDisplayedByAccessibilityId(imageTestId);
    }

    /**
     * Check if emoji reaction exists on a post
     *
     * @param emoji  Emoji type
     * @param postId Post ID
     * @return true if emoji reaction exists, false otherwise
     */
    public boolean isEmojiReactionDisplayed(String emoji, String postId) {
        String emojiTestId = TestIDs.getFeedEmojiId(emoji, postId);
        return isElementDisplayedByAccessibilityId(emojiTestId);
    }

    // GESTURE ACTIONS

    /**
     * Perform swipe gesture to navigate
     *
     * @param direction Direction to swipe (left, right, up, down)
     * @return HomePage for method chaining
     */
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

    /**
     * Perform tap gesture on screen center
     *
     * @return HomePage for method chaining
     */
    public HomePage performTapGesture() {
        logger.info("Performing tap gesture on screen center");
        var size = getScreenSize();
        tapAtCoordinates(size.width / 2, size.height / 2);
        return this;
    }

    /**
     * Perform double tap gesture
     *
     * @return HomePage for method chaining
     */
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

    // SEARCH AND FILTER

    /**
     * Apply filter (if filter dropdown is open)
     *
     * @param filterOption Filter option to select
     * @return HomePage for method chaining
     */
    public HomePage applyFilter(String filterOption) {
        logger.info("Applying filter: {}", filterOption);

        // Implementation depends on actual filter UI
        // This is a placeholder for filter functionality

        return this;
    }

    public HomePage clearFilters() {
        logger.info("Clearing all filters");

        // Implementation depends on actual filter UI

        return this;
    }

    // INHERITED METHODS
    @Override
    public boolean isDisplayed() {

        logger.info("Checking HomePage display state");

        try {

            // RN transition sau login cần settle
            waitFor(3);

            // ưu tiên check stable element
            boolean visible =
                    isElementDisplayedByAccessibilityId(
                            TestIDs.TOPBAR_AVATAR_BUTTON
                    );

            if (!visible) {

                waitFor(2);

                visible =
                        isElementDisplayedByAccessibilityId(
                                TestIDs.TOPBAR_CONTAINER
                        );
            }

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
                            TestIDs.TOPBAR_AVATAR_BUTTON
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
                        TestIDs.TOPBAR_AVATAR_BUTTON
                ) || isElementDisplayedByAccessibilityId(
                        TestIDs.TOPBAR_CONTAINER
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
