# Modis Mobile App Automation Framework - Appium Java
> Framework automation testing cho ứng dụng mạng xã hội Modis sử dụng Appium + Java 11 + TestNG

---

## 🎯 Tổng quan Framework

**Modis Automation Framework** là hệ thống kiểm thử tự động hoàn chỉnh cho ứng dụng mobile Modis, được thiết kế theo **Page Object Model** với **TestNG** framework và **Allure reporting**.

### 🏗️ **Kiến trúc thực tế:**
```text
┌─────────────────────────────────────────────────────────────┐
│                MODIS AUTOMATION ARCHITECTURE                │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  📱 Target App           🤖 Test Framework    📊 Reporting  │
│  ┌─────────────────┐    ┌─────────────────┐   ┌──────────┐  │
│  │ Modis App       │◄──►│ Appium Server   │──►│ TestNG   │  │
│  │ React Native    │    │ UiAutomator2    │   │ Allure   │  │
│  │ 0.81.4          │    │ Java 11         │   │ ExtentR  │  │
│  │                 │    │ Maven 3.9.x     │   │          │  │
│  │ Package:        │    │ TestNG 7.8.0    │   │ HTML     │  │
│  │ com.modis.app   │    │ Appium 9.3.0    │   │ Reports  │  │
│  └─────────────────┘    └─────────────────┘   └──────────┘  │
│                                                             │
│  🌐 Backend API: https://modis-backend.onrender.com        │
│  📲 Platform: Android 11.0 (API 30)                       │
│  🔧 Driver: UiAutomator2                                   │
└─────────────────────────────────────────────────────────────┘
```

### 🔧 **Tech Stack thực tế:**
- **Java Version**: Java 11 (theo pom.xml)
- **Appium Version**: 9.3.0 với UiAutomator2 driver
- **Test Framework**: TestNG 7.8.0 với groups và parallel execution
- **Build Tool**: Maven 3.9.x
- **Reporting**: ExtentReports 5.1.1 + Allure 2.24.0
- **Target Platform**: Android 11.0 (API 30)
- **App Package**: `com.modis.app`
- **App Activity**: `com.modis.MainActivity`

---

## 📁 Cấu trúc thư mục thực tế

Framework được tổ chức theo **Page Object Model** với cấu trúc rõ ràng dựa trên source code thực tế:

```text
testing/automation/appium-java/
├── src/                              # 📁 Source Code Directory
│   ├── main/java/com/modis/          # 🏗️ Framework Infrastructure
│   │   ├── base/                     # Base classes
│   │   │   └── BasePage.java         # Common page functionality (waits, gestures)
│   │   ├── constants/                # Constants và test data
│   │   │   ├── AppConstants.java     # App-wide constants (timeouts, URLs)
│   │   │   └── TestIDs.java          # UI element test IDs
│   │   ├── drivers/                  # Driver management
│   │   │   └── DriverManager.java    # Appium driver lifecycle management
│   │   ├── listeners/                # TestNG listeners
│   │   │   └── TestListener.java     # Test execution listeners cho reporting
│   │   ├── pages/                    # 📱 Page Object Model Classes (10+ pages)
│   │   │   ├── LoginPage.java        # Login screen interactions
│   │   │   ├── SignupPage.java       # User registration functionality
│   │   │   ├── HomePage.java         # Main feed và bottom navigation
│   │   │   ├── ProfilePage.java      # User profile management
│   │   │   ├── MessagePage.java      # Chat messaging interface
│   │   │   ├── ConversationPage.java # Individual chat conversations
│   │   │   ├── FriendsPage.java      # Friends list và friend requests
│   │   │   ├── TakePage.java         # Camera capture functionality
│   │   │   ├── SendPhotoPage.java    # Photo sharing workflow
│   │   │   ├── AllImagesPage.java    # Gallery và image browsing
│   │   │   └── LoadingPage.java      # Loading states và splash screens
│   │   └── utils/                    # 🛠️ Utility Classes
│   │       ├── ConfigReader.java     # Configuration file reader
│   │       ├── WaitUtils.java        # Explicit wait strategies
│   │       ├── GestureUtils.java     # Touch gestures (swipe, tap, pinch)
│   │       ├── ScreenshotUtils.java  # Screenshot capture on failures
│   │       ├── DeviceUtils.java      # Device-specific operations
│   │       ├── ApiUtils.java         # API helper methods
│   │       ├── TestDataManager.java  # Test data management
│   │       └── LoggerUtil.java       # Logging utilities
│   └── test/                         # 🧪 Test Implementation
│       ├── java/com/modis/           # Test classes
│       │   ├── base/                 # Test infrastructure
│       │   │   └── BaseTest.java     # Test setup/teardown, driver initialization
│       │   └── tests/                # 🎯 Actual Test Classes (6 test classes)
│       │       ├── AuthenticationTests.java # Login, logout, registration tests
│       │       ├── NavigationTests.java     # App navigation, menu tests
│       │       ├── CameraTests.java         # Photo capture, gallery tests
│       │       ├── MessagingTests.java      # Chat, messaging functionality tests
│       │       ├── FriendsTests.java        # Friend requests, management tests
│       │       └── ProfileTests.java        # Profile management tests
│       └── resources/                # 📋 Test Configuration & Data
│           ├── config/               # Environment configurations
│           │   ├── test.properties   # Main test configuration
│           │   ├── android.properties # Android-specific settings (detailed)
│           │   └── ios.properties    # iOS-specific settings (future)
│           ├── testdata/             # Test data files (JSON format)
│           └── logback-test.xml      # Logging configuration
├── target/                           # 🎯 Maven Build Output (generated)
│   ├── classes/                      # Compiled main classes
│   ├── test-classes/                 # Compiled test classes
│   ├── surefire-reports/             # 📊 TestNG execution reports
│   │   ├── index.html               # Main TestNG report
│   │   ├── testng-results.xml       # Detailed XML results
│   │   └── emailable-report.html    # Email-friendly report
│   ├── allure-results/               # 📈 Allure raw results
│   ├── allure-report/                # 📈 Generated Allure report
│   └── screenshots/                  # 📸 Failure screenshots với timestamp
├── screenshots/                      # Screenshot directory (runtime)
├── logs/                            # Log files directory (runtime)
├── reports/                         # Generated reports directory (runtime)
├── pom.xml                          # 🔧 Maven Configuration (Java 11, dependencies)
├── testng.xml                       # 🎯 TestNG Suite Configuration với groups
├── run-tests.bat                    # 🚀 Windows execution script với parameters
├── run-tests.sh                     # 🐧 Linux/Mac execution script
├── CHANGELOG.md                     # Version history
└── README.md                        # 📚 Framework documentation
```

### 📋 Giải thích chức năng từng component:

#### 🏗️ **Framework Infrastructure** (`src/main/java/`)
- **`base/BasePage.java`**: Common functionality cho tất cả page objects
- **`constants/`**: App constants và test IDs từ React Native app
- **`drivers/DriverManager.java`**: Quản lý Appium driver lifecycle
- **`listeners/TestListener.java`**: TestNG listeners cho custom reporting
- **`pages/`**: 10+ page object classes cho các màn hình chính của app
- **`utils/`**: 8 utility classes cho waits, gestures, screenshots, config

#### 🧪 **Test Implementation** (`src/test/java/`)
- **`base/BaseTest.java`**: Setup/teardown, driver initialization cho mỗi test
- **`tests/`**: 6 test classes organized theo chức năng app
- **`resources/config/`**: 3 configuration files cho different environments
- **`resources/testdata/`**: JSON test data files

#### 🎯 **Build & Execution**
- **`pom.xml`**: Maven config với Java 11, Appium 9.3.0, TestNG 7.8.0
- **`testng.xml`**: Test suite config với groups (smoke, regression, etc.)
- **`run-tests.bat`**: Windows script với command-line parameters

## 🚀 Hướng dẫn chạy đầy đủ

### 1. 🛠️ Setup môi trường

#### 1.1 Cài đặt Java 17
```bash
# Kiểm tra Java version hiện tại
java -version

# Nếu chưa có Java 17:
# Tải từ: https://adoptium.net/temurin/releases/
# Chọn: OpenJDK 17 LTS > Windows > x64 > .msi

# Thiết lập biến môi trường:
# JAVA_HOME = C:\Program Files\Eclipse Adoptium\jdk-17.0.x.x-hotspot
# Thêm %JAVA_HOME%\bin vào System PATH

# Verify installation
java -version
javac -version
```

#### 1.2 Cài đặt Maven
```bash
# Tải Maven từ: https://maven.apache.org/download.cgi
# Chọn: Binary zip archive (apache-maven-3.9.x-bin.zip)
# Giải nén vào: C:\apache-maven-3.9.x

# Thiết lập biến môi trường:
# MAVEN_HOME = C:\apache-maven-3.9.x
# Thêm %MAVEN_HOME%\bin vào System PATH

# Verify installation
mvn -version
```

#### 1.3 Cài đặt Android Studio + Android SDK
```bash
# Tải Android Studio từ: https://developer.android.com/studio
# Cài đặt với default settings

# Sau khi cài đặt, mở Android Studio:
# Tools > SDK Manager > SDK Platforms
# ✅ Chọn: Android 13.0 (API 33) hoặc Android 14.0 (API 34)

# SDK Tools tab:
# ✅ Android SDK Command-line Tools (latest)
# ✅ Android Emulator
# ✅ Android SDK Platform-Tools

# Thiết lập biến môi trường:
# ANDROID_HOME = C:\Users\%USERNAME%\AppData\Local\Android\Sdk
# Thêm vào System PATH:
# %ANDROID_HOME%\platform-tools
# %ANDROID_HOME%\emulator  
# %ANDROID_HOME%\cmdline-tools\latest\bin

# Verify installation
adb --version
emulator -list-avds
```

#### 1.4 Cài đặt Node.js và Appium
```bash
# Tải Node.js LTS từ: https://nodejs.org/
# Chọn: LTS version (v18.x.x hoặc v20.x.x)
# Cài đặt với default settings

# Verify Node.js installation
node -v
npm -v

# Cài Appium global
npm install -g appium

# Cài UiAutomator2 driver cho Android
appium driver install uiautomator2

# Verify Appium installation
appium -v
appium driver list
```

### 2. 🏃‍♂️ Hướng dẫn chạy Backend trước khi test

**⚠️ QUAN TRỌNG**: Backend phải chạy trước khi thực hiện automation testing!

```bash
# Điều hướng đến thư mục backend
cd ..\..\..\Modis_BE_TL

# Chạy backend với Maven
mvn spring-boot:run

# Hoặc nếu đã build JAR file:
java -jar target\modis-backend-1.0.0.jar
```

**Kiểm tra backend đã chạy thành công:**
```bash
# Log thành công sẽ hiển thị:
# Tomcat started on port(s): 8080 (http) with context path ''
# Started PlantShopApplication in X.XXX seconds (JVM running for Y.YYY)

# Verify backend hoạt động:
curl http://localhost:8080/actuator/health
# Hoặc mở browser: http://localhost:8080
```

### 3. 📱 Hướng dẫn chạy Automation Testing (Appium)

#### Bước 1: Chuẩn bị thiết bị Android

**Cách 1: Sử dụng Android Emulator (Khuyến nghị)**
```bash
# Mở Android Studio
# Tools > Device Manager (hoặc AVD Manager)
# Create Device > Chọn Pixel 7 > API 33/34 > Finish
# Click nút ▶️ để start emulator

# Verify emulator đã chạy
adb devices
# Kết quả: emulator-5554    device
```

**Cách 2: Sử dụng thiết bị thật**
```bash
# Trên điện thoại Android:
# Settings > About phone > Tap "Build number" 7 lần
# Settings > Developer options > Enable "USB debugging"
# Cắm cáp USB vào máy tính

# Verify thiết bị đã kết nối
adb devices
# Kết quả: XXXXXXXXXX    device
```

#### Bước 2: Khởi động Appium Server
```bash
# Mở terminal/PowerShell riêng biệt (giữ mở suốt quá trình test)
appium --allow-insecure chromedriver_autodownload

# Log thành công sẽ hiển thị:
# [Appium] Welcome to Appium v2.x.x
# [Appium] Appium REST http interface listener started on 0.0.0.0:4723
# [Appium] Available drivers:
# [Appium]   - uiautomator2@x.x.x (automationName 'UiAutomator2')
```

#### Bước 3: Cấu hình Test Environment
```bash
# Điều hướng đến thư mục automation
cd testing\automation\appium-java

# Kiểm tra file cấu hình
type src\test\resources\config\config.properties

# Đảm bảo các settings sau:
# platform.name=Android
# automation.name=UiAutomator2
# app.package=com.modis.app
# app.activity=com.modis.MainActivity
# device.name=emulator-5554 (hoặc device ID thực tế)
```

#### Bước 4: Build Dependencies
```bash
# Build và tải dependencies (chỉ chạy lần đầu hoặc khi có thay đổi)
mvn clean install -DskipTests

# Verify build thành công:
# [INFO] BUILD SUCCESS
# [INFO] Total time: XX.XXX s
```

#### Bước 5: Chạy Automation Tests

**5.1 Chạy toàn bộ test suite:**
```bash
mvn test

# Hoặc chạy theo TestNG configuration:
mvn test -DsuiteXmlFile=testng.xml
```

**5.2 Chạy specific test class:**
```bash
# Authentication tests (Login, Logout, Registration)
mvn test -Dtest=AuthenticationTests

# Camera tests (Photo capture, Gallery)
mvn test -Dtest=CameraTests

# Friends tests (Friend requests, Management)
mvn test -Dtest=FriendsTests

# Messaging tests (Chat, Send messages)
mvn test -Dtest=MessagingTests

# Navigation tests (Menu, Tabs)
mvn test -Dtest=NavigationTests

# Profile tests (Profile management)
mvn test -Dtest=ProfileTests

# Smoke tests (Critical path)
mvn test -Dtest=SmokeTests
```

**5.3 Chạy với custom parameters:**
```bash
# Chạy trên device cụ thể
mvn test -Ddevice.name="Pixel_7_API_33"

# Chạy với platform khác
mvn test -Dplatform.name=Android -Dplatform.version=13

# Chạy với app package khác
mvn test -Dapp.package=com.modis.staging

# Chạy parallel tests
mvn test -Dparallel=methods -DthreadCount=2
```

**5.4 Chạy test groups:**
```bash
# Chỉ chạy smoke tests
mvn test -Dgroups=smoke

# Chỉ chạy regression tests
mvn test -Dgroups=regression

# Exclude flaky tests
mvn test -DexcludedGroups=flaky
```

#### Bước 6: Xem kết quả Automation

**6.1 TestNG Reports:**
```bash
# Mở main report
start target\surefire-reports\index.html

# Xem detailed XML results
type target\surefire-reports\testng-results.xml

# Email-friendly report
start target\surefire-reports\emailable-report.html
```

**6.2 Screenshots (khi test fail):**
```bash
# Xem screenshots folder
dir target\screenshots\

# Mở screenshot cụ thể
start target\screenshots\failed_test_20241216_143022.png
```

**6.3 Logs:**
```bash
# Xem Appium logs
type target\logs\appium.log

# Xem test execution logs
type target\logs\test-execution.log
```

### 4. 🔄 Flow hoạt động Automation Framework

```text
1. TestNG Suite Initialization
   ├── Read testng.xml configuration
   ├── Load test classes và methods
   └── Setup parallel execution (nếu có)

2. BaseTest Setup (@BeforeMethod)
   ├── Read configuration từ config.properties
   ├── Initialize DriverManager
   ├── Create AndroidDriver với capabilities
   ├── Setup implicit waits
   └── Navigate to app home screen

3. Test Execution
   ├── Page Object initialization
   ├── UI interactions (tap, swipe, type)
   ├── Data input từ JSON/CSV files
   ├── Assertions và verifications
   └── Screenshot capture (nếu fail)

4. Test Cleanup (@AfterMethod)
   ├── Capture final screenshot
   ├── Close app session
   ├── Quit driver
   └── Clean up resources

5. Report Generation
   ├── TestNG HTML reports
   ├── Allure results (nếu enabled)
   ├── Screenshots organization
   └── Log file consolidation
```

### 5. 📊 Reports & Kết quả

#### 5.1 TestNG Reports
- **Main Report**: `target/surefire-reports/index.html`
  - Test suite overview
  - Pass/Fail statistics
  - Execution timeline
  - Failed test details với stack traces

- **Emailable Report**: `target/surefire-reports/emailable-report.html`
  - Compact format cho email sharing
  - Summary statistics
  - Failed test highlights

#### 5.2 Screenshots
- **Location**: `target/screenshots/`
- **Naming**: `{testMethodName}_{timestamp}.png`
- **Captured**: Automatically khi test fail
- **Format**: PNG với full screen capture

#### 5.3 Logs
- **Appium Logs**: `target/logs/appium.log`
  - Driver commands và responses
  - Element location details
  - Performance metrics

- **Test Logs**: `target/logs/test-execution.log`
  - Test method execution flow
  - Custom log messages
  - Error details và stack traces

### 6. 🚨 Troubleshooting cực kỳ chi tiết

#### 6.1 Environment Issues
| Lỗi | Nguyên nhân | Cách fix |
|-----|-------------|----------|
| `mvn is not recognized` | Maven chưa trong PATH | Add `%MAVEN_HOME%\bin` vào System PATH, restart terminal |
| `java is not recognized` | Java chưa cài hoặc PATH sai | Install Java 17, set JAVA_HOME, add `%JAVA_HOME%\bin` to PATH |
| `JAVA_HOME is set to an invalid directory` | JAVA_HOME path incorrect | Set JAVA_HOME = `C:\Program Files\Eclipse Adoptium\jdk-17.x.x` |
| `adb is not recognized` | Android SDK PATH missing | Add `%ANDROID_HOME%\platform-tools` to PATH |

#### 6.2 Appium Connection Issues
| Lỗi | Nguyên nhân | Cách fix |
|-----|-------------|----------|
| `Connection refused (Connection refused)` | Appium server chưa start | Run `appium` trong terminal riêng |
| `Could not find a connected Android device` | Device chưa connect hoặc ADB issue | `adb kill-server && adb start-server`, check `adb devices` |
| `Unable to create driver session` | Capabilities sai hoặc app không found | Verify appPackage/appActivity trong config |
| `Session creation timeout` | Device quá chậm hoặc overloaded | Increase timeout trong capabilities, restart device |

#### 6.3 Element Location Issues  
| Lỗi | Nguyên nhân | Cách fix |
|-----|-------------|----------|
| `NoSuchElementException` | Element không tìm thấy | Use Appium Inspector để verify locator, add explicit wait |
| `StaleElementReferenceException` | Element đã thay đổi sau khi locate | Re-find element trước khi interact |
| `ElementNotInteractableException` | Element không thể click/type | Check element visibility, scroll to element |
| `TimeoutException` | Element không appear trong timeout | Increase wait time, check app loading state |

#### 6.4 App-Specific Issues
| Lỗi | Nguyên nhân | Cách fix |
|-----|-------------|----------|
| `App crashes on launch` | App build issue hoặc device incompatible | Check app logs, verify APK compatibility |
| `Permission dialogs block test` | System permissions chưa granted | Handle permission popups trong test code |
| `Keyboard covers elements` | Soft keyboard interference | Hide keyboard sau input: `driver.hideKeyboard()` |
| `Network requests fail` | Backend không available | Verify backend running, check network connectivity |

### 7. 💡 Best Practices

#### 7.1 Test Design
- ✅ **Sử dụng Page Object Model** để tái sử dụng code
- ✅ **Implement explicit waits** thay vì `Thread.sleep()`
- ✅ **Use testID/accessibilityId** làm locator chính
- ✅ **Capture screenshots** khi test fail
- ✅ **Implement retry mechanism** cho flaky tests
- ❌ **Không hardcode test data** trong test methods
- ❌ **Không dùng XPath phức tạp** khi có alternative

#### 7.2 Test Execution
- ✅ **Clean app state** trước mỗi test
- ✅ **Run tests independently** (không phụ thuộc lẫn nhau)
- ✅ **Use test groups** để organize tests
- ✅ **Monitor test execution** qua logs
- ❌ **Không interact với device** khi test đang chạy
- ❌ **Không chạy quá nhiều tests parallel** trên 1 device

#### 7.3 Maintenance
- ✅ **Update locators** khi UI thay đổi
- ✅ **Review failed tests** thường xuyên
- ✅ **Keep test data updated** với app changes
- ✅ **Document test scenarios** rõ ràng
- ❌ **Không ignore flaky tests** mà không investigate
- ❌ **Không commit** với failing tests

### 8. 📋 Commands Cheatsheet

#### 8.1 Maven Commands
```bash
# Build project
mvn clean compile

# Install dependencies
mvn clean install -DskipTests

# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthenticationTests

# Run with TestNG suite
mvn test -DsuiteXmlFile=testng.xml

# Run with system properties
mvn test -Ddevice.name=emulator-5554

# Skip tests during build
mvn install -DskipTests

# Clean target directory
mvn clean
```

#### 8.2 ADB Commands
```bash
# List connected devices
adb devices

# Install APK
adb install -r path\to\app.apk

# Uninstall app
adb uninstall com.modis.app

# Clear app data
adb shell pm clear com.modis.app

# Start app
adb shell am start -n com.modis.app/.MainActivity

# View device logs
adb logcat

# Clear device logs
adb logcat -c

# Restart ADB server
adb kill-server && adb start-server

# Take screenshot
adb shell screencap /sdcard/screenshot.png
adb pull /sdcard/screenshot.png
```

#### 8.3 Appium Commands
```bash
# Start Appium server
appium

# Start với custom port
appium -p 4724

# Start với log level
appium --log-level debug

# List installed drivers
appium driver list

# Install UiAutomator2 driver
appium driver install uiautomator2

# Update driver
appium driver update uiautomator2

# Uninstall driver
appium driver uninstall uiautomator2
```

#### 8.4 Test Execution Scripts
```bash
# Windows batch scripts
run-tests.bat                    # Run all tests
run-tests.bat smoke             # Run smoke tests only
run-tests.bat AuthenticationTests # Run specific test class

# PowerShell commands
.\run-tests.bat -TestClass AuthenticationTests -Device emulator-5554
```

#### 8.5 Report & Log Commands
```bash
# Open TestNG report
start target\surefire-reports\index.html

# View screenshots
explorer target\screenshots\

# View logs
type target\logs\appium.log
type target\logs\test-execution.log

# Clean reports
rmdir /s /q target\surefire-reports
rmdir /s /q target\screenshots
rmdir /s /q target\logs

# Archive results
powershell Compress-Archive -Path target\surefire-reports -DestinationPath test-results-%date%.zip
```

Quy trình thiết lập cực kỳ nghiêm ngặt trên **Windows**. Bất kỳ sai sót nào trong Environment Variables đều khiến Appium không thể khởi chạy.

### Bước 1: Setup Java & Maven
1.  Đảm bảo `JAVA_HOME` trỏ đúng vào thư mục cài đặt JDK 17.
2.  Đảm bảo `MAVEN_HOME` trỏ vào thư mục cài Apache Maven.
3.  Add `%JAVA_HOME%\bin` và `%MAVEN_HOME%\bin` vào `PATH` của System.

### Bước 2: Setup Android SDK (Bộ não của Mobile Testing)
1.  Cài đặt Android Studio.
2.  Vào SDK Manager, tải `Android SDK Platform` (API 33+) và `Android SDK Command-line Tools`.
3.  Cấu hình `ANDROID_HOME` trỏ tới `C:\Users\<Tên_Bạn>\AppData\Local\Android\Sdk`.
4.  Cập nhật `PATH` bằng 3 biến cực kỳ quan trọng sau:
    *   `%ANDROID_HOME%\platform-tools` (Để dùng lệnh ADB).
    *   `%ANDROID_HOME%\emulator` (Để bật máy ảo từ Terminal).
    *   `%ANDROID_HOME%\cmdline-tools\latest\bin`

### Bước 3: Setup NodeJS & Appium Server
Appium 2.x là một hệ sinh thái được quản lý qua NPM.
1.  Cài đặt NodeJS (LTS).
2.  Mở CMD (Admin) và cài core của Appium:
    ```bash
    npm install -g appium
    ```
3.  Cài đặt Driver chuyên dụng cho Android (UiAutomator2):
    ```bash
    appium driver install uiautomator2
    ```
4.  *Gợi ý:* Cài thêm Appium Inspector (Phần mềm giao diện) từ Github để dễ dàng soi cấu trúc (DOM) của Mobile App.

---

## 3. Quy Trình Vận Hành (Execution Workflow)

### Bước 1: Chuẩn bị Device (Thiết bị)
*   **Máy ảo (Emulator):** Mở Android Studio -> Device Manager -> Nhấn chạy máy ảo.
*   **Máy thật:** Cắm cáp, bật Developer Options -> Bật USB Debugging.
*   **Xác nhận:** Mở CMD, gõ `adb devices`. Nếu hiển thị chữ `device` bên cạnh số Serial, bạn đã thành công. Nếu báo `unauthorized`, hãy nhìn vào màn hình điện thoại và bấm "Cho phép".

### Bước 2: Kích hoạt Máy Chủ Appium
Mở một cửa sổ PowerShell hoặc CMD **riêng biệt**. Bạn không được tắt nó trong quá trình chạy code.
```bash
appium --allow-insecure chromedriver_autodownload
```
*(Tham số allow-insecure giúp tự động tải Webdriver nếu App của bạn có chứa WebView).*

### Bước 3: Cấu hình Capabilities
Trước khi chạy lệnh Maven, hãy kiểm tra file config (Ví dụ: `src/main/resources/config.properties` hoặc file XML). Bạn cần đảm bảo:
*   `platformName` = `Android`
*   `automationName` = `UiAutomator2`
*   `app` = Đường dẫn tuyệt đối trỏ tới file `.apk` (nếu có cài tự động), hoặc dùng `appPackage` + `appActivity` nếu App đã cài sẵn trên máy ảo.

### Bước 4: Chạy Tự Động (Automation Execution)
Mở một cửa sổ Terminal mới, điều hướng tới `testing\automation\appium-java`.

**Tải viện thư viện (Lần đầu):**
```bash
mvn clean install -DskipTests
```

**1. Chạy TOÀN BỘ kịch bản test (Regression Suite):**
```bash
mvn test
```

**2. Chạy thông qua file cấu hình Suite (Thực thi theo luồng testng.xml):**
```bash
mvn test -DsuiteXmlFile=testng.xml
```

**3. Chạy MỘT test class cụ thể (Tùy chọn 1 trong các Class dưới đây):**
```bash
mvn test -Dtest=AuthenticationTests
mvn test -Dtest=CameraTests
mvn test -Dtest=FriendsTests
mvn test -Dtest=MessagingTests
mvn test -Dtest=NavigationTests
mvn test -Dtest=ProfileTests
```

---

## 4. Các Vấn Đề Kinh Điển (Troubleshooting Arsenal)

Chạy Automation trên Mobile là một "nghệ thuật" xử lý sự cố. Dưới đây là cách giải quyết các lỗi kinh điển:

| Mã Lỗi / Hiện Tượng | Nguyên Nhân Cốt Lõi | Cách Khắc Phục Tận Gốc |
| :--- | :--- | :--- |
| `Original error: Could not find 'adb' in PATH` | Biến `ANDROID_HOME` bị sai hoặc bạn quên Add `platform-tools` vào System `PATH`. | Mở Environment Variables, kiểm tra lại biến `ANDROID_HOME`. Tắt hẳn IDE (IntelliJ/VSCode) và mở lại. |
| `Connection refused: connect` | Code Java không thể liên lạc với Appium. | Mở CMD, chạy lại lệnh `appium`. Đảm bảo Port `4723` không bị phần mềm khác chiếm dụng. |
| `NoSuchElementException` | Appium không tìm thấy nút bấm/text. | 1. Điện thoại load chậm -> Hãy dùng Explicit Wait (`WebDriverWait`) thay vì Sleep.<br>2. Locator (XPath/ID) đã bị Dev đổi tên. Hãy dùng Appium Inspector để lấy ID mới. |
| `StaleElementReferenceException` | Element có tồn tại, nhưng cấu trúc DOM (Màn hình) vừa bị refresh. | Catch exception này và thực hiện lệnh `findElement` lại một lần nữa trước khi Click. |
| App tự động Crash ngay khi mở | Bản build `.apk` bị lỗi, hoặc UiAutomator2 không tương thích với version Android. | Check lại file APK. Thử cài thủ công bằng lệnh `adb install <file.apk>`. |

---

## 5. Tiêu Chuẩn Viết Code (Best Practices)

1.  **Chỉ định Locator theo thứ tự ưu tiên:** Luôn giục team React Native đặt biến `testID`. Bên Automation sẽ dùng `MobileBy.AccessibilityId("tên_testID")`. Đây là cách truy xuất nhanh nhất và không bao giờ bị gãy (flaky) khi Dev đổi giao diện. Chỉ dùng `XPath` khi không còn cách nào khác.
2.  **Tuyệt đối không dùng `Thread.sleep()`:**
    *   *Sai lầm:* `Thread.sleep(5000)` (Bắt hệ thống đứng yên 5 giây dù App đã load xong ở giây thứ 1).
    *   *Chuẩn mực:* Sử dụng `WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.visibilityOf(element));` (Nếu element xuất hiện ở giây 1, nó sẽ chạy tiếp ngay lập tức, tiết kiệm 4 giây).
3.  **Quy tắc "Bàn tay sạch" (Clean State):** Mỗi một bài Test (Method) không được phụ thuộc vào trạng thái của bài test trước. Hãy viết hàm `@BeforeMethod` để đưa App về trạng thái Home/Login cơ bản nhất. Đừng để Test B thất bại chỉ vì Test A vô tình tắt nhầm màn hình.
4.  **Tách biệt Dữ liệu (Data-Driven):** Đừng hardcode Username/Password vào thẳng file Code. Hãy đọc chúng từ file `.properties` hoặc `.json`. Tương lai có đổi môi trường (Staging -> Prod) thì chỉ cần sửa 1 file cấu hình.
## 🚀 Hướng dẫn chạy đầy đủ

### 1. 🛠️ Setup môi trường

#### 1.1 Cài đặt Java 11 (Bắt buộc)
```bash
# Tải Java 11 từ: https://adoptium.net/temurin/releases/
# Chọn: OpenJDK 11 LTS > Windows > x64 > .msi

# Thiết lập biến môi trường:
# JAVA_HOME = C:\Program Files\Eclipse Adoptium\jdk-11.0.x.x-hotspot
# Thêm %JAVA_HOME%\bin vào System PATH

# Verify installation (PHẢI là Java 11)
java -version
# Expected: openjdk version "11.0.x"
javac -version
```

#### 1.2 Cài đặt Maven
```bash
# Tải Maven từ: https://maven.apache.org/download.cgi
# Chọn: Binary zip archive (apache-maven-3.9.x-bin.zip)
# Giải nén vào: C:\apache-maven-3.9.x

# Thiết lập biến môi trường:
# MAVEN_HOME = C:\apache-maven-3.9.x
# Thêm %MAVEN_HOME%\bin vào System PATH

# Verify installation
mvn -version
```

#### 1.3 Cài đặt Android Studio + Android SDK
```bash
# Tải Android Studio từ: https://developer.android.com/studio
# Cài đặt với default settings

# Sau khi cài đặt, mở Android Studio:
# Tools > SDK Manager > SDK Platforms
# ✅ Chọn: Android 11.0 (API 30) - theo config thực tế

# SDK Tools tab:
# ✅ Android SDK Command-line Tools (latest)
# ✅ Android Emulator
# ✅ Android SDK Platform-Tools

# Thiết lập biến môi trường:
# ANDROID_HOME = C:\Users\%USERNAME%\AppData\Local\Android\Sdk
# Thêm vào System PATH:
# %ANDROID_HOME%\platform-tools
# %ANDROID_HOME%\emulator  
# %ANDROID_HOME%\cmdline-tools\latest\bin

# Verify installation
adb --version
```

#### 1.4 Cài đặt Node.js và Appium
```bash
# Tải Node.js LTS từ: https://nodejs.org/
# Chọn: LTS version (v18.x.x hoặc v20.x.x)

# Verify Node.js installation
node -v
npm -v

# Cài Appium global
npm install -g appium

# Cài UiAutomator2 driver (theo config thực tế)
appium driver install uiautomator2

# Verify Appium installation
appium -v
appium driver list
```

### 2. 🌐 Kiểm tra Backend API

**⚠️ QUAN TRỌNG**: Framework test với deployed backend, KHÔNG cần local backend!

```bash
# Backend API đang chạy tại:
# https://modis-backend.onrender.com

# Kiểm tra API health:
curl https://modis-backend.onrender.com/actuator/health
# Expected response: {"status":"UP"}

# Note: Render free tier có cold start delay ~30-90s
```

### 3. 📱 Chuẩn bị thiết bị Android

#### Cách 1: Android Emulator (Khuyến nghị)
```bash
# Mở Android Studio
# Tools > Device Manager (hoặc AVD Manager)
# Create Device > Chọn Pixel 4 > API 30 (Android 11.0) > Finish
# Click nút ▶️ để start emulator

# Verify emulator đã chạy
adb devices
# Expected: emulator-5554    device
```

#### Cách 2: Thiết bị thật
```bash
# Trên điện thoại Android:
# Settings > About phone > Tap "Build number" 7 lần
# Settings > Developer options > Enable "USB debugging"
# Cắm cáp USB vào máy tính

# Verify thiết bị đã kết nối
adb devices
# Expected: XXXXXXXXXX    device
```

### 4. 🚀 Chạy Automation Tests

#### Bước 1: Khởi động Appium Server
```bash
# Mở terminal/PowerShell riêng biệt (giữ mở suốt quá trình test)
appium

# Log thành công sẽ hiển thị:
# [Appium] Welcome to Appium v2.x.x
# [Appium] Appium REST http interface listener started on 0.0.0.0:4723
# [Appium] Available drivers:
# [Appium]   - uiautomator2@x.x.x (automationName 'UiAutomator2')
```

#### Bước 2: Build Dependencies
```bash
# Điều hướng đến thư mục automation
cd testing\automation\appium-java

# Đảm bảo JAVA_HOME trỏ đến Java 11
echo %JAVA_HOME%
java -version

# Build và tải dependencies (chỉ chạy lần đầu hoặc khi có thay đổi)
mvn clean install -DskipTests

# Verify build thành công:
# [INFO] BUILD SUCCESS
# [INFO] Total time: XX.XXX s
```

#### Bước 3: Chạy Tests

**3.1 Sử dụng execution script (Khuyến nghị):**
```bash
# Chạy smoke tests (critical path)
run-tests.bat --suite smoke

# Chạy regression tests (full functionality)
run-tests.bat --suite regression

# Chạy specific test class
run-tests.bat --suite authentication
run-tests.bat --suite navigation
run-tests.bat --suite camera
run-tests.bat --suite messaging
run-tests.bat --suite friends
run-tests.bat --suite profile

# Chạy với custom device
run-tests.bat --device "Pixel 4" --suite smoke

# Xem help và available options
run-tests.bat --help
```

**3.2 Chạy trực tiếp với Maven:**
```bash
# Chạy theo TestNG groups (theo testng.xml thực tế)
mvn test -Dgroups=smoke
mvn test -Dgroups=regression
mvn test -Dgroups=authentication

# Chạy specific test class
mvn test -Dtest=AuthenticationTests
mvn test -Dtest=NavigationTests
mvn test -Dtest=CameraTests
mvn test -Dtest=MessagingTests
mvn test -Dtest=FriendsTests
mvn test -Dtest=ProfileTests

# Chạy với custom parameters
mvn test -Dplatform=android -DdeviceName="Android Emulator" -DplatformVersion=11.0
```

**3.3 Chạy với Maven profiles:**
```bash
# Android profile (default)
mvn test -Pandroid

# Smoke tests profile
mvn test -Psmoke

# Regression tests profile
mvn test -Pregression

# Parallel execution profile
mvn test -Pparallel
```

### 5. 📊 Xem kết quả Tests

#### 5.1 TestNG Reports
```bash
# Mở main TestNG report
start target\surefire-reports\index.html

# Xem detailed XML results
type target\surefire-reports\testng-results.xml

# Email-friendly report
start target\surefire-reports\emailable-report.html
```

#### 5.2 Allure Reports
```bash
# Generate Allure report
mvn allure:report

# Serve Allure report (opens in browser)
mvn allure:serve

# Mở generated Allure report
start target\allure-report\index.html
```

#### 5.3 Screenshots và Logs
```bash
# Xem screenshots (khi test fail)
dir target\screenshots\
start target\screenshots\

# Xem execution logs
type logs\test-execution.log

# Xem Appium logs (nếu có)
type logs\appium.log
```

### 6. 🔄 Test Configuration thực tế

#### 6.1 Main Configuration (test.properties)
```properties
# Platform settings (theo config thực tế)
platform=android
deviceName=Android Emulator
platformVersion=11.0
automationName=UiAutomator2

# Appium server
appium.serverUrl=http://127.0.0.1:4723

# App settings
android.appPackage=com.modis.app
android.appActivity=com.modis.MainActivity

# Timeouts
implicit.wait=10
explicit.wait=20
element.wait.timeout=15
```

#### 6.2 Android Configuration (android.properties)
```properties
# Device capabilities (detailed config)
android.deviceName=Android Emulator
android.platformVersion=11.0
android.automationName=UiAutomator2
android.appPackage=com.modis.app
android.appActivity=com.modis.MainActivity

# Permissions
android.autoGrantPermissions=true
android.autoAcceptAlerts=true
android.autoDismissAlerts=true

# Performance
android.newCommandTimeout=300
android.androidInstallTimeout=90000
```

#### 6.3 TestNG Configuration (testng.xml)
```xml
<!-- Test suites với groups -->
<suite name="Modis Mobile App Test Suite">
    <!-- Smoke Tests -->
    <test name="Smoke Tests">
        <groups>
            <run><include name="smoke"/></run>
        </groups>
    </test>
    
    <!-- Regression Tests -->
    <test name="Regression Tests">
        <groups>
            <run><include name="regression"/></run>
        </groups>
    </test>
</suite>
```

### 7. 🚨 Troubleshooting theo Project thực tế

#### 7.1 Environment Issues
| Lỗi | Nguyên nhân | Cách fix |
|-----|-------------|----------|
| `Wrong Java version` | Cần Java 11, không phải Java 17 | Set JAVA_HOME đến Java 11 installation |
| `mvn is not recognized` | Maven chưa trong PATH | Add `%MAVEN_HOME%\bin` to System PATH |
| `adb is not recognized` | Android SDK PATH missing | Add `%ANDROID_HOME%\platform-tools` to PATH |
| `Build failure` | Dependencies issue | Run `mvn clean install -DskipTests` |

#### 7.2 Appium Connection Issues
| Lỗi | Nguyên nhân | Cách fix |
|-----|-------------|----------|
| `Connection refused` | Appium server chưa start | Run `appium` trong terminal riêng |
| `No connected devices` | Device chưa connect | Check `adb devices`, restart ADB |
| `Unable to create session` | App package/activity sai | Verify `com.modis.app` và `com.modis.MainActivity` |
| `UiAutomator2 not found` | Driver chưa install | Run `appium driver install uiautomator2` |

#### 7.3 Test Execution Issues
| Lỗi | Nguyên nhân | Cách fix |
|-----|-------------|----------|
| `Element not found` | Locator sai hoặc timing issue | Use explicit waits, check element IDs |
| `App crashes` | App build issue | Check app logs với `adb logcat` |
| `Test timeout` | Network/API slow | Increase timeouts trong config |
| `Screenshot failure` | Permissions issue | Check screenshot directory permissions |

### 8. 💡 Best Practices theo Framework thực tế

#### 8.1 Test Design
- ✅ **Sử dụng TestNG groups** để organize tests (smoke, regression, authentication)
- ✅ **Page Object Model** với 10+ page classes cho maintainability
- ✅ **Explicit waits** với WaitUtils.java thay vì Thread.sleep()
- ✅ **Screenshots on failure** với ScreenshotUtils.java
- ✅ **Config-driven testing** với .properties files
- ❌ **Không hardcode app package/activity** trong test code
- ❌ **Không assume local backend** - framework dùng deployed API

#### 8.2 Test Execution
- ✅ **Clean app state** trước mỗi test với BaseTest.java
- ✅ **Independent tests** không phụ thuộc lẫn nhau
- ✅ **Use test groups** để run specific test types
- ✅ **Monitor test execution** qua logs và reports
- ❌ **Không interact với device** khi test đang chạy
- ❌ **Không chạy parallel** trên single device

#### 8.3 Maintenance
- ✅ **Update locators** khi UI thay đổi trong TestIDs.java
- ✅ **Review failed tests** thường xuyên
- ✅ **Keep test data updated** trong testdata/ directory
- ✅ **Document test scenarios** rõ ràng
- ❌ **Không ignore flaky tests** mà không investigate
- ❌ **Không commit** với failing tests

### 9. 📋 Commands Cheatsheet

#### 9.1 Environment Commands
```bash
# Java version check
java -version
echo %JAVA_HOME%

# Maven commands
mvn -version
mvn clean install -DskipTests
mvn clean compile

# Android commands
adb devices
adb logcat
adb kill-server && adb start-server
```

#### 9.2 Test Execution Commands
```bash
# Build project
mvn clean install -DskipTests

# Run tests by groups
mvn test -Dgroups=smoke
mvn test -Dgroups=regression
mvn test -Dgroups=authentication

# Run specific test classes
mvn test -Dtest=AuthenticationTests
mvn test -Dtest=NavigationTests

# Run with profiles
mvn test -Pandroid -Psmoke
mvn test -Pregression
```

#### 9.3 Reporting Commands
```bash
# Open TestNG reports
start target\surefire-reports\index.html

# Generate Allure reports
mvn allure:report
mvn allure:serve

# View screenshots
start target\screenshots\

# View logs
type logs\test-execution.log
```

#### 9.4 Appium Commands
```bash
# Start Appium server
appium

# Install drivers
appium driver install uiautomator2
appium driver list

# Check Appium version
appium -v
```

---

## 🎯 Kết luận

**Modis Automation Framework** là một framework production-ready với:

- **Java 11** + **Appium 9.3.0** + **TestNG 7.8.0**
- **Page Object Model** với 10+ page classes
- **TestNG groups** cho flexible test execution
- **Multiple reporting** (TestNG + Allure + ExtentReports)
- **Config-driven** testing với .properties files
- **Production backend** integration (Render.com)
- **Comprehensive utilities** cho waits, gestures, screenshots

Framework này có thể sử dụng ngay để test Modis mobile app và dễ dàng mở rộng cho các features mới.