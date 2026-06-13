package com.modis.pages;

import com.modis.base.BasePage;
import com.modis.constants.TestIDs;
import com.modis.constants.AppConstants;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;

import java.util.List;

public class AllImagesPage extends BasePage {
    @AndroidFindBy(accessibility = TestIDs.ALL_IMAGES_SCREEN)
    @iOSXCUITFindBy(accessibility = TestIDs.ALL_IMAGES_SCREEN)
    private WebElement allImagesScreen;
    @AndroidFindBy(accessibility = TestIDs.ALL_IMAGES_BACK_BUTTON)
    @iOSXCUITFindBy(accessibility = TestIDs.ALL_IMAGES_BACK_BUTTON)
    private WebElement backButton;
    @AndroidFindBy(accessibility = TestIDs.ALL_IMAGES_GRID)
    @iOSXCUITFindBy(accessibility = TestIDs.ALL_IMAGES_GRID)
    private WebElement imagesGrid;
    @AndroidFindBy(accessibility = TestIDs.ALL_IMAGES_EMPTY_STATE)
    @iOSXCUITFindBy(accessibility = TestIDs.ALL_IMAGES_EMPTY_STATE)
    private WebElement emptyState;

    public TakePage navigateBack() {
        logger.info("Navigating back from All Images screen");
        waitForElementClickable(TestIDs.ALL_IMAGES_BACK_BUTTON);
        clickByAccessibilityId(
                TestIDs.ALL_IMAGES_BACK_BUTTON
        );
        return new TakePage();
    }

    public boolean hasImages() {
        return getVisibleImagesCount() > 0;
    }

    public boolean areImagesDisplayed() {
        return isImagesGridDisplayed()
                &&
                getVisibleImagesCount() > 0;
    }

    public int getImageCount() {
        return getVisibleImagesCount();
    }

    public AllImagesPage tapImage(String imageId) {
        logger.info("Tapping image: {}", imageId);
        String imageTestId = TestIDs.ALL_IMAGES_ITEM_PREFIX + imageId;
        WebElement imageElement = scrollToElementByAccessibilityId(imageTestId);
        if (imageElement != null) {
            clickElement(imageElement);
            waitForAnimation();
        } else {
            logger.warn("Image {} not found in grid", imageId);
        }
        return this;
    }

    public AllImagesPage longPressImage(String imageId) {
        logger.info("Long pressing image: {}", imageId);
        String imageTestId = TestIDs.ALL_IMAGES_ITEM_PREFIX + imageId;
        WebElement imageElement = scrollToElementByAccessibilityId(imageTestId);
        if (imageElement != null) {
            longPressElement(imageElement, AppConstants.LONG_PRESS_DURATION_MS);
        } else {
            logger.warn("Image {} not found for long press", imageId);
        }
        return this;
    }

    public AllImagesPage selectImage(String imageId) {
        logger.info("Selecting image: {}", imageId);
        String imageTestId = TestIDs.ALL_IMAGES_ITEM_PREFIX + imageId;
        WebElement imageElement = scrollToElementByAccessibilityId(imageTestId);
        if (imageElement != null) {
            clickElement(imageElement);
        } else {
            logger.warn("Image {} not found for selection", imageId);
        }
        return this;
    }

    public AllImagesPage selectImages(String[] imageIds) {
        logger.info("Selecting {} images", imageIds.length);
        for (String imageId : imageIds) {
            selectImage(imageId);
        }
        return this;
    }

    public AllImagesPage deleteImage(String imageId) {
        logger.info("Deleting image: {}", imageId);
        longPressImage(imageId);
        return this;
    }

    public AllImagesPage shareImage(String imageId) {
        logger.info("Sharing image: {}", imageId);
        longPressImage(imageId);
        return this;
    }

    public AllImagesPage scrollImagesGrid(String direction) {
        logger.debug("Scrolling images grid: {}", direction);
        waitForElementVisible(TestIDs.ALL_IMAGES_GRID);
        WebElement grid =
                findByAccessibilityId(
                        TestIDs.ALL_IMAGES_GRID
                );
        scrollInElement(grid, direction);
        return this;
    }

    public AllImagesPage scrollToTop() {
        logger.info("Scrolling to top of images grid");
        scrollToTopBase();
        return this;
    }

    public AllImagesPage scrollToBottom() {
        logger.info("Scrolling to bottom of images grid");
        scrollToBottomBase();
        return this;
    }

    public AllImagesPage refreshImages() {
        logger.info("Refreshing images grid");
        pullToRefresh();
        waitForAnimation();
        return this;
    }

    public boolean findImageInGrid(String imageId) {
        logger.info("Searching for image in grid: {}", imageId);
        String imageTestId = TestIDs.ALL_IMAGES_ITEM_PREFIX + imageId;
        WebElement imageElement = scrollToElementByAccessibilityId(imageTestId);
        return imageElement != null;
    }

    public boolean isImagesGridDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.ALL_IMAGES_GRID);
    }

    public boolean isEmptyStateDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.ALL_IMAGES_EMPTY_STATE);
    }

    public boolean isImageDisplayed(String imageId) {
        String imageTestId = TestIDs.ALL_IMAGES_ITEM_PREFIX + imageId;
        return isElementDisplayedByAccessibilityId(imageTestId);
    }

    public int getVisibleImagesCount() {
        logger.info(
                "Getting visible images count"
        );
        if (isEmptyStateDisplayed()) {
            return 0;
        }
        try {
            List<WebElement> images =
                    findElementsByXPath(
                            "//*[contains(@resource-id,'" + TestIDs.ALL_IMAGES_ITEM_PREFIX + "')]"
                    );
            int count =
                    images != null
                            ? images.size()
                            : 0;
            logger.info(
                    "Visible images count: {}",
                    count
            );
            return count;
        } catch (Exception e) {
            logger.warn(
                    "Failed to get visible images count: {}",
                    e.getMessage()
            );
            return 0;
        }
    }

    public boolean isImageSelected(String imageId) {
        String imageTestId = TestIDs.ALL_IMAGES_ITEM_PREFIX + imageId;
        try {
            WebElement imageElement = findByAccessibilityId(imageTestId);
            String selected = imageElement.getAttribute("selected");
            return "true".equals(selected);
        } catch (Exception e) {
            logger.debug("Image {} not found or selection state unavailable", imageId);
            return false;
        }
    }

    public int getSelectedImagesCount() {
        try {
            List<WebElement> images =
                    findElementsByXPath(
                            "//*[contains(@resource-id,'" + TestIDs.ALL_IMAGES_ITEM_PREFIX + "')]"
                    );
            int selectedCount = 0;
            for (WebElement image : images) {
                String selected =
                        image.getAttribute("selected");
                if ("true".equals(selected)) {
                    selectedCount++;
                }
            }
            logger.debug(
                    "Selected images count: {}",
                    selectedCount
            );
            return selectedCount;
        } catch (Exception e) {
            logger.warn(
                    "Failed to get selected images count: {}",
                    e.getMessage()
            );
            return 0;
        }
    }

    public AllImagesPage attemptSelectNonExistentImage(String nonExistentId) {
        logger.info("Attempting to select non-existent image: {}", nonExistentId);
        String imageTestId = TestIDs.ALL_IMAGES_ITEM_PREFIX + nonExistentId;
        if (isElementDisplayedByAccessibilityId(imageTestId)) {
            clickByAccessibilityId(imageTestId);
        } else {
            logger.info("Non-existent image not found as expected");
        }
        return this;
    }

    public AllImagesPage testEmptyState() {
        logger.info("Testing empty state behavior");
        if (isEmptyStateDisplayed()) {
            clickByAccessibilityId(
                    TestIDs.ALL_IMAGES_EMPTY_STATE
            );
        }
        return this;
    }

    public AllImagesPage testRapidSelection(String[] imageIds) {
        logger.info("Testing rapid selection of {} images", imageIds.length);
        for (String imageId : imageIds) {
            selectImage(imageId);
        }
        return this;
    }

    public AllImagesPage performLoadTest() {
        logger.info("Performing load test with image grid");
        for (int i = 0; i < 10; i++) {
            scrollImagesGrid("down");
            waitFor(1);
        }
        scrollToTop();
        return this;
    }

    @Override
    public boolean isDisplayed() {
        try {
            return isElementDisplayedByAccessibilityId(TestIDs.ALL_IMAGES_SCREEN);
        } catch (Exception e) {
            logger.debug("All images screen not displayed", e);
            return false;
        }
    }

    @Override
    public String getPageTitle() {
        return "All Images Screen";
    }

    public AllImagesPage waitForPageToLoad() {
        logger.info("Waiting for all images page to load");
        waitForElementVisible(TestIDs.ALL_IMAGES_SCREEN);
        try {
            waitForElementVisible(TestIDs.ALL_IMAGES_GRID);
        } catch (Exception e) {
            if (!isElementDisplayedByAccessibilityId(TestIDs.ALL_IMAGES_EMPTY_STATE)) {
                logger.warn("Neither images grid nor empty state found");
            }
        }
        logger.info("All images page loaded successfully");
        return this;
    }

    public boolean verifyPageElements() {
        logger.info("Verifying all images page elements");
        boolean screenPresent = isElementDisplayedByAccessibilityId(TestIDs.ALL_IMAGES_SCREEN);
        boolean backButtonPresent = isElementDisplayedByAccessibilityId(TestIDs.ALL_IMAGES_BACK_BUTTON);
        boolean gridOrEmptyPresent = isImagesGridDisplayed() || isEmptyStateDisplayed();
        boolean allElementsPresent =
                screenPresent
                        &&
                        (
                                backButtonPresent
                                        ||
                                        gridOrEmptyPresent
                        );
        logger.info("All images page elements verification: {}", allElementsPresent ? "PASSED" : "FAILED");
        return allElementsPresent;
    }

    public String getPageStateSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("All Images Page State:\n");
        summary.append("- Screen displayed: ").append(isDisplayed()).append("\n");
        summary.append("- Images grid displayed: ").append(isImagesGridDisplayed()).append("\n");
        summary.append("- Empty state displayed: ").append(isEmptyStateDisplayed()).append("\n");
        summary.append("- Visible images count: ").append(getVisibleImagesCount()).append("\n");
        summary.append("- Selected images count: ").append(getSelectedImagesCount()).append("\n");
        return summary.toString();
    }
}
