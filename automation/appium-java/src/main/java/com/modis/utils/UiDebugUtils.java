package com.modis.utils;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Debug utilities for flaky / not-found element issues.
 * Only called on failures to avoid slowing down normal runs.
 */
public final class UiDebugUtils {

    private static final Logger logger = LoggerUtil.getLogger(UiDebugUtils.class);
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private UiDebugUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Dump UI state to help diagnose locator/navigation issues on the next run:
     * - current package/activity (Android)
     * - visible texts + content-desc (best-effort)
     * - page source (best-effort) to target/ui-dumps/*.xml
     * - screenshot (best-effort) to target/ui-dumps/*.png
     *
     * This method NEVER throws; it only logs. The caller should still throw the original failure.
     */
    public static void dumpOnFailure(AppiumDriver driver, String reasonTag) {
        try {
            String ts = TS.format(LocalDateTime.now());
            String safeTag = sanitize(reasonTag);
            Path outDir = Paths.get("target", "ui-dumps");
            Files.createDirectories(outDir);

            Path base = outDir.resolve(ts + "_" + safeTag);

            logAndroidContext(driver);
            logVisibleStrings(driver);
            savePageSource(driver, base.resolveSibling(base.getFileName() + ".xml"));
            saveScreenshot(driver, base.resolveSibling(base.getFileName() + ".png"));
        } catch (Exception e) {
            logger.error("[UI-DUMP] Failed to dump UI state: {}", e.toString());
        }
    }

    private static void logAndroidContext(AppiumDriver driver) {
        if (!(driver instanceof AndroidDriver)) return;
        AndroidDriver android = (AndroidDriver) driver;
        try {
            logger.error("[UI-DUMP] currentPackage={}", android.getCurrentPackage());
        } catch (Exception e) {
            logger.error("[UI-DUMP] getCurrentPackage failed: {}", e.toString());
        }
        try {
            logger.error("[UI-DUMP] currentActivity={}", android.currentActivity());
        } catch (Exception e) {
            logger.error("[UI-DUMP] currentActivity failed: {}", e.toString());
        }
    }

    private static void logVisibleStrings(AppiumDriver driver) {
        // Keep logs bounded (avoid flooding CI output)
        int limit = 30;

        try {
            Set<String> texts = new LinkedHashSet<>();
            List<WebElement> nodes = driver.findElements(By.xpath("//*[@text!='']"));
            for (WebElement el : nodes) {
                if (texts.size() >= limit) break;
                String t = el.getAttribute("text");
                if (t != null && !t.trim().isEmpty()) texts.add(t.trim());
            }
            if (!texts.isEmpty()) {
                logger.error("[UI-DUMP] Visible texts (max {}): {}", limit, texts);
            }
        } catch (Exception e) {
            logger.error("[UI-DUMP] Collecting visible texts failed: {}", e.toString());
        }

        try {
            Set<String> descs = new LinkedHashSet<>();
            List<WebElement> nodes = driver.findElements(By.xpath("//*[@content-desc!='']"));
            for (WebElement el : nodes) {
                if (descs.size() >= limit) break;
                String d = el.getAttribute("content-desc");
                if (d != null && !d.trim().isEmpty()) descs.add(d.trim());
            }
            if (!descs.isEmpty()) {
                logger.error("[UI-DUMP] Visible content-desc (max {}): {}", limit, descs);
            }
        } catch (Exception e) {
            logger.error("[UI-DUMP] Collecting content-desc failed: {}", e.toString());
        }
    }

    private static void savePageSource(AppiumDriver driver, Path outFile) {
        try {
            String xml = driver.getPageSource();
            Files.write(outFile, xml.getBytes(StandardCharsets.UTF_8));
            logger.error("[UI-DUMP] pageSource saved: {}", outFile.toAbsolutePath());
        } catch (Exception e) {
            logger.error("[UI-DUMP] getPageSource failed: {}", e.toString());
        }
    }

    private static void saveScreenshot(AppiumDriver driver, Path outFile) {
        try {
            if (!(driver instanceof TakesScreenshot)) return;
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Files.write(outFile, png);
            logger.error("[UI-DUMP] screenshot saved: {}", outFile.toAbsolutePath());
        } catch (Exception e) {
            logger.error("[UI-DUMP] screenshot failed: {}", e.toString());
        }
    }

    private static String sanitize(String input) {
        if (input == null || input.isBlank()) return "unknown";
        return input.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}

