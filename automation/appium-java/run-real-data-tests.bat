@echo off
echo ========================================
echo    MODIS REAL DATA TESTING SCRIPT
echo ========================================
echo.

REM Set environment variables
set JAVA_HOME=C:\Program Files\Java\jdk-11.0.16
set MAVEN_HOME=C:\apache-maven-3.8.6
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

echo Checking Java version...
java -version
echo.

echo Checking Maven version...
mvn -version
echo.

echo Starting Appium server...
start "Appium Server" cmd /c "appium --port 4723 --log-level info"
timeout /t 5 /nobreak > nul

echo.
echo ========================================
echo Select test type to run:
echo ========================================
echo 1. All Real Data Tests
echo 2. Authentication Tests Only  
echo 3. Login Retry Flow Tests Only
echo 4. Friends Tests with Real Data
echo 5. Messaging Tests with Real Data
echo 6. Camera Tests with Real Data
echo 7. Smoke Tests Only
echo 8. Custom Test Selection
echo ========================================

set /p choice="Enter your choice (1-8): "

if "%choice%"=="1" (
    echo Running all real data tests...
    mvn clean test -Dsuite=modis-real-data-tests.xml -Dgroups=data,regression
) else if "%choice%"=="2" (
    echo Running authentication tests with real data...
    mvn clean test -Dtest=AuthenticationTests -Dgroups=authentication,data
) else if "%choice%"=="3" (
    echo Running login retry flow tests...
    mvn clean test -Dtest=AuthenticationTests -Dmethods=testLoginRetryFlow,testValidLoginWithRealData,testInvalidLoginWithRealData
) else if "%choice%"=="4" (
    echo Running friends tests with real data...
    mvn clean test -Dtest=FriendsTests -Dgroups=friends,data
) else if "%choice%"=="5" (
    echo Running messaging tests with real data...
    mvn clean test -Dtest=MessagingTests -Dgroups=messaging,data
) else if "%choice%"=="6" (
    echo Running camera tests with real data...
    mvn clean test -Dtest=CameraTests -Dgroups=camera,data
) else if "%choice%"=="7" (
    echo Running smoke tests only...
    mvn clean test -Dgroups=smoke
) else if "%choice%"=="8" (
    echo.
    echo Available test classes:
    echo - AuthenticationTests
    echo - FriendsTests  
    echo - MessagingTests
    echo - CameraTests
    echo - NavigationTests
    echo - ProfileTests
    echo.
    set /p testclass="Enter test class name: "
    set /p testgroups="Enter groups (comma-separated, e.g., regression,data): "
    echo Running custom tests...
    mvn clean test -Dtest=%testclass% -Dgroups=%testgroups%
) else (
    echo Invalid choice. Running default smoke tests...
    mvn clean test -Dgroups=smoke
)

echo.
echo ========================================
echo Test execution completed!
echo ========================================
echo.

REM Generate Allure report
echo Generating Allure report...
if exist "target\allure-results" (
    allure generate target\allure-results --clean -o target\allure-report
    echo Allure report generated at: target\allure-report\index.html
    
    set /p openreport="Open Allure report in browser? (y/n): "
    if /i "%openreport%"=="y" (
        start target\allure-report\index.html
    )
) else (
    echo No Allure results found. Make sure Allure is configured properly.
)

echo.
echo ========================================
echo Real Data Test Summary:
echo ========================================
echo - Test data location: src\test\resources\testdata\
echo - Test reports: target\surefire-reports\
echo - Screenshots: screenshots\
echo - Logs: logs\
echo.
echo Check the reports for detailed results!
echo ========================================

pause