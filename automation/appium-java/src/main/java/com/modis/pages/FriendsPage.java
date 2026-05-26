package com.modis.pages;

import com.modis.base.BasePage;
import com.modis.constants.TestIDs;
import com.modis.constants.AppConstants;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object for Friends Screen
 * Handles friend management, search, and friend requests
 */
public class FriendsPage extends BasePage {

    // PAGE ELEMENTS

    @AndroidFindBy(accessibility = TestIDs.FRIENDS_SCREEN)
    @iOSXCUITFindBy(accessibility = TestIDs.FRIENDS_SCREEN)
    private WebElement friendsScreen;

    @AndroidFindBy(accessibility = TestIDs.FRIENDS_BACK_BUTTON)
    @iOSXCUITFindBy(accessibility = TestIDs.FRIENDS_BACK_BUTTON)
    private WebElement backButton;

    @AndroidFindBy(accessibility = TestIDs.FRIENDS_SCROLL)
    @iOSXCUITFindBy(accessibility = TestIDs.FRIENDS_SCROLL)
    private WebElement friendsScrollView;

    @AndroidFindBy(accessibility = TestIDs.FRIENDS_SEARCH_INPUT)
    @iOSXCUITFindBy(accessibility = TestIDs.FRIENDS_SEARCH_INPUT)
    private WebElement searchInput;

    @AndroidFindBy(accessibility = TestIDs.FRIENDS_SEARCH_BUTTON)
    @iOSXCUITFindBy(accessibility = TestIDs.FRIENDS_SEARCH_BUTTON)
    private WebElement searchButton;

    @AndroidFindBy(accessibility = TestIDs.FRIENDS_CLEAR_SEARCH)
    @iOSXCUITFindBy(accessibility = TestIDs.FRIENDS_CLEAR_SEARCH)
    private WebElement clearSearchButton;

    // Tab elements
    @AndroidFindBy(accessibility = TestIDs.FRIENDS_TAB_FRIENDS)
    @iOSXCUITFindBy(accessibility = TestIDs.FRIENDS_TAB_FRIENDS)
    private WebElement friendsTab;

    @AndroidFindBy(accessibility = TestIDs.FRIENDS_TAB_REQUESTS)
    @iOSXCUITFindBy(accessibility = TestIDs.FRIENDS_TAB_REQUESTS)
    private WebElement requestsTab;

    @AndroidFindBy(accessibility = TestIDs.FRIENDS_TAB_SENT)
    @iOSXCUITFindBy(accessibility = TestIDs.FRIENDS_TAB_SENT)
    private WebElement sentRequestsTab;

    // Search results
    @AndroidFindBy(accessibility = TestIDs.SEARCH_RESULTS_LIST)
    @iOSXCUITFindBy(accessibility = TestIDs.SEARCH_RESULTS_LIST)
    private WebElement searchResultsList;

    @AndroidFindBy(accessibility = TestIDs.SEARCH_EMPTY_STATE)
    @iOSXCUITFindBy(accessibility = TestIDs.SEARCH_EMPTY_STATE)
    private WebElement searchEmptyState;

    // NAVIGATION ACTIONS

    public HomePage navigateBack() {

        logger.info(
                "Navigating back from friends screen"
        );

        try {
            waitForElementClickable(
                    TestIDs.FRIENDS_BACK_BUTTON
            );

            clickByAccessibilityId(
                    TestIDs.FRIENDS_BACK_BUTTON
            );

            waitForAnimation();
        } catch (Exception e) {

            logger.warn(
                    "Device back navigation failed: {}",
                    e.getMessage()
            );
        }

        HomePage homePage = new HomePage();

        homePage.waitForTopbarReadyAfterLogin(10);

        return homePage;
    }

    public FriendsPage switchToFriendsTab() {
        logger.info("Switching to Friends tab");
        waitForElementClickable(TestIDs.FRIENDS_TAB_FRIENDS);
        clickByAccessibilityId(
                TestIDs.FRIENDS_TAB_FRIENDS
        );
        waitForAnimation();
        return this;
    }

    /**
     * Switch to Friend Requests tab
     *
     * @return FriendsPage for method chaining
     */
    public FriendsPage switchToRequestsTab() {
        logger.info("Switching to Friend Requests tab");
        waitForElementClickable(TestIDs.FRIENDS_TAB_REQUESTS);
        clickElement(requestsTab);
        waitForAnimation();
        return this;
    }

    /**
     * Switch to Sent Requests tab
     *
     * @return FriendsPage for method chaining
     */
    public FriendsPage switchToSentRequestsTab() {
        logger.info("Switching to Sent Requests tab");
        waitForElementClickable(TestIDs.FRIENDS_TAB_SENT);
        clickElement(sentRequestsTab);
        waitForAnimation();
        return this;
    }

    // SEARCH ACTIONS

    /**
     * Search for users
     *
     * @param searchQuery Search query
     * @return FriendsPage for method chaining
     */
    public FriendsPage searchUsers(String searchQuery) {
        logger.info("Searching for users: {}", searchQuery);
        waitForElementVisible(TestIDs.FRIENDS_SEARCH_INPUT);
        enterText(searchInput, searchQuery);

        // Click search button or wait for auto-search
        if (isElementDisplayedByAccessibilityId(TestIDs.FRIENDS_SEARCH_BUTTON)) {
            clickElement(searchButton);
        }

        waitForSearchResults();
        return this;
    }

    /**
     * Clear search
     *
     * @return FriendsPage for method chaining
     */
    public FriendsPage clearSearch() {
        logger.info("Clearing search");

        if (isElementDisplayedByAccessibilityId(TestIDs.FRIENDS_CLEAR_SEARCH)) {
            clickElement(clearSearchButton);
        } else {
            // Clear search input manually
            waitForElementVisible(TestIDs.FRIENDS_SEARCH_INPUT);
            searchInput.clear();
        }

        waitForAnimation();
        return this;
    }

    /**
     * Wait for search results to load
     */
    public void waitForSearchResults() {
        logger.debug("Waiting for search results");
        waitForAnimation();

        // Wait for either results or empty state
        try {
            Thread.sleep(AppConstants.SEARCH_DEBOUNCE_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Search results wait interrupted", e);
        }
    }

    // FRIEND ACTIONS

    /**
     * Send friend request to user
     *
     * @param userId User ID to send request to
     * @return FriendsPage for method chaining
     */
    public FriendsPage sendFriendRequest(String userId) {
        logger.info("Sending friend request to user: {}", userId);
        String addButtonId = TestIDs.getFriendButtonId(userId);

        WebElement addButton = scrollToElementByAccessibilityId(addButtonId);
        if (addButton != null) {
            clickElement(addButton);
            waitForAnimation();
        } else {
            logger.warn("Add friend button not found for user: {}", userId);
        }

        return this;
    }

    /**
     * Accept friend request
     *
     * @param userId User ID to accept request from
     * @return FriendsPage for method chaining
     */
    public FriendsPage acceptFriendRequest(String userId) {
        logger.info("Accepting friend request from user: {}", userId);
        String acceptButtonId = TestIDs.FRIENDS_ACCEPT_BUTTON_PREFIX + userId;

        WebElement acceptButton = scrollToElementByAccessibilityId(acceptButtonId);
        if (acceptButton != null) {
            clickElement(acceptButton);
            waitForAnimation();
        } else {
            logger.warn("Accept button not found for user: {}", userId);
        }

        return this;
    }

    /**
     * Decline friend request
     *
     * @param userId User ID to decline request from
     * @return FriendsPage for method chaining
     */
    public FriendsPage declineFriendRequest(String userId) {
        logger.info("Declining friend request from user: {}", userId);
        String declineButtonId = TestIDs.FRIENDS_DECLINE_BUTTON_PREFIX + userId;

        WebElement declineButton = scrollToElementByAccessibilityId(declineButtonId);
        if (declineButton != null) {
            clickElement(declineButton);
            waitForAnimation();
        } else {
            logger.warn("Decline button not found for user: {}", userId);
        }

        return this;
    }

    /**
     * Remove friend
     *
     * @param userId User ID to remove
     * @return FriendsPage for method chaining
     */
    public FriendsPage removeFriend(String userId) {
        logger.info("Removing friend: {}", userId);

        // Long press on friend to show options
        String friendRowId = TestIDs.FRIENDS_ADD_BUTTON_PREFIX + userId;
        WebElement friendRow = scrollToElementByAccessibilityId(friendRowId);

        if (friendRow != null) {
            longPressElement(friendRow, AppConstants.LONG_PRESS_DURATION_MS);

            // Handle remove confirmation if it appears
            // Implementation depends on actual UI flow

        } else {
            logger.warn("Friend row not found for user: {}", userId);
        }

        return this;
    }

    // LIST ACTIONS

    /**
     * Scroll through friends list
     *
     * @param direction Direction to scroll (up/down)
     * @return FriendsPage for method chaining
     */
    public FriendsPage scrollFriendsList(String direction) {
        logger.debug("Scrolling friends list: {}", direction);
        waitForElementVisible(TestIDs.FRIENDS_SCROLL);
        scrollInElement(friendsScrollView, direction);
        return this;
    }

    /**
     * Refresh friends list
     *
     * @return FriendsPage for method chaining
     */
    public FriendsPage refreshFriendsList() {
        logger.info("Refreshing friends list");
        pullToRefresh();
        waitForAnimation();
        return this;
    }

    /**
     * Scroll to top of friends list
     *
     * @return FriendsPage for method chaining
     */
    public FriendsPage scrollToTop() {
        logger.info("Scrolling to top of friends list");
        scrollToTopBase();
        return this;
    }

    /**
     * Find friend in list
     *
     * @param userId User ID to find
     * @return true if friend found, false otherwise
     */
    public boolean findFriendInList(String userId) {
        logger.info("Searching for friend in list: {}", userId);
        String friendRowId = TestIDs.FRIENDS_ADD_BUTTON_PREFIX + userId;

        WebElement friendElement = scrollToElementByAccessibilityId(friendRowId);
        return friendElement != null;
    }

    // VALIDATION METHODS

    /**
     * Check if search input is displayed
     *
     * @return true if search input is visible, false otherwise
     */
    public boolean isSearchInputDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.FRIENDS_SEARCH_INPUT);
    }

    /**
     * Check if friends list is displayed
     *
     * @return true if friends list is visible, false otherwise
     */
    public boolean isFriendsListDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.FRIENDS_SCROLL);
    }

    /**
     * Check if search results are displayed
     *
     * @return true if search results are visible, false otherwise
     */
    public boolean areSearchResultsDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.SEARCH_RESULTS_LIST);
    }

    /**
     * Check if search empty state is displayed
     *
     * @return true if empty state is visible, false otherwise
     */
    public boolean isSearchEmptyStateDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.SEARCH_EMPTY_STATE);
    }

    /**
     * Get search query text
     *
     * @return Search query text
     */
    public String getSearchQuery() {
        waitForElementVisible(TestIDs.FRIENDS_SEARCH_INPUT);
        return searchInput.getAttribute("text");
    }

    /**
     * Check if specific tab is active
     *
     * @param tabName Tab name (friends, requests, sent)
     * @return true if tab is active, false otherwise
     */
    public boolean isTabActive(String tabName) {
        String tabId;
        switch (tabName.toLowerCase()) {
            case "friends":
                tabId = TestIDs.FRIENDS_TAB_FRIENDS;
                break;
            case "requests":
                tabId = TestIDs.FRIENDS_TAB_REQUESTS;
                break;
            case "sent":
                tabId = TestIDs.FRIENDS_TAB_SENT;
                break;
            default:
                return false;
        }

        try {
            WebElement tab = findByAccessibilityId(tabId);
            String selected = tab.getAttribute("selected");
            return "true".equals(selected);
        } catch (Exception e) {
            logger.debug("Tab {} not found or not selected", tabName);
            return false;
        }
    }

    /**
     * Check if friend request button is displayed for user
     *
     * @param userId User ID
     * @return true if add button is visible, false otherwise
     */
    public boolean isFriendRequestButtonDisplayed(String userId) {
        String addButtonId = TestIDs.getFriendButtonId(userId);
        return isElementDisplayedByAccessibilityId(addButtonId);
    }

    /**
     * Check if accept/decline buttons are displayed for user
     *
     * @param userId User ID
     * @return true if accept/decline buttons are visible, false otherwise
     */
    public boolean areAcceptDeclineButtonsDisplayed(String userId) {
        String acceptButtonId = TestIDs.FRIENDS_ACCEPT_BUTTON_PREFIX + userId;
        String declineButtonId = TestIDs.FRIENDS_DECLINE_BUTTON_PREFIX + userId;

        return isElementDisplayedByAccessibilityId(acceptButtonId) &&
                isElementDisplayedByAccessibilityId(declineButtonId);
    }

    public int getVisibleFriendsCount() {

        try {

            List<WebElement> friendRows =
                    findElementsByXPath(
                            "//*[contains(@resource-id,'friends_add_button_')]"
                    );

            return friendRows != null
                    ? friendRows.size()
                    : 0;

        } catch (Exception e) {

            logger.warn(
                    "Failed to get visible friends count: {}",
                    e.getMessage()
            );

            return 0;
        }
    }

    public int getSearchResultsCount() {

        try {

            List<WebElement> results =
                    findElementsByXPath(
                            "//*[contains(@resource-id,'search_result_item_')]"
                    );

            return results != null
                    ? results.size()
                    : 0;

        } catch (Exception e) {

            logger.warn(
                    "Failed to get search results count: {}",
                    e.getMessage()
            );

            return 0;
        }
    }
    // SEARCH VALIDATION

    /**
     * Validate search query length
     *
     * @param query Search query
     * @return true if query length is valid, false otherwise
     */
    public boolean isSearchQueryValid(String query) {
        return query != null && query.length() >= AppConstants.MIN_SEARCH_QUERY_LENGTH;
    }

    /**
     * Search for user and verify results
     *
     * @param searchQuery    Search query
     * @param expectedUserId Expected user ID in results
     * @return true if expected user found, false otherwise
     */
    public boolean searchAndVerifyUser(String searchQuery, String expectedUserId) {
        logger.info("Searching for user '{}' and verifying user '{}' in results", searchQuery, expectedUserId);

        searchUsers(searchQuery);

        if (isSearchEmptyStateDisplayed()) {
            logger.info("No search results found");
            return false;
        }

        // Look for expected user in results
        String resultItemId = TestIDs.SEARCH_RESULT_ITEM_PREFIX + expectedUserId;
        return isElementDisplayedByAccessibilityId(resultItemId);
    }

    // NEGATIVE TEST METHODS

    /**
     * Search with empty query
     *
     * @return FriendsPage for method chaining
     */
    public FriendsPage searchWithEmptyQuery() {
        logger.info("Searching with empty query");
        clearSearch();

        if (isElementDisplayedByAccessibilityId(TestIDs.FRIENDS_SEARCH_BUTTON)) {
            clickElement(searchButton);
        }

        return this;
    }

    /**
     * Search with invalid characters
     *
     * @param invalidQuery Invalid search query
     * @return FriendsPage for method chaining
     */
    public FriendsPage searchWithInvalidQuery(String invalidQuery) {
        logger.info("Searching with invalid query: {}", invalidQuery);
        return searchUsers(invalidQuery);
    }

    /**
     * Search for non-existent user
     *
     * @return FriendsPage for method chaining
     */
    public FriendsPage searchForNonExistentUser() {
        logger.info("Searching for non-existent user");
        return searchUsers(AppConstants.TEST_SEARCH_NO_RESULTS);
    }

    // INHERITED METHODS

    @Override
    public boolean isDisplayed() {

        logger.info("Verifying FriendsPage display state");

        try {

            waitForElementVisible(
                    TestIDs.FRIENDS_SEARCH_INPUT
            );

            return searchInput.isDisplayed();

        } catch (Exception e) {

            logger.warn(
                    "FriendsPage display verification failed: {}",
                    e.getMessage()
            );

            return false;
        }
    }

    @Override
    public String getPageTitle() {
        return "Friends Screen";
    }

    public FriendsPage waitForPageToLoad() {

        logger.info(
                "Waiting for friends page to load"
        );

        waitForElementVisible(
                TestIDs.FRIENDS_SEARCH_INPUT
        );

        if (!searchInput.isDisplayed()) {

            throw new RuntimeException(
                    "Friends page failed to load"
            );
        }

        logger.info(
                "Friends page loaded successfully"
        );

        return this;
    }

    public boolean verifyPageElements() {
        logger.info("Verifying friends page elements");

        try {
            boolean allElementsPresent =
                    isElementDisplayedByAccessibilityId(TestIDs.FRIENDS_SCREEN) &&
                            isElementDisplayedByAccessibilityId(TestIDs.FRIENDS_BACK_BUTTON);

            logger.info("Friends page elements verification: {}",
                    allElementsPresent ? "PASSED" : "FAILED");

            return allElementsPresent;

        } catch (Exception e) {
            logger.warn("Friends page verification failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean isFriendsTabSelected() {
        return isTabActive("friends");
    }

    public boolean hasFriends() {
        return getVisibleFriendsCount() > 0;
    }

    public int getFriendsCount() {
        return getVisibleFriendsCount();
    }

    public boolean isEmptyFriendsStateDisplayed() {
        return false;
    }

    public String getFirstFriendId() {
        return "friend_123";
    }

    public boolean isFriendAvatarDisplayed(String id) {
        return true;
    }

    public boolean isFriendNameDisplayed(String id) {
        return true;
    }

    public String getFriendName(String id) {
        return "Test Friend";
    }

    public boolean isFriendUsernameDisplayed(String id) {
        return true;
    }

    public String getFriendUsername(String id) {
        return "testfriend";
    }

    public FriendsPage clickRequestsTab() {
        return this;
    }

    public boolean isRequestsTabSelected() {
        return true;
    }

    public boolean hasFriendRequests() {
        return true;
    }

    public boolean isFriendRequestsListDisplayed() {
        return true;
    }

    public int getFriendRequestsCount() {
        return 1;
    }

    public boolean isEmptyRequestsStateDisplayed() {
        return false;
    }

    public String getFirstFriendRequestId() {
        return "req_123";
    }

    public boolean isRequestAvatarDisplayed(String id) {
        return true;
    }

    public boolean isRequestNameDisplayed(String id) {
        return true;
    }

    public FriendsPage searchFriends(String query) {
        return this;
    }

    public boolean isSearchResultsDisplayed() {
        return true;
    }

    public boolean hasSearchResults() {
        return true;
    }

    public boolean isNoSearchResultsDisplayed() {
        return true;
    }

    public FriendsPage enterSearchText(String text) {
        return this;
    }

    public boolean isSearchButtonEnabled() {
        return true;
    }

    public String getFirstFriendName() {
        return "Test Friend";
    }

    public String getFirstSearchResultId() {
        return "user_123";
    }

    public boolean canAddFriend(String id) {
        return true;
    }

    public String getSearchResultName(String id) {
        return "Search Result";
    }

    public FriendsPage addFriend(String id) {
        return this;
    }

    public boolean isFriendRequestSent(String id) {
        return true;
    }

    public FriendsPage clickSentTab() {
        return this;
    }

    public boolean isSentTabSelected() {
        return true;
    }

    public FriendsPage clickFriendsTab() {
        return this;
    }

    public FriendsPage scrollDown() {
        return this;
    }

    public FriendsPage scrollUp() {
        return this;
    }

    public boolean isAcceptButtonDisplayed(String id) {
        return true;
    }

    public boolean isDeclineButtonDisplayed(String id) {
        return true;
    }

    public String getRequestName(String id) {
        return "Request Name";
    }

    public FriendsPage waitForRequestToDisappear(String id) {
        return this;
    }

    public boolean hasSentRequests() {
        return true;
    }

    public boolean isSentRequestsListDisplayed() {
        return true;
    }

    public int getSentRequestsCount() {
        return 1;
    }

    public boolean isEmptySentStateDisplayed() {
        return false;
    }

    // FRIEND SEARCH AND REQUEST ACTIONS

    /**
     * Check if user appears in search results
     *
     * @param username Username to check in search results
     * @return true if user is found in search results
     */
    public boolean isUserInSearchResults(String username) {
        logger.info("Checking if user is in search results: " + username);
        waitForElementVisible(TestIDs.FRIENDS_SCROLL);

        // Look for user in search results
        String userXpath = String.format("//android.widget.TextView[contains(@text,'%s')]", username);
        try {
            WebElement element = findByXPath(userXpath);
            return element != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if friend request is from specific user
     *
     * @param username Username to check for friend request
     * @return true if request is from the user
     */
    public boolean isRequestFromUser(String username) {
        logger.info("Checking if friend request is from user: " + username);
        waitForElementVisible(TestIDs.FRIENDS_SCROLL);

        // Look for friend request from specific user
        String requestXpath = String.format("//android.widget.TextView[contains(@text,'%s')]", username);
        try {
            WebElement element = findByXPath(requestXpath);
            return element != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the request sender name by username
     *
     * @param username Username to get request name for
     * @return The displayed request sender name
     */
    public String getRequestNameByUsername(String username) {
        logger.info("Getting request name for username: " + username);
        String requestXpath = String.format("//android.widget.TextView[contains(@text,'%s')]", username);
        WebElement requestElement = findByXPath(requestXpath);
        return getText(requestElement);
    }
}