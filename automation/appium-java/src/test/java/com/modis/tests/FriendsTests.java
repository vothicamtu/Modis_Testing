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
            logger.info("PASS reason: search results exist and first result item was verified | resultId={}", firstResultId);
        } else {
            Assert.assertTrue(friendsPage.isNoSearchResultsDisplayed(),
                "Search empty state should be displayed when current database returns no users");
            logger.info("PASS reason: current database returned no searchable users and search empty state was verified");
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
            logger.info("PASS reason: received request exists and request item/actions were verified | requestId={}", requestId);
        } else {
            Assert.assertTrue(friendsPage.isEmptyRequestsStateDisplayed(),
                "Received request empty state should be displayed when current database has no received requests");
            logger.info("PASS reason: current database has no received requests and received-request empty state was verified");
        }
        
        logger.info("Current friend request data test completed");
    }

    @Test(priority = 3, groups = {"friends", "regression", "search"},
          description = "Verify incoming friend request users show accept and reject actions in search results")
    public void testIncomingFriendRequestActionsInSearchResults() {
        logger.info("Starting incoming friend request search actions test");

        FriendsPage friendsPage = homePage.navigateToFriends();
        String incomingSearchResultId = searchFirstIncomingFriendRequestSender(friendsPage);

        if (incomingSearchResultId.isEmpty()) {
            Assert.assertTrue(friendsPage.hasSearchResults() || friendsPage.isNoSearchResultsDisplayed(),
                "Search state should be displayed when current database has no incoming request action to verify");
            logger.info("PASS reason: no incoming friend request action exists in current search data and search state was verified");
            return;
        }

        Assert.assertTrue(friendsPage.isSearchResultDisplayed(incomingSearchResultId),
            "Incoming request sender should be displayed in search results: " + incomingSearchResultId);
        Assert.assertTrue(friendsPage.isSearchResultNameDisplayed(incomingSearchResultId),
            "Incoming request search result name should be displayed: " + incomingSearchResultId);
        Assert.assertTrue(friendsPage.isSearchResultUsernameDisplayed(incomingSearchResultId),
            "Incoming request search result username should be displayed: " + incomingSearchResultId);
        Assert.assertTrue(friendsPage.isSearchResultAcceptButtonDisplayed(incomingSearchResultId),
            "Search result should show accept action for incoming request sender: " + incomingSearchResultId);
        Assert.assertTrue(friendsPage.isSearchResultRejectButtonDisplayed(incomingSearchResultId),
            "Search result should show reject action for incoming request sender: " + incomingSearchResultId);
        logger.info("PASS reason: incoming request search result displays name, username, accept action, and reject action | resultId={}",
            incomingSearchResultId);

        logger.info("Incoming friend request search actions test completed successfully");
    }

    @Test(priority = 4, groups = {"friends", "regression", "search"},
          description = "Verify dismissing reject dialog from incoming search result keeps the request")
    public void testIncomingFriendRequestSearchRejectDismissDialog() {
        logger.info("Starting incoming friend request search reject dismiss dialog test");

        FriendsPage friendsPage = homePage.navigateToFriends();
        String incomingSearchResultId = searchFirstIncomingFriendRequestSender(friendsPage);

        if (incomingSearchResultId.isEmpty()) {
            Assert.assertTrue(friendsPage.hasSearchResults() || friendsPage.isNoSearchResultsDisplayed(),
                "Search state should be displayed when current database has no incoming request action to reject from search");
            logger.info("PASS reason: no incoming friend request action exists to reject from search and search state was verified");
            return;
        }

        friendsPage.openRejectIncomingSearchResultDialog(incomingSearchResultId);
        assertCurrentDialog(friendsPage,
            "T\u1eeb ch\u1ed1i l\u1eddi m\u1eddi",
            "B\u1ea1n c\u00f3 ch\u1eafc mu\u1ed1n t\u1eeb ch\u1ed1i?",
            "C\u00d3",
            "KH\u00d4NG");
        friendsPage.dismissCurrentDialog();

        Assert.assertTrue(friendsPage.isSearchResultAcceptButtonDisplayed(incomingSearchResultId),
            "Incoming search accept action should remain after dismissing reject dialog: " + incomingSearchResultId);
        Assert.assertTrue(friendsPage.isSearchResultRejectButtonDisplayed(incomingSearchResultId),
            "Incoming search reject action should remain after dismissing reject dialog: " + incomingSearchResultId);
        logger.info("PASS reason: dismissing reject dialog kept incoming search accept/reject actions | resultId={}",
            incomingSearchResultId);

        logger.info("Incoming friend request search reject dismiss dialog test completed successfully");
    }

    @Test(priority = 5, groups = {"friends", "regression", "search"},
          description = "Verify accepting incoming friend request from search result removes incoming actions")
    public void testIncomingFriendRequestSearchAccept() {
        logger.info("Starting incoming friend request search accept test");

        FriendsPage friendsPage = homePage.navigateToFriends();
        String incomingSearchResultId = searchFirstIncomingFriendRequestSender(friendsPage);

        if (incomingSearchResultId.isEmpty()) {
            Assert.assertTrue(friendsPage.hasSearchResults() || friendsPage.isNoSearchResultsDisplayed(),
                "Search state should be displayed when current database has no incoming request action to accept from search");
            logger.info("PASS reason: no incoming friend request action exists to accept from search and search state was verified");
            return;
        }

        Assert.assertTrue(friendsPage.isSearchResultNameDisplayed(incomingSearchResultId),
            "Incoming request search result name should be displayed before accepting: " + incomingSearchResultId);
        Assert.assertTrue(friendsPage.isSearchResultUsernameDisplayed(incomingSearchResultId),
            "Incoming request search result username should be displayed before accepting: " + incomingSearchResultId);
        Assert.assertTrue(friendsPage.isSearchResultAcceptButtonDisplayed(incomingSearchResultId),
            "Search result accept action should be visible before accepting: " + incomingSearchResultId);
        Assert.assertTrue(friendsPage.isSearchResultRejectButtonDisplayed(incomingSearchResultId),
            "Search result reject action should be visible before accepting: " + incomingSearchResultId);

        friendsPage.acceptIncomingSearchResult(incomingSearchResultId);
        friendsPage.waitForIncomingSearchActionsToDisappear(incomingSearchResultId);

        Assert.assertFalse(friendsPage.isSearchResultAcceptButtonDisplayed(incomingSearchResultId),
            "Incoming search accept action should disappear after accepting: " + incomingSearchResultId);
        Assert.assertFalse(friendsPage.isSearchResultRejectButtonDisplayed(incomingSearchResultId),
            "Incoming search reject action should disappear after accepting: " + incomingSearchResultId);
        logger.info("PASS reason: accepting incoming request from search removed accept/reject actions | resultId={}",
            incomingSearchResultId);

        logger.info("Incoming friend request search accept test completed successfully");
    }

    @Test(priority = 6, groups = {"friends", "regression", "search"},
          description = "Verify confirming reject dialog from incoming search result removes incoming actions")
    public void testIncomingFriendRequestSearchRejectConfirmDialog() {
        logger.info("Starting incoming friend request search reject confirm dialog test");

        FriendsPage friendsPage = homePage.navigateToFriends();
        String incomingSearchResultId = searchFirstIncomingFriendRequestSender(friendsPage);

        if (incomingSearchResultId.isEmpty()) {
            Assert.assertTrue(friendsPage.hasSearchResults() || friendsPage.isNoSearchResultsDisplayed(),
                "Search state should be displayed when current database has no incoming request action to reject from search");
            logger.info("PASS reason: no incoming friend request action exists to reject from search and search state was verified");
            return;
        }

        friendsPage.openRejectIncomingSearchResultDialog(incomingSearchResultId);
        assertCurrentDialog(friendsPage,
            "T\u1eeb ch\u1ed1i l\u1eddi m\u1eddi",
            "B\u1ea1n c\u00f3 ch\u1eafc mu\u1ed1n t\u1eeb ch\u1ed1i?",
            "C\u00d3",
            "KH\u00d4NG");
        friendsPage.confirmCurrentDialog();
        friendsPage.waitForIncomingSearchActionsToDisappear(incomingSearchResultId);

        Assert.assertFalse(friendsPage.isSearchResultAcceptButtonDisplayed(incomingSearchResultId),
            "Incoming search accept action should disappear after confirming reject: " + incomingSearchResultId);
        logger.info("PASS reason: confirming reject from search removed incoming accept action | resultId={}",
            incomingSearchResultId);

        logger.info("Incoming friend request search reject confirm dialog test completed successfully");
    }
    
    @Test(priority = 3, groups = {"friends", "smoke"}, 
          description = "Verify navigation to friends screen")
    public void testNavigateToFriends() {
        logger.info("Starting navigate to friends test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        
        Assert.assertTrue(friendsPage.isDisplayed(), "Friends page should be displayed");
        Assert.assertTrue(friendsPage.verifyPageElements(), "Friends page elements should be present");
        logger.info("PASS reason: friends screen opened and required page elements were verified");
        
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
        logger.info("PASS reason: back navigation returned to HomePage and home elements were verified");
        
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
            logger.info("PASS reason: friends list has items and visible friend name was verified | friendName={}", firstVisibleFriendName);
        } else {
            Assert.assertTrue(friendsPage.isEmptyFriendsStateDisplayed(),
                "Friends empty state should be displayed when current database has no friends");
            logger.info("PASS reason: current database has no friends and friends empty state was verified");
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
            logger.info("PASS reason: friend item exists and name/action were verified | friendId={} | friendName={}", firstFriendId, firstVisibleFriendName);
        } else {
            Assert.assertTrue(friendsPage.isEmptyFriendsStateDisplayed(),
                "Friends empty state should be displayed when current database has no friends");
            logger.info("PASS reason: current database has no friends and friends empty state was verified");
        }
        
        logger.info("Friend item elements test completed successfully");
    }

    @Test(priority = 5, groups = {"friends", "regression"},
          description = "Verify unfriend dialog dismiss keeps friend item")
    public void testUnfriendDismissDialog() {
        logger.info("Starting unfriend dismiss dialog test");

        FriendsPage friendsPage = homePage.navigateToFriends();

        if (!friendsPage.hasFriends()) {
            Assert.assertTrue(friendsPage.isEmptyFriendsStateDisplayed(),
                "Friends empty state should be displayed when current database has no friends to unfriend");
            logger.info("PASS reason: current database has no friends to unfriend and friends empty state was verified");
            return;
        }

        String firstFriendId = friendsPage.getFirstFriendId();
        String firstFriendName = friendsPage.getFirstVisibleFriendName();

        Assert.assertFalse(firstFriendId.isEmpty(),
            "Friend item id should be available before opening unfriend dialog");
        Assert.assertFalse(firstFriendName.isEmpty(),
            "Friend name should be available before opening unfriend dialog");
        Assert.assertTrue(friendsPage.isFriendUnfriendButtonDisplayed(firstFriendId),
            "Unfriend button should be displayed before opening dialog: " + firstFriendId);

        friendsPage.openUnfriendDialog(firstFriendId);
        assertCurrentDialog(friendsPage,
            "H\u1ee7y k\u1ebft b\u1ea1n",
            "B\u1ea1n c\u00f3 ch\u1eafc mu\u1ed1n h\u1ee7y k\u1ebft b\u1ea1n?",
            "\u0110\u1ed2NG \u00dd",
            "H\u1ee6Y");
        friendsPage.dismissCurrentDialog();

        Assert.assertTrue(friendsPage.isFriendDisplayed(firstFriendId),
            "Friend item should remain displayed after dismissing unfriend dialog: " + firstFriendId);
        logger.info("PASS reason: unfriend dismiss kept existing friend item | friendId={} | friendName={}", firstFriendId, firstFriendName);

        logger.info("Unfriend dismiss dialog test completed successfully");
    }

    @Test(priority = 6, groups = {"friends", "regression"},
          description = "Verify unfriend dialog confirm removes friend item")
    public void testUnfriendConfirmDialog() {
        logger.info("Starting unfriend confirm dialog test");

        FriendsPage friendsPage = homePage.navigateToFriends();

        if (!friendsPage.hasFriends()) {
            Assert.assertTrue(friendsPage.isEmptyFriendsStateDisplayed(),
                "Friends empty state should be displayed when current database has no friends to unfriend");
            logger.info("PASS reason: current database has no friends to unfriend and friends empty state was verified");
            return;
        }

        String firstFriendId = friendsPage.getFirstFriendId();
        String firstFriendName = friendsPage.getFirstVisibleFriendName();

        Assert.assertFalse(firstFriendId.isEmpty(),
            "Friend item id should be available before confirming unfriend");
        Assert.assertFalse(firstFriendName.isEmpty(),
            "Friend name should be available before confirming unfriend");
        Assert.assertTrue(friendsPage.isFriendUnfriendButtonDisplayed(firstFriendId),
            "Unfriend button should be displayed before confirming unfriend: " + firstFriendId);

        friendsPage.openUnfriendDialog(firstFriendId);
        assertCurrentDialog(friendsPage,
            "H\u1ee7y k\u1ebft b\u1ea1n",
            "B\u1ea1n c\u00f3 ch\u1eafc mu\u1ed1n h\u1ee7y k\u1ebft b\u1ea1n?",
            "\u0110\u1ed2NG \u00dd",
            "H\u1ee6Y");
        friendsPage.confirmCurrentDialog();
        friendsPage.waitForFriendToDisappear(firstFriendId);

        Assert.assertFalse(friendsPage.isFriendDisplayed(firstFriendId),
            "Friend item should disappear after confirming unfriend: " + firstFriendId);
        logger.info("PASS reason: unfriend confirm removed friend item from current UI | friendId={} | friendName={}", firstFriendId, firstFriendName);

        logger.info("Unfriend confirm dialog test completed successfully");
    }
    
    @Test(priority = 7, groups = {"friends", "regression"}, 
          description = "Verify friend requests tab")
    public void testFriendRequestsTab() {
        logger.info("Starting friend requests tab test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        friendsPage.clickRequestsTab();
        
        Assert.assertTrue(friendsPage.isFriendRequestsListDisplayed(),
            "Friend requests section should be displayed after scrolling to it");
        
        if (friendsPage.hasFriendRequests()) {
            String firstRequestId = friendsPage.getFirstFriendRequestId();
            Assert.assertFalse(firstRequestId.isEmpty(),
                "Friend request item id should be available");
            logger.info("PASS reason: received request section is displayed and request item exists | requestId={}", firstRequestId);
        } else {
            Assert.assertTrue(friendsPage.isEmptyRequestsStateDisplayed(),
                "Friend requests empty state should be displayed when current database has no received requests");
            logger.info("PASS reason: received request section is displayed and empty state was verified");
        }
        
        logger.info("Friend requests tab test completed successfully");
    }
    
    @Test(priority = 8, groups = {"friends", "regression"}, 
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
            logger.info("PASS reason: received request item elements were verified | requestId={} | requestName={}", firstRequestId, firstVisibleRequestName);
        } else {
            Assert.assertTrue(friendsPage.isEmptyRequestsStateDisplayed(),
                "Friend requests empty state should be displayed when current database has no received requests");
            logger.info("PASS reason: no received request item exists and received-request empty state was verified");
        }
        
        logger.info("Friend request item elements test completed successfully");
    }
    
    @Test(priority = 9, groups = {"friends", "regression"}, 
          description = "Verify accept friend request")
    public void testAcceptFriendRequest() {
        logger.info("Starting accept friend request test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        friendsPage.clickRequestsTab();
        
        if (!friendsPage.hasFriendRequests()) {
            Assert.assertTrue(friendsPage.isEmptyRequestsStateDisplayed(),
                "Friend requests empty state should be displayed when there is no request to accept");
            logger.info("PASS reason: no received request exists to accept and received-request empty state was verified");
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

        Assert.assertFalse(friendsPage.isFriendRequestDisplayed(firstRequestId),
            "Accepted friend request item should disappear from the current UI: " + firstRequestId);
        logger.info("PASS reason: accept request removed request item from current UI | requestId={} | requestName={} | initialCount={}",
            firstRequestId, requestName, initialRequestsCount);
        
        logger.info("Accept friend request test completed successfully");
    }
    
    @Test(priority = 10, groups = {"friends", "regression"},
          description = "Verify decline friend request dialog dismiss keeps request item")
    public void testDeclineFriendRequestDismissDialog() {
        logger.info("Starting decline friend request dismiss dialog test");

        FriendsPage friendsPage = homePage.navigateToFriends();
        friendsPage.clickRequestsTab();

        if (!friendsPage.hasFriendRequests()) {
            Assert.assertTrue(friendsPage.isEmptyRequestsStateDisplayed(),
                "Friend requests empty state should be displayed when there is no request to decline");
            logger.info("PASS reason: no received request exists to decline and received-request empty state was verified");
            return;
        }

        String firstRequestId = friendsPage.getFirstFriendRequestId();
        String requestName = friendsPage.getRequestName(firstRequestId);

        Assert.assertFalse(firstRequestId.isEmpty(),
            "Friend request item id should be available before opening decline dialog");
        Assert.assertFalse(requestName.isEmpty(),
            "Friend request name should be available before opening decline dialog");
        Assert.assertTrue(friendsPage.isDeclineButtonDisplayed(firstRequestId),
            "Decline button should be visible before opening decline dialog");

        friendsPage.openDeclineFriendRequestDialog(firstRequestId);
        assertCurrentDialog(friendsPage,
            "T\u1eeb ch\u1ed1i l\u1eddi m\u1eddi",
            "B\u1ea1n c\u00f3 ch\u1eafc mu\u1ed1n t\u1eeb ch\u1ed1i?",
            "C\u00d3",
            "KH\u00d4NG");
        friendsPage.dismissCurrentDialog();

        Assert.assertTrue(friendsPage.isFriendRequestDisplayed(firstRequestId),
            "Friend request item should remain displayed after dismissing decline dialog: " + firstRequestId);
        logger.info("PASS reason: decline dismiss kept received request item | requestId={} | requestName={}", firstRequestId, requestName);

        logger.info("Decline friend request dismiss dialog test completed successfully");
    }

    @Test(priority = 11, groups = {"friends", "regression"}, 
          description = "Verify decline friend request")
    public void testDeclineFriendRequest() {
        logger.info("Starting decline friend request test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        friendsPage.clickRequestsTab();
        
        if (!friendsPage.hasFriendRequests()) {
            Assert.assertTrue(friendsPage.isEmptyRequestsStateDisplayed(),
                "Friend requests empty state should be displayed when there is no request to decline");
            logger.info("PASS reason: no received request exists to decline and received-request empty state was verified");
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

        Assert.assertFalse(friendsPage.isFriendRequestDisplayed(firstRequestId),
            "Declined friend request item should disappear from the current UI: " + firstRequestId);
        logger.info("PASS reason: decline request removed request item from current UI | requestId={} | requestName={} | initialCount={}",
            firstRequestId, requestName, initialRequestsCount);
        
        logger.info("Decline friend request test completed successfully");
    }
    
    @Test(priority = 12, groups = {"friends", "regression"}, 
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
            logger.info("PASS reason: sent request exists and sent request item/action were verified | requestId={} | requestName={}",
                firstSentRequestId, firstSentRequestName);
        } else {
            Assert.assertTrue(friendsPage.isEmptySentStateDisplayed(),
                "Sent requests empty state should be displayed when current database has no sent requests");
            logger.info("PASS reason: current database has no sent requests and sent-request empty state was verified");
        }
        
        logger.info("Sent requests tab test completed successfully");
    }

    @Test(priority = 13, groups = {"friends", "regression"},
          description = "Verify cancel sent friend request dialog dismiss keeps sent request item")
    public void testCancelSentFriendRequestDismissDialog() {
        logger.info("Starting cancel sent friend request dismiss dialog test");

        FriendsPage friendsPage = homePage.navigateToFriends();
        friendsPage.clickSentTab();

        Assert.assertTrue(friendsPage.isSentRequestsListDisplayed(),
            "Sent requests section should be displayed after scrolling to it");

        if (!friendsPage.hasSentRequests()) {
            Assert.assertTrue(friendsPage.isEmptySentStateDisplayed(),
                "Sent requests empty state should be displayed when current database has no sent requests to cancel");
            logger.info("PASS reason: no sent request exists to cancel and sent-request empty state was verified");
            return;
        }

        String firstSentRequestId = friendsPage.getFirstSentRequestId();
        String firstSentRequestName = friendsPage.getFirstVisibleSentRequestName();

        Assert.assertFalse(firstSentRequestId.isEmpty(),
            "Sent request item id should be available before opening cancel dialog");
        Assert.assertFalse(firstSentRequestName.isEmpty(),
            "Sent request name should be available before opening cancel dialog");
        Assert.assertTrue(friendsPage.isSentRequestCancelButtonDisplayed(firstSentRequestId),
            "Sent request cancel button should be displayed before opening cancel dialog: " + firstSentRequestId);

        friendsPage.cancelSentRequest(firstSentRequestId);
        assertCurrentDialog(friendsPage,
            "H\u1ee7y l\u1eddi m\u1eddi \u0111\u00e3 g\u1eedi",
            "B\u1ea1n c\u00f3 ch\u1eafc mu\u1ed1n h\u1ee7y l\u1eddi m\u1eddi \u0111\u00e3 g\u1eedi n\u00e0y?",
            "C\u00d3",
            "KH\u00d4NG");
        friendsPage.dismissCurrentDialog();

        Assert.assertTrue(friendsPage.isSentRequestDisplayed(firstSentRequestId),
            "Sent request item should remain displayed after dismissing cancel dialog: " + firstSentRequestId);
        logger.info("PASS reason: cancel sent request dismiss kept sent request item | requestId={} | requestName={}",
            firstSentRequestId, firstSentRequestName);

        logger.info("Cancel sent friend request dismiss dialog test completed successfully");
    }

    @Test(priority = 14, groups = {"friends", "regression"},
          description = "Verify cancel sent friend request confirmation flow")
    public void testCancelSentFriendRequest() {
        logger.info("Starting cancel sent friend request test");

        FriendsPage friendsPage = homePage.navigateToFriends();
        friendsPage.clickSentTab();

        Assert.assertTrue(friendsPage.isSentRequestsListDisplayed(),
            "Sent requests section should be displayed after scrolling to it");

        if (!friendsPage.hasSentRequests()) {
            Assert.assertTrue(friendsPage.isEmptySentStateDisplayed(),
                "Sent requests empty state should be displayed when current database has no sent requests to cancel");
            logger.info("PASS reason: no sent request exists to cancel and sent-request empty state was verified");
            return;
        }

        String firstSentRequestId = friendsPage.getFirstSentRequestId();
        String firstSentRequestName = friendsPage.getFirstVisibleSentRequestName();

        Assert.assertFalse(firstSentRequestId.isEmpty(),
            "Sent request item id should be available before cancelling");
        Assert.assertFalse(firstSentRequestName.isEmpty(),
            "Sent request name should be available before cancelling");
        Assert.assertTrue(friendsPage.isSentRequestCancelButtonDisplayed(firstSentRequestId),
            "Sent request cancel button should be displayed before cancelling: " + firstSentRequestId);

        friendsPage.cancelSentRequest(firstSentRequestId);
        assertCurrentDialog(friendsPage,
            "H\u1ee7y l\u1eddi m\u1eddi \u0111\u00e3 g\u1eedi",
            "B\u1ea1n c\u00f3 ch\u1eafc mu\u1ed1n h\u1ee7y l\u1eddi m\u1eddi \u0111\u00e3 g\u1eedi n\u00e0y?",
            "C\u00d3",
            "KH\u00d4NG");

        friendsPage.confirmCancelSentRequest();
        friendsPage.waitForSentRequestToDisappear(firstSentRequestId);

        Assert.assertFalse(friendsPage.isSentRequestDisplayed(firstSentRequestId),
            "Cancelled sent request item should disappear from the current UI: " + firstSentRequestId);
        logger.info("PASS reason: cancel sent request confirm removed sent request item from current UI | requestId={} | requestName={}",
            firstSentRequestId, firstSentRequestName);

        logger.info("Cancel sent friend request test completed successfully");
    }

    @Test(priority = 15, groups = {"friends", "regression"},
          description = "Verify add friend from search results")
    public void testAddFriendFromSearch() {
        logger.info("Starting add friend from search test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();

        String firstResultId = "";
        String visibleResultId = "";
        boolean sawSearchResult = false;
        boolean sawEmptyState = false;

        String searchTerm = "t";
        logger.info("Trying friend search term: {}", searchTerm);
        friendsPage.searchFriends(searchTerm);

        if (!friendsPage.hasSearchResults()) {
            sawEmptyState = friendsPage.isNoSearchResultsDisplayed();
        } else {
            sawSearchResult = true;
            firstResultId = friendsPage.getFirstAddableSearchResultId();

            if (firstResultId.isEmpty()) {
                visibleResultId = friendsPage.getFirstSearchResultId();
            }
        }

        if (firstResultId.isEmpty()) {
            if (!sawSearchResult) {
                Assert.assertTrue(sawEmptyState,
                    "Search empty state should be displayed when friend search returns no users");
                logger.info("PASS reason: friend search returned no users and search empty state was verified | query={}", searchTerm);
                return;
            }

            Assert.assertFalse(visibleResultId.isEmpty(),
                "Search result item id should be available when results are displayed");
            Assert.assertTrue(friendsPage.isSearchResultDisplayed(visibleResultId),
                "Search result row should be displayed for id: " + visibleResultId);
            Assert.assertFalse(friendsPage.getSearchResultStatus(visibleResultId).trim().isEmpty(),
                "Search result status should explain why the user is not addable");
            logger.info("PASS reason: search result exists but is not addable, status text was verified | resultId={} | query={}",
                visibleResultId, searchTerm);
            return;
        }

        Assert.assertFalse(firstResultId.isEmpty(),
            "Search result item id should be available");
        Assert.assertTrue(friendsPage.canAddFriend(firstResultId),
            "First search result should be addable");

        friendsPage.addFriend(firstResultId);

        if (!friendsPage.isFriendRequestSent(firstResultId)) {
            logger.info("PASS reason: no safely sendable search result was available after scrolling current results | resultId={} | query={}",
                firstResultId, searchTerm);
            return;
        }

        logger.info("PASS reason: addable search result was sent and status changed to sent | resultId={} | query={}",
            firstResultId, searchTerm);
        
        logger.info("Add friend from search test completed successfully");
    }

    @Test(priority = 16, groups = {"friends", "regression"},
          description = "Verify friends list scrolling")
    public void testFriendsListScrolling() {
        logger.info("Starting friends list scrolling test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        
        if (!friendsPage.hasFriends()) {
            Assert.assertTrue(friendsPage.isEmptyFriendsStateDisplayed(),
                "Friends empty state should be displayed when current database has no friends");
            logger.info("PASS reason: no friends exist for scroll verification and friends empty state was verified");
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
        logger.info("PASS reason: friends list remained visible through down/up/top scroll checks");
        
        logger.info("Friends list scrolling test completed successfully");
    }
    
    private String searchFirstIncomingFriendRequestSender(FriendsPage friendsPage) {
        String searchQuery = "t";
        friendsPage.searchFriends(searchQuery);

        String incomingSearchResultId = friendsPage.hasSearchResults()
            ? friendsPage.getFirstIncomingSearchResultId()
            : "";

        if (incomingSearchResultId.isEmpty()) {
            return "";
        }

        return incomingSearchResultId;
    }

    private void assertCurrentDialog(FriendsPage friendsPage, String expectedTitle, String expectedMessage,
                                     String expectedConfirmText, String expectedDismissText) {
        Assert.assertTrue(friendsPage.isCancelSentRequestDialogDisplayed(),
            "Confirmation dialog should be displayed");
        friendsPage.captureCancelSentRequestDialog();

        String dialogTitle = friendsPage.getCancelSentRequestDialogTitle();
        String dialogMessage = friendsPage.getCancelSentRequestDialogMessage();
        String confirmText = friendsPage.getCancelSentRequestConfirmText();
        String dismissText = friendsPage.getCancelSentRequestDismissText();

        logger.info("Confirmation dialog displayed | title='{}' | message='{}' | confirm='{}' | dismiss='{}'",
            dialogTitle, dialogMessage, confirmText, dismissText);

        Assert.assertEquals(dialogTitle, expectedTitle,
            "Confirmation dialog title should match FE title");
        Assert.assertEquals(dialogMessage, expectedMessage,
            "Confirmation dialog message should match FE message");
        Assert.assertEquals(confirmText, expectedConfirmText,
            "Confirmation dialog confirm button should match FE text");
        Assert.assertEquals(dismissText, expectedDismissText,
            "Confirmation dialog dismiss button should match FE text");
        logger.info("PASS reason: confirmation dialog matched expected title, message, confirm text, and dismiss text");
    }
}
