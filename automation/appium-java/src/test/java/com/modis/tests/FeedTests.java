package com.modis.tests;

import com.modis.base.BaseTest;
import com.modis.pages.ConversationPage;
import com.modis.pages.HomePage;
import com.modis.utils.TestDataReader;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FeedTests extends BaseTest {

    private HomePage homePage;
    private final TestDataReader testDataReader = new TestDataReader();

    @BeforeMethod(alwaysRun = true)
    public void loginBeforeTest() {
        homePage = TestSessionHelper.loginAsDefaultUser(testDataReader);
    }

    @Test(priority = 1, groups = {"feed", "smoke", "regression"}, description = "View friend feed")
    public void testViewFriendFeed() {
        homePage.navigateToFeed();

        Assert.assertTrue(homePage.isDisplayed(), "Home screen should be displayed");
        Assert.assertTrue(homePage.isFeedDisplayed(), "Feed should be displayed on home screen");
    }

    @Test(priority = 2, groups = {"feed", "regression"}, description = "Refresh feed")
    public void testRefreshFeed() {
        homePage.navigateToFeed();
        homePage.refreshFeed();

        Assert.assertTrue(homePage.isDisplayed(), "Home screen should remain displayed after refresh");
        Assert.assertTrue(homePage.isFeedDisplayed(), "Feed should remain displayed after refresh");
    }

    @Test(priority = 3, groups = {"feed", "regression"}, description = "Scroll feed")
    public void testScrollFeed() {
        homePage.navigateToFeed();
        homePage.scrollFeedDown();
        Assert.assertTrue(homePage.isFeedDisplayed(), "Feed should remain displayed after scrolling down");

        homePage.scrollFeedUp();
        Assert.assertTrue(homePage.isFeedDisplayed(), "Feed should remain displayed after scrolling up");
    }

    @Test(priority = 4, groups = {"feed", "regression"}, description = "Open a feed post")
    public void testOpenFeedPost() {
        homePage.navigateToFeed();
        String postId = homePage.getFirstVisibleFeedPostId();

        Assert.assertFalse(postId.isEmpty(), "Visible feed post id should be available");
        Assert.assertTrue(homePage.isFeedPostDisplayed(postId), "Visible feed post should be displayed: " + postId);

        homePage.clickFeedPost(postId);

        Assert.assertTrue(homePage.isDisplayed(), "App should stay in a valid home/feed state after opening post");
    }

    @Test(priority = 5, groups = {"feed", "regression"}, description = "Open a feed photo")
    public void testOpenFeedPhoto() {
        homePage.navigateToFeed();
        String postId = homePage.getFirstVisibleFeedPostImageId();

        Assert.assertFalse(postId.isEmpty(), "Visible feed post image id should be available");
        Assert.assertTrue(homePage.isFeedPostImageDisplayed(postId), "Visible feed post image should be displayed: " + postId);

        homePage.clickFeedPostImage(postId);

        Assert.assertTrue(homePage.isDisplayed(), "App should stay in a valid home/feed state after opening photo");
    }

    @Test(priority = 6, groups = {"feed", "regression"}, description = "React to feed post with emoji")
    public void testReactToFeedPostWithEmoji() {
        homePage.navigateToFeed();
        String emojiButtonId = homePage.findFirstFeedEmojiButtonId(30);

        if (emojiButtonId.isEmpty()) {
            Assert.assertTrue(homePage.isFeedDisplayed(), "Feed should be displayed when no reactable friend post is available");
            return;
        }

        homePage.clickEmojiReactionById(emojiButtonId);

        Assert.assertTrue(homePage.isDisplayed(), "Home/feed should remain stable after emoji reaction");
    }

    @Test(priority = 7, groups = {"feed", "regression"}, description = "Open comments for feed post")
    public void testOpenFeedPostComments() {
        homePage.navigateToFeed();
        String postId = homePage.findFirstFeedPostWithCommentButton(30);

        if (postId.isEmpty()) {
            Assert.assertTrue(homePage.isFeedDisplayed(), "Feed should be displayed when no friend post is available for comments");
            return;
        }

        Assert.assertTrue(homePage.isFeedCommentButtonDisplayed(postId), "Comment button should be displayed for post: " + postId);

        homePage.clickCommentButton(postId);

        Assert.assertTrue(homePage.isDisplayed(), "Home/feed should remain stable after opening comments");
    }

    @Test(priority = 8, groups = {"feed", "messaging", "regression"}, description = "Send image comment from friend feed post")
    public void testSendImageCommentFromFriendFeedPost() {
        homePage.navigateToFeed();
        String postId = homePage.findFirstFeedPostWithCommentButton(30);

        if (postId.isEmpty()) {
            Assert.assertTrue(homePage.isFeedDisplayed(), "Feed should be displayed when no friend post is available for image comment");
            return;
        }

        String comment = "Auto image comment " + System.currentTimeMillis();

        Assert.assertTrue(homePage.isFeedCommentButtonDisplayed(postId), "Comment button should be displayed for friend post: " + postId);

        homePage.clickCommentButton(postId);
        ConversationPage conversationPage = homePage.sendFeedComment(comment);
        conversationPage.waitForPageToLoad();
        conversationPage.waitForMessageToAppear(comment);

        Assert.assertTrue(conversationPage.hasVisibleSentImageMessage(), "Sent feed comment should include the commented post image");
    }

}
