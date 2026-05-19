#!/bin/bash
# Modis Complete Performance Test Suite Runner
# Runs all test types in sequence with proper intervals

echo "========================================"
echo "Modis Complete Performance Test Suite"
echo "========================================"
echo "This script will run all performance tests:"
echo "1. Load Test (50 users, 10 minutes)"
echo "2. Stress Test (200 users, 15 minutes)"
echo "3. Image Upload Test (20 users, 10 minutes)"
echo "4. WebSocket Test (30 users, 10 minutes)"
echo "5. Spike Test (500 users, 5 minutes)"
echo
echo "Total estimated time: ~60 minutes"
echo "========================================"

read -p "Continue with full test suite? (y/n): " CONFIRM
if [[ "$CONFIRM" != "y" && "$CONFIRM" != "Y" ]]; then
    echo "Test suite cancelled by user"
    exit 0
fi

# Create master results directory
MASTER_TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
MASTER_DIR="reports/full-suite-${MASTER_TIMESTAMP}"
mkdir -p "$MASTER_DIR"

echo
echo "========================================"
echo "Starting Test Suite at $(date)"
echo "Master Results Directory: $MASTER_DIR"
echo "========================================"

# Function to check if command succeeded
check_result() {
    if [ $? -ne 0 ]; then
        echo "ERROR: $1 failed"
        exit 1
    fi
}

# Test 1: Load Test
echo
echo "[1/5] Running Load Test..."
./run-load-test.sh 50 300 600
check_result "Load test"

# Wait between tests
echo "Waiting 2 minutes before next test..."
sleep 120

# Test 2: Stress Test
echo
echo "[2/5] Running Stress Test..."
./run-stress-test.sh 200 180 900
check_result "Stress test"

# Wait between tests
echo "Waiting 3 minutes before next test..."
sleep 180

# Test 3: Image Upload Test
echo
echo "[3/5] Running Image Upload Test..."
UPLOAD_TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
jmeter -n -t test-plans/load-tests/modis-image-upload-test.jmx \
    -Jusers=20 \
    -Jramp_up=120 \
    -Jduration=600 \
    -l "results/image-upload-test-${UPLOAD_TIMESTAMP}.jtl" \
    -e -o "reports/image-upload-test-${UPLOAD_TIMESTAMP}"
check_result "Image upload test"

# Wait between tests
echo "Waiting 2 minutes before next test..."
sleep 120

# Test 4: WebSocket Test
echo
echo "[4/5] Running WebSocket Test..."
WS_TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
jmeter -n -t test-plans/load-tests/modis-websocket-test.jmx \
    -Jusers=30 \
    -Jramp_up=180 \
    -Jduration=600 \
    -l "results/websocket-test-${WS_TIMESTAMP}.jtl" \
    -e -o "reports/websocket-test-${WS_TIMESTAMP}"
check_result "WebSocket test"

# Wait between tests
echo "Waiting 2 minutes before final test..."
sleep 120

# Test 5: Spike Test
echo
echo "[5/5] Running Spike Test..."
SPIKE_TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
jmeter -n -t test-plans/spike-tests/modis-spike-test.jmx \
    -Jusers=500 \
    -Jramp_up=60 \
    -Jduration=300 \
    -l "results/spike-test-${SPIKE_TIMESTAMP}.jtl" \
    -e -o "reports/spike-test-${SPIKE_TIMESTAMP}"
check_result "Spike test"

# Generate summary report
echo
echo "========================================"
echo "Generating Test Suite Summary..."
echo "========================================"

cat > "$MASTER_DIR/index.html" << EOF
<html>
<head>
    <title>Modis Performance Test Suite Results</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; }
        h1 { color: #333; }
        h2 { color: #666; }
        ul { list-style-type: none; padding: 0; }
        li { margin: 10px 0; }
        a { color: #0066cc; text-decoration: none; }
        a:hover { text-decoration: underline; }
        .info { background: #f0f0f0; padding: 15px; border-radius: 5px; }
    </style>
</head>
<body>
    <h1>Modis Performance Test Suite Results</h1>
    <div class="info">
        <p><strong>Test Suite Executed:</strong> $(date)</p>
        <p><strong>Total Duration:</strong> ~60 minutes</p>
        <p><strong>Test Environment:</strong> https://modis-backend.onrender.com</p>
    </div>
    
    <h2>Test Results</h2>
    <ul>
EOF

# Find and link to individual reports
for report_dir in reports/load-test-* reports/stress-test-* reports/image-upload-test-* reports/websocket-test-* reports/spike-test-*; do
    if [ -d "$report_dir" ] && [ -f "$report_dir/index.html" ]; then
        report_name=$(basename "$report_dir")
        test_type=$(echo "$report_name" | sed 's/-[0-9]*_[0-9]*$//' | sed 's/-/ /g' | sed 's/\b\w/\U&/g')
        echo "        <li><a href=\"../$report_name/index.html\">$test_type Results</a></li>" >> "$MASTER_DIR/index.html"
    fi
done

cat >> "$MASTER_DIR/index.html" << EOF
    </ul>
    
    <h2>Test Summary</h2>
    <p>This test suite validates the performance characteristics of the Modis social media application under various load conditions:</p>
    <ul>
        <li><strong>Load Test:</strong> Normal expected usage patterns</li>
        <li><strong>Stress Test:</strong> High load to find breaking points</li>
        <li><strong>Image Upload Test:</strong> File upload performance with Cloudinary</li>
        <li><strong>WebSocket Test:</strong> Real-time messaging and notifications</li>
        <li><strong>Spike Test:</strong> Sudden traffic spikes handling</li>
    </ul>
    
    <p>Click on individual test results above to view detailed performance metrics, response times, and throughput analysis.</p>
</body>
</html>
EOF

echo
echo "========================================"
echo "Test Suite Completed Successfully!"
echo "========================================"
echo "Suite completed at: $(date)"
echo "Master summary: $MASTER_DIR/index.html"
echo
echo "All individual test reports are available in their respective directories."
echo "========================================"

# Ask if user wants to open the report (macOS only)
if [[ "$OSTYPE" == "darwin"* ]]; then
    read -p "Open test suite summary? (y/n): " OPEN_SUMMARY
    if [[ "$OPEN_SUMMARY" == "y" || "$OPEN_SUMMARY" == "Y" ]]; then
        open "$MASTER_DIR/index.html"
    fi
fi

echo "Test suite execution completed successfully!"