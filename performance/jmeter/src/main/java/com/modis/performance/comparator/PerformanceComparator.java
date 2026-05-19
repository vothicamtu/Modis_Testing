package com.modis.performance.comparator;

import com.modis.performance.model.*;
import com.modis.performance.parsers.JTLResultsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Compares performance metrics and detects regressions
 */
public class PerformanceComparator {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceComparator.class);
    
    private final Map<String, Double> regressionThresholds;
    
    // Default regression thresholds (percentage)
    private static final Map<String, Double> DEFAULT_THRESHOLDS = Map.of(
        "avgResponseTime", 20.0,    // 20% increase is regression
        "percentile95", 25.0,       // 25% increase is regression  
        "errorRate", 50.0,          // 50% increase is regression
        "throughput", -15.0         // 15% decrease is regression
    );
    
    /**
     * Constructor with default thresholds
     */
    public PerformanceComparator() {
        this.regressionThresholds = new HashMap<>(DEFAULT_THRESHOLDS);
    }
    
    /**
     * Constructor with custom thresholds
     * @param customThresholds Custom regression thresholds
     */
    public PerformanceComparator(Map<String, Double> customThresholds) {
        this.regressionThresholds = new HashMap<>(DEFAULT_THRESHOLDS);
        if (customThresholds != null) {
            this.regressionThresholds.putAll(customThresholds);
        }
    }
    
    /**
     * Compare two sets of performance metrics
     * @param baseline Baseline metrics
     * @param current Current metrics
     * @return Comparison result
     */
    public LabelComparison compareMetrics(PerformanceMetrics baseline, PerformanceMetrics current) {
        LabelComparison comparison = new LabelComparison(baseline, current);
        
        // Calculate percentage changes for each metric
        calculateMetricChanges(comparison, baseline, current);
        
        // Detect regressions based on thresholds
        detectRegressions(comparison, baseline, current);
        
        return comparison;
    }
    
    /**
     * Compare two JTL result files
     * @param baselineFile Path to baseline JTL file
     * @param currentFile Path to current JTL file
     * @return Complete comparison result
     * @throws IOException If files cannot be read
     */
    public ComparisonResult compareResults(String baselineFile, String currentFile) throws IOException {
        logger.info("Comparing performance results: {} vs {}", baselineFile, currentFile);
        
        // Parse both files
        Map<String, PerformanceMetrics> baselineMetrics = JTLResultsParser.parseJTLFile(baselineFile);
        Map<String, PerformanceMetrics> currentMetrics = JTLResultsParser.parseJTLFile(currentFile);
        
        // Create comparison result
        ComparisonResult result = new ComparisonResult(baselineFile, currentFile);
        
        // Find common labels between baseline and current
        Set<String> commonLabels = getCommonLabels(baselineMetrics, currentMetrics);
        
        if (commonLabels.isEmpty()) {
            logger.warn("No common labels found between baseline and current results");
            return result;
        }
        
        logger.info("Comparing {} common labels", commonLabels.size());
        
        // Compare metrics for each common label
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
    
    /**
     * Calculate percentage changes for all metrics
     * @param comparison Comparison object to populate
     * @param baseline Baseline metrics
     * @param current Current metrics
     */
    private void calculateMetricChanges(LabelComparison comparison, 
                                      PerformanceMetrics baseline, 
                                      PerformanceMetrics current) {
        
        // Average response time
        double avgChange = calculatePercentageChange(
            baseline.getAvgResponseTime(), current.getAvgResponseTime());
        comparison.addChange("avgResponseTime", avgChange);
        
        // 95th percentile
        double p95Change = calculatePercentageChange(
            baseline.getPercentile95(), current.getPercentile95());
        comparison.addChange("percentile95", p95Change);
        
        // Error rate
        double errorChange = calculatePercentageChange(
            baseline.getErrorRate(), current.getErrorRate());
        comparison.addChange("errorRate", errorChange);
        
        // Throughput
        double throughputChange = calculatePercentageChange(
            baseline.getThroughput(), current.getThroughput());
        comparison.addChange("throughput", throughputChange);
        
        // Min response time
        double minChange = calculatePercentageChange(
            baseline.getMinResponseTime(), current.getMinResponseTime());
        comparison.addChange("minResponseTime", minChange);
        
        // Max response time
        double maxChange = calculatePercentageChange(
            baseline.getMaxResponseTime(), current.getMaxResponseTime());
        comparison.addChange("maxResponseTime", maxChange);
    }
    
    /**
     * Detect regressions based on thresholds
     * @param comparison Comparison object to populate
     * @param baseline Baseline metrics
     * @param current Current metrics
     */
    private void detectRegressions(LabelComparison comparison, 
                                 PerformanceMetrics baseline, 
                                 PerformanceMetrics current) {
        
        Map<String, Double> changes = comparison.getChanges();
        
        for (Map.Entry<String, Double> entry : changes.entrySet()) {
            String metric = entry.getKey();
            double changePercent = entry.getValue();
            
            Double threshold = regressionThresholds.get(metric);
            if (threshold == null) {
                continue; // No threshold defined for this metric
            }
            
            // Check if change exceeds threshold
            boolean isRegression = false;
            if (threshold > 0 && changePercent > threshold) {
                isRegression = true; // Positive threshold, positive change is bad
            } else if (threshold < 0 && changePercent < threshold) {
                isRegression = true; // Negative threshold, negative change is bad
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
    
    /**
     * Calculate percentage change between two values
     * @param baseline Baseline value
     * @param current Current value
     * @return Percentage change
     */
    private double calculatePercentageChange(double baseline, double current) {
        if (baseline == 0.0) {
            return current == 0.0 ? 0.0 : 100.0; // Avoid division by zero
        }
        return ((current - baseline) / baseline) * 100.0;
    }
    
    /**
     * Get metric value by name from PerformanceMetrics object
     * @param metrics Performance metrics object
     * @param metricName Metric name
     * @return Metric value
     */
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
    
    /**
     * Find common labels between baseline and current metrics
     * @param baselineMetrics Baseline metrics map
     * @param currentMetrics Current metrics map
     * @return Set of common labels
     */
    private Set<String> getCommonLabels(Map<String, PerformanceMetrics> baselineMetrics,
                                       Map<String, PerformanceMetrics> currentMetrics) {
        Set<String> commonLabels = baselineMetrics.keySet();
        commonLabels.retainAll(currentMetrics.keySet());
        return commonLabels;
    }
    
    /**
     * Get regression thresholds
     * @return Map of metric name to threshold percentage
     */
    public Map<String, Double> getRegressionThresholds() {
        return new HashMap<>(regressionThresholds);
    }
    
    /**
     * Set regression threshold for a specific metric
     * @param metric Metric name
     * @param threshold Threshold percentage
     */
    public void setRegressionThreshold(String metric, double threshold) {
        regressionThresholds.put(metric, threshold);
        logger.debug("Set regression threshold for '{}': {}%", metric, threshold);
    }
    
    /**
     * Update multiple regression thresholds
     * @param thresholds Map of metric name to threshold percentage
     */
    public void updateRegressionThresholds(Map<String, Double> thresholds) {
        if (thresholds != null) {
            regressionThresholds.putAll(thresholds);
            logger.info("Updated regression thresholds: {}", thresholds);
        }
    }
}