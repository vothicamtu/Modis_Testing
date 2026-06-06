@echo off
REM Modis Stress Test Runner Script for Windows
REM Usage: run-stress-test.bat [users] [ramp_up] [duration]

setlocal

REM Default values
set USERS=200
set RAMP_UP=180
set DURATION=900
set BASE_URL=modis-backend.onrender.com

REM Override with command line arguments if provided
if not "%1"=="" set USERS=%1
if not "%2"=="" set RAMP_UP=%2
if not "%3"=="" set DURATION=%3

REM Create timestamp for unique file names
for /f %%a in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd_HHmmss"') do set "timestamp=%%a"

REM Resolve JMeter executable. Prefer JMETER_HOME, then jmeter.bat from PATH.
set "JMETER_CMD="
if not "%JMETER_HOME%"=="" if exist "%JMETER_HOME%\bin\jmeter.bat" set "JMETER_CMD=%JMETER_HOME%\bin\jmeter.bat"
if "%JMETER_CMD%"=="" for /f "delims=" %%i in ('where jmeter.bat 2^>nul') do if "%JMETER_CMD%"=="" set "JMETER_CMD=%%i"
if "%JMETER_CMD%"=="" for /f "delims=" %%i in ('where jmeter 2^>nul') do if "%JMETER_CMD%"=="" set "JMETER_CMD=%%i"

REM File paths
set TEST_PLAN=test-plans\stress-tests\modis-stress-test.jmx
set RESULT_FILE=results\stress-test-%timestamp%.jtl
set REPORT_DIR=reports\stress-test-%timestamp%

echo ========================================
echo Modis Stress Test Execution
echo ========================================
echo Test Plan: %TEST_PLAN%
echo Users: %USERS%
echo Ramp Up: %RAMP_UP% seconds
echo Duration: %DURATION% seconds
echo Base URL: %BASE_URL%
echo JMeter: %JMETER_CMD%
echo Result File: %RESULT_FILE%
echo Report Directory: %REPORT_DIR%
echo ========================================

REM Check if JMeter is available
if "%JMETER_CMD%"=="" (
    echo ERROR: JMeter not found in PATH
    echo Please install Apache JMeter and add it to your PATH
    echo Download from: https://jmeter.apache.org/download_jmeter.cgi
    pause
    exit /b 1
)
call "%JMETER_CMD%" -v >nul 2>&1
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

echo Starting JMeter stress test execution...
echo WARNING: This test will generate high load on the server
echo.

REM Run JMeter test
call "%JMETER_CMD%" -n -t "%TEST_PLAN%" ^
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
if not exist "%REPORT_DIR%\index.html" (
    echo.
    echo ERROR: HTML report was not generated: %REPORT_DIR%\index.html
    pause
    exit /b 1
)

echo.
echo ========================================
echo Stress Test Execution Completed!
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
