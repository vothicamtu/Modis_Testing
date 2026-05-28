package com.modis.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ExtentManager {

    private static ExtentReports extent;

    public static ExtentReports getInstance() {

        if (extent == null) {

            String timestamp =
                    new SimpleDateFormat("yyyyMMdd_HHmmss")
                            .format(new Date());

            String reportPath =
                    "reports/ModisReport_" +
                            timestamp +
                            ".html";

            ExtentSparkReporter sparkReporter =
                    new ExtentSparkReporter(reportPath);

            sparkReporter.config().setReportName(
                    "MODIS AUTOMATION REPORT"
            );

            sparkReporter.config().setDocumentTitle(
                    "MODIS TEST EXECUTION"
            );

            extent = new ExtentReports();

            extent.attachReporter(sparkReporter);

            extent.setSystemInfo(
                    "Framework",
                    "Appium + TestNG"
            );

            extent.setSystemInfo(
                    "Platform",
                    System.getProperty(
                            "platform",
                            "Android"
                    )
            );

            extent.setSystemInfo(
                    "Environment",
                    "QA"
            );
        }

        return extent;
    }
}