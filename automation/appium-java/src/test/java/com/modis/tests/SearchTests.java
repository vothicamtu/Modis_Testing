package com.modis.tests;

import com.modis.base.BaseTest;
import com.modis.pages.FriendsPage;
import com.modis.pages.HomePage;
import com.modis.utils.TestDataReader;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SearchTests extends BaseTest {

    private HomePage homePage;
    private final TestDataReader testDataReader = new TestDataReader();

    @BeforeMethod(alwaysRun = true)
    public void loginBeforeTest() {
        homePage = TestSessionHelper.loginAsDefaultUser(testDataReader);
    }

    @Test(priority = 1, groups = {"search", "smoke", "regression"}, description = "Search users with results")
    public void testSearchUsersWithResults() {
        FriendsPage friendsPage = homePage.navigateToFriends();
        friendsPage.searchUsers("t");

        if (friendsPage.hasSearchResults()) {
            String firstResultId = friendsPage.getFirstSearchResultId();

            Assert.assertFalse(firstResultId.isEmpty(), "Search result item id should be available");
            Assert.assertTrue(friendsPage.isSearchResultDisplayed(firstResultId), "Search result row should be displayed");
            Assert.assertTrue(friendsPage.isSearchResultNameDisplayed(firstResultId), "Search result name should be displayed");
            Assert.assertTrue(friendsPage.isSearchResultUsernameDisplayed(firstResultId), "Search result username should be displayed");
        } else {
            Assert.assertTrue(friendsPage.isSearchEmptyStateDisplayed() || friendsPage.isNoSearchResultsDisplayed(), "Empty state should be displayed when search returns no users");
        }
    }

    @Test(priority = 2, groups = {"search", "regression"}, description = "Search users with no results")
    public void testSearchUsersWithNoResults() {
        FriendsPage friendsPage = homePage.navigateToFriends();
        friendsPage.searchForNonExistentUser();
        Assert.assertTrue(friendsPage.isSearchEmptyStateDisplayed() || friendsPage.isNoSearchResultsDisplayed(), "No-result state should be displayed for a non-existent user");
    }

    @Test(priority = 3, groups = {"search", "regression"}, description = "Validate empty search input")
    public void testSearchInputValidationForEmptyValue() {
        FriendsPage friendsPage = homePage.navigateToFriends();
        friendsPage.enterSearchText("");
        Assert.assertTrue(friendsPage.isDisplayed(),
            "Friends screen should remain displayed when search input is empty");
        Assert.assertTrue(friendsPage.isFriendsListDisplayed()
                || !friendsPage.getFirstVisibleFriendName().isEmpty()
                || friendsPage.isEmptyFriendsStateDisplayed(),
            "Friends list or empty state should be displayed when search input is empty");
        Assert.assertFalse(friendsPage.hasSearchResults(), "No search results should be shown for empty search");
    }

    @Test(priority = 4, groups = {"search", "regression"}, description = "Validate short search input")
    public void testSearchInputValidationForShortValue() {
        FriendsPage friendsPage = homePage.navigateToFriends();
        friendsPage.searchUsers("t");

        Assert.assertTrue(friendsPage.isSearchQueryValid("t"), "Single-character search query should be valid");
        Assert.assertTrue(friendsPage.hasSearchResults()
                || friendsPage.isSearchEmptyStateDisplayed()
                || friendsPage.isNoSearchResultsDisplayed(),
            "Single-character search should render search results or an empty state");
    }

    @Test(priority = 5, groups = {"search", "regression"}, description = "Search with special characters")
    public void testSearchWithSpecialCharacters() {
        FriendsPage friendsPage = homePage.navigateToFriends();
        friendsPage.enterSearchText("!@#$%^&*");
        Assert.assertFalse(friendsPage.hasSearchResults(), "Special characters should not return valid users");
    }

    @Test(priority = 6, groups = {"search", "regression"}, description = "Search with long text")
    public void testSearchWithLongText() {
        FriendsPage friendsPage = homePage.navigateToFriends();
        Assert.assertTrue(friendsPage.isSearchInputIconDisplayed(), "Search input icon should be displayed before long search");

        friendsPage.searchUsers("testuser009b_long_query_123456789_!@#$%^&*()_+-=[]{}|;:'\",.<>/?_testuser009b_long_query_123456789");

        if (friendsPage.hasSearchResults()) {
            String firstResultId = friendsPage.getFirstSearchResultId();

            Assert.assertFalse(firstResultId.isEmpty(), "Search result item id should be available for long search text");
            Assert.assertTrue(friendsPage.isSearchResultDisplayed(firstResultId), "Search result row should be displayed for long search text");
            Assert.assertTrue(friendsPage.isSearchResultNameDisplayed(firstResultId), "Search result name should be displayed for long search text");
            Assert.assertTrue(friendsPage.isSearchResultUsernameDisplayed(firstResultId), "Search result username should be displayed for long search text");
        } else {
            Assert.assertTrue(friendsPage.isSearchEmptyStateDisplayed() || friendsPage.isNoSearchResultsDisplayed(), "Empty state should be displayed when long search text returns no users");
        }
    }
}
