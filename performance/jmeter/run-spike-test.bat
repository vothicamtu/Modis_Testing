@echo off
REM Modis Spike Test Runner Script for Windows
REM Usage: run-spike-test.bat [users] [ramp_up] [duration]

setlocal

REM Default values
set USERS=500
set RAMP_UP=60
set DURATION=300
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
set TEST_PLAN=test-plans\spike-tests\modis-spike-test.jmx
set RESULT_FILE=results\spike-test-%timestamp%.jtl
set REPORT_DIR=reports\spike-test-%timestamp%

echo ========================================
echo Modis Spike Test Execution
echo ========================================
echo Test Plan: %TEST_PLAN%
echo Users: %USERS%
echo Ramp Up: %RAMP_UP% seconds
echo Duration: %DURATION% seconds
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

echo Starting JMeter spike test execution...
echo WARNING: This test will generate very high sudden load on the server
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
echo Spike Test Execution Completed!
echo ========================================
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