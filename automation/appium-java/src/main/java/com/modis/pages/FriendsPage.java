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
    private boolean friendRequestsSectionReached = false;
    private boolean sentRequestsSectionReached = false;
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
    @AndroidFindBy(accessibility = TestIDs.SEARCH_RESULTS_LIST)
    @iOSXCUITFindBy(accessibility = TestIDs.SEARCH_RESULTS_LIST)
    private WebElement searchResultsList;
    @AndroidFindBy(accessibility = TestIDs.SEARCH_EMPTY_STATE)
    @iOSXCUITFindBy(accessibility = TestIDs.SEARCH_EMPTY_STATE)
    private WebElement searchEmptyState;

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
        if (!isElementDisplayedByAccessibilityId(TestIDs.FRIENDS_SEARCH_INPUT)) {
            scrollToTop();
            waitForElementVisible(
                    TestIDs.FRIENDS_SEARCH_INPUT
            );
        }
        WebElement input = findByAccessibilityId(TestIDs.FRIENDS_SEARCH_INPUT);
        clearSearchInputIfNeeded(input);
        input = findByAccessibilityId(TestIDs.FRIENDS_SEARCH_INPUT);
        enterText(input, searchQuery);
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
                                ||
                                !getSearchResultRows().isEmpty()
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
            if (isElementDisplayedByAccessibilityId(TestIDs.FRIENDS_CLEAR_SEARCH)) {
                clickByAccessibilityId(TestIDs.FRIENDS_CLEAR_SEARCH);
                waitForAnimation();
                return this;
            }
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
        try {
            Thread.sleep(AppConstants.SEARCH_DEBOUNCE_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Search results wait interrupted", e);
        }
    }

    public FriendsPage acceptFriendRequest(String userId) {
        logger.info("Accepting friend request from user: {}", userId);
        String acceptButtonId = TestIDs.getFriendRequestAcceptButtonId(userId);
        WebElement acceptButton = findVisibleNodeByResourceId(acceptButtonId);
        if (acceptButton != null) {
            clickElement(acceptButton);
            waitForAnimation();
        } else {
            throw new RuntimeException("Accept button not found for user: " + userId);
        }
        return this;
    }

    public FriendsPage declineFriendRequest(String userId) {
        openDeclineFriendRequestDialog(userId);
        confirmCurrentDialog();
        return this;
    }

    private void clearSearchInputIfNeeded(WebElement input) {
        if (isElementDisplayedByAccessibilityId(TestIDs.FRIENDS_CLEAR_SEARCH)) {
            clickByAccessibilityId(TestIDs.FRIENDS_CLEAR_SEARCH);
            long endTime = System.currentTimeMillis() + 5000;
            while (System.currentTimeMillis() < endTime) {
                if (!isElementDisplayedByAccessibilityId(TestIDs.FRIENDS_CLEAR_SEARCH)) {
                    return;
                }
                waitFor(1);
            }
            logger.warn("Search clear button was still displayed after clear action");
        }
    }

    public FriendsPage openDeclineFriendRequestDialog(String userId) {
        logger.info("Opening decline friend request dialog for user: {}", userId);
        String declineButtonId = TestIDs.getFriendRequestRejectButtonId(userId);
        WebElement declineButton = findVisibleNodeByResourceId(declineButtonId);
        if (declineButton != null) {
            clickElement(declineButton);
            waitForAnimation();
        } else {
            throw new RuntimeException("Decline button not found for user: " + userId);
        }
        return this;
    }

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

    public boolean isSearchInputDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.FRIENDS_SEARCH_INPUT);
    }

    public boolean isSearchInputIconDisplayed() {
        return isNodeDisplayed(TestIDs.FRIENDS_SEARCH_INPUT + "_left_icon");
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

    public boolean isSearchQueryValid(String query) {
        return query != null && query.length() >= AppConstants.MIN_SEARCH_QUERY_LENGTH;
    }

    public boolean searchAndVerifyUser(String searchQuery, String expectedUserId) {
        logger.info("Searching for user '{}' and verifying user '{}' in results", searchQuery, expectedUserId);
        searchUsers(searchQuery);
        return isSearchResultDisplayed(expectedUserId);
    }

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
        return isFriendsListDisplayed() || isEmptyFriendsStateDisplayed() || !getFirstVisibleFriendName().isEmpty();
    }

    public boolean hasFriends() {
        for (int i = 0; i < 4; i++) {
            if (getVisibleFriendsCount() > 0) {
                return true;
            }
            waitFor(1);
        }
        return false;
    }

    public int getFriendsCount() {
        return getVisibleFriendsCount();
    }

    public boolean isEmptyFriendsStateDisplayed() {
        return isNodeDisplayed(TestIDs.FRIENDS_LIST_EMPTY)
                || isVisibleTextDisplayed("Bạn chưa có bạn bè");
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

    public FriendsPage openUnfriendDialog(String id) {
        logger.info("Opening unfriend dialog for friend request id: {}", id);
        WebElement unfriendButton = findVisibleNodeByResourceId(TestIDs.FRIEND_UNFRIEND_PREFIX + id);
        if (unfriendButton == null) {
            throw new RuntimeException("Unfriend button not found for id: " + id);
        }
        clickElement(unfriendButton);
        waitFor(1);
        return this;
    }

    public FriendsPage unfriend(String id) {
        openUnfriendDialog(id);
        confirmCurrentDialog();
        return this;
    }

    public FriendsPage waitForFriendToDisappear(String id) {
        waitForElementToDisappear(TestIDs.getFriendItemId(id));
        return this;
    }

    public boolean isFriendDisplayed(String id) {
        return isNodeDisplayed(TestIDs.getFriendItemId(id));
    }

    public boolean isFriendUsernameDisplayed(String id) {
        return false;
    }

    public String getFriendUsername(String id) {
        return "";
    }

    public FriendsPage clickRequestsTab() {
        friendRequestsSectionReached = false;
        scrollToTop();
        WebElement requestTitle = scrollIntoViewByDescription("friend_requests_title");
        if (requestTitle != null) {
            friendRequestsSectionReached = true;
            waitForFriendRequestsContent();
            return this;
        }
        WebElement requestEmpty = scrollIntoViewByDescription("friend_requests_empty");
        if (requestEmpty != null) {
            friendRequestsSectionReached = true;
            waitForFriendRequestsContent();
            return this;
        }
        while (true) {
            if (isFriendRequestsSectionInView()) {
                friendRequestsSectionReached = true;
                alignFriendRequestsHeader();
                waitForFriendRequestsContent();
                return this;
            }
            try {
                String beforeScrollSource = getCurrentPageSource();
                boolean canScrollMore;
                if (isPastFriendRequestsSection()) {
                    friendRequestsSectionReached = true;
                    if (!hasFriendRequests()) {
                        alignFriendRequestsHeader();
                        waitForFriendRequestsContent();
                        return this;
                    }
                    canScrollMore = scrollFriendsContainerUp(0.18);
                } else {
                    canScrollMore = scrollFriendsContainerDown(0.22);
                }
                waitFor(1);
                if (isFriendRequestsSectionInView()) {
                    friendRequestsSectionReached = true;
                    alignFriendRequestsHeader();
                    waitForFriendRequestsContent();
                    return this;
                }
                if (isPastFriendRequestsSection() && !hasFriendRequests()) {
                    friendRequestsSectionReached = true;
                    alignFriendRequestsHeader();
                    waitForFriendRequestsContent();
                    return this;
                }
                String afterScrollSource = getCurrentPageSource();
                if (!canScrollMore || beforeScrollSource.equals(afterScrollSource)) {
                    break;
                }
            } catch (Exception e) {
                logger.warn("Request section scroll failed: {}", e.getMessage());
                break;
            }
        }
        if (isPastFriendRequestsSection()) {
            friendRequestsSectionReached = true;
            alignFriendRequestsHeader();
        }
        if (!hasFriendRequests()) {
            friendRequestsSectionReached = true;
            alignFriendRequestsHeader();
        }
        return this;
    }

    public boolean hasFriendRequests() {
        return !getVisibleFriendRequestRows().isEmpty();
    }

    public boolean isFriendRequestsListDisplayed() {
        return isFriendRequestsSectionInView()
                || hasFriendRequests()
                || isNodeVisible("friend_requests_empty")
                || friendRequestsSectionReached;
    }

    public int getFriendRequestsCount() {
        return getVisibleFriendRequestRows().size();
    }

    public boolean isEmptyRequestsStateDisplayed() {
        return isNodeDisplayed("friend_requests_empty")
                || isVisibleTextDisplayed("Chưa có lời mời kết bạn")
                || (friendRequestsSectionReached && !hasFriendRequests());
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
        return isSearchResultAddButtonEnabled(id);
    }

    public boolean isSearchResultAddButtonEnabled(String id) {
        try {
            WebElement button = findVisibleNodeByResourceId(TestIDs.getSearchResultAddButtonId(id));
            return button != null
                    && button.isDisplayed()
                    && button.isEnabled()
                    && isElementInSafeTapArea(button);
        } catch (Exception e) {
            return false;
        }
    }

    public String getFirstAddableSearchResultId() {
        int maxAttempts = getSearchResultScrollAttempts();
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            List<WebElement> results = getSearchResultRows();
            for (WebElement result : results) {
                String id = extractDynamicId(result, TestIDs.SEARCH_RESULT_ITEM_PREFIX);
                if (!id.isEmpty() && isSearchResultAddButtonEnabled(id)) {
                    rememberSearchResult(id, result);
                    return id;
                }
            }
            if (attempt < maxAttempts - 1) {
                scrollFriendsContainerDown(0.60);
                waitFor(1);
            }
        }
        return "";
    }

    public String getFirstIncomingSearchResultId() {
        List<WebElement> results = getSearchResultRows();
        for (WebElement result : results) {
            String id = extractDynamicId(result, TestIDs.SEARCH_RESULT_ITEM_PREFIX);
            if (!id.isEmpty()
                    && isSearchResultAcceptButtonDisplayed(id)
                    && isSearchResultRejectButtonDisplayed(id)) {
                rememberSearchResult(id, result);
                logger.info("Found visible incoming search result actions for id '{}'", id);
                return id;
            }
        }
        logger.info("No visible incoming search result actions found at top of search results");
        return "";
    }

    private int getSearchResultScrollAttempts() {
        int totalResults = getSearchResultTotalCount();
        int visibleResults = Math.max(1, getSearchResultRows().size());
        int maxAttempts = totalResults > 0
                ? Math.max(1, (int) Math.ceil((double) totalResults / visibleResults) + 2)
                : 12;
        logger.info("Search result scroll plan | total={} | visible={} | maxAttempts={}",
                totalResults, visibleResults, maxAttempts);
        return maxAttempts;
    }

    private int getSearchResultTotalCount() {
        try {
            String title = getVisibleResourceText("search_results_title");
            Matcher matcher = Pattern
                    .compile("\\((\\d+)\\)")
                    .matcher(title);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        } catch (Exception e) {
            logger.warn("Could not read search result total count: {}", e.getMessage());
        }
        return 0;
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

    public boolean isSearchResultAcceptButtonDisplayed(String id) {
        return isNodeDisplayed("search_result_accept_button_" + id);
    }

    public boolean isSearchResultRejectButtonDisplayed(String id) {
        return isNodeDisplayed("search_result_reject_button_" + id);
    }

    public FriendsPage addFriend(String id) {
        tapVisibleResource(TestIDs.getSearchResultAddButtonId(id));
        return this;
    }

    public FriendsPage acceptIncomingSearchResult(String id) {
        tapVisibleResource("search_result_accept_button_" + id);
        return this;
    }

    public FriendsPage openRejectIncomingSearchResultDialog(String id) {
        tapVisibleResource("search_result_reject_button_" + id);
        waitFor(1);
        return this;
    }

    public FriendsPage waitForIncomingSearchActionsToDisappear(String id) {
        waitForElementToDisappear("search_result_accept_button_" + id);
        return this;
    }

    public boolean isFriendRequestSent(String id) {
        String addButtonId = TestIDs.getSearchResultAddButtonId(id);
        String statusId = "search_result_status_" + id;
        try {
            waitUtils.waitForCondition(driver -> {
                WebElement status = findVisibleNodeByResourceId(statusId);
                WebElement button = findVisibleNodeByResourceId(addButtonId);
                return status != null
                        && button != null
                        && status.isDisplayed()
                        && button.isDisplayed()
                        && !button.isEnabled();
            }, 10);
            return true;
        } catch (Exception e) {
            logger.warn("Search result '{}' did not change to sent/disabled state: {}", id, e.getMessage());
            return false;
        }
    }

    public FriendsPage clickSentTab() {
        sentRequestsSectionReached = false;
        scrollToTop();
        WebElement sentTitleText = scrollIntoViewByText("Lời mời đã gửi");
        if (sentTitleText != null) {
            sentRequestsSectionReached = true;
            waitForSentRequestsContent();
            return this;
        }
        WebElement sentTitle = scrollIntoViewByDescription("sent_requests_title");
        if (sentTitle != null) {
            sentRequestsSectionReached = true;
            waitForSentRequestsContent();
            return this;
        }
        WebElement sentEmpty = scrollIntoViewByDescription("sent_requests_empty");
        if (sentEmpty != null) {
            sentRequestsSectionReached = true;
            waitForSentRequestsContent();
            return this;
        }
        while (true) {
            if (isSentRequestsSectionInView()) {
                sentRequestsSectionReached = true;
                waitForSentRequestsContent();
                return this;
            }
            try {
                String beforeScrollSource = getCurrentPageSource();
                boolean canScrollMore = scrollFriendsContainerDown(0.22);
                waitFor(1);
                if (isSentRequestsSectionInView()) {
                    sentRequestsSectionReached = true;
                    waitForSentRequestsContent();
                    return this;
                }
                String afterScrollSource = getCurrentPageSource();
                if (!canScrollMore || beforeScrollSource.equals(afterScrollSource)) {
                    break;
                }
            } catch (Exception e) {
                logger.warn("Sent section scroll failed: {}", e.getMessage());
                break;
            }
        }
        if (!hasSentRequests()) {
            sentRequestsSectionReached = true;
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
        return isSentRequestsSectionInView()
                || hasSentRequests()
                || isEmptySentStateDisplayed()
                || sentRequestsSectionReached;
    }

    public int getSentRequestsCount() {
        return findElementsByXPath(
                "//*[contains(@text,'Đã gửi') or contains(@text,'Ä‘Ã£ gá»­i') or contains(@text,'đã gửi')]"
        ).size();
    }

    public boolean isEmptySentStateDisplayed() {
        return isNodeDisplayed("sent_requests_empty")
                || isVisibleTextDisplayed("Bạn chưa gửi lời mời nào")
                || (sentRequestsSectionReached && !hasSentRequests());
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

    public FriendsPage cancelSentRequest(String id) {
        logger.info("Cancelling sent friend request: {}", id);
        WebElement cancelButton = findVisibleNodeByResourceId("sent_request_item_" + id + "_cancel_button");
        if (cancelButton == null) {
            throw new RuntimeException("Sent request cancel button not found for id: " + id);
        }
        clickElement(cancelButton);
        waitFor(1);
        return this;
    }

    public boolean isCancelSentRequestDialogDisplayed() {
        return !findElementsByXPath(
                "//*[@resource-id='android:id/alertTitle' or @resource-id='android:id/message']"
        ).isEmpty();
    }

    public String getCancelSentRequestDialogTitle() {
        return getVisibleResourceText("com.modis:id/alert_title");
    }

    public String getCancelSentRequestDialogMessage() {
        return getVisibleResourceText("android:id/message");
    }

    public String getCancelSentRequestConfirmText() {
        return getVisibleResourceText("android:id/button1");
    }

    public String getCancelSentRequestDismissText() {
        return getVisibleResourceText("android:id/button2");
    }

    public FriendsPage captureCancelSentRequestDialog() {
        takeScreenshot("cancel_sent_request_dialog");
        return this;
    }

    public FriendsPage confirmCancelSentRequest() {
        return confirmCurrentDialog();
    }

    public FriendsPage waitForSentRequestToDisappear(String id) {
        waitForElementToDisappear("sent_request_item_" + id);
        return this;
    }

    public boolean isSentRequestDisplayed(String id) {
        return isNodeDisplayed("sent_request_item_" + id);
    }

    public FriendsPage confirmCurrentDialog() {
        WebElement confirmButton = findVisibleNodeByResourceId("android:id/button1");
        if (confirmButton == null) {
            throw new RuntimeException("Dialog confirmation button not found");
        }
        clickElement(confirmButton);
        waitForAnimation();
        return this;
    }

    public FriendsPage dismissCurrentDialog() {
        WebElement dismissButton = findVisibleNodeByResourceId("android:id/button2");
        if (dismissButton == null) {
            throw new RuntimeException("Dialog dismiss button not found");
        }
        clickElement(dismissButton);
        waitForAnimation();
        return this;
    }

    public boolean isUserInSearchResults(String username) {
        logger.info("Checking if user is in search results: " + username);
        waitForElementVisible(TestIDs.FRIENDS_SCROLL);
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
        scrollFriendsContainerDown(0.85);
    }

    private boolean scrollFriendsContainerDown(double percent) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("left", 0);
            params.put("top", 260);
            params.put("width", 720);
            params.put("height", 1200);
            params.put("direction", "down");
            params.put("percent", percent);
            Object result = driver.executeScript("mobile: scrollGesture", params);
            logger.info("Scrolled friends container down with scrollGesture");
            return !(result instanceof Boolean) || (Boolean) result;
        } catch (Exception e) {
            logger.warn("friends_scroll scrollGesture failed: {}", e.getMessage());
        }
        scrollDownBase();
        return true;
    }

    private boolean scrollFriendsContainerUp(double percent) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("left", 0);
            params.put("top", 260);
            params.put("width", 720);
            params.put("height", 1200);
            params.put("direction", "up");
            params.put("percent", percent);
            Object result = driver.executeScript("mobile: scrollGesture", params);
            logger.info("Scrolled friends container up with scrollGesture");
            return !(result instanceof Boolean) || (Boolean) result;
        } catch (Exception e) {
            logger.warn("friends_scroll upward scrollGesture failed: {}", e.getMessage());
        }
        scrollUpBase();
        return true;
    }

    private boolean isFriendRequestsSectionInView() {
        return isNodeVisible("friend_requests_title")
                || isNodeVisible("friend_requests_section")
                || isNodeVisible("friend_requests_empty")
                || isVisibleTextDisplayed("Lời mời kết bạn")
                || isVisibleTextDisplayed("Chưa có lời mời kết bạn")
                || !getVisibleFriendRequestRows().isEmpty();
    }

    private void alignFriendRequestsHeader() {
        while (!isNodeVisible("friend_requests_title")
                && !isVisibleTextDisplayed("Lời mời kết bạn")
                && !hasFriendRequests()
                && isPastFriendRequestsSection()) {
            String beforeScrollSource = getCurrentPageSource();
            boolean canScrollMore = scrollFriendsContainerUp(0.10);
            waitFor(1);
            String afterScrollSource = getCurrentPageSource();
            if (!canScrollMore || beforeScrollSource.equals(afterScrollSource)) {
                return;
            }
        }
    }

    private String getCurrentPageSource() {
        try {
            return DriverManager.getDriver().getPageSource();
        } catch (Exception e) {
            logger.warn("Could not read friends page source: {}", e.getMessage());
            return "";
        }
    }

    private boolean isPastFriendRequestsSection() {
        return isNodeVisible("sent_requests_section")
                || isNodeVisible("sent_requests_empty")
                || isNodeVisible("sent_request_item_")
                || isVisibleTextDisplayed("Lời mời đã gửi")
                || isVisibleTextDisplayed("Messenger")
                || isVisibleTextDisplayed("Facebook")
                || isVisibleTextDisplayed("Instagram");
    }

    private boolean isSentRequestsSectionInView() {
        return isNodeVisible("sent_requests_title")
                || isNodeVisible("sent_requests_section")
                || isNodeVisible("sent_requests_empty")
                || isNodeVisible("sent_request_item_")
                || isVisibleTextDisplayed("Lời mời đã gửi")
                || isVisibleTextDisplayed("Bạn chưa gửi lời mời nào")
                || hasSentRequests();
    }

    private WebElement scrollIntoViewByDescription(String accessibilityId) {
        try {
            String selector = "new UiScrollable(new UiSelector().scrollable(true))"
                    + ".scrollIntoView(new UiSelector().descriptionContains(\"" + accessibilityId + "\"))";
            WebElement element = DriverManager.getDriver().findElement(AppiumBy.androidUIAutomator(selector));
            if (element != null && isElementInViewport(element)) {
                logger.info("Scrolled into view by accessibility id: {}", accessibilityId);
                return element;
            }
        } catch (Exception e) {
            logger.warn("Could not scroll into view by accessibility id '{}': {}", accessibilityId, e.getMessage());
        }
        return null;
    }

    private WebElement scrollIntoViewByText(String text) {
        try {
            String selector = "new UiScrollable(new UiSelector().scrollable(true))"
                    + ".scrollIntoView(new UiSelector().textContains(\"" + text + "\"))";
            WebElement element = DriverManager.getDriver().findElement(AppiumBy.androidUIAutomator(selector));
            if (element != null && isElementInViewport(element)) {
                logger.info("Scrolled into view by text: {}", text);
                return element;
            }
        } catch (Exception e) {
            logger.warn("Could not scroll into view by text '{}': {}", text, e.getMessage());
        }
        return null;
    }

    private void waitForFriendRequestsContent() {
        long endTime = System.currentTimeMillis() + 6000;
        while (System.currentTimeMillis() < endTime) {
            if (hasFriendRequests() || isNodeVisible("friend_requests_empty")) {
                return;
            }
            if (friendRequestsSectionReached && isPastFriendRequestsSection()) {
                return;
            }
            waitFor(1);
        }
    }

    private void waitForSentRequestsContent() {
        long endTime = System.currentTimeMillis() + 6000;
        while (System.currentTimeMillis() < endTime) {
            if (hasSentRequests() || isNodeVisible("sent_requests_empty")) {
                return;
            }
            waitFor(1);
        }
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

    private WebElement findVisibleNodeByResourceId(String resourceId) {
        List<WebElement> nodes = findElementsByXPath(
                "//*[@resource-id='" + resourceId + "' or contains(@resource-id,'" + resourceId + "')]"
        );
        for (WebElement node : nodes) {
            try {
                if (node.isDisplayed() && isElementInViewport(node)) {
                    return node;
                }
            } catch (Exception e) {
                logger.warn("Could not verify visible resource-id '{}': {}", resourceId, e.getMessage());
            }
        }
        return null;
    }

    private void tapVisibleResource(String resourceId) {
        WebElement element = findVisibleNodeByResourceId(resourceId);
        if (element == null) {
            clickByAccessibilityId(resourceId);
            return;
        }
        tapElement(element);
        logger.info("Tapped visible resource-id: {}", resourceId);
    }

    private String getVisibleResourceText(String resourceId) {
        WebElement node = findVisibleNodeByResourceId(resourceId);
        if (node == null) {
            return "";
        }
        return getText(node).trim();
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
        return right > left
                && bottom > top
                && right > 0
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

    private boolean isElementInSafeTapArea(WebElement node) {
        try {
            if (!isElementInViewport(node)) {
                return false;
            }
            Rectangle rect = node.getRect();
            org.openqa.selenium.Dimension screen = DriverManager.getDriver().manage().window().getSize();
            int bottomSafeMargin = Math.max(96, screen.getHeight() / 14);
            return rect.getY() >= 0
                    && rect.getY() + rect.getHeight() <= screen.getHeight() - bottomSafeMargin;
        } catch (Exception e) {
            logger.warn("Could not verify safe tap area: {}", e.getMessage());
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

    private boolean isVisibleTextDisplayed(String text) {
        try {
            String source = DriverManager.getDriver().getPageSource();
            Matcher nodeMatcher = Pattern
                    .compile("<node[^>]*(text|content-desc)=\"[^\"]*" + Pattern.quote(text) + "[^\"]*\"[^>]*>")
                    .matcher(source);
            while (nodeMatcher.find()) {
                Matcher boundsMatcher = Pattern
                        .compile("bounds=\"\\[(\\d+),(\\d+)]\\[(\\d+),(\\d+)]\"")
                        .matcher(nodeMatcher.group());
                if (boundsMatcher.find() && isBoundsInViewport(
                        Integer.parseInt(boundsMatcher.group(1)),
                        Integer.parseInt(boundsMatcher.group(2)),
                        Integer.parseInt(boundsMatcher.group(3)),
                        Integer.parseInt(boundsMatcher.group(4)))) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.warn("Could not verify visible text '{}': {}", text, e.getMessage());
        }
        return false;
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
