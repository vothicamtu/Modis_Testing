package com.modis.performance.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modis.performance.comparator.PerformanceComparator;
import com.modis.performance.model.ComparisonResult;
import com.modis.performance.parsers.JTLResultsParser;
import com.modis.performance.reports.ReportGenerator;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Main class for comparing JMeter performance test results
 * Replaces the Python compare-results.py script
 */
public class ResultsComparator {
    
    private static final Logger logger = LoggerFactory.getLogger(ResultsComparator.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static void main(String[] args) {
        try {
            // Parse command line arguments
            CommandLine cmd = parseArguments(args);
            
            if (cmd == null) {
                System.exit(1);
                return;
            }
            
            // Get required arguments
            String baselineFile = cmd.getOptionValue("baseline");
            String currentFile = cmd.getOptionValue("current");
            
            // Validate input files
            if (!validateInputFiles(baselineFile, currentFile)) {
                System.exit(1);
                return;
            }
            
            // Load custom thresholds if provided
            Map<String, Double> customThresholds = null;
            if (cmd.hasOption("thresholds")) {
                customThresholds = loadCustomThresholds(cmd.getOptionValue("thresholds"));
                if (customThresholds == null) {
                    System.exit(1);
                    return;
                }
            }
            
            // Perform comparison
            logger.info("Starting performance comparison");
            PerformanceComparator comparator = new PerformanceComparator(customThresholds);
            ComparisonResult result = comparator.compareResults(baselineFile, currentFile);
            
            // Generate console report (always)
            ReportGenerator.generateConsoleReport(result);
            
            // Generate additional reports if requested
            if (cmd.hasOption("json-output")) {
                ReportGenerator.generateJsonReport(result, cmd.getOptionValue("json-output"));
            }
            
            if (cmd.hasOption("html-output")) {
                ReportGenerator.generateHtmlReport(result, cmd.getOptionValue("html-output"));
            }
            
            if (cmd.hasOption("csv-output")) {
                ReportGenerator.generateCsvReport(result, cmd.getOptionValue("csv-output"));
            }
            
            // Exit with error code if regressions detected and flag is set
            if (cmd.hasOption("fail-on-regression") && result.hasRegressions()) {
                System.out.println("\nExiting with error code due to performance regressions.");
                System.exit(1);
            }
            
            logger.info("Performance comparison completed successfully");
            System.exit(0);
            
        } catch (Exception e) {
            logger.error("Performance comparison failed", e);
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * Parse command line arguments
     * @param args Command line arguments
     * @return Parsed CommandLine object or null if parsing failed
     */
    private static CommandLine parseArguments(String[] args) {
        Options options = new Options();
        
        // Required arguments
        options.addRequiredOption("b", "baseline", true, "Baseline JTL file path");
        options.addRequiredOption("c", "current", true, "Current JTL file path");
        
        // Optional arguments
        options.addOption("j", "json-output", true, "Output JSON report to file");
        options.addOption("h", "html-output", true, "Output HTML report to file");
        options.addOption("v", "csv-output", true, "Output CSV report to file");
        options.addOption("f", "fail-on-regression", false, "Exit with code 1 if regressions detected");
        options.addOption("t", "thresholds", true, "JSON file with custom regression thresholds");
        options.addOption("help", "help", false, "Show help message");
        
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        
        try {
            CommandLine cmd = parser.parse(options, args);
            
            if (cmd.hasOption("help")) {
                printUsage(formatter, options);
                return null;
            }
            
            return cmd;
            
        } catch (ParseException e) {
            System.err.println("Error parsing arguments: " + e.getMessage());
            printUsage(formatter, options);
            return null;
        }
    }
    
    /**
     * Print usage information
     * @param formatter Help formatter
     * @param options Command line options
     */
    private static void printUsage(HelpFormatter formatter, Options options) {
        System.out.println("\nModis Performance Results Comparator");
        System.out.println("Compares JMeter test results and detects performance regressions\n");
        
        formatter.printHelp("java -jar modis-performance-tests.jar", options);
        
        System.out.println("\nExamples:");
        System.out.println("  java -jar modis-performance-tests.jar -b baseline.jtl -c current.jtl");
        System.out.println("  java -jar modis-performance-tests.jar -b baseline.jtl -c current.jtl -j report.json -h report.html");
        System.out.println("  java -jar modis-performance-tests.jar -b baseline.jtl -c current.jtl -f -t thresholds.json");
        
        System.out.println("\nThresholds JSON format:");
        System.out.println("  {");
        System.out.println("    \"avgResponseTime\": 20.0,");
        System.out.println("    \"percentile95\": 25.0,");
        System.out.println("    \"errorRate\": 50.0,");
        System.out.println("    \"throughput\": -15.0");
        System.out.println("  }");
    }
    
    /**
     * Validate input files exist and are readable
     * @param baselineFile Baseline file path
     * @param currentFile Current file path
     * @return true if files are valid, false otherwise
     */
    private static boolean validateInputFiles(String baselineFile, String currentFile) {
        // Check baseline file
        if (!Files.exists(Paths.get(baselineFile))) {
            System.err.println("Error: Baseline file not found: " + baselineFile);
            return false;
        }
        
        if (!Files.isReadable(Paths.get(baselineFile))) {
            System.err.println("Error: Baseline file is not readable: " + baselineFile);
            return false;
        }
        
        // Check current file
        if (!Files.exists(Paths.get(currentFile))) {
            System.err.println("Error: Current file not found: " + currentFile);
            return false;
        }
        
        if (!Files.isReadable(Paths.get(currentFile))) {
            System.err.println("Error: Current file is not readable: " + currentFile);
            return false;
        }
        
        // Validate JTL format
        if (!JTLResultsParser.validateJTLFormat(Paths.get(baselineFile))) {
            System.err.println("Error: Invalid JTL format in baseline file: " + baselineFile);
            return false;
        }
        
        if (!JTLResultsParser.validateJTLFormat(Paths.get(currentFile))) {
            System.err.println("Error: Invalid JTL format in current file: " + currentFile);
            return false;
        }
        
        logger.info("Input file validation passed");
        return true;
    }
    
    /**
     * Load custom thresholds from JSON file
     * @param thresholdsFile Path to thresholds JSON file
     * @return Map of thresholds or null if loading failed
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Double> loadCustomThresholds(String thresholdsFile) {
        try {
            File file = new File(thresholdsFile);
            if (!file.exists()) {
                System.err.println("Error: Thresholds file not found: " + thresholdsFile);
                return null;
            }
            
            Map<String, Double> thresholds = objectMapper.readValue(file, Map.class);
            logger.info("Loaded custom thresholds: {}", thresholds);
            return thresholds;
            
        } catch (IOException e) {
            System.err.println("Error loading thresholds file: " + e.getMessage());
            logger.error("Failed to load thresholds file: {}", thresholdsFile, e);
            return null;
        }
    }
    
    /**
     * Utility method to run comparison programmatically
     * @param baselineFile Baseline JTL file path
     * @param currentFile Current JTL file path
     * @param customThresholds Custom regression thresholds (optional)
     * @return Comparison result
     * @throws IOException If files cannot be read
     */
    public static ComparisonResult compareResults(String baselineFile, String currentFile, 
                                                Map<String, Double> customThresholds) throws IOException {
        PerformanceComparator comparator = new PerformanceComparator(customThresholds);
        return comparator.compareResults(baselineFile, currentFile);
    }
    
    /**
     * Utility method to run comparison with default thresholds
     * @param baselineFile Baseline JTL file path
     * @param currentFile Current JTL file path
     * @return Comparison result
     * @throws IOException If files cannot be read
     */
    public static ComparisonResult compareResults(String baselineFile, String currentFile) throws IOException {
        return compareResults(baselineFile, currentFile, null);
    }
}