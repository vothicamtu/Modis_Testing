#!/bin/bash

# Modis Performance Testing - Shell Script for Linux/Mac
# Usage: ./run-tests.sh [test-type] [users] [duration] [rampup]

# Default values
TEST_TYPE=${1:-load}
USERS=${2:-50}
DURATION=${3:-1800}
RAMPUP=${4:-300}

# Set JMeter path (adjust as needed)
JMETER_HOME=${JMETER_HOME:-/opt/apache-jmeter}
JMETER_BIN="$JMETER_HOME/bin/jmeter"

# Check if JMeter exists
if [ ! -f "$JMETER_BIN" ]; then
    echo "ERROR: JMeter not found at $JMETER_BIN"
    echo "Please install JMeter or set JMETER_HOME environment variable"
    exit 1
fi

# Create results and reports directories
mkdir -p results
mkdir -p reports

# Set timestamp for unique file names
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

echo "========================================"
echo "Modis Performance Testing Suite"
echo "========================================"
echo "Test Type: $TEST_TYPE"
echo "Users: $USERS"
echo "Duration: $DURATION seconds"
echo "Ramp-up: $RAMPUP seconds"
echo "Timestamp: $TIMESTAMP"
echo "========================================"

# Set test plan file based on test type
case $TEST_TYPE in
    "load")
        TEST_PLAN="test-plans/modis-load-test.jmx"
        REPORT_NAME="load-test-$TIMESTAMP"
        ;;
    "stress")
        TEST_PLAN="test-plans/modis-stress-test.jmx"
        REPORT_NAME="stress-test-$TIMESTAMP"
        ;;
    "spike")
        TEST_PLAN="test-plans/modis-spike-test.jmx"
        REPORT_NAME="spike-test-$TIMESTAMP"
        ;;
    "endurance")
        TEST_PLAN="test-plans/modis-endurance-test.jmx"
        REPORT_NAME="endurance-test-$TIMESTAMP"
        ;;
    "image")
        TEST_PLAN="test-plans/modis-image-upload-test.jmx"
        REPORT_NAME="image-upload-test-$TIMESTAMP"
        ;;
    *)
        echo "ERROR: Invalid test type. Use: load, stress, spike, endurance, or image"
        exit 1
        ;;
esac

# Check if test plan exists
if [ ! -f "$TEST_PLAN" ]; then
    echo "ERROR: Test plan not found: $TEST_PLAN"
    exit 1
fi

echo "Running test plan: $TEST_PLAN"
echo "Results will be saved to: results/$REPORT_NAME.jtl"
echo "HTML report will be generated at: reports/$REPORT_NAME/"

# Set JMeter heap size for better performance
export HEAP="-Xms2g -Xmx4g -XX:MaxMetaspaceSize=512m"

# Run JMeter test
"$JMETER_BIN" -n -t "$TEST_PLAN" \
    -Jusers=$USERS \
    -Jrampup=$RAMPUP \
    -Jduration=$DURATION \
    -Jhost=modis-backend.onrender.com \
    -l "results/$REPORT_NAME.jtl" \
    -e -o "reports/$REPORT_NAME"

if [ $? -eq 0 ]; then
    echo "========================================"
    echo "Test completed successfully!"
    echo "========================================"
    echo "Results: results/$REPORT_NAME.jtl"
    echo "HTML Report: reports/$REPORT_NAME/index.html"
    echo "========================================"
    
    # Try to open HTML report in default browser (Linux/Mac)
    if command -v xdg-open > /dev/null; then
        xdg-open "reports/$REPORT_NAME/index.html"
    elif command -v open > /dev/null; then
        open "reports/$REPORT_NAME/index.html"
    else
        echo "Open reports/$REPORT_NAME/index.html in your browser to view results"
    fi
else
    echo "========================================"
    echo "Test failed with error code: $?"
    echo "========================================"
    exit 1
fi