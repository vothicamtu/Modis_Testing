# Modis Testing Deliverables

Tài liệu tổng hợp các hạng mục cần nộp cho phần kiểm thử tự động của tiểu luận tốt nghiệp Modis.

## 1. Đối chiếu yêu cầu nộp

| Yêu cầu | File/thư mục hiện có | Trạng thái |
| --- | --- | --- |
| Báo cáo tiểu luận tốt nghiệp hoàn chỉnh Word/PDF trình bày lý thuyết, thực hành và đánh giá kết quả | `C:\DATLTN\22130308_ĐỀCƯƠNGTIỂULUẬN.docx` | Thiếu bản báo cáo hoàn chỉnh; hiện mới thấy đề cương, chưa thấy PDF |
| Bộ test case chi tiết Excel/CSV | `C:\DATLTN\TEST_CASE_TLTN_ANDROID_FROM_CODE.xlsx` | Đã có Excel |
| Script kiểm thử tự động bằng Java, tổ chức theo Page Object Model | `testing/automation/appium-java/src/main/java/com/modis/pages/`, `testing/automation/appium-java/src/test/java/com/modis/tests/` | Đã có |
| Source code kiểm thử tự động bằng Appium Java lưu trên GitHub | `testing/automation/appium-java/` có source và `.github/workflows/mobile-tests.yml` | Source đã có; cần kiểm tra đã push lên GitHub hay chưa |
| Báo cáo kết quả kiểm thử HTML có ảnh và log minh họa | `testing/automation/appium-java/reports/`, `target/surefire-reports/`, `screenshots/`, `logs/` | Đã có |
| Video demo chạy test bằng Appium Inspector/OBS | Chưa tìm thấy file `.mp4`, `.mkv`, `.avi`, `.mov`, `.wmv` trong workspace ngoài `node_modules/build` | Thiếu |
| README hướng dẫn cài đặt và chạy test | `testing/automation/appium-java/README.md` | Đã cập nhật |

## 2. Thành phần kiểm thử tự động

Thư mục chính:

```text
testing/automation/appium-java/
```

Công nghệ:

- Java 11
- Maven
- Appium Java Client 9.3.0
- Selenium 4.22.0
- TestNG 7.8.0
- ExtentReports, TestNG HTML report, Allure results

Mô hình tổ chức:

- `src/main/java/com/modis/pages/`: Page Object Model cho các màn hình.
- `src/main/java/com/modis/base/BasePage.java`: thao tác dùng chung cho Page Object.
- `src/test/java/com/modis/base/BaseTest.java`: setup/teardown Appium session.
- `src/test/java/com/modis/tests/`: test case tự động.
- `src/test/resources/config/`: cấu hình Android/iOS/test.
- `src/test/resources/testdata/`: dữ liệu test JSON.

## 3. Module test hiện có

| Module | File |
| --- | --- |
| Authentication | `AuthenticationTests.java` |
| Feed | `FeedTests.java` |
| Friends | `FriendsTests.java` |
| Messaging | `MessagingTests.java` |
| Photo Sharing | `PhotoSharingTests.java` |
| Search | `SearchTests.java` |

Suite chính:

```text
testing/automation/appium-java/testng.xml
```

## 4. Cách chạy nhanh

Mở terminal tại thư mục automation:

```bash
cd C:\DATLTN\testing\automation\appium-java
```

Khởi động Appium:

```bash
start-appium-server.bat
```

Kiểm tra thiết bị:

```bash
adb devices
```

Build test:

```bash
mvn test-compile
```

Chạy toàn bộ test:

```bash
mvn test -DsuiteXmlFile=testng.xml
```

Chạy smoke/regression:

```bash
mvn test -Dgroups=smoke
mvn test -Dgroups=regression
```

## 5. Vị trí kết quả

| Kết quả | Đường dẫn |
| --- | --- |
| Extent HTML report mới nhất | `testing/automation/appium-java/reports/ModisReport_*.html` |
| TestNG HTML report | `testing/automation/appium-java/target/surefire-reports/index.html` |
| Email-friendly report | `testing/automation/appium-java/target/surefire-reports/emailable-report.html` |
| Screenshot minh họa | `testing/automation/appium-java/screenshots/` |
| Log chạy test | `testing/automation/appium-java/logs/` |
| Allure raw results | `testing/automation/appium-java/target/allure-results/` |

## 6. Việc còn cần bổ sung trước khi nộp

1. Viết bản báo cáo tiểu luận hoàn chỉnh dạng Word và export PDF. File hiện có ở root là đề cương, chưa phải báo cáo hoàn chỉnh.
2. Quay video demo chạy test bằng OBS hoặc Appium Inspector, lưu vào workspace, ví dụ `testing/automation/appium-java/demo/Modis_Appium_Demo.mp4`.
3. Xác nhận repository GitHub đã chứa source `testing/automation/appium-java` và cập nhật link GitHub vào báo cáo/README nếu giảng viên yêu cầu.
4. Mở file Excel `TEST_CASE_TLTN_ANDROID_FROM_CODE.xlsx` kiểm tra lần cuối các cột thường cần có: ID, module, mục tiêu, precondition, steps, expected result, actual result, status, note.

