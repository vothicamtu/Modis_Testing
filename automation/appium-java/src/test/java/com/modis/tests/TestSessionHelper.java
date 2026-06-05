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

        String username = (String) testUser.get("username");
        String password = (String) testUser.get("password");

        HomePage homePage = null;

        for (int attempt = 1; attempt <= 2; attempt++) {
            BasePage afterLogin = loginPage.login(username, password);

            if (afterLogin instanceof HomePage) {
                homePage = (HomePage) afterLogin;
            } else {
                homePage = detectHomePage();
            }

            if (homePage != null) {
                homePage.waitForTopbarReadyAfterLogin(8);

                if (homePage.isDisplayed()) {
                    return homePage;
                }
            }

            if (attempt < 2) {
                loginPage = prepareLoginPage();
            }
        }

        Assert.assertTrue(
                false,
                "Login should navigate to HomePage"
        );

        return null;
    }

    private static LoginPage prepareLoginPage() {
        try {
            LoginPage currentLogin = new LoginPage();

            if (currentLogin.isDisplayed()) {
                return currentLogin;
            }
        } catch (Exception ignored) {
        }

        return new LoadingPage().clickLoginButton();
    }

    private static HomePage detectHomePage() {
        try {
            HomePage homePage = new HomePage();
            homePage.waitForTopbarReadyAfterLogin(8);

            if (homePage.isDisplayed()) {
                return homePage;
            }
        } catch (Exception ignored) {
        }

        return null;
    }
}
