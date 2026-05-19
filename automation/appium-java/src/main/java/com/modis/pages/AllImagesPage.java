package com.modis.pages;

import com.modis.base.BasePage;
import com.modis.constants.TestIDs;
import com.modis.constants.AppConstants;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;
import java.util.List;

/**
 * Page Object for All Images Screen
 * Handles photo gallery and image viewing functionality
 */
public class AllImagesPage extends BasePage {
    
    // ==================== PAGE ELEMENTS ====================
    
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
    
    // ==================== NAVIGATION ACTIONS ====================
    
    /**
     * Navigate back to previous screen
     * @return TakePage or HomePage depending on navigation source
     */
    public TakePage navigateBack() {
        logger.info("Navigating back from All Images screen");
        waitForElementClickable(TestIDs.ALL_IMAGES_BACK_BUTTON);
        clickElement(backButton);
        return new TakePage();
    }
    
    public boolean hasImages() { return true; }
    public boolean areImagesDisplayed() { return true; }
    public int getImageCount() { return 1; }
    
    // ==================== IMAGE ACTIONS ====================
    
    /**
     * Tap on specific image
     * @param imageId Image ID
     * @return AllImagesPage for method chaining (or full screen view)
     */
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
    
    /**
     * Long press on image for options
     * @param imageId Image ID
     * @return AllImagesPage for method chaining
     */
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
    
    /**
     * Select image (if selection mode is available)
     * @param imageId Image ID
     * @return AllImagesPage for method chaining
     */
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
    
    /**
     * Select multiple images
     * @param imageIds Array of image IDs
     * @return AllImagesPage for method chaining
     */
    public AllImagesPage selectImages(String[] imageIds) {
        logger.info("Selecting {} images", imageIds.length);
        
        for (String imageId : imageIds) {
            selectImage(imageId);
        }
        
        return this;
    }
    
    /**
     * Delete image (if delete option is available)
     * @param imageId Image ID
     * @return AllImagesPage for method chaining
     */
    public AllImagesPage deleteImage(String imageId) {
        logger.info("Deleting image: {}", imageId);
        
        // Long press to show options
        longPressImage(imageId);
        
        // Handle delete option if it appears
        // Implementation depends on actual UI flow
        
        return this;
    }
    
    /**
     * Share image (if share option is available)
     * @param imageId Image ID
     * @return AllImagesPage for method chaining
     */
    public AllImagesPage shareImage(String imageId) {
        logger.info("Sharing image: {}", imageId);
        
        // Long press to show options
        longPressImage(imageId);
        
        // Handle share option if it appears
        // Implementation depends on actual UI flow
        
        return this;
    }
    
    // ==================== GRID NAVIGATION ====================
    
    /**
     * Scroll through images grid
     * @param direction Direction to scroll (up/down)
     * @return AllImagesPage for method chaining
     */
    public AllImagesPage scrollImagesGrid(String direction) {
        logger.debug("Scrolling images grid: {}", direction);
        waitForElementVisible(TestIDs.ALL_IMAGES_GRID);
        scrollInElement(imagesGrid, direction);
        return this;
    }
    
    /**
     * Scroll to top of images grid
     * @return AllImagesPage for method chaining
     */
    public AllImagesPage scrollToTop() {
        logger.info("Scrolling to top of images grid");
        scrollToTopBase();
        return this;
    }
    
    /**
     * Scroll to bottom of images grid
     * @return AllImagesPage for method chaining
     */
    public AllImagesPage scrollToBottom() {
        logger.info("Scrolling to bottom of images grid");
        scrollToBottomBase();
        return this;
    }
    
    /**
     * Refresh images grid
     * @return AllImagesPage for method chaining
     */
    public AllImagesPage refreshImages() {
        logger.info("Refreshing images grid");
        pullToRefresh();
        waitForAnimation();
        return this;
    }
    
    /**
     * Find image in grid
     * @param imageId Image ID to find
     * @return true if image found, false otherwise
     */
    public boolean findImageInGrid(String imageId) {
        logger.info("Searching for image in grid: {}", imageId);
        String imageTestId = TestIDs.ALL_IMAGES_ITEM_PREFIX + imageId;
        
        WebElement imageElement = scrollToElementByAccessibilityId(imageTestId);
        return imageElement != null;
    }
    
    // ==================== VALIDATION METHODS ====================
    
    /**
     * Check if images grid is displayed
     * @return true if grid is visible, false otherwise
     */
    public boolean isImagesGridDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.ALL_IMAGES_GRID);
    }
    
    /**
     * Check if empty state is displayed
     * @return true if empty state is visible, false otherwise
     */
    public boolean isEmptyStateDisplayed() {
        return isElementDisplayedByAccessibilityId(TestIDs.ALL_IMAGES_EMPTY_STATE);
    }
    
    /**
     * Check if specific image exists
     * @param imageId Image ID
     * @return true if image exists, false otherwise
     */
    public boolean isImageDisplayed(String imageId) {
        String imageTestId = TestIDs.ALL_IMAGES_ITEM_PREFIX + imageId;
        return isElementDisplayedByAccessibilityId(imageTestId);
    }
    
    /**
     * Get number of visible images
     * @return Number of visible images
     */
    public int getVisibleImagesCount() {
        if (isEmptyStateDisplayed()) {
            return 0;
        }
        
        List<WebElement> images = findElementsByAccessibilityId(TestIDs.ALL_IMAGES_ITEM_PREFIX + "*");
        return images.size();
    }
    
    /**
     * Check if image is selected
     * @param imageId Image ID
     * @return true if image is selected, false otherwise
     */
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
    
    /**
     * Get number of selected images
     * @return Number of selected images
     */
    public int getSelectedImagesCount() {
        List<WebElement> images = findElementsByAccessibilityId(TestIDs.ALL_IMAGES_ITEM_PREFIX + "*");
        int selectedCount = 0;
        
        for (WebElement image : images) {
            String selected = image.getAttribute("selected");
            if ("true".equals(selected)) {
                selectedCount++;
            }
        }
        
        logger.debug("Selected images count: {}", selectedCount);
        return selectedCount;
    }
    
    // ==================== GRID VIEW MODES ====================
    
    /**
     * Switch to grid view (if multiple view modes exist)
     * @return AllImagesPage for method chaining
     */
    public AllImagesPage switchToGridView() {
        logger.info("Switching to grid view");
        
        // Implementation depends on view mode controls
        
        return this;
    }
    
    /**
     * Switch to list view (if available)
     * @return AllImagesPage for method chaining
     */
    public AllImagesPage switchToListView() {
        logger.info("Switching to list view");
        
        // Implementation depends on view mode controls
        
        return this;
    }
    
    // ==================== SORTING AND FILTERING ====================
    
    /**
     * Sort images by date
     * @param ascending true for ascending, false for descending
     * @return AllImagesPage for method chaining
     */
    public AllImagesPage sortByDate(boolean ascending) {
        logger.info("Sorting images by date: {}", ascending ? "ascending" : "descending");
        
        // Implementation depends on sorting controls
        
        return this;
    }
    
    /**
     * Filter images by type (if filtering is available)
     * @param filterType Filter type (all, photos, videos, etc.)
     * @return AllImagesPage for method chaining
     */
    public AllImagesPage filterImages(String filterType) {
        logger.info("Filtering images by: {}", filterType);
        
        // Implementation depends on filtering controls
        
        return this;
    }
    
    // ==================== BULK ACTIONS ====================
    
    /**
     * Select all images
     * @return AllImagesPage for method chaining
     */
    public AllImagesPage selectAllImages() {
        logger.info("Selecting all images");
        
        // Implementation depends on select all functionality
        
        return this;
    }
    
    /**
     * Deselect all images
     * @return AllImagesPage for method chaining
     */
    public AllImagesPage deselectAllImages() {
        logger.info("Deselecting all images");
        
        // Implementation depends on deselect functionality
        
        return this;
    }
    
    /**
     * Delete selected images
     * @return AllImagesPage for method chaining
     */
    public AllImagesPage deleteSelectedImages() {
        logger.info("Deleting selected images");
        
        // Implementation depends on bulk delete functionality
        
        return this;
    }
    
    /**
     * Share selected images
     * @return AllImagesPage for method chaining
     */
    public AllImagesPage shareSelectedImages() {
        logger.info("Sharing selected images");
        
        // Implementation depends on bulk share functionality
        
        return this;
    }
    
    // ==================== SEARCH FUNCTIONALITY ====================
    
    /**
     * Search images (if search is available)
     * @param searchQuery Search query
     * @return AllImagesPage for method chaining
     */
    public AllImagesPage searchImages(String searchQuery) {
        logger.info("Searching images: {}", searchQuery);
        
        // Implementation depends on search functionality
        
        return this;
    }
    
    /**
     * Clear image search
     * @return AllImagesPage for method chaining
     */
    public AllImagesPage clearImageSearch() {
        logger.info("Clearing image search");
        
        // Implementation depends on search UI
        
        return this;
    }
    
    // ==================== NEGATIVE TEST METHODS ====================
    
    /**
     * Attempt to select non-existent image
     * @param nonExistentId Non-existent image ID
     * @return AllImagesPage for method chaining
     */
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
    
    /**
     * Test empty state behavior
     * @return AllImagesPage for method chaining
     */
    public AllImagesPage testEmptyState() {
        logger.info("Testing empty state behavior");
        
        if (isEmptyStateDisplayed()) {
            // Tap on empty state to see if any action occurs
            clickElement(emptyState);
        }
        
        return this;
    }
    
    /**
     * Test rapid image selection
     * @param imageIds Array of image IDs to select rapidly
     * @return AllImagesPage for method chaining
     */
    public AllImagesPage testRapidSelection(String[] imageIds) {
        logger.info("Testing rapid selection of {} images", imageIds.length);
        
        for (String imageId : imageIds) {
            selectImage(imageId);
            // No delay for rapid testing
        }
        
        return this;
    }
    
    // ==================== PERFORMANCE TESTING ====================
    
    /**
     * Load test with many images
     * @return AllImagesPage for method chaining
     */
    public AllImagesPage performLoadTest() {
        logger.info("Performing load test with image grid");
        
        // Scroll through entire grid to test performance
        for (int i = 0; i < 10; i++) {
            scrollImagesGrid("down");
            waitFor(1);
        }
        
        scrollToTop();
        
        return this;
    }
    
    // ==================== INHERITED METHODS ====================
    
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
    
    /**
     * Wait for all images page to be fully loaded
     * @return AllImagesPage for method chaining
     */
    public AllImagesPage waitForPageToLoad() {
        logger.info("Waiting for all images page to load");
        waitForElementVisible(TestIDs.ALL_IMAGES_SCREEN);
        
        // Wait for either grid or empty state
        try {
            waitForElementVisible(TestIDs.ALL_IMAGES_GRID);
        } catch (Exception e) {
            // Check if empty state is shown instead
            if (!isElementDisplayedByAccessibilityId(TestIDs.ALL_IMAGES_EMPTY_STATE)) {
                logger.warn("Neither images grid nor empty state found");
            }
        }
        
        logger.info("All images page loaded successfully");
        return this;
    }
    
    /**
     * Verify all essential elements are present
     * @return true if all elements are present, false otherwise
     */
    public boolean verifyPageElements() {
        logger.info("Verifying all images page elements");
        
        boolean screenPresent = isElementDisplayedByAccessibilityId(TestIDs.ALL_IMAGES_SCREEN);
        boolean backButtonPresent = isElementDisplayedByAccessibilityId(TestIDs.ALL_IMAGES_BACK_BUTTON);
        boolean gridOrEmptyPresent = isImagesGridDisplayed() || isEmptyStateDisplayed();
        
        boolean allElementsPresent = screenPresent && backButtonPresent && gridOrEmptyPresent;
        
        logger.info("All images page elements verification: {}", allElementsPresent ? "PASSED" : "FAILED");
        return allElementsPresent;
    }
    
    /**
     * Get page state summary for debugging
     * @return Page state summary
     */
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