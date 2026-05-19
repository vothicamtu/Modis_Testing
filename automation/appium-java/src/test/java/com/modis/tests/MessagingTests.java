package com.modis.tests;

import com.modis.base.BaseTest;
import com.modis.constants.AppConstants;
import com.modis.pages.*;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for Messaging functionality
 * Covers message list, conversations, and messaging features
 */
public class MessagingTests extends BaseTest {
    
    private HomePage homePage;
    
    @BeforeMethod(groups = {"messaging", "regression"})
    public void loginBeforeTest() {
        logger.info("Logging in before messaging test");
        
        LoadingPage loadingPage = new LoadingPage();
        LoginPage loginPage = loadingPage.clickLoginButton();
        homePage = (HomePage) loginPage.loginWithTestUser();
        
        Assert.assertTrue(homePage.isDisplayed(), "Should be logged in before messaging tests");
    }
    
    // ==================== MESSAGE LIST TESTS ====================
    
    @Test(priority = 1, groups = {"messaging", "smoke"}, 
          description = "Verify navigation to message screen")
    public void testNavigateToMessages() {
        logger.info("Starting navigate to messages test");
        
        MessagePage messagePage = homePage.navigateToMessages();
        
        Assert.assertTrue(messagePage.isDisplayed(), "Message page should be displayed");
        Assert.assertTrue(messagePage.verifyPageElements(), "Message page elements should be present");
        
        logger.info("Navigate to messages test completed successfully");
    }
    
    @Test(priority = 2, groups = {"messaging", "regression"}, 
          description = "Verify message list display")
    public void testMessageListDisplay() {
        logger.info("Starting message list display test");
        
        MessagePage messagePage = homePage.navigateToMessages();
        
        if (messagePage.hasConversations()) {
            Assert.assertTrue(messagePage.isConversationListDisplayed(), 
                "Conversation list should be displayed when conversations exist");
            Assert.assertTrue(messagePage.getConversationCount() > 0, 
                "Should have at least one conversation");
        } else {
            Assert.assertTrue(messagePage.isEmptyStateDisplayed(), 
                "Empty state should be displayed when no conversations exist");
        }
        
        logger.info("Message list display test completed successfully");
    }
    
    @Test(priority = 3, groups = {"messaging", "regression"}, 
          description = "Verify conversation item elements")
    public void testConversationItemElements() {
        logger.info("Starting conversation item elements test");
        
        MessagePage messagePage = homePage.navigateToMessages();
        
        if (messagePage.hasConversations()) {
            String firstConversationId = messagePage.getFirstConversationId();
            
            Assert.assertTrue(messagePage.isConversationAvatarDisplayed(firstConversationId), 
                "Conversation avatar should be displayed");
            Assert.assertTrue(messagePage.isConversationNameDisplayed(firstConversationId), 
                "Conversation name should be displayed");
            Assert.assertFalse(messagePage.getConversationName(firstConversationId).isEmpty(), 
                "Conversation name should not be empty");
            
            if (messagePage.hasLastMessage(firstConversationId)) {
                Assert.assertTrue(messagePage.isLastMessageDisplayed(firstConversationId), 
                    "Last message should be displayed");
                Assert.assertTrue(messagePage.isMessageTimeDisplayed(firstConversationId), 
                    "Message time should be displayed");
            }
        } else {
            logger.info("No conversations available for testing conversation item elements");
        }
        
        logger.info("Conversation item elements test completed successfully");
    }
    
    @Test(priority = 4, groups = {"messaging", "regression"}, 
          description = "Verify conversation selection")
    public void testConversationSelection() {
        logger.info("Starting conversation selection test");
        
        MessagePage messagePage = homePage.navigateToMessages();
        
        if (messagePage.hasConversations()) {
            String firstConversationId = messagePage.getFirstConversationId();
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
    
    // ==================== CONVERSATION TESTS ====================
    
    @Test(priority = 5, groups = {"messaging", "regression"}, 
          description = "Verify conversation page elements")
    public void testConversationPageElements() {
        logger.info("Starting conversation page elements test");
        
        MessagePage messagePage = homePage.navigateToMessages();
        
        if (messagePage.hasConversations()) {
            String firstConversationId = messagePage.getFirstConversationId();
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
    
    @Test(priority = 6, groups = {"messaging", "regression"}, 
          description = "Verify send message functionality")
    public void testSendMessage() {
        logger.info("Starting send message test");
        
        MessagePage messagePage = homePage.navigateToMessages();
        
        if (messagePage.hasConversations()) {
            String firstConversationId = messagePage.getFirstConversationId();
            ConversationPage conversationPage = messagePage.selectConversation(firstConversationId);
            
            String testMessage = "Test message " + System.currentTimeMillis();
            int initialMessageCount = conversationPage.getMessageCount();
            
            conversationPage.sendMessage(testMessage);
            
            // Wait for message to be sent and verify
            conversationPage.waitForMessageToAppear(testMessage);
            
            Assert.assertTrue(conversationPage.getMessageCount() > initialMessageCount, 
                "Message count should increase after sending message");
            Assert.assertTrue(conversationPage.isMessageDisplayed(testMessage), 
                "Sent message should be displayed in conversation");
        } else {
            logger.info("No conversations available for testing send message");
        }
        
        logger.info("Send message test completed successfully");
    }
    
    @Test(priority = 7, groups = {"messaging", "regression"}, 
          description = "Verify empty message handling")
    public void testEmptyMessageHandling() {
        logger.info("Starting empty message handling test");
        
        MessagePage messagePage = homePage.navigateToMessages();
        
        if (messagePage.hasConversations()) {
            String firstConversationId = messagePage.getFirstConversationId();
            ConversationPage conversationPage = messagePage.selectConversation(firstConversationId);
            
            // Try to send empty message
            conversationPage.enterMessage("");
            Assert.assertFalse(conversationPage.isSendButtonEnabled(), 
                "Send button should be disabled for empty message");
            
            // Try to send whitespace only message
            conversationPage.enterMessage("   ");
            Assert.assertFalse(conversationPage.isSendButtonEnabled(), 
                "Send button should be disabled for whitespace-only message");
            
            // Enter valid message
            conversationPage.enterMessage("Valid message");
            Assert.assertTrue(conversationPage.isSendButtonEnabled(), 
                "Send button should be enabled for valid message");
        } else {
            logger.info("No conversations available for testing empty message handling");
        }
        
        logger.info("Empty message handling test completed successfully");
    }
    
    @Test(priority = 8, groups = {"messaging", "regression"}, 
          description = "Verify long message handling")
    public void testLongMessageHandling() {
        logger.info("Starting long message handling test");
        
        MessagePage messagePage = homePage.navigateToMessages();
        
        if (messagePage.hasConversations()) {
            String firstConversationId = messagePage.getFirstConversationId();
            ConversationPage conversationPage = messagePage.selectConversation(firstConversationId);
            
            // Create a long message
            String longMessage = "This is a very long message that exceeds normal length limits. ".repeat(10);
            
            conversationPage.enterMessage(longMessage);
            
            if (conversationPage.isSendButtonEnabled()) {
                int initialMessageCount = conversationPage.getMessageCount();
                conversationPage.clickSendButton();
                
                // Verify message was sent (may be truncated)
                Assert.assertTrue(conversationPage.getMessageCount() > initialMessageCount, 
                    "Long message should be sent successfully");
            } else {
                logger.info("Long message was rejected by input validation");
            }
        } else {
            logger.info("No conversations available for testing long message handling");
        }
        
        logger.info("Long message handling test completed successfully");
    }
    
    @Test(priority = 9, groups = {"messaging", "regression"}, 
          description = "Verify message list scrolling")
    public void testMessageListScrolling() {
        logger.info("Starting message list scrolling test");
        
        MessagePage messagePage = homePage.navigateToMessages();
        
        if (messagePage.hasConversations()) {
            String firstConversationId = messagePage.getFirstConversationId();
            ConversationPage conversationPage = messagePage.selectConversation(firstConversationId);
            
            if (conversationPage.hasMessages() && conversationPage.getMessageCount() > 5) {
                // Test scrolling up to see older messages
                conversationPage.scrollToTop();
                Assert.assertTrue(conversationPage.isMessageListDisplayed(), 
                    "Message list should still be displayed after scrolling");
                
                // Test scrolling down to see newer messages
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
    
    // ==================== NAVIGATION TESTS ====================
    
    @Test(priority = 10, groups = {"messaging", "navigation"}, 
          description = "Verify back navigation from conversation")
    public void testBackNavigationFromConversation() {
        logger.info("Starting back navigation from conversation test");
        
        MessagePage messagePage = homePage.navigateToMessages();
        
        if (messagePage.hasConversations()) {
            String firstConversationId = messagePage.getFirstConversationId();
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
    
    @Test(priority = 11, groups = {"messaging", "navigation"}, 
          description = "Verify back navigation from message list")
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
    
    // ==================== ERROR HANDLING TESTS ====================
    
    @Test(priority = 12, groups = {"messaging", "regression"}, 
          description = "Verify message sending error handling")
    public void testMessageSendingErrorHandling() {
        logger.info("Starting message sending error handling test");
        
        MessagePage messagePage = homePage.navigateToMessages();
        
        if (messagePage.hasConversations()) {
            String firstConversationId = messagePage.getFirstConversationId();
            ConversationPage conversationPage = messagePage.selectConversation(firstConversationId);
            
            // Simulate network error by putting app in background briefly
            backgroundApp(2);
            
            String testMessage = "Test message during network issue " + System.currentTimeMillis();
            conversationPage.sendMessage(testMessage);
            
            // Check if error handling is working (message might be queued or show error)
            // This test verifies the app doesn't crash during network issues
            Assert.assertTrue(conversationPage.isDisplayed(), 
                "Conversation page should remain stable during network issues");
            
        } else {
            logger.info("No conversations available for testing message sending error handling");
        }
        
        logger.info("Message sending error handling test completed successfully");
    }
    
    // ==================== UI RESPONSIVENESS TESTS ====================
    
    @Test(priority = 13, groups = {"messaging", "regression"}, 
          description = "Verify message input responsiveness")
    public void testMessageInputResponsiveness() {
        logger.info("Starting message input responsiveness test");
        
        MessagePage messagePage = homePage.navigateToMessages();
        
        if (messagePage.hasConversations()) {
            String firstConversationId = messagePage.getFirstConversationId();
            ConversationPage conversationPage = messagePage.selectConversation(firstConversationId);
            
            // Test rapid typing
            String[] rapidMessages = {"Hello", "How are you?", "Test message", "Quick typing test"};
            
            for (String message : rapidMessages) {
                conversationPage.enterMessage(message);
                Assert.assertEquals(conversationPage.getMessageInputText(), message, 
                    "Message input should reflect typed text accurately");
                conversationPage.clearMessageInput();
            }
            
            // Test input field clearing
            conversationPage.enterMessage("Test message to clear");
            conversationPage.clearMessageInput();
            Assert.assertTrue(conversationPage.getMessageInputText().isEmpty(), 
                "Message input should be empty after clearing");
            
        } else {
            logger.info("No conversations available for testing message input responsiveness");
        }
        
        logger.info("Message input responsiveness test completed successfully");
    }
    
    @Test(priority = 14, groups = {"messaging", "regression"}, 
          description = "Verify conversation refresh functionality")
    public void testConversationRefresh() {
        logger.info("Starting conversation refresh test");
        
        MessagePage messagePage = homePage.navigateToMessages();
        
        if (messagePage.hasConversations()) {
            String firstConversationId = messagePage.getFirstConversationId();
            ConversationPage conversationPage = messagePage.selectConversation(firstConversationId);
            
            int initialMessageCount = conversationPage.getMessageCount();
            
            // Perform pull-to-refresh
            conversationPage.refreshConversation();
            
            // Verify conversation is still functional after refresh
            Assert.assertTrue(conversationPage.isDisplayed(), 
                "Conversation page should be displayed after refresh");
            Assert.assertTrue(conversationPage.verifyPageElements(), 
                "Conversation page elements should be present after refresh");
            
            // Message count should be same or more (in case new messages arrived)
            Assert.assertTrue(conversationPage.getMessageCount() >= initialMessageCount, 
                "Message count should not decrease after refresh");
            
        } else {
            logger.info("No conversations available for testing conversation refresh");
        }
        
        logger.info("Conversation refresh test completed successfully");
    }
}