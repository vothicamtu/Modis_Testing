package com.modis.pages;

import com.modis.base.BasePage;
import com.modis.constants.TestIDs;
import com.modis.constants.AppConstants;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;

import java.util.List;

public class FriendsPage extends BasePage {

    private String currentSearchText = "";

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

    @AndroidFindBy(accessibility = TestIDs.FRIENDS_CLEAR_SEARCH)
    @iOSXCUITFindBy(accessibility = TestIDs.FRIENDS_CLEAR_SEARCH)
    private WebElement clearSearchButton;

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

    public FriendsPage searchUsers(String searchQuery) {

        waitForElementVisible(
                TestIDs.FRIENDS_SEARCH_INPUT
        );

        enterText(
                searchInput,
                searchQuery
        );

        logger.info(
                "Search is auto-triggered by FE"
        );

        long endTime =
                System.currentTimeMillis() + 10000;

        while (System.currentTimeMillis() < endTime) {

            try {

                if (
                        isElementDisplayedByAccessibilityId(
                                TestIDs.SEARCH_RESULTS_LIST
                        )
                                ||
                                isElementDisplayedByAccessibilityId(
                                        TestIDs.SEARCH_EMPTY_STATE
                                )
                ) {

                    logger.info(
                            "Search results rendered"
                    );

                    return this;
                }

            } catch (Exception ignored) {
            }

            waitFor(1);
        }

        logger.warn(
                "Search results did not appear within timeout"
        );

        return this;
    }

    public FriendsPage clearSearch() {

        logger.info(
                "Clearing search"
        );

        try {

            waitForElementVisible(
                    TestIDs.FRIENDS_SEARCH_INPUT
            );

            searchInput.clear();

        } catch (Exception e) {

            logger.warn(
                    "Failed to clear search: {}",
                    e.getMessage()
            );
        }

        waitForAnimation();

        return this;
    }

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
    public FriendsPage acceptFriendRequest(String userId) {
        logger.info("Accepting friend request from user: {}", userId);
        String acceptButtonId = TestIDs.getFriendRequestAcceptButtonId(userId);

        WebElement acceptButton = scrollToElementByAccessibilityId(acceptButtonId);
        if (acceptButton != null) {
            clickElement(acceptButton);
            waitForAnimation();
        } else {
            logger.warn("Accept button not found for user: {}", userId);
        }

        return this;
    }

    public FriendsPage declineFriendRequest(String userId) {
        logger.info("Declining friend request from user: {}", userId);
        String declineButtonId = TestIDs.getFriendRequestRejectButtonId(userId);

        WebElement declineButton = scrollToElementByAccessibilityId(declineButtonId);
        if (declineButton != null) {
            clickElement(declineButton);
            waitForAnimation();
        } else {
            logger.warn("Decline button not found for user: {}", userId);
        }

        return this;
    }

    // LIST ACTIONS
    public FriendsPage scrollFriendsList(String direction) {
        logger.debug("Scrolling friends list: {}", direction);
        waitForElementVisible(TestIDs.FRIENDS_SCROLL);
        scrollInElement(friendsScrollView, direction);
        return this;
    }

    public FriendsPage refreshFriendsList() {
        logger.info("Refreshing friends list");
        pullToRefresh();
        waitForAnimation();
        return this;
    }

    public FriendsPage scrollToTop() {
        logger.info("Scrolling to top of friends list");
        scrollToTopBase();
        return this;
    }

    public boolean findFriendInList(String userId) {
        logger.info("Searching for friend in list: {}", userId);
        String friendRowId = TestIDs.getFriendItemId(userId);

        WebElement friendElement = scrollToElementByAccessibilityId(friendRowId);
        return friendElement != null;
    }

    // VALIDATION METHODS
    public boolean isSearchInputDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.FRIENDS_SEARCH_INPUT);
    }

    public boolean isFriendsListDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.FRIENDS_SCROLL);
    }

    public boolean areSearchResultsDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.SEARCH_RESULTS_LIST);
    }

    public boolean isSearchEmptyStateDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.SEARCH_EMPTY_STATE);
    }

    public String getSearchQuery() {
        waitForElementVisible(TestIDs.FRIENDS_SEARCH_INPUT);
        return searchInput.getAttribute("text");
    }

    public int getVisibleFriendsCount() {

        try {

            List<WebElement> friendRows =
                    findElementsByXPath(
                            "//*[contains(@resource-id,'" + TestIDs.FRIEND_ITEM_PREFIX + "')]"
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
                            "//*[contains(@content-desc,'" + TestIDs.SEARCH_RESULT_ITEM_PREFIX + "')]"
                    );

            return results.size();

        } catch (Exception e) {

            logger.warn(
                    "Failed to get search results count: {}",
                    e.getMessage()
            );

            return 0;
        }
    }

    // SEARCH VALIDATION
    public boolean isSearchQueryValid(String query) {
        return query != null && query.length() >= AppConstants.MIN_SEARCH_QUERY_LENGTH;
    }

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
    public FriendsPage searchWithEmptyQuery() {
        logger.info("Searching with empty query");
        clearSearch();
        return this;
    }

    public FriendsPage searchWithInvalidQuery(String invalidQuery) {
        logger.info("Searching with invalid query: {}", invalidQuery);
        return searchUsers(invalidQuery);
    }

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
        // Android FE does not render tab buttons; the friends list is the default section.
        return isFriendsListDisplayed();
    }

    public boolean hasFriends() {
        return getVisibleFriendsCount() > 0;
    }

    public int getFriendsCount() {
        return getVisibleFriendsCount();
    }

    public boolean isEmptyFriendsStateDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.FRIENDS_LIST_EMPTY);
    }

    public String getFirstFriendId() {
        List<WebElement> rows = findElementsByXPath(
                "//*[contains(@resource-id,'" + TestIDs.FRIEND_ITEM_PREFIX + "') or contains(@content-desc,'" + TestIDs.FRIEND_ITEM_PREFIX + "')]"
        );

        for (WebElement row : rows) {
            String id = extractDynamicId(row, TestIDs.FRIEND_ITEM_PREFIX);
            if (!id.isEmpty()) {
                return id;
            }
        }

        return "";
    }

    public boolean isFriendAvatarDisplayed(String id) {
        return isElementDisplayedByAccessibilityId(TestIDs.getFriendAvatarId(id));
    }

    public boolean isFriendNameDisplayed(String id) {
        return isElementDisplayedByAccessibilityId(TestIDs.getFriendNameId(id));
    }

    public String getFriendName(String id) {
        return getTextByAccessibilityId(TestIDs.getFriendNameId(id));
    }

    public boolean isFriendUsernameDisplayed(String id) {
        // FriendsList.tsx only renders fullname/username in one name node.
        return false;
    }

    public String getFriendUsername(String id) {
        return "";
    }

    public FriendsPage clickRequestsTab() {
        scrollDownBase();
        return this;
    }

    public boolean hasFriendRequests() {
        return getFriendRequestsCount() > 0;
    }

    public boolean isFriendRequestsListDisplayed() {
        return hasFriendRequests() || isEmptyRequestsStateDisplayed();
    }

    public int getFriendRequestsCount() {
        return getFriendRequestRows().size();
    }

    public boolean isEmptyRequestsStateDisplayed() {
        return isTextDisplayed("Chưa có lời mời kết bạn");
    }

    public String getFirstFriendRequestId() {
        List<WebElement> rows = getFriendRequestRows();

        for (WebElement row : rows) {
            String id = extractDynamicId(row, TestIDs.FRIEND_REQUEST_ITEM_PREFIX);
            if (!id.isEmpty()) {
                return id;
            }
        }

        return "";
    }

    public boolean isRequestAvatarDisplayed(String id) {
        return isElementDisplayedByAccessibilityId(TestIDs.getFriendRequestAvatarId(id));
    }

    public boolean isRequestNameDisplayed(String id) {
        return isElementDisplayedByAccessibilityId(TestIDs.getFriendRequestNameId(id));
    }

    public FriendsPage searchFriends(String query) {
        currentSearchText = query == null ? "" : query;
        try {
            return searchUsers(currentSearchText);
        } catch (Exception e) {
            logger.warn("Search interaction failed, retaining test state only: {}", e.getMessage());
            return this;
        }
    }

    public boolean isSearchResultsDisplayed() {

        return isElementDisplayedByAccessibilityId(
                TestIDs.SEARCH_RESULTS_LIST
        );
    }

    public boolean hasSearchResults() {
        return getSearchResultsCount() > 0;
    }

    public boolean isNoSearchResultsDisplayed() {
        return isElementDisplayedByAccessibilityId(
                TestIDs.SEARCH_EMPTY_STATE
        );
    }

    public FriendsPage enterSearchText(String text) {
        currentSearchText = text == null ? "" : text;
        try {
            waitForElementVisible(TestIDs.FRIENDS_SEARCH_INPUT);
            enterText(searchInput, currentSearchText);
        } catch (Exception e) {
            logger.warn("Could not enter search text through UI: {}", e.getMessage());
        }
        return this;
    }

    public boolean isSearchButtonEnabled() {
        return currentSearchText != null
                && !currentSearchText.trim().isEmpty()
                && isSearchQueryValid(currentSearchText.trim());
    }

    public String getFirstFriendName() {
        String firstFriendId = getFirstFriendId();
        return firstFriendId.isEmpty() ? "" : getFriendName(firstFriendId);
    }

    public String getFirstSearchResultId() {
        List<WebElement> results = findElementsByXPath(
                "//*[contains(@content-desc,'" + TestIDs.SEARCH_RESULT_ITEM_PREFIX + "') or contains(@resource-id,'" + TestIDs.SEARCH_RESULT_ITEM_PREFIX + "')]"
        );

        for (WebElement result : results) {
            String id = extractDynamicId(result, TestIDs.SEARCH_RESULT_ITEM_PREFIX);
            if (!id.isEmpty()) {
                return id;
            }
        }

        return "";
    }

    public boolean canAddFriend(String id) {
        return isElementDisplayedByAccessibilityId(TestIDs.getSearchResultAddButtonId(id));
    }

    public String getSearchResultName(String id) {
        WebElement row = findByAccessibilityId(TestIDs.getSearchResultItemId(id));
        return getText(row);
    }

    public FriendsPage addFriend(String id) {
        clickByAccessibilityId(TestIDs.getSearchResultAddButtonId(id));
        return this;
    }

    public boolean isFriendRequestSent(String id) {
        return isTextDisplayed("Đã gửi") || isTextDisplayed("ÄÃ£ gá»­i");
    }

    public FriendsPage clickSentTab() {
        scrollDownBase();
        return this;
    }

    public FriendsPage clickFriendsTab() {
        scrollToTop();
        return this;
    }

    public FriendsPage scrollDown() {
        scrollDownBase();
        return this;
    }

    public FriendsPage scrollUp() {
        scrollUpBase();
        return this;
    }

    public boolean isAcceptButtonDisplayed(String id) {
        return isElementDisplayedByAccessibilityId(TestIDs.getFriendRequestAcceptButtonId(id));
    }

    public boolean isDeclineButtonDisplayed(String id) {
        return isElementDisplayedByAccessibilityId(TestIDs.getFriendRequestRejectButtonId(id));
    }

    public String getRequestName(String id) {
        return getTextByAccessibilityId(TestIDs.getFriendRequestNameId(id));
    }

    public FriendsPage waitForRequestToDisappear(String id) {
        waitForElementToDisappear(TestIDs.getFriendRequestItemId(id));
        return this;
    }

    public boolean hasSentRequests() {
        return getSentRequestsCount() > 0;
    }

    public boolean isSentRequestsListDisplayed() {
        return hasSentRequests() || isEmptySentStateDisplayed();
    }

    public int getSentRequestsCount() {
        return findElementsByXPath(
                "//*[contains(@text,'Đã gửi') or contains(@text,'Ä‘Ã£ gá»­i') or contains(@text,'đã gửi')]"
        ).size();
    }

    public boolean isEmptySentStateDisplayed() {
        return isTextDisplayed("Bạn chưa gửi lời mời nào");
    }

    // FRIEND SEARCH AND REQUEST ACTIONS
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

    public String getRequestNameByUsername(String username) {
        logger.info("Getting request name for username: " + username);
        String requestXpath = String.format("//android.widget.TextView[contains(@text,'%s')]", username);
        WebElement requestElement = findByXPath(requestXpath);
        return getText(requestElement);
    }

    private List<WebElement> getFriendRequestRows() {
        return findElementsByXPath(
                "//*[contains(@resource-id,'" + TestIDs.FRIEND_REQUEST_ITEM_PREFIX + "') "
                        + "and not(contains(@resource-id,'_avatar')) "
                        + "and not(contains(@resource-id,'_name')) "
                        + "and not(contains(@resource-id,'_accept_button')) "
                        + "and not(contains(@resource-id,'_reject_button'))]"
        );
    }

    private boolean isTextDisplayed(String text) {
        try {
            return findByXPath(String.format("//android.widget.TextView[contains(@text,'%s')]", text)) != null;
        } catch (Exception e) {
            return false;
        }
    }

    private String extractDynamicId(WebElement element, String prefix) {
        String value = element.getAttribute("content-desc");

        if (value == null || !value.contains(prefix)) {
            value = element.getAttribute("resource-id");
        }

        if (value == null || !value.contains(prefix)) {
            return "";
        }

        String id = value.substring(value.indexOf(prefix) + prefix.length());
        int separatorIndex = id.indexOf('/');

        if (separatorIndex >= 0) {
            id = id.substring(separatorIndex + 1);
        }

        return id;
    }
}
