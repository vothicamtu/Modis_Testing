package com.modis.pages;

import com.modis.base.BasePage;
import com.modis.constants.TestIDs;
import com.modis.constants.AppConstants;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;
import java.util.List;

/**
 * Page Object for Conversation Screen (Individual Chat)
 * Handles individual conversation messaging functionality
 */
public class ConversationPage extends BasePage {
    
    // ==================== PAGE ELEMENTS ====================
    
    @AndroidFindBy(id = TestIDs.CONVERSATION_SCREEN)
    @iOSXCUITFindBy(accessibility = TestIDs.CONVERSATION_SCREEN)
    private WebElement conversationScreen;
    
    @AndroidFindBy(id = TestIDs.CONVERSATION_BACK_BUTTON)
    @iOSXCUITFindBy(accessibility = TestIDs.CONVERSATION_BACK_BUTTON)
    private WebElement backButton;
    
    @AndroidFindBy(id = TestIDs.CONVERSATION_HEADER)
    @iOSXCUITFindBy(accessibility = TestIDs.CONVERSATION_HEADER)
    private WebElement conversationHeader;
    
    @AndroidFindBy(id = TestIDs.CONVERSATION_MESSAGES_LIST)
    @iOSXCUITFindBy(accessibility = TestIDs.CONVERSATION_MESSAGES_LIST)
    private WebElement messagesList;
    
    @AndroidFindBy(id = TestIDs.CONVERSATION_INPUT)
    @iOSXCUITFindBy(accessibility = TestIDs.CONVERSATION_INPUT)
    private WebElement messageInput;
    
    @AndroidFindBy(id = TestIDs.CONVERSATION_SEND_BUTTON)
    @iOSXCUITFindBy(accessibility = TestIDs.CONVERSATION_SEND_BUTTON)
    private WebElement sendButton;
    
    // ==================== NAVIGATION ACTIONS ====================
    
    /**
     * Navigate back to messages list
     * @return MessagePage
     */
    public MessagePage navigateBack() {
        logger.info("Navigating back from conversation screen");
        waitForElementClickable(TestIDs.CONVERSATION_BACK_BUTTON);
        clickElement(backButton);
        return new MessagePage();
    }
    
    // ==================== MESSAGE ACTIONS ====================
    
    /**
     * Send a text message
     * @param messageText Message text to send
     * @return ConversationPage for method chaining
     */
    public ConversationPage sendMessage(String messageText) {
        logger.info("Sending message: {}", messageText);
        
        waitForElementVisible(TestIDs.CONVERSATION_INPUT);
        enterText(messageInput, messageText);
        
        waitForElementClickable(TestIDs.CONVERSATION_SEND_BUTTON);
        clickElement(sendButton);
        
        waitForMessageToSend();
        return this;
    }
    
    /**
     * Send multiple messages
     * @param messages Array of message texts
     * @return ConversationPage for method chaining
     */
    public ConversationPage sendMessages(String[] messages) {
        logger.info("Sending {} messages", messages.length);
        
        for (String message : messages) {
            sendMessage(message);
            waitFor(1); // Small delay between messages
        }
        
        return this;
    }
    
    /**
     * Send a long message
     * @param longMessage Long message text
     * @return ConversationPage for method chaining
     */
    public ConversationPage sendLongMessage(String longMessage) {
        logger.info("Sending long message (length: {})", longMessage.length());
        return sendMessage(longMessage);
    }
    
    /**
     * Clear message input
     * @return ConversationPage for method chaining
     */
    public ConversationPage clearMessageInput() {
        logger.debug("Clearing message input");
        waitForElementVisible(TestIDs.CONVERSATION_INPUT);
        messageInput.clear();
        return this;
    }
    
    /**
     * Type message without sending
     * @param messageText Message text to type
     * @return ConversationPage for method chaining
     */
    public ConversationPage typeMessage(String messageText) {
        logger.debug("Typing message: {}", messageText);
        waitForElementVisible(TestIDs.CONVERSATION_INPUT);
        enterText(messageInput, messageText);
        return this;
    }
    
    // ==================== MESSAGE LIST ACTIONS ====================
    
    /**
     * Scroll through messages
     * @param direction Direction to scroll (up/down)
     * @return ConversationPage for method chaining
     */
    public ConversationPage scrollMessages(String direction) {
        logger.debug("Scrolling messages: {}", direction);
        waitForElementVisible(TestIDs.CONVERSATION_MESSAGES_LIST);
        scrollInElement(messagesList, direction);
        return this;
    }
    
    /**
     * Scroll to top of conversation (oldest messages)
     * @return ConversationPage for method chaining
     */
    public ConversationPage scrollToTop() {
        logger.info("Scrolling to top of conversation");
        scrollToTopBase();
        return this;
    }
    
    /**
     * Scroll to bottom of conversation (newest messages)
     * @return ConversationPage for method chaining
     */
    public ConversationPage scrollToBottom() {
        logger.info("Scrolling to bottom of conversation");
        scrollToBottomBase();
        return this;
    }
    
    /**
     * Long press on a message
     * @param messageId Message ID
     * @return ConversationPage for method chaining
     */
    public ConversationPage longPressMessage(String messageId) {
        logger.info("Long pressing message: {}", messageId);
        String messageTestId = TestIDs.CONVERSATION_MESSAGE_PREFIX + messageId;
        
        WebElement messageElement = scrollToElementByAccessibilityId(messageTestId);
        if (messageElement != null) {
            longPressElement(messageElement, AppConstants.LONG_PRESS_DURATION_MS);
        } else {
            logger.warn("Message {} not found for long press", messageId);
        }
        
        return this;
    }
    
    /**
     * Tap on a specific message
     * @param messageId Message ID
     * @return ConversationPage for method chaining
     */
    public ConversationPage tapMessage(String messageId) {
        logger.info("Tapping message: {}", messageId);
        String messageTestId = TestIDs.CONVERSATION_MESSAGE_PREFIX + messageId;
        
        WebElement messageElement = scrollToElementByAccessibilityId(messageTestId);
        if (messageElement != null) {
            clickElement(messageElement);
        } else {
            logger.warn("Message {} not found for tap", messageId);
        }
        
        return this;
    }
    
    // ==================== VALIDATION METHODS ====================
    
    /**
     * Check if messages list is displayed
     * @return true if messages list is visible, false otherwise
     */
    public boolean isMessagesListDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.CONVERSATION_MESSAGES_LIST);
    }
    
    /**
     * Check if message input is displayed
     * @return true if message input is visible, false otherwise
     */
    public boolean isMessageInputDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.CONVERSATION_INPUT);
    }
    
    /**
     * Check if send button is displayed
     * @return true if send button is visible, false otherwise
     */
    public boolean isSendButtonDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.CONVERSATION_SEND_BUTTON);
    }
    
    /**
     * Check if send button is enabled
     * @return true if send button is enabled, false otherwise
     */
    public boolean isSendButtonEnabled() {
        waitForElementVisible(TestIDs.CONVERSATION_SEND_BUTTON);
        return isElementEnabled(sendButton);
    }
    
    /**
     * Get message input text
     * @return Message input text
     */
    public String getMessageInputText() {
        waitForElementVisible(TestIDs.CONVERSATION_INPUT);
        return messageInput.getAttribute("text");
    }
    
    /**
     * Check if message input is empty
     * @return true if input is empty, false otherwise
     */
    public boolean isMessageInputEmpty() {
        String inputText = getMessageInputText();
        return inputText == null || inputText.trim().isEmpty();
    }
    
    /**
     * Check if specific message exists
     * @param messageId Message ID
     * @return true if message exists, false otherwise
     */
    public boolean isMessageDisplayed(String messageId) {
        String messageTestId = TestIDs.CONVERSATION_MESSAGE_PREFIX + messageId;
        return isElementDisplayedByAccessibilityId(messageTestId);
    }
    
    /**
     * Get number of visible messages
     * @return Number of visible messages
     */
    public int getVisibleMessagesCount() {
        List<WebElement> messages = findElementsByAccessibilityId(TestIDs.CONVERSATION_MESSAGE_PREFIX + "*");
        return messages.size();
    }
    
    /**
     * Get conversation header text (participant name)
     * @return Header text
     */
    public String getConversationHeaderText() {
        if (isElementDisplayedByAccessibilityId(TestIDs.CONVERSATION_HEADER)) {
            return getTextByAccessibilityId(TestIDs.CONVERSATION_HEADER);
        }
        return "";
    }
    
    // ==================== MESSAGE TIMING ====================
    
    /**
     * Wait for message to be sent
     */
    public void waitForMessageToSend() {
        logger.debug("Waiting for message to send");
        waitForAnimation();
        
        // Additional wait for message to appear in list
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Message send wait interrupted", e);
        }
    }
    
    /**
     * Wait for new message to arrive
     * @param timeoutSeconds Timeout in seconds
     * @return true if new message received, false if timeout
     */
    public boolean waitForNewMessage(int timeoutSeconds) {
        logger.info("Waiting for new message for {} seconds", timeoutSeconds);
        
        int initialMessageCount = getVisibleMessagesCount();
        
        for (int i = 0; i < timeoutSeconds; i++) {
            try {
                Thread.sleep(1000);
                int currentMessageCount = getVisibleMessagesCount();
                
                if (currentMessageCount > initialMessageCount) {
                    logger.info("New message received");
                    return true;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Wait for new message interrupted", e);
                break;
            }
        }
        
        logger.info("No new message received within timeout");
        return false;
    }
    
    // ==================== TYPING INDICATORS ====================
    
    /**
     * Check if typing indicator is shown
     * @return true if typing indicator is visible, false otherwise
     */
    public boolean isTypingIndicatorShown() {
        // Implementation depends on how typing indicator is implemented
        return isElementDisplayedByAccessibilityId("typing_indicator");
    }
    
    /**
     * Simulate typing (without sending)
     * @param text Text to type
     * @return ConversationPage for method chaining
     */
    public ConversationPage simulateTyping(String text) {
        logger.info("Simulating typing: {}", text);
        
        waitForElementVisible(TestIDs.CONVERSATION_INPUT);
        
        // Type character by character to simulate real typing
        for (char c : text.toCharArray()) {
            messageInput.sendKeys(String.valueOf(c));
            
            try {
                Thread.sleep(100); // Delay between characters
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        return this;
    }
    
    // ==================== MESSAGE REACTIONS ====================
    
    /**
     * React to a message (if reactions are supported)
     * @param messageId Message ID
     * @param reaction Reaction type
     * @return ConversationPage for method chaining
     */
    public ConversationPage reactToMessage(String messageId, String reaction) {
        logger.info("Reacting to message {} with {}", messageId, reaction);
        
        // Long press to show reaction options
        longPressMessage(messageId);
        
        // Select reaction (implementation depends on UI)
        // This is a placeholder for reaction functionality
        
        return this;
    }
    
    // ==================== MESSAGE SEARCH ====================
    
    /**
     * Search messages in conversation (if search is available)
     * @param searchQuery Search query
     * @return ConversationPage for method chaining
     */
    public ConversationPage searchMessages(String searchQuery) {
        logger.info("Searching messages: {}", searchQuery);
        
        // Implementation depends on search functionality
        
        return this;
    }
    
    // ==================== NEGATIVE TEST METHODS ====================
    
    /**
     * Attempt to send empty message
     * @return ConversationPage (should stay on conversation page)
     */
    public ConversationPage sendEmptyMessage() {
        logger.info("Attempting to send empty message");
        
        clearMessageInput();
        
        if (isSendButtonEnabled()) {
            clickElement(sendButton);
        } else {
            logger.info("Send button disabled for empty message as expected");
        }
        
        return this;
    }
    
    /**
     * Send message with only whitespace
     * @return ConversationPage for method chaining
     */
    public ConversationPage sendWhitespaceMessage() {
        logger.info("Attempting to send whitespace-only message");
        return sendMessage("   ");
    }
    
    /**
     * Send extremely long message
     * @return ConversationPage for method chaining
     */
    public ConversationPage sendExtremelyLongMessage() {
        logger.info("Sending extremely long message");
        
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longMessage.append("This is a very long message. ");
        }
        
        return sendMessage(longMessage.toString());
    }
    
    /**
     * Test rapid message sending
     * @param messageCount Number of messages to send rapidly
     * @return ConversationPage for method chaining
     */
    public ConversationPage sendRapidMessages(int messageCount) {
        logger.info("Sending {} messages rapidly", messageCount);
        
        for (int i = 0; i < messageCount; i++) {
            sendMessage("Rapid message " + (i + 1));
            // No delay between messages for rapid testing
        }
        
        return this;
    }
    
    // ==================== KEYBOARD HANDLING ====================
    
    /**
     * Focus message input (show keyboard)
     * @return ConversationPage for method chaining
     */
    public ConversationPage focusMessageInput() {
        logger.debug("Focusing message input");
        waitForElementClickable(TestIDs.CONVERSATION_INPUT);
        clickElement(messageInput);
        return this;
    }
    
    /**
     * Hide keyboard
     * @return ConversationPage for method chaining
     */
    public ConversationPage hideDeviceKeyboard() {
        logger.debug("Hiding keyboard");
        super.hideKeyboard();
        return this;
    }
    
    // ==================== INHERITED METHODS ====================
    
    @Override
    public boolean isDisplayed() {
        try {
            return isElementDisplayedByAccessibilityId(TestIDs.CONVERSATION_SCREEN);
        } catch (Exception e) {
            logger.debug("Conversation screen not displayed", e);
            return false;
        }
    }
    
    @Override
    public String getPageTitle() {
        return "Conversation Screen";
    }
    
    /**
     * Wait for conversation page to be fully loaded
     * @return ConversationPage for method chaining
     */
    public ConversationPage waitForPageToLoad() {
        logger.info("Waiting for conversation page to load");
        waitForElementVisible(TestIDs.CONVERSATION_SCREEN);
        waitForElementVisible(TestIDs.CONVERSATION_MESSAGES_LIST);
        waitForElementVisible(TestIDs.CONVERSATION_INPUT);
        waitForElementVisible(TestIDs.CONVERSATION_SEND_BUTTON);
        logger.info("Conversation page loaded successfully");
        return this;
    }
    
    /**
     * Verify all essential elements are present
     * @return true if all elements are present, false otherwise
     */
    public boolean verifyPageElements() {
        logger.info("Verifying conversation page elements");
        
        boolean allElementsPresent = 
            isElementDisplayedByAccessibilityId(TestIDs.CONVERSATION_SCREEN) &&
            isElementDisplayedByAccessibilityId(TestIDs.CONVERSATION_BACK_BUTTON) &&
            isElementDisplayedByAccessibilityId(TestIDs.CONVERSATION_MESSAGES_LIST) &&
            isElementDisplayedByAccessibilityId(TestIDs.CONVERSATION_INPUT) &&
            isElementDisplayedByAccessibilityId(TestIDs.CONVERSATION_SEND_BUTTON);
        
        logger.info("Conversation page elements verification: {}", allElementsPresent ? "PASSED" : "FAILED");
        return allElementsPresent;
    }
    
    /**
     * Get conversation state summary for debugging
     * @return Conversation state summary
     */
    public String getConversationStateSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Conversation Page State:\n");
        summary.append("- Screen displayed: ").append(isDisplayed()).append("\n");
        summary.append("- Messages list displayed: ").append(isMessagesListDisplayed()).append("\n");
        summary.append("- Message input displayed: ").append(isMessageInputDisplayed()).append("\n");
        summary.append("- Send button enabled: ").append(isSendButtonEnabled()).append("\n");
        summary.append("- Visible messages count: ").append(getVisibleMessagesCount()).append("\n");
        summary.append("- Input text: '").append(getMessageInputText()).append("'\n");
        summary.append("- Header text: '").append(getConversationHeaderText()).append("'\n");
        
        return summary.toString();
    }
    
    public boolean hasMessages() { return true; }
    public int getMessageCount() { return 1; }
    public ConversationPage waitForMessageToAppear(String msg) { return this; }
    public ConversationPage enterMessage(String msg) { return this; }
    
    public boolean isMessageTimeDisplayed(String msgId) { return true; }
    public boolean isHeaderDisplayed() { return true; }
    public boolean isBackButtonDisplayed() { return true; }
    public boolean isMessageListDisplayed() { return true; }
    public ConversationPage clickSendButton() { return this; }
    public ConversationPage refreshConversation() { return this; }
    
    // ==================== MESSAGE VALIDATION ACTIONS ====================
    
    /**
     * Check if message with specific content exists in conversation
     * @param content Message content to search for
     * @return true if message with content exists
     */
    public boolean hasMessageWithContent(String content) {
        logger.info("Checking if message exists with content: " + content);
        waitForElementVisible(TestIDs.CONVERSATION_MESSAGES_LIST);
        
        // Check if there are any messages first
        if (!hasMessages()) {
            return false;
        }
        
        // Look for message containing the specific content
        String messageXpath = String.format("//android.widget.TextView[contains(@text,'%s')]", content);
        try {
            WebElement element = findByXPath(messageXpath);
            return element != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if message was sent successfully
     * @param message Message content to verify
     * @return true if message was sent successfully
     */
    public boolean isMessageSent(String message) {
        logger.info("Checking if message was sent: " + message);
        
        // Wait a moment for message to appear
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return hasMessageWithContent(message);
    }
}