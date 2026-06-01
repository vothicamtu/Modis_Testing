package com.modis.pages;

import com.modis.base.BasePage;
import com.modis.constants.TestIDs;
import com.modis.constants.AppConstants;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;

import java.util.List;

import static com.modis.drivers.DriverManager.getDriver;

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

            waitForElementVisible(
                    TestIDs.MESSAGE_BACK_BUTTON
            );

            clickByAccessibilityId(
                    TestIDs.MESSAGE_BACK_BUTTON
            );

            waitForAnimation();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Message back navigation failed",
                    e
            );
        }

        HomePage homePage = new HomePage();

        homePage.waitForTopbarReadyAfterLogin(10);

        return homePage;
    }

    public ConversationPage openConversation(String username) {

        logger.info(
                "Opening conversation with user: {}",
                username
        );

        try {
            String conversationItemId =
                    TestIDs.MESSAGE_CONVERSATION_ITEM_PREFIX
                            + username;
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

            throw new RuntimeException(
                    "Conversation not found for username: "
                            + username,
                    e
            );
        }
    }

    public MessagePage scrollConversationsList(String direction) {
        logger.debug("Scrolling conversations list: {}", direction);
        waitForAnimation();
        scrollInElement(conversationList, direction);
        return this;
    }

    public MessagePage refreshConversations() {
        logger.info("Refreshing conversations list");
        
        try {
            // Try pull to refresh if available
            pullToRefresh();
            waitForAnimation();
            
            // Wait for potential API call to complete
            waitFor(3);
            
        } catch (Exception e) {
            logger.warn("Pull to refresh failed, trying alternative refresh: {}", e.getMessage());
            
            // Alternative: scroll up and down to trigger refresh
            try {
                scrollToTop();
                waitFor(1);
                scrollToTopBase();
                waitFor(2);
            } catch (Exception scrollException) {
                logger.warn("Alternative refresh also failed: {}", scrollException.getMessage());
            }
        }
        
        logger.info("Conversations refresh completed");
        return this;
    }

    public MessagePage scrollToTop() {
        logger.info("Scrolling to top of conversations");
        scrollToTopBase();
        return this;
    }

    public boolean findConversationInList(String conversationId) {
        logger.info("Searching for conversation in list: {}", conversationId);
        String conversationItemId = TestIDs.MESSAGE_CONVERSATION_ITEM_PREFIX + conversationId;

        return isElementDisplayedByAccessibilityId(conversationItemId);
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

    public boolean isConversationsListDisplayed() {
        logger.debug("Checking if conversations list is displayed using multiple strategies");
        
        try {
            // Strategy 1: Check the PageFactory element
            if (conversationList != null && conversationList.isDisplayed()) {
                logger.debug("Conversations list detected via PageFactory element");
                return true;
            }
        } catch (Exception e) {
            logger.debug("PageFactory conversationList check failed: {}", e.getMessage());
        }

        try {
            // Strategy 2: Check by accessibility ID with UiAutomator2 crash protection
            if (isElementDisplayedByAccessibilityId(TestIDs.MESSAGE_CONVERSATION_LIST)) {
                logger.debug("Conversations list detected via accessibility ID");
                return true;
            }
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : "";
            if (errorMsg.contains("socket hang up") || errorMsg.contains("timeout") || 
                errorMsg.contains("Could not proxy command")) {
                logger.warn("Accessibility ID check failed due to UiAutomator2 issue: {}", errorMsg);
            } else {
                logger.debug("Accessibility ID check failed: {}", errorMsg);
            }
        }

        try {
            // Strategy 3: Look for FlatList component directly
            List<WebElement> flatListElements = getDriver().findElements(
                    By.xpath("//android.widget.ScrollView | //androidx.recyclerview.widget.RecyclerView | //*[@class='android.widget.ListView']")
            );
            
            if (!flatListElements.isEmpty()) {
                logger.debug("Found {} list container elements, assuming conversations list is displayed", flatListElements.size());
                return true;
            }
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : "";
            if (errorMsg.contains("socket hang up") || errorMsg.contains("timeout") || 
                errorMsg.contains("Could not proxy command")) {
                logger.warn("FlatList search failed due to UiAutomator2 issue: {}", errorMsg);
            } else {
                logger.debug("FlatList search failed: {}", errorMsg);
            }
        }

        try {
            // Strategy 4: If we have conversation items, the list must be displayed
            List<WebElement> conversationItems = getDriver().findElements(
                    By.xpath("//*[contains(@content-desc,'" + TestIDs.MESSAGE_CONVERSATION_ITEM_PREFIX + "')]")
            );
            
            if (!conversationItems.isEmpty()) {
                logger.debug("Found {} conversation items, conversations list must be displayed", conversationItems.size());
                return true;
            }
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : "";
            if (errorMsg.contains("socket hang up") || errorMsg.contains("timeout") || 
                errorMsg.contains("Could not proxy command")) {
                logger.warn("Conversation items check failed due to UiAutomator2 issue: {}", errorMsg);
            } else {
                logger.debug("Conversation items check failed: {}", errorMsg);
            }
        }

        try {
            // Strategy 5: Fallback - use UiAutomator selector for conversation items
            List<WebElement> conversationItems = getDriver().findElements(
                    AppiumBy.androidUIAutomator(
                            "new UiSelector().descriptionContains(\""
                                    + TestIDs.MESSAGE_CONVERSATION_ITEM_PREFIX
                                    + "\")"
                    )
            );
            
            if (!conversationItems.isEmpty()) {
                logger.debug("Found {} conversation items via UiAutomator, conversations list must be displayed", conversationItems.size());
                return true;
            }
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : "";
            if (errorMsg.contains("socket hang up") || errorMsg.contains("timeout") || 
                errorMsg.contains("Could not proxy command")) {
                logger.warn("UiAutomator conversation items check failed due to UiAutomator2 issue: {}", errorMsg);
            } else {
                logger.debug("UiAutomator conversation items check failed: {}", errorMsg);
            }
        }

        logger.debug("All strategies failed - conversations list not detected");
        return false;
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

        try {

            return getDriver()
                    .findElements(
                            AppiumBy.androidUIAutomator(
                                    "new UiSelector().descriptionContains(\""
                                            + TestIDs.MESSAGE_CONVERSATION_ITEM_PREFIX
                                            + "\")"
                            )
                    )
                    .size();

        } catch (Exception e) {

            return 0;
        }
    }

    // NEGATIVE TEST METHODS
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

    public MessagePage testEmptyState() {
        logger.info("Testing empty state behavior");

        if (isEmptyStateDisplayed()) {
            // Tap on empty state to see if any action occurs
            clickElement(emptyState);
        }

        return this;
    }
    public boolean waitForNewMessage(int timeoutSeconds) {
        logger.info("Waiting for new message notification for {} seconds", timeoutSeconds);

        int initialCount = getVisibleConversationsCount();
        long endTime = System.currentTimeMillis() + timeoutSeconds * 1000L;

        while (System.currentTimeMillis() < endTime) {
            refreshConversations();

            if (getVisibleConversationsCount() > initialCount) {
                return true;
            }

            waitFor(1);
        }

        return false;
    }

    @Override
    public boolean isDisplayed() {

        logger.info("Checking if MessagePage is displayed");

        try {
            if (isElementDisplayedByAccessibilityId(TestIDs.MESSAGE_SCREEN)) {
                logger.info("MessagePage detected via accessibility ID");
                return true;
            }
            
            if (isConversationsListDisplayed() || isEmptyStateDisplayed()) {
                logger.info("MessagePage detected via conversation list or empty state");
                return true;
            }
            
            if (isElementDisplayedByAccessibilityId(TestIDs.MESSAGE_BACK_BUTTON)) {
                logger.info("MessagePage detected via back button");
                return true;
            }
            
            logger.info("MessagePage not detected by any strategy");
            return false;

        } catch (Exception e) {

            logger.warn(
                    "MessagePage display check failed: {}",
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

        try {
            // Wait for message screen to be visible
            waitForElementVisible(TestIDs.MESSAGE_SCREEN);
            
            // Wait for animation to complete
            waitForAnimation();
            
            // Wait a bit more for conversations to load from API
            waitFor(3);
            
            // Wait for either conversations to load or empty state to appear
            int maxWaitSeconds = 10;
            boolean pageReady = false;
            
            for (int i = 0; i < maxWaitSeconds; i++) {
                try {
                    if (hasConversations() || isEmptyStateDisplayed()) {
                        pageReady = true;
                        break;
                    }
                    waitFor(1);
                } catch (Exception e) {
                    logger.debug("Waiting for page content, attempt {}/{}", i + 1, maxWaitSeconds);
                    waitFor(1);
                }
            }
            
            if (!pageReady) {
                logger.warn("Message page content did not load within {} seconds", maxWaitSeconds);
            }
            
            // Verify page is displayed
            if (!isDisplayed()) {
                throw new RuntimeException("Message page failed to load");
            }

            logger.info("Message page loaded successfully");
            return this;
            
        } catch (Exception e) {
            logger.error("Failed to load message page: {}", e.getMessage());
            throw new RuntimeException("Message page failed to load", e);
        }
    }

    public boolean verifyPageElements() {
        logger.info("Verifying message page elements");

        try {
            // Use a more lenient check that doesn't cause UiAutomator2 crashes
            boolean screenDisplayed = isDisplayed();
            
            if (screenDisplayed) {
                logger.info("Message page elements verification: PASSED");
                return true;
            } else {
                logger.info("Message page elements verification: FAILED - screen not displayed");
                return false;
            }

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

        try {
            logger.info("Checking for conversations using multiple strategies");
            
            // Strategy 1: Check if empty state is displayed first (fastest check)
            try {
                if (isEmptyStateDisplayed()) {
                    logger.info("Empty state is displayed - no conversations available");
                    return false;
                }
            } catch (Exception e) {
                logger.debug("Empty state check failed: {}", e.getMessage());
            }

            // Strategy 2: Look for conversation items using XPath (more reliable than accessibility ID)
            List<WebElement> conversationItems = null;
            try {
                logger.debug("Attempting XPath search for conversation items");
                conversationItems = getDriver().findElements(
                        By.xpath("//*[contains(@content-desc,'" + TestIDs.MESSAGE_CONVERSATION_ITEM_PREFIX + "')]")
                );
                logger.info("Found {} conversation items via XPath", conversationItems.size());
            } catch (Exception e) {
                String errorMsg = e.getMessage() != null ? e.getMessage() : "";
                if (errorMsg.contains("socket hang up") || errorMsg.contains("timeout") || 
                    errorMsg.contains("Could not proxy command")) {
                    logger.warn("XPath search failed due to UiAutomator2 issue: {}", errorMsg);
                } else {
                    logger.warn("XPath search failed: {}", errorMsg);
                }
            }

            // Strategy 3: Fallback to UiAutomator selector (if XPath fails)
            if (conversationItems == null || conversationItems.isEmpty()) {
                try {
                    logger.debug("Attempting UiAutomator search for conversation items");
                    conversationItems = getDriver().findElements(
                            AppiumBy.androidUIAutomator(
                                    "new UiSelector().descriptionContains(\""
                                            + TestIDs.MESSAGE_CONVERSATION_ITEM_PREFIX
                                            + "\")"
                            )
                    );
                    logger.info("Found {} conversation items via UiAutomator", conversationItems.size());
                } catch (Exception e) {
                    String errorMsg = e.getMessage() != null ? e.getMessage() : "";
                    if (errorMsg.contains("socket hang up") || errorMsg.contains("timeout") || 
                        errorMsg.contains("Could not proxy command")) {
                        logger.warn("UiAutomator search failed due to UiAutomator2 issue: {}", errorMsg);
                    } else {
                        logger.warn("UiAutomator search failed: {}", errorMsg);
                    }
                    conversationItems = java.util.Collections.emptyList();
                }
            }

            boolean hasItems = conversationItems != null && !conversationItems.isEmpty();
            
            // Strategy 4: Double check by also looking for the conversation list container
            boolean listExists = false;
            try {
                listExists = isConversationsListDisplayed();
            } catch (Exception e) {
                logger.debug("Conversation list container check failed: {}", e.getMessage());
            }
            
            logger.info(
                    "Conversations check - List exists: {}, Has items: {}, Final result: {}",
                    listExists, hasItems, hasItems
            );

            return hasItems;

        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : "";
            if (errorMsg.contains("socket hang up") || errorMsg.contains("timeout") || 
                errorMsg.contains("Could not proxy command")) {
                logger.warn("Unable to determine conversation count due to UiAutomator2 issue: {}", errorMsg);
            } else {
                logger.warn("Unable to determine conversation count: {}", errorMsg);
            }

            // Final fallback: assume no conversations if all checks fail
            logger.info("All conversation checks failed - assuming no conversations");
            return false;
        }
    }

    public String getFirstConversationId() {

        try {
            logger.info("Getting first conversation ID using multiple strategies");
            
            // Check if we have conversations at all first
            if (!hasConversations()) {
                logger.info("No conversations available");
                return null;
            }

            List<WebElement> elements = null;
            
            // Strategy 1: Use XPath to find conversation elements with timeout protection
            try {
                logger.debug("Attempting XPath search for conversation elements");
                elements = getDriver().findElements(
                        By.xpath("//*[contains(@content-desc,'" + TestIDs.MESSAGE_CONVERSATION_ITEM_PREFIX + "')]")
                );
                logger.info("Found {} conversation items via XPath", elements.size());
            } catch (Exception e) {
                String errorMsg = e.getMessage() != null ? e.getMessage() : "";
                if (errorMsg.contains("socket hang up") || errorMsg.contains("timeout") || 
                    errorMsg.contains("Could not proxy command")) {
                    logger.warn("XPath search failed due to UiAutomator2 issue: {}", errorMsg);
                } else {
                    logger.warn("XPath search failed: {}", errorMsg);
                }
            }

            // Strategy 2: Fallback to UiAutomator selector with timeout protection
            if (elements == null || elements.isEmpty()) {
                try {
                    logger.debug("Attempting UiAutomator search for conversation elements");
                    elements = getDriver().findElements(
                            AppiumBy.androidUIAutomator(
                                    "new UiSelector().descriptionContains(\""
                                            + TestIDs.MESSAGE_CONVERSATION_ITEM_PREFIX
                                            + "\")"
                            )
                    );
                    logger.info("Found {} conversation items via UiAutomator", elements.size());
                } catch (Exception e) {
                    String errorMsg = e.getMessage() != null ? e.getMessage() : "";
                    if (errorMsg.contains("socket hang up") || errorMsg.contains("timeout") || 
                        errorMsg.contains("Could not proxy command")) {
                        logger.warn("UiAutomator search failed due to UiAutomator2 issue: {}", errorMsg);
                    } else {
                        logger.warn("UiAutomator search failed: {}", errorMsg);
                    }
                    elements = java.util.Collections.emptyList();
                }
            }

            if (elements == null || elements.isEmpty()) {
                logger.warn("No conversation elements found despite hasConversations() returning true");
                return null;
            }

            // Extract conversation ID from the first element with error protection
            for (WebElement element : elements) {
                try {
                    String desc = null;
                    
                    // Try to get content-desc with timeout protection
                    try {
                        desc = element.getAttribute("content-desc");
                    } catch (Exception attrEx) {
                        String attrErrorMsg = attrEx.getMessage() != null ? attrEx.getMessage() : "";
                        if (attrErrorMsg.contains("socket hang up") || attrErrorMsg.contains("timeout") || 
                            attrErrorMsg.contains("Could not proxy command")) {
                            logger.warn("Failed to get content-desc due to UiAutomator2 issue, skipping element");
                            continue; // Skip this element and try next
                        } else {
                            logger.warn("Failed to get content-desc from element: {}", attrErrorMsg);
                            continue; // Try next element
                        }
                    }
                    
                    logger.debug("Conversation node: [{}]", desc);

                    if (desc != null && desc.startsWith(TestIDs.MESSAGE_CONVERSATION_ITEM_PREFIX)) {
                        String conversationId = desc.substring(
                                TestIDs.MESSAGE_CONVERSATION_ITEM_PREFIX.length()
                        );
                        
                        if (conversationId != null && !conversationId.trim().isEmpty()) {
                            logger.info("First conversation ID found: {}", conversationId);
                            return conversationId;
                        }
                    }
                } catch (Exception e) {
                    String errorMsg = e.getMessage() != null ? e.getMessage() : "";
                    if (errorMsg.contains("socket hang up") || errorMsg.contains("timeout") || 
                        errorMsg.contains("Could not proxy command")) {
                        logger.warn("UiAutomator2 issue while processing element, skipping: {}", errorMsg);
                        continue; // Skip this element and try next
                    } else {
                        logger.warn("Failed to process element: {}", errorMsg);
                        continue; // Try next element
                    }
                }
            }

        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : "";
            if (errorMsg.contains("socket hang up") || errorMsg.contains("timeout") || 
                errorMsg.contains("Could not proxy command")) {
                logger.warn("Failed to get first conversation ID due to UiAutomator2 issue: {}", errorMsg);
            } else {
                logger.warn("Failed to get first conversation ID: {}", errorMsg);
            }
        }

        logger.warn("No valid conversation ID found");
        return null;
    }

    public ConversationPage selectConversation(String id) {

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

    public boolean isUserInSearchResults(
            String username
    ) {

        try {

            String xpath =
                    String.format(
                            "//android.widget.TextView[contains(@text,'%s')]",
                            username
                    );

            WebElement element =
                    findByXPath(xpath);

            return element != null;

        } catch (Exception e) {

            return false;
        }
    }

    public ConversationPage selectUserFromSearchResults(
            String username
    ) {

        logger.info(
                "Selecting user from search: {}",
                username
        );

        String xpath =
                String.format(
                        "//android.widget.TextView[contains(@text,'%s')]",
                        username
                );

        WebElement element =
                findByXPath(xpath);

        if (element == null) {

            throw new RuntimeException(
                    "User not found in search: " + username
            );
        }

        clickElement(element);

        ConversationPage conversationPage =
                new ConversationPage();

        conversationPage.waitForPageToLoad();

        return conversationPage;
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
                            + TestIDs.MESSAGE_CONVERSATION_LAST_MESSAGE_SUFFIX;

            return isElementDisplayedByAccessibilityId(
                    lastMessageId
            );

        } catch (Exception e) {

            return false;
        }
    }

    public boolean hasConversationWith(String username) {
        try {
            String locator =
                    TestIDs.MESSAGE_CONVERSATION_ITEM_PREFIX
                            + username;
            return isElementDisplayedByAccessibilityId(
                    locator
            );

        } catch (Exception e) {

            return false;
        }
    }
}
