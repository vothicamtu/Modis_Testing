# Modis Performance Testing - Best Practices

## Tổng quan

Hướng dẫn best practices cho performance testing của Modis social media app, bao gồm test design, execution, và analysis.

## Test Design Best Practices

### 1. Realistic User Scenarios

#### User Journey Modeling
```
Typical Modis User Session:
1. Login (1 request)
2. Check feed (3-5 requests với pagination)
3. View friend requests (1 request)
4. Check messages (2-3 requests)
5. Upload photo (2 requests: upload + create post)
6. React to posts (2-4 requests)
7. Browse more feed (2-3 requests)
8. Logout or background (1 request)

Total: 12-20 requests per session
Duration: 5-15 minutes
```

#### Think Time Patterns
```
Action Type          | Think Time Range | Realistic Behavior
Login               | 5-10s            | User entering credentials
Feed browsing       | 3-8s             | Reading posts, scrolling
Photo upload        | 10-30s           | Selecting, editing photo
Messaging           | 2-5s             | Reading, typing response
Reactions           | 1-3s             | Quick emotional response
```

### 2. Data Management

#### Test Data Strategy
- **Separate test users**: Không dùng production data
- **Data isolation**: Mỗi test run có data riêng
- **Cleanup strategy**: Xóa test data sau khi test
- **Data variety**: Different user types, content types

#### CSV Data Best Practices
```csv
# Good: Varied, realistic data
username,password,email,fullName,userType
testuser001,SecurePass123!,test001@modis.test,Alice Johnson,active
testuser002,SecurePass123!,test002@modis.test,Bob Smith,premium
testuser003,SecurePass123!,test003@modis.test,Carol Davis,new

# Bad: Repetitive, unrealistic data
user1,pass,user1@test.com,User One,normal
user2,pass,user2@test.com,User Two,normal
user3,pass,user3@test.com,User Three,normal
```

### 3. Load Pattern Design

#### Ramp-up Strategies
```
Test Type     | Users | Ramp-up | Reasoning
Load Test     | 50    | 5 min   | Gradual, realistic growth
Stress Test   | 200   | 3 min   | Faster to find limits
Spike Test    | 500   | 1 min   | Sudden traffic surge
Endurance     | 30    | 10 min  | Slow, stable long-term
```

#### Load Distribution
```
Feature Distribution (based on Modis usage):
- Feed browsing: 40%
- Messaging: 25%
- Photo upload: 15%
- Friend management: 10%
- Profile/settings: 10%
```

## Test Environment Best Practices

### 1. Environment Setup

#### Production-like Environment
- **Same infrastructure**: AWS/cloud setup matching production
- **Same data volume**: Realistic database size
- **Same integrations**: Cloudinary, WebSocket services
- **Same security**: SSL, authentication mechanisms

#### Network Simulation
```bash
# Simulate mobile network conditions
# 3G: 1.6 Mbps down, 768 Kbps up, 300ms latency
# 4G: 9 Mbps down, 9 Mbps up, 150ms latency

# JMeter network simulation
-Jhttpclient.timeout=30000
-Jhttpclient.connection.timeout=10000
```

### 2. Monitoring Setup

#### Server-side Monitoring
- **CPU usage**: Target < 80% during peak
- **Memory usage**: Monitor for leaks
- **Database connections**: Pool utilization
- **Disk I/O**: Especially for image uploads
- **Network bandwidth**: Upload/download rates

#### Application Monitoring
- **Response times**: Per endpoint
- **Error rates**: By error type
- **Database queries**: Slow query log
- **Cache hit rates**: Redis/memory cache
- **WebSocket connections**: Active connections

## JMeter Configuration Best Practices

### 1. JMeter Tuning

#### Memory Configuration
```bash
# jmeter.bat / jmeter.sh modifications
export JVM_ARGS="-Xms1g -Xmx4g -XX:MaxMetaspaceSize=256m"

# For large tests
export JVM_ARGS="-Xms2g -Xmx8g -XX:MaxMetaspaceSize=512m"
```

#### JMeter Properties
```properties
# jmeter.properties optimizations
jmeter.save.saveservice.output_format=xml
jmeter.save.saveservice.response_data=false
jmeter.save.saveservice.samplerData=false
jmeter.save.saveservice.response_headers=false
jmeter.save.saveservice.requestHeaders=false

# HTTP settings
httpclient4.retrycount=1
httpclient4.request.timeout=30000
httpclient4.connect_timeout=10000
```

### 2. Test Plan Structure

#### Modular Design
```
Test Plan
├── User Defined Variables (global config)
├── HTTP Request Defaults
├── HTTP Header Manager
├── Cookie Manager
├── CSV Data Sets
├── Thread Groups
│   ├── Setup (login, initialization)
│   ├── Main Scenarios (weighted)
│   └── Teardown (cleanup)
└── Listeners (minimal for non-GUI)
```

#### Reusable Components
- **HTTP Request Defaults**: Base URL, timeouts
- **Header Managers**: Common headers, auth
- **CSV Data Sets**: Shared across thread groups
- **Timers**: Consistent think times

### 3. Authentication Handling

#### JWT Token Management
```xml
<!-- Extract token once per user -->
<JSONPostProcessor>
  <stringProp name="JSONPostProcessor.referenceNames">jwt_token</stringProp>
  <stringProp name="JSONPostProcessor.jsonPathExprs">$.token</stringProp>
  <stringProp name="JSONPostProcessor.match_numbers">1</stringProp>
</JSONPostProcessor>

<!-- Reuse token in subsequent requests -->
<HeaderManager>
  <elementProp name="Authorization" elementType="Header">
    <stringProp name="Header.value">Bearer ${jwt_token}</stringProp>
  </elementProp>
</HeaderManager>
```

#### Token Refresh Handling
```xml
<!-- Check for token expiry -->
<IfController>
  <stringProp name="IfController.condition">${__javaScript(
    "${__time()}" > "${token_expiry}" - 300000
  )}</stringProp>
  <!-- Refresh token logic here -->
</IfController>
```

## Mobile-Specific Best Practices

### 1. Network Behavior Simulation

#### Connection Management
- **Keep-alive**: Simulate mobile connection reuse
- **Connection limits**: Mobile browsers limit concurrent connections
- **Retry logic**: Handle network interruptions

#### Bandwidth Considerations
```xml
<!-- Simulate mobile bandwidth limits -->
<ConstantThroughputTimer>
  <stringProp name="throughput">60.0</stringProp> <!-- requests per minute -->
  <intProp name="calcMode">0</intProp> <!-- This thread only -->
</ConstantThroughputTimer>
```

### 2. Image Upload Optimization

#### File Size Simulation
```
Image Type    | Size Range | Compression | Use Case
Profile pic   | 100KB-500KB| High        | User avatars
Post image    | 500KB-5MB  | Medium      | Social posts
Original      | 2MB-10MB   | Low         | High quality
```

#### Upload Pattern
```xml
<!-- Realistic upload with progress simulation -->
<HTTPSamplerProxy>
  <stringProp name="HTTPSampler.path">/api/posts/upload/image</stringProp>
  <stringProp name="HTTPSampler.method">POST</stringProp>
  <boolProp name="HTTPSampler.DO_MULTIPART_POST">true</boolProp>
  <!-- Large timeout for uploads -->
  <stringProp name="HTTPSampler.response_timeout">120000</stringProp>
</HTTPSamplerProxy>
```

### 3. Real-time Features Testing

#### WebSocket Simulation
```xml
<!-- Connection establishment -->
<HTTPSamplerProxy testname="WebSocket Handshake">
  <elementProp name="HTTPsampler.Arguments" elementType="Arguments">
    <elementProp name="token" elementType="HTTPArgument">
      <stringProp name="Argument.value">${jwt_token}</stringProp>
    </elementProp>
  </elementProp>
  <stringProp name="HTTPSampler.path">/ws</stringProp>
</HTTPSamplerProxy>

<!-- Message polling simulation -->
<LoopController>
  <stringProp name="LoopController.loops">10</stringProp>
  <!-- Poll messages every 5 seconds -->
  <ConstantTimer>
    <stringProp name="ConstantTimer.delay">5000</stringProp>
  </ConstantTimer>
</LoopController>
```

## Test Execution Best Practices

### 1. Pre-test Validation

#### Smoke Testing
```bash
# Run with 1 user first
jmeter -n -t modis-load-test.jmx -Jusers=1 -Jduration=60 -l smoke-test.jtl

# Validate all requests pass
grep "false" smoke-test.jtl | wc -l  # Should be 0
```

#### Environment Health Check
- **Server availability**: All endpoints responding
- **Database connectivity**: Connection pool healthy
- **Third-party services**: Cloudinary, WebSocket broker
- **Test data**: Users exist, clean state

### 2. Test Monitoring

#### Real-time Monitoring
```bash
# Monitor test progress
tail -f jmeter.log | grep -E "(ERROR|WARN)"

# Check server resources
top -p $(pgrep java)  # JMeter process
htop  # Overall system

# Monitor network
iftop  # Network traffic
netstat -an | grep :8080  # Server connections
```

#### Early Warning Signs
- **Response times increasing**: Server struggling
- **Error rate climbing**: Capacity exceeded
- **Memory usage growing**: Potential memory leak
- **Connection errors**: Network/server issues

### 3. Test Data Integrity

#### During Test Validation
- **User sessions**: No cross-contamination
- **Data consistency**: Database state valid
- **File uploads**: Images properly stored
- **WebSocket connections**: Proper cleanup

#### Post-test Cleanup
```sql
-- Clean up test data
DELETE FROM posts WHERE user_id IN (SELECT id FROM users WHERE username LIKE 'testuser%');
DELETE FROM messages WHERE sender_id IN (SELECT id FROM users WHERE username LIKE 'testuser%');
DELETE FROM users WHERE username LIKE 'testuser%';

-- Reset sequences if needed
ALTER SEQUENCE posts_id_seq RESTART WITH 1;
```

## Analysis Best Practices

### 1. Results Interpretation

#### Statistical Significance
- **Sample size**: Minimum 1000 requests per endpoint
- **Test duration**: At least 10 minutes for stable results
- **Multiple runs**: 3-5 runs for consistency validation
- **Outlier handling**: Identify and investigate anomalies

#### Trend Analysis
```
Metric Trends to Monitor:
- Response time degradation over time
- Error rate patterns (spikes vs gradual)
- Throughput stability
- Resource utilization correlation
```

### 2. Bottleneck Identification

#### Performance Correlation
```
High Response Time + High CPU = Processing bottleneck
High Response Time + Low CPU = I/O bottleneck
High Error Rate + High Memory = Memory leak
High Error Rate + Network Issues = Infrastructure problem
```

#### Root Cause Analysis
1. **Application logs**: Error messages, stack traces
2. **Database logs**: Slow queries, lock waits
3. **Server metrics**: CPU, memory, disk, network
4. **Network analysis**: Latency, packet loss

### 3. Reporting Best Practices

#### Executive Summary Format
```
Performance Test Summary - Modis Load Test
Date: [Date]
Environment: [Environment]
Test Duration: [Duration]
Peak Users: [Users]

Key Findings:
✅ Average response time: 1.2s (Target: <2s)
✅ Error rate: 0.3% (Target: <1%)
⚠️  95th percentile: 4.5s (Target: <4s)
❌ Image upload: 12s average (Target: <10s)

Recommendations:
1. Optimize image upload process
2. Investigate 95th percentile outliers
3. Monitor database query performance
```

#### Technical Details
- **Test configuration**: Users, ramp-up, duration
- **Environment details**: Server specs, network
- **Detailed metrics**: All KPIs with trends
- **Error analysis**: Types, frequencies, patterns
- **Resource utilization**: Server performance data

## Continuous Improvement

### 1. Performance Regression Testing

#### Baseline Management
```bash
# Store baseline results
cp current-results.jtl baselines/baseline-v1.2.3.jtl

# Compare with baseline
jmeter -g baseline-v1.2.3.jtl -o baseline-report/
jmeter -g current-results.jtl -o current-report/

# Automated comparison
java -cp target/modis-performance-tests-2.0.0-jar-with-dependencies.jar com.modis.performance.utils.ResultsComparator baseline-v1.2.3.jtl current-results.jtl
```

#### CI/CD Integration
```yaml
# GitHub Actions example
performance-test:
  runs-on: ubuntu-latest
  steps:
    - name: Run Performance Tests
      run: |
        jmeter -n -t modis-load-test.jmx \
          -Jusers=25 -Jduration=300 \
          -l results.jtl -e -o report/
    
    - name: Check Performance Regression
      run: |
        python check-regression.py results.jtl baseline.jtl
        if [ $? -ne 0 ]; then
          echo "Performance regression detected!"
          exit 1
        fi
```

### 2. Test Maintenance

#### Regular Updates
- **Test data refresh**: Monthly user data update
- **Scenario updates**: Match new app features
- **Environment sync**: Keep test env current
- **Baseline updates**: After major optimizations

#### Documentation Updates
- **Test procedures**: Keep instructions current
- **Known issues**: Document workarounds
- **Environment changes**: Track infrastructure updates
- **Results history**: Maintain performance trends

### 3. Team Collaboration

#### Knowledge Sharing
- **Regular reviews**: Monthly performance meetings
- **Training sessions**: JMeter best practices
- **Documentation**: Shared knowledge base
- **Tool updates**: Keep team informed of changes

#### Responsibility Matrix
```
Role                | Responsibilities
QA Engineer        | Test design, execution, analysis
DevOps Engineer    | Environment setup, monitoring
Developer          | Performance optimization, fixes
Product Manager    | Requirements, acceptance criteria
```

## Common Pitfalls to Avoid

### 1. Test Design Issues
- **Unrealistic scenarios**: Not matching actual user behavior
- **Insufficient think time**: Creating artificial load
- **Poor data management**: Using production data
- **Missing cleanup**: Leaving test artifacts

### 2. Execution Problems
- **Inadequate monitoring**: Missing performance bottlenecks
- **Wrong environment**: Testing on non-production-like setup
- **Insufficient duration**: Not allowing for warm-up/stabilization
- **Ignoring errors**: Focusing only on response times

### 3. Analysis Mistakes
- **Single metric focus**: Ignoring holistic performance view
- **No baseline comparison**: Can't identify regressions
- **Ignoring trends**: Missing gradual degradation
- **Poor communication**: Technical results without business impact

## Mobile Performance Optimization Tips

### 1. Network Optimization
- **Request batching**: Combine multiple API calls
- **Caching strategy**: Reduce redundant requests
- **Compression**: Enable gzip for all responses
- **CDN usage**: Optimize image delivery

### 2. Application Optimization
- **Lazy loading**: Load content as needed
- **Image optimization**: Multiple resolutions, formats
- **Background sync**: Minimize foreground requests
- **Connection pooling**: Reuse HTTP connections

### 3. User Experience
- **Progressive loading**: Show content incrementally
- **Offline support**: Cache critical functionality
- **Error handling**: Graceful degradation
- **Performance budgets**: Set and monitor limits
