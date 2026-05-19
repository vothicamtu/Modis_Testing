package com.modis.performance.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Result of performance metrics comparison
 */
public class ComparisonResult {
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String baselineFile;
    private String currentFile;
    private Map<String, LabelComparison> comparisons = new HashMap<>();
    private ComparisonSummary summary = new ComparisonSummary();
    
    // Constructors
    public ComparisonResult() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ComparisonResult(String baselineFile, String currentFile) {
        this();
        this.baselineFile = baselineFile;
        this.currentFile = currentFile;
    }
    
    // Getters and Setters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getBaselineFile() {
        return baselineFile;
    }
    
    public void setBaselineFile(String baselineFile) {
        this.baselineFile = baselineFile;
    }
    
    public String getCurrentFile() {
        return currentFile;
    }
    
    public void setCurrentFile(String currentFile) {
        this.currentFile = currentFile;
    }
    
    public Map<String, LabelComparison> getComparisons() {
        return comparisons;
    }
    
    public void setComparisons(Map<String, LabelComparison> comparisons) {
        this.comparisons = comparisons;
    }
    
    public ComparisonSummary getSummary() {
        return summary;
    }
    
    public void setSummary(ComparisonSummary summary) {
        this.summary = summary;
    }
    
    // Business Methods
    
    /**
     * Add a label comparison result
     * @param label The label name
     * @param comparison The comparison result
     */
    public void addComparison(String label, LabelComparison comparison) {
        comparisons.put(label, comparison);
        updateSummary();
    }
    
    /**
     * Update summary statistics based on current comparisons
     */
    private void updateSummary() {
        summary.setLabelsCompared(comparisons.size());
        
        int labelsWithRegressions = 0;
        int totalRegressions = 0;
        
        for (LabelComparison comparison : comparisons.values()) {
            if (!comparison.getRegressions().isEmpty()) {
                labelsWithRegressions++;
                totalRegressions += comparison.getRegressions().size();
            }
        }
        
        summary.setLabelsWithRegressions(labelsWithRegressions);
        summary.setTotalRegressions(totalRegressions);
    }
    
    /**
     * Check if any regressions were detected
     * @return true if regressions found, false otherwise
     */
    public boolean hasRegressions() {
        return summary.getTotalRegressions() > 0;
    }
    
    /**
     * Get all regressions across all labels
     * @return List of all regression details
     */
    public List<RegressionDetail> getAllRegressions() {
        return comparisons.values().stream()
                .flatMap(comparison -> comparison.getRegressions().stream())
                .toList();
    }
    
    /**
     * Inner class for comparison summary
     */
    public static class ComparisonSummary {
        private int labelsCompared = 0;
        private int labelsWithRegressions = 0;
        private int totalRegressions = 0;
        
        // Getters and Setters
        public int getLabelsCompared() {
            return labelsCompared;
        }
        
        public void setLabelsCompared(int labelsCompared) {
            this.labelsCompared = labelsCompared;
        }
        
        public int getLabelsWithRegressions() {
            return labelsWithRegressions;
        }
        
        public void setLabelsWithRegressions(int labelsWithRegressions) {
            this.labelsWithRegressions = labelsWithRegressions;
        }
        
        public int getTotalRegressions() {
            return totalRegressions;
        }
        
        public void setTotalRegressions(int totalRegressions) {
            this.totalRegressions = totalRegressions;
        }
        
        @Override
        public String toString() {
            return String.format(
                "ComparisonSummary{labelsCompared=%d, labelsWithRegressions=%d, totalRegressions=%d}",
                labelsCompared, labelsWithRegressions, totalRegressions
            );
        }
    }
}