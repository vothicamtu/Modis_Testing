package com.modis.tests;

import com.modis.base.BaseTest;
import com.modis.base.BasePage;
import com.modis.pages.*;
import com.modis.utils.TestDataReader;
import com.modis.drivers.DriverManager;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.List;

public class MessagingTests extends BaseTest {

    private HomePage homePage;
    private TestDataReader testDataReader = new TestDataReader();

    @DataProvider(name = "testMessagesData")
    public Object[][] getTestMessagesData() {

        List<Map<String, Object>> allMessages =
                testDataReader.getTestMessages();

        List<Map<String, Object>> filteredMessages =
                new ArrayList<>();

        for (Map<String, Object> message : allMessages) {

            String sender =
                    (String) message.get("senderUsername");

            String receiver =
                    (String) message.get("receiverUsername");

            boolean isTuTuneConversation =

                    (sender.equals("tu")
                            && receiver.equals("tune"))

                            ||

                            (sender.equals("tune")
                                    && receiver.equals("tu"));

            if (isTuTuneConversation) {
                filteredMessages.add(message);
            }
        }

        Object[][] data =
                new Object[filteredMessages.size()][];

        for (int i = 0; i < filteredMessages.size(); i++) {

            Map<String, Object> message =
                    filteredMessages.get(i);

            data[i] = new Object[]{

                    message.get("senderUsername"),

                    message.get("receiverUsername"),

                    message.get("content")
            };
        }

        return data;
    }

    @DataProvider(name = "conversationScenariosData")
    public Object[][] getConversationScenariosData() {
        List<Map<String, Object>> scenarios = testDataReader.getConversationScenarios();
        Object[][] data = new Object[scenarios.size()][];

        for (int i = 0; i < scenarios.size(); i++) {
            Map<String, Object> scenario = scenarios.get(i);
            data[i] = new Object[]{
                    scenario.get("name"),
                    scenario.get("participants"),
                    scenario.get("messages")
            };
        }
        return data;
    }

    @BeforeMethod(alwaysRun = true)
    public void loginBeforeTest() {

        logger.info(
                "Logging in before messaging test"
        );

        try {

            HomePage currentHome =
                    new HomePage();

            if (currentHome.isDisplayed()) {

                logger.info(
                        "Already on Home screen - skipping login"
                );

                homePage = currentHome;

                return;
            }

        } catch (Exception ignore) {
        }

        LoginPage loginPage;

        try {

            LoginPage currentLogin =
                    new LoginPage();

            if (currentLogin.isDisplayed()) {

                logger.info(
                        "Detected Login screen"
                );

                loginPage = currentLogin;

            } else {

                logger.info(
                        "Detected Loading screen"
                );

                LoadingPage loadingPage =
                        new LoadingPage();

                loginPage =
                        loadingPage.clickLoginButton();
            }

        } catch (Exception e) {

            logger.warn(
                    "Fallback to LoadingPage flow: {}",
                    e.getMessage()
            );

            LoadingPage loadingPage =
                    new LoadingPage();

            loginPage =
                    loadingPage.clickLoginButton();
        }

        Map<String, Object> testUser =
                testDataReader.getValidUserByUsername("tu");

        Assert.assertNotNull(
                testUser,
                "Test user should exist"
        );

        String username =
                (String) testUser.get("username");

        String password =
                (String) testUser.get("password");

        BasePage afterLogin =
                loginPage.login(username, password);

        Assert.assertTrue(
                afterLogin instanceof HomePage,
                "Login should navigate to HomePage"
        );

        homePage = (HomePage) afterLogin;

        homePage.waitForTopbarReadyAfterLogin(8);

        Assert.assertTrue(
                homePage.isDisplayed(),
                "Should be on Home page"
        );

        logger.info(
                "Logged in successfully with user: {}",
                username
        );
    }

    @Test(priority = 1, groups = {"messaging", "smoke"}, description = "Verify navigation to message screen")
    public void testNavigateToMessages() {
        logger.info("Starting navigate to messages test");

        int maxRetries = 3;
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                logger.info("Navigation attempt {}/{}", attempt, maxRetries);
                
                Assert.assertTrue(homePage.isDisplayed(), "Should be on home page before navigation");
                
                MessagePage messagePage = homePage.navigateToMessages();
                
                Assert.assertTrue(messagePage.isDisplayed(), "Message page should be displayed after navigation");
                Assert.assertTrue(messagePage.verifyPageElements(), "Message page elements should be present");
                
                String pageState = messagePage.getPageStateSummary();
                logger.info("Message page state after navigation: {}", pageState);

                logger.info("Navigate to messages test completed successfully on attempt {}", attempt);
                return;
                
            } catch (Exception e) {
                lastException = e;
                logger.warn("Navigation attempt {} failed: {}", attempt, e.getMessage());
                
                if (attempt < maxRetries) {
                    logger.info("Retrying navigation in 3 seconds...");
                    try {
                        Thread.sleep(3000);
                        
                        try {
                            homePage = new HomePage();
                            if (!homePage.isDisplayed()) {
                                DriverManager.getDriver().navigate().back();
                                Thread.sleep(2000);
                            }
                        } catch (Exception recoveryException) {
                            logger.warn("Recovery attempt failed: {}", recoveryException.getMessage());
                        }
                        
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Test interrupted during retry", ie);
                    }
                } else {
                    logger.error("All navigation attempts failed");
                }
            }
        }
        
        throw new RuntimeException("Navigation to messages failed after " + maxRetries + " attempts", lastException);
    }

    @Test(priority = 2, groups = {"messaging", "regression"}, description = "Verify message list display")
    public void testMessageListDisplay() {
        logger.info("Starting message list display test");

        MessagePage messagePage = homePage.navigateToMessages();

        Assert.assertTrue(messagePage.isDisplayed(), "Message page should be displayed");
        Assert.assertTrue(messagePage.verifyPageElements(), "Message page elements should be present");

        boolean hasConversations = messagePage.hasConversations();
        logger.info("Conversations available: {}", hasConversations);

        if (hasConversations) {
            Assert.assertTrue(hasConversations, "Conversation list should contain conversations");
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            boolean isListDisplayed = false;
            int maxRetries = 3;
            
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                logger.info("Checking if conversations list is displayed (attempt {}/{})", attempt, maxRetries);
                
                try {
                    isListDisplayed = messagePage.isConversationsListDisplayed();
                    if (isListDisplayed) {
                        logger.info("Conversations list detected on attempt {}", attempt);
                        break;
                    } else {
                        logger.warn("Conversations list not detected on attempt {}", attempt);
                        if (attempt < maxRetries) {
                            Thread.sleep(1000);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Exception during conversations list check on attempt {}: {}", attempt, e.getMessage());
                    if (attempt < maxRetries) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
            
            logger.info("Final state check:");
            logger.info("- Has conversations: {}", hasConversations);
            logger.info("- Is conversations list displayed: {}", isListDisplayed);
            logger.info("- Page state summary:\n{}", messagePage.getPageStateSummary());
            
            Assert.assertTrue(isListDisplayed, 
                    "Conversations list should be displayed when conversations exist. " +
                    "Has conversations: " + hasConversations + ", List displayed: " + isListDisplayed);
            
            int conversationCount = messagePage.getVisibleConversationsCount();
            Assert.assertTrue(conversationCount > 0, 
                    "Should have at least one visible conversation, found: " + conversationCount);
            
            logger.info("Found {} conversations", conversationCount);
        } else {
            Assert.assertTrue(messagePage.isEmptyStateDisplayed(),
                    "Empty state should be displayed when no conversations exist");
            
            logger.info("No conversations found - empty state is correctly displayed");
        }

        logger.info("Message list display test completed successfully");
    }

    @Test(priority = 3, groups = {"messaging", "regression"}, description = "Verify conversation item elements")
    public void testConversationItemElements() {
        logger.info("Starting conversation item elements test");

        MessagePage messagePage = homePage.navigateToMessages();

        if (!messagePage.hasConversations()) {
            logger.info("No conversations available for testing conversation item elements - test will be skipped");
            throw new org.testng.SkipException("No conversations available for testing conversation item elements");
        }

        String firstConversationId = messagePage.getFirstConversationId();
        
        if (firstConversationId == null || firstConversationId.trim().isEmpty()) {
            logger.error("First conversation ID is null or empty despite hasConversations() returning true");
            logger.error("This indicates a data loading or UI rendering issue");
            
            messagePage.refreshConversations();
            
            firstConversationId = messagePage.getFirstConversationId();
            
            if (firstConversationId == null || firstConversationId.trim().isEmpty()) {
                Assert.fail("First conversation ID should not be null or empty after refresh. " +
                           "Check if conversations are properly loaded from backend API.");
            }
        }
        
        logger.info("Testing conversation elements for ID: {}", firstConversationId);
        
        Assert.assertTrue(messagePage.isConversationAvatarDisplayed(firstConversationId),
                "Conversation avatar should be displayed for ID: " + firstConversationId);
        Assert.assertTrue(messagePage.isConversationNameDisplayed(firstConversationId),
                "Conversation name should be displayed for ID: " + firstConversationId);
        
        String conversationName = messagePage.getConversationName(firstConversationId);
        Assert.assertFalse(conversationName.isEmpty(),
                "Conversation name should not be empty for ID: " + firstConversationId);

        if (messagePage.hasLastMessage(firstConversationId)) {
            Assert.assertTrue(messagePage.isLastMessageDisplayed(firstConversationId),
                    "Last message should be displayed for ID: " + firstConversationId);
            Assert.assertTrue(messagePage.isMessageTimeDisplayed(firstConversationId),
                    "Message time should be displayed for ID: " + firstConversationId);
        } else {
            logger.info("No last message found for conversation ID: {}", firstConversationId);
        }

        logger.info("Conversation item elements test completed successfully");
    }

    @Test(priority = 4, groups = {"messaging", "regression"}, description = "Verify conversation selection")
    public void testConversationSelection() {
        logger.info("Starting conversation selection test");

        MessagePage messagePage = homePage.navigateToMessages();

        if (messagePage.hasConversations()) {
            String firstConversationId = messagePage.getFirstConversationId();
            Assert.assertNotNull(
                    firstConversationId,
                    "First conversation id should not be null"
            );
            ConversationPage conversationPage = messagePage.selectConversation(firstConversationId);

            Assert.assertTrue(conversationPage.isDisplayed(),
                    "Conversation page should be displayed after selection");
            Assert.assertTrue(conversationPage.verifyPageElements(),
                    "Conversation page elements should be present");
        } else {
            logger.info("No conversations available for testing conversation selection");
        }

        logger.info("Conversation selection test completed successfully");
    }

    @Test(priority = 5, groups = {"messaging", "regression"}, description = "Verify conversation page elements")
    public void testConversationPageElements() {
        logger.info("Starting conversation page elements test");

        MessagePage messagePage = homePage.navigateToMessages();

        if (messagePage.hasConversations()) {
            String firstConversationId = messagePage.getFirstConversationId();
            Assert.assertNotNull(
                    firstConversationId,
                    "First conversation id should not be null"
            );
            ConversationPage conversationPage = messagePage.selectConversation(firstConversationId);

            Assert.assertTrue(conversationPage.isHeaderDisplayed(),
                    "Conversation header should be displayed");
            Assert.assertTrue(conversationPage.isBackButtonDisplayed(),
                    "Back button should be displayed");
            Assert.assertTrue(conversationPage.isMessageInputDisplayed(),
                    "Message input should be displayed");
            Assert.assertTrue(conversationPage.isSendButtonDisplayed(),
                    "Send button should be displayed");

            if (conversationPage.hasMessages()) {
                Assert.assertTrue(conversationPage.isMessageListDisplayed(),
                        "Message list should be displayed when messages exist");
            }
        } else {
            logger.info("No conversations available for testing conversation page elements");
        }

        logger.info("Conversation page elements test completed successfully");
    }

    @Test(priority = 6, groups = {"messaging", "navigation"}, description = "Verify back navigation from conversation")
    public void testBackNavigationFromConversation() {
        logger.info("Starting back navigation from conversation test");

        MessagePage messagePage = homePage.navigateToMessages();

        if (messagePage.hasConversations()) {
            String firstConversationId = messagePage.getFirstConversationId();
            Assert.assertNotNull(
                    firstConversationId,
                    "First conversation id should not be null"
            );
            ConversationPage conversationPage = messagePage.selectConversation(firstConversationId);

            MessagePage returnedMessagePage = conversationPage.navigateBack();

            Assert.assertTrue(returnedMessagePage.isDisplayed(),
                    "Should return to message page after back navigation");
            Assert.assertTrue(returnedMessagePage.verifyPageElements(),
                    "Message page elements should be present after back navigation");
        } else {
            logger.info("No conversations available for testing back navigation");
        }

        logger.info("Back navigation from conversation test completed successfully");
    }

    @Test(priority = 7, groups = {"messaging", "navigation"}, description = "Verify back navigation from message list")
    public void testBackNavigationFromMessageList() {
        logger.info("Starting back navigation from message list test");

        MessagePage messagePage = homePage.navigateToMessages();
        HomePage returnedHomePage = messagePage.navigateBack();

        Assert.assertTrue(returnedHomePage.isDisplayed(),
                "Should return to home page after back navigation from message list");
        Assert.assertTrue(returnedHomePage.verifyPageElements(),
                "Home page elements should be present after back navigation");

        logger.info("Back navigation from message list test completed successfully");
    }

    @Test(priority = 8, groups = {"messaging", "regression", "data"}, dataProvider = "testMessagesData", description = "Verify messages data from real database")
    public void testMessagesWithRealData(String senderUsername, String receiverUsername, String content) {
        logger.info("Testing message data - From: " + senderUsername + " To: " + receiverUsername +
                " Content: " + content.substring(0, Math.min(content.length(), 20)) + "...");

        MessagePage messagePage = homePage.navigateToMessages();

        if (messagePage.hasConversations()) {
            if (messagePage.hasConversationWith(senderUsername) || messagePage.hasConversationWith(receiverUsername)) {
                String conversationPartner = messagePage.hasConversationWith(senderUsername) ? senderUsername : receiverUsername;

                ConversationPage conversationPage = messagePage.openConversation(conversationPartner);

                Assert.assertTrue(conversationPage.isDisplayed(),
                        "Conversation page should be displayed");

                if (conversationPage.hasMessages()) {
                    String shortContent = content.length() > 10 ? content.substring(0, 10) : content;
                    if (conversationPage.hasMessageWithContent(shortContent)) {
                        logger.info("Found message with content: " + shortContent);
                    }
                }

                conversationPage.navigateBack();
            }
        }

        logger.info("Message data test completed for: " + senderUsername + " -> " + receiverUsername);
    }

    @Test(priority = 9, groups = {"messaging", "regression", "data"}, dataProvider = "conversationScenariosData", description = "Verify tu and tune conversation scenarios")
    public void testConversationScenariosWithRealData(String scenarioName, List<String> participants, List<String> messages) {

        logger.info("Testing scenario: " + scenarioName);

        if (!(participants.contains("tu")
                && participants.contains("tune"))) {

            logger.info("Skipping non tu-tune scenario");
            return;
        }

        MessagePage messagePage =
                homePage.navigateToMessages();

        Assert.assertTrue(
                messagePage.hasConversationWith("tune"),
                "Conversation with tune should exist"
        );

        ConversationPage conversationPage =
                messagePage.openConversation("tune");

        Assert.assertTrue(
                conversationPage.isDisplayed(),
                "Conversation should open"
        );

        for (String testMessage : messages) {

            conversationPage.sendMessage(testMessage);

            conversationPage.waitForMessageToAppear(testMessage);

            Assert.assertTrue(
                    conversationPage.isMessageDisplayed(testMessage),
                    "Message should display: " + testMessage
            );

            logger.info("Sent scenario message: "
                    + testMessage);
        }

        conversationPage.navigateBack();

        logger.info("Scenario completed: " + scenarioName);
    }

    @Test(priority = 10, groups = {"messaging", "regression"}, description = "Verify send message functionality")
    public void testSendMessage() {
        logger.info("Starting send message test");

        MessagePage messagePage = homePage.navigateToMessages();

        if (messagePage.hasConversations()) {
            String firstConversationId = messagePage.getFirstConversationId();
            Assert.assertNotNull(
                    firstConversationId,
                    "First conversation id should not be null"
            );
            ConversationPage conversationPage = messagePage.selectConversation(firstConversationId);

            String testMessage =
                    testDataReader.getRandomMessageTemplate("greeting");

            conversationPage.sendMessage(testMessage);

            conversationPage.waitForMessageToAppear(testMessage);

            Assert.assertTrue(
                    conversationPage.isMessageDisplayed(testMessage),
                    "Message should be displayed"
            );
            Assert.assertTrue(conversationPage.isMessageDisplayed(testMessage),
                    "Sent message should be displayed in conversation");
        } else {
            logger.info("No conversations available for testing send message");
        }

        logger.info("Send message test completed successfully");
    }

    @Test(priority = 11, groups = {"messaging", "regression"}, description = "Verify empty message handling")
    public void testEmptyMessageHandling() {
        logger.info("Starting empty message handling test");

        MessagePage messagePage = homePage.navigateToMessages();

        if (messagePage.hasConversations()) {
            String firstConversationId = messagePage.getFirstConversationId();
            Assert.assertNotNull(
                    firstConversationId,
                    "First conversation id should not be null"
            );
            ConversationPage conversationPage = messagePage.selectConversation(firstConversationId);

            conversationPage.enterMessage(
                    testDataReader.getTestData("emptyMessage"));
            Assert.assertFalse(conversationPage.isSendButtonEnabled(),
                    "Send button should be disabled for empty message");

            conversationPage.enterMessage(
                    testDataReader.getTestData("whitespaceMessage"));
            Assert.assertFalse(conversationPage.isSendButtonEnabled(),
                    "Send button should be disabled for whitespace-only message");

            conversationPage.enterMessage("Valid message");
            Assert.assertTrue(conversationPage.isSendButtonEnabled(),
                    "Send button should be enabled for valid message");
        } else {
            logger.info("No conversations available for testing empty message handling");
        }

        logger.info("Empty message handling test completed successfully");
    }

    @Test(priority = 12, groups = {"messaging", "regression"}, description = "Verify long message handling")
    public void testLongMessageHandling() {
        logger.info("Starting long message handling test");

        MessagePage messagePage = homePage.navigateToMessages();

        if (messagePage.hasConversations()) {
            String firstConversationId = messagePage.getFirstConversationId();
            Assert.assertNotNull(
                    firstConversationId,
                    "First conversation id should not be null"
            );
            ConversationPage conversationPage = messagePage.selectConversation(firstConversationId);

            String longMessage =
                    testDataReader.getTestData("longMessage");

            conversationPage.enterMessage(longMessage);

            if (conversationPage.isSendButtonEnabled()) {
                conversationPage.clickSendButton();

                conversationPage.waitForMessageToAppear(longMessage);

                Assert.assertTrue(
                        conversationPage.isMessageDisplayed(longMessage)
                );
            } else {
                logger.info("Long message was rejected by input validation");
            }
        } else {
            logger.info("No conversations available for testing long message handling");
        }

        logger.info("Long message handling test completed successfully");
    }

    @Test(priority = 13, groups = {"messaging", "regression"}, description = "Verify message list scrolling")
    public void testMessageListScrolling() {
        logger.info("Starting message list scrolling test");

        MessagePage messagePage = homePage.navigateToMessages();

        if (messagePage.hasConversations()) {
            String firstConversationId = messagePage.getFirstConversationId();
            Assert.assertNotNull(
                    firstConversationId,
                    "First conversation id should not be null"
            );
            ConversationPage conversationPage = messagePage.selectConversation(firstConversationId);

            if (conversationPage.hasMessages() && conversationPage.getMessageCount() > 5) {
                conversationPage.scrollToTop();
                Assert.assertTrue(conversationPage.isMessageListDisplayed(),
                        "Message list should still be displayed after scrolling");

                conversationPage.scrollToBottom();
                Assert.assertTrue(conversationPage.isMessageListDisplayed(),
                        "Message list should still be displayed after scrolling");
            } else {
                logger.info("Not enough messages for scrolling test");
            }
        } else {
            logger.info("No conversations available for testing message list scrolling");
        }

        logger.info("Message list scrolling test completed successfully");
    }

    @Test(priority = 14, groups = {"messaging", "regression"}, description = "Verify message input responsiveness")
    public void testMessageInputResponsiveness() {
        logger.info("Starting message input responsiveness test");

        MessagePage messagePage = homePage.navigateToMessages();

        if (messagePage.hasConversations()) {
            String firstConversationId = messagePage.getFirstConversationId();
            Assert.assertNotNull(
                    firstConversationId,
                    "First conversation id should not be null"
            );
            ConversationPage conversationPage = messagePage.selectConversation(firstConversationId);

            List<String> rapidMessages =
                    testDataReader.getRapidMessages();

            for (String message : rapidMessages) {
                conversationPage.enterMessage(message);
                Assert.assertEquals(conversationPage.getMessageInputText(), message,
                        "Message input should reflect typed text accurately");
                conversationPage.clearMessageInput();
            }

            conversationPage.enterMessage("Test message to clear");
            conversationPage.clearMessageInput();
            Assert.assertTrue(conversationPage.getMessageInputText().isEmpty(),
                    "Message input should be empty after clearing");

        } else {
            logger.info("No conversations available for testing message input responsiveness");
        }

        logger.info("Message input responsiveness test completed successfully");
    }

    @Test(priority = 15, groups = {"messaging", "regression"}, description = "Verify conversation refresh functionality")
    public void testConversationRefresh() {
        logger.info("Starting conversation refresh test");

        MessagePage messagePage = homePage.navigateToMessages();

        if (messagePage.hasConversations()) {
            String firstConversationId = messagePage.getFirstConversationId();
            Assert.assertNotNull(
                    firstConversationId,
                    "First conversation id should not be null"
            );
            ConversationPage conversationPage = messagePage.selectConversation(firstConversationId);

            int initialMessageCount = conversationPage.getMessageCount();

            conversationPage.refreshConversation();

            Assert.assertTrue(conversationPage.isDisplayed(),
                    "Conversation page should be displayed after refresh");
            Assert.assertTrue(conversationPage.verifyPageElements(),
                    "Conversation page elements should be present after refresh");

            Assert.assertTrue(conversationPage.getMessageCount() >= initialMessageCount,
                    "Message count should not decrease after refresh");

        } else {
            logger.info("No conversations available for testing conversation refresh");
        }

        logger.info("Conversation refresh test completed successfully");
    }

    @Test(priority = 16, groups = {"messaging", "regression"}, description = "Verify sending multiple message types")
    public void testSendMultipleMessageTypes() {

        logger.info("Starting multiple message types test");

        MessagePage messagePage =
                homePage.navigateToMessages();

        if (messagePage.hasConversations()) {

            String firstConversationId =
                    messagePage.getFirstConversationId();
            Assert.assertNotNull(
                    firstConversationId,
                    "First conversation id should not be null"
            );
            ConversationPage conversationPage =
                    messagePage.selectConversation(firstConversationId);

            List<String> testMessages = Arrays.asList(

                    testDataReader.getRandomMessageTemplate("greeting"),

                    testDataReader.getRandomMessageTemplate("question"),

                    testDataReader.getRandomMessageTemplate("emoji"),

                    testDataReader.getTestData("vietnameseMessage"),

                    testDataReader.getTestData("unicodeMessage")
            );

            for (String message : testMessages) {

                conversationPage.sendMessage(message);

                conversationPage.waitForMessageToAppear(message);

                Assert.assertTrue(
                        conversationPage.isMessageDisplayed(message),
                        "Message should be displayed: " + message
                );

                logger.info("Sent message: " + message);
            }

        } else {
            logger.info("No conversations available");
        }

        logger.info("Multiple message types test completed");
    }

    @Test(priority = 17, groups = {"messaging", "regression"}, description = "Verify message sending error handling")
    public void testMessageSendingErrorHandling() {
        logger.info("Starting message sending error handling test");

        MessagePage messagePage = homePage.navigateToMessages();

        if (messagePage.hasConversations()) {
            String firstConversationId = messagePage.getFirstConversationId();
            Assert.assertNotNull(
                    firstConversationId,
                    "First conversation id should not be null"
            );
            ConversationPage conversationPage = messagePage.selectConversation(firstConversationId);

            backgroundApp(2);

            String testMessage = "Test message during network issue " + System.currentTimeMillis();
            conversationPage.sendMessage(testMessage);

            Assert.assertTrue(conversationPage.isDisplayed(),
                    "Conversation page should remain stable during network issues");

        } else {
            logger.info("No conversations available for testing message sending error handling");
        }

        logger.info("Message sending error handling test completed successfully");
    }

    @Test(priority = 18, groups = {"messaging", "e2e"}, description = "Verify messaging between tu and tune")
    public void testSendMessageBetweenTuAndTune() {

        String newMessage =
                testDataReader.getTestData("vietnameseMessage");

        MessagePage messagePage =
                homePage.navigateToMessages();

        ConversationPage conversationPage =
                messagePage.openConversation("tune");

        conversationPage.sendMessage(newMessage);

        conversationPage.waitForMessageToAppear(newMessage);

        Assert.assertTrue(
                conversationPage.isMessageDisplayed(newMessage),
                "Message should be displayed"
        );

        logger.info("Message sent between tu and tune: "
                + newMessage);
    }
}
