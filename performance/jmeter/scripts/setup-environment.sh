#!/bin/bash
# Modis Performance Testing Environment Setup Script
# Sets up JMeter environment and validates configuration

set -e

echo "========================================"
echo "Modis Performance Testing Setup"
echo "========================================"
echo "This script will:"
echo "1. Validate JMeter installation"
echo "2. Check required files and directories"
echo "3. Create sample image for upload tests"
echo "4. Validate test data files"
echo "5. Set up environment variables"
echo "========================================"

# Check JMeter installation
echo "[1/5] Checking JMeter installation..."
if ! command -v jmeter &> /dev/null; then
    echo "ERROR: JMeter not found in PATH"
    echo
    echo "Please install Apache JMeter:"
    echo "1. Download from: https://jmeter.apache.org/download_jmeter.cgi"
    echo "2. Extract to a directory (e.g., /opt/apache-jmeter-5.6.3)"
    echo "3. Add bin directory to PATH environment variable"
    echo "4. Restart terminal and run this script again"
    echo
    echo "Or install via package manager:"
    echo "  macOS: brew install jmeter"
    echo "  Ubuntu: sudo apt-get install jmeter"
    echo
    exit 1
fi

JMETER_VERSION=$(jmeter -v 2>&1 | grep "Version" | head -1)
echo "✓ JMeter found: $JMETER_VERSION"

# Check and create directories
echo
echo "[2/5] Checking directory structure..."
REQUIRED_DIRS=(
    "test-plans"
    "test-plans/load-tests"
    "test-plans/stress-tests"
    "test-plans/spike-tests"
    "test-plans/endurance-tests"
    "test-data"
    "configs"
    "results"
    "reports"
    "docs"
    "scripts"
)

for dir in "${REQUIRED_DIRS[@]}"; do
    if [ ! -d "$dir" ]; then
        echo "Creating directory: $dir"
        mkdir -p "$dir"
    else
        echo "✓ Directory exists: $dir"
    fi
done

# Create sample image if not exists
echo
echo "[3/5] Setting up sample image for upload tests..."
if [ ! -f "test-data/sample-image.jpg" ]; then
    echo "Creating sample image..."
    
    # Try different methods to create sample image
    if command -v convert &> /dev/null; then
        # ImageMagick method
        echo "Using ImageMagick to create sample image..."
        convert -size 400x400 xc:lightblue \
                -pointsize 30 -fill black -gravity center \
                -annotate +0+0 "Test Image" \
                test-data/sample-image.jpg
        echo "✓ Sample image created with ImageMagick"

    elif command -v curl &> /dev/null; then
        # Download placeholder image
        echo "Downloading placeholder image..."
        curl -s "https://via.placeholder.com/400x400/87CEEB/000000?text=Test+Image" \
             -o test-data/sample-image.jpg
        echo "✓ Sample image downloaded from placeholder service"
    else
        echo "⚠ Could not create sample image automatically"
        echo "Please create test-data/sample-image.jpg manually"
        echo "See test-data/sample-image-info.txt for instructions"
    fi
else
    echo "✓ Sample image already exists"
fi

# Validate test data files
echo
echo "[4/5] Validating test data files..."
TEST_DATA_FILES=("users.csv" "post-content.csv" "reactions.csv" "messages.csv")
MISSING_FILES=0

for file in "${TEST_DATA_FILES[@]}"; do
    if [ -f "test-data/$file" ]; then
        echo "✓ Test data file exists: $file"
        # Count lines in CSV (excluding header)
        DATA_COUNT=$(($(wc -l < "test-data/$file") - 1))
        echo "  - Contains $DATA_COUNT data records"
    else
        echo "❌ Missing test data file: $file"
        MISSING_FILES=1
    fi
done

if [ $MISSING_FILES -eq 1 ]; then
    echo
    echo "ERROR: Some test data files are missing"
    echo "Please ensure all CSV files are present in test-data directory"
    exit 1
fi

# Set up environment variables
echo
echo "[5/5] Setting up environment variables..."

# Create environment configuration file
cat > .env << EOF
# Modis Performance Testing Environment Configuration
# Generated on $(date)

# JMeter Configuration
JMETER_HOME=${JMETER_HOME:-$(which jmeter | sed 's|/bin/jmeter||')}
JMETER_VERSION=$JMETER_VERSION

# Test Configuration
MODIS_BASE_URL=https://modis-backend.onrender.com
MODIS_WS_URL=wss://modis-backend.onrender.com/ws

# Default Test Parameters
DEFAULT_LOAD_USERS=50
DEFAULT_STRESS_USERS=200
DEFAULT_SPIKE_USERS=500
DEFAULT_ENDURANCE_USERS=30

# Timeouts (milliseconds)
DEFAULT_CONNECT_TIMEOUT=30000
DEFAULT_RESPONSE_TIMEOUT=90000
UPLOAD_TIMEOUT=120000
EOF

echo "✓ Environment configuration saved to .env"

# Validate JMeter test plans
echo
echo "Validating JMeter test plans..."
TEST_PLANS=(
    "test-plans/load-tests/modis-load-test.jmx"
    "test-plans/stress-tests/modis-stress-test.jmx"
    "test-plans/spike-tests/modis-spike-test.jmx"
    "test-plans/endurance-tests/modis-endurance-test.jmx"
)
MISSING_PLANS=0

for plan in "${TEST_PLANS[@]}"; do
    if [ -f "$plan" ]; then
        echo "✓ Test plan exists: $plan"
        # Basic XML validation
        if ! grep -q "<?xml" "$plan"; then
            echo "⚠ Warning: $plan may not be valid XML"
        fi
    else
        echo "❌ Missing test plan: $plan"
        MISSING_PLANS=1
    fi
done

if [ $MISSING_PLANS -eq 1 ]; then
    echo
    echo "ERROR: Some test plans are missing"
    echo "Please ensure all .jmx files are present"
    exit 1
fi

# Create quick test script
echo
echo "Creating quick test script..."
cat > quick-test.sh << 'EOF'
#!/bin/bash
# Quick smoke test for Modis performance testing

echo "Running quick smoke test..."
jmeter -n -t test-plans/load-tests/modis-load-test.jmx \
    -Jusers=1 -Jramp_up=10 -Jduration=60 \
    -l results/smoke-test.jtl

if [ $? -ne 0 ]; then
    echo "Smoke test failed!"
    exit 1
fi

echo "Smoke test passed!"
EOF

chmod +x quick-test.sh
echo "✓ Quick test script created: quick-test.sh"

# Make all shell scripts executable
echo
echo "Making shell scripts executable..."
find . -name "*.sh" -exec chmod +x {} \;
echo "✓ Shell scripts are now executable"

# Performance recommendations
echo
echo "========================================"
echo "Setup Complete!"
echo "========================================"
echo
echo "Next Steps:"
echo "1. Run quick smoke test: ./quick-test.sh"
echo "2. Review configuration in .env file"
echo "3. Customize test parameters as needed"
echo "4. Run full test suite: ./run-all-tests.sh"
echo
echo "Performance Recommendations:"
echo "- Ensure at least 4GB RAM for JMeter"
echo "- Use SSD storage for better I/O performance"
echo "- Close unnecessary applications during testing"
echo "- Monitor system resources during tests"
echo
echo "Documentation:"
echo "- README.md: Getting started guide"
echo "- docs/METRICS_GUIDE.md: Understanding results"
echo "- docs/BEST_PRACTICES.md: Testing guidelines"
echo
echo "========================================"

read -p "Run quick smoke test now? (y/n): " RUN_SMOKE
if [[ "$RUN_SMOKE" == "y" || "$RUN_SMOKE" == "Y" ]]; then
    echo
    echo "Running smoke test..."
    ./quick-test.sh
fi

echo
echo "Setup completed successfully!"