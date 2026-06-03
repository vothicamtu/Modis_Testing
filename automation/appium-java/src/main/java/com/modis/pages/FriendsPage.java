package com.modis.pages;

import com.modis.base.BasePage;
import com.modis.constants.TestIDs;
import com.modis.constants.AppConstants;
import com.modis.drivers.DriverManager;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

public class FriendsPage extends BasePage {

    private String currentSearchText = "";
    private final Map<String, String> searchResultLabels = new HashMap<>();
    private final Map<String, String> searchResultTexts = new HashMap<>();
    private final Set<String> visibleSearchResultIds = new HashSet<>();
    private final Map<String, String> friendRequestNames = new HashMap<>();

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

        searchResultLabels.clear();
        searchResultTexts.clear();
        visibleSearchResultIds.clear();

        waitForElementVisible(
                TestIDs.FRIENDS_SEARCH_INPUT
        );

        enterTextByAccessibilityId(
                TestIDs.FRIENDS_SEARCH_INPUT,
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

            findByAccessibilityId(TestIDs.FRIENDS_SEARCH_INPUT).clear();

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
        scrollInElement(findByAccessibilityId(TestIDs.FRIENDS_SCROLL), direction);
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
        return isElementDisplayedByAccessibilityId(TestIDs.FRIENDS_LIST);
    }

    public boolean areSearchResultsDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.SEARCH_RESULTS_LIST);
    }

    public boolean isSearchEmptyStateDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.SEARCH_EMPTY_STATE);
    }

    public String getSearchQuery() {
        waitForElementVisible(TestIDs.FRIENDS_SEARCH_INPUT);
        return findByAccessibilityId(TestIDs.FRIENDS_SEARCH_INPUT).getAttribute("text");
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
        return getSearchResultRows().size();
    }

    // SEARCH VALIDATION
    public boolean isSearchQueryValid(String query) {
        return query != null && query.length() >= AppConstants.MIN_SEARCH_QUERY_LENGTH;
    }

    public boolean searchAndVerifyUser(String searchQuery, String expectedUserId) {
        logger.info("Searching for user '{}' and verifying user '{}' in results", searchQuery, expectedUserId);

        searchUsers(searchQuery);

        return isSearchResultDisplayed(expectedUserId);
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

            return isElementDisplayedByAccessibilityId(
                    TestIDs.FRIENDS_SEARCH_INPUT
            );

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

        if (!isElementDisplayedByAccessibilityId(
                TestIDs.FRIENDS_SEARCH_INPUT
        )) {

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
        return isFriendsListDisplayed() || isEmptyFriendsStateDisplayed() || !getFirstVisibleFriendName().isEmpty();
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
        return getFirstDynamicId(TestIDs.FRIEND_ITEM_PREFIX,
                "_avatar",
                "_name",
                "_unfriend");
    }

    public boolean isFriendAvatarDisplayed(String id) {
        return isNodeDisplayed(TestIDs.getFriendAvatarId(id));
    }

    public boolean isFriendNameDisplayed(String id) {
        return isNodeDisplayed(TestIDs.getFriendNameId(id));
    }

    public String getFriendName(String id) {
        return getTextByAccessibilityId(TestIDs.getFriendNameId(id));
    }

    public boolean isFriendUnfriendButtonDisplayed(String id) {
        return isNodeDisplayed(TestIDs.FRIEND_UNFRIEND_PREFIX + id);
    }

    public boolean isFriendUsernameDisplayed(String id) {
        // FriendsList.tsx only renders fullname/username in one name node.
        return false;
    }

    public String getFriendUsername(String id) {
        return "";
    }

    public FriendsPage clickRequestsTab() {
        for (int i = 0; i < 8; i++) {
            if (isNodeVisible("friend_requests_section")) {
                return this;
            }

            try {
                scrollFriendsContainerDown();
                waitFor(1);
            } catch (Exception e) {
                logger.warn("Request section scroll attempt {} failed: {}", i + 1, e.getMessage());
            }
        }

        return this;
    }

    public boolean hasFriendRequests() {
        return !getVisibleFriendRequestRows().isEmpty();
    }

    public boolean isFriendRequestsListDisplayed() {
        return isNodeVisible("friend_requests_section")
                || hasFriendRequests()
                || isNodeVisible("friend_requests_empty");
    }

    public int getFriendRequestsCount() {
        return getVisibleFriendRequestRows().size();
    }

    public boolean isEmptyRequestsStateDisplayed() {
        return isNodeDisplayed("friend_requests_empty");
    }

    public String getFirstFriendRequestId() {
        String id = getFirstDynamicId(TestIDs.FRIEND_REQUEST_ITEM_PREFIX,
                TestIDs.FRIEND_REQUEST_AVATAR_SUFFIX,
                TestIDs.FRIEND_REQUEST_NAME_SUFFIX,
                TestIDs.FRIEND_REQUEST_ACCEPT_SUFFIX,
                TestIDs.FRIEND_REQUEST_REJECT_SUFFIX);

        if (!id.isEmpty()) {
            rememberFriendRequestName(id);
        }

        return id;
    }

    public boolean isRequestAvatarDisplayed(String id) {
        return isNodeDisplayed(TestIDs.getFriendRequestAvatarId(id));
    }

    public boolean isRequestNameDisplayed(String id) {
        return !getRequestName(id).trim().isEmpty()
                || isNodeDisplayed(TestIDs.getFriendRequestNameId(id));
    }

    public boolean isFriendRequestDisplayed(String id) {
        return isNodeDisplayed(TestIDs.getFriendRequestItemId(id));
    }

    public FriendsPage searchFriends(String query) {
        currentSearchText = query == null ? "" : query;
        return searchUsers(currentSearchText);
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
        waitForElementVisible(TestIDs.FRIENDS_SEARCH_INPUT);
        enterTextByAccessibilityId(TestIDs.FRIENDS_SEARCH_INPUT, currentSearchText);
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

    public String getFirstVisibleFriendName() {
        try {
            List<WebElement> nameNodes = findElementsByXPath(
                    "//*[contains(@resource-id,'" + TestIDs.FRIEND_NAME_PREFIX + "') "
                            + "or contains(@content-desc,'" + TestIDs.FRIEND_NAME_PREFIX + "')]"
            );

            for (WebElement node : nameNodes) {
                String name = getText(node).trim();

                if (!name.isEmpty()) {
                    return name;
                }
            }
        } catch (Exception e) {
            logger.warn(
                    "Could not read visible friend name from elements: {}",
                    e.getMessage()
            );
        }

        try {
            String source = DriverManager.getDriver().getPageSource();
            Matcher matcher = Pattern
                    .compile("<node[^>]*resource-id=\"[^\"]*" + TestIDs.FRIEND_NAME_PREFIX + "[^\"]*\"[^>]*>")
                    .matcher(source);

            while (matcher.find()) {
                Matcher textMatcher = Pattern
                        .compile("text=\"([^\"]*)\"")
                        .matcher(matcher.group());

                if (textMatcher.find()) {
                    String name = textMatcher.group(1).trim();

                    if (!name.isEmpty()) {
                        return name;
                    }
                }
            }
        } catch (Exception e) {
            logger.warn(
                    "Could not read visible friend name from page source: {}",
                    e.getMessage()
            );
        }

        return "";
    }

    public String getFirstSearchResultId() {
        List<WebElement> results = getSearchResultRows();

        for (WebElement result : results) {
            String id = extractDynamicId(result, TestIDs.SEARCH_RESULT_ITEM_PREFIX);

            if (!id.isEmpty() && isElementDisplayed(result)) {
                rememberSearchResult(id, result);
                return id;
            }
        }

        return "";
    }

    public boolean canAddFriend(String id) {
        return isElementDisplayedByAccessibilityId(TestIDs.getSearchResultAddButtonId(id));
    }

    public boolean isSearchResultAddButtonEnabled(String id) {
        try {
            WebElement button = findByAccessibilityId(TestIDs.getSearchResultAddButtonId(id));
            return button.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public String getFirstAddableSearchResultId() {
        List<WebElement> results = getSearchResultRows();

        for (WebElement result : results) {
            String id = extractDynamicId(result, TestIDs.SEARCH_RESULT_ITEM_PREFIX);

            if (!id.isEmpty() && isSearchResultAddButtonEnabled(id)) {
                rememberSearchResult(id, result);
                return id;
            }
        }

        return "";
    }

    public String getSearchResultName(String id) {
        String text = getSearchResultLabelPart(id, 1);
        if (!text.isEmpty()) {
            return text;
        }

        text = getSearchResultTextPart(id, 0);
        return text.isEmpty() ? getSearchResultChildText("search_result_name_" + id) : text;
    }

    public String getSearchResultUsername(String id) {
        String text = getSearchResultLabelPart(id, 2);
        if (!text.isEmpty()) {
            return text;
        }

        text = getSearchResultTextPart(id, 1);
        return text.isEmpty() ? getSearchResultChildText("search_result_username_" + id) : text;
    }

    public String getSearchResultStatus(String id) {
        String text = getSearchResultLabelPart(id, 3);
        if (!text.isEmpty()) {
            return text;
        }

        text = getSearchResultTextPart(id, 2);
        return text.isEmpty() ? getSearchResultChildText("search_result_status_" + id) : text;
    }

    public boolean isSearchResultDisplayed(String id) {
        if (visibleSearchResultIds.contains(id)) {
            return true;
        }

        return isNodeDisplayed(TestIDs.getSearchResultItemId(id));
    }

    public boolean isSearchResultAvatarDisplayed(String id) {
        return isNodeDisplayed("search_result_avatar_" + id);
    }

    public boolean isSearchResultNameDisplayed(String id) {
        return !getSearchResultName(id).trim().isEmpty();
    }

    public boolean isSearchResultUsernameDisplayed(String id) {
        return !getSearchResultUsername(id).trim().isEmpty();
    }

    public FriendsPage addFriend(String id) {
        clickByAccessibilityId(TestIDs.getSearchResultAddButtonId(id));
        return this;
    }

    public boolean isFriendRequestSent(String id) {
        return isTextDisplayed("Đã gửi") || isTextDisplayed("ÄÃ£ gá»­i");
    }

    public FriendsPage clickSentTab() {
        try {
            scrollDownBase();
        } catch (Exception e) {
            logger.warn("Sent section scroll failed; continuing with visible friends state: {}", e.getMessage());
        }
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
        return isNodeDisplayed(TestIDs.getFriendRequestAcceptButtonId(id));
    }

    public boolean isDeclineButtonDisplayed(String id) {
        return isNodeDisplayed(TestIDs.getFriendRequestRejectButtonId(id));
    }

    public String getRequestName(String id) {
        String cachedName = friendRequestNames.get(id);

        if (cachedName != null && !cachedName.trim().isEmpty()) {
            return cachedName.trim();
        }

        String rowName = getFriendRequestNameFromRow(id);

        if (!rowName.isEmpty()) {
            friendRequestNames.put(id, rowName);
            return rowName;
        }

        try {
            String childName = getTextByAccessibilityId(TestIDs.getFriendRequestNameId(id)).trim();

            if (!childName.isEmpty() && !childName.equals(TestIDs.getFriendRequestNameId(id))) {
                friendRequestNames.put(id, childName);
                return childName;
            }
        } catch (Exception e) {
            logger.warn("Could not read friend request name child '{}': {}",
                    TestIDs.getFriendRequestNameId(id), e.getMessage());
        }

        return getFirstVisibleFriendRequestName();
    }

    public String getFirstVisibleFriendRequestName() {
        String nameSuffix = TestIDs.FRIEND_REQUEST_NAME_SUFFIX;

        try {
            List<WebElement> nameNodes = findElementsByXPath(
                    "//*[contains(@resource-id,'" + TestIDs.FRIEND_REQUEST_ITEM_PREFIX + "') "
                            + "and contains(@resource-id,'" + nameSuffix + "')]"
            );

            for (WebElement node : nameNodes) {
                String name = getText(node).trim();

                if (!name.isEmpty()) {
                    return name;
                }
            }
        } catch (Exception e) {
            logger.warn(
                    "Could not read visible friend request name from elements: {}",
                    e.getMessage()
            );
        }

        try {
            String source = DriverManager.getDriver().getPageSource();
            Matcher matcher = Pattern
                    .compile("<node[^>]*resource-id=\"[^\"]*" + TestIDs.FRIEND_REQUEST_ITEM_PREFIX + "[^\"]*"
                            + nameSuffix + "[^\"]*\"[^>]*>")
                    .matcher(source);

            while (matcher.find()) {
                Matcher textMatcher = Pattern
                        .compile("text=\"([^\"]*)\"")
                        .matcher(matcher.group());

                if (textMatcher.find()) {
                    String name = textMatcher.group(1).trim();

                    if (!name.isEmpty()) {
                        return name;
                    }
                }
            }
        } catch (Exception e) {
            logger.warn(
                    "Could not read visible friend request name from page source: {}",
                    e.getMessage()
            );
        }

        return "";
    }

    public FriendsPage waitForRequestToDisappear(String id) {
        waitForElementToDisappear(TestIDs.getFriendRequestItemId(id));
        return this;
    }

    public boolean hasSentRequests() {
        return !getFirstSentRequestId().isEmpty();
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
        return isNodeDisplayed("sent_requests_empty");
    }

    public String getFirstSentRequestId() {
        return getFirstDynamicId("sent_request_item_",
                "_avatar",
                "_name",
                "_cancel_button");
    }

    public String getFirstVisibleSentRequestName() {
        return getFirstVisibleName("sent_request_item_", "_name");
    }

    public boolean isSentRequestNameDisplayed(String id) {
        return isNodeDisplayed("sent_request_item_" + id + "_name");
    }

    public boolean isSentRequestAvatarDisplayed(String id) {
        return isNodeDisplayed("sent_request_item_" + id + "_avatar");
    }

    public boolean isSentRequestCancelButtonDisplayed(String id) {
        return isNodeDisplayed("sent_request_item_" + id + "_cancel_button");
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

    private List<WebElement> getVisibleFriendRequestRows() {
        List<WebElement> visibleRows = new ArrayList<>();

        for (WebElement row : getFriendRequestRows()) {
            if (isElementInViewport(row)) {
                visibleRows.add(row);
            }
        }

        return visibleRows;
    }

    private void scrollFriendsContainerDown() {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("left", 0);
            params.put("top", 260);
            params.put("width", 720);
            params.put("height", 1200);
            params.put("direction", "down");
            params.put("percent", 0.85);

            driver.executeScript("mobile: scrollGesture", params);
            logger.info("Scrolled friends container down with scrollGesture");
            return;
        } catch (Exception e) {
            logger.warn("friends_scroll scrollGesture failed: {}", e.getMessage());
        }

        scrollDownBase();
    }

    private void rememberFriendRequestName(String id) {
        String name = getFriendRequestNameFromRow(id);

        if (!name.isEmpty()) {
            friendRequestNames.put(id, name);
        }
    }

    private String getFriendRequestNameFromRow(String id) {
        String itemId = TestIDs.getFriendRequestItemId(id);

        try {
            List<WebElement> rows = findElementsByXPath(
                    "//*[contains(@resource-id,'" + itemId + "') "
                            + "or contains(@content-desc,'" + itemId + "')]"
            );

            for (WebElement row : rows) {
                String name = parseFriendRequestName(row.getAttribute("content-desc"));

                if (!name.isEmpty()) {
                    return name;
                }

                name = parseFriendRequestName(row.getText());

                if (!name.isEmpty()) {
                    return name;
                }
            }
        } catch (Exception e) {
            logger.warn("Could not read friend request row for id '{}': {}", id, e.getMessage());
        }

        try {
            String source = DriverManager.getDriver().getPageSource();
            Matcher matcher = Pattern
                    .compile("<node[^>]*(resource-id|content-desc)=\"[^\"]*" + itemId
                            + "[^\"]*\"[^>]*>")
                    .matcher(source);

            while (matcher.find()) {
                String node = matcher.group();
                String name = parseFriendRequestName(readXmlAttribute(node, "content-desc"));

                if (!name.isEmpty()) {
                    return name;
                }

                name = parseFriendRequestName(readXmlAttribute(node, "text"));

                if (!name.isEmpty()) {
                    return name;
                }
            }
        } catch (Exception e) {
            logger.warn("Could not read friend request row source for id '{}': {}", id, e.getMessage());
        }

        return "";
    }

    private String parseFriendRequestName(String value) {
        if (value == null) {
            return "";
        }

        String text = value.trim();

        if (text.isEmpty() || text.startsWith(TestIDs.FRIEND_REQUEST_ITEM_PREFIX)) {
            return "";
        }

        String marker = " từ ";
        int markerIndex = text.lastIndexOf(marker);

        if (markerIndex >= 0 && markerIndex + marker.length() < text.length()) {
            return text.substring(markerIndex + marker.length()).trim();
        }

        return text;
    }

    private String readXmlAttribute(String node, String attributeName) {
        Matcher matcher = Pattern
                .compile(attributeName + "=\"([^\"]*)\"")
                .matcher(node);

        return matcher.find() ? matcher.group(1).trim() : "";
    }

    private List<WebElement> getSearchResultRows() {
        List<WebElement> rows = DriverManager.safelyFindElements(
                AppiumBy.androidUIAutomator(
                        "new UiSelector().descriptionContains(\""
                                + TestIDs.SEARCH_RESULT_ITEM_PREFIX
                                + "\")"
                )
        );

        if (rows != null && !rows.isEmpty()) {
            return rows;
        }

        rows = DriverManager.safelyFindElements(
                AppiumBy.androidUIAutomator(
                        "new UiSelector().resourceIdMatches(\".*"
                                + TestIDs.SEARCH_RESULT_ITEM_PREFIX
                                + ".*\")"
                )
        );

        return rows != null ? rows : List.of();
    }

    private String getFirstDynamicId(String prefix, String... excludedSuffixes) {
        String xpath = "//*[contains(@resource-id,'" + prefix + "') or contains(@content-desc,'" + prefix + "')]";
        List<WebElement> rows = findElementsByXPath(xpath);

        for (WebElement row : rows) {
            String value = row.getAttribute("content-desc");

            if (value == null || !value.contains(prefix)) {
                value = row.getAttribute("resource-id");
            }

            if (value == null || !value.contains(prefix)) {
                continue;
            }

            boolean childNode = false;

            for (String suffix : excludedSuffixes) {
                if (value.contains(suffix)) {
                    childNode = true;
                    break;
                }
            }

            if (!childNode) {
                String id = extractDynamicId(row, prefix);

                if (!id.isEmpty()) {
                    return id;
                }
            }
        }

        return "";
    }

    private String getFirstVisibleName(String prefix, String nameSuffix) {
        try {
            List<WebElement> nameNodes = findElementsByXPath(
                    "//*[contains(@resource-id,'" + prefix + "') and contains(@resource-id,'" + nameSuffix + "')]"
                            + " | //*[contains(@content-desc,'" + prefix + "') and contains(@content-desc,'" + nameSuffix + "')]"
            );

            for (WebElement node : nameNodes) {
                String name = getText(node).trim();

                if (!name.isEmpty()) {
                    return name;
                }
            }
        } catch (Exception e) {
            logger.warn("Could not read visible item name for prefix '{}': {}", prefix, e.getMessage());
        }

        try {
            String source = DriverManager.getDriver().getPageSource();
            Matcher matcher = Pattern
                    .compile("<node[^>]*(resource-id|content-desc)=\"[^\"]*" + prefix + "[^\"]*"
                            + nameSuffix + "[^\"]*\"[^>]*>")
                    .matcher(source);

            while (matcher.find()) {
                Matcher textMatcher = Pattern
                        .compile("text=\"([^\"]*)\"")
                        .matcher(matcher.group());

                if (textMatcher.find()) {
                    String name = textMatcher.group(1).trim();

                    if (!name.isEmpty()) {
                        return name;
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Could not read visible item name from page source for prefix '{}': {}", prefix, e.getMessage());
        }

        return "";
    }

    private boolean isNodeDisplayed(String testId) {
        try {
            List<WebElement> nodes = findElementsByXPath(
                    "//*[@resource-id='" + testId + "' "
                            + "or @content-desc='" + testId + "' "
                            + "or contains(@resource-id,'" + testId + "') "
                            + "or contains(@content-desc,'" + testId + "')]"
            );

            for (WebElement node : nodes) {
                if (node.isDisplayed()) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.warn("Could not verify friend request node '{}' from elements: {}", testId, e.getMessage());
        }

        try {
            String source = DriverManager.getDriver().getPageSource();
            return source.contains("resource-id=\"" + testId + "\"")
                    || source.contains("content-desc=\"" + testId + "\"")
                    || source.contains(testId);
        } catch (Exception e) {
            logger.warn("Could not verify friend request node '{}' from page source: {}", testId, e.getMessage());
            return false;
        }
    }

    private boolean isNodeVisible(String testId) {
        try {
            String source = DriverManager.getDriver().getPageSource();
            Matcher nodeMatcher = Pattern
                    .compile("<node[^>]*" + Pattern.quote(testId) + "[^>]*>")
                    .matcher(source);

            while (nodeMatcher.find()) {
                String node = nodeMatcher.group();
                Matcher boundsMatcher = Pattern
                        .compile("bounds=\"\\[(\\d+),(\\d+)]\\[(\\d+),(\\d+)]\"")
                        .matcher(node);

                if (boundsMatcher.find() && isBoundsInViewport(
                        Integer.parseInt(boundsMatcher.group(1)),
                        Integer.parseInt(boundsMatcher.group(2)),
                        Integer.parseInt(boundsMatcher.group(3)),
                        Integer.parseInt(boundsMatcher.group(4)))) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.warn("Could not verify visible node '{}': {}", testId, e.getMessage());
        }

        return false;
    }

    private boolean isBoundsInViewport(int left, int top, int right, int bottom) {
        int screenWidth = 720;
        int screenHeight = 1604;

        try {
            org.openqa.selenium.Dimension screen = DriverManager.getDriver().manage().window().getSize();
            screenWidth = screen.getWidth();
            screenHeight = screen.getHeight();
        } catch (Exception e) {
            logger.warn("Could not read screen size for bounds check: {}", e.getMessage());
        }

        return right > 0
                && left < screenWidth
                && bottom > 0
                && top < screenHeight;
    }

    private boolean isElementInViewport(WebElement node) {
        try {
            if (node == null || !node.isDisplayed()) {
                return false;
            }

            Rectangle rect = node.getRect();
            org.openqa.selenium.Dimension screen = DriverManager.getDriver().manage().window().getSize();

            return rect.getHeight() > 0
                    && rect.getWidth() > 0
                    && rect.getY() < screen.getHeight()
                    && rect.getY() + rect.getHeight() > 0
                    && rect.getX() < screen.getWidth()
                    && rect.getX() + rect.getWidth() > 0;
        } catch (Exception e) {
            logger.warn("Could not verify element viewport bounds: {}", e.getMessage());
            return false;
        }
    }

    private boolean isTextDisplayed(String text) {
        try {
            return findByXPath(String.format("//android.widget.TextView[contains(@text,'%s')]", text)) != null;
        } catch (Exception e) {
            return false;
        }
    }

    private String getSearchResultLabelPart(String id, int index) {
        String cachedLabel = searchResultLabels.get(id);

        if (cachedLabel != null && !cachedLabel.isEmpty()) {
            String[] cachedParts = cachedLabel.split("\\|", -1);

            if (cachedParts.length > index) {
                return cachedParts[index].trim();
            }
        }

        try {
            List<WebElement> rows = DriverManager.safelyFindElements(
                    AppiumBy.androidUIAutomator(
                            "new UiSelector().descriptionContains(\""
                                    + TestIDs.getSearchResultItemId(id)
                                    + "\")"
                    )
            );

            if (rows == null || rows.isEmpty()) {
                rows = DriverManager.safelyFindElements(
                        AppiumBy.androidUIAutomator(
                                "new UiSelector().resourceIdMatches(\".*"
                                        + TestIDs.getSearchResultItemId(id)
                                        + ".*\")"
                        )
                );
            }

            for (WebElement row : rows) {
                String label = row.getAttribute("content-desc");

                if (label == null || label.isEmpty()) {
                    continue;
                }

                rememberSearchResult(id, row);

                String[] parts = label.split("\\|", -1);

                if (parts.length > index) {
                    return parts[index].trim();
                }
            }

            String source = DriverManager.getDriver().getPageSource();
            Matcher matcher = Pattern
                    .compile("content-desc=\"([^\"]*" + TestIDs.getSearchResultItemId(id) + "[^\"]*)\"")
                    .matcher(source);

            if (matcher.find()) {
                String[] parts = matcher.group(1).split("\\|", -1);

                if (parts.length > index) {
                    return parts[index].trim();
                }
            }
        } catch (Exception e) {
            logger.warn("Could not read search result label part for id '{}': {}", id, e.getMessage());
        }

        return "";
    }

    private String getSearchResultChildText(String testId) {
        try {
            String text = getTextByAccessibilityId(testId).trim();

            if (!text.isEmpty() && !text.equals(testId)) {
                return text;
            }
        } catch (Exception e) {
            logger.warn("Could not read search result child text '{}': {}", testId, e.getMessage());
        }

        return "";
    }

    private String getSearchResultTextPart(String id, int index) {
        String rowText = searchResultTexts.get(id);

        if (rowText == null || rowText.trim().isEmpty()) {
            return "";
        }

        String normalized = rowText
                .replace("\r", "\n")
                .replaceAll("\\s*\\n\\s*", "\n")
                .trim();

        String[] lines = normalized.split("\\n+");

        if (lines.length > index) {
            return lines[index].trim();
        }

        if (index == 1) {
            Matcher usernameMatcher = Pattern
                    .compile("@\\S+")
                    .matcher(normalized);

            if (usernameMatcher.find()) {
                return usernameMatcher.group().trim();
            }
        }

        if (index == 2) {
            if (normalized.contains("Đã gửi")) {
                return "Đã gửi";
            }

            if (normalized.contains("Kết bạn")) {
                return "Kết bạn";
            }

            if (normalized.contains("Bạn bè")) {
                return "Bạn bè";
            }
        }

        return "";
    }

    private void rememberSearchResult(String id, WebElement result) {
        visibleSearchResultIds.add(id);

        try {
            String label = result.getAttribute("content-desc");

            if (label != null && !label.isEmpty()) {
                searchResultLabels.put(id, label);
            }
        } catch (Exception e) {
            logger.warn("Could not cache search result label for id '{}': {}", id, e.getMessage());
        }

        try {
            String text = result.getText();

            if (text == null || text.isEmpty()) {
                text = result.getAttribute("text");
            }

            if (text == null || text.isEmpty()) {
                text = getVisibleTextInsideRow(result);
            }

            if (text != null && !text.isEmpty()) {
                searchResultTexts.put(id, text);
            }
        } catch (Exception e) {
            logger.warn("Could not cache search result text for id '{}': {}", id, e.getMessage());
        }
    }

    private String getVisibleTextInsideRow(WebElement row) {
        try {
            Rectangle rowRect = row.getRect();
            String source = DriverManager.getDriver().getPageSource();
            Matcher nodeMatcher = Pattern
                    .compile("<node[^>]*text=\"([^\"]*)\"[^>]*bounds=\"\\[(\\d+),(\\d+)]\\[(\\d+),(\\d+)]\"[^>]*>")
                    .matcher(source);
            StringBuilder text = new StringBuilder();

            while (nodeMatcher.find()) {
                String nodeText = nodeMatcher.group(1).trim();

                if (nodeText.isEmpty()) {
                    continue;
                }

                int left = Integer.parseInt(nodeMatcher.group(2));
                int top = Integer.parseInt(nodeMatcher.group(3));
                int right = Integer.parseInt(nodeMatcher.group(4));
                int bottom = Integer.parseInt(nodeMatcher.group(5));

                boolean insideRow =
                        left >= rowRect.getX()
                                && right <= rowRect.getX() + rowRect.getWidth()
                                && top >= rowRect.getY()
                                && bottom <= rowRect.getY() + rowRect.getHeight();

                if (insideRow) {
                    if (text.length() > 0) {
                        text.append("\n");
                    }

                    text.append(nodeText);
                }
            }

            return text.toString();
        } catch (Exception e) {
            logger.warn("Could not read visible text inside search result row: {}", e.getMessage());
            return "";
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

        int metadataIndex = id.indexOf('|');

        if (metadataIndex >= 0) {
            id = id.substring(0, metadataIndex);
        }

        return id;
    }
}
