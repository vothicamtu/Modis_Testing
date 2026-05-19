# Modis Quality Assurance & Testing Framework
> Framework kiểm thử toàn diện cho ứng dụng mạng xã hội Modis - từ Mobile UI đến Backend Performance

---

## 🎯 Tổng Quan Project Modis

**Modis** là một ứng dụng mạng xã hội hoàn chỉnh với kiến trúc hiện đại:

### 🏗️ **Kiến trúc hệ thống thực tế:**
```text
┌─────────────────────────────────────────────────────────────┐
│                    🌐 MODIS ECOSYSTEM                       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  📱 Mobile Apps          🖥️  Backend API        🧪 Testing   │
│  ┌─────────────────┐    ┌─────────────────┐   ┌──────────┐  │
│  │ Android (RN)    │◄──►│ Spring Boot     │◄──┤ Appium   │  │
│  │ iOS (RN)        │    │ MongoDB Atlas   │   │ JMeter   │  │
│  │                 │    │ Redis Cloud     │   │          │  │
│  │ React Native    │    │ Cloudinary      │   │ TestNG   │  │
│  │ 0.81.4          │    │ JWT Auth        │   │ Maven    │  │
│  └─────────────────┘    └─────────────────┘   └──────────┘  │
│                                                             │
│  🚀 Deployment: Render.com                                 │
│  🌍 API Base: https://modis-backend.onrender.com           │
│  📊 Database: MongoDB Atlas (Cloud)                        │
│  🗄️  Cache: Redis Cloud                                    │
│  📸 Storage: Cloudinary                                     │
└─────────────────────────────────────────────────────────────┘
```

### 🔧 **Tech Stack thực tế:**
- **Backend**: Spring Boot 3.2.5 + Java 17 (deployed trên Render.com)
- **Database**: MongoDB Atlas (cloud database)
- **Cache**: Redis Cloud 
- **File Storage**: Cloudinary (image/video storage)
- **Mobile**: React Native 0.81.4 (Android + iOS)
- **Authentication**: JWT với Spring Security
- **Real-time**: WebSocket cho messaging

### 🧪 **Testing Framework Architecture:**
```text
┌─────────────────────────────────────────────────────────────┐
│                    MODIS TESTING PIPELINE                   │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  📱 Mobile UI Testing        ⚡ Backend Performance         │
│  ┌─────────────────────┐    ┌─────────────────────────┐     │
│  │ Appium + Java 11    │    │ JMeter + Java 17        │     │
│  │ TestNG Framework    │    │ Custom Java Utilities   │     │
│  │ Page Object Model   │    │ Load/Stress/Spike Tests │     │
│  │ ExtentReports       │    │ Real-time Monitoring    │     │
│  │ Allure Reports      │    │ Performance Comparison  │     │
│  └─────────────────────┘    └─────────────────────────┘     │
│                                                             │
│  🎯 Target: Deployed Backend API                           │
│  🌐 API: https://modis-backend.onrender.com                │
│  📊 Metrics: Response Time, Throughput, Error Rate         │
└─────────────────────────────────────────────────────────────┘
```

## 📁 Cấu trúc thư mục thực tế

Dưới đây là cấu trúc chi tiết của testing framework dựa trên source code thực tế:

```text
testing/
├── README.md                        # 📖 Tài liệu tổng quan testing framework
├── automation/                      # 🤖 Mobile UI Automation Framework
│   └── appium-java/                 # Appium + Java 11 automation tests
│       ├── src/                     # Source code directory
│       │   ├── main/java/com/modis/ # Framework utilities và page objects
│       │   │   ├── base/            # BasePage.java - common page functionality
│       │   │   ├── constants/       # AppConstants.java, TestIDs.java
│       │   │   ├── drivers/         # DriverManager.java - Appium driver management
│       │   │   ├── listeners/       # TestListener.java - TestNG listeners
│       │   │   ├── pages/           # Page Object Model classes
│       │   │   │   ├── LoginPage.java        # Login screen interactions
│       │   │   │   ├── SignupPage.java       # Registration functionality
│       │   │   │   ├── HomePage.java         # Main feed và navigation
│       │   │   │   ├── ProfilePage.java      # User profile management
│       │   │   │   ├── MessagePage.java      # Chat messaging
│       │   │   │   ├── ConversationPage.java # Chat conversations
│       │   │   │   ├── FriendsPage.java      # Friends list và requests
│       │   │   │   ├── TakePage.java         # Camera capture
│       │   │   │   ├── SendPhotoPage.java    # Photo sharing
│       │   │   │   ├── AllImagesPage.java    # Gallery view
│       │   │   │   └── LoadingPage.java      # Loading states
│       │   │   └── utils/           # Utility classes
│       │   │       ├── ConfigReader.java     # Configuration management
│       │   │       ├── WaitUtils.java        # Explicit wait strategies
│       │   │       ├── GestureUtils.java     # Touch gestures (swipe, tap)
│       │   │       ├── ScreenshotUtils.java  # Screenshot capture
│       │   │       ├── DeviceUtils.java      # Device operations
│       │   │       ├── ApiUtils.java         # API helper methods
│       │   │       ├── TestDataManager.java  # Test data handling
│       │   │       └── LoggerUtil.java       # Logging utilities
│       │   └── test/                # Test classes và resources
│       │       ├── java/com/modis/  # Test implementation
│       │       │   ├── base/        # BaseTest.java - setup/teardown
│       │       │   └── tests/       # Actual test classes
│       │       │       ├── AuthenticationTests.java # Login/signup tests
│       │       │       ├── NavigationTests.java     # App navigation tests
│       │       │       ├── CameraTests.java         # Photo capture tests
│       │       │       ├── MessagingTests.java      # Chat functionality tests
│       │       │       ├── FriendsTests.java        # Friends management tests
│       │       │       └── ProfileTests.java        # Profile tests
│       │       └── resources/       # Test configuration và data
│       │           ├── config/      # Environment configurations
│       │           │   ├── test.properties         # Main test config
│       │           │   ├── android.properties      # Android-specific settings
│       │           │   └── ios.properties          # iOS-specific settings
│       │           ├── testdata/    # Test data files (JSON format)
│       │           └── logback-test.xml # Logging configuration
│       ├── target/                  # Maven build output (generated)
│       │   ├── classes/             # Compiled main classes
│       │   ├── test-classes/        # Compiled test classes
│       │   ├── surefire-reports/    # TestNG HTML reports
│       │   ├── allure-results/      # Allure raw results
│       │   └── screenshots/         # Failure screenshots
│       ├── screenshots/             # Screenshot directory (runtime)
│       ├── logs/                    # Log files directory (runtime)
│       ├── reports/                 # Generated reports directory (runtime)
│       ├── pom.xml                  # 🔧 Maven config (Java 11, Appium 9.3.0, TestNG 7.8.0)
│       ├── testng.xml               # 🎯 TestNG suite configuration với groups
│       ├── run-tests.bat            # 🚀 Windows execution script với parameters
│       ├── run-tests.sh             # 🐧 Linux/Mac execution script
│       ├── CHANGELOG.md             # Version history
│       └── README.md                # 📚 Automation framework documentation
│
└── performance/                     # ⚡ Backend Performance Testing Framework
    └── jmeter/                      # JMeter + Java 17 performance tests
        ├── src/main/java/com/modis/performance/ # Java utilities cho JMeter
        │   ├── auth/                # AuthHelper.java - JWT authentication
        │   ├── generators/          # DataHelper.java - test data generation
        │   ├── parsers/             # JTLResultsParser.java - results parsing
        │   ├── reports/             # ReportGenerator.java - custom reports
        │   ├── utils/               # ResultsComparator.java - performance comparison
        │   ├── model/               # Data models (PerformanceMetrics, ComparisonResult)
        │   ├── comparator/          # PerformanceComparator.java
        │   ├── listeners/           # Custom JMeter listeners (empty - future use)
        │   ├── assertions/          # Custom assertions (empty - future use)
        │   └── config/              # Configuration classes (empty - future use)
        ├── configs/                 # 🔧 JMeter configuration files
        │   ├── modis-config.properties # Environment config (API URLs, timeouts)
        │   ├── auth-config.jmx      # Authentication setup fragment
        │   ├── http-defaults.jmx    # HTTP defaults fragment
        │   └── websocket-config.jmx # WebSocket configuration fragment
        ├── test-plans/              # 🎯 JMeter test plans (.jmx files)
        │   ├── load-tests/          # Load testing scenarios
        │   │   ├── modis-load-test.jmx      # Standard load test
        │   │   ├── modis-image-upload-test.jmx # Image upload performance
        │   │   └── modis-websocket-test.jmx # Real-time messaging test
        │   ├── stress-tests/        # Stress testing scenarios
        │   │   └── modis-stress-test.jmx    # Maximum capacity test
        │   ├── spike-tests/         # Spike testing scenarios
        │   │   └── modis-spike-test.jmx     # Traffic spike simulation
        │   ├── endurance-tests/     # Endurance testing scenarios
        │   │   └── modis-endurance-test.jmx # Long-running stability test
        │   ├── modis-load-test.jmx          # Main load test plan
        │   ├── modis-stress-test.jmx        # Main stress test plan
        │   ├── modis-spike-test.jmx         # Main spike test plan
        │   ├── modis-endurance-test.jmx     # Main endurance test plan
        │   └── modis-image-upload-test.jmx  # Image upload test plan
        ├── test-data/               # 📊 CSV test data files
        │   ├── users.csv            # User credentials cho authentication
        │   ├── messages.csv         # Sample messages cho chat testing
        │   ├── post-content.csv     # Social media post content
        │   ├── post-captions.csv    # Image post captions
        │   ├── reactions.csv        # Reaction types (like, love, etc.)
        │   ├── chat-messages.csv    # Chat message templates
        │   ├── friend-requests.csv  # Friend request scenarios
        │   ├── search-queries.csv   # Search query examples
        │   ├── sample-image-urls.csv # Image URLs cho upload tests
        │   └── sample-image-info.txt # Image metadata information
        ├── results/                 # 📈 Test execution results (.jtl files)
        ├── reports/                 # 📊 Generated HTML reports
        ├── docs/                    # 📖 Performance testing documentation
        │   ├── BEST_PRACTICES.md    # Performance testing best practices
        │   └── METRICS_GUIDE.md     # Metrics analysis guide
        ├── scripts/                 # 🛠️ Environment setup scripts
        │   ├── setup-environment.bat # Windows setup script
        │   └── setup-environment.sh # Linux/Mac setup script
        ├── target/                  # Maven build output (generated)
        │   ├── classes/             # Compiled Java utilities
        │   ├── modis-performance-tests-2.0.0.jar # Main JAR
        │   └── modis-performance-tests-2.0.0-jar-with-dependencies.jar # Fat JAR
        ├── pom.xml                  # 🔧 Maven config (Java 17, JMeter 5.6.2)
        ├── run-*.bat                # 🚀 Windows execution scripts
        │   ├── run-load-test.bat    # Load test runner với parameters
        │   ├── run-stress-test.bat  # Stress test runner
        │   ├── run-spike-test.bat   # Spike test runner
        │   ├── run-endurance-test.bat # Endurance test runner
        │   ├── run-image-upload-test.bat # Image upload test runner
        │   └── run-all-tests.bat    # All tests runner
        ├── run-*.sh                 # 🐧 Linux/Mac execution scripts
        ├── .gitignore               # Git ignore rules
        ├── CHANGELOG.md             # Version history
        ├── DEPLOYMENT_GUIDE.md      # Production deployment guide
        └── README.md                # 📚 Performance framework documentation
```

### 📋 Giải thích chức năng từng component:

#### 🤖 **Automation Framework** (`automation/appium-java/`)
- **Java Version**: Java 11 (theo pom.xml thực tế)
- **Appium Version**: 9.3.0 với UiAutomator2 driver
- **Test Framework**: TestNG 7.8.0 với groups và parallel execution
- **Reporting**: ExtentReports + Allure reports
- **Target App**: Modis React Native app (Android/iOS)
- **Page Objects**: 10+ page classes cho các màn hình chính
- **Test Classes**: 6 test classes theo chức năng (Auth, Navigation, Camera, etc.)

#### ⚡ **Performance Framework** (`performance/jmeter/`)
- **Java Version**: Java 17 (theo pom.xml thực tế)  
- **JMeter Version**: 5.6.2 với custom Java utilities
- **Target API**: `https://modis-backend.onrender.com` (deployed backend)
- **Test Types**: Load, Stress, Spike, Endurance, Image Upload tests
- **Java Utilities**: Authentication, data generation, results comparison
- **Test Data**: CSV files với realistic social media data
## 🚀 Hướng dẫn chạy đầy đủ

### 1. 🛠️ Setup môi trường

#### 1.1 Cài đặt Java (Quan trọng: 2 versions khác nhau)

**Java 11 cho Automation Framework:**
```bash
# Tải Java 11 từ: https://adoptium.net/temurin/releases/
# Chọn: OpenJDK 11 LTS > Windows > x64 > .msi

# Thiết lập JAVA_HOME cho automation:
# JAVA_HOME = C:\Program Files\Eclipse Adoptium\jdk-11.0.x.x-hotspot
# Thêm %JAVA_HOME%\bin vào System PATH

# Verify Java 11 installation
java -version
# Output: openjdk version "11.0.x"
```

**Java 17 cho Performance Framework:**
```bash
# Tải Java 17 từ: https://adoptium.net/temurin/releases/
# Chọn: OpenJDK 17 LTS > Windows > x64 > .msi

# Note: Có thể cài cả 2 versions, switch bằng JAVA_HOME khi cần
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

#### 1.3 Cài đặt Android Studio + Android SDK (cho Automation)
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

# Cài UiAutomator2 driver cho Android (theo config thực tế)
appium driver install uiautomator2

# Verify Appium installation
appium -v
appium driver list
```

#### 1.5 Setup Apache JMeter (cho Performance)
```bash
# JMeter hiện tại đã có tại:
# C:\Users\Camt9\Downloads\apache-jmeter-5.6.3\apache-jmeter-5.6.3\bin

# Thêm JMeter vào System PATH:
# System Properties > Environment Variables > System variables
# Chọn Path > Edit > New
# Thêm: C:\Users\Camt9\Downloads\apache-jmeter-5.6.3\apache-jmeter-5.6.3\bin

# Verify installation
jmeter -v
```

### 2. 🌐 Kiểm tra Backend API (Deployed trên Render.com)

**⚠️ QUAN TRỌNG**: Project sử dụng backend đã deploy, KHÔNG cần chạy local backend!

```bash
# Backend API đang chạy tại:
# https://modis-backend.onrender.com

# Kiểm tra API health:
curl https://modis-backend.onrender.com/actuator/health
# Expected response: {"status":"UP"}

# Kiểm tra API endpoints:
curl https://modis-backend.onrender.com/api/auth/health
# Hoặc mở browser: https://modis-backend.onrender.com

# Note: Render free tier có cold start ~30-90s nếu không có traffic
# Timeout trong config đã được set 90s để handle cold start
```

**Backend Tech Stack thực tế:**
- **Platform**: Render.com (cloud deployment)
- **Database**: MongoDB Atlas (cloud database)
- **Cache**: Redis Cloud
- **File Storage**: Cloudinary
- **Authentication**: JWT tokens
- **Real-time**: WebSocket support
        ├── results/                 # 📈 Test execution results
        │   └── .gitkeep             # Placeholder để giữ thư mục trong Git
        ├── reports/                 # 📊 Generated HTML reports
        │   └── .gitkeep             # Placeholder để giữ thư mục trong Git
        ├── pom.xml                  # 🔧 Maven configuration cho Java utilities
        ├── run-*.bat                # 🚀 Windows execution scripts
        │   ├── run-all-tests.bat    # Chạy tất cả test types
        │   ├── run-load-test.bat    # Chạy load test
        │   ├── run-stress-test.bat  # Chạy stress test
        │   ├── run-spike-test.bat   # Chạy spike test
        │   ├── run-endurance-test.bat # Chạy endurance test
        │   └── run-image-upload-test.bat # Chạy image upload test
        ├── run-*.sh                 # 🐧 Linux/Mac execution scripts
        │   ├── run-all-tests.sh     # Chạy tất cả test types
        │   ├── run-load-test.sh     # Chạy load test
        │   └── run-stress-test.sh   # Chạy stress test
        ├── .gitignore               # Git ignore rules
        ├── CHANGELOG.md             # Version history
        ├── DEPLOYMENT_GUIDE.md      # Production deployment guide
        └── README.md                # 📚 Chi tiết performance framework
```

### 📋 Giải thích chức năng chi tiết:

#### 🤖 Automation Framework (`automation/appium-java/`)
- **`src/main/java/`**: Chứa reusable utilities, base classes, configurations, helpers
- **`src/test/java/`**: Chứa test cases chính cho automation testing
- **`target/surefire-reports/`**: Chứa TestNG reports sau khi chạy test
- **`target/screenshots/`**: Chứa screenshots khi test fail
- **`pom.xml`**: Quản lý dependencies (Appium, TestNG, Selenium WebDriver)
- **`testng.xml`**: Cấu hình test suites, groups, parallel execution
- **`run-tests.bat/.sh`**: Scripts để chạy tests dễ dàng

#### ⚡ Performance Framework (`performance/jmeter/`)
- **`configs/`**: Chứa JMeter configuration fragments có thể tái sử dụng
- **`src/main/java/`**: Java utilities để tối ưu JMeter performance
- **`test-data/`**: Chứa dữ liệu test dạng CSV cho data-driven testing
- **`test-plans/`**: Chứa file .jmx cho các loại performance test khác nhau
- **`results/`**: Chứa file .jtl (raw results) sau khi chạy test
- **`reports/`**: Chứa HTML dashboard reports được generate từ .jtl files
- **`scripts/`**: Utility scripts cho setup environment và automation
- **`docs/`**: Documentation về best practices và metrics analysis

## 🚀 Hướng dẫn chạy đầy đủ

### 1. 🛠️ Setup môi trường

#### 1.1 Cài đặt Java 17
```bash
# Kiểm tra Java version hiện tại
java -version

# Nếu chưa có Java 17, tải từ: https://adoptium.net/
# Sau khi cài đặt, thiết lập biến môi trường:
# JAVA_HOME = C:\Program Files\Eclipse Adoptium\jdk-17.0.x.x-hotspot
# Thêm %JAVA_HOME%\bin vào PATH

# Verify installation
java -version
javac -version
```

#### 1.2 Cài đặt Maven
```bash
# Tải Maven từ: https://maven.apache.org/download.cgi
# Giải nén vào C:\apache-maven-3.9.x
# Thiết lập biến môi trường:
# MAVEN_HOME = C:\apache-maven-3.9.x
# Thêm %MAVEN_HOME%\bin vào PATH

# Verify installation
mvn -version
```

#### 1.3 Cài đặt Android Studio + Android SDK (cho Automation)
```bash
# Tải Android Studio từ: https://developer.android.com/studio
# Sau khi cài đặt, mở SDK Manager và cài:
# - Android SDK Platform (API 33/34)
# - Android SDK Command-line Tools
# - Android Emulator

# Thiết lập biến môi trường:
# ANDROID_HOME = C:\Users\%USERNAME%\AppData\Local\Android\Sdk
# Thêm vào PATH:
# %ANDROID_HOME%\platform-tools
# %ANDROID_HOME%\emulator
# %ANDROID_HOME%\cmdline-tools\latest\bin

# Verify installation
adb --version
```

#### 1.4 Cài đặt Node.js và Appium (cho Automation)
```bash
# Tải Node.js LTS từ: https://nodejs.org/
# Sau khi cài đặt Node.js:

# Cài Appium global
npm install -g appium

# Cài UiAutomator2 driver cho Android
appium driver install uiautomator2

# Verify installation
appium -v
appium driver list
```

#### 1.5 Setup Apache JMeter (cho Performance)
```bash
# JMeter hiện tại đã có tại:
# C:\Users\Camt9\Downloads\apache-jmeter-5.6.3\apache-jmeter-5.6.3\bin

# Thêm JMeter vào PATH:
# Mở System Properties > Environment Variables
# Thêm vào PATH: C:\Users\Camt9\Downloads\apache-jmeter-5.6.3\apache-jmeter-5.6.3\bin

# Verify installation
jmeter -v
```

### 2. 🏃‍♂️ Hướng dẫn chạy Backend trước khi test

**⚠️ QUAN TRỌNG**: Phải bật backend trước khi chạy automation/performance testing!

```bash
# Điều hướng đến thư mục backend
cd Modis_BE_TL

# Chạy backend với Maven
mvn spring-boot:run

# Hoặc nếu đã build JAR:
java -jar target/modis-backend-1.0.0.jar
```

**Kiểm tra backend đã chạy thành công:**
- Log hiển thị: `Tomcat started on port(s): 8080`
- Log hiển thị: `Started PlantShopApplication in X.XXX seconds`
- API endpoint: `http://localhost:8080`
- Health check: `http://localhost:8080/actuator/health`

### 3. 📱 Hướng dẫn chạy Automation Testing (Appium)

#### Bước 1: Chuẩn bị thiết bị
```bash
# Cách 1: Sử dụng Android Emulator
# Mở Android Studio > Device Manager > Chạy emulator

# Cách 2: Sử dụng thiết bị thật
# Cắm điện thoại Android qua USB
# Bật Developer Options > USB Debugging

# Verify thiết bị đã kết nối
adb devices
# Kết quả mong đợi: List of devices attached
# emulator-5554    device
```

#### Bước 2: Khởi động Appium Server
```bash
# Mở terminal riêng biệt (giữ mở suốt quá trình test)
appium --allow-insecure chromedriver_autodownload

# Log thành công:
# [Appium] Welcome to Appium v2.x.x
# [Appium] Appium REST http interface listener started on 0.0.0.0:4723
```

#### Bước 3: Chạy Automation Tests
```bash
# Điều hướng đến thư mục automation
cd testing\automation\appium-java

# Build dependencies (chỉ chạy lần đầu hoặc khi có thay đổi)
mvn clean install -DskipTests

# Chạy toàn bộ test suite
mvn test

# Chạy specific test suite
mvn test -DsuiteXmlFile=testng.xml

# Chạy specific test class
mvn test -Dtest=AuthenticationTests
mvn test -Dtest=CameraTests
mvn test -Dtest=FriendsTests
mvn test -Dtest=MessagingTests
mvn test -Dtest=NavigationTests
mvn test -Dtest=ProfileTests

# Chạy với custom parameters
mvn test -Dplatform=android -DdeviceName="Pixel_7_API_33"
```

#### Bước 4: Xem kết quả Automation
```bash
# TestNG Reports
start target\surefire-reports\index.html

# Screenshots (nếu test fail)
dir target\screenshots\

# Logs
type target\surefire-reports\testng-results.xml
```

### 4. ⚡ Hướng dẫn chạy Performance Testing (JMeter)

#### Bước 1: Đảm bảo Backend đang chạy
```bash
# Kiểm tra backend status
curl http://localhost:8080/actuator/health
# Hoặc mở browser: http://localhost:8080
```

#### Bước 2: Build Java Utilities
```bash
# Điều hướng đến thư mục performance
cd testing\performance\jmeter

# Build Java utilities (bắt buộc trước khi chạy JMeter)
mvn clean package -DskipTests

# Verify JAR file được tạo
dir target\modis-performance-tests-2.0.0-jar-with-dependencies.jar
```

#### Bước 3: Chạy JMeter GUI Mode (chỉ để debug/tạo test)
```bash
# Mở JMeter GUI
jmeter

# Trong JMeter GUI:
# File > Open > Chọn file .jmx trong test-plans/
# Ví dụ: test-plans\load-tests\modis-load-test.jmx

# Config environment variables nếu cần:
# Test Plan > User Defined Variables
# - BASE_URL: http://localhost:8080
# - USERS: 100
# - RAMP_UP: 60
# - DURATION: 300

# Chạy test bằng GUI (chỉ cho debug với ít users)
# Click nút Start (màu xanh)
```

#### Bước 4: Chạy Non-GUI Mode (Production mode - khuyến nghị)
```bash
# Xóa reports cũ
rmdir /s /q reports\html-report

# Chạy Load Test
jmeter -n -t test-plans\load-tests\modis-load-test.jmx -l results\load-test-results.jtl -e -o reports\load-test-html

# Chạy Stress Test
jmeter -n -t test-plans\stress-tests\modis-stress-test.jmx -l results\stress-test-results.jtl -e -o reports\stress-test-html

# Chạy Spike Test
jmeter -n -t test-plans\spike-tests\modis-spike-test.jmx -l results\spike-test-results.jtl -e -o reports\spike-test-html

# Chạy Endurance Test
jmeter -n -t test-plans\endurance-tests\modis-endurance-test.jmx -l results\endurance-test-results.jtl -e -o reports\endurance-test-html

# Chạy Image Upload Test
jmeter -n -t test-plans\modis-image-upload-test.jmx -l results\image-upload-results.jtl -e -o reports\image-upload-html
```

#### Bước 5: Chạy với custom parameters
```bash
# Override số lượng users, ramp-up time, duration
jmeter -n -t test-plans\load-tests\modis-load-test.jmx -l results\custom-results.jtl -Jusers=500 -Jrampup=120 -Jduration=600 -e -o reports\custom-html

# Chạy với environment khác
jmeter -n -t test-plans\load-tests\modis-load-test.jmx -l results\staging-results.jtl -Jbase_url=https://staging.modis.com -e -o reports\staging-html
```

#### Bước 6: Generate HTML Report từ existing .jtl
```bash
# Nếu đã có file .jtl từ lần chạy trước
jmeter -g results\load-test-results.jtl -o reports\new-html-report
```

#### Bước 7: Xem Performance Reports
```bash
# Mở HTML Dashboard
start reports\load-test-html\index.html

# Các metrics quan trọng cần chú ý:
# - APDEX Score (gần 1.0 là tốt)
# - Error % (< 1% là acceptable)
# - Throughput (requests/second)
# - Response Time (90th percentile)
# - Active Threads Over Time
```

### 5. 🔄 Flow hoạt động framework

#### 5.1 Automation Testing Flow
```text
1. Appium Server khởi động → Listen port 4723
2. Test code khởi tạo → AndroidDriver với capabilities
3. Driver kết nối device → Cài đặt/mở app
4. Page Objects thực hiện → UI interactions (tap, swipe, type)
5. Assertions verify → Expected vs Actual results
6. Screenshots capture → Khi test fail
7. TestNG generate → HTML reports với Pass/Fail status
```

#### 5.2 Performance Testing Flow
```text
1. JMeter load test plan → Parse .jmx file
2. Java utilities initialize → Auth helpers, data generators
3. Thread groups ramp up → Simulate concurrent users
4. HTTP requests execute → API calls với realistic data
5. Response validation → Status codes, response times
6. Results collection → Raw data vào .jtl files
7. HTML dashboard generation → Visual reports với metrics
```

### 6. 📊 Reports & Kết quả

#### 6.1 Automation Reports
- **TestNG HTML Report**: `testing/automation/appium-java/target/surefire-reports/index.html`
- **Screenshots**: `testing/automation/appium-java/target/screenshots/` (khi test fail)
- **Logs**: `testing/automation/appium-java/target/surefire-reports/testng-results.xml`
- **Allure Report** (nếu có): `testing/automation/appium-java/target/allure-results/`

#### 6.2 Performance Reports
- **HTML Dashboard**: `testing/performance/jmeter/reports/html-report/index.html`
- **Raw Results**: `testing/performance/jmeter/results/*.jtl` files
- **JMeter Logs**: `testing/performance/jmeter/jmeter.log`
- **Custom Reports**: Generated bởi Java utilities trong `src/main/java/`

### 7. 🚨 Troubleshooting cực kỳ chi tiết

#### 7.1 Lỗi Environment/System
| Lỗi | Nguyên nhân | Cách fix |
|-----|-------------|----------|
| `mvn is not recognized` | Maven chưa được add vào PATH | Thêm `%MAVEN_HOME%\bin` vào System PATH, restart CMD |
| `java is not recognized` | Java chưa được cài hoặc PATH sai | Cài Java 17, set JAVA_HOME, thêm `%JAVA_HOME%\bin` vào PATH |
| `JAVA_HOME is set to an invalid directory` | JAVA_HOME trỏ sai thư mục | Set JAVA_HOME = `C:\Program Files\Eclipse Adoptium\jdk-17.x.x` (không có \bin) |
| `adb is not recognized` | Android SDK chưa được add vào PATH | Thêm `%ANDROID_HOME%\platform-tools` vào PATH |

#### 7.2 Lỗi Automation (Appium)
| Lỗi | Nguyên nhân | Cách fix |
|-----|-------------|----------|
| `Connection refused` | Appium server chưa chạy | Chạy `appium` trong terminal riêng |
| `No connected devices` | Device chưa kết nối hoặc ADB issue | Chạy `adb kill-server` rồi `adb start-server`, check `adb devices` |
| `Unable to create driver session` | Capabilities sai hoặc app không tìm thấy | Check appPackage, appActivity trong config files |
| `Element not found` | Locator sai hoặc element chưa load | Dùng explicit waits, check element ID với Appium Inspector |
| `Device offline` | USB connection issue | Reconnect device, enable USB debugging lại |

#### 7.3 Lỗi Performance (JMeter)
| Lỗi | Nguyên nhân | Cách fix |
|-----|-------------|----------|
| `OutOfMemoryError: Java heap space` | JMeter heap size quá nhỏ | Tăng heap trong jmeter.bat: `-Xms2g -Xmx4g` |
| `HttpHostConnectException` | Backend không chạy hoặc URL sai | Check backend status, verify BASE_URL trong test plan |
| `Connection refused` | Port 8080 không available | Start backend trước, check port conflicts |
| `ClassNotFoundException` | Java utilities chưa được build | Chạy `mvn clean package -DskipTests` |
| `MySQL connection failed` | Database không available | Start MySQL service, check connection string |

#### 7.4 Lỗi Backend
| Lỗi | Nguyên nhân | Cách fix |
|-----|-------------|----------|
| `Port 8080 already in use` | Process khác đang dùng port 8080 | `netstat -ano \| findstr :8080`, kill process hoặc đổi port |
| `Unable to create driver session` | Database connection issue | Check MySQL service, verify application.properties |
| `Application failed to start` | Configuration hoặc dependency issue | Check logs, verify Java version, clean install |

### 8. 💡 Best Practices

#### 8.1 Automation Testing
- ✅ **Dùng explicit waits** thay vì `Thread.sleep()`
- ✅ **Sử dụng Page Object Model** để tái sử dụng code
- ✅ **Capture screenshots** khi test fail
- ✅ **Sử dụng testID/accessibilityId** thay vì XPath khi có thể
- ❌ **Không di chuột/keyboard** khi test đang chạy
- ❌ **Không hardcode test data** trong code

#### 8.2 Performance Testing
- ✅ **Luôn dùng Non-GUI mode** cho production tests
- ✅ **Warm-up backend** trước khi chạy test chính
- ✅ **Monitor system resources** trong khi test
- ✅ **Clean up results** trước mỗi lần chạy
- ❌ **Không chạy GUI mode** với > 50 users
- ❌ **Không chạy test** mà không có baseline

### 9. 📋 Commands Cheatsheet

#### 9.1 Maven Commands
```bash
# Build và install dependencies
mvn clean install -DskipTests

# Chạy tests
mvn test

# Build JAR file
mvn clean package -DskipTests

# Skip tests khi build
mvn install -DskipTests

# Chạy specific test class
mvn test -Dtest=ClassName

# Chạy với profile
mvn test -Pdev
```

#### 9.2 ADB Commands
```bash
# List connected devices
adb devices

# Install APK
adb install path\to\app.apk

# Uninstall app
adb uninstall com.package.name

# Clear app data
adb shell pm clear com.package.name

# View device logs
adb logcat

# Clear logs
adb logcat -c

# Restart ADB
adb kill-server && adb start-server
```

#### 9.3 Appium Commands
```bash
# Start Appium server
appium

# Start với specific port
appium -p 4724

# List installed drivers
appium driver list

# Install driver
appium driver install uiautomator2

# Update driver
appium driver update uiautomator2
```

#### 9.4 JMeter Commands
```bash
# Chạy test non-GUI mode
jmeter -n -t test.jmx -l results.jtl -e -o reports

# Generate report từ existing .jtl
jmeter -g results.jtl -o html-report

# Chạy với parameters
jmeter -n -t test.jmx -l results.jtl -Jusers=100 -Jduration=300

# Check JMeter version
jmeter -v

# Chạy distributed testing
jmeter -n -t test.jmx -R server1,server2 -l results.jtl
```

#### 9.5 Report Generation Commands
```bash
# Automation reports
start target\surefire-reports\index.html

# Performance reports
start reports\html-report\index.html

# View logs
type jmeter.log
type target\surefire-reports\testng-results.xml

# Cleanup commands
rmdir /s /q target\screenshots
rmdir /s /q reports\html-report
del /f /q results\*.jtl
```

Dưới đây là cấu hình chi tiết dành cho hệ điều hành **Windows 10/11**. Vui lòng cài đặt và thiết lập biến môi trường (Environment Variables) theo đúng thứ tự.

### 2.1. Java 17 (Nền tảng cốt lõi)
*   **Tải xuống:** Cài đặt JDK 17 từ Adoptium hoặc Oracle.
*   **Setup Path:**
    *   `JAVA_HOME` = `C:\Program Files\Java\jdk-17`
    *   Thêm `%JAVA_HOME%\bin` vào `Path` của System.
*   **Kiểm tra:** `java -version` và `javac -version` (Phải hiển thị version 17.x).

### 2.2. Apache Maven (Quản lý thư viện & Build)
*   **Tải xuống:** Tải file `.zip` của Maven (3.8+) từ `maven.apache.org` và giải nén vào `C:\maven`.
*   **Setup Path:**
    *   `MAVEN_HOME` = `C:\maven`
    *   Thêm `%MAVEN_HOME%\bin` vào `Path`.
*   **Kiểm tra:** `mvn -v`.

### 2.3. Android SDK & Studio (Cho Automation)
*   **Tải xuống:** Tải Android Studio. Cài đặt các package sau qua SDK Manager:
    *   Android SDK Platform (API 33/34)
    *   Android SDK Command-line Tools
    *   Android Emulator
*   **Setup Path:**
    *   `ANDROID_HOME` = `C:\Users\<Tên_Bạn>\AppData\Local\Android\Sdk`
    *   Thêm `%ANDROID_HOME%\platform-tools` vào `Path` (chứa lệnh adb).
    *   Thêm `%ANDROID_HOME%\cmdline-tools\latest\bin` vào `Path`.
*   **Kiểm tra:** `adb --version`.

### 2.4. NodeJS & Appium (Cho Automation)
*   **Tải xuống:** Cài NodeJS bản LTS (v18 hoặc v20).
*   **Cài đặt Appium:** Mở PowerShell quyền Admin:
    ```bash
    npm install -g appium
    appium driver install uiautomator2
    ```
*   **Kiểm tra:** `appium -v` và `appium driver list`.

---

## 3. Apache JMeter Setup (Performance)

Giả sử bạn đã giải nén JMeter tại thư mục: `C:\Users\Camt9\Downloads\apache-jmeter-5.6.3\apache-jmeter-5.6.3`

### 3.1. Thêm JMeter vào PATH
*   Copy đường dẫn `C:\Users\Camt9\Downloads\apache-jmeter-5.6.3\apache-jmeter-5.6.3\bin`.
*   Mở **Environment Variables** -> **System variables** -> Nháy đúp vào **Path** -> Chọn **New** -> Dán đường dẫn trên vào.
*   Mở cửa sổ CMD mới, gõ `jmeter -v` để xác nhận.

### 3.2. Cấu hình JVM Heap (Quan trọng cho Stress Test)
Mặc định JMeter chỉ sử dụng 1GB RAM, nếu bạn giả lập hàng ngàn User, JMeter sẽ bị crash (`OutOfMemoryError`).
1.  Vào thư mục `bin` của JMeter, chuột phải vào file `jmeter.bat` -> Mở bằng Notepad.
2.  Tìm (Ctrl+F) dòng chữ `set HEAP`.
3.  Sửa dòng đó thành:
    ```bat
    set HEAP=-Xms2g -Xmx2g -XX:MaxMetaspaceSize=256m
    ```
    *(Nếu máy bạn có 16GB RAM và chạy test rất lớn, có thể đẩy lên `-Xms4g -Xmx4g`).*

---

## 4. Automation Testing Setup & Execution

### 4.1. Chuẩn bị thiết bị (Device)
*   **Cách 1 (Emulator):** Mở Android Studio -> Device Manager -> Nhấn nút "Play" để bật một máy ảo.
*   **Cách 2 (Máy thật):** Cắm cáp điện thoại Android, vào Cài đặt -> Tùy chọn nhà phát triển -> Bật **USB Debugging**.
*   **Xác nhận kết nối:** Gõ `adb devices` trong CMD. Đảm bảo thấy danh sách thiết bị có trạng thái là `device`.

### 4.2. Khởi chạy Appium Server
Mở một cửa sổ CMD/PowerShell (đặt tên là Cửa sổ 1) và gõ:
```bash
appium --allow-insecure chromedriver_autodownload
```
*Lưu ý: Bạn phải giữ nguyên cửa sổ này không được tắt trong suốt thời gian chạy test.*

### 4.3. Biên dịch và Chạy Test
Mở cửa sổ CMD thứ 2 (đặt tên là Cửa sổ 2):
```bash
cd testing\automation\appium-java
```

**Cài đặt thư viện lần đầu (Chỉ chạy 1 lần):**
```bash
mvn clean install -DskipTests
```

**Các lệnh chạy Test:**
```bash
# 1. Chạy TOÀN BỘ kịch bản test (Regression Suite)
mvn test

# 2. Chạy thông qua file cấu hình Suite (Thực thi các luồng đã định nghĩa)
mvn test -DsuiteXmlFile=testng.xml

# 3. Chạy MỘT test class cụ thể (Tùy chọn 1 trong các Class dưới đây)
mvn test -Dtest=AuthenticationTests
mvn test -Dtest=CameraTests
mvn test -Dtest=FriendsTests
mvn test -Dtest=MessagingTests
mvn test -Dtest=NavigationTests
mvn test -Dtest=ProfileTests
```

---

## 5. Performance Testing Setup & Execution

Performance testing của Modis không dùng script JSR223 Groovy lỏng lẻo, mà được đóng gói hoàn toàn vào Java để tối ưu CPU.

### 5.1. Biên dịch Java Utilities
Do JMeter `.jmx` gọi trực tiếp vào class Java (`AuthHelper`, `DataHelper`), bạn phải build chúng trước.
```bash
cd testing\performance\jmeter
mvn clean package -DskipTests
```
Lệnh này tạo ra `modis-performance-tests-2.0.0-jar-with-dependencies.jar` nằm trong thư mục `target/`. JMeter sẽ tự động nạp file này.

### 5.2. Chạy Test Thực Tế (Non-GUI Mode)
> **Quy tắc Vàng:** KHÔNG BAO GIỜ dùng JMeter GUI để chạy tải thực. Chỉ dùng GUI để tạo file `.jmx` và Debug.

Khởi động Backend Server của Modis, đảm bảo API đang sống. Sau đó mở CMD tại thư mục `testing\performance\jmeter`:
```bash
# Xóa thư mục HTML report cũ nếu có
rmdir /s /q reports\html-report

# Chạy lệnh JMeter chuẩn Production
jmeter -n -t test-plans\load-tests\modis-load-test.jmx -l results\results.jtl -e -o reports\html-report
```
**Giải nghĩa lệnh:**
*   `-n`: Kích hoạt Non-GUI (CLI) mode.
*   `-t`: Chỉ định file kịch bản cần chạy.
*   `-l`: Chỉ định file lưu kết quả thô (`.jtl`).
*   `-e -o`: Tự động biên dịch `.jtl` thành Dashboard HTML trực quan vào thư mục `reports/html-report`.

---

## 6. Đọc Reports (Báo Cáo)

### 6.1. Automation Reports
*   **TestNG Report:** Nằm tại `testing/automation/appium-java/target/surefire-reports/index.html`. Cung cấp danh sách các Pass/Fail và Stacktrace lỗi (nếu có).
*   **Screenshots:** Khi một testcase (ví dụ Login) thất bại, ảnh chụp màn hình thiết bị vào khoảnh khắc đó sẽ được lưu ở `target/screenshots/`.

### 6.2. Performance Reports
*   **HTML Dashboard:** Nằm tại `testing/performance/jmeter/reports/html-report/index.html`. Bạn cần chú ý các bảng sau:
    *   **APDEX (Application Performance Index):** Điểm sức khỏe tổng thể. Xanh lá cây (gần 1.0) là hoàn hảo.
    *   **Statistics Table:** Chú ý cột `Error %` (phải < 1%), `Throughput` (Transactions/sec - càng cao càng tốt), và `90th pct / 95th pct` (Thời gian phản hồi tính bằng millisecond cho 90% lượng user).
*   **Log kỹ thuật:** `jmeter.log` nằm ở thư mục root, ghi lại các lỗi nội bộ của bản thân phần mềm JMeter.

---

## 7. Troubleshooting (Lỗi Thường Gặp)

### 7.1 Lỗi về Môi trường / System
1.  **`mvn is not recognized`** hoặc **`javac is not recognized`**:
    *   *Nguyên nhân:* Biến môi trường PATH chưa được set đúng.
    *   *Sửa lỗi:* Kiểm tra lại System Properties -> Environment Variables. Đảm bảo khởi động lại CMD/PowerShell sau khi set.
2.  **`JAVA_HOME is set to an invalid directory`**:
    *   *Nguyên nhân:* JAVA_HOME không được chứa thư mục `\bin` ở đuôi. Nó phải trỏ vào gốc (VD: `C:\Program Files\Java\jdk-17`).

### 7.2 Lỗi về Automation (Appium)
1.  **`Connection refused (Connection refused)`**:
    *   *Nguyên nhân:* Appium server chưa được bật. Hãy chạy lệnh `appium` ở một cửa sổ riêng.
2.  **`No connected devices` / `Could not find a connected Android device`**:
    *   *Nguyên nhân:* Chưa bật máy ảo hoặc cáp lỏng, ADB bị treo.
    *   *Sửa lỗi:* Chạy `adb kill-server`, sau đó `adb start-server` và check lại `adb devices`.

### 7.3 Lỗi về Performance (JMeter)
1.  **`java.lang.OutOfMemoryError: Java heap space`**:
    *   *Nguyên nhân:* Chạy test quá lớn với RAM quá ít.
    *   *Sửa lỗi:* Tăng biến `HEAP` trong `jmeter.bat` lên `-Xms2g -Xmx2g` hoặc cao hơn. Đảm bảo đang chạy ở chế độ Non-GUI.
2.  **`Non HTTP response code: org.apache.http.conn.HttpHostConnectException`**:
    *   *Nguyên nhân:* Server Backend (Modis_BE) đã bị "chết" vì quá tải hoặc bạn quên chưa bật backend.
    *   *Sửa lỗi:* Check lại database, restart backend, giảm số lượng users (`Thread Group`) trong file `.jmx` xuống.

---

## 8. Best Practices (Quy tắc thực hành chuẩn)

### Quy tắc Performance Testing
1.  **Warm-up:** Hệ thống Java (JVM, Tomcat, Hibernate) luôn chậm ở những phút đầu. Hãy set thời gian `Ramp-up` trong JMeter ít nhất 30 giây để hệ thống "nóng máy", tránh bị spike ảo.
2.  **Không chạy GUI Mode:** Không bao giờ click nút "Start" xanh lá trên JMeter khi cấu hình số lượng User > 50.
3.  **Tách biệt Network:** Chạy JMeter trên máy tính cá nhân qua Wifi sẽ bị "nút thắt cổ chai" mạng (Network Bottleneck). Kết quả tốt nhất là chạy JMeter CLI trên một server AWS/EC2 nằm cùng Local Network với Backend.

### Quy tắc Automation Testing
1.  **Không dùng `Thread.sleep(5000)`:** Việc Fix cứng thời gian chờ là tối kỵ. Thay vào đó hãy dùng `WebDriverWait` (Explicit Wait) để đợi đúng lúc Element xuất hiện rồi click ngay lập tức.
2.  **Đừng di chuột/Gõ phím khi test chạy:** Appium giả lập thao tác thật, việc bạn cố tình click chuột vào màn hình máy ảo sẽ làm sai lệch tọa độ và làm hỏng test.
3.  **Dữ liệu độc lập:** Mỗi Test Method nên tự tạo dữ liệu riêng (VD: Đăng ký một user random bằng Java Faker) thay vì dùng chung 1 account, tránh đụng độ (Race condition).

---

## 9. Commands Cheatsheet

Dưới đây là phao cứu sinh nhanh cho mọi thao tác.

### 🔧 Maven & Java
```bash
# Build code, tải thư viện nhưng không chạy test
mvn clean install -DskipTests

# Build code thành file JAR
mvn clean package -DskipTests
```

### 📱 Appium & ADB
```bash
# Bật Appium
appium --allow-insecure chromedriver_autodownload

# Quản lý thiết bị Android
adb devices           # Liệt kê thiết bị
adb logcat -c         # Xóa log cũ trên điện thoại
adb logcat            # Xem log real-time từ điện thoại
adb kill-server       # Reset kết nối ADB
```

### 🚀 JMeter
```bash
# Chạy Non-GUI Test + Gen Report HTML tự động
jmeter -n -t test-plans/my-test.jmx -l results/my-results.jtl -e -o reports/my-html-report

# Tạo Report HTML từ file .jtl cũ (đã chạy trước đó)
jmeter -g results/my-results.jtl -o reports/my-html-report

# So sánh 2 kết quả Performance (Sử dụng công cụ Modis tự viết)
java -cp target/modis-performance-tests-2.0.0-jar-with-dependencies.jar com.modis.performance.utils.ResultsComparator
```

### 3. 📱 Hướng dẫn chạy Automation Testing (Appium)

#### Bước 1: Chuẩn bị thiết bị Android

**Cách 1: Sử dụng Android Emulator (Khuyến nghị)**
```bash
# Mở Android Studio
# Tools > Device Manager (hoặc AVD Manager)
# Create Device > Chọn Pixel 4 > API 30 (Android 11.0) > Finish
# Click nút ▶️ để start emulator

# Verify emulator đã chạy
adb devices
# Expected: emulator-5554    device
```

**Cách 2: Sử dụng thiết bị thật**
```bash
# Trên điện thoại Android:
# Settings > About phone > Tap "Build number" 7 lần
# Settings > Developer options > Enable "USB debugging"
# Cắm cáp USB vào máy tính

# Verify thiết bị đã kết nối
adb devices
# Expected: XXXXXXXXXX    device
```

#### Bước 2: Khởi động Appium Server
```bash
# Mở terminal/PowerShell riêng biệt (giữ mở suốt quá trình test)
appium

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

# Kiểm tra config thực tế
type src\test\resources\config\test.properties

# Config chính (theo file thực tế):
# platform=android
# deviceName=Android Emulator
# platformVersion=11.0
# automationName=UiAutomator2
# android.appPackage=com.modis.app
# android.appActivity=com.modis.MainActivity
# appium.serverUrl=http://127.0.0.1:4723
```

#### Bước 4: Build Dependencies (Java 11)
```bash
# Đảm bảo JAVA_HOME trỏ đến Java 11
echo %JAVA_HOME%
java -version

# Build và tải dependencies (chỉ chạy lần đầu hoặc khi có thay đổi)
mvn clean install -DskipTests

# Verify build thành công:
# [INFO] BUILD SUCCESS
# [INFO] Total time: XX.XXX s
```

#### Bước 5: Chạy Automation Tests

**5.1 Sử dụng execution script (Khuyến nghị):**
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

# Xem help
run-tests.bat --help
```

**5.2 Chạy trực tiếp với Maven:**
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

**6.2 Allure Reports (nếu có):**
```bash
# Generate Allure report
mvn allure:report

# Serve Allure report
mvn allure:serve

# Mở Allure report
start target\allure-report\index.html
```

**6.3 Screenshots và Logs:**
```bash
# Xem screenshots (khi test fail)
dir target\screenshots\
start target\screenshots\

# Xem logs
type logs\test-execution.log
```

### 4. ⚡ Hướng dẫn chạy Performance Testing (JMeter)

#### Bước 1: Kiểm tra Backend API
```bash
# Verify backend đang hoạt động
curl https://modis-backend.onrender.com/actuator/health

# Test API response time (should be <90s for cold start)
curl -w "@curl-format.txt" -o /dev/null -s https://modis-backend.onrender.com/api/auth/health
```

#### Bước 2: Build Java Utilities (Java 17)
```bash
# Điều hướng đến thư mục performance
cd testing\performance\jmeter

# Switch to Java 17 nếu cần
# set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot

# Build Java utilities (bắt buộc trước khi chạy JMeter)
mvn clean package -DskipTests

# Verify JAR files được tạo thành công:
dir target\modis-performance-tests-2.0.0-jar-with-dependencies.jar
# File size should be > 5MB (chứa all dependencies)
```

#### Bước 3: Chạy Performance Tests

**3.1 Sử dụng execution scripts (Khuyến nghị):**
```bash
# Chạy load test với default parameters (50 users, 5min)
run-load-test.bat

# Chạy với custom parameters (users, ramp-up, duration)
run-load-test.bat 100 300 600

# Chạy các test types khác
run-stress-test.bat
run-spike-test.bat
run-endurance-test.bat
run-image-upload-test.bat

# Chạy tất cả tests
run-all-tests.bat
```

**3.2 Chạy trực tiếp với JMeter CLI:**
```bash
# Load Test (theo config thực tế)
jmeter -n -t test-plans\load-tests\modis-load-test.jmx -l results\load-test-results.jtl -e -o reports\load-test-html

# Stress Test
jmeter -n -t test-plans\stress-tests\modis-stress-test.jmx -l results\stress-test-results.jtl -e -o reports\stress-test-html

# Với custom parameters (theo modis-config.properties)
jmeter -n -t test-plans\load-tests\modis-load-test.jmx -l results\custom-results.jtl -Jusers=100 -Jramp_up=300 -Jduration=600 -e -o reports\custom-html
```

#### Bước 4: Xem Performance Reports

**4.1 HTML Dashboard:**
```bash
# Mở main dashboard
start reports\load-test-html\index.html

# Hoặc specific report
start reports\stress-test-html\index.html
```

**4.2 Key Metrics cần chú ý (theo thực tế Render.com):**
- **Response Time Thresholds** (từ modis-config.properties):
  - Authentication: < 2000ms
  - Feed API: < 3000ms  
  - Image Upload: < 10000ms
  - Messaging: < 500ms
  - General API: < 1000ms
- **Error Rate**: < 1% (acceptable)
- **Cold Start**: First request có thể mất 30-90s (Render free tier)
- **Throughput**: Depends on Render.com limits

### 5. 🔄 Flow hoạt động thực tế

#### 5.1 Automation Testing Flow
```text
1. Appium Server (localhost:4723) ← theo config thực tế
2. AndroidDriver với UiAutomator2 ← theo android.properties
3. Target App: com.modis.app/com.modis.MainActivity ← theo config
4. Page Objects: 10+ classes cho UI interactions
5. TestNG Groups: smoke, regression, authentication, etc. ← theo testng.xml
6. Reports: TestNG + Allure + Screenshots ← theo pom.xml
7. API Backend: https://modis-backend.onrender.com ← deployed backend
```

#### 5.2 Performance Testing Flow
```text
1. JMeter CLI mode (non-GUI) ← production best practice
2. Java 17 utilities ← theo pom.xml thực tế
3. Target API: https://modis-backend.onrender.com ← theo config
4. Test Types: Load/Stress/Spike/Endurance ← theo test-plans thực tế
5. Timeouts: 90s cho cold start ← theo Render.com characteristics
6. Results: .jtl files + HTML dashboard ← theo execution scripts
7. Comparison: Java utilities cho performance regression ← custom tools
```

### 6. 📊 Reports & Kết quả

#### 6.1 Automation Reports
- **TestNG HTML Report**: `target/surefire-reports/index.html`
- **Allure Report**: `target/allure-report/index.html` (nếu generated)
- **Screenshots**: `target/screenshots/` (khi test fail)
- **Logs**: `logs/test-execution.log`

#### 6.2 Performance Reports
- **HTML Dashboard**: `reports/{test-type}-html/index.html`
- **Raw Results**: `results/*.jtl` files
- **JMeter Logs**: `jmeter.log`
- **Custom Comparison**: Generated bởi Java utilities

### 7. 🚨 Troubleshooting theo Project thực tế

#### 7.1 Environment Issues
| Lỗi | Nguyên nhân | Cách fix |
|-----|-------------|----------|
| `java is not recognized` | Java chưa cài hoặc PATH sai | Install Java 11/17, set JAVA_HOME, add to PATH |
| `mvn is not recognized` | Maven chưa trong PATH | Add `%MAVEN_HOME%\bin` to System PATH |
| `Wrong Java version` | Automation cần Java 11, Performance cần Java 17 | Switch JAVA_HOME theo framework đang dùng |
| `adb is not recognized` | Android SDK PATH missing | Add `%ANDROID_HOME%\platform-tools` to PATH |

#### 7.2 Appium Connection Issues
| Lỗi | Nguyên nhân | Cách fix |
|-----|-------------|----------|
| `Connection refused (Connection refused)` | Appium server chưa start | Run `appium` trong terminal riêng |
| `Could not find a connected Android device` | Device chưa connect hoặc ADB issue | `adb kill-server && adb start-server`, check `adb devices` |
| `Unable to create driver session` | App package/activity sai | Verify `com.modis.app` và `com.modis.MainActivity` |
| `Session creation timeout` | Device quá chậm | Increase timeout trong config |

#### 7.3 Backend API Issues (Render.com specific)
| Lỗi | Nguyên nhân | Cách fix |
|-----|-------------|----------|
| `Connection timeout` | Render cold start | Wait 30-90s, retry request |
| `502 Bad Gateway` | Render service down | Check Render dashboard, wait for recovery |
| `Rate limiting` | Too many requests | Implement delays trong test scripts |
| `JWT token expired` | Authentication issue | Check AuthHelper.java implementation |

#### 7.4 Performance Test Issues
| Lỗi | Nguyên nhân | Cách fix |
|-----|-------------|----------|
| `OutOfMemoryError` | JMeter heap size quá nhỏ | Increase heap: `-Xms2g -Xmx4g` |
| `ClassNotFoundException` | Java utilities chưa build | Run `mvn clean package -DskipTests` |
| High error rate (>5%) | Render.com overload | Reduce concurrent users |
| Zero throughput | Test plan configuration issue | Verify .jmx file và parameters |

### 8. 💡 Best Practices theo Project thực tế

#### 8.1 Automation Testing
- ✅ **Sử dụng TestNG groups** để organize tests (smoke, regression)
- ✅ **Page Object Model** với 10+ page classes
- ✅ **Explicit waits** thay vì Thread.sleep()
- ✅ **Screenshots on failure** với ScreenshotUtils.java
- ✅ **Config-driven testing** với .properties files
- ❌ **Không hardcode app package/activity** trong test code
- ❌ **Không assume local backend** - dùng deployed API

#### 8.2 Performance Testing
- ✅ **Always non-GUI mode** cho production tests
- ✅ **Handle Render.com cold start** với 90s timeout
- ✅ **Use Java utilities** thay vì Groovy scripts
- ✅ **Monitor Render.com limits** và adjust accordingly
- ✅ **Custom comparison tools** cho regression analysis
- ❌ **Không ignore cold start delays** của Render free tier
- ❌ **Không chạy quá nhiều concurrent users** với free tier

### 9. 📋 Commands Cheatsheet theo Project thực tế

#### 9.1 Automation Commands
```bash
# Environment setup
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-11.x.x-hotspot
cd testing\automation\appium-java

# Build và test
mvn clean install -DskipTests
mvn test -Dgroups=smoke
run-tests.bat --suite regression

# Reports
start target\surefire-reports\index.html
mvn allure:serve
```

#### 9.2 Performance Commands
```bash
# Environment setup
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot
cd testing\performance\jmeter

# Build và test
mvn clean package -DskipTests
run-load-test.bat 50 300 600
jmeter -n -t test-plans\load-tests\modis-load-test.jmx -l results\test.jtl -e -o reports\html

# Analysis
java -cp target\modis-performance-tests-2.0.0-jar-with-dependencies.jar com.modis.performance.utils.ResultsComparator
```

#### 9.3 API Testing Commands
```bash
# Backend health check
curl https://modis-backend.onrender.com/actuator/health

# API endpoints
curl https://modis-backend.onrender.com/api/auth/health
curl -X POST https://modis-backend.onrender.com/api/auth/login

# Performance check
curl -w "@curl-format.txt" -o /dev/null -s https://modis-backend.onrender.com
```

---

## 🎯 Kết luận

Framework testing của Modis được thiết kế để test một ứng dụng mạng xã hội thực tế với:

- **Backend deployed** trên Render.com (không phải local)
- **Mobile apps** React Native kết nối với cloud API
- **Automation testing** với Appium + Java 11
- **Performance testing** với JMeter + Java 17
- **Production-ready** configuration và best practices

Tài liệu này phản ánh chính xác project thực tế và có thể sử dụng ngay để onboard QA team mới.