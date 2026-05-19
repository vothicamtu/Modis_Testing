@echo off
REM Modis Endurance Test Runner Script for Windows
REM Usage: run-endurance-test.bat [users] [ramp_up] [duration]

setlocal

REM Default values
set USERS=30
set RAMP_UP=600
set DURATION=7200
set BASE_URL=https://modis-backend.onrender.com

REM Override with command line arguments if provided
if not "%1"=="" set USERS=%1
if not "%2"=="" set RAMP_UP=%2
if not "%3"=="" set DURATION=%3

REM Create timestamp for unique file names
for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
set "YY=%dt:~2,2%" & set "YYYY=%dt:~0,4%" & set "MM=%dt:~4,2%" & set "DD=%dt:~6,2%"
set "HH=%dt:~8,2%" & set "Min=%dt:~10,2%" & set "Sec=%dt:~12,2%"
set "timestamp=%YYYY%%MM%%DD%_%HH%%Min%%Sec%"

REM File paths
set TEST_PLAN=test-plans\endurance-tests\modis-endurance-test.jmx
set RESULT_FILE=results\endurance-test-%timestamp%.jtl
set REPORT_DIR=reports\endurance-test-%timestamp%

REM Calculate duration in hours and minutes
set /a HOURS=%DURATION% / 3600
set /a MINUTES=(%DURATION% %% 3600) / 60

echo ========================================
echo Modis Endurance Test Execution
echo ========================================
echo Test Plan: %TEST_PLAN%
echo Users: %USERS%
echo Ramp Up: %RAMP_UP% seconds
echo Duration: %DURATION% seconds (%HOURS%h %MINUTES%m)
echo Base URL: %BASE_URL%
echo Result File: %RESULT_FILE%
echo Report Directory: %REPORT_DIR%
echo ========================================

REM Check if JMeter is available
jmeter -v >nul 2>&1
if errorlevel 1 (
    echo ERROR: JMeter not found in PATH
    echo Please install Apache JMeter and add it to your PATH
    echo Download from: https://jmeter.apache.org/download_jmeter.cgi
    pause
    exit /b 1
)

REM Check if test plan exists
if not exist "%TEST_PLAN%" (
    echo ERROR: Test plan not found: %TEST_PLAN%
    echo Please ensure you are running this script from the jmeter directory
    pause
    exit /b 1
)

REM Create directories if they don't exist
if not exist "results" mkdir results
if not exist "reports" mkdir reports

echo Starting JMeter endurance test execution...
echo WARNING: This test will run for %HOURS% hours and %MINUTES% minutes
echo Make sure your system can handle long-running tests
echo.

set /p CONFIRM="Continue with endurance test? (y/n): "
if /i not "%CONFIRM%"=="y" (
    echo Test cancelled by user
    pause
    exit /b 0
)

echo.
echo Test started at: %date% %time%
echo Estimated completion: Will run for %HOURS%h %MINUTES%m
echo.

REM Run JMeter test
jmeter -n -t "%TEST_PLAN%" ^
    -Jusers=%USERS% ^
    -Jramp_up=%RAMP_UP% ^
    -Jduration=%DURATION% ^
    -Jbase_url=%BASE_URL% ^
    -l "%RESULT_FILE%" ^
    -e -o "%REPORT_DIR%"

if errorlevel 1 (
    echo.
    echo ERROR: JMeter test execution failed
    pause
    exit /b 1
)

echo.
echo ========================================
echo Endurance Test Execution Completed!
echo ========================================
echo Test completed at: %date% %time%
echo Results saved to: %RESULT_FILE%
echo HTML Report generated: %REPORT_DIR%\index.html
echo.
echo To view the report, open: %REPORT_DIR%\index.html
echo.

REM Ask if user wants to open the report
set /p OPEN_REPORT="Open HTML report now? (y/n): "
if /i "%OPEN_REPORT%"=="y" (
    start "" "%REPORT_DIR%\index.html"
)

pause