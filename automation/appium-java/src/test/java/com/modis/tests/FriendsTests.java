package com.modis.tests;

import com.modis.base.BaseTest;
import com.modis.constants.AppConstants;
import com.modis.pages.*;
import com.modis.utils.TestDataReader;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import java.util.Map;
import java.util.List;

/**
 * Test class for Friends functionality with real data
 * Covers friend search, friend requests, and friend management using actual user data
 */
public class FriendsTests extends BaseTest {
    
    private HomePage homePage;
    private TestDataReader testDataReader = new TestDataReader();
    
    // ==================== DATA PROVIDERS ====================
    
    @DataProvider(name = "testFriendsData")
    public Object[][] getTestFriendsData() {
        List<Map<String, Object>> friends = testDataReader.getTestFriends();
        Object[][] data = new Object[friends.size()][];
        
        for (int i = 0; i < friends.size(); i++) {
            Map<String, Object> friend = friends.get(i);
            data[i] = new Object[]{
                friend.get("id"),
                friend.get("username"),
                friend.get("fullName"),
                friend.get("email"),
                friend.get("status")
            };
        }
        return data;
    }
    
    @DataProvider(name = "friendRequestsData")
    public Object[][] getFriendRequestsData() {
        List<Map<String, Object>> requests = testDataReader.getFriendRequests();
        Object[][] data = new Object[requests.size()][];
        
        for (int i = 0; i < requests.size(); i++) {
            Map<String, Object> request = requests.get(i);
            data[i] = new Object[]{
                request.get("id"),
                request.get("senderUsername"),
                request.get("senderFullName"),
                request.get("status")
            };
        }
        return data;
    }
    
    @BeforeMethod(groups = {"friends", "regression"})
    public void loginBeforeTest() {
        logger.info("Logging in before friends test with real user data");
        
        // Use real user data for login
        Map<String, Object> testUser = testDataReader.getRandomValidUser();
        String username = (String) testUser.get("username");
        String password = (String) testUser.get("password");
        
        LoadingPage loadingPage = new LoadingPage();
        LoginPage loginPage = loadingPage.clickLoginButton();
        homePage = (HomePage) loginPage.login(username, password);
        
        Assert.assertTrue(homePage.isDisplayed(), "Should be logged in before friends tests");
        logger.info("Logged in successfully with user: " + username);
    }
    
    // ==================== FRIENDS DATA TESTS ====================
    
    @Test(priority = 1, groups = {"friends", "regression", "data"}, 
          dataProvider = "testFriendsData", description = "Verify friends data from real database")
    public void testFriendsWithRealData(String friendId, String username, String fullName, String email, String status) {
        logger.info("Testing friend data - Username: " + username + ", FullName: " + fullName);
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        
        // Search for the specific friend
        friendsPage.searchFriends(username);
        
        if (friendsPage.hasSearchResults()) {
            // Verify friend appears in search results
            Assert.assertTrue(friendsPage.isUserInSearchResults(username), 
                "User " + username + " should appear in search results");
            
            // Verify friend details if found
            if (friendsPage.isUserInSearchResults(username)) {
                String displayedName = friendsPage.getSearchResultName(username);
                Assert.assertTrue(displayedName.contains(fullName) || displayedName.contains(username),
                    "Displayed name should contain expected fullname or username");
            }
        }
        
        logger.info("Friend data test completed for: " + username);
    }
    
    @Test(priority = 2, groups = {"friends", "regression", "data"}, 
          dataProvider = "friendRequestsData", description = "Verify friend requests data from real database")
    public void testFriendRequestsWithRealData(String requestId, String senderUsername, String senderFullName, String status) {
        logger.info("Testing friend request data - Sender: " + senderUsername + ", Status: " + status);
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        friendsPage.clickRequestsTab();
        
        if (friendsPage.hasFriendRequests()) {
            // Look for the specific friend request
            if (friendsPage.isRequestFromUser(senderUsername)) {
                String displayedName = friendsPage.getRequestNameByUsername(senderUsername);
                Assert.assertTrue(displayedName.contains(senderFullName) || displayedName.contains(senderUsername),
                    "Request should show correct sender name");
                
                // Verify request has proper action buttons based on status
                if ("pending".equals(status)) {
                    Assert.assertTrue(friendsPage.isAcceptButtonDisplayed(senderUsername), 
                        "Accept button should be displayed for pending requests");
                    Assert.assertTrue(friendsPage.isDeclineButtonDisplayed(senderUsername), 
                        "Decline button should be displayed for pending requests");
                }
            }
        }
        
        logger.info("Friend request data test completed for: " + senderUsername);
    }
    
    // ==================== NAVIGATION TESTS ====================
    
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
    
    // ==================== FRIENDS LIST TESTS ====================
    
    @Test(priority = 3, groups = {"friends", "regression"}, 
          description = "Verify friends list display")
    public void testFriendsListDisplay() {
        logger.info("Starting friends list display test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        
        // Should be on Friends tab by default
        Assert.assertTrue(friendsPage.isFriendsTabSelected(), 
            "Friends tab should be selected by default");
        
        if (friendsPage.hasFriends()) {
            Assert.assertTrue(friendsPage.isFriendsListDisplayed(), 
                "Friends list should be displayed when friends exist");
            Assert.assertTrue(friendsPage.getFriendsCount() > 0, 
                "Should have at least one friend");
        } else {
            Assert.assertTrue(friendsPage.isEmptyFriendsStateDisplayed(), 
                "Empty state should be displayed when no friends exist");
        }
        
        logger.info("Friends list display test completed successfully");
    }
    
    @Test(priority = 4, groups = {"friends", "regression"}, 
          description = "Verify friend item elements")
    public void testFriendItemElements() {
        logger.info("Starting friend item elements test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        
        if (friendsPage.hasFriends()) {
            String firstFriendId = friendsPage.getFirstFriendId();
            
            Assert.assertTrue(friendsPage.isFriendAvatarDisplayed(firstFriendId), 
                "Friend avatar should be displayed");
            Assert.assertTrue(friendsPage.isFriendNameDisplayed(firstFriendId), 
                "Friend name should be displayed");
            Assert.assertFalse(friendsPage.getFriendName(firstFriendId).isEmpty(), 
                "Friend name should not be empty");
            
            // Check if username is displayed
            if (friendsPage.isFriendUsernameDisplayed(firstFriendId)) {
                Assert.assertFalse(friendsPage.getFriendUsername(firstFriendId).isEmpty(), 
                    "Friend username should not be empty when displayed");
            }
        } else {
            logger.info("No friends available for testing friend item elements");
        }
        
        logger.info("Friend item elements test completed successfully");
    }
    
    // ==================== FRIEND REQUESTS TESTS ====================
    
    @Test(priority = 5, groups = {"friends", "regression"}, 
          description = "Verify friend requests tab")
    public void testFriendRequestsTab() {
        logger.info("Starting friend requests tab test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        friendsPage.clickRequestsTab();
        
        Assert.assertTrue(friendsPage.isRequestsTabSelected(), 
            "Requests tab should be selected after clicking");
        
        if (friendsPage.hasFriendRequests()) {
            Assert.assertTrue(friendsPage.isFriendRequestsListDisplayed(), 
                "Friend requests list should be displayed when requests exist");
            Assert.assertTrue(friendsPage.getFriendRequestsCount() > 0, 
                "Should have at least one friend request");
        } else {
            Assert.assertTrue(friendsPage.isEmptyRequestsStateDisplayed(), 
                "Empty state should be displayed when no requests exist");
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
            String firstRequestId = friendsPage.getFirstFriendRequestId();
            
            Assert.assertTrue(friendsPage.isRequestAvatarDisplayed(firstRequestId), 
                "Request avatar should be displayed");
            Assert.assertTrue(friendsPage.isRequestNameDisplayed(firstRequestId), 
                "Request name should be displayed");
            Assert.assertTrue(friendsPage.isAcceptButtonDisplayed(firstRequestId), 
                "Accept button should be displayed");
            Assert.assertTrue(friendsPage.isDeclineButtonDisplayed(firstRequestId), 
                "Decline button should be displayed");
            
            Assert.assertFalse(friendsPage.getRequestName(firstRequestId).isEmpty(), 
                "Request name should not be empty");
        } else {
            logger.info("No friend requests available for testing request item elements");
        }
        
        logger.info("Friend request item elements test completed successfully");
    }
    
    @Test(priority = 7, groups = {"friends", "regression"}, 
          description = "Verify accept friend request")
    public void testAcceptFriendRequest() {
        logger.info("Starting accept friend request test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        friendsPage.clickRequestsTab();
        
        if (friendsPage.hasFriendRequests()) {
            String firstRequestId = friendsPage.getFirstFriendRequestId();
            String requestName = friendsPage.getRequestName(firstRequestId);
            int initialRequestsCount = friendsPage.getFriendRequestsCount();
            
            friendsPage.acceptFriendRequest(firstRequestId);
            
            // Wait for request to be processed
            friendsPage.waitForRequestToDisappear(firstRequestId);
            
            // Verify request was removed from list
            Assert.assertTrue(friendsPage.getFriendRequestsCount() < initialRequestsCount, 
                "Friend requests count should decrease after accepting");
            
            // Check if user appears in friends list
            friendsPage.clickFriendsTab();
            if (friendsPage.hasFriends()) {
                // Note: This might not always work if the friend list is paginated
                logger.info("Accepted friend request for: {}", requestName);
            }
        } else {
            logger.info("No friend requests available for testing accept functionality");
        }
        
        logger.info("Accept friend request test completed successfully");
    }
    
    @Test(priority = 8, groups = {"friends", "regression"}, 
          description = "Verify decline friend request")
    public void testDeclineFriendRequest() {
        logger.info("Starting decline friend request test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        friendsPage.clickRequestsTab();
        
        if (friendsPage.hasFriendRequests()) {
            String firstRequestId = friendsPage.getFirstFriendRequestId();
            String requestName = friendsPage.getRequestName(firstRequestId);
            int initialRequestsCount = friendsPage.getFriendRequestsCount();
            
            friendsPage.declineFriendRequest(firstRequestId);
            
            // Wait for request to be processed
            friendsPage.waitForRequestToDisappear(firstRequestId);
            
            // Verify request was removed from list
            Assert.assertTrue(friendsPage.getFriendRequestsCount() < initialRequestsCount, 
                "Friend requests count should decrease after declining");
            
            logger.info("Declined friend request for: {}", requestName);
        } else {
            logger.info("No friend requests available for testing decline functionality");
        }
        
        logger.info("Decline friend request test completed successfully");
    }
    
    // ==================== SENT REQUESTS TESTS ====================
    
    @Test(priority = 9, groups = {"friends", "regression"}, 
          description = "Verify sent requests tab")
    public void testSentRequestsTab() {
        logger.info("Starting sent requests tab test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        friendsPage.clickSentTab();
        
        Assert.assertTrue(friendsPage.isSentTabSelected(), 
            "Sent tab should be selected after clicking");
        
        if (friendsPage.hasSentRequests()) {
            Assert.assertTrue(friendsPage.isSentRequestsListDisplayed(), 
                "Sent requests list should be displayed when requests exist");
            Assert.assertTrue(friendsPage.getSentRequestsCount() > 0, 
                "Should have at least one sent request");
        } else {
            Assert.assertTrue(friendsPage.isEmptySentStateDisplayed(), 
                "Empty state should be displayed when no sent requests exist");
        }
        
        logger.info("Sent requests tab test completed successfully");
    }
    
    // ==================== SEARCH FUNCTIONALITY TESTS ====================
    
    @Test(priority = 10, groups = {"friends", "regression"}, 
          description = "Verify friend search functionality")
    public void testFriendSearch() {
        logger.info("Starting friend search test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        
        if (friendsPage.hasFriends()) {
            String firstFriendName = friendsPage.getFirstFriendName();
            String searchTerm = firstFriendName.substring(0, Math.min(3, firstFriendName.length()));
            
            friendsPage.searchFriends(searchTerm);
            
            // Verify search results
            Assert.assertTrue(friendsPage.isSearchResultsDisplayed(), 
                "Search results should be displayed");
            
            if (friendsPage.hasSearchResults()) {
                Assert.assertTrue(friendsPage.getSearchResultsCount() > 0, 
                    "Should have search results for existing friend name");
            }
            
            // Clear search
            friendsPage.clearSearch();
            Assert.assertTrue(friendsPage.isFriendsListDisplayed(), 
                "Friends list should be displayed after clearing search");
        } else {
            logger.info("No friends available for testing search functionality");
        }
        
        logger.info("Friend search test completed successfully");
    }
    
    @Test(priority = 11, groups = {"friends", "regression"}, 
          description = "Verify search with no results")
    public void testSearchWithNoResults() {
        logger.info("Starting search with no results test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        
        String nonExistentSearchTerm = "nonexistentuser" + System.currentTimeMillis();
        friendsPage.searchFriends(nonExistentSearchTerm);
        
        // Verify no results state
        Assert.assertTrue(friendsPage.isNoSearchResultsDisplayed(), 
            "No results state should be displayed for non-existent search term");
        
        // Clear search and verify return to normal state
        friendsPage.clearSearch();
        
        if (friendsPage.hasFriends()) {
            Assert.assertTrue(friendsPage.isFriendsListDisplayed(), 
                "Friends list should be displayed after clearing search");
        } else {
            Assert.assertTrue(friendsPage.isEmptyFriendsStateDisplayed(), 
                "Empty friends state should be displayed after clearing search");
        }
        
        logger.info("Search with no results test completed successfully");
    }
    
    @Test(priority = 12, groups = {"friends", "regression"}, 
          description = "Verify search input validation")
    public void testSearchInputValidation() {
        logger.info("Starting search input validation test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        
        // Test empty search
        friendsPage.enterSearchText("");
        Assert.assertFalse(friendsPage.isSearchButtonEnabled(), 
            "Search button should be disabled for empty input");
        
        // Test whitespace only search
        friendsPage.enterSearchText("   ");
        Assert.assertFalse(friendsPage.isSearchButtonEnabled(), 
            "Search button should be disabled for whitespace-only input");
        
        // Test minimum length search
        friendsPage.enterSearchText("ab");
        // Behavior may vary - some apps allow 2 characters, others require 3+
        logger.info("Search button enabled for 2 characters: {}", friendsPage.isSearchButtonEnabled());
        
        // Test valid search
        friendsPage.enterSearchText("test");
        Assert.assertTrue(friendsPage.isSearchButtonEnabled(), 
            "Search button should be enabled for valid input");
        
        logger.info("Search input validation test completed successfully");
    }
    
    // ==================== ADD FRIEND TESTS ====================
    
    @Test(priority = 13, groups = {"friends", "regression"}, 
          description = "Verify add friend from search results")
    public void testAddFriendFromSearch() {
        logger.info("Starting add friend from search test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        
        // Search for potential friends (users not already friends)
        String searchTerm = "test";
        friendsPage.searchFriends(searchTerm);
        
        if (friendsPage.hasSearchResults()) {
            String firstResultId = friendsPage.getFirstSearchResultId();
            
            if (friendsPage.canAddFriend(firstResultId)) {
                String userName = friendsPage.getSearchResultName(firstResultId);
                
                friendsPage.addFriend(firstResultId);
                
                // Verify friend request was sent
                Assert.assertTrue(friendsPage.isFriendRequestSent(firstResultId), 
                    "Friend request should be marked as sent");
                
                logger.info("Sent friend request to: {}", userName);
            } else {
                logger.info("No users available to add as friends in search results");
            }
        } else {
            logger.info("No search results available for testing add friend functionality");
        }
        
        logger.info("Add friend from search test completed successfully");
    }
    
    // ==================== TAB NAVIGATION TESTS ====================
    
    @Test(priority = 14, groups = {"friends", "navigation"}, 
          description = "Verify tab navigation functionality")
    public void testTabNavigation() {
        logger.info("Starting tab navigation test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        
        // Test Friends tab (should be default)
        Assert.assertTrue(friendsPage.isFriendsTabSelected(), 
            "Friends tab should be selected by default");
        
        // Test Requests tab
        friendsPage.clickRequestsTab();
        Assert.assertTrue(friendsPage.isRequestsTabSelected(), 
            "Requests tab should be selected after clicking");
        Assert.assertFalse(friendsPage.isFriendsTabSelected(), 
            "Friends tab should not be selected when Requests tab is active");
        
        // Test Sent tab
        friendsPage.clickSentTab();
        Assert.assertTrue(friendsPage.isSentTabSelected(), 
            "Sent tab should be selected after clicking");
        Assert.assertFalse(friendsPage.isRequestsTabSelected(), 
            "Requests tab should not be selected when Sent tab is active");
        
        // Return to Friends tab
        friendsPage.clickFriendsTab();
        Assert.assertTrue(friendsPage.isFriendsTabSelected(), 
            "Friends tab should be selected after clicking");
        Assert.assertFalse(friendsPage.isSentTabSelected(), 
            "Sent tab should not be selected when Friends tab is active");
        
        logger.info("Tab navigation test completed successfully");
    }
    
    // ==================== LIST SCROLLING TESTS ====================
    
    @Test(priority = 15, groups = {"friends", "regression"}, 
          description = "Verify friends list scrolling")
    public void testFriendsListScrolling() {
        logger.info("Starting friends list scrolling test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        
        if (friendsPage.hasFriends() && friendsPage.getFriendsCount() > 5) {
            // Test scrolling down
            friendsPage.scrollDown();
            Assert.assertTrue(friendsPage.isFriendsListDisplayed(), 
                "Friends list should still be displayed after scrolling down");
            
            // Test scrolling up
            friendsPage.scrollUp();
            Assert.assertTrue(friendsPage.isFriendsListDisplayed(), 
                "Friends list should still be displayed after scrolling up");
            
            // Test scroll to top
            friendsPage.scrollToTop();
            Assert.assertTrue(friendsPage.isFriendsListDisplayed(), 
                "Friends list should still be displayed after scrolling to top");
        } else {
            logger.info("Not enough friends for scrolling test");
        }
        
        logger.info("Friends list scrolling test completed successfully");
    }
    
    // ==================== REFRESH FUNCTIONALITY TESTS ====================
    
    @Test(priority = 16, groups = {"friends", "regression"}, 
          description = "Verify friends list refresh")
    public void testFriendsListRefresh() {
        logger.info("Starting friends list refresh test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        
        int initialFriendsCount = friendsPage.getFriendsCount();
        
        // Perform pull-to-refresh
        friendsPage.refreshFriendsList();
        
        // Verify page is still functional after refresh
        Assert.assertTrue(friendsPage.isDisplayed(), 
            "Friends page should be displayed after refresh");
        Assert.assertTrue(friendsPage.verifyPageElements(), 
            "Friends page elements should be present after refresh");
        
        // Friends count should be same or different (if there were updates)
        int newFriendsCount = friendsPage.getFriendsCount();
        logger.info("Friends count before refresh: {}, after refresh: {}", 
                   initialFriendsCount, newFriendsCount);
        
        logger.info("Friends list refresh test completed successfully");
    }
    
    // ==================== ERROR HANDLING TESTS ====================
    
    @Test(priority = 17, groups = {"friends", "regression"}, 
          description = "Verify network error handling")
    public void testNetworkErrorHandling() {
        logger.info("Starting network error handling test");
        
        FriendsPage friendsPage = homePage.navigateToFriends();
        
        // Simulate network issue by putting app in background
        backgroundApp(3);
        
        // Try to perform actions that require network
        friendsPage.refreshFriendsList();
        
        // Verify app remains stable
        Assert.assertTrue(friendsPage.isDisplayed(), 
            "Friends page should remain stable during network issues");
        
        // Try search functionality
        if (friendsPage.hasFriends()) {
            friendsPage.searchFriends("test");
            Assert.assertTrue(friendsPage.isDisplayed(), 
                "Friends page should remain stable during search with network issues");
        }
        
        logger.info("Network error handling test completed successfully");
    }
}