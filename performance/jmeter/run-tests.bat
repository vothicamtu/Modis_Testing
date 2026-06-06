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

REM Resolve JMeter executable. Prefer JMETER_HOME, then jmeter.bat from PATH.
set "JMETER_CMD="
if not "%JMETER_HOME%"=="" if exist "%JMETER_HOME%\bin\jmeter.bat" set "JMETER_CMD=%JMETER_HOME%\bin\jmeter.bat"
if "%JMETER_CMD%"=="" for /f "delims=" %%i in ('where jmeter.bat 2^>nul') do if "%JMETER_CMD%"=="" set "JMETER_CMD=%%i"
if "%JMETER_CMD%"=="" for /f "delims=" %%i in ('where jmeter 2^>nul') do if "%JMETER_CMD%"=="" set "JMETER_CMD=%%i"

REM Check if JMeter exists
if "%JMETER_CMD%"=="" (
    echo ERROR: JMeter not found. Please install Apache JMeter and add it to PATH, or set JMETER_HOME.
    exit /b 1
)
call "%JMETER_CMD%" -v >nul 2>&1
if errorlevel 1 (
    echo ERROR: JMeter not found. Please install Apache JMeter and add it to PATH, or set JMETER_HOME.
    exit /b 1
)

REM Create results and reports directories
if not exist "results" mkdir results
if not exist "reports" mkdir reports

REM Set timestamp for unique file names
for /f %%a in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd_HHmmss"') do set "timestamp=%%a"

echo ========================================
echo Modis Performance Testing Suite
echo ========================================
echo Test Type: %TEST_TYPE%
echo Users: %USERS%
echo Duration: %DURATION% seconds
echo Ramp-up: %RAMPUP% seconds
echo Timestamp: %timestamp%
echo JMeter: %JMETER_CMD%
echo ========================================

REM Set test plan file based on test type
if "%TEST_TYPE%"=="load" (
    set TEST_PLAN=test-plans\load-tests\modis-load-test.jmx
    set REPORT_NAME=load-test-%timestamp%
) else if "%TEST_TYPE%"=="stress" (
    set TEST_PLAN=test-plans\stress-tests\modis-stress-test.jmx
    set REPORT_NAME=stress-test-%timestamp%
) else if "%TEST_TYPE%"=="spike" (
    set TEST_PLAN=test-plans\spike-tests\modis-spike-test.jmx
    set REPORT_NAME=spike-test-%timestamp%
) else if "%TEST_TYPE%"=="endurance" (
    set TEST_PLAN=test-plans\endurance-tests\modis-endurance-test.jmx
    set REPORT_NAME=endurance-test-%timestamp%
) else if "%TEST_TYPE%"=="image" (
    set TEST_PLAN=test-plans\load-tests\modis-image-upload-test.jmx
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
call "%JMETER_CMD%" -n -t "%TEST_PLAN%" ^
    -Jusers=%USERS% ^
    -Jramp_up=%RAMPUP% ^
    -Jduration=%DURATION% ^
    -Jrun_id=%timestamp% ^
    -Jbase_url=modis-backend.onrender.com ^
    -l "results\%REPORT_NAME%.jtl" ^
    -e -o "reports\%REPORT_NAME%"

if %ERRORLEVEL% EQU 0 if exist "reports\%REPORT_NAME%\index.html" (
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
    echo Test failed or HTML report was not generated.
    echo Expected report: reports\%REPORT_NAME%\index.html
    echo ========================================
    exit /b 1
)

pause
