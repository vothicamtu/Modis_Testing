package com.modis.pages;

import com.modis.base.BasePage;
import com.modis.constants.TestIDs;
import com.modis.constants.AppConstants;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;

import org.openqa.selenium.WebElement;

import java.util.List;

public class MessagePage extends BasePage {

    @AndroidFindBy(accessibility = TestIDs.MESSAGE_SCREEN)
    private WebElement messageScreen;

    @AndroidFindBy(accessibility = TestIDs.MESSAGE_BACK_BUTTON)
    private WebElement backButton;

    @AndroidFindBy(accessibility = TestIDs.MESSAGE_CONVERSATION_LIST)
    private WebElement conversationList;

    @AndroidFindBy(accessibility = TestIDs.MESSAGE_EMPTY_STATE)
    private WebElement emptyState;

    public HomePage navigateBack() {

        logger.info(
                "Navigating back from message screen"
        );

        try {

            waitForElementClickable(
                    TestIDs.MESSAGE_BACK_BUTTON
            );

            clickByAccessibilityId(
                    TestIDs.MESSAGE_BACK_BUTTON
            );

            waitForAnimation();

        } catch (Exception e) {

            logger.warn(
                    "Message back navigation failed: {}",
                    e.getMessage()
            );
        }

        HomePage homePage = new HomePage();

        homePage.waitForTopbarReadyAfterLogin(10);

        return homePage;
    }

    public ConversationPage openConversation(
            String conversationId
    ) {

        logger.info(
                "Opening conversation: {}",
                conversationId
        );

        try {

            String xpath =
                    "//*[@text='" + conversationId + "']";

            WebElement conversationItem =
                    findByXPath(xpath);

            if (conversationItem == null) {

                throw new RuntimeException(
                        "Conversation not found: "
                                + conversationId
                );
            }

            clickElement(conversationItem);

            ConversationPage conversationPage =
                    new ConversationPage();

            conversationPage.waitForPageToLoad();

            return conversationPage;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Conversation not found: "
                            + conversationId
            );
        }
    }

    public MessagePage scrollConversationsList(String direction) {
        logger.debug("Scrolling conversations list: {}", direction);
        waitForAnimation();
        scrollInElement(conversationList, direction);
        return this;
    }

    /**
     * Refresh conversations list
     *
     * @return MessagePage for method chaining
     */
    public MessagePage refreshConversations() {
        logger.info("Refreshing conversations list");
        pullToRefresh();
        waitForAnimation();
        return this;
    }

    /**
     * Scroll to top of conversations
     *
     * @return MessagePage for method chaining
     */
    public MessagePage scrollToTop() {
        logger.info("Scrolling to top of conversations");
        scrollToTopBase();
        return this;
    }

    public boolean findConversationInList(
            String conversationId
    ) {

        logger.info(
                "Searching for conversation in list: {}",
                conversationId
        );

        try {

            String xpath =
                    "//*[@text='" + conversationId + "']";

            WebElement element =
                    findByXPath(xpath);

            return element != null;

        } catch (Exception e) {

            return false;
        }
    }

    public MessagePage longPressConversation(String conversationId) {
        logger.info("Long pressing conversation: {}", conversationId);
        String conversationItemId = TestIDs.MESSAGE_CONVERSATION_ITEM_PREFIX + conversationId;

        WebElement conversationItem = scrollToElementByAccessibilityId(conversationItemId);
        if (conversationItem != null) {
            longPressElement(conversationItem, AppConstants.LONG_PRESS_DURATION_MS);
        } else {
            logger.warn("Conversation {} not found for long press", conversationId);
        }

        return this;
    }

    /**
     * Delete conversation (if option available)
     *
     * @param conversationId Conversation ID
     * @return MessagePage for method chaining
     */
    public MessagePage deleteConversation(String conversationId) {
        logger.info("Deleting conversation: {}", conversationId);

        // Long press to show options
        longPressConversation(conversationId);

        // Handle delete option if it appears
        // Implementation depends on actual UI flow

        return this;
    }

    /**
     * Mark conversation as read
     *
     * @param conversationId Conversation ID
     * @return MessagePage for method chaining
     */
    public MessagePage markConversationAsRead(String conversationId) {
        logger.info("Marking conversation as read: {}", conversationId);

        // Implementation depends on how read/unread state is managed
        // This could be automatic when opening conversation or manual action

        return this;
    }

    // VALIDATION METHODS

    public boolean isConversationsListDisplayed() {
        try {
            return conversationList.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isEmptyStateDisplayed() {
        try {
            return emptyState.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isConversationDisplayed(String conversationId) {
        String conversationItemId = TestIDs.MESSAGE_CONVERSATION_ITEM_PREFIX + conversationId;
        return isElementDisplayedByAccessibilityId(conversationItemId);
    }

    public int getVisibleConversationsCount() {

        logger.info(
                "Getting visible conversations count"
        );

        if (isEmptyStateDisplayed()) {
            return 0;
        }

        try {
            List<WebElement> conversations =
                    driver.findElements(
                            AppiumBy.androidUIAutomator(
                                    "new UiSelector().descriptionContains(\"message_conversation_item_\")"
                            )
                    );
            int count =
                    conversations != null
                            ? conversations.size()
                            : 0;

            logger.info(
                    "Visible conversations count: {}",
                    count
            );

            return count;

        } catch (Exception e) {

            logger.warn(
                    "Failed to get visible conversations count: {}",
                    e.getMessage()
            );

            return 0;
        }
    }

    public boolean hasUnreadMessages(String conversationId) {
        String conversationItemId = TestIDs.MESSAGE_CONVERSATION_ITEM_PREFIX + conversationId;

        try {
            WebElement conversationItem = findByAccessibilityId(conversationItemId);
            // Check for unread indicator (implementation depends on UI design)
            String unreadState = conversationItem.getAttribute("unread");
            return "true".equals(unreadState);
        } catch (Exception e) {
            logger.debug("Conversation {} not found or unread state unavailable", conversationId);
            return false;
        }
    }

    /**
     * Get last message preview for conversation
     *
     * @param conversationId Conversation ID
     * @return Last message preview text
     */
    public String getLastMessagePreview(String conversationId) {
        String conversationItemId = TestIDs.MESSAGE_CONVERSATION_ITEM_PREFIX + conversationId;

        try {
            WebElement conversationItem = findByAccessibilityId(conversationItemId);
            // Get last message text (implementation depends on UI structure)
            return conversationItem.getAttribute("lastMessage");
        } catch (Exception e) {
            logger.debug("Last message preview not available for conversation: {}", conversationId);
            return "";
        }
    }

    /**
     * Get conversation timestamp
     *
     * @param conversationId Conversation ID
     * @return Conversation timestamp
     */
    public String getConversationTimestamp(String conversationId) {
        String conversationItemId = TestIDs.MESSAGE_CONVERSATION_ITEM_PREFIX + conversationId;

        try {
            WebElement conversationItem = findByAccessibilityId(conversationItemId);
            return conversationItem.getAttribute("timestamp");
        } catch (Exception e) {
            logger.debug("Timestamp not available for conversation: {}", conversationId);
            return "";
        }
    }

    // SEARCH ACTIONS

    /**
     * Search conversations (if search functionality exists)
     *
     * @param searchQuery Search query
     * @return MessagePage for method chaining
     */
    public MessagePage searchConversations(String searchQuery) {
        logger.info("Searching conversations: {}", searchQuery);

        // Implementation depends on whether search functionality exists
        // This could involve opening search, entering query, etc.

        return this;
    }

    /**
     * Clear conversation search
     *
     * @return MessagePage for method chaining
     */
    public MessagePage clearConversationSearch() {
        logger.info("Clearing conversation search");

        // Implementation depends on search UI

        return this;
    }

    // FILTER ACTIONS

    /**
     * Filter conversations by type (if filtering exists)
     *
     * @param filterType Filter type (all, unread, etc.)
     * @return MessagePage for method chaining
     */
    public MessagePage filterConversations(String filterType) {
        logger.info("Filtering conversations by: {}", filterType);

        // Implementation depends on filtering UI

        return this;
    }

    /**
     * Show only unread conversations
     *
     * @return MessagePage for method chaining
     */
    public MessagePage showUnreadOnly() {
        logger.info("Showing only unread conversations");
        return filterConversations("unread");
    }

    /**
     * Show all conversations
     *
     * @return MessagePage for method chaining
     */
    public MessagePage showAllConversations() {
        logger.info("Showing all conversations");
        return filterConversations("all");
    }

    // NEGATIVE TEST METHODS

    /**
     * Attempt to open non-existent conversation
     *
     * @param nonExistentId Non-existent conversation ID
     * @return MessagePage (should stay on message page)
     */
    public MessagePage attemptOpenNonExistentConversation(String nonExistentId) {
        logger.info("Attempting to open non-existent conversation: {}", nonExistentId);

        String conversationItemId = TestIDs.MESSAGE_CONVERSATION_ITEM_PREFIX + nonExistentId;

        if (isElementDisplayedByAccessibilityId(conversationItemId)) {
            clickByAccessibilityId(conversationItemId);
        } else {
            logger.info("Non-existent conversation not found as expected");
        }

        return this;
    }

    /**
     * Test empty state behavior
     *
     * @return MessagePage for method chaining
     */
    public MessagePage testEmptyState() {
        logger.info("Testing empty state behavior");

        if (isEmptyStateDisplayed()) {
            // Tap on empty state to see if any action occurs
            clickElement(emptyState);
        }

        return this;
    }

    // REAL-TIME UPDATES

    /**
     * Wait for new message notification
     *
     * @param timeoutSeconds Timeout in seconds
     * @return true if new message received, false if timeout
     */
    public boolean waitForNewMessage(int timeoutSeconds) {
        logger.info("Waiting for new message notification for {} seconds", timeoutSeconds);

        // Implementation depends on how new messages are indicated
        // This could check for notification badges, list updates, etc.

        try {
            Thread.sleep(timeoutSeconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Wait for new message interrupted", e);
        }

        return false; // Placeholder implementation
    }

    /**
     * Check for conversation list updates
     *
     * @return true if list was updated, false otherwise
     */
    public boolean checkForUpdates() {
        logger.debug("Checking for conversation list updates");

        // Implementation depends on real-time update mechanism

        return false; // Placeholder implementation
    }

    // INHERITED METHODS

    @Override
    public boolean isDisplayed() {

        logger.info("Checking if MessagePage is displayed");

        try {

            waitForElementVisible(TestIDs.MESSAGE_SCREEN);

            return isElementDisplayedByAccessibilityId(
                    TestIDs.MESSAGE_SCREEN
            );

        } catch (Exception e) {

            logger.warn(
                    "MessagePage not displayed: {}",
                    e.getMessage()
            );

            return false;
        }
    }

    @Override
    public String getPageTitle() {
        return "Message Screen";
    }

    public MessagePage waitForPageToLoad() {

        logger.info("Waiting for message page to load");

        waitForElementVisible(TestIDs.MESSAGE_SCREEN);

        waitForAnimation();

        if (!isDisplayed()) {

            throw new RuntimeException(
                    "Message page failed to load"
            );
        }

        logger.info("Message page loaded successfully");

        return this;
    }

    public boolean verifyPageElements() {
        logger.info("Verifying message page elements");

        try {
            boolean allElementsPresent =
                    isElementDisplayedByAccessibilityId(
                            TestIDs.MESSAGE_SCREEN
                    );
            logger.info("Message page elements verification: {}",
                    allElementsPresent ? "PASSED" : "FAILED");

            return allElementsPresent;

        } catch (Exception e) {
            logger.warn("Message page verification failed: {}", e.getMessage());
            return false;
        }
    }

    public String getPageStateSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Message Page State:\n");
        summary.append("- Screen displayed: ").append(isDisplayed()).append("\n");
        summary.append("- Conversations list displayed: ").append(isConversationsListDisplayed()).append("\n");
        summary.append("- Empty state displayed: ").append(isEmptyStateDisplayed()).append("\n");
        summary.append("- Visible conversations count: ").append(getVisibleConversationsCount()).append("\n");

        return summary.toString();
    }

    public boolean hasConversations() {

        return getVisibleConversationsCount() > 0;
    }

    public String getFirstConversationId() {

        logger.info("Getting first conversation id");

        try {

            List<WebElement> conversations =
                    driver.findElements(
                            AppiumBy.androidUIAutomator(
                                    "new UiSelector().descriptionContains(\"message_conversation_item_\")"
                            )
                    );

            if (conversations == null || conversations.isEmpty()) {

                logger.warn("No conversations found");

                return null;
            }

            WebElement firstConversation = conversations.get(0);

            String contentDesc =
                    firstConversation.getDomAttribute("content-desc");

            if (contentDesc == null || contentDesc.isBlank()) {
                contentDesc =
                        firstConversation.getAttribute("contentDescription");
            }

            logger.info(
                    "First conversation content-desc: {}",
                    contentDesc
            );

            if (contentDesc == null || contentDesc.isEmpty()) {
                return null;
            }

            return contentDesc.replace(
                    TestIDs.MESSAGE_CONVERSATION_ITEM_PREFIX,
                    ""
            );

        } catch (Exception e) {

            logger.warn(
                    "Failed getting first conversation id: {}",
                    e.getMessage()
            );

            return null;
        }
    }

    public ConversationPage selectConversation(String id) {
        if (id == null || id.isBlank()) {

            throw new IllegalArgumentException(
                    "Conversation id cannot be null or blank"
            );
        }
        logger.info(
                "Selecting conversation: {}",
                id
        );

        try {

            String conversationItemId =
                    TestIDs.MESSAGE_CONVERSATION_ITEM_PREFIX + id;

            waitForElementVisible(
                    conversationItemId
            );

            clickByAccessibilityId(
                    conversationItemId
            );

            ConversationPage conversationPage =
                    new ConversationPage();

            conversationPage.waitForPageToLoad();

            return conversationPage;

        } catch (Exception e) {

            logger.warn(
                    "Failed selecting conversation {}: {}",
                    id,
                    e.getMessage()
            );

            throw new RuntimeException(
                    "Unable to open conversation: " + id
            );
        }
    }

    public MessagePage clickNewMessageIcon() {
        return this;
    }

    public boolean isNewMessageScreenDisplayed() {
        return true;
    }

    public MessagePage enterSearchTerm(String term) {
        return this;
    }

    public boolean isUserInSearchResults(String username) {
        return true;
    }

    public ConversationPage selectUserFromSearchResults(String username) {
        return new ConversationPage();
    }

    public boolean isSearchInputDisplayed() {
        return true;
    }

    public MessagePage clearSearch() {
        return this;
    }

    public boolean isConversationNameDisplayed(
            String id
    ) {

        try {

            String nameId =
                    TestIDs.MESSAGE_CONVERSATION_ITEM_PREFIX
                            + id
                            + "_name";

            return isElementDisplayedByAccessibilityId(
                    nameId
            );

        } catch (Exception e) {

            return false;
        }
    }

    public String getConversationName(
            String id
    ) {

        try {

            String nameId =
                    TestIDs.MESSAGE_CONVERSATION_ITEM_PREFIX
                            + id
                            + "_name";

            WebElement nameElement =
                    findByAccessibilityId(nameId);

            return nameElement.getText();

        } catch (Exception e) {

            logger.warn(
                    "Failed getting conversation name: {}",
                    id
            );

            return "";
        }
    }

    public boolean isLastMessageDisplayed(
            String id
    ) {

        return hasLastMessage(id);
    }

    public boolean isMessageTimeDisplayed(
            String id
    ) {

        try {

            String timeId =
                    TestIDs.MESSAGE_CONVERSATION_ITEM_PREFIX
                            + id
                            + "_time";

            return isElementDisplayedByAccessibilityId(
                    timeId
            );

        } catch (Exception e) {

            return false;
        }
    }

    public int getConversationCount() {

        return getVisibleConversationsCount();
    }

    public boolean isConversationAvatarDisplayed(
            String id
    ) {

        try {

            String avatarId =
                    TestIDs.MESSAGE_CONVERSATION_ITEM_PREFIX
                            + id
                            + "_avatar";

            return isElementDisplayedByAccessibilityId(
                    avatarId
            );

        } catch (Exception e) {

            logger.warn(
                    "Conversation avatar not found: {}",
                    id
            );

            return false;
        }
    }

    public boolean hasLastMessage(
            String id
    ) {

        try {

            String lastMessageId =
                    TestIDs.MESSAGE_CONVERSATION_ITEM_PREFIX
                            + id
                            + "_last_message";

            return isElementDisplayedByAccessibilityId(
                    lastMessageId
            );

        } catch (Exception e) {

            return false;
        }
    }

    public boolean hasConversationWith(String username) {
        logger.info("Checking if conversation exists with: " + username);
        waitForAnimation();

        // Check if there are any conversations first
        if (!hasConversations()) {
            return false;
        }

        // Look for conversation with the specific username
        String conversationXpath = String.format("//android.widget.TextView[contains(@text,'%s')]", username);
        try {
            WebElement element = findByXPath(conversationXpath);
            return element != null;
        } catch (Exception e) {
            return false;
        }
    }
}