# Modis Performance Testing Framework - JMeter + Java 17
> Framework performance testing cho backend API của ứng dụng mạng xã hội Modis

---

## 🎯 Tổng quan Framework

**Modis Performance Framework** là hệ thống kiểm thử hiệu năng chuyên nghiệp cho backend API của Modis, sử dụng **Apache JMeter** kết hợp với **Java 17 utilities** để đạt hiệu suất tối ưu.

### 🏗️ **Kiến trúc thực tế:**
```text
┌─────────────────────────────────────────────────────────────┐
│              MODIS PERFORMANCE ARCHITECTURE                 │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  🎯 JMeter Tests        🔧 Java Utilities   📊 Results      │
│  ┌─────────────────┐    ┌─────────────────┐   ┌──────────┐  │
│  │ .jmx Test Plans │◄──►│ AuthHelper      │──►│ HTML     │  │
│  │ Load/Stress     │    │ DataHelper      │   │ Reports  │  │
│  │ Spike/Endurance │    │ ResultsParser   │   │ .jtl     │  │
│  │ Image Upload    │    │ Comparator      │   │ Files    │  │
│  │                 │    │ Java 17         │   │          │  │
│  │ Non-GUI Mode    │    │ Maven 3.9.x     │   │ Charts   │  │
│  └─────────────────┘    └─────────────────┘   └──────────┘  │
│                                                             │
│  🌐 Target: https://modis-backend.onrender.com             │
│  ⏱️  Timeouts: 90s (Render.com cold start)                 │
│  📈 Metrics: Response Time, Throughput, Error Rate         │
└─────────────────────────────────────────────────────────────┘
```

### 🔧 **Tech Stack thực tế:**
- **Java Version**: Java 17 (theo pom.xml)
- **JMeter Version**: 5.6.2 với custom Java utilities
- **Build Tool**: Maven 3.9.x
- **Target API**: `https://modis-backend.onrender.com` (deployed backend)
- **Test Types**: Load, Stress, Spike, Endurance, Image Upload
- **Reporting**: HTML Dashboard + Custom Java reports
- **Data Format**: CSV test data + .jtl results

---

## 📁 Cấu trúc thư mục thực tế

Framework được tổ chức để tách biệt **Java utilities**, **JMeter test plans**, và **test data** dựa trên source code thực tế:

```text
testing/performance/jmeter/
├── src/main/java/com/modis/performance/ # 🏗️ Java Utilities cho JMeter
│   ├── auth/                         # 🔐 Authentication & JWT Handling
│   │   └── AuthHelper.java           # JWT token generation/validation
│   ├── generators/                   # 🎲 Test Data Generators
│   │   └── DataHelper.java           # Random data generation (users, messages, posts)
│   ├── parsers/                      # 📊 Results Parsing
│   │   └── JTLResultsParser.java     # Parse .jtl files to Java objects
│   ├── reports/                      # 📈 Custom Report Generation
│   │   └── ReportGenerator.java      # Generate custom JSON/HTML reports
│   ├── utils/                        # 🛠️ Utilities
│   │   └── ResultsComparator.java    # Compare performance results between runs
│   ├── model/                        # 📋 Data Models
│   │   ├── PerformanceMetrics.java   # Performance metrics model
│   │   ├── ComparisonResult.java     # Comparison result model
│   │   ├── LabelComparison.java      # Label-specific comparison
│   │   └── RegressionDetail.java     # Regression analysis details
│   ├── comparator/                   # 📊 Performance Comparison
│   │   └── PerformanceComparator.java # Advanced performance comparison logic
│   ├── listeners/                    # 👂 JMeter Listeners (empty - future use)
│   ├── assertions/                   # ✅ Custom Assertions (empty - future use)
│   └── config/                       # 🔧 Configuration (empty - future use)
├── configs/                          # ⚙️ JMeter Configuration Files
│   ├── modis-config.properties       # 🌐 Environment config (API URLs, timeouts, thresholds)
│   ├── auth-config.jmx              # 🔐 Authentication setup fragment
│   ├── http-defaults.jmx            # 🌐 HTTP defaults fragment (headers, timeouts)
│   └── websocket-config.jmx         # 🔌 WebSocket configuration fragment
├── test-plans/                       # 🎯 JMeter Test Plans (.jmx files)
│   ├── load-tests/                   # 📈 Load Testing Scenarios
│   │   ├── modis-load-test.jmx       # Standard load test (50 users, 10min)
│   │   ├── modis-image-upload-test.jmx # Image upload performance test
│   │   └── modis-websocket-test.jmx  # Real-time messaging load test
│   ├── stress-tests/                 # 🔥 Stress Testing Scenarios
│   │   └── modis-stress-test.jmx     # Maximum capacity test (200 users)
│   ├── spike-tests/                  # ⚡ Spike Testing Scenarios
│   │   └── modis-spike-test.jmx      # Traffic spike simulation (500 users)
│   ├── endurance-tests/              # ⏰ Endurance Testing Scenarios
│   │   └── modis-endurance-test.jmx  # Long-running stability test (30 users, 2h)
│   ├── modis-load-test.jmx           # Main load test plan
│   ├── modis-stress-test.jmx         # Main stress test plan
│   ├── modis-spike-test.jmx          # Main spike test plan
│   ├── modis-endurance-test.jmx      # Main endurance test plan
│   └── modis-image-upload-test.jmx   # Image upload specific test
├── test-data/                        # 📊 CSV Test Data Files
│   ├── users.csv                     # User credentials (username, password, email)
│   ├── messages.csv                  # Sample messages cho chat performance
│   ├── post-content.csv              # Social media post content templates
│   ├── post-captions.csv             # Image post captions
│   ├── reactions.csv                 # Reaction types (like, love, angry, sad, wow)
│   ├── chat-messages.csv             # Chat message templates với different lengths
│   ├── friend-requests.csv           # Friend request scenarios
│   ├── search-queries.csv            # Search query examples
│   ├── sample-image-urls.csv         # Image URLs cho upload performance testing
│   └── sample-image-info.txt         # Image metadata và guidelines
├── results/                          # 📈 Test Execution Results (.jtl files)
│   └── .gitkeep                      # Placeholder (results generated at runtime)
├── reports/                          # 📊 Generated HTML Reports
│   └── .gitkeep                      # Placeholder (reports generated at runtime)
├── docs/                             # 📖 Documentation
│   ├── BEST_PRACTICES.md             # Performance testing best practices
│   └── METRICS_GUIDE.md              # Metrics analysis guide
├── scripts/                          # 🛠️ Environment Setup Scripts
│   ├── setup-environment.bat         # Windows environment setup
│   └── setup-environment.sh          # Linux/Mac environment setup
├── target/                           # 🎯 Maven Build Output (generated)
│   ├── classes/                      # Compiled Java utilities
│   ├── modis-performance-tests-2.0.0.jar # Main JAR file
│   └── modis-performance-tests-2.0.0-jar-with-dependencies.jar # Fat JAR với all dependencies
├── pom.xml                           # 🔧 Maven Configuration (Java 17, JMeter 5.6.2)
├── run-*.bat                         # 🚀 Windows Execution Scripts
│   ├── run-load-test.bat             # Load test runner với parameters
│   ├── run-stress-test.bat           # Stress test runner
│   ├── run-spike-test.bat            # Spike test runner
│   ├── run-endurance-test.bat        # Endurance test runner
│   ├── run-image-upload-test.bat     # Image upload test runner
│   └── run-all-tests.bat             # All tests runner sequentially
├── run-*.sh                          # 🐧 Linux/Mac Execution Scripts
│   ├── run-load-test.sh              # Load test runner
│   └── run-stress-test.sh            # Stress test runner
├── .gitignore                        # 📝 Git ignore rules
├── CHANGELOG.md                      # 📋 Version history
├── DEPLOYMENT_GUIDE.md               # 🚀 Production deployment guide
└── README.md                         # 📚 Performance framework documentation
```

### 📋 Giải thích chức năng từng component:

#### 🏗️ **Java Utilities** (`src/main/java/`)
- **`auth/AuthHelper.java`**: JWT token generation và validation cho secure API testing
- **`generators/DataHelper.java`**: Generate realistic test data (users, messages, posts)
- **`parsers/JTLResultsParser.java`**: Parse JMeter .jtl results thành Java objects
- **`reports/ReportGenerator.java`**: Generate custom performance reports
- **`utils/ResultsComparator.java`**: Compare performance giữa multiple test runs
- **`model/`**: Data models cho type-safe operations (PerformanceMetrics, etc.)
- **`comparator/PerformanceComparator.java`**: Advanced performance comparison logic

#### ⚙️ **Configuration** (`configs/`)
- **`modis-config.properties`**: Environment variables (API URLs, timeouts, thresholds)
- **`auth-config.jmx`**: Authentication setup có thể tái sử dụng
- **`http-defaults.jmx`**: HTTP request defaults (headers, timeouts, SSL)
- **`websocket-config.jmx`**: WebSocket configuration cho real-time features

#### 🎯 **Test Plans** (`test-plans/`)
- **`load-tests/`**: Normal traffic simulation (50 users, 10 minutes)
- **`stress-tests/`**: Maximum capacity testing (200 users, 15 minutes)
- **`spike-tests/`**: Sudden traffic spike (500 users, 5 minutes)
- **`endurance-tests/`**: Long-running stability (30 users, 2 hours)
- **Image upload tests**: Specific cho file upload performance

#### 📊 **Test Data** (`test-data/`)
- **CSV files**: Realistic social media data cho data-driven testing
- **User credentials**: Authentication testing data
- **Content templates**: Posts, messages, captions cho realistic load
- **Image URLs**: File upload performance testing

#### 🎯 **Build & Execution**
- **`pom.xml`**: Maven config với Java 17, JMeter 5.6.2, custom dependencies
- **`run-*.bat`**: Windows scripts với command-line parameters
- **Target API**: `https://modis-backend.onrender.com` (deployed backend)

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

#### 1.3 Setup Apache JMeter
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

#### 1.4 Tối ưu JVM cho JMeter (Quan trọng cho Stress Test)
```bash
# Mở file jmeter.bat trong thư mục bin của JMeter
# Tìm dòng chứa "set HEAP" và sửa thành:
set HEAP=-Xms2g -Xmx4g -XX:MaxMetaspaceSize=256m -XX:+UseG1GC

# Giải thích:
# -Xms2g: Initial heap size 2GB
# -Xmx4g: Maximum heap size 4GB (adjust theo RAM máy bạn)
# -XX:MaxMetaspaceSize=256m: Metadata space limit
# -XX:+UseG1GC: Use G1 garbage collector (better for large heaps)
```

### 2. 🏃‍♂️ Hướng dẫn chạy Backend trước khi test

**⚠️ QUAN TRỌNG**: Backend phải chạy và stable trước khi thực hiện performance testing!

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
# Response: {"status":"UP"}

# Test API endpoint:
curl http://localhost:8080/api/health
# Hoặc mở browser: http://localhost:8080
```

### 3. ⚡ Hướng dẫn chạy Performance Testing (JMeter)

#### Bước 1: Build Java Utilities (Bắt buộc)
```bash
# Điều hướng đến thư mục performance
cd testing\performance\jmeter

# Build Java utilities (bắt buộc trước khi chạy JMeter)
mvn clean package -DskipTests

# Verify JAR files được tạo thành công:
dir target\modis-performance-tests-2.0.0-jar-with-dependencies.jar
# File size should be > 10MB (chứa all dependencies)
```

#### Bước 2: Chạy JMeter GUI Mode (chỉ để debug/tạo test)

**⚠️ Chú ý**: GUI mode chỉ dùng để tạo/edit test plans và debug với ít users (<50)

```bash
# Mở JMeter GUI
jmeter

# Trong JMeter GUI:
# 1. File > Open > Chọn file .jmx
# Ví dụ: test-plans\load-tests\modis-load-test.jmx

# 2. Config environment variables (nếu cần):
# Test Plan > User Defined Variables
# - BASE_URL: http://localhost:8080
# - USERS: 10 (cho debug)
# - RAMP_UP: 10
# - DURATION: 60

# 3. Chạy test bằng GUI (chỉ cho debug)
# Click nút Start (màu xanh lá)

# 4. Xem kết quả real-time:
# - View Results Tree: Chi tiết từng request/response
# - Summary Report: Tổng quan performance metrics
# - Aggregate Report: Detailed statistics
```

#### Bước 3: Chạy Non-GUI Mode (Production mode - Khuyến nghị)

**🔥 Quy tắc vàng**: KHÔNG BAO GIỜ dùng GUI mode cho production testing với >50 users

```bash
# Xóa reports cũ trước khi chạy
rmdir /s /q reports\load-test-html
rmdir /s /q reports\stress-test-html

# 3.1 Chạy Load Test (Normal traffic simulation)
jmeter -n -t test-plans\load-tests\modis-load-test.jmx -l results\load-test-results.jtl -e -o reports\load-test-html

# 3.2 Chạy Stress Test (Maximum capacity testing)
jmeter -n -t test-plans\stress-tests\modis-stress-test.jmx -l results\stress-test-results.jtl -e -o reports\stress-test-html

# 3.3 Chạy Spike Test (Sudden traffic spike)
jmeter -n -t test-plans\spike-tests\modis-spike-test.jmx -l results\spike-test-results.jtl -e -o reports\spike-test-html

# 3.4 Chạy Endurance Test (Long-running stability)
jmeter -n -t test-plans\endurance-tests\modis-endurance-test.jmx -l results\endurance-test-results.jtl -e -o reports\endurance-test-html

# 3.5 Chạy Image Upload Test (File upload performance)
jmeter -n -t test-plans\modis-image-upload-test.jmx -l results\image-upload-results.jtl -e -o reports\image-upload-html
```

**Giải thích parameters:**
- `-n`: Non-GUI mode (CLI mode)
- `-t`: Test plan file (.jmx)
- `-l`: Log file để lưu raw results (.jtl)
- `-e`: Generate HTML dashboard report
- `-o`: Output directory cho HTML report

#### Bước 4: Chạy với Custom Parameters

```bash
# Override số lượng users, ramp-up time, duration
jmeter -n -t test-plans\load-tests\modis-load-test.jmx -l results\custom-results.jtl -Jusers=500 -Jrampup=120 -Jduration=600 -e -o reports\custom-html

# Chạy với environment khác
jmeter -n -t test-plans\load-tests\modis-load-test.jmx -l results\staging-results.jtl -Jbase_url=https://staging.modis.com -e -o reports\staging-html

# Chạy với database connection khác
jmeter -n -t test-plans\load-tests\modis-load-test.jmx -l results\prod-results.jtl -Jdb_host=prod-db.modis.com -Jdb_port=3306 -e -o reports\prod-html

# Chạy với custom thread configuration
jmeter -n -t test-plans\stress-tests\modis-stress-test.jmx -l results\high-load.jtl -Jusers=1000 -Jrampup=300 -Jduration=1800 -e -o reports\high-load-html
```

#### Bước 5: Sử dụng Execution Scripts (Recommended)

```bash
# Chạy tất cả test types sequentially
run-all-tests.bat

# Chạy specific test type
run-load-test.bat
run-stress-test.bat
run-spike-test.bat
run-endurance-test.bat
run-image-upload-test.bat

# Chạy với custom parameters
run-load-test.bat 500 120 600  # users=500, rampup=120s, duration=600s
```

#### Bước 6: Generate HTML Report từ existing .jtl

```bash
# Nếu đã có file .jtl từ lần chạy trước
jmeter -g results\load-test-results.jtl -o reports\new-load-report

# Generate comparison report
jmeter -g results\baseline-results.jtl -o reports\baseline-html
jmeter -g results\current-results.jtl -o reports\current-html

# Sử dụng Java utility để compare
java -cp target\modis-performance-tests-2.0.0-jar-with-dependencies.jar com.modis.performance.utils.ResultsComparator results\baseline-results.jtl results\current-results.jtl
```

### 4. 📊 Xem Performance Reports

#### 4.1 Mở HTML Dashboard
```bash
# Mở main dashboard
start reports\load-test-html\index.html

# Hoặc specific report
start reports\stress-test-html\index.html
start reports\spike-test-html\index.html
```

#### 4.2 Các metrics quan trọng cần chú ý:

**Dashboard Overview:**
- **APDEX Score**: Application Performance Index (0.0-1.0, gần 1.0 là excellent)
- **Requests Summary**: Total requests, success rate, error rate
- **Statistics**: Response times, throughput, data transfer

**Key Metrics:**
- **Error %**: Phải < 1% (acceptable), < 0.1% (excellent)
- **Throughput**: Requests/second (càng cao càng tốt)
- **90th Percentile**: 90% users có response time ≤ giá trị này
- **95th Percentile**: 95% users có response time ≤ giá trị này
- **Average Response Time**: Thời gian phản hồi trung bình

**Charts quan trọng:**
- **Response Times Over Time**: Trend của response time
- **Active Threads Over Time**: Load pattern
- **Throughput Over Time**: Performance stability
- **Response Times Percentiles**: Distribution của response times

#### 4.3 Xem Raw Results
```bash
# Xem .jtl file (raw data)
type results\load-test-results.jtl | more

# Xem JMeter execution logs
type jmeter.log | more

# Xem specific metrics
findstr "ERROR" jmeter.log
findstr "WARN" jmeter.log
```

### 5. 🔄 Flow hoạt động Performance Framework

```text
1. Test Plan Initialization
   ├── Load .jmx file configuration
   ├── Initialize Java utilities JAR
   ├── Read test data từ CSV files
   └── Setup thread groups và ramp-up

2. Java Utilities Loading
   ├── AuthHelper: Generate JWT tokens
   ├── DataHelper: Create realistic test data
   ├── ConfigUtils: Load environment settings
   └── DatabaseUtils: Setup DB connections

3. Test Execution Phase
   ├── Thread ramp-up theo configured pattern
   ├── HTTP requests với realistic payloads
   ├── Real-time metrics collection
   ├── Response validation và assertions
   └── Error handling và retry logic

4. Results Collection
   ├── Raw data capture vào .jtl files
   ├── Real-time metrics aggregation
   ├── Custom listeners data collection
   └── Performance threshold monitoring

5. Report Generation
   ├── HTML dashboard creation
   ├── Interactive charts generation
   ├── Custom reports từ Java utilities
   └── Performance comparison analysis
```

### 6. 📈 Reports & Kết quả

#### 6.1 JMeter HTML Dashboard
- **Location**: `reports/{test-type}-html/index.html`
- **Content**: Interactive dashboard với charts, statistics, trends
- **Key Sections**:
  - Dashboard: Overview metrics và APDEX
  - Charts: Response times, throughput, active threads over time
  - Statistics: Detailed metrics table
  - Errors: Error analysis và details

#### 6.2 Raw Results (.jtl files)
- **Location**: `results/*.jtl`
- **Format**: CSV-like format với timestamp, response time, status, etc.
- **Usage**: Input cho custom analysis, comparison tools

#### 6.3 Custom Reports (Java-generated)
- **Comparison Reports**: Performance comparison giữa multiple runs
- **Trend Analysis**: Long-term performance trends
- **Custom Metrics**: Business-specific performance indicators

#### 6.4 Logs
- **JMeter Logs**: `jmeter.log` - JMeter execution logs
- **Application Logs**: Backend application logs during test
- **System Logs**: OS-level performance metrics (if monitored)

### 7. 🚨 Troubleshooting cực kỳ chi tiết

#### 7.1 Environment Issues
| Lỗi | Nguyên nhân | Cách fix |
|-----|-------------|----------|
| `java is not recognized` | Java chưa cài hoặc PATH sai | Install Java 17, set JAVA_HOME, add to PATH |
| `mvn is not recognized` | Maven chưa trong PATH | Add `%MAVEN_HOME%\bin` to System PATH |
| `jmeter is not recognized` | JMeter chưa trong PATH | Add JMeter bin directory to PATH |
| `JAVA_HOME is set to an invalid directory` | JAVA_HOME path incorrect | Set JAVA_HOME to JDK root directory (not bin) |

#### 7.2 JMeter Memory Issues
| Lỗi | Nguyên nhân | Cách fix |
|-----|-------------|----------|
| `OutOfMemoryError: Java heap space` | JMeter heap size quá nhỏ | Increase heap in jmeter.bat: `-Xms2g -Xmx4g` |
| `OutOfMemoryError: Metaspace` | Metaspace limit reached | Add `-XX:MaxMetaspaceSize=512m` |
| `GC overhead limit exceeded` | Too much time spent in GC | Use G1GC: `-XX:+UseG1GC` |
| JMeter becomes unresponsive | Memory pressure | Run in non-GUI mode, reduce thread count |

#### 7.3 Backend Connection Issues
| Lỗi | Nguyên nhân | Cách fix |
|-----|-------------|----------|
| `HttpHostConnectException` | Backend không chạy hoặc URL sai | Verify backend status, check BASE_URL |
| `Connection refused` | Port không available | Start backend, check port conflicts |
| `SocketTimeoutException` | Request timeout | Increase timeout settings, check network |
| `Connection reset` | Server overloaded | Reduce load, check server capacity |

#### 7.4 Test Execution Issues
| Lỗi | Nguyên nhân | Cách fix |
|-----|-------------|----------|
| `ClassNotFoundException` | Java utilities chưa build | Run `mvn clean package -DskipTests` |
| `FileNotFoundException` | Test data files missing | Verify CSV files exist in test-data/ |
| High error rate (>5%) | Server overload hoặc config sai | Reduce users, check server logs |
| Zero throughput | Test plan configuration issue | Verify thread group settings |

#### 7.5 Database Issues
| Lỗi | Nguyên nhân | Cách fix |
|-----|-------------|----------|
| `MySQL connection failed` | Database không available | Start MySQL service, check connection |
| `Too many connections` | Connection pool exhausted | Increase max connections, optimize queries |
| `Lock wait timeout exceeded` | Database deadlocks | Optimize queries, reduce concurrent writes |
| Slow query performance | Database not optimized | Add indexes, optimize queries |

### 8. 💡 Best Practices

#### 8.1 Test Design
- ✅ **Start với baseline test** để establish performance benchmark
- ✅ **Use realistic test data** từ production-like datasets
- ✅ **Implement proper ramp-up** để avoid sudden load spikes
- ✅ **Monitor system resources** during test execution
- ✅ **Test one variable at a time** để isolate performance factors
- ❌ **Không chạy GUI mode** với > 50 concurrent users
- ❌ **Không skip warm-up period** cho JVM-based applications

#### 8.2 Test Execution
- ✅ **Always run non-GUI mode** cho production tests
- ✅ **Clean up results** trước mỗi test run
- ✅ **Monitor backend logs** during test execution
- ✅ **Use distributed testing** cho very high loads
- ✅ **Validate test environment** trước khi chạy
- ❌ **Không chạy tests** trên shared/unstable environments
- ❌ **Không ignore error rates** > 1%

#### 8.3 Results Analysis
- ✅ **Focus on percentiles** (90th, 95th) hơn là averages
- ✅ **Compare với baseline** để identify regressions
- ✅ **Analyze trends over time** thay vì single point metrics
- ✅ **Correlate với system metrics** (CPU, memory, disk I/O)
- ❌ **Không rely solely on average** response times
- ❌ **Không ignore outliers** without investigation

### 9. 📋 Commands Cheatsheet

#### 9.1 Maven Commands
```bash
# Build Java utilities
mvn clean package -DskipTests

# Clean build artifacts
mvn clean

# Install dependencies
mvn install -DskipTests

# Run with specific profile
mvn package -Pproduction -DskipTests

# Generate dependency report
mvn dependency:tree
```

#### 9.2 JMeter Commands
```bash
# Basic non-GUI execution
jmeter -n -t test.jmx -l results.jtl -e -o reports

# With custom parameters
jmeter -n -t test.jmx -l results.jtl -Jusers=100 -Jduration=300

# Generate report from existing .jtl
jmeter -g results.jtl -o html-report

# Check JMeter version và plugins
jmeter -v
jmeter -?

# Run distributed testing
jmeter -n -t test.jmx -R server1,server2 -l results.jtl

# Server mode (for distributed testing)
jmeter-server -Djava.rmi.server.hostname=192.168.1.100
```

#### 9.3 Results Analysis Commands
```bash
# View .jtl file content
type results\load-test-results.jtl | more

# Count total requests
findstr /c:"," results\load-test-results.jtl

# Count errors
findstr "false" results\load-test-results.jtl | find /c ","

# Extract response times
for /f "tokens=2 delims=," %i in (results\load-test-results.jtl) do echo %i

# Custom analysis với Java utility
java -cp target\modis-performance-tests-2.0.0-jar-with-dependencies.jar com.modis.performance.utils.ResultsComparator baseline.jtl current.jtl
```

#### 9.4 System Monitoring Commands
```bash
# Monitor system resources during test
# CPU usage
wmic cpu get loadpercentage /value

# Memory usage
wmic OS get TotalVisibleMemorySize,FreePhysicalMemory /value

# Network connections
netstat -an | findstr :8080

# Process monitoring
tasklist | findstr java

# Disk I/O monitoring
wmic logicaldisk get size,freespace,caption
```

#### 9.5 Cleanup Commands
```bash
# Clean all results
del /f /q results\*.jtl
rmdir /s /q reports\*-html

# Clean build artifacts
mvn clean
rmdir /s /q target

# Clean logs
del /f /q jmeter.log
del /f /q *.log

# Archive results
powershell Compress-Archive -Path results\*.jtl -DestinationPath results-backup-%date%.zip
powershell Compress-Archive -Path reports\ -DestinationPath reports-backup-%date%.zip
```

### Bước 1: Điều kiện tiên quyết
*   Môi trường phải có **Java 17** (Bắt buộc vì code sử dụng Java 15+ features).
*   Cài đặt **Apache JMeter 5.6.3** và đã Add Path `jmeter/bin`.
*   Cài đặt **Apache Maven 3.8+**.

### Bước 2: Biên Dịch Lõi Java (Bắt Buộc Trước Mỗi Lần Chạy)
Do script test (`.jmx`) tham chiếu trực tiếp đến các file Java do team viết, mỗi khi bạn checkout code mới hoặc sửa code Java, bạn phải build lại bằng Maven:
```bash
cd testing\performance\jmeter
mvn clean package -DskipTests
```
Lệnh này tải các dependencies (Jackson JSON, Commons CSV...) và đóng gói mọi thứ thành 1 file:
`target/modis-performance-tests-2.0.0-jar-with-dependencies.jar`.
JMeter Plugin Maven đã được cấu hình để tự nạp class từ file này.

### Bước 3: Tối Ưu JVM (Bắt Buộc Nếu Chạy > 1000 Users)
Sửa file `jmeter.bat` (Window) trong thư mục cài JMeter:
```bat
# Tìm dòng HEAP và đổi thành
set HEAP=-Xms2g -Xmx2g -XX:MaxMetaspaceSize=256m -XX:+UseG1GC
```
*Ghi chú: `-XX:+UseG1GC` bật bộ dọn rác thế hệ mới giúp giảm thời gian Pause-time của JVM.*

---

## 4. Thực Thi Kịch Bản (Execution Guide)

⚠️ **CHÚ Ý:** GUI Mode (Giao diện phần mềm) chỉ dành cho việc tạo kịch bản (Edit .jmx) và kiểm tra lỗi logic (Debug 1-2 Users).

### 4.1. Chuẩn Bị Workspace
Trước khi chạy test, luôn phải dọn dẹp kết quả cũ:
```bash
del /F /Q results\*.jtl
rmdir /S /Q reports\load-test-html
```

### 4.2. Chạy Lệnh Cốt Lõi (Non-GUI / CLI Mode)
```bash
jmeter -n -t test-plans\load-tests\modis-load-test.jmx -l results\run_2026.jtl -e -o reports\load-test-html
```

**Bóc tách ý nghĩa câu lệnh:**
| Tham số | Ý nghĩa |
| :--- | :--- |
| `-n` | Ép JMeter chạy chế độ Dòng lệnh (Không khởi động đồ họa). Tiết kiệm đến 90% RAM. |
| `-t <file>` | `Test Plan`: Đường dẫn tới kịch bản test `.jmx` bạn muốn chạy. |
| `-l <file>` | `Log`: Nơi lưu dữ liệu từng Request phản hồi (Tên file phải là `.jtl` hoặc `.csv`). |
| `-e` | `Export`: Ra lệnh cho JMeter biên dịch dữ liệu thô sang Dashboard trực quan. |
| `-o <dir>` | `Output`: Thư mục trống để JMeter nhả các file HTML, JS, CSS của Dashboard vào. |

### 4.3. Override Biến (Inject Parameter)
Nếu không muốn sửa file `.jmx`, bạn có thể can thiệp số lượng User từ dòng lệnh thông qua cờ `-J`:
```bash
jmeter -n -t test-plans\modis-load-test.jmx -l results\run.jtl -Jusers=500 -Jrampup=60 -Jduration=300
```
Lệnh trên tiêm vào kịch bản: `500 Users`, bật dần lên trong `60 giây`, chạy liên tục trong `300 giây (5 phút)`.

---

## 5. Phân Tích Dữ Liệu (The Reporting Phase)

### 5.1 Đọc Dashboard Cơ Bản
Mở file `reports/load-test-html/index.html`. 
1.  **Dashboard > APDEX:** Chỉ số hài lòng của hệ thống. Nếu Toleration Threshold là `1500ms`, bất kỳ request nào trên mức đó sẽ làm Apdex tụt khỏi 1.0 (Xanh nhạt/Vàng/Đỏ).
2.  **Dashboard > Statistics:** Đây là bảng quan trọng nhất.
    *   **Throughput (TPS):** Số giao dịch 1 giây. Đây là sức mạnh thực sự của Server.
    *   **Error %:** Chữ số đỏ. Mọi con số > 0.5% đều đáng lo ngại.
    *   **90th pct:** 90% lượng người dùng có thời gian tải trang thấp hơn hoặc bằng con số này. Nó là thước đo chính xác hơn Average (Trung bình).

### 5.2. Tool So Sánh (Java Custom Util)
Thay vì so sánh bằng mắt, Modis cung cấp sẵn Tool tự động phân tích độ chênh lệch (Regression/Improvement) giữa 2 lần chạy.
```bash
java -cp target/modis-performance-tests-2.0.0-jar-with-dependencies.jar com.modis.performance.utils.ResultsComparator
```
*Hệ thống sẽ dựa vào JTLResultsParser để bóc tách 2 file JTL và in ra màn hình hoặc xuất JSON báo cáo phần trăm thay đổi của Throughput & Response Time.*

---

## 6. Sổ Tay Chẩn Đoán (Troubleshooting Guide)

| Hiện Tượng | Nguyên Nhân | Giải Pháp Tức Thời |
| :--- | :--- | :--- |
| Báo lỗi ClassNotFoundException trên JMeter | Chưa build mã nguồn Java | Chạy lệnh `mvn clean package -DskipTests` |
| Báo `OutOfMemory` ngay giữa chừng | RAM cấu hình trong JMeter bị cạn | Tăng `set HEAP` lên `-Xms4g -Xmx4g`, đảm bảo chạy `-n` (Non-GUI) |
| Throughput bị chững, không tăng dù số Users tăng | Server Backend (Database/Thread Pool) đạt giới hạn | Phân tích logs Database, tăng Max Connections của HikariCP Backend. |
| `java.net.SocketException: Connection reset` | Quá tải đường truyền hoặc Firewall ngắt kết nối | Phân tán tải bằng cơ chế JMeter Distributed Testing (Master-Slave) |

## Tôn Chỉ (Mantra)
> "Không ai đoán được hệ thống chịu được bao nhiêu User, cho đến khi nó thực sự sập. Hãy chạy Stress Test một cách có trách nhiệm."
## 🚀 Hướng dẫn chạy đầy đủ

### 1. 🛠️ Setup môi trường

#### 1.1 Cài đặt Java 17 (Bắt buộc)
```bash
# Tải Java 17 từ: https://adoptium.net/temurin/releases/
# Chọn: OpenJDK 17 LTS > Windows > x64 > .msi

# Thiết lập biến môi trường:
# JAVA_HOME = C:\Program Files\Eclipse Adoptium\jdk-17.0.x.x-hotspot
# Thêm %JAVA_HOME%\bin vào System PATH

# Verify installation (PHẢI là Java 17)
java -version
# Expected: openjdk version "17.0.x"
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

#### 1.3 Setup Apache JMeter
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

#### 1.4 Tối ưu JVM cho JMeter (Quan trọng)
```bash
# Mở file jmeter.bat trong thư mục bin của JMeter
# Tìm dòng chứa "set HEAP" và sửa thành:
set HEAP=-Xms2g -Xmx4g -XX:MaxMetaspaceSize=256m -XX:+UseG1GC

# Giải thích:
# -Xms2g: Initial heap size 2GB
# -Xmx4g: Maximum heap size 4GB (adjust theo RAM máy bạn)
# -XX:MaxMetaspaceSize=256m: Metadata space limit
# -XX:+UseG1GC: Use G1 garbage collector (better for large heaps)
```

### 2. 🌐 Kiểm tra Backend API (Render.com)

**⚠️ QUAN TRỌNG**: Framework test với deployed backend trên Render.com!

```bash
# Backend API đang chạy tại (theo config thực tế):
# https://modis-backend.onrender.com

# Kiểm tra API health:
curl https://modis-backend.onrender.com/actuator/health
# Expected response: {"status":"UP"}

# Test API endpoints:
curl https://modis-backend.onrender.com/api/auth/health
# Hoặc mở browser: https://modis-backend.onrender.com

# ⚠️ Note: Render free tier có cold start ~30-90s nếu không có traffic
# Timeout trong config đã được set 90s để handle cold start
```

**Backend characteristics (Render.com):**
- **Cold Start**: 30-90 seconds cho first request
- **Timeout**: 90 seconds (theo modis-config.properties)
- **Rate Limiting**: Có thể có limits trên free tier
- **Database**: MongoDB Atlas (cloud)
- **Cache**: Redis Cloud

### 3. ⚡ Build Java Utilities (Bắt buộc)

#### Bước 1: Switch to Java 17
```bash
# Điều hướng đến thư mục performance
cd testing\performance\jmeter

# Đảm bảo JAVA_HOME trỏ đến Java 17
echo %JAVA_HOME%
java -version
# Expected: openjdk version "17.0.x"
```

#### Bước 2: Build Java Utilities
```bash
# Build Java utilities (bắt buộc trước khi chạy JMeter)
mvn clean package -DskipTests

# Verify JAR files được tạo thành công:
dir target\modis-performance-tests-2.0.0-jar-with-dependencies.jar
# File size should be > 5MB (chứa all dependencies)

# Verify build success:
# [INFO] BUILD SUCCESS
# [INFO] Total time: XX.XXX s
```

### 4. 🎯 Chạy Performance Tests

#### 4.1 Sử dụng Execution Scripts (Khuyến nghị)

**Load Test (Normal traffic):**
```bash
# Chạy với default parameters (50 users, 5min)
run-load-test.bat

# Chạy với custom parameters (users, ramp-up, duration)
run-load-test.bat 100 300 600
# 100 users, 300s ramp-up, 600s duration
```

**Stress Test (Maximum capacity):**
```bash
# Chạy stress test với default parameters (200 users)
run-stress-test.bat

# Chạy với custom parameters
run-stress-test.bat 300 180 900
# 300 users, 180s ramp-up, 900s duration
```

**Spike Test (Traffic spike):**
```bash
# Chạy spike test (500 users, 1min ramp-up)
run-spike-test.bat

# Custom spike test
run-spike-test.bat 1000 60 300
# 1000 users, 60s ramp-up, 300s duration
```

**Endurance Test (Long-running):**
```bash
# Chạy endurance test (30 users, 2 hours)
run-endurance-test.bat

# Custom endurance test
run-endurance-test.bat 50 600 7200
# 50 users, 600s ramp-up, 7200s (2h) duration
```

**Image Upload Test:**
```bash
# Test image upload performance
run-image-upload-test.bat

# All tests sequentially
run-all-tests.bat
```

#### 4.2 Chạy trực tiếp với JMeter CLI

**Basic JMeter commands:**
```bash
# Load Test (theo config thực tế)
jmeter -n -t test-plans\load-tests\modis-load-test.jmx -l results\load-test-results.jtl -e -o reports\load-test-html

# Stress Test
jmeter -n -t test-plans\stress-tests\modis-stress-test.jmx -l results\stress-test-results.jtl -e -o reports\stress-test-html

# Spike Test
jmeter -n -t test-plans\spike-tests\modis-spike-test.jmx -l results\spike-test-results.jtl -e -o reports\spike-test-html
```

**Với custom parameters (theo modis-config.properties):**
```bash
# Override default parameters
jmeter -n -t test-plans\load-tests\modis-load-test.jmx -l results\custom-results.jtl -Jusers=100 -Jramp_up=300 -Jduration=600 -e -o reports\custom-html

# Override base URL (nếu cần test staging)
jmeter -n -t test-plans\load-tests\modis-load-test.jmx -l results\staging-results.jtl -Jbase_url=https://staging-modis.onrender.com -e -o reports\staging-html
```

#### 4.3 Chạy với Maven Profiles

```bash
# Load test profile
mvn test -Pload-test

# Stress test profile
mvn test -Pstress-test

# Spike test profile
mvn test -Pspike-test

# Endurance test profile
mvn test -Pendurance-test
```

### 5. 📊 Xem Performance Reports

#### 5.1 HTML Dashboard
```bash
# Mở main dashboard
start reports\load-test-html\index.html

# Hoặc specific report
start reports\stress-test-html\index.html
start reports\spike-test-html\index.html
```

#### 5.2 Key Metrics cần chú ý (theo Render.com thực tế)

**Response Time Thresholds** (từ modis-config.properties):
- **Authentication**: < 2000ms
- **Feed API**: < 3000ms  
- **Image Upload**: < 10000ms
- **Messaging**: < 500ms
- **General API**: < 1000ms

**Performance Indicators:**
- **Error Rate**: < 1% (acceptable), < 0.1% (excellent)
- **Cold Start**: First request có thể mất 30-90s (Render free tier)
- **Throughput**: Depends on Render.com limits
- **APDEX Score**: > 0.8 (good), > 0.9 (excellent)

**Charts quan trọng:**
- **Response Times Over Time**: Trend của response time
- **Active Threads Over Time**: Load pattern
- **Throughput Over Time**: Performance stability
- **Error Rate Over Time**: Error distribution

#### 5.3 Custom Analysis với Java Utilities

```bash
# So sánh performance giữa 2 runs
java -cp target\modis-performance-tests-2.0.0-jar-with-dependencies.jar com.modis.performance.utils.ResultsComparator results\baseline.jtl results\current.jtl

# Generate custom reports
java -cp target\modis-performance-tests-2.0.0-jar-with-dependencies.jar com.modis.performance.reports.ReportGenerator results\load-test-results.jtl

# Performance comparison
java -cp target\modis-performance-tests-2.0.0-jar-with-dependencies.jar com.modis.performance.comparator.PerformanceComparator
```

### 6. 🔄 Flow hoạt động thực tế

#### 6.1 Performance Testing Flow
```text
1. JMeter CLI mode (non-GUI) ← production best practice
2. Java 17 utilities loading ← theo pom.xml thực tế
3. Target API: https://modis-backend.onrender.com ← theo config
4. Test execution với realistic data ← CSV files
5. Cold start handling (90s timeout) ← Render.com specific
6. Results collection (.jtl files) ← raw performance data
7. HTML dashboard generation ← interactive reports
8. Custom analysis với Java utilities ← advanced metrics
```

#### 6.2 Test Types Flow
```text
Load Test (50 users, 10min):
├── Ramp-up: 5 minutes
├── Steady state: 5 minutes
├── Target: Normal usage simulation
└── Metrics: Baseline performance

Stress Test (200 users, 15min):
├── Ramp-up: 3 minutes
├── Steady state: 12 minutes
├── Target: Find breaking point
└── Metrics: Maximum capacity

Spike Test (500 users, 5min):
├── Ramp-up: 1 minute
├── Peak load: 4 minutes
├── Target: Traffic spike simulation
└── Metrics: System resilience

Endurance Test (30 users, 2h):
├── Ramp-up: 10 minutes
├── Steady state: 110 minutes
├── Target: Memory leaks, stability
└── Metrics: Long-term performance
```

### 7. 🚨 Troubleshooting theo Render.com

#### 7.1 Environment Issues
| Lỗi | Nguyên nhân | Cách fix |
|-----|-------------|----------|
| `Wrong Java version` | Cần Java 17, không phải Java 11 | Set JAVA_HOME đến Java 17 installation |
| `mvn is not recognized` | Maven chưa trong PATH | Add `%MAVEN_HOME%\bin` to System PATH |
| `jmeter is not recognized` | JMeter chưa trong PATH | Add JMeter bin directory to PATH |
| `Build failure` | Dependencies issue | Run `mvn clean package -DskipTests` |

#### 7.2 JMeter Memory Issues
| Lỗi | Nguyên nhân | Cách fix |
|-----|-------------|----------|
| `OutOfMemoryError: Java heap space` | JMeter heap size quá nhỏ | Increase heap: `-Xms2g -Xmx4g` |
| `OutOfMemoryError: Metaspace` | Metaspace limit reached | Add `-XX:MaxMetaspaceSize=512m` |
| `GC overhead limit exceeded` | Too much time in GC | Use G1GC: `-XX:+UseG1GC` |
| JMeter becomes unresponsive | Memory pressure | Run non-GUI mode, reduce thread count |

#### 7.3 Render.com Specific Issues
| Lỗi | Nguyên nhân | Cách fix |
|-----|-------------|----------|
| `Connection timeout (90s)` | Render cold start | Wait và retry, normal behavior |
| `502 Bad Gateway` | Render service down | Check Render dashboard, wait for recovery |
| `Rate limiting errors` | Too many requests | Implement delays, reduce concurrent users |
| `High response times` | Render resource limits | Reduce load, check Render metrics |

#### 7.4 Test Execution Issues
| Lỗi | Nguyên nhân | Cách fix |
|-----|-------------|----------|
| `ClassNotFoundException` | Java utilities chưa build | Run `mvn clean package -DskipTests` |
| `FileNotFoundException` | Test data files missing | Verify CSV files exist in test-data/ |
| High error rate (>5%) | Render overload hoặc config sai | Reduce users, check Render logs |
| Zero throughput | Test plan configuration issue | Verify .jmx file và parameters |

### 8. 💡 Best Practices theo Render.com

#### 8.1 Test Design
- ✅ **Handle cold start delays** với 90s timeout
- ✅ **Use realistic ramp-up** để avoid overwhelming Render
- ✅ **Monitor Render metrics** during test execution
- ✅ **Test with realistic data** từ CSV files
- ✅ **Implement proper delays** giữa requests
- ❌ **Không ignore cold start** của Render free tier
- ❌ **Không chạy quá nhiều concurrent users** với free tier

#### 8.2 Test Execution
- ✅ **Always run non-GUI mode** cho production tests
- ✅ **Clean up results** trước mỗi test run
- ✅ **Monitor backend logs** during test execution
- ✅ **Use Java utilities** thay vì Groovy scripts
- ✅ **Validate test environment** trước khi chạy
- ❌ **Không chạy tests** khi Render đang deploy
- ❌ **Không ignore error rates** > 1%

#### 8.3 Results Analysis
- ✅ **Focus on percentiles** (90th, 95th) hơn là averages
- ✅ **Account for cold start** trong analysis
- ✅ **Compare với baseline** để identify regressions
- ✅ **Use custom Java tools** cho advanced analysis
- ❌ **Không rely solely on average** response times
- ❌ **Không ignore Render-specific** performance characteristics

### 9. 📋 Commands Cheatsheet

#### 9.1 Environment Commands
```bash
# Java version check
java -version
echo %JAVA_HOME%

# Maven commands
mvn -version
mvn clean package -DskipTests
mvn clean compile

# JMeter commands
jmeter -v
jmeter -?
```

#### 9.2 Test Execution Commands
```bash
# Build utilities
mvn clean package -DskipTests

# Run tests với scripts
run-load-test.bat 50 300 600
run-stress-test.bat 200 180 900
run-spike-test.bat 500 60 300

# Run tests với JMeter CLI
jmeter -n -t test-plans\load-tests\modis-load-test.jmx -l results\test.jtl -e -o reports\html
```

#### 9.3 Analysis Commands
```bash
# Custom analysis
java -cp target\modis-performance-tests-2.0.0-jar-with-dependencies.jar com.modis.performance.utils.ResultsComparator baseline.jtl current.jtl

# Generate reports
jmeter -g results\test.jtl -o reports\html-report

# View results
start reports\load-test-html\index.html
```

#### 9.4 Backend Health Commands
```bash
# Check backend health
curl https://modis-backend.onrender.com/actuator/health

# Test API endpoints
curl https://modis-backend.onrender.com/api/auth/health

# Performance check với timing
curl -w "@curl-format.txt" -o /dev/null -s https://modis-backend.onrender.com
```

---

## 🎯 Kết luận

**Modis Performance Framework** là một framework production-ready với:

- **Java 17** + **JMeter 5.6.2** + **Custom utilities**
- **Render.com optimized** với cold start handling
- **Multiple test types** (Load, Stress, Spike, Endurance, Image Upload)
- **Realistic test data** từ CSV files
- **Advanced analysis** với custom Java tools
- **Production backend** integration (https://modis-backend.onrender.com)
- **Comprehensive reporting** (HTML dashboard + custom reports)

Framework này được thiết kế đặc biệt cho Render.com deployment và có thể sử dụng ngay để test Modis backend performance.