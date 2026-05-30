package com.modis.tests;

import com.modis.base.BasePage;
import com.modis.pages.HomePage;
import com.modis.pages.LoadingPage;
import com.modis.pages.LoginPage;
import com.modis.utils.TestDataReader;
import org.testng.Assert;

import java.util.Map;

final class TestSessionHelper {

    private TestSessionHelper() {
    }

    static HomePage loginAsDefaultUser(TestDataReader testDataReader) {

        LoginPage loginPage;

        try {
            HomePage currentHome = new HomePage();

            if (currentHome.isDisplayed()) {
                currentHome.waitForTopbarReadyAfterLogin(8);
                return currentHome;
            }
        } catch (Exception ignored) {
        }

        try {
            LoginPage currentLogin = new LoginPage();

            if (currentLogin.isDisplayed()) {
                loginPage = currentLogin;
            } else {
                loginPage = new LoadingPage().clickLoginButton();
            }
        } catch (Exception e) {
            loginPage = new LoadingPage().clickLoginButton();
        }

        Map<String, Object> testUser = testDataReader.getValidUserByUsername("tu");

        Assert.assertNotNull(
                testUser,
                "Default user 'tu' should exist in test data"
        );

        BasePage afterLogin = loginPage.login(
                (String) testUser.get("username"),
                (String) testUser.get("password")
        );

        Assert.assertTrue(
                afterLogin instanceof HomePage,
                "Login should navigate to HomePage"
        );

        HomePage homePage = (HomePage) afterLogin;
        homePage.waitForTopbarReadyAfterLogin(8);

        Assert.assertTrue(
                homePage.isDisplayed(),
                "Home page should display after login"
        );

        return homePage;
    }
}
