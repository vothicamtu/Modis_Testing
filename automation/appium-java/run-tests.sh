#!/bin/bash

# Modis Mobile App - Appium Java Automation Tests
# Unix/Linux/macOS Shell Script for Test Execution

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
PLATFORM="android"
DEVICE_NAME="Android Emulator"
PLATFORM_VERSION="11.0"
TEST_SUITE="smoke"
APPIUM_SERVER="http://127.0.0.1:4723"

# Function to print colored output
print_info() {
    echo -e "${BLUE}$1${NC}"
}

print_success() {
    echo -e "${GREEN}$1${NC}"
}

print_warning() {
    echo -e "${YELLOW}$1${NC}"
}

print_error() {
    echo -e "${RED}$1${NC}"
}

# Function to show help
show_help() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --platform PLATFORM     Target platform (android/ios) [default: android]"
    echo "  --device DEVICE_NAME     Device name [default: Android Emulator]"
    echo "  --version VERSION        Platform version [default: 11.0]"
    echo "  --suite SUITE           Test suite to run [default: smoke]"
    echo "  --server SERVER_URL     Appium server URL [default: http://127.0.0.1:4723]"
    echo "  --help                  Show this help message"
    echo ""
    echo "Available test suites:"
    echo "  smoke                   Critical path tests"
    echo "  regression              All regression tests"
    echo "  authentication          Authentication tests only"
    echo "  navigation              Navigation tests only"
    echo "  camera                  Camera tests only"
    echo "  messaging               Messaging tests only"
    echo "  friends                 Friends tests only"
    echo "  profile                 Profile tests only"
    echo "  full                    All tests"
    echo ""
    echo "Examples:"
    echo "  $0"
    echo "  $0 --suite regression"
    echo "  $0 --platform android --device \"Pixel 4\" --suite smoke"
    echo "  $0 --platform ios --device \"iPhone 12\" --version 14.0"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --platform)
            PLATFORM="$2"
            shift 2
            ;;
        --device)
            DEVICE_NAME="$2"
            shift 2
            ;;
        --version)
            PLATFORM_VERSION="$2"
            shift 2
            ;;
        --suite)
            TEST_SUITE="$2"
            shift 2
            ;;
        --server)
            APPIUM_SERVER="$2"
            shift 2
            ;;
        --help)
            show_help
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
done

# Print header
echo "========================================"
print_info "Modis Mobile App Automation Tests"
echo "========================================"
echo ""

# Print configuration
print_info "Configuration:"
echo "  Platform: $PLATFORM"
echo "  Device: $DEVICE_NAME"
echo "  Version: $PLATFORM_VERSION"
echo "  Test Suite: $TEST_SUITE"
echo "  Appium Server: $APPIUM_SERVER"
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    print_error "ERROR: Java is not installed or not in PATH"
    print_error "Please install Java 11 or higher and add it to PATH"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    print_error "ERROR: Maven is not installed or not in PATH"
    print_error "Please install Maven and add it to PATH"
    exit 1
fi

# Check if Appium server is running
print_info "Checking Appium server connection..."
if ! curl -s "$APPIUM_SERVER/status" > /dev/null 2>&1; then
    print_warning "WARNING: Cannot connect to Appium server at $APPIUM_SERVER"
    print_warning "Please make sure Appium server is running:"
    print_warning "  appium"
    echo ""
    read -p "Continue anyway? (y/N): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_error "Test execution cancelled"
        exit 1
    fi
else
    print_success "Appium server is running"
fi

# Check device connection for Android
if [[ "$PLATFORM" == "android" ]]; then
    print_info "Checking Android device connection..."
    if ! adb devices | grep -q "device$"; then
        print_warning "WARNING: No Android devices found"
        print_warning "Please connect an Android device or start an emulator"
        print_warning "  adb devices"
        echo ""
        read -p "Continue anyway? (y/N): " -n 1 -r
        echo ""
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            print_error "Test execution cancelled"
            exit 1
        fi
    else
        print_success "Android device(s) found"
    fi
fi

# Check iOS setup for macOS
if [[ "$PLATFORM" == "ios" ]]; then
    if [[ "$OSTYPE" != "darwin"* ]]; then
        print_error "ERROR: iOS testing is only supported on macOS"
        exit 1
    fi
    
    print_info "Checking iOS setup..."
    if ! command -v xcrun &> /dev/null; then
        print_error "ERROR: Xcode command line tools not found"
        print_error "Please install Xcode and command line tools"
        exit 1
    fi
    
    print_success "iOS setup verified"
fi

# Create directories if they don't exist
mkdir -p screenshots logs reports

# Set Maven options
MAVEN_OPTS="-Dplatform=$PLATFORM -DdeviceName=\"$DEVICE_NAME\" -DplatformVersion=$PLATFORM_VERSION"

echo ""
print_info "Starting test execution..."
echo "========================================"

# Run tests based on suite
case $TEST_SUITE in
    smoke)
        mvn clean test -Dgroups=smoke $MAVEN_OPTS
        ;;
    regression)
        mvn clean test -Dgroups=regression $MAVEN_OPTS
        ;;
    authentication)
        mvn clean test -Dtest=AuthenticationTests $MAVEN_OPTS
        ;;
    navigation)
        mvn clean test -Dtest=NavigationTests $MAVEN_OPTS
        ;;
    camera)
        mvn clean test -Dtest=CameraTests $MAVEN_OPTS
        ;;
    messaging)
        mvn clean test -Dtest=MessagingTests $MAVEN_OPTS
        ;;
    friends)
        mvn clean test -Dtest=FriendsTests $MAVEN_OPTS
        ;;
    profile)
        mvn clean test -Dtest=ProfileTests $MAVEN_OPTS
        ;;
    full)
        mvn clean test $MAVEN_OPTS
        ;;
    *)
        print_error "ERROR: Unknown test suite: $TEST_SUITE"
        print_error "Run '$0 --help' for available options"
        exit 1
        ;;
esac

TEST_EXIT_CODE=$?

echo ""
echo "========================================"
if [[ $TEST_EXIT_CODE -eq 0 ]]; then
    print_success "Test execution completed successfully"
else
    print_error "Test execution failed"
fi
echo "Exit code: $TEST_EXIT_CODE"

# Show results location
echo ""
print_info "Test Results:"
echo "  HTML Reports: target/surefire-reports/index.html"
echo "  Screenshots: screenshots/"
echo "  Logs: logs/"

# Generate Allure report if available
if command -v allure &> /dev/null; then
    if [[ -d "target/allure-results" ]]; then
        print_info "Generating Allure report..."
        allure generate target/allure-results -o target/allure-report --clean
        print_success "Allure report generated: target/allure-report/index.html"
    fi
fi

# Open reports if tests passed and on macOS
if [[ $TEST_EXIT_CODE -eq 0 && "$OSTYPE" == "darwin"* ]]; then
    echo ""
    read -p "Open test report? (y/N): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        if [[ -f "target/surefire-reports/index.html" ]]; then
            open target/surefire-reports/index.html
        elif [[ -f "target/allure-report/index.html" ]]; then
            open target/allure-report/index.html
        else
            print_warning "Report file not found"
        fi
    fi
fi

exit $TEST_EXIT_CODE