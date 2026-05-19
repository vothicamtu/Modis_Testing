# Modis Performance Testing - Metrics Guide

## Tổng quan

Hướng dẫn này giải thích các metrics quan trọng trong performance testing cho Modis app và cách đọc hiểu kết quả JMeter.

## Key Performance Indicators (KPIs)

### 1. Response Time Metrics

#### Average Response Time
- **Mô tả**: Thời gian phản hồi trung bình của tất cả requests
- **Target cho Modis**:
  - Authentication: < 2s
  - Feed Loading: < 3s
  - Image Upload: < 10s
  - Real-time Messages: < 500ms
  - API Calls: < 1s

#### 95th Percentile Response Time
- **Mô tả**: 95% requests có thời gian phản hồi nhanh hơn giá trị này
- **Quan trọng**: Đại diện cho user experience thực tế
- **Target**: Không quá 2x average response time

#### Max Response Time
- **Mô tả**: Thời gian phản hồi chậm nhất
- **Cảnh báo**: Nếu quá cao có thể có timeout issues

### 2. Throughput Metrics

#### Requests per Second (RPS)
- **Mô tả**: Số requests server xử lý được mỗi giây
- **Target cho Modis**: 50+ RPS
- **Calculation**: Total Requests / Test Duration

#### Transactions per Second (TPS)
- **Mô tả**: Số transactions hoàn thành thành công mỗi giây
- **Target**: 95% của RPS

### 3. Error Metrics

#### Error Rate (%)
- **Mô tả**: Tỷ lệ requests thất bại
- **Target**: < 1%
- **Calculation**: (Failed Requests / Total Requests) × 100

#### Error Types
- **HTTP 4xx**: Client errors (authentication, validation)
- **HTTP 5xx**: Server errors (crashes, overload)
- **Timeouts**: Network/server timeout issues
- **Connection Errors**: Network connectivity problems

### 4. Concurrency Metrics

#### Active Threads
- **Mô tả**: Số users đồng thời đang active
- **Monitor**: Ramp-up pattern và stability

#### Connect Time
- **Mô tả**: Thời gian thiết lập connection
- **Target**: < 1s
- **Quan trọng**: Cho WebSocket connections

## Mobile-Specific Metrics

### 1. Network Efficiency

#### Bytes per Request
- **Mô tả**: Lượng data trung bình mỗi request
- **Quan trọng**: Mobile data usage
- **Target**: Minimize while maintaining functionality

#### Compression Ratio
- **Mô tả**: Hiệu quả nén data
- **Check**: Response headers cho gzip/deflate

### 2. Battery Impact Simulation

#### Request Frequency
- **Mô tả**: Tần suất requests từ mobile app
- **Target**: Balance between real-time và battery life

#### Keep-alive Efficiency
- **Mô tả**: Tái sử dụng connections
- **Target**: > 80% connection reuse

## Modis-Specific Metrics

### 1. Social Media Features

#### Feed Load Time
- **Mô tả**: Thời gian load feed với pagination
- **Target**: < 3s cho 20 posts
- **Include**: Image loading time

#### Image Upload Performance
- **Mô tả**: End-to-end image upload time
- **Target**: < 10s cho 5MB image
- **Include**: Cloudinary processing time

#### Reaction Response Time
- **Mô tả**: Thời gian xử lý emoji reactions
- **Target**: < 500ms
- **Critical**: Real-time user experience

### 2. Real-time Features

#### WebSocket Connection Success Rate
- **Mô tả**: Tỷ lệ thành công kết nối WebSocket
- **Target**: > 95%

#### Message Delivery Latency
- **Mô tả**: Thời gian từ send đến receive
- **Target**: < 1s
- **Include**: STOMP protocol overhead

#### Notification Delivery Time
- **Mô tả**: Thời gian push notifications
- **Target**: < 2s

### 3. Authentication & Security

#### JWT Token Validation Time
- **Mô tả**: Thời gian validate JWT token
- **Target**: < 100ms
- **Critical**: Affects all authenticated requests

#### Login Success Rate
- **Mô tả**: Tỷ lệ login thành công
- **Target**: > 99%
- **Monitor**: Rate limiting effects

## Reading JMeter Reports

### 1. Summary Report

```
Label               | Samples | Average | Min | Max | Std. Dev. | Error % | Throughput | KB/sec
Login              | 1000    | 1500ms  | 800 | 5000| 450       | 0.1%    | 10.5/sec   | 15.2
Get Feed           | 5000    | 2200ms  | 1000| 8000| 800       | 0.5%    | 25.3/sec   | 125.8
Upload Image       | 500     | 8500ms  | 3000|15000| 2100      | 2.0%    | 2.1/sec    | 450.2
```

**Phân tích**:
- Login: Good performance, low error rate
- Get Feed: Acceptable, monitor high max time
- Upload Image: High variance, investigate 2% errors

### 2. Response Times Over Time

**Patterns to Look For**:
- **Flat line**: Consistent performance ✅
- **Gradual increase**: Memory leaks or resource exhaustion ⚠️
- **Spikes**: Garbage collection or resource contention ⚠️
- **Sawtooth**: Periodic cleanup (normal) ✅

### 3. Active Threads Over Time

**Healthy Patterns**:
- Smooth ramp-up curve
- Stable plateau during test
- Gradual ramp-down

**Warning Signs**:
- Threads not reaching target (server overload)
- Erratic thread count (connection issues)

## Performance Baselines

### Load Test Baselines (50 users)
```
Metric                  | Target    | Acceptable | Poor
Average Response Time   | < 2s      | < 5s       | > 5s
95th Percentile        | < 4s      | < 8s       | > 8s
Error Rate             | < 0.5%    | < 2%       | > 2%
Throughput             | > 25 RPS  | > 15 RPS   | < 15 RPS
```

### Stress Test Baselines (200 users)
```
Metric                  | Target    | Acceptable | Poor
Average Response Time   | < 5s      | < 10s      | > 10s
95th Percentile        | < 10s     | < 20s      | > 20s
Error Rate             | < 2%      | < 5%       | > 5%
Throughput             | > 50 RPS  | > 30 RPS   | < 30 RPS
```

### Image Upload Baselines
```
File Size | Target Time | Max Acceptable
1MB       | < 3s        | < 8s
5MB       | < 8s        | < 15s
10MB      | < 15s       | < 30s
```

## Troubleshooting Common Issues

### High Response Times
1. **Check server resources** (CPU, Memory, Disk I/O)
2. **Database performance** (slow queries, connection pool)
3. **Network latency** (CDN, geographic distance)
4. **Cloudinary processing** (image optimization delays)

### High Error Rates
1. **Authentication failures** (token expiry, rate limiting)
2. **Server overload** (503 Service Unavailable)
3. **Database connection issues** (connection pool exhausted)
4. **Third-party service failures** (Cloudinary, WebSocket)

### Low Throughput
1. **Server capacity limits** (thread pool, connection limits)
2. **Database bottlenecks** (lock contention, slow queries)
3. **Network bandwidth** (especially for image uploads)
4. **Client-side delays** (think time too high)

### WebSocket Issues
1. **Connection failures** (firewall, proxy issues)
2. **Message delivery delays** (STOMP broker performance)
3. **Connection drops** (network instability, timeouts)

## Recommendations

### Before Testing
1. **Baseline measurement** với single user
2. **Environment consistency** (same as production)
3. **Data preparation** (realistic test data)
4. **Monitoring setup** (server metrics, logs)

### During Testing
1. **Monitor server resources** real-time
2. **Check error logs** for issues
3. **Validate test data** integrity
4. **Network monitoring** for bottlenecks

### After Testing
1. **Compare với baselines** và previous results
2. **Identify bottlenecks** và root causes
3. **Document findings** và recommendations
4. **Plan optimization** strategies

## Mobile Performance Considerations

### Network Conditions
- **3G simulation**: 1.6 Mbps down, 768 Kbps up
- **4G simulation**: 9 Mbps down, 9 Mbps up
- **WiFi simulation**: 30+ Mbps both ways

### Battery Impact
- **Frequent polling**: Minimize background requests
- **Connection reuse**: Implement HTTP keep-alive
- **Data compression**: Enable gzip/deflate
- **Image optimization**: Use appropriate resolutions

### User Experience
- **Progressive loading**: Show content as it loads
- **Offline handling**: Cache critical data
- **Error recovery**: Graceful degradation
- **Loading indicators**: Keep users informed

## Continuous Monitoring

### Automated Alerts
- Response time > 5s for 5 minutes
- Error rate > 2% for 2 minutes
- Throughput drop > 50% for 3 minutes
- WebSocket connection success < 90%

### Regular Testing Schedule
- **Daily**: Smoke tests (basic functionality)
- **Weekly**: Load tests (normal capacity)
- **Monthly**: Stress tests (peak capacity)
- **Quarterly**: Endurance tests (stability)

### Performance Regression Detection
- **Baseline comparison**: Track performance trends
- **Automated testing**: CI/CD integration
- **Performance budgets**: Set acceptable limits
- **Early warning**: Detect issues before production