# Modis JMeter Performance Tests

## Tổng quan dự án

Module này chứa performance tests cho backend Modis bằng Apache JMeter và Java utilities. Source code hiện có bao gồm:

- JMeter test plans cho load, stress, spike, endurance, image upload và WebSocket.
- CSV test data cho users, messages, posts, reactions, friend requests và search queries.
- Java utilities để đọc/phân tích kết quả `.jtl`, tạo dữ liệu và so sánh metrics.
- Script chạy test trên Windows và shell.

## Công nghệ sử dụng

- Java 17.
- Maven.
- Apache JMeter 5.6.2.
- JMeter Maven Plugin 3.7.0.
- Jackson Databind, JSR310 và CSV.
- Apache Commons CSV.
- Apache Commons Math.
- Commons IO, Commons CLI, Commons Lang.
- Logback.
- JUnit Jupiter.

## Kiến trúc

- `test-plans` chứa các file `.jmx`.
- `configs` chứa JMeter fragments và file properties.
- `test-data` chứa CSV và sample image data.
- `src/main/java/com/modis/performance` chứa Java utilities:
  - `auth`: helper authentication.
  - `generators`: tạo dữ liệu test.
  - `parsers`: parse JTL.
  - `comparator`: so sánh performance.
  - `utils`: entrypoint `ResultsComparator`.
  - `model`: model kết quả và metrics.

## Cấu trúc thư mục

```text
performance/jmeter/
|-- configs/
|   |-- auth-config.jmx
|   |-- http-defaults.jmx
|   |-- modis-config.properties
|   `-- websocket-config.jmx
|-- scripts/
|   |-- setup-environment.bat
|   `-- setup-environment.sh
|-- src/main/java/com/modis/performance/
|   |-- auth/
|   |-- comparator/
|   |-- generators/
|   |-- model/
|   |-- parsers/
|   `-- utils/
|-- test-data/
|-- test-plans/
|   |-- endurance-tests/
|   |-- load-tests/
|   |-- spike-tests/
|   |-- stress-tests/
|   `-- *.jmx
|-- pom.xml
|-- run-tests.bat
|-- run-tests.sh
|-- run-load-test.bat
|-- run-load-test.sh
|-- run-stress-test.bat
|-- run-stress-test.sh
|-- run-spike-test.bat
|-- run-endurance-test.bat
|-- run-image-upload-test.bat
|-- run-all-tests.bat
`-- run-all-tests.sh
```

## Cài đặt

Yêu cầu môi trường:

- JDK 17.
- Maven.
- Apache JMeter nếu chạy trực tiếp bằng lệnh `jmeter`.

Build Java utilities:

```bash
cd C:\DATLTN\testing\performance\jmeter
mvn clean package -DskipTests
```

## Biến môi trường và cấu hình

Module không dùng `.env`. Cấu hình chính nằm trong:

```text
configs/modis-config.properties
```

Backend hiện tại:

```text
modis.base.url=https://modis-backend.onrender.com
modis.websocket.url=wss://modis-backend.onrender.com/ws
```

Tham số test trong file config:

```text
load.test.users=50
load.test.ramp.up=300
load.test.duration=600
stress.test.users=200
stress.test.ramp.up=180
stress.test.duration=900
spike.test.users=500
spike.test.ramp.up=60
spike.test.duration=300
endurance.test.users=30
endurance.test.ramp.up=600
endurance.test.duration=3600
```

## Hướng dẫn chạy và sử dụng

Chạy bằng Maven profiles:

```bash
mvn test -Pload-test
mvn test -Pstress-test
mvn test -Pspike-test
mvn test -Pendurance-test
```

Chạy bằng script Windows:

```bash
run-load-test.bat
run-stress-test.bat
run-spike-test.bat
run-endurance-test.bat
run-image-upload-test.bat
run-all-tests.bat
```

Chạy bằng script shell:

```bash
./run-load-test.sh
./run-stress-test.sh
./run-all-tests.sh
```

Chạy trực tiếp bằng JMeter CLI:

```bash
jmeter -n -t test-plans/load-tests/modis-load-test.jmx -l results/load-test-results.jtl -e -o reports/load-test-html
```

So sánh kết quả bằng Java utility sau khi build:

```bash
java -cp target/modis-performance-tests-2.0.0-jar-with-dependencies.jar com.modis.performance.utils.ResultsComparator results/baseline.jtl results/current.jtl
```

Cách đọc kết quả:

- File `.jtl` trong `results/` là dữ liệu raw của JMeter.
- HTML report trong `reports/` là dashboard để xem response time, throughput, error rate và request detail.
- `ResultsComparator` dùng để so sánh hai lần chạy test và tìm regression.

## API Documentation

Module này không cung cấp API. Test plans gọi backend Modis theo URL trong `configs/modis-config.properties`.

## Database

Module performance không kết nối database trực tiếp. Tất cả request đi qua backend API.

## Test Data

```text
test-data/users.csv
test-data/messages.csv
test-data/chat-messages.csv
test-data/post-content.csv
test-data/post-captions.csv
test-data/reactions.csv
test-data/friend-requests.csv
test-data/search-queries.csv
test-data/sample-image-urls.csv
test-data/sample-image-info.txt
test-data/sample-image.jpg
```

## Reports

JMeter tạo kết quả trong:

```text
results/
reports/
```

Trong workspace hiện có cũng có các file/report sinh ra từ lần chạy trước; đây là output runtime, không phải source test plan.

## Scripts

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
scripts/setup-environment.bat
scripts/setup-environment.sh
```

## Giải thích số liệu và cấu hình quan trọng

| Tham số | Giá trị trong code/config | Ý nghĩa |
| --- | ---: | --- |
| `default.timeout` | 90000 ms | Timeout mặc định 90 giây cho request. |
| `default.connect.timeout` | 30000 ms | Timeout kết nối 30 giây. |
| `default.response.timeout` | 90000 ms | Timeout phản hồi 90 giây. |
| `load.test.users` | 50 | Số virtual users cho load test. |
| `load.test.ramp.up` | 300 giây | Thời gian tăng dần từ 0 lên 50 users. |
| `load.test.duration` | 600 giây | Tổng thời gian chạy load test. |
| `stress.test.users` | 200 | Số virtual users cho stress test. |
| `stress.test.ramp.up` | 180 giây | Thời gian tăng dần tải stress. |
| `stress.test.duration` | 900 giây | Tổng thời gian chạy stress test. |
| `spike.test.users` | 500 | Số virtual users khi kiểm tra tải tăng đột biến. |
| `spike.test.ramp.up` | 60 giây | Thời gian tăng tải nhanh cho spike test. |
| `spike.test.duration` | 300 giây | Tổng thời gian chạy spike test. |
| `endurance.test.users` | 30 | Số virtual users cho test chạy dài. |
| `endurance.test.ramp.up` | 600 giây | Thời gian tăng tải từ từ cho endurance test. |
| `endurance.test.duration` | 3600 giây | Tổng thời gian chạy endurance test, tương đương 1 giờ. |
| `max.image.size` | 10485760 bytes | Kích thước ảnh tối đa 10 MB. |
| `response.time.threshold.auth` | 2000 ms | Ngưỡng response time cho auth API. |
| `response.time.threshold.feed` | 3000 ms | Ngưỡng response time cho feed API. |
| `response.time.threshold.upload` | 10000 ms | Ngưỡng response time cho upload ảnh. |
| `response.time.threshold.message` | 500 ms | Ngưỡng response time cho messaging. |
| `error.rate.threshold` | 1.0 | Ngưỡng error rate tham chiếu trong config. |
| `success.rate.threshold` | 99.0 | Ngưỡng success rate tham chiếu trong config. |

Diễn giải nhanh:

- `users` là số lượng virtual users JMeter mô phỏng.
- `ramp.up` là thời gian để JMeter tăng dần đến đủ số users, giúp tránh dồn tải ngay lập tức.
- `duration` là tổng thời gian giữ test chạy.
- Response time threshold là mốc tham chiếu để đọc report, không phải logic backend.
- Error rate càng thấp càng tốt; config đặt mốc tham chiếu `1.0`.
- Success rate càng cao càng tốt; config đặt mốc tham chiếu `99.0`.
