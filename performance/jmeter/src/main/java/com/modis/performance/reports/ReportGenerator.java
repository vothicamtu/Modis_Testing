package com.modis.performance.reports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.modis.performance.model.ComparisonResult;
import com.modis.performance.model.LabelComparison;
import com.modis.performance.model.RegressionDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ReportGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ReportGenerator.class);
    private static final ObjectMapper objectMapper = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }

    public static void generateConsoleReport(ComparisonResult comparisonResult) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("MODIS PERFORMANCE COMPARISON REPORT");
        System.out.println("=".repeat(80));
        System.out.printf("Timestamp: %s%n",
                comparisonResult.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        System.out.printf("Baseline: %s%n", comparisonResult.getBaselineFile());
        System.out.printf("Current:  %s%n", comparisonResult.getCurrentFile());
        System.out.println();
        ComparisonResult.ComparisonSummary summary = comparisonResult.getSummary();
        System.out.println("Summary:");
        System.out.printf("  Labels compared: %d%n", summary.getLabelsCompared());
        System.out.printf("  Labels with regressions: %d%n", summary.getLabelsWithRegressions());
        System.out.printf("  Total regressions: %d%n", summary.getTotalRegressions());
        System.out.println();
        if (summary.getTotalRegressions() == 0) {
            System.out.println("✅ No performance regressions detected!");
            System.out.println("=".repeat(80));
            return;
        }
        System.out.println("🚨 PERFORMANCE REGRESSIONS DETECTED:");
        System.out.println("-".repeat(80));
        for (Map.Entry<String, LabelComparison> entry : comparisonResult.getComparisons().entrySet()) {
            String label = entry.getKey();
            LabelComparison comparison = entry.getValue();
            if (comparison.hasRegressions()) {
                System.out.printf("%n📊 %s:%n", label);
                for (RegressionDetail regression : comparison.getRegressions()) {
                    System.out.printf("  ❌ %s:%n", regression.getMetric());
                    System.out.printf("     Baseline: %.2f%n", regression.getBaselineValue());
                    System.out.printf("     Current:  %.2f%n", regression.getCurrentValue());
                    System.out.printf("     Change:   %+.1f%% (threshold: %+.1f%%)%n",
                            regression.getChangePercent(), regression.getThreshold());
                    System.out.printf("     Severity: %s%n", regression.getSeverity());
                }
            }
        }
        System.out.println("\n" + "=".repeat(80));
    }

    public static void generateJsonReport(ComparisonResult comparisonResult, String outputFile)
            throws IOException {
        Path outputPath = Paths.get(outputFile);
        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }
        try (FileWriter writer = new FileWriter(outputPath.toFile())) {
            objectMapper.writeValue(writer, comparisonResult);
        }
        logger.info("JSON report saved to: {}", outputPath.toAbsolutePath());
        System.out.printf("JSON report saved to: %s%n", outputPath.toAbsolutePath());
    }

    public static void generateHtmlReport(ComparisonResult comparisonResult, String outputFile)
            throws IOException {
        Path outputPath = Paths.get(outputFile);
        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }
        String htmlContent = buildHtmlReport(comparisonResult);
        try (FileWriter writer = new FileWriter(outputPath.toFile())) {
            writer.write(htmlContent);
        }
        logger.info("HTML report saved to: {}", outputPath.toAbsolutePath());
        System.out.printf("HTML report saved to: %s%n", outputPath.toAbsolutePath());
    }

    private static String buildHtmlReport(ComparisonResult result) {
        StringBuilder html = new StringBuilder();
        html.append("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Modis Performance Comparison Report</title>
                    <style>
                        body { 
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
                            margin: 0; 
                            padding: 20px; 
                            background-color: #f5f5f5; 
                        }
                        .container { 
                            max-width: 1200px; 
                            margin: 0 auto; 
                            background: white; 
                            border-radius: 8px; 
                            box-shadow: 0 2px 10px rgba(0,0,0,0.1); 
                            overflow: hidden; 
                        }
                        .header { 
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); 
                            color: white; 
                            padding: 30px; 
                            text-align: center; 
                        }
                        .header h1 { margin: 0 0 10px 0; font-size: 2.5em; }
                        .header p { margin: 5px 0; opacity: 0.9; }
                        .content { padding: 30px; }
                        .summary { 
                            background: #e8f5e8; 
                            padding: 20px; 
                            border-radius: 8px; 
                            margin: 20px 0; 
                            border-left: 5px solid #4caf50; 
                        }
                        .summary.has-regressions { 
                            background: #ffe8e8; 
                            border-left-color: #f44336; 
                        }
                        .summary h2 { margin-top: 0; color: #333; }
                        .summary ul { margin: 10px 0; padding-left: 20px; }
                        .summary li { margin: 5px 0; }
                        .no-regression { 
                            background: #e8f5e8; 
                            padding: 30px; 
                            border-radius: 8px; 
                            text-align: center; 
                            margin: 20px 0; 
                        }
                        .no-regression h2 { color: #4caf50; margin: 0; font-size: 2em; }
                        .regression-section { margin: 30px 0; }
                        .regression-section h2 { color: #f44336; border-bottom: 2px solid #f44336; padding-bottom: 10px; }
                        .label-card { 
                            background: #fff; 
                            border: 1px solid #ddd; 
                            border-radius: 8px; 
                            margin: 20px 0; 
                            overflow: hidden; 
                            box-shadow: 0 2px 4px rgba(0,0,0,0.1); 
                        }
                        .label-header { 
                            background: #f8f9fa; 
                            padding: 15px 20px; 
                            border-bottom: 1px solid #ddd; 
                            font-weight: bold; 
                            color: #333; 
                        }
                        .metrics-table { 
                            width: 100%; 
                            border-collapse: collapse; 
                            margin: 0; 
                        }
                        .metrics-table th, .metrics-table td { 
                            padding: 12px 15px; 
                            text-align: left; 
                            border-bottom: 1px solid #eee; 
                        }
                        .metrics-table th { 
                            background-color: #f8f9fa; 
                            font-weight: 600; 
                            color: #555; 
                        }
                        .metrics-table tr:hover { background-color: #f8f9fa; }
                        .metric-good { color: #4caf50; font-weight: bold; }
                        .metric-bad { color: #f44336; font-weight: bold; }
                        .metric-neutral { color: #666; }
                        .severity-low { background: #fff3cd; color: #856404; padding: 4px 8px; border-radius: 4px; font-size: 0.8em; }
                        .severity-medium { background: #ffeaa7; color: #d63031; padding: 4px 8px; border-radius: 4px; font-size: 0.8em; }
                        .severity-high { background: #fab1a0; color: #d63031; padding: 4px 8px; border-radius: 4px; font-size: 0.8em; }
                        .severity-critical { background: #ff7675; color: white; padding: 4px 8px; border-radius: 4px; font-size: 0.8em; }
                        .footer { 
                            background: #f8f9fa; 
                            padding: 20px; 
                            text-align: center; 
                            color: #666; 
                            border-top: 1px solid #ddd; 
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                """);
        html.append(String.format("""
                        <div class="header">
                            <h1>🚀 Modis Performance Report</h1>
                            <p><strong>Generated:</strong> %s</p>
                            <p><strong>Baseline:</strong> %s</p>
                            <p><strong>Current:</strong> %s</p>
                        </div>
                        """,
                result.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                result.getBaselineFile(),
                result.getCurrentFile()
        ));
        html.append("<div class=\"content\">");
        ComparisonResult.ComparisonSummary summary = result.getSummary();
        String summaryClass = summary.getTotalRegressions() > 0 ? "summary has-regressions" : "summary";
        html.append(String.format("""
                        <div class="%s">
                            <h2>📊 Summary</h2>
                            <ul>
                                <li><strong>Labels compared:</strong> %d</li>
                                <li><strong>Labels with regressions:</strong> %d</li>
                                <li><strong>Total regressions:</strong> %d</li>
                            </ul>
                        </div>
                        """, summaryClass, summary.getLabelsCompared(),
                summary.getLabelsWithRegressions(), summary.getTotalRegressions()));
        if (summary.getTotalRegressions() == 0) {
            html.append("""
                    <div class="no-regression">
                        <h2>✅ No Performance Regressions Detected!</h2>
                        <p>All performance metrics are within acceptable thresholds.</p>
                    </div>
                    """);
        } else {
            html.append("<div class=\"regression-section\">");
            html.append("<h2>🚨 Performance Regressions Detected</h2>");
            for (Map.Entry<String, LabelComparison> entry : result.getComparisons().entrySet()) {
                String label = entry.getKey();
                LabelComparison comparison = entry.getValue();
                if (comparison.hasRegressions()) {
                    html.append(buildLabelCard(label, comparison));
                }
            }
            html.append("</div>");
        }
        html.append("</div>");
        html.append("""
                <div class="footer">
                    <p>Generated by Modis Performance Testing Framework</p>
                    <p>For more information, contact the QA team</p>
                </div>
                """);
        html.append("</div></body></html>");
        return html.toString();
    }

    private static String buildLabelCard(String label, LabelComparison comparison) {
        StringBuilder card = new StringBuilder();
        card.append(String.format("""
                        <div class="label-card">
                            <div class="label-header">
                                📈 %s (%d regression%s)
                            </div>
                            <table class="metrics-table">
                                <thead>
                                    <tr>
                                        <th>Metric</th>
                                        <th>Baseline</th>
                                        <th>Current</th>
                                        <th>Change</th>
                                        <th>Severity</th>
                                        <th>Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                        """, label, comparison.getRegressionCount(),
                comparison.getRegressionCount() == 1 ? "" : "s"));
        for (RegressionDetail regression : comparison.getRegressions()) {
            String severityClass = "severity-" + regression.getSeverity().name().toLowerCase();
            String changeClass = regression.isPerformanceDegradation() ? "metric-bad" : "metric-good";
            card.append(String.format("""
                            <tr>
                                <td><strong>%s</strong></td>
                                <td>%.2f</td>
                                <td>%.2f</td>
                                <td class="%s">%+.1f%%</td>
                                <td><span class="%s">%s</span></td>
                                <td class="metric-bad">❌ Regression</td>
                            </tr>
                            """,
                    regression.getMetric(),
                    regression.getBaselineValue(),
                    regression.getCurrentValue(),
                    changeClass,
                    regression.getChangePercent(),
                    severityClass,
                    regression.getSeverity().getDisplayName()
            ));
        }
        card.append("</tbody></table></div>");
        return card.toString();
    }

    public static void generateCsvReport(ComparisonResult comparisonResult, String outputFile)
            throws IOException {
        Path outputPath = Paths.get(outputFile);
        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }
        try (FileWriter writer = new FileWriter(outputPath.toFile())) {
            writer.write("Label,Metric,BaselineValue,CurrentValue,ChangePercent,Threshold,IsRegression,Severity\n");
            for (Map.Entry<String, LabelComparison> entry : comparisonResult.getComparisons().entrySet()) {
                String label = entry.getKey();
                LabelComparison comparison = entry.getValue();
                writeMetricRow(writer, label, "avgResponseTime", comparison);
                writeMetricRow(writer, label, "percentile95", comparison);
                writeMetricRow(writer, label, "errorRate", comparison);
                writeMetricRow(writer, label, "throughput", comparison);
            }
        }
        logger.info("CSV report saved to: {}", outputPath.toAbsolutePath());
        System.out.printf("CSV report saved to: %s%n", outputPath.toAbsolutePath());
    }

    private static void writeMetricRow(FileWriter writer, String label, String metric,
                                       LabelComparison comparison) throws IOException {
        double baselineValue = getMetricValue(comparison.getBaseline(), metric);
        double currentValue = getMetricValue(comparison.getCurrent(), metric);
        double changePercent = comparison.getChangePercent(metric);
        RegressionDetail regression = comparison.getRegressionForMetric(metric);
        boolean isRegression = regression != null;
        String severity = isRegression ? regression.getSeverity().name() : "NONE";
        double threshold = isRegression ? regression.getThreshold() : 0.0;
        writer.write(String.format("%s,%s,%.2f,%.2f,%.2f,%.2f,%s,%s\n",
                label, metric, baselineValue, currentValue, changePercent, threshold,
                isRegression, severity));
    }

    private static double getMetricValue(com.modis.performance.model.PerformanceMetrics metrics, String metricName) {
        return switch (metricName) {
            case "avgResponseTime" -> metrics.getAvgResponseTime();
            case "percentile95" -> metrics.getPercentile95();
            case "errorRate" -> metrics.getErrorRate();
            case "throughput" -> metrics.getThroughput();
            default -> 0.0;
        };
    }
}