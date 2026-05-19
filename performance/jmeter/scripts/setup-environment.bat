@echo off
REM Modis Performance Testing Environment Setup Script
REM Sets up JMeter environment and validates configuration

setlocal

echo ========================================
echo Modis Performance Testing Setup
echo ========================================
echo This script will:
echo 1. Validate JMeter installation
echo 2. Check required files and directories
echo 3. Create sample image for upload tests
echo 4. Validate test data files
echo 5. Set up environment variables
echo ========================================

REM Check JMeter installation
echo [1/5] Checking JMeter installation...
jmeter -v >nul 2>&1
if errorlevel 1 (
    echo ERROR: JMeter not found in PATH
    echo.
    echo Please install Apache JMeter:
    echo 1. Download from: https://jmeter.apache.org/download_jmeter.cgi
    echo 2. Extract to a directory (e.g., C:\apache-jmeter-5.6.3)
    echo 3. Add bin directory to PATH environment variable
    echo 4. Restart command prompt and run this script again
    echo.
    pause
    exit /b 1
)

REM Get JMeter version
for /f "tokens=*" %%i in ('jmeter -v 2^>^&1 ^| findstr "Version"') do set JMETER_VERSION=%%i
echo ✓ JMeter found: %JMETER_VERSION%

REM Check and create directories
echo.
echo [2/5] Checking directory structure...
set REQUIRED_DIRS=test-plans test-plans\load-tests test-plans\stress-tests test-plans\spike-tests test-plans\endurance-tests test-data configs results reports docs scripts

for %%d in (%REQUIRED_DIRS%) do (
    if not exist "%%d" (
        echo Creating directory: %%d
        mkdir "%%d"
    ) else (
        echo ✓ Directory exists: %%d
    )
)

REM Create sample image if not exists
echo.
echo [3/5] Setting up sample image for upload tests...
if not exist "test-data\sample-image.jpg" (
    echo Creating sample image using PowerShell...
    powershell -Command "& {
        Add-Type -AssemblyName System.Drawing
        $bitmap = New-Object System.Drawing.Bitmap(400, 400)
        $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
        $graphics.Clear([System.Drawing.Color]::LightBlue)
        
        $font = New-Object System.Drawing.Font('Arial', 24, [System.Drawing.FontStyle]::Bold)
        $brush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::Black)
        $text = 'Test Image'
        $textSize = $graphics.MeasureString($text, $font)
        $x = (400 - $textSize.Width) / 2
        $y = (400 - $textSize.Height) / 2
        $graphics.DrawString($text, $font, $brush, $x, $y)
        
        $bitmap.Save('test-data\sample-image.jpg', [System.Drawing.Imaging.ImageFormat]::Jpeg)
        $graphics.Dispose()
        $bitmap.Dispose()
        $font.Dispose()
        $brush.Dispose()
    }"
    
    if exist "test-data\sample-image.jpg" (
        echo ✓ Sample image created successfully
    ) else (
        echo ⚠ Could not create sample image automatically
        echo Please create test-data\sample-image.jpg manually
        echo See test-data\sample-image-info.txt for instructions
    )
) else (
    echo ✓ Sample image already exists
)

REM Validate test data files
echo.
echo [4/5] Validating test data files...
set TEST_DATA_FILES=users.csv post-content.csv reactions.csv messages.csv

for %%f in (%TEST_DATA_FILES%) do (
    if exist "test-data\%%f" (
        echo ✓ Test data file exists: %%f
        REM Count lines in CSV (excluding header)
        for /f %%i in ('type "test-data\%%f" ^| find /c /v ""') do set LINE_COUNT=%%i
        set /a DATA_COUNT=!LINE_COUNT!-1
        echo   - Contains !DATA_COUNT! data records
    ) else (
        echo ❌ Missing test data file: %%f
        set MISSING_FILES=1
    )
)

if defined MISSING_FILES (
    echo.
    echo ERROR: Some test data files are missing
    echo Please ensure all CSV files are present in test-data directory
    pause
    exit /b 1
)

REM Set up environment variables
echo.
echo [5/5] Setting up environment variables...

REM Create environment configuration file
echo # Modis Performance Testing Environment Configuration > .env
echo # Generated on %date% %time% >> .env
echo. >> .env
echo # JMeter Configuration >> .env
echo JMETER_HOME=%JMETER_HOME% >> .env
echo JMETER_VERSION=%JMETER_VERSION% >> .env
echo. >> .env
echo # Test Configuration >> .env
echo MODIS_BASE_URL=https://modis-backend.onrender.com >> .env
echo MODIS_WS_URL=wss://modis-backend.onrender.com/ws >> .env
echo. >> .env
echo # Default Test Parameters >> .env
echo DEFAULT_LOAD_USERS=50 >> .env
echo DEFAULT_STRESS_USERS=200 >> .env
echo DEFAULT_SPIKE_USERS=500 >> .env
echo DEFAULT_ENDURANCE_USERS=30 >> .env
echo. >> .env
echo # Timeouts (milliseconds) >> .env
echo DEFAULT_CONNECT_TIMEOUT=30000 >> .env
echo DEFAULT_RESPONSE_TIMEOUT=90000 >> .env
echo UPLOAD_TIMEOUT=120000 >> .env

echo ✓ Environment configuration saved to .env

REM Validate JMeter test plans
echo.
echo Validating JMeter test plans...
set TEST_PLANS=test-plans\load-tests\modis-load-test.jmx test-plans\stress-tests\modis-stress-test.jmx test-plans\spike-tests\modis-spike-test.jmx test-plans\endurance-tests\modis-endurance-test.jmx

for %%t in (%TEST_PLANS%) do (
    if exist "%%t" (
        echo ✓ Test plan exists: %%t
        REM Basic XML validation
        findstr /c:"<?xml" "%%t" >nul
        if errorlevel 1 (
            echo ⚠ Warning: %%t may not be valid XML
        )
    ) else (
        echo ❌ Missing test plan: %%t
        set MISSING_PLANS=1
    )
)

if defined MISSING_PLANS (
    echo.
    echo ERROR: Some test plans are missing
    echo Please ensure all .jmx files are present
    pause
    exit /b 1
)

REM Create quick test script
echo.
echo Creating quick test script...
echo @echo off > quick-test.bat
echo REM Quick smoke test for Modis performance testing >> quick-test.bat
echo echo Running quick smoke test... >> quick-test.bat
echo jmeter -n -t test-plans\load-tests\modis-load-test.jmx -Jusers=1 -Jramp_up=10 -Jduration=60 -l results\smoke-test.jtl >> quick-test.bat
echo if errorlevel 1 ( >> quick-test.bat
echo     echo Smoke test failed! >> quick-test.bat
echo     pause >> quick-test.bat
echo     exit /b 1 >> quick-test.bat
echo ^) >> quick-test.bat
echo echo Smoke test passed! >> quick-test.bat
echo pause >> quick-test.bat

echo ✓ Quick test script created: quick-test.bat

REM Performance recommendations
echo.
echo ========================================
echo Setup Complete! 
echo ========================================
echo.
echo Next Steps:
echo 1. Run quick smoke test: quick-test.bat
echo 2. Review configuration in .env file
echo 3. Customize test parameters as needed
echo 4. Run full test suite: run-all-tests.bat
echo.
echo Performance Recommendations:
echo - Ensure at least 4GB RAM for JMeter
echo - Use SSD storage for better I/O performance
echo - Close unnecessary applications during testing
echo - Monitor system resources during tests
echo.
echo Documentation:
echo - README.md: Getting started guide
echo - docs\METRICS_GUIDE.md: Understanding results
echo - docs\BEST_PRACTICES.md: Testing guidelines
echo.
echo ========================================

set /p RUN_SMOKE="Run quick smoke test now? (y/n): "
if /i "%RUN_SMOKE%"=="y" (
    echo.
    echo Running smoke test...
    call quick-test.bat
)

echo.
echo Setup completed successfully!
pause