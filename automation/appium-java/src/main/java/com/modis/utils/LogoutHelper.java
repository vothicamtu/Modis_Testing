package com.modis.utils;

import com.modis.constants.TestIDs;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.List;

/**
 * Helper logout theo chiến lược "không clear data" (Android 11 real device).
 * Mục tiêu: nếu app đang ở trạng thái logged-in thì logout về Login/Loading.
 *
 * Lưu ý: Method này không được phép làm crash suite nếu element không tồn tại.
 */
public final class LogoutHelper {

    private static final Logger logger = LoggerUtil.getLogger(LogoutHelper.class);

    private LogoutHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void logoutIfLoggedIn(AppiumDriver driver) {
        if (driver == null) return;

        try {
            // Nếu đang ở Login/Loading thì không cần logout
            if (isVisible(driver, AppiumBy.accessibilityId(TestIDs.LOGIN_SCREEN), 1) ||
                    isVisible(driver, AppiumBy.accessibilityId(TestIDs.LOADING_LOGIN_BUTTON), 1) ||
                    isVisible(driver, AppiumBy.accessibilityId(TestIDs.LOADING_SIGNUP_BUTTON), 1)) {
                logger.info("Đã ở trạng thái chưa login (Login/Loading) -> skip logout");
                return;
            }

            // Tín hiệu logged-in thường gặp: topbar avatar button hoặc home_screen
            boolean looksLoggedIn =
                    isVisible(driver, AppiumBy.accessibilityId(TestIDs.TOPBAR_AVATAR_BUTTON), 1) ||
                            isVisible(driver, AppiumBy.accessibilityId(TestIDs.HOME_SCREEN), 1);

            if (!looksLoggedIn) {
                logger.info("Không detect trạng thái logged-in -> skip logout");
                return;
            }

            logger.info("Detect logged-in -> thực hiện logout qua UI");

            // 1) Vào Profile (tap avatar) nếu đang ở Home
            if (isVisible(driver, AppiumBy.accessibilityId(TestIDs.TOPBAR_AVATAR_BUTTON), 1)) {
                safeClick(driver, AppiumBy.accessibilityId(TestIDs.TOPBAR_AVATAR_BUTTON));
            }

            // 2) Chờ nút logout xuất hiện (bounded)
            if (!isVisible(driver, AppiumBy.accessibilityId(TestIDs.PROFILE_LOGOUT_BUTTON), 5)) {
                try {
                    GestureUtils gestureUtils = new GestureUtils(driver);
                    WebElement logout = gestureUtils.scrollToElementByAccessibilityId(TestIDs.PROFILE_LOGOUT_BUTTON);
                    if (logout == null || !logout.isDisplayed()) {
                        logger.warn("Không thấy logout button trong Profile -> skip logout");
                        return;
                    }
                    logout.click();
                } catch (Exception e) {
                    logger.warn("Không thể scroll tới logout button -> skip logout: {}", e.getMessage());
                    return;
                }
            } else {
                safeClick(driver, AppiumBy.accessibilityId(TestIDs.PROFILE_LOGOUT_BUTTON));
            }

            // 3) Nếu có modal confirm -> confirm
            if (isVisible(driver, AppiumBy.accessibilityId(TestIDs.MODAL_CONFIRM_BUTTON), 2)) {
                safeClick(driver, AppiumBy.accessibilityId(TestIDs.MODAL_CONFIRM_BUTTON));
            }

            // 4) Chờ về Login/Loading (bounded)
            waitForAny(driver, 10,
                    AppiumBy.accessibilityId(TestIDs.LOGIN_SCREEN),
                    AppiumBy.accessibilityId(TestIDs.LOADING_LOGIN_BUTTON),
                    AppiumBy.accessibilityId(TestIDs.LOADING_SIGNUP_BUTTON));

            logger.info("Logout complete (best-effort)");
        } catch (Exception e) {
            logger.warn("logoutIfLoggedIn gặp lỗi nhưng sẽ bỏ qua để không crash suite: {}", e.getMessage());
        }
    }

    private static boolean isVisible(AppiumDriver driver, By locator, int timeoutSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            wait.pollingEvery(Duration.ofMillis(250));
            Boolean visible = wait.until(d -> {
                try {
                    List<WebElement> els = d.findElements(locator);
                    if (els == null || els.isEmpty()) return false;
                    WebElement el = els.get(0);
                    return el != null && el.isDisplayed();
                } catch (Exception ex) {
                    return false;
                }
            });
            return visible != null && visible;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static void safeClick(AppiumDriver driver, By locator) {
        try {
            List<WebElement> els = driver.findElements(locator);
            if (els == null || els.isEmpty()) return;
            els.get(0).click();
        } catch (Exception e) {
            // fallback tap by coordinates not necessary here; just best-effort
            logger.debug("safeClick failed for {}: {}", locator, e.getMessage());
        }
    }

    @SafeVarargs
    private static void waitForAny(AppiumDriver driver, int timeoutSeconds, By... locators) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            wait.pollingEvery(Duration.ofMillis(250));
            wait.until(d -> {
                for (By by : locators) {
                    try {
                        List<WebElement> els = d.findElements(by);
                        if (els == null || els.isEmpty()) continue;
                        WebElement el = els.get(0);
                        if (el != null && el.isDisplayed()) return true;
                    } catch (Exception ignored) {
                    }
                }
                return false;
            });
        } catch (Exception ignored) {
        }
    }
}
