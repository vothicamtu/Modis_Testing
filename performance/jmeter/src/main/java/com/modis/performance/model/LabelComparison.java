package com.modis.performance.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LabelComparison {
    private PerformanceMetrics baseline;
    private PerformanceMetrics current;
    private Map<String, Double> changes = new HashMap<>();
    private List<RegressionDetail> regressions = new ArrayList<>();

    public LabelComparison() {
    }

    public LabelComparison(PerformanceMetrics baseline, PerformanceMetrics current) {
        this.baseline = baseline;
        this.current = current;
    }

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

    public void addChange(String metric, double changePercent) {
        changes.put(metric, Math.round(changePercent * 100.0) / 100.0);
    }

    public void addRegression(RegressionDetail regression) {
        regressions.add(regression);
    }

    public boolean hasRegressions() {
        return !regressions.isEmpty();
    }

    public int getRegressionCount() {
        return regressions.size();
    }

    public double getChangePercent(String metric) {
        return changes.getOrDefault(metric, 0.0);
    }

    public boolean hasMetricRegressed(String metric) {
        return regressions.stream()
                .anyMatch(regression -> regression.getMetric().equals(metric));
    }

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