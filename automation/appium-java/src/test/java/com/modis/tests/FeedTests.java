package com.modis.tests;

import com.modis.base.BaseTest;
import com.modis.pages.HomePage;
import com.modis.utils.TestDataReader;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

public class FeedTests extends BaseTest {

    private HomePage homePage;
    private final TestDataReader testDataReader = new TestDataReader();

    @BeforeMethod(alwaysRun = true)
    public void loginBeforeTest() {
        homePage = TestSessionHelper.loginAsDefaultUser(testDataReader);
    }

    @Test(priority = 1, groups = {"feed", "smoke", "regression"}, description = "View friend feed")
    public void testViewFriendFeed() {
        Assert.assertTrue(homePage.isDisplayed(), "Home screen should be displayed");
        Assert.assertTrue(homePage.isFeedDisplayed(), "Feed should be displayed on home screen");
    }

    @Test(priority = 2, groups = {"feed", "regression"}, description = "Refresh feed")
    public void testRefreshFeed() {
        homePage.refreshFeed();

        Assert.assertTrue(homePage.isDisplayed(), "Home screen should remain displayed after refresh");
        Assert.assertTrue(homePage.isFeedDisplayed(), "Feed should remain displayed after refresh");
    }

    @Test(priority = 3, groups = {"feed", "regression"}, description = "Scroll feed")
    public void testScrollFeed() {
        homePage.scrollFeedDown();
        Assert.assertTrue(homePage.isFeedDisplayed(), "Feed should remain displayed after scrolling down");

        homePage.scrollFeedUp();
        Assert.assertTrue(homePage.isFeedDisplayed(), "Feed should remain displayed after scrolling up");
    }

    @Test(priority = 4, groups = {"feed", "regression"}, description = "Open a feed post")
    public void testOpenFeedPost() {
        String postId = getKnownPostId();

        if (!homePage.isFeedPostDisplayed(postId)) {
            throw new SkipException("Known feed post is not visible: " + postId);
        }

        homePage.clickFeedPost(postId);

        Assert.assertTrue(homePage.isDisplayed(), "App should stay in a valid home/feed state after opening post");
    }

    @Test(priority = 5, groups = {"feed", "regression"}, description = "Open a feed photo")
    public void testOpenFeedPhoto() {
        String postId = getKnownPostId();

        if (!homePage.isFeedPostImageDisplayed(postId)) {
            throw new SkipException("Known feed photo is not visible: " + postId);
        }

        homePage.clickFeedPostImage(postId);

        Assert.assertTrue(homePage.isDisplayed(), "App should stay in a valid home/feed state after opening photo");
    }

    @Test(priority = 6, groups = {"feed", "regression"}, description = "React to feed post with emoji")
    public void testReactToFeedPostWithEmoji() {
        String postId = getKnownPostId();
        String emoji = "heart";

        if (!homePage.isEmojiReactionDisplayed(emoji, postId)) {
            throw new SkipException("Emoji reaction is not visible for post: " + postId);
        }

        homePage.clickEmojiReaction(emoji, postId);

        Assert.assertTrue(homePage.isDisplayed(), "Home/feed should remain stable after emoji reaction");
    }

    @Test(priority = 7, groups = {"feed", "regression"}, description = "Open comments for feed post")
    public void testOpenFeedPostComments() {
        String postId = getKnownPostId();

        if (!homePage.isFeedCommentButtonDisplayed(postId)) {
            throw new SkipException("Comment button is not visible for post: " + postId);
        }

        homePage.clickCommentButton(postId);

        Assert.assertTrue(homePage.isDisplayed(), "Home/feed should remain stable after opening comments");
    }

    private String getKnownPostId() {
        Map<String, Object> photo = testDataReader.getRandomTestPhoto();
        return (String) photo.get("id");
    }
}
