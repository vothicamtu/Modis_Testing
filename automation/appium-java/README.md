# Modis Appium Java Automation Testing

Tài liệu hướng dẫn cài đặt, chạy test và xem kết quả kiểm thử tự động cho ứng dụng Android Modis. Bộ test được viết bằng Java, Appium, TestNG và tổ chức theo mô hình Page Object Model (POM).

## 1. Thông tin nhanh

| Hạng mục | Nội dung |
| --- | --- |
| Nền tảng kiểm thử | Android |
| Ngôn ngữ | Java 11 |
| Build tool | Maven |
| Automation tool | Appium Java Client 9.3.0 |
| Test runner | TestNG 7.8.0 |
| Báo cáo | TestNG HTML, ExtentReports, Allure results |
| Ảnh minh họa | `screenshots/`, `target/screenshots/` |
| Log | `logs/` |

## 2. Cấu trúc thư mục

```text
testing/automation/appium-java/
├── src/main/java/com/modis/
│   ├── base/BasePage.java
│   ├── constants/AppConstants.java
│   ├── constants/TestIDs.java
│   ├── drivers/DriverManager.java
│   ├── listeners/TestListener.java
│   ├── pages/
│   │   ├── AllImagesPage.java
│   │   ├── ConversationPage.java
│   │   ├── FriendsPage.java
│   │   ├── HomePage.java
│   │   ├── LoadingPage.java
│   │   ├── LoginPage.java
│   │   ├── MessagePage.java
│   │   ├── ProfilePage.java
│   │   ├── SendPhotoPage.java
│   │   ├── SignupPage.java
│   │   └── TakePage.java
│   └── utils/
├── src/test/java/com/modis/
│   ├── base/BaseTest.java
│   └── tests/
│       ├── AuthenticationTests.java
│       ├── FeedTests.java
│       ├── FriendsTests.java
│       ├── MessagingTests.java
│       ├── PhotoSharingTests.java
│       ├── SearchTests.java
│       └── TestSessionHelper.java
├── src/test/resources/
│   ├── config/
│   ├── testdata/
│   └── testsuites/modis-real-data-tests.xml
├── reports/
├── screenshots/
├── logs/
├── pom.xml
├── testng.xml
├── run-tests.bat
├── run-real-data-tests.bat
└── start-appium-server.bat
```

## 3. Test scope hiện có

| Module | File test | Nội dung chính |
| --- | --- | --- |
| Authentication | `AuthenticationTests.java` | Loading, login hợp lệ/không hợp lệ, validation, signup, logout |
| Feed | `FeedTests.java` | Xem feed, refresh, scroll, mở bài viết/ảnh, reaction, comment ảnh |
| Friends | `FriendsTests.java` | Danh sách bạn bè, request, accept/decline, sent request, search, unfriend |
| Messaging | `MessagingTests.java` | Danh sách hội thoại, mở chat, gửi tin nhắn, input validation, dữ liệu thật |
| Photo Sharing | `PhotoSharingTests.java` | Mở camera, chụp ảnh, chọn người nhận, gửi ảnh, caption |
| Search | `SearchTests.java` | Tìm user, không có kết quả, input rỗng/ngắn/dài/ký tự đặc biệt |

## 4. Cài đặt môi trường

### 4.1 Công cụ cần có

- JDK 11 hoặc JDK 17, project compile target Java 11.
- Maven 3.8 trở lên.
- Node.js LTS.
- Android Studio, Android SDK Platform Tools, Android Emulator.
- Appium Server 2.x.
- Appium UiAutomator2 driver.

### 4.2 Biến môi trường trên Windows

Thêm các biến sau vào System Environment Variables:

```text
JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.x.x
MAVEN_HOME=C:\apache-maven-3.9.x
ANDROID_HOME=C:\Users\<user>\AppData\Local\Android\Sdk
```

Thêm vào `Path`:

```text
%JAVA_HOME%\bin
%MAVEN_HOME%\bin
%ANDROID_HOME%\platform-tools
%ANDROID_HOME%\emulator
%ANDROID_HOME%\cmdline-tools\latest\bin
```

Kiểm tra:

```bash
java -version
mvn -version
adb devices
```

### 4.3 Cài Appium

```bash
npm install -g appium
appium driver install uiautomator2
appium driver list --installed
```

## 5. Chuẩn bị trước khi chạy test

1. Build APK Android của app Modis hoặc đảm bảo app đã cài sẵn trên emulator/thiết bị thật.
2. Mở emulator hoặc kết nối điện thoại Android đã bật USB debugging.
3. Kiểm tra thiết bị:

```bash
adb devices
```

4. Khởi động Appium server:

```bash
cd C:\DATLTN\testing\automation\appium-java
start-appium-server.bat
```

Hoặc chạy thủ công:

```bash
appium server --port 4723 --address 127.0.0.1
```

Kiểm tra server:

```bash
curl http://127.0.0.1:4723/status
```

## 6. Chạy test

Di chuyển vào thư mục automation:

```bash
cd C:\DATLTN\testing\automation\appium-java
```

Biên dịch code:

```bash
mvn test-compile
```

Chạy toàn bộ suite theo `testng.xml`:

```bash
mvn test -DsuiteXmlFile=testng.xml
```

Chạy theo group:

```bash
mvn test -Dgroups=smoke
mvn test -Dgroups=regression
mvn test -Dgroups=authentication
mvn test -Dgroups=friends
mvn test -Dgroups=photo-sharing
mvn test -Dgroups=feed
mvn test -Dgroups=search
mvn test -Dgroups=messaging
```

Chạy từng class:

```bash
mvn -Dtest=AuthenticationTests test
mvn -Dtest=FeedTests test
mvn -Dtest=FriendsTests test
mvn -Dtest=MessagingTests test
mvn -Dtest=PhotoSharingTests test
mvn -Dtest=SearchTests test
```

Chạy bằng script Windows:

```bash
run-tests.bat --suite smoke
run-tests.bat --suite regression
run-real-data-tests.bat
```

## 7. Xem báo cáo, ảnh và log

Sau khi chạy test, kiểm tra các thư mục sau:

| Loại kết quả | Đường dẫn |
| --- | --- |
| TestNG report | `target/surefire-reports/index.html` |
| TestNG emailable report | `target/surefire-reports/emailable-report.html` |
| ExtentReports | `reports/ModisReport_*.html` |
| Allure raw results | `target/allure-results/` |
| Screenshots | `screenshots/`, `target/screenshots/` |
| Logs | `logs/` |

Nếu muốn tạo Allure HTML report:

```bash
mvn allure:report
```

Report sẽ nằm ở:

```text
target/allure-report/index.html
```

## 8. GitHub Actions

Project có workflow CI tại:

```text
.github/workflows/mobile-tests.yml
```

Khi đưa source lên GitHub, workflow này có thể dùng để build/chạy mobile test và upload report artifact. Cần cấu hình runner/emulator phù hợp trước khi dùng cho môi trường thật.

## 9. Troubleshooting

| Lỗi | Cách xử lý |
| --- | --- |
| `adb is not recognized` | Kiểm tra `ANDROID_HOME` và thêm `%ANDROID_HOME%\platform-tools` vào `Path` |
| `No connected devices` | Mở emulator, cắm lại thiết bị, chạy `adb kill-server` rồi `adb start-server` |
| `Connection refused` tới Appium | Khởi động Appium server và kiểm tra port `4723` |
| Không tạo được session | Kiểm tra app package/activity, Android version và UiAutomator2 driver |
| Test fail do element chưa xuất hiện | Kiểm tra `testID` trong app React Native và wait trong Page Object |

## 10. Checklist bàn giao automation

| Yêu cầu | Trạng thái trong thư mục này |
| --- | --- |
| Script kiểm thử tự động Java theo POM | Đã có trong `src/main/java/com/modis` và `src/test/java/com/modis` |
| Source Appium Java | Đã có, có thể đưa lên GitHub |
| HTML report có log và ảnh | Đã có `reports/`, `target/surefire-reports/`, `screenshots/`, `logs/` |
| Hướng dẫn cài đặt/chạy test | README này đã cập nhật |
| Video demo | Chưa thấy file video trong thư mục này; cần bổ sung file quay bằng OBS/Appium Inspector |

