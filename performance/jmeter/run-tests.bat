@echo off
REM Modis Performance Testing - Batch Script for Windows
REM Usage: run-tests.bat [test-type] [users] [duration]

setlocal enabledelayedexpansion

REM Default values
set TEST_TYPE=%1
set USERS=%2
set DURATION=%3
set RAMPUP=%4

if "%TEST_TYPE%"=="" set TEST_TYPE=load
if "%USERS%"=="" set USERS=50
if "%DURATION%"=="" set DURATION=1800
if "%RAMPUP%"=="" set RAMPUP=300

REM Set JMeter path (adjust as needed)
set JMETER_HOME=C:\apache-jmeter
set JMETER_BIN=%JMETER_HOME%\bin\jmeter.bat

REM Check if JMeter exists
if not exist "%JMETER_BIN%" (
    echo ERROR: JMeter not found at %JMETER_BIN%
    echo Please install JMeter or update JMETER_HOME path
    exit /b 1
)

REM Create results and reports directories
if not exist "results" mkdir results
if not exist "reports" mkdir reports

REM Set timestamp for unique file names
for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
set "YY=%dt:~2,2%" & set "YYYY=%dt:~0,4%" & set "MM=%dt:~4,2%" & set "DD=%dt:~6,2%"
set "HH=%dt:~8,2%" & set "Min=%dt:~10,2%" & set "Sec=%dt:~12,2%"
set "timestamp=%YYYY%%MM%%DD%_%HH%%Min%%Sec%"

echo ========================================
echo Modis Performance Testing Suite
echo ========================================
echo Test Type: %TEST_TYPE%
echo Users: %USERS%
echo Duration: %DURATION% seconds
echo Ramp-up: %RAMPUP% seconds
echo Timestamp: %timestamp%
echo ========================================

REM Set test plan file based on test type
if "%TEST_TYPE%"=="load" (
    set TEST_PLAN=test-plans\modis-load-test.jmx
    set REPORT_NAME=load-test-%timestamp%
) else if "%TEST_TYPE%"=="stress" (
    set TEST_PLAN=test-plans\modis-stress-test.jmx
    set REPORT_NAME=stress-test-%timestamp%
) else if "%TEST_TYPE%"=="spike" (
    set TEST_PLAN=test-plans\modis-spike-test.jmx
    set REPORT_NAME=spike-test-%timestamp%
) else if "%TEST_TYPE%"=="endurance" (
    set TEST_PLAN=test-plans\modis-endurance-test.jmx
    set REPORT_NAME=endurance-test-%timestamp%
) else if "%TEST_TYPE%"=="image" (
    set TEST_PLAN=test-plans\modis-image-upload-test.jmx
    set REPORT_NAME=image-upload-test-%timestamp%
) else (
    echo ERROR: Invalid test type. Use: load, stress, spike, endurance, or image
    exit /b 1
)

REM Check if test plan exists
if not exist "%TEST_PLAN%" (
    echo ERROR: Test plan not found: %TEST_PLAN%
    exit /b 1
)

echo Running test plan: %TEST_PLAN%
echo Results will be saved to: results\%REPORT_NAME%.jtl
echo HTML report will be generated at: reports\%REPORT_NAME%\

REM Run JMeter test
"%JMETER_BIN%" -n -t "%TEST_PLAN%" ^
    -Jusers=%USERS% ^
    -Jrampup=%RAMPUP% ^
    -Jduration=%DURATION% ^
    -Jhost=modis-backend.onrender.com ^
    -l "results\%REPORT_NAME%.jtl" ^
    -e -o "reports\%REPORT_NAME%"

if %ERRORLEVEL% EQU 0 (
    echo ========================================
    echo Test completed successfully!
    echo ========================================
    echo Results: results\%REPORT_NAME%.jtl
    echo HTML Report: reports\%REPORT_NAME%\index.html
    echo ========================================
    
    REM Open HTML report in default browser
    start "" "reports\%REPORT_NAME%\index.html"
) else (
    echo ========================================
    echo Test failed with error code: %ERRORLEVEL%
    echo ========================================
)

pause