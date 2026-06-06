@echo off
REM Modis Complete Performance Test Suite Runner
REM Runs all test types in sequence with proper intervals

setlocal

REM Resolve JMeter executable. Prefer JMETER_HOME, then jmeter.bat from PATH.
set "JMETER_CMD="
if not "%JMETER_HOME%"=="" if exist "%JMETER_HOME%\bin\jmeter.bat" set "JMETER_CMD=%JMETER_HOME%\bin\jmeter.bat"
if "%JMETER_CMD%"=="" for /f "delims=" %%i in ('where jmeter.bat 2^>nul') do if "%JMETER_CMD%"=="" set "JMETER_CMD=%%i"
if "%JMETER_CMD%"=="" for /f "delims=" %%i in ('where jmeter 2^>nul') do if "%JMETER_CMD%"=="" set "JMETER_CMD=%%i"
if "%JMETER_CMD%"=="" (
    echo ERROR: JMeter not found. Please install Apache JMeter and add it to PATH, or set JMETER_HOME.
    pause
    exit /b 1
)

echo ========================================
echo Modis Complete Performance Test Suite
echo ========================================
echo This script will run all performance tests:
echo 1. Load Test (50 users, 10 minutes)
echo 2. Stress Test (200 users, 15 minutes)  
echo 3. Image Upload Test (20 users, 10 minutes)
echo 4. WebSocket Test (30 users, 10 minutes)
echo 5. Spike Test (500 users, 5 minutes)
echo.
echo Total estimated time: ~60 minutes
echo ========================================

set /p CONFIRM="Continue with full test suite? (y/n): "
if /i not "%CONFIRM%"=="y" (
    echo Test suite cancelled by user
    pause
    exit /b 0
)

REM Create master results directory
for /f %%a in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd_HHmmss"') do set "master_timestamp=%%a"

set MASTER_DIR=reports\full-suite-%master_timestamp%
mkdir "%MASTER_DIR%"

echo.
echo ========================================
echo Starting Test Suite at %date% %time%
echo Master Results Directory: %MASTER_DIR%
echo JMeter: %JMETER_CMD%
echo ========================================

REM Test 1: Load Test
echo.
echo [1/5] Running Load Test...
call run-load-test.bat 50 300 600
if errorlevel 1 (
    echo ERROR: Load test failed
    goto :error
)

REM Wait between tests
echo Waiting 2 minutes before next test...
timeout /t 120 /nobreak

REM Test 2: Stress Test  
echo.
echo [2/5] Running Stress Test...
call run-stress-test.bat 200 180 900
if errorlevel 1 (
    echo ERROR: Stress test failed
    goto :error
)

REM Wait between tests
echo Waiting 3 minutes before next test...
timeout /t 180 /nobreak

REM Test 3: Image Upload Test
echo.
echo [3/5] Running Image Upload Test...
call run-image-upload-test.bat 20 120 600
if errorlevel 1 (
    echo ERROR: Image upload test failed
    goto :error
)

REM Wait between tests
echo Waiting 2 minutes before next test...
timeout /t 120 /nobreak

REM Test 4: WebSocket Test
echo.
echo [4/5] Running WebSocket Test...
call "%JMETER_CMD%" -n -t test-plans\load-tests\modis-websocket-test.jmx -Jusers=30 -Jramp_up=180 -Jduration=600 -l results\websocket-test-%master_timestamp%.jtl -e -o reports\websocket-test-%master_timestamp%
if errorlevel 1 (
    echo ERROR: WebSocket test failed
    goto :error
)

REM Wait between tests
echo Waiting 2 minutes before final test...
timeout /t 120 /nobreak

REM Test 5: Spike Test
echo.
echo [5/5] Running Spike Test...
call run-spike-test.bat 500 60 300
if errorlevel 1 (
    echo ERROR: Spike test failed
    goto :error
)

REM Generate summary report
echo.
echo ========================================
echo Generating Test Suite Summary...
echo ========================================

echo ^<html^>^<head^>^<title^>Modis Performance Test Suite Results^</title^>^</head^> > "%MASTER_DIR%\index.html"
echo ^<body^>^<h1^>Modis Performance Test Suite Results^</h1^> >> "%MASTER_DIR%\index.html"
echo ^<p^>Test Suite Executed: %date% %time%^</p^> >> "%MASTER_DIR%\index.html"
echo ^<h2^>Test Results^</h2^>^<ul^> >> "%MASTER_DIR%\index.html"

REM Find and link to individual reports
for /d %%d in (reports\load-test-*) do (
    echo ^<li^>^<a href="../%%d/index.html"^>Load Test Results^</a^>^</li^> >> "%MASTER_DIR%\index.html"
    goto :found_load
)
:found_load

for /d %%d in (reports\stress-test-*) do (
    echo ^<li^>^<a href="../%%d/index.html"^>Stress Test Results^</a^>^</li^> >> "%MASTER_DIR%\index.html"
    goto :found_stress
)
:found_stress

for /d %%d in (reports\image-upload-test-*) do (
    echo ^<li^>^<a href="../%%d/index.html"^>Image Upload Test Results^</a^>^</li^> >> "%MASTER_DIR%\index.html"
    goto :found_upload
)
:found_upload

for /d %%d in (reports\websocket-test-*) do (
    echo ^<li^>^<a href="../%%d/index.html"^>WebSocket Test Results^</a^>^</li^> >> "%MASTER_DIR%\index.html"
    goto :found_ws
)
:found_ws

for /d %%d in (reports\spike-test-*) do (
    echo ^<li^>^<a href="../%%d/index.html"^>Spike Test Results^</a^>^</li^> >> "%MASTER_DIR%\index.html"
    goto :found_spike
)
:found_spike

echo ^</ul^>^</body^>^</html^> >> "%MASTER_DIR%\index.html"

echo.
echo ========================================
echo Test Suite Completed Successfully!
echo ========================================
echo Suite completed at: %date% %time%
echo Master summary: %MASTER_DIR%\index.html
echo.
echo All individual test reports are available in their respective directories.
echo ========================================

set /p OPEN_SUMMARY="Open test suite summary? (y/n): "
if /i "%OPEN_SUMMARY%"=="y" (
    start "" "%MASTER_DIR%\index.html"
)

goto :end

:error
echo.
echo ========================================
echo Test Suite Failed!
echo ========================================
echo One or more tests failed. Check the error messages above.
echo Partial results may be available in the reports directory.
echo ========================================
pause
exit /b 1

:end
pause
