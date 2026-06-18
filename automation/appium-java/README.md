# Modis Appium Java Automation

## Tổng quan dự án

Module này chứa automation tests cho mobile app Modis bằng Java, Appium và TestNG. Source code được tổ chức theo Page Object Model và có test cho:

- Authentication.
- Feed.
- Friends.
- Messaging.
- Photo sharing.
- Search.

## Công nghệ sử dụng

- Java 11.
- Maven.
- Appium Java Client 9.3.0.
- Selenium 4.22.0.
- TestNG 7.8.0.
- ExtentReports 5.1.1.
- Allure TestNG 2.24.0.
- Jackson Databind/YAML.
- Logback.
- AssertJ.
- WebDriverManager.
- JavaFaker.
- JsonPath.
- OkHttp.
- Awaitility.

## Kiến trúc

- Page Object Model trong `src/main/java/com/modis/pages`.
- Base page trong `src/main/java/com/modis/base/BasePage.java`.
- Driver/session management trong `drivers/DriverManager.java` và `src/test/java/com/modis/base/BaseTest.java`.
- Test listener trong `listeners/TestListener.java`.
- Test data JSON trong `src/test/resources/testdata`.
- Cấu hình platform và Appium capabilities trong `src/test/resources/config`.

## Cấu trúc thư mục

```text
automation/appium-java/
|-- src/
|   |-- main/java/com/modis/
|   |   |-- base/        BasePage
|   |   |-- constants/   App constants và testIDs
|   |   |-- drivers/     DriverManager
|   |   |-- listeners/   TestNG listener/report hook
|   |   |-- pages/       Page Object classes
|   |   `-- utils/       Config, wait, gesture, screenshot, logger, API helpers
|   `-- test/
|       |-- java/com/modis/
|       |   |-- base/    BaseTest
|       |   `-- tests/   Test classes
|       `-- resources/
|           |-- config/      Android/iOS/test properties
|           |-- testdata/    JSON test data
|           `-- testsuites/  TestNG suite XML
|-- pom.xml
|-- testng.xml
|-- run-tests.bat
|-- run-tests.sh
|-- run-real-data-tests.bat
`-- run-real-data-tests.sh
```

## Cài đặt

Yêu cầu môi trường:

- JDK 11 hoặc JDK mới hơn có thể compile target 11.
- Maven.
- Node.js/npm nếu cài Appium qua npm.
- Android SDK và `adb` nếu chạy Android.
- Appium Server 2.x.
- UiAutomator2 driver cho Android.

Cài Appium và driver:

```bash
npm install -g appium
appium driver install uiautomator2
```

Build test code:

```bash
cd C:\DATLTN\testing\automation\appium-java
mvn test-compile
```

## Biến môi trường và cấu hình

Module không dùng `.env`. Cấu hình nằm trong:

```text
src/test/resources/config/test.properties
src/test/resources/config/android.properties
src/test/resources/config/ios.properties
```

Giá trị đang được cấu hình trong source:

- Appium server: `http://127.0.0.1:4723`
- Android package: `com.modis`
- Android activity: `com.modis.MainActivity`
- Android app path: `C:\DATLTN\Modis_FE_TL_ANDROID\android\app\build\outputs\apk\release\app-release.apk`
- iOS bundle id: `com.modis.app`
- Platform mặc định: `android`

## Hướng dẫn chạy và sử dụng

Khởi động Appium server:

```bash
appium server --port 4723 --address 127.0.0.1
```

Kiểm tra Android device:

```bash
adb devices
```

Chạy toàn bộ suite:

```bash
mvn test
```

Chạy suite XML:

```bash
mvn test -DsuiteXmlFile=testng.xml
```

Chạy theo Maven profile:

```bash
mvn test -Pandroid
mvn test -Pios
mvn test -Psmoke
mvn test -Pregression
mvn test -Pparallel
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

Chạy bằng script:

```bash
run-tests.bat
run-real-data-tests.bat
```

## API Documentation

Module này không cung cấp API. `ApiUtils.java` là helper HTTP client cho test.

## Database

Module automation không kết nối database trực tiếp.

## Test Data

```text
src/test/resources/testdata/users.json
src/test/resources/testdata/photos.json
src/test/resources/testdata/messages.json
src/test/resources/testdata/invalid_login.json
src/test/resources/testdata/friends.json
```

## Reports

Sau khi chạy test, kết quả được tạo tại:

```text
target/surefire-reports/
target/allure-results/
reports/
screenshots/
logs/
```

Tạo Allure report:

```bash
mvn allure:report
```

## Scripts

```bash
run-tests.bat
run-tests.sh
run-real-data-tests.bat
run-real-data-tests.sh
```

## Giải thích số liệu và cấu hình quan trọng

- Maven compiler source/target là `11`.
- `newCommandTimeout=300`: Appium session cho phép 300 giây không có command trước khi timeout.
- `implicit.wait=0`: không dùng implicit wait mặc định; test dựa vào explicit wait.
- `explicit.wait=10`: explicit wait mặc định là 10 giây.
- `short.wait=3`, `long.wait=30`: các mức chờ phụ trợ cho test.
- `test.retryCount=2`: retry test tối đa 2 lần theo cấu hình.
- `screenshot.onFailure=true`: tự chụp ảnh khi test fail.
- `report.extent.name=ModisAutomationReport.html`: tên report Extent mặc định.
- `parallel.enabled=false`: cấu hình properties mặc định không bật parallel execution.
