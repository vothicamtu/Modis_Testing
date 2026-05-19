# Modis Performance Testing - Deployment Guide

## Tổng quan

Hướng dẫn triển khai hệ thống performance testing cho Modis trong môi trường production và CI/CD pipeline.

## Kiến trúc hệ thống

```
Modis Performance Testing Architecture

┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Developer     │    │   CI/CD         │    │   QA Team       │
│   Local Testing │    │   Pipeline      │    │   Manual Tests  │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────▼───────────────┐
                    │   JMeter Test Controller    │
                    │   - Test Plans              │
                    │   - Test Data               │
                    │   - Result Analysis         │
                    └─────────────┬───────────────┘
                                 │
                    ┌─────────────▼───────────────┐
                    │   Target Environment        │
                    │   ┌─────────────────────┐   │
                    │   │ Modis Backend API   │   │
                    │   │ - Authentication    │   │
                    │   │ - Social Features   │   │
                    │   │ - Real-time Chat    │   │
                    │   │ - Image Upload      │   │
                    │   └─────────────────────┘   │
                    │   ┌─────────────────────┐   │
                    │   │ External Services   │   │
                    │   │ - Cloudinary        │   │
                    │   │ - WebSocket Broker  │   │
                    │   │ - Database          │   │
                    │   └─────────────────────┘   │
                    └─────────────────────────────┘
```

## Môi trường triển khai

### 1. Development Environment

#### Local Setup
```bash
# Clone repository
git clone <repository-url>
cd testing/performance/jmeter

# Setup environment
chmod +x scripts/setup-environment.sh
./scripts/setup-environment.sh

# Run quick test
./quick-test.sh
```

#### Requirements
- **OS**: Windows 10+, macOS 10.15+, Ubuntu 18.04+
- **Java**: JDK 11 hoặc cao hơn
- **RAM**: Minimum 4GB, Recommended 8GB+
- **Storage**: 2GB free space cho JMeter và results
- **Network**: Stable internet connection

### 2. CI/CD Environment

#### GitHub Actions Setup
```yaml
# File: .github/workflows/performance-tests.yml
name: Performance Tests
on:
  schedule:
    - cron: '0 2 * * 1'  # Weekly
  workflow_dispatch:     # Manual trigger
  pull_request:          # PR validation
```

#### Required Secrets
```bash
# GitHub Repository Secrets
SLACK_WEBHOOK_URL=https://hooks.slack.com/services/...
PERFORMANCE_TEST_TOKEN=<api-token-for-auth>
```

#### Environment Variables
```bash
# CI Environment
MODIS_BASE_URL=https://modis-backend-staging.onrender.com
MODIS_PROD_URL=https://modis-backend.onrender.com
JMETER_HEAP_SIZE=2g
```

### 3. Production Testing Environment

#### Dedicated Test Infrastructure
```yaml
# Docker Compose for JMeter Cluster
version: '3.8'
services:
  jmeter-master:
    image: justb4/jmeter:5.6.3
    volumes:
      - ./test-plans:/test-plans
      - ./results:/results
    command: >
      jmeter -n -t /test-plans/modis-load-test.jmx
      -R jmeter-slave1,jmeter-slave2
      -l /results/distributed-test.jtl
      
  jmeter-slave1:
    image: justb4/jmeter:5.6.3
    command: jmeter-server
    
  jmeter-slave2:
    image: justb4/jmeter:5.6.3
    command: jmeter-server
```

## Triển khai từng bước

### Phase 1: Basic Setup (Week 1)

#### Day 1-2: Environment Setup
```bash
# 1. Install JMeter on test machines
wget https://downloads.apache.org//jmeter/binaries/apache-jmeter-5.6.3.zip
unzip apache-jmeter-5.6.3.zip
export PATH=$PATH:/opt/apache-jmeter-5.6.3/bin

# 2. Setup test data
cd testing/performance/jmeter
./scripts/setup-environment.sh

# 3. Validate basic functionality
./quick-test.sh
```

#### Day 3-4: CI/CD Integration
```bash
# 1. Setup GitHub Actions
cp .github/workflows/performance-tests.yml .github/workflows/

# 2. Configure secrets
gh secret set SLACK_WEBHOOK_URL --body "https://hooks.slack.com/..."

# 3. Test CI pipeline
git push origin feature/performance-testing
```

#### Day 5: Documentation & Training
- Team training session
- Documentation review
- Process establishment

### Phase 2: Advanced Features (Week 2)

#### Day 1-2: Distributed Testing
```bash
# Setup JMeter cluster
docker-compose up -d

# Run distributed test
jmeter -n -t test-plans/modis-stress-test.jmx \
  -R slave1,slave2,slave3 \
  -l results/distributed-stress.jtl
```

#### Day 3-4: Monitoring Integration
```bash
# Setup Grafana dashboard
docker run -d -p 3000:3000 grafana/grafana

# Configure JMeter metrics
jmeter -Jjmeter.reportgenerator.overall_granularity=1000 \
  -n -t test-plans/modis-load-test.jmx
```

#### Day 5: Performance Baselines
```bash
# Establish baselines
java -cp target/modis-performance-tests-2.0.0-jar-with-dependencies.jar com.modis.performance.utils.ResultsComparator \
  baseline.jtl current.jtl \
  --json-output baseline-report.json
```

### Phase 3: Production Integration (Week 3)

#### Day 1-2: Production Testing
```bash
# Production-safe testing
jmeter -n -t test-plans/modis-load-test.jmx \
  -Jusers=10 \
  -Jduration=300 \
  -Jbase_url=https://modis-backend.onrender.com
```

#### Day 3-4: Automated Reporting
```bash
# Setup automated reports
crontab -e
# Add: 0 2 * * 1 /path/to/run-weekly-tests.sh
```

#### Day 5: Team Handover
- Final documentation
- Process handover
- Support procedures

## Configuration Management

### 1. Environment-specific Configurations

#### Staging Environment
```properties
# configs/staging.properties
modis.base.url=https://modis-backend-staging.onrender.com
load.test.users=25
stress.test.users=100
spike.test.users=250
test.duration=300
```

#### Production Environment
```properties
# configs/production.properties
modis.base.url=https://modis-backend.onrender.com
load.test.users=50
stress.test.users=200
spike.test.users=500
test.duration=600
```

### 2. Test Data Management

#### Data Refresh Strategy
```bash
#!/bin/bash
# scripts/refresh-test-data.sh

# Generate fresh test users
python scripts/generate-test-users.py --count 100 --output test-data/users.csv

# Update post content with current trends
python scripts/update-post-content.py --source social-trends.json

# Validate data integrity
python scripts/validate-test-data.py test-data/
```

#### Data Cleanup
```bash
#!/bin/bash
# scripts/cleanup-test-data.sh

# Remove test data from database
psql -h $DB_HOST -U $DB_USER -d $DB_NAME << EOF
DELETE FROM posts WHERE user_id IN (
  SELECT id FROM users WHERE username LIKE 'testuser%'
);
DELETE FROM users WHERE username LIKE 'testuser%';
EOF
```

## Monitoring và Alerting

### 1. Real-time Monitoring

#### JMeter Metrics
```bash
# Enable JMeter metrics export
export JVM_ARGS="-Djmeter.reportgenerator.overall_granularity=1000"

# Monitor test execution
tail -f jmeter.log | grep -E "(ERROR|WARN|FATAL)"
```

#### System Metrics
```bash
# Monitor system resources
htop
iotop
nethogs
```

### 2. Automated Alerting

#### Slack Integration
```python
# scripts/slack-notifier.py
import requests
import json

def send_alert(webhook_url, message):
    payload = {
        "text": f"🚨 Modis Performance Alert: {message}",
        "channel": "#performance-alerts"
    }
    requests.post(webhook_url, json=payload)
```

#### Email Notifications
```bash
# Setup email alerts
echo "Performance test failed" | mail -s "Modis Performance Alert" team@company.com
```

## Security Considerations

### 1. Test Data Security

#### Sensitive Data Handling
```bash
# Encrypt sensitive test data
gpg --symmetric --cipher-algo AES256 test-data/users.csv
rm test-data/users.csv

# Decrypt for testing
gpg --decrypt test-data/users.csv.gpg > test-data/users.csv
```

#### Access Control
```bash
# Restrict access to test files
chmod 600 test-data/*.csv
chown testuser:testgroup test-data/
```

### 2. Network Security

#### VPN Requirements
- All production testing through VPN
- Whitelist test IPs on firewall
- Use secure protocols (HTTPS, WSS)

#### API Security
```bash
# Use environment-specific API keys
export MODIS_API_KEY=$STAGING_API_KEY  # For staging
export MODIS_API_KEY=$PROD_API_KEY     # For production
```

## Troubleshooting

### 1. Common Issues

#### JMeter Memory Issues
```bash
# Increase heap size
export JVM_ARGS="-Xms2g -Xmx8g"

# Monitor memory usage
jstat -gc $(pgrep java) 5s
```

#### Network Connectivity
```bash
# Test connectivity
curl -I https://modis-backend.onrender.com/actuator/health

# Check DNS resolution
nslookup modis-backend.onrender.com

# Test WebSocket connection
wscat -c wss://modis-backend.onrender.com/ws
```

#### Test Data Issues
```bash
# Validate CSV format
python -c "import csv; csv.Sniffer().sniff(open('test-data/users.csv').read())"

# Check data integrity
wc -l test-data/*.csv
head -5 test-data/users.csv
```

### 2. Performance Issues

#### Slow Test Execution
```bash
# Profile JMeter performance
jmeter -n -t test-plan.jmx -Jjmeter.save.saveservice.thread_counts=true

# Optimize test plan
# - Reduce listeners in non-GUI mode
# - Use CSV datasets efficiently
# - Minimize response data saving
```

#### High Error Rates
```bash
# Analyze error patterns
grep "false" results/test-results.jtl | cut -d',' -f4 | sort | uniq -c

# Check server logs
tail -f /var/log/modis/application.log | grep ERROR
```

## Maintenance Procedures

### 1. Regular Maintenance

#### Weekly Tasks
```bash
#!/bin/bash
# scripts/weekly-maintenance.sh

# Update test data
./scripts/refresh-test-data.sh

# Clean old results
find results/ -name "*.jtl" -mtime +30 -delete
find reports/ -type d -mtime +30 -exec rm -rf {} +

# Update baselines if needed
if [ -f "results/latest-production.jtl" ]; then
  cp results/latest-production.jtl baselines/baseline-$(date +%Y%m%d).jtl
fi
```

#### Monthly Tasks
```bash
#!/bin/bash
# scripts/monthly-maintenance.sh

# Update JMeter version
wget https://downloads.apache.org//jmeter/binaries/apache-jmeter-latest.zip

# Review and update test scenarios
python scripts/analyze-usage-patterns.py --update-scenarios

# Performance trend analysis
python scripts/generate-trend-report.py --period 30days
```

### 2. Backup Procedures

#### Test Assets Backup
```bash
#!/bin/bash
# scripts/backup-test-assets.sh

BACKUP_DIR="/backup/performance-testing/$(date +%Y%m%d)"
mkdir -p $BACKUP_DIR

# Backup test plans
tar -czf $BACKUP_DIR/test-plans.tar.gz test-plans/

# Backup test data
tar -czf $BACKUP_DIR/test-data.tar.gz test-data/

# Backup baselines
tar -czf $BACKUP_DIR/baselines.tar.gz baselines/

# Backup configurations
tar -czf $BACKUP_DIR/configs.tar.gz configs/
```

## Support và Documentation

### 1. Team Responsibilities

#### QA Team
- Daily test execution monitoring
- Weekly performance reviews
- Monthly baseline updates
- Incident response

#### DevOps Team
- Infrastructure maintenance
- CI/CD pipeline updates
- Monitoring setup
- Capacity planning

#### Development Team
- Performance optimization
- Test scenario updates
- Bug fixes
- Feature testing

### 2. Documentation Maintenance

#### Living Documentation
```bash
# Auto-generate API documentation
swagger-codegen generate -i api-spec.yaml -l html2 -o docs/api/

# Update performance baselines documentation
python scripts/update-baseline-docs.py

# Generate team runbooks
python scripts/generate-runbooks.py --output docs/runbooks/
```

#### Knowledge Base
- Performance testing procedures
- Troubleshooting guides
- Best practices
- Lessons learned

## Success Metrics

### 1. Testing Coverage
- **API Coverage**: 95% of endpoints tested
- **User Scenarios**: 90% of user journeys covered
- **Load Patterns**: All expected load patterns tested

### 2. Quality Metrics
- **Test Reliability**: 99% test success rate
- **Detection Rate**: 95% of performance issues caught
- **Response Time**: < 24 hours for critical issues

### 3. Process Metrics
- **Automation Rate**: 90% of tests automated
- **CI Integration**: 100% of releases tested
- **Team Adoption**: 100% team trained and using tools

## Continuous Improvement

### 1. Regular Reviews
- Monthly performance review meetings
- Quarterly process improvement sessions
- Annual tool and technology updates

### 2. Feedback Loop
- Developer feedback integration
- User experience correlation
- Production monitoring alignment

### 3. Innovation
- New testing techniques evaluation
- Tool upgrades and migrations
- Industry best practices adoption
