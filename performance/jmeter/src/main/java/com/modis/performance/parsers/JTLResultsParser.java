package com.modis.performance.parsers;

import com.modis.performance.model.PerformanceMetrics;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Parser for JMeter JTL result files
 */
public class JTLResultsParser {
    
    private static final Logger logger = LoggerFactory.getLogger(JTLResultsParser.class);
    
    // JTL file column names
    private static final String COLUMN_LABEL = "label";
    private static final String COLUMN_ELAPSED = "elapsed";
    private static final String COLUMN_SUCCESS = "success";
    private static final String COLUMN_TIMESTAMP = "timeStamp";
    private static final String COLUMN_RESPONSE_CODE = "responseCode";
    private static final String COLUMN_RESPONSE_MESSAGE = "responseMessage";
    private static final String COLUMN_THREAD_NAME = "threadName";
    private static final String COLUMN_DATA_TYPE = "dataType";
    private static final String COLUMN_BYTES = "bytes";
    private static final String COLUMN_SENT_BYTES = "sentBytes";
    private static final String COLUMN_GRPTHREADS = "grpThreads";
    private static final String COLUMN_ALLTHREADS = "allThreads";
    private static final String COLUMN_LATENCY = "Latency";
    private static final String COLUMN_IDLE_TIME = "IdleTime";
    private static final String COLUMN_CONNECT = "Connect";
    
    /**
     * Parse JTL file and extract performance metrics by label
     * @param filePath Path to the JTL file
     * @return Map of label to performance metrics
     * @throws IOException If file cannot be read
     */
    public static Map<String, PerformanceMetrics> parseJTLFile(String filePath) throws IOException {
        return parseJTLFile(Paths.get(filePath));
    }
    
    /**
     * Parse JTL file and extract performance metrics by label
     * @param filePath Path to the JTL file
     * @return Map of label to performance metrics
     * @throws IOException If file cannot be read
     */
    public static Map<String, PerformanceMetrics> parseJTLFile(Path filePath) throws IOException {
        logger.info("Parsing JTL file: {}", filePath);
        
        if (!Files.exists(filePath)) {
            throw new IOException("JTL file not found: " + filePath);
        }
        
        Map<String, PerformanceMetrics> metricsByLabel = new HashMap<>();
        long startTime = System.currentTimeMillis();
        int recordCount = 0;
        
        try (FileReader reader = new FileReader(filePath.toFile());
             CSVParser parser = CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim()
                     .parse(reader)) {
            
            for (CSVRecord record : parser) {
                recordCount++;
                
                try {
                    String label = getStringValue(record, COLUMN_LABEL, "Unknown");
                    double elapsed = getDoubleValue(record, COLUMN_ELAPSED, 0.0);
                    boolean success = getBooleanValue(record, COLUMN_SUCCESS, true);
                    
                    // Get or create metrics for this label
                    PerformanceMetrics metrics = metricsByLabel.computeIfAbsent(
                        label, k -> new PerformanceMetrics()
                    );
                    
                    // Add sample to metrics
                    metrics.addSample(elapsed, success);
                    
                } catch (Exception e) {
                    logger.warn("Error parsing record {}: {}", recordCount, e.getMessage());
                }
            }
            
        } catch (IOException e) {
            logger.error("Error reading JTL file: {}", filePath, e);
            throw e;
        }
        
        // Calculate derived metrics for all labels
        for (Map.Entry<String, PerformanceMetrics> entry : metricsByLabel.entrySet()) {
            PerformanceMetrics metrics = entry.getValue();
            metrics.calculateDerivedMetrics();
            metrics.roundMetrics();
            
            logger.debug("Parsed metrics for label '{}': {}", entry.getKey(), metrics);
        }
        
        long parseTime = System.currentTimeMillis() - startTime;
        logger.info("Parsed {} records from {} labels in {} ms", 
                   recordCount, metricsByLabel.size(), parseTime);
        
        return metricsByLabel;
    }
    
    /**
     * Get string value from CSV record with default fallback
     * @param record CSV record
     * @param columnName Column name
     * @param defaultValue Default value if column not found or empty
     * @return String value
     */
    private static String getStringValue(CSVRecord record, String columnName, String defaultValue) {
        try {
            if (record.isMapped(columnName)) {
                String value = record.get(columnName);
                return (value != null && !value.trim().isEmpty()) ? value.trim() : defaultValue;
            }
        } catch (Exception e) {
            logger.debug("Error getting string value for column '{}': {}", columnName, e.getMessage());
        }
        return defaultValue;
    }
    
    /**
     * Get double value from CSV record with default fallback
     * @param record CSV record
     * @param columnName Column name
     * @param defaultValue Default value if column not found or invalid
     * @return Double value
     */
    private static double getDoubleValue(CSVRecord record, String columnName, double defaultValue) {
        try {
            if (record.isMapped(columnName)) {
                String value = record.get(columnName);
                if (value != null && !value.trim().isEmpty()) {
                    return Double.parseDouble(value.trim());
                }
            }
        } catch (NumberFormatException e) {
            logger.debug("Error parsing double value for column '{}': {}", columnName, e.getMessage());
        } catch (Exception e) {
            logger.debug("Error getting double value for column '{}': {}", columnName, e.getMessage());
        }
        return defaultValue;
    }
    
    /**
     * Get boolean value from CSV record with default fallback
     * @param record CSV record
     * @param columnName Column name
     * @param defaultValue Default value if column not found or invalid
     * @return Boolean value
     */
    private static boolean getBooleanValue(CSVRecord record, String columnName, boolean defaultValue) {
        try {
            if (record.isMapped(columnName)) {
                String value = record.get(columnName);
                if (value != null && !value.trim().isEmpty()) {
                    return "true".equalsIgnoreCase(value.trim());
                }
            }
        } catch (Exception e) {
            logger.debug("Error getting boolean value for column '{}': {}", columnName, e.getMessage());
        }
        return defaultValue;
    }
    
    /**
     * Validate JTL file format
     * @param filePath Path to the JTL file
     * @return true if file format is valid, false otherwise
     */
    public static boolean validateJTLFormat(Path filePath) {
        try {
            if (!Files.exists(filePath)) {
                logger.error("JTL file not found: {}", filePath);
                return false;
            }
            
            try (FileReader reader = new FileReader(filePath.toFile());
                 CSVParser parser = CSVFormat.DEFAULT
                         .withFirstRecordAsHeader()
                         .withIgnoreHeaderCase()
                         .parse(reader)) {
                
                // Check if required columns exist
                Map<String, Integer> headerMap = parser.getHeaderMap();
                
                if (!headerMap.containsKey(COLUMN_LABEL)) {
                    logger.error("JTL file missing required column: {}", COLUMN_LABEL);
                    return false;
                }
                
                if (!headerMap.containsKey(COLUMN_ELAPSED)) {
                    logger.error("JTL file missing required column: {}", COLUMN_ELAPSED);
                    return false;
                }
                
                // Try to parse first record
                if (parser.iterator().hasNext()) {
                    CSVRecord firstRecord = parser.iterator().next();
                    getStringValue(firstRecord, COLUMN_LABEL, "");
                    getDoubleValue(firstRecord, COLUMN_ELAPSED, 0.0);
                    getBooleanValue(firstRecord, COLUMN_SUCCESS, true);
                }
                
                logger.info("JTL file format validation passed: {}", filePath);
                return true;
                
            }
        } catch (Exception e) {
            logger.error("JTL file format validation failed: {}", filePath, e);
            return false;
        }
    }
    
    /**
     * Get basic file statistics
     * @param filePath Path to the JTL file
     * @return Map containing file statistics
     */
    public static Map<String, Object> getFileStatistics(Path filePath) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            if (!Files.exists(filePath)) {
                stats.put("error", "File not found");
                return stats;
            }
            
            stats.put("filePath", filePath.toString());
            stats.put("fileSize", Files.size(filePath));
            stats.put("lastModified", Files.getLastModifiedTime(filePath).toString());
            
            // Count records
            try (FileReader reader = new FileReader(filePath.toFile());
                 CSVParser parser = CSVFormat.DEFAULT
                         .withFirstRecordAsHeader()
                         .withIgnoreHeaderCase()
                         .parse(reader)) {
                
                long recordCount = parser.stream().count();
                stats.put("recordCount", recordCount);
                
                Map<String, Integer> headerMap = parser.getHeaderMap();
                stats.put("columnCount", headerMap.size());
                stats.put("columns", headerMap.keySet());
                
            }
            
        } catch (Exception e) {
            logger.error("Error getting file statistics: {}", filePath, e);
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }
}