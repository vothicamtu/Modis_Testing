package com.modis.performance.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.util.ArrayList;
import java.util.List;

public class PerformanceMetrics {
    private int samples = 0;
    private double avgResponseTime = 0.0;
    private double minResponseTime = Double.MAX_VALUE;
    private double maxResponseTime = 0.0;
    private double percentile95 = 0.0;
    private double errorRate = 0.0;
    private double throughput = 0.0;
    @JsonIgnore
    private List<Double> responseTimes = new ArrayList<>();
    @JsonIgnore
    private int errorCount = 0;

    public PerformanceMetrics() {
    }

    public int getSamples() {
        return samples;
    }

    public void setSamples(int samples) {
        this.samples = samples;
    }

    public double getAvgResponseTime() {
        return avgResponseTime;
    }

    public void setAvgResponseTime(double avgResponseTime) {
        this.avgResponseTime = avgResponseTime;
    }

    public double getMinResponseTime() {
        return minResponseTime;
    }

    public void setMinResponseTime(double minResponseTime) {
        this.minResponseTime = minResponseTime;
    }

    public double getMaxResponseTime() {
        return maxResponseTime;
    }

    public void setMaxResponseTime(double maxResponseTime) {
        this.maxResponseTime = maxResponseTime;
    }

    public double getPercentile95() {
        return percentile95;
    }

    public void setPercentile95(double percentile95) {
        this.percentile95 = percentile95;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(double errorRate) {
        this.errorRate = errorRate;
    }

    public double getThroughput() {
        return throughput;
    }

    public void setThroughput(double throughput) {
        this.throughput = throughput;
    }

    public List<Double> getResponseTimes() {
        return responseTimes;
    }

    public void setResponseTimes(List<Double> responseTimes) {
        this.responseTimes = responseTimes;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public void addSample(double responseTime, boolean success) {
        samples++;
        responseTimes.add(responseTime);
        if (responseTime < minResponseTime) {
            minResponseTime = responseTime;
        }
        if (responseTime > maxResponseTime) {
            maxResponseTime = responseTime;
        }
        if (!success) {
            errorCount++;
        }
    }

    public void calculateDerivedMetrics() {
        if (samples > 0) {
            avgResponseTime = responseTimes.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
            errorRate = (double) errorCount / samples * 100.0;
            calculatePercentile95();
            throughput = samples;
        }
    }

    private void calculatePercentile95() {
        if (!responseTimes.isEmpty()) {
            double[] times = responseTimes.stream()
                    .mapToDouble(Double::doubleValue)
                    .toArray();
            Percentile percentile = new Percentile();
            percentile95 = percentile.evaluate(times, 95.0);
        }
    }

    public void roundMetrics() {
        avgResponseTime = Math.round(avgResponseTime * 100.0) / 100.0;
        minResponseTime = Math.round(minResponseTime * 100.0) / 100.0;
        maxResponseTime = Math.round(maxResponseTime * 100.0) / 100.0;
        percentile95 = Math.round(percentile95 * 100.0) / 100.0;
        errorRate = Math.round(errorRate * 100.0) / 100.0;
        throughput = Math.round(throughput * 100.0) / 100.0;
    }

    public boolean isValid() {
        return samples > 0;
    }

    public void reset() {
        samples = 0;
        avgResponseTime = 0.0;
        minResponseTime = Double.MAX_VALUE;
        maxResponseTime = 0.0;
        percentile95 = 0.0;
        errorRate = 0.0;
        throughput = 0.0;
        responseTimes.clear();
        errorCount = 0;
    }

    @Override
    public String toString() {
        return String.format(
                "PerformanceMetrics{samples=%d, avgResponseTime=%.2f, minResponseTime=%.2f, " +
                        "maxResponseTime=%.2f, percentile95=%.2f, errorRate=%.2f%%, throughput=%.2f}",
                samples, avgResponseTime, minResponseTime, maxResponseTime,
                percentile95, errorRate, throughput
        );
    }
}