# 📱 Modis Social Mobile App Automation Testing Suite
> Framework kiểm thử tự động toàn diện cho ứng dụng di động React Native Modis, sử dụng **Appium Java Client 9.x**, **Selenium 4.22.0**, **TestNG 7.8.0** và mô hình **Page Object Model (POM)**.

---

## 🏗️ Kiến trúc Framework & Tech Stack

Sự đồng bộ hoàn chỉnh giữa kịch bản kiểm thử tự động và môi trường runtime thực tế của hệ sinh thái **Modis App**:

```text
┌────────────────────────────────────────────────────────────────────────┐
│                        MODIS AUTOMATION ARCHITECTURE                   │
├────────────────────────────────────────────────────────────────────────┤
│                                                                        │
│   📱 Target App               🤖 Automation Engine       📊 Reporting  │
│  ┌──────────────────┐        ┌────────────────────┐     ┌───────────┐  │
│  │ React Native App │◄──────►│ Appium Server 2.x  │────►│ TestNG    │  │
│  │ Android:         │        │ UiAutomator2       │     │ Allure    │  │
│  │ - Package:       │        │ Java 11/17         │     │ Extent    │  │
│  │   com.modis      │        │ Maven 3.x          │     │ Reports   │  │
│  │ - MainActivity   │        │ Appium Client 9.3  │     └───────────┘  │
│  │ iOS (Experimental│        │ Selenium 4.22.0    │                    │
│  │ - BundleId:      │        └────────────────────┘                    │
│  │   com.modis.app  │                                                  │
│  └──────────────────┘                                                  │
│                                                                        │
│  🌐 Backend API: http://10.0.150.148:8080/ (React Native dev proxy)   │
│  📲 Emulator / Real Device: Android 11.0+ (API 30+)                     │
└────────────────────────────────────────────────────────────────────────┘
```

### 🔧 Công nghệ sử dụng (Tech Stack thực tế):
* **Language & Runtime**: Java 11 / 17 (Target 11)
* **Build Tool**: Maven 3.x
* **Core Drivers**: Appium Java Client `9.3.0` + Selenium `4.22.0` (Đã đồng bộ triệt để qua `<dependencyManagement>` tránh xung đột lớp `ContextAware`)
* **Test Runner**: TestNG `7.8.0` (Hỗ trợ phân nhóm test suites, parameters, parallel execution)
* **Reporting Engine**: ExtentReports `5.1.1` + Allure `2.24.0` (HTML Reports sống động & tự động đính kèm ảnh chụp màn hình khi test thất bại)

---

## 📁 Cấu trúc Thư mục Kỹ thuật

Dự án tuân thủ nghiêm ngặt mô hình thiết kế **Page Object Model (POM)**:

```text
testing/automation/appium-java/
├── src/
│   ├── main/java/com/modis/
│   │   ├── base/                     # Các lớp cơ sở quản lý tương tác và cử chỉ
│   │   │   └── BasePage.java         # Chứa cơ chế Explicit Wait & Touch Gestures
│   │   ├── constants/                # Lưu trữ hằng số dùng chung toàn suite
│   │   │   ├── AppConstants.java     # Package name thực tế (com.modis) & Timeouts
│   │   │   └── TestIDs.java          # Danh sách Accessibility IDs đồng bộ với React Native
│   │   ├── drivers/                  # Quản lý driver tự động hóa
│   │   │   └── DriverManager.java    # Khởi tạo Appium Driver qua UiAutomator2Options/XCUITestOptions
│   │   ├── listeners/                # Bộ lắng nghe sự kiện thực thi kiểm thử
│   │   │   └── TestListener.java     # Chụp ảnh màn hình khi fail & ghi log ExtentReports
│   │   ├── pages/                    # 📱 Các Page Object thực tế (10+ màn hình)
│   │   │   ├── LoginPage.java        # Xử lý đăng nhập
│   │   │   ├── SignupPage.java       # Đăng ký tài khoản mới
│   │   │   ├── HomePage.java         # Bảng tin chính & Navigation Bottom
│   │   │   ├── ProfilePage.java      # Trang cá nhân & cập nhật thông tin
│   │   │   ├── MessagePage.java      # Danh sách cuộc hội thoại chat
│   │   │   ├── ConversationPage.java # Giao diện nhắn tin chi tiết
│   │   │   ├── FriendsPage.java      # Danh sách bạn bè & xử lý kết bạn
│   │   │   ├── TakePage.java         # Trình chụp ảnh từ Camera
│   │   │   ├── SendPhotoPage.java    # Gửi ảnh chụp tới bạn bè
│   │   │   ├── AllImagesPage.java    # Màn hình chọn ảnh thư viện
│   │   │   └── LoadingPage.java      # Xử lý màn hình chào & chờ tải
│   │   └── utils/                    # 🛠️ Bộ công cụ tiện ích bổ trợ
│   │       ├── ConfigReader.java     # Đọc cấu hình properties linh hoạt
│   │       └── GestureUtils.java     # Thực hiện các thao tác vuốt (swipe), chạm (tap) W3C
│   └── test/
│       ├── java/com/modis/
│       │   ├── base/
│       │   │   └── BaseTest.java     # Setup/Teardown Appium Session trước & sau mỗi kịch bản
│       │   └── tests/                # 🎯 Kịch bản kiểm thử thực tế (6 modules)
│       │       ├── AuthenticationTests.java # Kiểm thử Đăng nhập, Đăng ký, Đăng xuất
│       │       ├── NavigationTests.java     # Điều hướng giữa các Tab chức năng
│       │       ├── CameraTests.java         # Chụp hình & truy cập Album ảnh
│       │       ├── MessagingTests.java      # Gửi tin nhắn văn bản, emoji, hình ảnh
│       │       ├── FriendsTests.java        # Gửi/Nhận yêu cầu kết bạn
│       │       └── ProfileTests.java        # Cập nhật thông tin cá nhân & Đổi mật khẩu
│       └── resources/
│           ├── config/
│           │   ├── test.properties   # Cấu hình chung cho suite
│           │   ├── android.properties # Cấu hình chi tiết cho hệ điều hành Android
│           │   └── ios.properties    # Cấu hình dự phòng cho iOS
│           ├── testdata/             # Bộ dữ liệu test định dạng JSON
│           └── logback-test.xml      # Cấu hình log đầu ra
├── pom.xml                          # Quản lý Maven Dependencies
└── testng.xml                       # Định nghĩa luồng chạy Suite & phân nhóm (smoke/regression)
```

---

## 🎯 Danh sách Chức năng được Kiểm thử (Tested Features)

Các kịch bản kiểm thử được thiết kế bám sát theo luồng nghiệp vụ thực tế của sản phẩm Modis:
1. **Authentication (Xác thực)**: Luồng Đăng nhập hợp lệ/không hợp lệ, Đăng ký người dùng mới với xác thực định dạng dữ liệu đầu vào (email, phone, password), Đăng xuất an toàn.
2. **Navigation (Điều hướng)**: Khả năng di chuyển mượt mà giữa các Tab chính (Home, Friends, Messaging, Camera, Profile) mà không làm mất trạng thái ứng dụng.
3. **Camera & Sharing (Máy ảnh)**: Yêu cầu cấp quyền hệ thống, kích hoạt camera ảo hoặc camera vật lý, chụp ảnh thực tế, chọn ảnh từ thư viện, và gửi ảnh chia sẻ.
4. **Friends Management (Bạn bè)**: Tìm kiếm bạn bè bằng username, gửi lời mời kết bạn, kiểm tra danh sách lời mời chờ duyệt, đồng ý/từ chối kết bạn.
5. **Messaging (Nhắn tin)**: Tạo cuộc trò chuyện mới, nhắn tin thời gian thực với backend API, gửi tin nhắn chứa văn bản dài, ký tự đặc biệt, emoji và hình ảnh.
6. **Profile (Cá nhân)**: Thay đổi ảnh đại diện (avatar), chỉnh sửa họ tên, thay đổi mật khẩu và lưu cấu hình giao diện Sáng/Tối (Light/Dark mode).

---

## 🛠️ Hướng dẫn Setup Môi trường Android Chi tiết (Windows)

Để đảm bảo Appium có thể khởi chạy và giao tiếp thành công với thiết bị Android Emulator hoặc Real Device, vui lòng thực hiện tuần tự các bước thiết lập dưới đây.

### Bước 1: Cài đặt các công cụ nền tảng
1. **Java Development Kit (JDK 17)**:
   * Tải bản cài đặt `.msi` cho Windows từ [Eclipse Adoptium Temurin](https://adoptium.net/temurin/releases/?version=17).
   * Cài đặt vào thư mục mặc định (ví dụ: `C:\Program Files\Eclipse Adoptium\jdk-17.x.x`).
2. **Apache Maven**:
   * Tải bản Binary zip từ [Apache Maven Download](https://maven.apache.org/download.cgi).
   * Giải nén vào thư mục `C:\apache-maven-3.9.x`.
3. **Android Studio & Android SDK**:
   * Tải và cài đặt [Android Studio](https://developer.android.com/studio).
   * Mở Android Studio, vào **SDK Manager** > tải **Android SDK Platform-Tools**, **Android SDK Command-line Tools (Latest)**, và **Android Emulator**.
4. **Node.js**:
   * Tải phiên bản LTS mới nhất từ [NodeJS](https://nodejs.org/).

### Bước 2: Thiết lập Biến môi trường (Environment Variables)
Bấm phím Windows, tìm kiếm **"Edit the system environment variables"**, thêm mới các biến hệ thống sau:

| Biến (Variable) | Giá trị (Value) |
| :--- | :--- |
| `JAVA_HOME` | `C:\Program Files\Eclipse Adoptium\jdk-17.x.x` |
| `MAVEN_HOME` | `C:\apache-maven-3.9.x` |
| `ANDROID_HOME` | `C:\Users\<Tên_User_Của_Bạn>\AppData\Local\Android\Sdk` |

Tiếp tục chỉnh sửa biến **`Path`** trong System Variables và thêm vào 5 dòng sau:
```text
%JAVA_HOME%\bin
%MAVEN_HOME%\bin
%ANDROID_HOME%\platform-tools
%ANDROID_HOME%\emulator
%ANDROID_HOME%\cmdline-tools\latest\bin
```
> ⚠️ **LƯU Ý QUAN TRỌNG**: Sau khi lưu các biến môi trường, hãy khởi động lại toàn bộ IDE (IntelliJ/VSCode) hoặc Terminal (PowerShell/CMD) để hệ thống nhận diện các lệnh mới.

### Bước 3: Khởi tạo Máy ảo Android (Emulator) hoặc Thiết bị thật
* **Máy ảo (Emulator)**: Mở Android Studio > **Device Manager** > Tạo thiết bị ảo mới (Khuyến nghị: **Pixel 7**, Hệ điều hành **Android 11.0 (API 30)** hoặc cao hơn). Bấm nút Play (▶️) để khởi chạy máy ảo.
* **Thiết bị thật (Real Device)**:
  * Trên điện thoại Android, vào **Settings** > **About Phone** > Chạm vào **Build Number** 7 lần liên tục để mở chế độ Nhà phát triển (Developer Options).
  * Vào **Developer Options** > kích hoạt **USB Debugging** và **Install via USB**.
  * Kết nối điện thoại với máy tính qua cáp truyền dữ liệu chất lượng cao.
* **Xác thực kết nối**: Mở CMD/PowerShell và chạy lệnh:
  ```bash
  adb devices
  ```
  *Danh sách thiết bị phải hiển thị mã thiết bị kèm theo chữ `device`. Nếu hiển thị `unauthorized`, hãy kiểm tra màn hình điện thoại và chọn "Cho phép gỡ lỗi USB".*

### Bước 4: Cài đặt Appium & Drivers
Mở Terminal/CMD dưới quyền **Administrator** và chạy:
```bash
# Cài đặt Appium Server toàn hệ thống
npm install -g appium

# Cài đặt Driver UiAutomator2 cho thiết bị Android
appium driver install uiautomator2

# Verify các drivers đã được cài đặt thành công
appium driver list --installed
```

### Bước 5: Khởi động Appium Server
**⚠️ QUAN TRỌNG**: Bạn PHẢI khởi động Appium Server trước khi chạy test!

**Cách 1: Sử dụng script tự động (Khuyến nghị)**
```bash
# Chạy script khởi động tự động
start-appium-server.bat
```

**Cách 2: Khởi động thủ công**
Chạy lệnh sau trên một cửa sổ Terminal riêng biệt và giữ nguyên cửa sổ này trong suốt thời gian thực hiện kiểm thử:
```bash
# Khởi động Appium server
appium

# HOẶC với cấu hình chi tiết
appium server --port 4723 --address 127.0.0.1 --allow-insecure chromedriver_autodownload
```

**Kiểm tra server đã chạy:**
```bash
# Kiểm tra server status
curl http://127.0.0.1:4723/status
```
*(Nếu server chạy thành công, bạn sẽ thấy JSON response chứa thông tin server)*

---

## 🍏 Hướng dẫn Setup Môi trường iOS (Experimental)

> 💡 **TRẠNG THÁI HỖ TRỢ**: Hiện tại, framework đã tích hợp sẵn Driver class dành cho iOS (`XCUITestOptions`) và cơ chế quản lý đa nền tảng trong `DriverManager.java`. Tuy nhiên, do ràng buộc hệ sinh thái, việc chạy thử nghiệm trên iOS là **thử nghiệm (experimental)** và yêu cầu một máy tính chạy hệ điều hành macOS.

### Yêu cầu tiên quyết trên macOS:
1. **Xcode**: Cài đặt phiên bản Xcode mới nhất từ App Store cùng với iOS Simulators tương ứng.
2. **CocoaPods**: Cài đặt trình quản lý thư viện thông qua lệnh `sudo gem install cocoapods`.
3. **Appium XCUITest Driver**:
   ```bash
   appium driver install xcuitest
   ```
4. **Cấu hình WebDriverAgent**:
   * Truy cập thư mục cài đặt driver XCUITest trong hệ thống (thường nằm ở `~/.appium/node_modules/appium-xcuitest-driver/node_modules/appium-webdriveragent`).
   * Mở dự án `WebDriverAgent.xcodeproj` bằng Xcode, thiết lập Signing & Capabilities với tài khoản Apple Developer của bạn để build thành công lên thiết bị thật hoặc máy ảo Simulator.

---

## 🚀 Thực thi Kiểm thử (Run Tests)

**⚠️ TRƯỚC KHI CHẠY TEST**: Đảm bảo Appium server đã được khởi động!

Di chuyển Terminal của bạn tới thư mục gốc của automation project trước khi chạy lệnh:
```bash
cd C:\DATLTN\testing\automation\appium-java
```

### Checklist trước khi chạy test:
1. ✅ **Appium server đang chạy**: `curl http://127.0.0.1:4723/status`
2. ✅ **Device/emulator kết nối**: `adb devices`
3. ✅ **App đã cài đặt**: Modis app có trên device
4. ✅ **APK file tồn tại**: `C:\DATLTN\Modis_FE_TL_ANDROID\android\app\build\outputs\apk\release\app-release.apk`

### 1. Biên dịch Dự án (Compilation)
```bash
# Clean target folder và biên dịch mã nguồn test
mvn clean compile

# Biên dịch riêng kịch bản test để kiểm tra lỗi cú pháp
mvn test-compile
```

### 2. Kiểm thử Toàn bộ Suite (testng.xml)
Lệnh này sẽ thực thi tất cả các kịch bản test được định nghĩa trong file `testng.xml` bám sát các luồng hoạt động thực tế:
```bash
mvn test -DsuiteXmlFile=testng.xml
```

### 3. Thực thi theo từng Module Test Class riêng biệt
Nếu bạn chỉ muốn kiểm tra một chức năng cụ thể mà không muốn chạy toàn bộ suite, hãy sử dụng các lệnh dưới đây:

* **Authentication (Đăng ký/Đăng nhập/Đăng xuất)**:
  ```bash
  mvn test -Dtest=AuthenticationTests
  ```
* **Navigation (Luồng đi lại giữa các Tab)**:
  ```bash
  mvn test -Dtest=NavigationTests
  ```
* **Camera (Chụp ảnh/Cấp quyền chụp ảnh)**:
  ```bash
  mvn test -Dtest=CameraTests
  ```
* **Messaging (Chat văn bản & hình ảnh)**:
  ```bash
  mvn test -Dtest=MessagingTests
  ```
* **Friends (Gửi kết bạn/Đồng ý kết bạn)**:
  ```bash
  mvn test -Dtest=FriendsTests
  ```
* **Profile (Đổi thông tin cá nhân/Avatar/Theme Sáng Tối)**:
  ```bash
  mvn test -Dtest=ProfileTests
  ```

---

## 📊 Xem Báo cáo & Phân tích Kết quả

Sau khi kịch bản kiểm thử kết thúc, các tệp báo cáo sẽ tự động được sinh ra trong thư mục `target/`:

1. **Báo cáo Giao diện Web (ExtentReports)**:
   * Đường dẫn: `C:\DATLTN\testing\automation\appium-java\target\surefire-reports\index.html` hoặc `reports\ModisAutomationReport.html`.
   * Mở trực tiếp bằng bất kỳ trình duyệt nào để xem tỷ lệ Pass/Fail trực quan, thời gian chạy chi tiết của từng method.
2. **Ảnh chụp khi kiểm thử thất bại (Screenshots)**:
   * Thư mục lưu trữ: `C:\DATLTN\testing\automation\appium-java\target\screenshots\`
   * Định dạng tệp: `{testMethodName}_{timestamp}.png`
   * *Mỗi khi kịch bản phát hiện assertion fail, hệ thống sẽ tự động chụp màn hình thiết bị tại thời điểm đó và chèn trực tiếp đường dẫn ảnh vào báo cáo HTML giúp debug cực kỳ nhanh chóng.*
3. **Mã nguồn nhật ký hoạt động (Logs)**:
   * File log tổng hợp: `logs\automation.log`
   * Appium Server command logs: Được ghi nhận trực tiếp tại cửa sổ khởi động Appium Server giúp kiểm tra chính xác các lệnh API được truyền tải từ client.

---

## 🚨 Sổ tay Khắc phục Sự cố Thực tế (Troubleshooting Arsenal)

Dưới đây là tổng hợp các lỗi kinh điển trong quá trình cài đặt tự động hóa di động và giải pháp khắc phục triệt để:

### Lỗi 1: `java.net.ConnectException` - Không kết nối được Appium server
* **Nguyên nhân**: Appium server chưa được khởi động hoặc không chạy trên port 4723.
* **Cách sửa**: 
  1. Khởi động Appium server: `start-appium-server.bat` hoặc `appium`
  2. Kiểm tra server: `curl http://127.0.0.1:4723/status`
  3. Nếu port 4723 bị chiếm: `netstat -an | findstr :4723`

### Lỗi 2: `NoClassDefFoundError: org/openqa/selenium/ContextAware`
* **Nguyên nhân**: Phiên bản Appium Java Client 9.3.x kéo theo các thành phần phụ thuộc Selenium transitive quá mới (Selenium 4.35+ đã loại bỏ interface `ContextAware`), dẫn đến xung đột Class Loader khi khởi tạo driver.
* **Cách sửa (Đã áp dụng)**: Thêm khối cấu hình `<dependencyManagement>` vào `pom.xml` sử dụng `selenium-bom` ở phiên bản tương thích `${selenium.version}` (`4.22.0`). Maven sẽ tự động khóa và đồng bộ toàn bộ các package `selenium-*` về cùng phiên bản 4.22.0.

### Lỗi 2: `Original error: Could not find 'adb' in PATH` hoặc Appium không kết nối được device
* **Nguyên nhân**: Hệ thống chưa nhận diện được đường dẫn SDK do thiếu biến môi trường hoặc chưa khởi động lại Terminal/IDE.
* **Cách sửa**: 
  1. Kiểm tra biến `ANDROID_HOME` có trỏ đúng vào thư mục Sdk hay không.
  2. Mở cmd, chạy lệnh `where adb` để đảm bảo hệ thống tìm thấy tệp thi hành adb.
  3. Đảm bảo đã đóng toàn bộ cửa sổ terminal cũ và khởi động lại.

### Lỗi 3: Appium Inspector quay mãi không load hoặc không tạo được session
* **Nguyên nhân**: Mismatch cổng kết nối (Port) hoặc thiếu cấu hình namespace driver.
* **Cách sửa**: 
  1. Đảm bảo Appium Server đang chạy trên cổng `4723`.
  2. Trong Appium Inspector (Appium 2/3), Remote Path phải là `/` (KHÔNG dùng `/wd/hub`).
  3. Điền đầy đủ các thông tin capabilities theo định dạng chuẩn W3C (Ví dụ: `appium:automationName` thay vì `automationName`).

### Lỗi 4: Lỗi phiên bản Chromedriver không tương thích (`SessionNotCreatedException`)
* **Nguyên nhân**: Phiên bản trình duyệt Chrome được cài đặt sẵn trên thiết bị máy ảo/máy thật của bạn mới hơn/cũ hơn phiên bản Chromedriver mà Appium đang dùng.
* **Cách sửa**: Sử dụng tham số tự động tải khi kích hoạt Appium Server:
  ```bash
  appium --allow-insecure=uiautomator2:chromedriver_autodownload
  ```
  *Appium sẽ tự phân tích phiên bản Chrome trên thiết bị và tự động tải phiên bản Chromedriver tương thích từ Internet về máy của bạn.*

### Lỗi 5: Thiết bị thật báo trạng thái `unauthorized` khi chạy `adb devices`
* **Nguyên nhân**: Bạn chưa xác nhận quyền tin cậy máy tính trên điện thoại Android.
* **Cách sửa**: Rút cáp kết nối ra cắm lại, nhìn vào màn hình điện thoại, đánh dấu chọn "Luôn cho phép từ máy tính này" và bấm **OK**.

### Lỗi 6: Ngoại lệ `UnsupportedCommandException: Not implemented yet for pageLoad`
* **Nguyên nhân**: Thiết lập `pageLoadTimeout` qua driver không được Appium/UiAutomator2 hỗ trợ cho các ứng dụng Native di động, dẫn đến crash khi tạo session driver.
* **Cách sửa (Đã áp dụng)**: Loại bỏ hoàn toàn cuộc gọi `pageLoadTimeout` trong `BaseTest.java` và `DriverManager.java`. Chỉ cấu hình `implicitlyWait` để phục vụ tìm kiếm phần tử di động.

### Lỗi 7: Lỗi timeout ADB hoặc mất kết nối giữa chừng trên Android 15 (Đặc biệt dòng máy OPPO/ColorOS)
* **Nguyên nhân**: Cơ chế quản lý tài nguyên của Android 15/OPPO ngắt kết nối USB hoặc làm chậm phản hồi của tiến trình ADB, vượt quá giới hạn thời gian chờ mặc định của Appium (20 giây).
* **Cách sửa (Đã áp dụng)**: 
  1. Thiết lập các tham số thời gian chờ chuyên sâu thông qua DesiredCapabilities/UiAutomator2Options nhằm tăng cường độ bền vững:
     - `appium:adbExecTimeout` = `120000` (Tăng lên 120 giây)
     - `appium:uiautomator2ServerLaunchTimeout` = `90000` (90 giây)
     - `appium:uiautomator2ServerInstallTimeout` = `60000` (60 giây)
     - `appium:androidInstallTimeout` = `90000` (90 giây)
  2. Trên điện thoại OPPO, bắt buộc phải bật thêm cấu hình **"Disable Permission Monitoring"** và **"Install via USB"** trong Developer Options để tự động cấp quyền không chặn kịch bản chạy.

---

## 💡 Tiêu chuẩn và Chỉ dẫn Phát triển (Best Practices)
1. **Định locator bằng `testID` (Accessibility ID)**: Luôn ưu tiên dùng `MobileBy.AccessibilityId()` vì đây là locator nhanh nhất trên cả Android/iOS và không bao giờ bị thay đổi khi designer thay đổi cấu trúc giao diện hoặc đổi CSS/Styling. Hãy thống nhất với team phát triển React Native đặt thuộc tính `testID` cho mọi phần tử tương tác quan trọng.
2. **Không lạm dụng `Thread.sleep()`**: Sử dụng Explicit Wait thông qua lớp `WebDriverWait` để chờ phần tử hiển thị hoặc có thể click. Việc này giúp giảm thiểu tối đa thời gian chạy suite kiểm thử (Nếu phần tử hiển thị trong 0.5s, code sẽ chạy tiếp ngay lập tức thay vì bắt buộc phải dừng 5-10s lãng phí).
3. **Quy tắc cô lập trạng thái (State Isolation)**: Mỗi một kịch bản test phải hoạt động độc lập và tự dọn dẹp dữ liệu/trạng thái sau khi hoàn thành. Sử dụng `@BeforeMethod` để đảm bảo ứng dụng luôn bắt đầu ở trạng thái đăng nhập hoặc trang Home ổn định nhất.
4. **Data Driven Testing**: Đọc toàn bộ tài khoản thử nghiệm từ file JSON trong thư mục `src/test/resources/testdata/` thay vì hardcode trực tiếp vào mã nguồn Java.

---
*Chúc các bạn có những trải nghiệm tự động hóa tuyệt vời và duy trì chất lượng ứng dụng tốt nhất cùng với Modis Mobile App Automation Testing Suite!*
