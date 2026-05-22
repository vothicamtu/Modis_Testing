#!/bin/bash

echo "========================================"
echo "    MODIS REAL DATA TESTING SCRIPT"
echo "========================================"
echo

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo -e "${RED}Java is not installed or not in PATH${NC}"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Maven is not installed or not in PATH${NC}"
    exit 1
fi

echo -e "${GREEN}Checking Java version...${NC}"
java -version
echo

echo -e "${GREEN}Checking Maven version...${NC}"
mvn -version
echo

# Start Appium server in background
echo -e "${GREEN}Starting Appium server...${NC}"
appium --port 4723 --log-level info &
APPIUM_PID=$!
sleep 5

echo
echo "========================================"
echo "Select test type to run:"
echo "========================================"
echo "1. All Real Data Tests"
echo "2. Authentication Tests Only"
echo "3. Login Retry Flow Tests Only"
echo "4. Friends Tests with Real Data"
echo "5. Messaging Tests with Real Data"
echo "6. Camera Tests with Real Data"
echo "7. Smoke Tests Only"
echo "8. Custom Test Selection"
echo "========================================"

read -p "Enter your choice (1-8): " choice

case $choice in
    1)
        echo -e "${BLUE}Running all real data tests...${NC}"
        mvn clean test -Dsuite=modis-real-data-tests.xml -Dgroups=data,regression
        ;;
    2)
        echo -e "${BLUE}Running authentication tests with real data...${NC}"
        mvn clean test -Dtest=AuthenticationTests -Dgroups=authentication,data
        ;;
    3)
        echo -e "${BLUE}Running login retry flow tests...${NC}"
        mvn clean test -Dtest=AuthenticationTests -Dmethods=testLoginRetryFlow,testValidLoginWithRealData,testInvalidLoginWithRealData
        ;;
    4)
        echo -e "${BLUE}Running friends tests with real data...${NC}"
        mvn clean test -Dtest=FriendsTests -Dgroups=friends,data
        ;;
    5)
        echo -e "${BLUE}Running messaging tests with real data...${NC}"
        mvn clean test -Dtest=MessagingTests -Dgroups=messaging,data
        ;;
    6)
        echo -e "${BLUE}Running camera tests with real data...${NC}"
        mvn clean test -Dtest=CameraTests -Dgroups=camera,data
        ;;
    7)
        echo -e "${BLUE}Running smoke tests only...${NC}"
        mvn clean test -Dgroups=smoke
        ;;
    8)
        echo
        echo "Available test classes:"
        echo "- AuthenticationTests"
        echo "- FriendsTests"
        echo "- MessagingTests"
        echo "- CameraTests"
        echo "- NavigationTests"
        echo "- ProfileTests"
        echo
        read -p "Enter test class name: " testclass
        read -p "Enter groups (comma-separated, e.g., regression,data): " testgroups
        echo -e "${BLUE}Running custom tests...${NC}"
        mvn clean test -Dtest=$testclass -Dgroups=$testgroups
        ;;
    *)
        echo -e "${YELLOW}Invalid choice. Running default smoke tests...${NC}"
        mvn clean test -Dgroups=smoke
        ;;
esac

echo
echo "========================================"
echo -e "${GREEN}Test execution completed!${NC}"
echo "========================================"
echo

# Generate Allure report
echo -e "${GREEN}Generating Allure report...${NC}"
if [ -d "target/allure-results" ]; then
    if command -v allure &> /dev/null; then
        allure generate target/allure-results --clean -o target/allure-report
        echo -e "${GREEN}Allure report generated at: target/allure-report/index.html${NC}"
        
        read -p "Open Allure report in browser? (y/n): " openreport
        if [[ $openreport == "y" || $openreport == "Y" ]]; then
            if command -v xdg-open &> /dev/null; then
                xdg-open target/allure-report/index.html
            elif command -v open &> /dev/null; then
                open target/allure-report/index.html
            else
                echo -e "${YELLOW}Please open target/allure-report/index.html manually${NC}"
            fi
        fi
    else
        echo -e "${YELLOW}Allure is not installed. Install it to generate reports.${NC}"
    fi
else
    echo -e "${YELLOW}No Allure results found. Make sure Allure is configured properly.${NC}"
fi

# Stop Appium server
echo -e "${GREEN}Stopping Appium server...${NC}"
kill $APPIUM_PID 2>/dev/null

echo
echo "========================================"
echo -e "${GREEN}Real Data Test Summary:${NC}"
echo "========================================"
echo "- Test data location: src/test/resources/testdata/"
echo "- Test reports: target/surefire-reports/"
echo "- Screenshots: screenshots/"
echo "- Logs: logs/"
echo
echo -e "${GREEN}Check the reports for detailed results!${NC}"
echo "========================================"