package com.modis.pages;

import com.modis.base.BasePage;
import com.modis.constants.TestIDs;
import com.modis.constants.AppConstants;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

import java.util.List;

import static com.modis.drivers.DriverManager.getDriver;

public class ConversationPage extends BasePage {
    @AndroidFindBy(accessibility = TestIDs.CONVERSATION_SCREEN)
    private WebElement conversationScreen;
    @AndroidFindBy(accessibility = TestIDs.CONVERSATION_BACK_BUTTON)
    private WebElement backButton;
    @AndroidFindBy(accessibility = TestIDs.CONVERSATION_HEADER)
    private WebElement conversationHeader;
    @AndroidFindBy(accessibility = TestIDs.CONVERSATION_MESSAGES_LIST)
    private WebElement messagesList;
    @AndroidFindBy(accessibility = TestIDs.CONVERSATION_INPUT)
    private WebElement messageInput;
    @AndroidFindBy(accessibility = TestIDs.CONVERSATION_SEND_BUTTON)
    private WebElement sendButton;

    public MessagePage navigateBack() {
        logger.info("Navigating back from conversation screen");
        waitForElementVisible(TestIDs.CONVERSATION_BACK_BUTTON);
        waitFor(1);
        clickByAccessibilityId(
                TestIDs.CONVERSATION_BACK_BUTTON
        );
        waitForAnimation();
        return new MessagePage();
    }

    public ConversationPage sendMessage(String messageText) {
        logger.info("Sending message: {}", messageText);
        waitForElementVisible(TestIDs.CONVERSATION_INPUT);
        clearMessageInput();
        enterText(
                messageInput,
                messageText
        );
        waitForElementClickable(TestIDs.CONVERSATION_SEND_BUTTON);
        clickElement(sendButton);
        waitForMessageToSend();
        return this;
    }

    public ConversationPage sendMessages(String[] messages) {
        logger.info("Sending {} messages", messages.length);
        for (String message : messages) {
            sendMessage(message);
            waitFor(1);
        }
        return this;
    }

    public ConversationPage sendLongMessage(String longMessage) {
        logger.info("Sending long message (length: {})", longMessage.length());
        return sendMessage(longMessage);
    }

    public ConversationPage clearMessageInput() {
        logger.debug("Clearing message input");
        waitForElementVisible(TestIDs.CONVERSATION_INPUT);
        messageInput.clear();
        return this;
    }

    public ConversationPage typeMessage(String messageText) {
        logger.debug("Typing message: {}", messageText);
        waitForElementVisible(TestIDs.CONVERSATION_INPUT);
        clearMessageInput();
        enterText(
                messageInput,
                messageText
        );
        return this;
    }

    public ConversationPage scrollMessages(String direction) {
        logger.debug("Scrolling messages: {}", direction);
        waitForElementVisible(TestIDs.CONVERSATION_MESSAGES_LIST);
        scrollInElement(messagesList, direction);
        return this;
    }

    public ConversationPage scrollToTop() {
        logger.info("Scrolling to top of conversation");
        scrollToTopBase();
        return this;
    }

    public ConversationPage scrollToBottom() {
        logger.info("Scrolling to bottom of conversation");
        scrollToBottomBase();
        return this;
    }

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

    public boolean isMessagesListDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.CONVERSATION_MESSAGES_LIST);
    }

    public boolean isMessageInputDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.CONVERSATION_INPUT);
    }

    public boolean isSendButtonDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.CONVERSATION_SEND_BUTTON);
    }

    public boolean isSendButtonEnabled() {
        waitForElementVisible(TestIDs.CONVERSATION_SEND_BUTTON);
        return isElementEnabled(sendButton);
    }

    public String getMessageInputText() {
        waitForElementVisible(TestIDs.CONVERSATION_INPUT);
        String text = messageInput.getAttribute("text");
        if (text == null || "Nhắn tin...".equals(text.trim())) {
            return "";
        }
        return text;
    }

    public boolean isMessageInputEmpty() {
        String inputText = getMessageInputText();
        return inputText == null || inputText.trim().isEmpty();
    }

    public boolean isMessageDisplayed(String messageText) {
        return hasMessageWithContent(messageText);
    }

    public int getVisibleMessagesCount() {
        try {
            waitForElementVisible(
                    TestIDs.CONVERSATION_MESSAGES_LIST
            );
            List<WebElement> messages =
                    getDriver().findElements(
                            AppiumBy.xpath(
                                    "//android.widget.TextView"
                            )
                    );
            int count = 0;
            for (WebElement element : messages) {
                String text = element.getText();
                if (text != null
                        && !text.trim().isEmpty()) {
                    count++;
                }
            }
            return count;
        } catch (Exception e) {
            logger.warn(
                    "Failed getting visible messages count: {}",
                    e.getMessage()
            );
            return 0;
        }
    }

    public String getConversationHeaderText() {
        if (isElementDisplayedByAccessibilityId(TestIDs.CONVERSATION_HEADER)) {
            return getTextByAccessibilityId(TestIDs.CONVERSATION_HEADER);
        }
        return "";
    }

    public void waitForMessageToSend() {
        logger.debug(
                "Waiting for message to send"
        );
        waitForAnimation();
        waitFor(2);
    }

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

    public ConversationPage simulateTyping(String text) {
        logger.info("Simulating typing: {}", text);
        waitForElementVisible(TestIDs.CONVERSATION_INPUT);
        for (char c : text.toCharArray()) {
            messageInput.sendKeys(String.valueOf(c));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return this;
    }

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

    public ConversationPage sendWhitespaceMessage() {
        logger.info("Attempting to send whitespace-only message");
        return sendMessage("   ");
    }

    public ConversationPage sendExtremelyLongMessage() {
        logger.info("Sending extremely long message");
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longMessage.append("This is a very long message. ");
        }
        return sendMessage(longMessage.toString());
    }

    public ConversationPage sendRapidMessages(int messageCount) {
        logger.info("Sending {} messages rapidly", messageCount);
        for (int i = 0; i < messageCount; i++) {
            sendMessage("Rapid message " + (i + 1));
        }
        return this;
    }

    public ConversationPage focusMessageInput() {
        logger.debug("Focusing message input");
        waitForElementClickable(TestIDs.CONVERSATION_INPUT);
        clickElement(messageInput);
        return this;
    }

    public ConversationPage hideDeviceKeyboard() {
        logger.debug("Hiding keyboard");
        super.hideKeyboard();
        return this;
    }

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

    public ConversationPage waitForPageToLoad() {
        logger.info("Waiting for conversation page to load");
        waitForElementVisible(TestIDs.CONVERSATION_SCREEN);
        waitForElementVisible(TestIDs.CONVERSATION_MESSAGES_LIST);
        waitForElementVisible(TestIDs.CONVERSATION_INPUT);
        waitForElementVisible(TestIDs.CONVERSATION_SEND_BUTTON);
        logger.info("Conversation page loaded successfully");
        return this;
    }

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

    public boolean hasMessages() {
        try {
            waitForElementVisible(
                    TestIDs.CONVERSATION_MESSAGES_LIST
            );
            List<WebElement> textViews =
                    getDriver().findElements(
                            AppiumBy.className(
                                    "android.widget.TextView"
                            )
                    );
            return !textViews.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public int getMessageCount() {
        return getVisibleMessagesCount();
    }

    public ConversationPage waitForMessageToAppear(
            String messageText
    ) {
        logger.info(
                "Waiting for message: {}",
                messageText
        );
        String expectedText =
                messageText.length() > 20
                        ? messageText.substring(0, 20)
                        : messageText;
        for (int i = 0; i < 15; i++) {
            if (hasMessageWithContent(expectedText)) {
                logger.info(
                        "Message appeared: {}",
                        expectedText
                );
                return this;
            }
            waitFor(1);
        }
        throw new RuntimeException(
                "Message did not appear: "
                        + expectedText
        );
    }

    public ConversationPage enterMessage(String msg) {
        logger.info(
                "Entering message: {}",
                msg
        );
        waitForElementVisible(
                TestIDs.CONVERSATION_INPUT
        );
        clearMessageInput();
        enterText(
                messageInput,
                msg
        );
        return this;
    }

    public boolean isMessageTimeDisplayed(
            String msgId
    ) {
        try {
            String timeId =
                    TestIDs.CONVERSATION_MESSAGE_PREFIX
                            + msgId
                            + "_time";
            return isElementDisplayedByAccessibilityId(
                    timeId
            );
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isHeaderDisplayed() {
        return isElementDisplayedByAccessibilityId(
                TestIDs.CONVERSATION_HEADER
        );
    }

    public boolean isBackButtonDisplayed() {
        return isElementDisplayedByAccessibilityId(
                TestIDs.CONVERSATION_BACK_BUTTON
        );
    }

    public boolean isMessageListDisplayed() {
        return isElementDisplayedByAccessibilityId(
                TestIDs.CONVERSATION_MESSAGES_LIST
        );
    }

    public ConversationPage clickSendButton() {
        waitForElementClickable(
                TestIDs.CONVERSATION_SEND_BUTTON
        );
        clickElement(sendButton);
        return this;
    }

    public ConversationPage refreshConversation() {
        logger.info(
                "Refreshing conversation"
        );
        pullToRefresh();
        waitForAnimation();
        return this;
    }

    public boolean hasMessageWithContent(
            String content
    ) {
        try {
            waitForElementVisible(
                    TestIDs.CONVERSATION_MESSAGES_LIST
            );
            List<WebElement> textViews =
                    getDriver().findElements(
                            AppiumBy.className(
                                    "android.widget.TextView"
                            )
                    );
            for (WebElement element : textViews) {
                String text =
                        element.getText();
                if (text == null) {
                    continue;
                }
                if (text.contains(content)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            logger.warn(
                    "Failed checking message content: {}",
                    e.getMessage()
            );
            return false;
        }
    }

    public boolean isMessageSent(String message) {
        logger.info("Checking if message was sent: " + message);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return hasMessageWithContent(message);
    }

    public boolean hasVisibleSentImageMessage() {
        try {
            waitForElementVisible(TestIDs.CONVERSATION_MESSAGES_LIST);
            for (int i = 0; i < 10; i++) {
                if (getDriver().getPageSource().contains(TestIDs.CONVERSATION_SENT_IMAGE_SUFFIX)) {
                    return true;
                }
                waitFor(1);
            }
            return false;
        } catch (Exception e) {
            logger.warn("Failed checking sent image message: {}", e.getMessage());
            return false;
        }
    }
}
