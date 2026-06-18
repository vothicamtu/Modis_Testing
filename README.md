# Modis Testing

## Tổng quan dự án

`testing` chứa source code kiểm thử cho Modis, gồm hai phần:

- `automation/appium-java`: automation test mobile bằng Java, Appium và TestNG.
- `performance/jmeter`: performance test backend bằng Apache JMeter và Java utilities.

## Công nghệ sử dụng

- Java 11 cho Appium automation.
- Maven.
- Appium Java Client 9.3.0.
- Selenium 4.22.0.
- TestNG 7.8.0.
- ExtentReports và Allure TestNG.
- Java 17 cho JMeter performance utilities.
- Apache JMeter 5.6.2 qua Maven dependency/plugin.
- Jackson, Commons CSV, Commons Math, Logback.

## Kiến trúc

- Automation module dùng Page Object Model.
- Performance module tách JMeter `.jmx` test plans, CSV test data và Java utilities để parse/compare kết quả `.jtl`.
- Không có application runtime trong thư mục `testing`; đây là workspace test riêng.

## Cấu trúc thư mục

```text
testing/
|-- automation/
|   `-- appium-java/
|       |-- src/main/java/com/modis/
|       |-- src/test/java/com/modis/
|       |-- src/test/resources/
|       |-- pom.xml
|       `-- testng.xml
`-- performance/
    `-- jmeter/
        |-- configs/
        |-- scripts/
        |-- src/main/java/com/modis/performance/
        |-- test-data/
        |-- test-plans/
        `-- pom.xml
```

## Cài đặt

Automation:

```bash
cd C:\DATLTN\testing\automation\appium-java
mvn test-compile
```

Performance:

```bash
cd C:\DATLTN\testing\performance\jmeter
mvn clean package -DskipTests
```

## Hướng dẫn chạy và sử dụng

Chạy automation suite:

```bash
cd C:\DATLTN\testing\automation\appium-java
mvn test
```

Chạy performance load test qua Maven profile:

```bash
cd C:\DATLTN\testing\performance\jmeter
mvn test -Pload-test
```

Chạy script JMeter trên Windows:

```bash
cd C:\DATLTN\testing\performance\jmeter
run-load-test.bat
```

Sử dụng kết quả:

- Automation tạo report trong `automation/appium-java/reports`, `target/surefire-reports`, `target/allure-results`, `screenshots` và `logs`.
- Performance tạo file `.jtl` trong `performance/jmeter/results` và HTML report trong `performance/jmeter/reports`.

## Biến môi trường và cấu hình

Không có `.env` trong testing module. Cấu hình test nằm trong các file:

```text
automation/appium-java/src/test/resources/config/test.properties
automation/appium-java/src/test/resources/config/android.properties
automation/appium-java/src/test/resources/config/ios.properties
performance/jmeter/configs/modis-config.properties
```

## API Documentation

Testing module không tạo API. Performance tests đang trỏ tới backend URL trong:

```text
performance/jmeter/configs/modis-config.properties
```

Giá trị hiện tại:

```text
https://modis-backend.onrender.com
```

## Dependency quan trọng

Dependency được khai báo trong hai file Maven:

- `automation/appium-java/pom.xml`
- `performance/jmeter/pom.xml`

## Scripts

Automation:

```bash
run-tests.bat
run-tests.sh
run-real-data-tests.bat
run-real-data-tests.sh
```

Performance:

```bash
run-tests.bat
run-tests.sh
run-load-test.bat
run-load-test.sh
run-stress-test.bat
run-stress-test.sh
run-spike-test.bat
run-endurance-test.bat
run-image-upload-test.bat
run-all-tests.bat
run-all-tests.sh
```

## Giải thích số liệu và cấu hình quan trọng

- Automation compile target là Java 11 theo `automation/appium-java/pom.xml`.
- Performance compile target là Java 17 theo `performance/jmeter/pom.xml`.
- Appium server mặc định là `http://127.0.0.1:4723` theo `test.properties`.
- Android package test là `com.modis`, activity là `com.modis.MainActivity`.
- JMeter target backend là `https://modis-backend.onrender.com`.
- Load test mặc định: 50 users, ramp-up 300 giây, duration 600 giây.
- Stress test mặc định: 200 users, ramp-up 180 giây, duration 900 giây.
- Spike test mặc định: 500 users, ramp-up 60 giây, duration 300 giây.
- Endurance test mặc định: 30 users, ramp-up 600 giây, duration 3600 giây.
