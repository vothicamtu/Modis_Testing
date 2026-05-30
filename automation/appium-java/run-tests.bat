@echo off
REM Modis Mobile App - Appium Java Automation Tests
REM Windows Batch Script for Test Execution

echo ========================================
echo Modis Mobile App Automation Tests
echo ========================================
echo.

REM Set default values
set PLATFORM=android
set DEVICE_NAME=Android Emulator
set PLATFORM_VERSION=11.0
set TEST_SUITE=smoke
set APPIUM_SERVER=http://127.0.0.1:4723

REM Parse command line arguments
:parse_args
if "%1"=="" goto :run_tests
if "%1"=="--platform" (
    set PLATFORM=%2
    shift
    shift
    goto :parse_args
)
if "%1"=="--device" (
    set DEVICE_NAME=%2
    shift
    shift
    goto :parse_args
)
if "%1"=="--version" (
    set PLATFORM_VERSION=%2
    shift
    shift
    goto :parse_args
)
if "%1"=="--suite" (
    set TEST_SUITE=%2
    shift
    shift
    goto :parse_args
)
if "%1"=="--server" (
    set APPIUM_SERVER=%2
    shift
    shift
    goto :parse_args
)
if "%1"=="--help" goto :show_help
shift
goto :parse_args

:show_help
echo Usage: run-tests.bat [OPTIONS]
echo.
echo Options:
echo   --platform PLATFORM     Target platform (android/ios) [default: android]
echo   --device DEVICE_NAME     Device name [default: Android Emulator]
echo   --version VERSION        Platform version [default: 11.0]
echo   --suite SUITE           Test suite to run [default: smoke]
echo   --server SERVER_URL     Appium server URL [default: http://127.0.0.1:4723]
echo   --help                  Show this help message
echo.
echo Available test suites:
echo   smoke                   Critical path tests
echo   regression              All regression tests
echo   authentication          Authentication tests only
echo   friends                 Friend management tests only
echo   photo-sharing           Photo sharing tests only
echo   feed                    Feed tests only
echo   search                  Search tests only
echo   messaging               Messaging tests only
echo   full                    All tests
echo.
echo Examples:
echo   run-tests.bat
echo   run-tests.bat --suite regression
echo   run-tests.bat --platform android --device "Pixel 4" --suite smoke
echo   run-tests.bat --platform ios --device "iPhone 12" --version 14.0
goto :eof

:run_tests
echo Configuration:
echo   Platform: %PLATFORM%
echo   Device: %DEVICE_NAME%
echo   Version: %PLATFORM_VERSION%
echo   Test Suite: %TEST_SUITE%
echo   Appium Server: %APPIUM_SERVER%
echo.

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 11 or higher and add it to PATH
    exit /b 1
)

REM Check if Maven is installed
mvn -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven and add it to PATH
    exit /b 1
)

REM Check if Appium server is running
echo Checking Appium server connection...
curl -s %APPIUM_SERVER%/status >nul 2>&1
if errorlevel 1 (
    echo WARNING: Cannot connect to Appium server at %APPIUM_SERVER%
    echo Please make sure Appium server is running:
    echo   appium
    echo.
    set /p continue="Continue anyway? (y/N): "
    if /i not "%continue%"=="y" (
        echo Test execution cancelled
        exit /b 1
    )
)

REM Check device connection for Android
if /i "%PLATFORM%"=="android" (
    echo Checking Android device connection...
    adb devices | findstr "device$" >nul
    if errorlevel 1 (
        echo WARNING: No Android devices found
        echo Please connect an Android device or start an emulator
        echo   adb devices
        echo.
        set /p continue="Continue anyway? (y/N): "
        if /i not "%continue%"=="y" (
            echo Test execution cancelled
            exit /b 1
        )
    ) else (
        echo Android device(s) found
    )
)

REM Create directories if they don't exist
if not exist "screenshots" mkdir screenshots
if not exist "logs" mkdir logs
if not exist "reports" mkdir reports

REM Set system properties
set MAVEN_OPTS=-Dplatform=%PLATFORM% -DdeviceName="%DEVICE_NAME%" -DplatformVersion=%PLATFORM_VERSION%

echo.
echo Starting test execution...
echo ========================================

REM Run tests based on suite
if /i "%TEST_SUITE%"=="smoke" (
    mvn clean test -Dgroups=smoke %MAVEN_OPTS%
) else if /i "%TEST_SUITE%"=="regression" (
    mvn clean test -Dgroups=regression %MAVEN_OPTS%
) else if /i "%TEST_SUITE%"=="authentication" (
    mvn clean test -Dtest=AuthenticationTests %MAVEN_OPTS%
) else if /i "%TEST_SUITE%"=="friends" (
    mvn clean test -Dtest=FriendsTests %MAVEN_OPTS%
) else if /i "%TEST_SUITE%"=="photo-sharing" (
    mvn clean test -Dtest=PhotoSharingTests %MAVEN_OPTS%
) else if /i "%TEST_SUITE%"=="feed" (
    mvn clean test -Dtest=FeedTests %MAVEN_OPTS%
) else if /i "%TEST_SUITE%"=="search" (
    mvn clean test -Dtest=SearchTests %MAVEN_OPTS%
) else if /i "%TEST_SUITE%"=="messaging" (
    mvn clean test -Dtest=MessagingTests %MAVEN_OPTS%
) else if /i "%TEST_SUITE%"=="full" (
    mvn clean test %MAVEN_OPTS%
) else (
    echo ERROR: Unknown test suite: %TEST_SUITE%
    echo Run 'run-tests.bat --help' for available options
    exit /b 1
)

set TEST_EXIT_CODE=%errorlevel%

echo.
echo ========================================
echo Test execution completed
echo Exit code: %TEST_EXIT_CODE%

REM Show results location
echo.
echo Test Results:
echo   HTML Reports: target\surefire-reports\index.html
echo   Screenshots: screenshots\
echo   Logs: logs\

REM Open reports if tests passed
if %TEST_EXIT_CODE%==0 (
    echo.
    set /p open_report="Open test report? (y/N): "
    if /i "%open_report%"=="y" (
        if exist "target\surefire-reports\index.html" (
            start target\surefire-reports\index.html
        ) else (
            echo Report file not found
        )
    )
)

exit /b %TEST_EXIT_CODE%
