package com.modis.performance.model;

/**
 * Details of a performance regression
 */
public class RegressionDetail {
    
    private String metric;
    private double changePercent;
    private double threshold;
    private double baselineValue;
    private double currentValue;
    private String label;
    
    // Constructors
    public RegressionDetail() {}
    
    public RegressionDetail(String metric, double changePercent, double threshold, 
                           double baselineValue, double currentValue) {
        this.metric = metric;
        this.changePercent = changePercent;
        this.threshold = threshold;
        this.baselineValue = baselineValue;
        this.currentValue = currentValue;
    }
    
    public RegressionDetail(String label, String metric, double changePercent, double threshold, 
                           double baselineValue, double currentValue) {
        this(metric, changePercent, threshold, baselineValue, currentValue);
        this.label = label;
    }
    
    // Getters and Setters
    public String getMetric() {
        return metric;
    }
    
    public void setMetric(String metric) {
        this.metric = metric;
    }
    
    public double getChangePercent() {
        return changePercent;
    }
    
    public void setChangePercent(double changePercent) {
        this.changePercent = changePercent;
    }
    
    public double getThreshold() {
        return threshold;
    }
    
    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }
    
    public double getBaselineValue() {
        return baselineValue;
    }
    
    public void setBaselineValue(double baselineValue) {
        this.baselineValue = baselineValue;
    }
    
    public double getCurrentValue() {
        return currentValue;
    }
    
    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    // Business Methods
    
    /**
     * Check if this is a positive regression (performance got worse)
     * @return true if performance degraded, false if improved
     */
    public boolean isPerformanceDegradation() {
        // For most metrics, positive change is bad (except throughput)
        if ("throughput".equals(metric)) {
            return changePercent < 0; // Negative change in throughput is bad
        } else {
            return changePercent > 0; // Positive change in response time/error rate is bad
        }
    }
    
    /**
     * Get the severity of the regression based on how much it exceeds the threshold
     * @return Severity level (LOW, MEDIUM, HIGH, CRITICAL)
     */
    public RegressionSeverity getSeverity() {
        double excessPercent = Math.abs(changePercent) - Math.abs(threshold);
        
        if (excessPercent < 5) {
            return RegressionSeverity.LOW;
        } else if (excessPercent < 15) {
            return RegressionSeverity.MEDIUM;
        } else if (excessPercent < 30) {
            return RegressionSeverity.HIGH;
        } else {
            return RegressionSeverity.CRITICAL;
        }
    }
    
    /**
     * Get a human-readable description of the regression
     * @return Description string
     */
    public String getDescription() {
        String direction = isPerformanceDegradation() ? "increased" : "decreased";
        return String.format(
            "%s %s by %.1f%% (from %.2f to %.2f), exceeding threshold of %.1f%%",
            metric, direction, Math.abs(changePercent), baselineValue, currentValue, Math.abs(threshold)
        );
    }
    
    /**
     * Round all numeric values to 2 decimal places
     */
    public void roundValues() {
        changePercent = Math.round(changePercent * 100.0) / 100.0;
        threshold = Math.round(threshold * 100.0) / 100.0;
        baselineValue = Math.round(baselineValue * 100.0) / 100.0;
        currentValue = Math.round(currentValue * 100.0) / 100.0;
    }
    
    @Override
    public String toString() {
        return String.format(
            "RegressionDetail{metric='%s', changePercent=%.2f%%, threshold=%.2f%%, " +
            "baselineValue=%.2f, currentValue=%.2f, severity=%s}",
            metric, changePercent, threshold, baselineValue, currentValue, getSeverity()
        );
    }
    
    /**
     * Enumeration for regression severity levels
     */
    public enum RegressionSeverity {
        LOW("Low"),
        MEDIUM("Medium"), 
        HIGH("High"),
        CRITICAL("Critical");
        
        private final String displayName;
        
        RegressionSeverity(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
}