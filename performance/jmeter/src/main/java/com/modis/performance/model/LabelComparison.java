package com.modis.performance.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Comparison result for a specific label/endpoint
 */
public class LabelComparison {
    
    private PerformanceMetrics baseline;
    private PerformanceMetrics current;
    private Map<String, Double> changes = new HashMap<>();
    private List<RegressionDetail> regressions = new ArrayList<>();
    
    // Constructors
    public LabelComparison() {}
    
    public LabelComparison(PerformanceMetrics baseline, PerformanceMetrics current) {
        this.baseline = baseline;
        this.current = current;
    }
    
    // Getters and Setters
    public PerformanceMetrics getBaseline() {
        return baseline;
    }
    
    public void setBaseline(PerformanceMetrics baseline) {
        this.baseline = baseline;
    }
    
    public PerformanceMetrics getCurrent() {
        return current;
    }
    
    public void setCurrent(PerformanceMetrics current) {
        this.current = current;
    }
    
    public Map<String, Double> getChanges() {
        return changes;
    }
    
    public void setChanges(Map<String, Double> changes) {
        this.changes = changes;
    }
    
    public List<RegressionDetail> getRegressions() {
        return regressions;
    }
    
    public void setRegressions(List<RegressionDetail> regressions) {
        this.regressions = regressions;
    }
    
    // Business Methods
    
    /**
     * Add a metric change percentage
     * @param metric The metric name
     * @param changePercent The percentage change
     */
    public void addChange(String metric, double changePercent) {
        changes.put(metric, Math.round(changePercent * 100.0) / 100.0);
    }
    
    /**
     * Add a regression detail
     * @param regression The regression detail
     */
    public void addRegression(RegressionDetail regression) {
        regressions.add(regression);
    }
    
    /**
     * Check if this comparison has any regressions
     * @return true if regressions exist, false otherwise
     */
    public boolean hasRegressions() {
        return !regressions.isEmpty();
    }
    
    /**
     * Get the number of regressions
     * @return Number of regressions
     */
    public int getRegressionCount() {
        return regressions.size();
    }
    
    /**
     * Get change percentage for a specific metric
     * @param metric The metric name
     * @return Change percentage, or 0.0 if not found
     */
    public double getChangePercent(String metric) {
        return changes.getOrDefault(metric, 0.0);
    }
    
    /**
     * Check if a specific metric has regressed
     * @param metric The metric name
     * @return true if metric has regressed, false otherwise
     */
    public boolean hasMetricRegressed(String metric) {
        return regressions.stream()
                .anyMatch(regression -> regression.getMetric().equals(metric));
    }
    
    /**
     * Get regression details for a specific metric
     * @param metric The metric name
     * @return RegressionDetail if found, null otherwise
     */
    public RegressionDetail getRegressionForMetric(String metric) {
        return regressions.stream()
                .filter(regression -> regression.getMetric().equals(metric))
                .findFirst()
                .orElse(null);
    }
    
    @Override
    public String toString() {
        return String.format(
            "LabelComparison{baseline=%s, current=%s, changes=%s, regressions=%d}",
            baseline != null ? baseline.getSamples() + " samples" : "null",
            current != null ? current.getSamples() + " samples" : "null",
            changes.size(),
            regressions.size()
        );
    }
}