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
        friendsPage.searchUsers("camtu");
        Assert.assertTrue(friendsPage.areSearchResultsDisplayed() || friendsPage.hasSearchResults(), "Search results should be displayed for an existing user");
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
        Assert.assertTrue(friendsPage.isFriendsListDisplayed(), "Friends screen should remain displayed when search input is empty");
        Assert.assertFalse(friendsPage.hasSearchResults(), "No search results should be shown for empty search");
    }

    @Test(priority = 4, groups = {"search", "regression"}, description = "Validate short search input")
    public void testSearchInputValidationForShortValue() {
        FriendsPage friendsPage = homePage.navigateToFriends();
        friendsPage.enterSearchText("a");
        Assert.assertFalse(friendsPage.isSearchQueryValid("a"), "Single-character search query should be invalid");
    }

    @Test(priority = 5, groups = {"search", "regression"}, description = "Search with special characters")
    public void testSearchWithSpecialCharacters() {
        FriendsPage friendsPage = homePage.navigateToFriends();
        friendsPage.enterSearchText("!@#$%^&*");
        Assert.assertFalse(friendsPage.hasSearchResults(), "Special characters should not return valid users");
    }
}
