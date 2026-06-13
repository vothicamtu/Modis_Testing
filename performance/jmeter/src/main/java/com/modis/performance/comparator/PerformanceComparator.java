package com.modis.performance.comparator;

import com.modis.performance.model.*;
import com.modis.performance.parsers.JTLResultsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PerformanceComparator {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceComparator.class);
    private final Map<String, Double> regressionThresholds;
    private static final Map<String, Double> DEFAULT_THRESHOLDS = Map.of(
            "avgResponseTime", 20.0,
            "percentile95", 25.0,
            "errorRate", 50.0,
            "throughput", -15.0
    );

    public PerformanceComparator() {
        this.regressionThresholds = new HashMap<>(DEFAULT_THRESHOLDS);
    }

    public PerformanceComparator(Map<String, Double> customThresholds) {
        this.regressionThresholds = new HashMap<>(DEFAULT_THRESHOLDS);
        if (customThresholds != null) {
            this.regressionThresholds.putAll(customThresholds);
        }
    }

    public LabelComparison compareMetrics(PerformanceMetrics baseline, PerformanceMetrics current) {
        LabelComparison comparison = new LabelComparison(baseline, current);
        calculateMetricChanges(comparison, baseline, current);
        detectRegressions(comparison, baseline, current);
        return comparison;
    }

    public ComparisonResult compareResults(String baselineFile, String currentFile) throws IOException {
        logger.info("Comparing performance results: {} vs {}", baselineFile, currentFile);
        Map<String, PerformanceMetrics> baselineMetrics = JTLResultsParser.parseJTLFile(baselineFile);
        Map<String, PerformanceMetrics> currentMetrics = JTLResultsParser.parseJTLFile(currentFile);
        ComparisonResult result = new ComparisonResult(baselineFile, currentFile);
        Set<String> commonLabels = getCommonLabels(baselineMetrics, currentMetrics);
        if (commonLabels.isEmpty()) {
            logger.warn("No common labels found between baseline and current results");
            return result;
        }
        logger.info("Comparing {} common labels", commonLabels.size());
        for (String label : commonLabels) {
            PerformanceMetrics baseline = baselineMetrics.get(label);
            PerformanceMetrics current = currentMetrics.get(label);
            LabelComparison labelComparison = compareMetrics(baseline, current);
            result.addComparison(label, labelComparison);
            logger.debug("Compared label '{}': {} regressions found",
                    label, labelComparison.getRegressionCount());
        }
        logger.info("Comparison completed: {} total regressions across {} labels",
                result.getSummary().getTotalRegressions(),
                result.getSummary().getLabelsWithRegressions());
        return result;
    }

    private void calculateMetricChanges(LabelComparison comparison,
                                        PerformanceMetrics baseline,
                                        PerformanceMetrics current) {
        double avgChange = calculatePercentageChange(
                baseline.getAvgResponseTime(), current.getAvgResponseTime());
        comparison.addChange("avgResponseTime", avgChange);
        double p95Change = calculatePercentageChange(
                baseline.getPercentile95(), current.getPercentile95());
        comparison.addChange("percentile95", p95Change);
        double errorChange = calculatePercentageChange(
                baseline.getErrorRate(), current.getErrorRate());
        comparison.addChange("errorRate", errorChange);
        double throughputChange = calculatePercentageChange(
                baseline.getThroughput(), current.getThroughput());
        comparison.addChange("throughput", throughputChange);
        double minChange = calculatePercentageChange(
                baseline.getMinResponseTime(), current.getMinResponseTime());
        comparison.addChange("minResponseTime", minChange);
        double maxChange = calculatePercentageChange(
                baseline.getMaxResponseTime(), current.getMaxResponseTime());
        comparison.addChange("maxResponseTime", maxChange);
    }

    private void detectRegressions(LabelComparison comparison,
                                   PerformanceMetrics baseline,
                                   PerformanceMetrics current) {
        Map<String, Double> changes = comparison.getChanges();
        for (Map.Entry<String, Double> entry : changes.entrySet()) {
            String metric = entry.getKey();
            double changePercent = entry.getValue();
            Double threshold = regressionThresholds.get(metric);
            if (threshold == null) {
                continue;
            }
            boolean isRegression = false;
            if (threshold > 0 && changePercent > threshold) {
                isRegression = true;
            } else if (threshold < 0 && changePercent < threshold) {
                isRegression = true;
            }
            if (isRegression) {
                double baselineValue = getMetricValue(baseline, metric);
                double currentValue = getMetricValue(current, metric);
                RegressionDetail regression = new RegressionDetail(
                        metric, changePercent, threshold, baselineValue, currentValue
                );
                regression.roundValues();
                comparison.addRegression(regression);
                logger.debug("Regression detected for metric '{}': {}% change (threshold: {}%)",
                        metric, changePercent, threshold);
            }
        }
    }

    private double calculatePercentageChange(double baseline, double current) {
        if (baseline == 0.0) {
            return current == 0.0 ? 0.0 : 100.0;
        }
        return ((current - baseline) / baseline) * 100.0;
    }

    private double getMetricValue(PerformanceMetrics metrics, String metricName) {
        return switch (metricName) {
            case "avgResponseTime" -> metrics.getAvgResponseTime();
            case "percentile95" -> metrics.getPercentile95();
            case "errorRate" -> metrics.getErrorRate();
            case "throughput" -> metrics.getThroughput();
            case "minResponseTime" -> metrics.getMinResponseTime();
            case "maxResponseTime" -> metrics.getMaxResponseTime();
            default -> 0.0;
        };
    }

    private Set<String> getCommonLabels(Map<String, PerformanceMetrics> baselineMetrics,
                                        Map<String, PerformanceMetrics> currentMetrics) {
        Set<String> commonLabels = baselineMetrics.keySet();
        commonLabels.retainAll(currentMetrics.keySet());
        return commonLabels;
    }

    public Map<String, Double> getRegressionThresholds() {
        return new HashMap<>(regressionThresholds);
    }

    public void setRegressionThreshold(String metric, double threshold) {
        regressionThresholds.put(metric, threshold);
        logger.debug("Set regression threshold for '{}': {}%", metric, threshold);
    }

    public void updateRegressionThresholds(Map<String, Double> thresholds) {
        if (thresholds != null) {
            regressionThresholds.putAll(thresholds);
            logger.info("Updated regression thresholds: {}", thresholds);
        }
    }
}