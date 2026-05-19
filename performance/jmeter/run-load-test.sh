#!/bin/bash
# Modis Load Test Runner Script for Unix/Linux/macOS
# Usage: ./run-load-test.sh [users] [ramp_up] [duration]

# Default values
USERS=${1:-50}
RAMP_UP=${2:-300}
DURATION=${3:-600}
BASE_URL=${4:-"https://modis-backend.onrender.com"}

# Create timestamp for unique file names
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# File paths
TEST_PLAN="test-plans/load-tests/modis-load-test.jmx"
RESULT_FILE="results/load-test-${TIMESTAMP}.jtl"
REPORT_DIR="reports/load-test-${TIMESTAMP}"

echo "========================================"
echo "Modis Load Test Execution"
echo "========================================"
echo "Test Plan: $TEST_PLAN"
echo "Users: $USERS"
echo "Ramp Up: $RAMP_UP seconds"
echo "Duration: $DURATION seconds"
echo "Base URL: $BASE_URL"
echo "Result File: $RESULT_FILE"
echo "Report Directory: $REPORT_DIR"
echo "========================================"

# Check if JMeter is available
if ! command -v jmeter &> /dev/null; then
    echo "ERROR: JMeter not found in PATH"
    echo "Please install Apache JMeter and add it to your PATH"
    echo "Download from: https://jmeter.apache.org/download_jmeter.cgi"
    exit 1
fi

# Check if test plan exists
if [ ! -f "$TEST_PLAN" ]; then
    echo "ERROR: Test plan not found: $TEST_PLAN"
    echo "Please ensure you are running this script from the jmeter directory"
    exit 1
fi

# Create directories if they don't exist
mkdir -p results
mkdir -p reports

echo "Starting JMeter test execution..."
echo

# Run JMeter test
jmeter -n -t "$TEST_PLAN" \
    -Jusers=$USERS \
    -Jramp_up=$RAMP_UP \
    -Jduration=$DURATION \
    -Jbase_url=$BASE_URL \
    -l "$RESULT_FILE" \
    -e -o "$REPORT_DIR"

if [ $? -ne 0 ]; then
    echo
    echo "ERROR: JMeter test execution failed"
    exit 1
fi

echo
echo "========================================"
echo "Test Execution Completed Successfully!"
echo "========================================"
echo "Results saved to: $RESULT_FILE"
echo "HTML Report generated: $REPORT_DIR/index.html"
echo
echo "To view the report, open: $REPORT_DIR/index.html"
echo

# Ask if user wants to open the report (macOS only)
if [[ "$OSTYPE" == "darwin"* ]]; then
    read -p "Open HTML report now? (y/n): " OPEN_REPORT
    if [[ "$OPEN_REPORT" == "y" || "$OPEN_REPORT" == "Y" ]]; then
        open "$REPORT_DIR/index.html"
    fi
fi

echo "Test completed at: $(date)"