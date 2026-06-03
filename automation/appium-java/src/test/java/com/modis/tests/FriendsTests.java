package com.modis.tests;

import com.modis.base.BaseTest;
import com.modis.base.BasePage;
import com.modis.pages.*;
import com.modis.utils.TestDataReader;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.Map;

public class FriendsTests extends BaseTest {
    
    private HomePage homePage;
    private TestDataReader testDataReader = new TestDataReader();

    @BeforeMethod(alwaysRun = true)
    public void loginBeforeTest() {

        logger.info(
                "Logging in before friends test"
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
                "Should be logged in before friends tests"
        );

        logger.info(
                "Logged in successfully with user: {}",
                username
        );
    }
    
    @Test(priority = 1, groups = {"friends", "regression", "data"}, 
          description = "Verify search result items or empty state from current database")
    public void testFriendsWithRealData() {
        logger.info("Testing current search result data");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        
        friendsPage.searchFriends("tu");

        if (friendsPage.hasSearchResults()) {
            String firstResultId = friendsPage.getFirstSearchResultId();

            Assert.assertFalse(firstResultId.isEmpty(),
                "Search result item id should be available");
            Assert.assertTrue(friendsPage.isSearchResultDisplayed(firstResultId),
                "Search result row should be displayed for id: " + firstResultId);
            Assert.assertTrue(friendsPage.isSearchResultNameDisplayed(firstResultId),
                "Search result name should be displayed for id: " + firstResultId);
            Assert.assertTrue(friendsPage.isSearchResultUsernameDisplayed(firstResultId),
                "Search result username should be displayed for id: " + firstResultId);
            Assert.assertFalse(friendsPage.getSearchResultName(firstResultId).trim().isEmpty(),
                "Search result name text should not be empty");
            Assert.assertFalse(friendsPage.getSearchResultUsername(firstResultId).trim().isEmpty(),
                "Search result username text should not be empty");
        } else {
            Assert.assertTrue(friendsPage.isNoSearchResultsDisplayed(),
                "Search empty state should be displayed when current database returns no users");
        }
        
        logger.info("Current search result data test completed");
    }
    
    @Test(priority = 2, groups = {"friends", "regression", "data"}, 
          description = "Verify received request items or empty state from current database")
    public void testFriendRequestsWithRealData() {
        logger.info("Testing current friend request data");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        friendsPage.clickRequestsTab();
        
        if (friendsPage.hasFriendRequests()) {
            String requestId = friendsPage.getFirstFriendRequestId();

            Assert.assertFalse(requestId.isEmpty(),
                "Friend request item id should be available");
            Assert.assertTrue(friendsPage.isFriendRequestDisplayed(requestId),
                "Request row should be displayed for id: " + requestId);
            Assert.assertTrue(friendsPage.isRequestNameDisplayed(requestId),
                "Request name should be displayed for id: " + requestId);
            Assert.assertFalse(friendsPage.getRequestName(requestId).trim().isEmpty(),
                "Request name text should not be empty");
            Assert.assertTrue(friendsPage.isAcceptButtonDisplayed(requestId),
                "Accept button should be displayed for request id: " + requestId);
            Assert.assertTrue(friendsPage.isDeclineButtonDisplayed(requestId),
                "Decline button should be displayed for request id: " + requestId);
        } else {
            Assert.assertTrue(friendsPage.isEmptyRequestsStateDisplayed(),
                "Received request empty state should be displayed when current database has no received requests");
        }
        
        logger.info("Current friend request data test completed");
    }
    
    @Test(priority = 3, groups = {"friends", "smoke"}, 
          description = "Verify navigation to friends screen")
    public void testNavigateToFriends() {
        logger.info("Starting navigate to friends test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        
        Assert.assertTrue(friendsPage.isDisplayed(), "Friends page should be displayed");
        Assert.assertTrue(friendsPage.verifyPageElements(), "Friends page elements should be present");
        
        logger.info("Navigate to friends test completed successfully");
    }
    
    @Test(priority = 2, groups = {"friends", "navigation"}, 
          description = "Verify back navigation from friends screen")
    public void testBackNavigationFromFriends() {
        logger.info("Starting back navigation from friends test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        HomePage returnedHomePage = friendsPage.navigateBack();
        
        Assert.assertTrue(returnedHomePage.isDisplayed(), 
            "Should return to home page after back navigation");
        Assert.assertTrue(returnedHomePage.verifyPageElements(), 
            "Home page elements should be present after back navigation");
        
        logger.info("Back navigation from friends test completed successfully");
    }
    
    @Test(priority = 3, groups = {"friends", "regression"}, 
          description = "Verify friends list display")
    public void testFriendsListDisplay() {
        logger.info("Starting friends list display test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        
        Assert.assertTrue(friendsPage.isFriendsTabSelected(), 
            "Friends tab should be selected by default");
        
        if (friendsPage.hasFriends()) {
            String firstVisibleFriendName = friendsPage.getFirstVisibleFriendName();

            Assert.assertFalse(firstVisibleFriendName.isEmpty(),
                "Visible friend name should not be empty when friends are displayed");
        } else {
            Assert.assertTrue(friendsPage.isEmptyFriendsStateDisplayed(),
                "Friends empty state should be displayed when current database has no friends");
        }
        
        logger.info("Friends list display test completed successfully");
    }
    
    @Test(priority = 4, groups = {"friends", "regression"}, 
          description = "Verify friend item elements")
    public void testFriendItemElements() {
        logger.info("Starting friend item elements test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        
        if (friendsPage.hasFriends()) {
            String firstVisibleFriendName = friendsPage.getFirstVisibleFriendName();
            String firstFriendId = friendsPage.getFirstFriendId();

            Assert.assertFalse(firstFriendId.isEmpty(),
                "Friend item id should be available");
            Assert.assertFalse(firstVisibleFriendName.isEmpty(),
                "Friend name should not be empty");
            Assert.assertTrue(friendsPage.isFriendNameDisplayed(firstFriendId),
                "Friend name should be displayed for item id: " + firstFriendId);
            Assert.assertTrue(friendsPage.isFriendUnfriendButtonDisplayed(firstFriendId),
                "Friend action button should be displayed for item id: " + firstFriendId);
        } else {
            Assert.assertTrue(friendsPage.isEmptyFriendsStateDisplayed(),
                "Friends empty state should be displayed when current database has no friends");
        }
        
        logger.info("Friend item elements test completed successfully");
    }
    
    @Test(priority = 5, groups = {"friends", "regression"}, 
          description = "Verify friend requests tab")
    public void testFriendRequestsTab() {
        logger.info("Starting friend requests tab test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        friendsPage.clickRequestsTab();
        
        Assert.assertTrue(friendsPage.isFriendRequestsListDisplayed(),
            "Friend requests section should be displayed after scrolling to it");
        
        if (friendsPage.hasFriendRequests()) {
            Assert.assertFalse(friendsPage.getFirstFriendRequestId().isEmpty(),
                "Friend request item id should be available");
        } else {
            Assert.assertTrue(friendsPage.isEmptyRequestsStateDisplayed(),
                "Friend requests empty state should be displayed when current database has no received requests");
        }
        
        logger.info("Friend requests tab test completed successfully");
    }
    
    @Test(priority = 6, groups = {"friends", "regression"}, 
          description = "Verify friend request item elements")
    public void testFriendRequestItemElements() {
        logger.info("Starting friend request item elements test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        friendsPage.clickRequestsTab();
        
        if (friendsPage.hasFriendRequests()) {
            String firstVisibleRequestName = friendsPage.getFirstVisibleFriendRequestName();
            String firstRequestId = friendsPage.getFirstFriendRequestId();

            Assert.assertFalse(firstRequestId.isEmpty(),
                "Friend request item id should be available");
            Assert.assertFalse(firstVisibleRequestName.isEmpty(),
                "Request name should not be empty");
            Assert.assertTrue(friendsPage.isRequestNameDisplayed(firstRequestId),
                "Request name should be displayed for item id: " + firstRequestId);
            Assert.assertTrue(friendsPage.isAcceptButtonDisplayed(firstRequestId),
                "Accept button should be displayed for item id: " + firstRequestId);
            Assert.assertTrue(friendsPage.isDeclineButtonDisplayed(firstRequestId),
                "Decline button should be displayed for item id: " + firstRequestId);
        } else {
            Assert.assertTrue(friendsPage.isEmptyRequestsStateDisplayed(),
                "Friend requests empty state should be displayed when current database has no received requests");
        }
        
        logger.info("Friend request item elements test completed successfully");
    }
    
    @Test(priority = 7, groups = {"friends", "regression"}, 
          description = "Verify accept friend request")
    public void testAcceptFriendRequest() {
        logger.info("Starting accept friend request test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        friendsPage.clickRequestsTab();
        
        if (!friendsPage.hasFriendRequests()) {
            Assert.assertTrue(friendsPage.isEmptyRequestsStateDisplayed(),
                "Friend requests empty state should be displayed when there is no request to accept");
            return;
        }

        String firstRequestId = friendsPage.getFirstFriendRequestId();
        String requestName = friendsPage.getRequestName(firstRequestId);
        int initialRequestsCount = friendsPage.getFriendRequestsCount();

        Assert.assertFalse(firstRequestId.isEmpty(),
            "Friend request item id should be available before accepting");
        Assert.assertFalse(requestName.isEmpty(),
            "Friend request name should be available before accepting");
        Assert.assertTrue(friendsPage.isAcceptButtonDisplayed(firstRequestId),
            "Accept button should be visible before accepting request");

        friendsPage.acceptFriendRequest(firstRequestId);
        friendsPage.waitForRequestToDisappear(firstRequestId);

        Assert.assertTrue(friendsPage.getFriendRequestsCount() < initialRequestsCount,
            "Friend requests count should decrease after accepting");
        
        logger.info("Accept friend request test completed successfully");
    }
    
    @Test(priority = 8, groups = {"friends", "regression"}, 
          description = "Verify decline friend request")
    public void testDeclineFriendRequest() {
        logger.info("Starting decline friend request test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        friendsPage.clickRequestsTab();
        
        if (!friendsPage.hasFriendRequests()) {
            Assert.assertTrue(friendsPage.isEmptyRequestsStateDisplayed(),
                "Friend requests empty state should be displayed when there is no request to decline");
            return;
        }

        String firstRequestId = friendsPage.getFirstFriendRequestId();
        String requestName = friendsPage.getRequestName(firstRequestId);
        int initialRequestsCount = friendsPage.getFriendRequestsCount();

        Assert.assertFalse(firstRequestId.isEmpty(),
            "Friend request item id should be available before declining");
        Assert.assertFalse(requestName.isEmpty(),
            "Friend request name should be available before declining");
        Assert.assertTrue(friendsPage.isDeclineButtonDisplayed(firstRequestId),
            "Decline button should be visible before declining request");

        friendsPage.declineFriendRequest(firstRequestId);
        friendsPage.waitForRequestToDisappear(firstRequestId);

        Assert.assertTrue(friendsPage.getFriendRequestsCount() < initialRequestsCount,
            "Friend requests count should decrease after declining");
        
        logger.info("Decline friend request test completed successfully");
    }
    
    @Test(priority = 9, groups = {"friends", "regression"}, 
          description = "Verify sent requests tab")
    public void testSentRequestsTab() {
        logger.info("Starting sent requests tab test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        friendsPage.clickSentTab();
        
        Assert.assertTrue(friendsPage.isSentRequestsListDisplayed(),
            "Sent requests section should be displayed after scrolling to it");
        
        if (friendsPage.hasSentRequests()) {
            String firstSentRequestId = friendsPage.getFirstSentRequestId();
            String firstSentRequestName = friendsPage.getFirstVisibleSentRequestName();

            Assert.assertFalse(firstSentRequestId.isEmpty(),
                "Sent request item id should be available");
            Assert.assertFalse(firstSentRequestName.isEmpty(),
                "Sent request name should not be empty");
            Assert.assertTrue(friendsPage.isSentRequestNameDisplayed(firstSentRequestId),
                "Sent request name should be displayed for item id: " + firstSentRequestId);
            Assert.assertTrue(friendsPage.isSentRequestCancelButtonDisplayed(firstSentRequestId),
                "Sent request cancel button should be displayed for item id: " + firstSentRequestId);
        } else {
            Assert.assertTrue(friendsPage.isEmptySentStateDisplayed(),
                "Sent requests empty state should be displayed when current database has no sent requests");
        }
        
        logger.info("Sent requests tab test completed successfully");
    }

    @Test(priority = 10, groups = {"friends", "regression"},
          description = "Verify add friend from search results")
    public void testAddFriendFromSearch() {
        logger.info("Starting add friend from search test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        
        String searchTerm = "test";
        friendsPage.searchFriends(searchTerm);
        
        if (!friendsPage.hasSearchResults()) {
            Assert.assertTrue(friendsPage.isNoSearchResultsDisplayed(),
                "Search empty state should be displayed when current database returns no addable users");
            return;
        }

        String firstResultId = friendsPage.getFirstAddableSearchResultId();

        if (firstResultId.isEmpty()) {
            String visibleResultId = friendsPage.getFirstSearchResultId();

            Assert.assertFalse(visibleResultId.isEmpty(),
                "Search result item id should be available when results are displayed");
            Assert.assertTrue(friendsPage.isSearchResultDisplayed(visibleResultId),
                "Search result row should be displayed for id: " + visibleResultId);
            Assert.assertFalse(friendsPage.getSearchResultStatus(visibleResultId).trim().isEmpty(),
                "Search result status should explain why the user is not addable");
            return;
        }

        Assert.assertFalse(firstResultId.isEmpty(),
            "Search result item id should be available");
        Assert.assertTrue(friendsPage.canAddFriend(firstResultId),
            "First search result should be addable");

        friendsPage.addFriend(firstResultId);

        Assert.assertTrue(friendsPage.isFriendRequestSent(firstResultId),
            "Friend request should be marked as sent");
        
        logger.info("Add friend from search test completed successfully");
    }
    
    @Test(priority = 11, groups = {"friends", "regression"},
          description = "Verify friends list scrolling")
    public void testFriendsListScrolling() {
        logger.info("Starting friends list scrolling test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        
        if (!friendsPage.hasFriends()) {
            Assert.assertTrue(friendsPage.isEmptyFriendsStateDisplayed(),
                "Friends empty state should be displayed when current database has no friends");
            return;
        }

        friendsPage.scrollDown();
        Assert.assertTrue(friendsPage.isFriendsListDisplayed() || !friendsPage.getFirstVisibleFriendName().isEmpty(),
            "Friends list should still be displayed after scrolling down");

        friendsPage.scrollUp();
        Assert.assertTrue(friendsPage.isFriendsListDisplayed() || !friendsPage.getFirstVisibleFriendName().isEmpty(),
            "Friends list should still be displayed after scrolling up");

        friendsPage.scrollToTop();
        Assert.assertTrue(friendsPage.isFriendsListDisplayed() || !friendsPage.getFirstVisibleFriendName().isEmpty(),
            "Friends list should still be displayed after scrolling to top");
        
        logger.info("Friends list scrolling test completed successfully");
    }
    
    @Test(priority = 12, groups = {"friends", "regression"},
          description = "Verify friends list refresh")
    public void testFriendsListRefresh() {
        logger.info("Starting friends list refresh test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        
        int initialFriendsCount = friendsPage.getFriendsCount();
        
        friendsPage.refreshFriendsList();
        
        Assert.assertTrue(friendsPage.isDisplayed(), 
            "Friends page should be displayed after refresh");
        Assert.assertTrue(friendsPage.verifyPageElements(), 
            "Friends page elements should be present after refresh");
        
        int newFriendsCount = friendsPage.getFriendsCount();
        logger.info("Friends count before refresh: {}, after refresh: {}", 
                   initialFriendsCount, newFriendsCount);
        
        logger.info("Friends list refresh test completed successfully");
    }
    
    @Test(priority = 13, groups = {"friends", "regression"},
          description = "Verify network error handling")
    public void testNetworkErrorHandling() {
        logger.info("Starting network error handling test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        
        backgroundApp(3);
        
        friendsPage.refreshFriendsList();
        
        Assert.assertTrue(friendsPage.isDisplayed(), 
            "Friends page should remain stable during network issues");
        
        if (friendsPage.hasFriends()) {
            Assert.assertFalse(friendsPage.getFirstFriendId().isEmpty(),
                "Friend item id should still be available after returning from background");
        } else {
            Assert.assertTrue(friendsPage.isEmptyFriendsStateDisplayed(),
                "Friends empty state should still be displayed after returning from background");
        }

        friendsPage.searchFriends("test");
        Assert.assertTrue(friendsPage.isDisplayed(),
            "Friends page should remain stable during search with network issues");
        
        logger.info("Network error handling test completed successfully");
    }
}
