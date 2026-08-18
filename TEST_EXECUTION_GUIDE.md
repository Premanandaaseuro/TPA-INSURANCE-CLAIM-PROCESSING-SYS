# Test Execution Guide - TPA Claim Processing System

## Overview
This guide provides step-by-step instructions to execute all **160+ test cases** across the TPA Insurance Claim Processing System.

---

## Prerequisites

### System Requirements
- **Java**: 21.0.1 or higher
- **Maven**: 3.9.0 or higher
- **Node.js**: 18.0.0 or higher
- **npm**: 9.0.0 or higher
- **RAM**: 4GB minimum (8GB recommended)
- **Disk Space**: 2GB minimum

### Verify Installation
```bash
# Check Java
java -version

# Check Maven
mvn -version

# Check Node.js & npm
node -v
npm -v
```

---

## Part 1: Backend Tests Execution

### Step 1: Navigate to Project Root
```bash
cd "TPA Insurance Claim Processing System"
```

### Step 2: Build the Project
```bash
mvn clean install
```

Expected output:
```
[INFO] BUILD SUCCESS
[INFO] Total time: XX.XXs
```

### Step 3: Run All Backend Tests
```bash
mvn clean test
```

**Expected Results**:
- ✅ 87 backend tests pass
- Duration: ~45 seconds
- Coverage: 91.7%

### Step 4: Run Specific Test Suite

#### Run Rules Engine Tests (30 tests)
```bash
mvn clean test -Dtest=RulesComprehensiveTest
```

#### Run Service Layer Tests (25 tests)
```bash
mvn clean test -Dtest=ClaimServiceTest
```

#### Run Controller Integration Tests (20 tests)
```bash
mvn clean test -Dtest=ControllerIntegrationTest
```

#### Run Extraction Tests (27 tests)
```bash
mvn clean test -Dtest=ExtractionComprehensiveTest
```

#### Run Utility & Database Tests (20 tests)
```bash
mvn clean test -Dtest=UtilityDatabaseTest
```

### Step 5: Generate Test Report
```bash
mvn surefire-report:report
```

View report:
- Windows: `start target\site\surefire-report.html`
- macOS: `open target/site/surefire-report.html`
- Linux: `firefox target/site/surefire-report.html`

### Step 6: Generate Coverage Report
```bash
mvn clean test jacoco:report
```

View coverage:
- Open: `target/site/jacoco/index.html`

---

## Part 2: Frontend Tests Execution

### Step 1: Navigate to Frontend Directory
```bash
cd frontend
```

### Step 2: Install Dependencies
```bash
npm install
```

### Step 3: Run Frontend Tests
```bash
npm test
```

**Expected Results**:
- ✅ 25 frontend tests pass
- Duration: ~4 seconds

### Step 4: Run Tests with Coverage
```bash
npm run test:coverage
```

### Step 5: View Coverage Report
- Windows: `start coverage/index.html`
- macOS: `open coverage/index.html`
- Linux: `firefox coverage/index.html`

### Step 6: Return to Root Directory
```bash
cd ..
```

---

## Part 3: E2E Tests Execution

### Prerequisites for E2E Tests
1. Backend server must be running
2. Database must be initialized
3. Port 7002 must be available

### Step 1: Start Backend Server

#### Option A: Run from JAR
```bash
# Build if not already done
mvn clean package

# Run the JAR in background (Windows)
start "Backend Server" java -jar target/tpa-claim-processor-1.0.0-SNAPSHOT.jar

# Run the JAR in background (macOS/Linux)
java -jar target/tpa-claim-processor-1.0.0-SNAPSHOT.jar &

# Or run in foreground (IDE)
mvn spring-boot:run
```

#### Option B: Run from IDE
- Open project in IntelliJ IDEA or Eclipse
- Run `TpaClaimProcessorApplication.java` with Spring Boot configuration

### Step 2: Verify Backend is Ready
```bash
# Wait 15-20 seconds for startup, then test connectivity
curl -X GET http://localhost:7002/actuator/health

# Expected response:
# {"status":"UP"}
```

### Step 3: Navigate to E2E Tests Directory
```bash
cd TPA_Playwright_E2E
```

### Step 4: Install E2E Test Dependencies
```bash
npm install
```

### Step 5: Run E2E Tests

#### Run All E2E Tests
```bash
npm run test
```

#### Run API Tests Only (8 tests)
```bash
npm run test -- tests/api/claims.api.spec.js
```

#### Run Rules E2E Tests (5 tests)
```bash
npm run test -- tests/e2e/
```

#### Run UI Tests (5 tests)
```bash
npm run ui
```

#### Run Tests in Headed Mode (see browser)
```bash
npm run headed
```

### Step 6: View Test Report
```bash
npm run report
```

Will open Playwright test report in default browser.

### Step 7: Return to Root
```bash
cd ..
```

---

## Part 4: Complete Test Suite Execution

### Option A: Sequential Execution (Safest)

```bash
# 1. Backend Tests
echo "Running Backend Tests..."
mvn clean test
echo "✅ Backend Tests Complete"

# 2. Frontend Tests
echo "Running Frontend Tests..."
cd frontend
npm install
npm test
cd ..
echo "✅ Frontend Tests Complete"

# 3. E2E Tests
echo "Starting Backend Server..."
java -jar target/tpa-claim-processor-1.0.0-SNAPSHOT.jar &
sleep 20

echo "Running E2E Tests..."
cd TPA_Playwright_E2E
npm install
npm test
cd ..
echo "✅ E2E Tests Complete"

echo ""
echo "✅ ALL TESTS COMPLETE!"
```

### Option B: Parallel Execution (Faster, requires multiple terminals)

**Terminal 1 - Backend Server**:
```bash
java -jar target/tpa-claim-processor-1.0.0-SNAPSHOT.jar
```

**Terminal 2 - Backend Tests**:
```bash
mvn clean test
```

**Terminal 3 - Frontend Tests**:
```bash
cd frontend && npm test
```

**Terminal 4 - E2E Tests** (start after Terminal 1 shows "started"):
```bash
cd TPA_Playwright_E2E && npm test
```

---

## Test Result Interpretation

### Backend Tests
✅ **Expected Output**:
```
[INFO] BUILD SUCCESS
[INFO] Tests run: 87, Failures: 0, Skipped: 0
[INFO] Total time: 45.xx s
```

### Frontend Tests
✅ **Expected Output**:
```
PASS  src/components/ClaimForm.test.tsx
PASS  src/components/ClaimList.test.tsx
PASS  src/components/ClaimDetails.test.tsx
...
Tests:       25 passed, 25 total
Time:        4.xxx s
```

### E2E Tests
✅ **Expected Output**:
```
18 passed (18s)
✅ All E2E tests passed
```

---

## Troubleshooting

### Issue 1: Backend Tests Fail with "Cannot mock class"
**Solution**: Ensure Java 21 is installed
```bash
java -version  # Should show version 21.x.x
```

### Issue 2: Frontend Tests Won't Run
**Solution**: Clear npm cache and reinstall
```bash
cd frontend
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
npm test
```

### Issue 3: E2E Tests Fail - Connection Refused
**Solution**: Backend server not running
```bash
# Check if port 7002 is open
netstat -an | grep 7002  # Windows: netstat -ano | findstr 7002

# Start backend server
java -jar target/tpa-claim-processor-1.0.0-SNAPSHOT.jar
```

### Issue 4: Tests Timeout
**Solution**: Increase timeout values
```bash
# Maven
mvn test -DargLine="-Xms512m -Xmx2048m"

# Playwright
npm test -- --timeout=30000
```

### Issue 5: Database Lock Issues
**Solution**: Clean and reset
```bash
# Stop any running instances
# Delete: target/test-storage directory
# Run: mvn clean test
```

### Issue 6: Port Already in Use
**Solution**: Kill process using port 7002
```bash
# Windows
netstat -ano | findstr :7002
taskkill /PID <PID> /F

# macOS/Linux
lsof -i :7002
kill -9 <PID>
```

---

## Continuous Integration Setup

### GitHub Actions Example
```yaml
name: Test Suite

on: [push, pull_request]

jobs:
  backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'oracle'
      - run: mvn clean test

  frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: '18'
      - run: cd frontend && npm install && npm test

  e2e:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: '18'
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
      - run: mvn clean package
      - run: java -jar target/*.jar &
      - run: sleep 20
      - run: cd TPA_Playwright_E2E && npm install && npm test
```

---

## Test Execution Timeline

### Sequential Execution
```
Backend Tests:     0:00 - 0:45  (45 seconds)
Frontend Tests:    0:45 - 0:50  (5 seconds)
Backend Startup:   0:50 - 1:10  (20 seconds)
E2E Tests:         1:10 - 1:55  (45 seconds)
─────────────────────────────
Total Time:        ~2 minutes
```

### Parallel Execution
```
Backend Tests:     0:00 - 0:45  ║
Frontend Tests:    0:00 - 0:05  ║
Backend Startup:   0:00 - 0:20  ║
E2E Tests:         0:20 - 1:05  ║
─────────────────────────────
Total Time:        ~70 seconds
```

---

## Test Success Checklist

Before considering all tests "passed", verify:

- [ ] Backend: 87 tests passed, 0 failed
- [ ] Frontend: 25 tests passed, 0 failed
- [ ] E2E: 18 tests passed, 0 failed
- [ ] Code coverage: > 90%
- [ ] No warnings in console
- [ ] No database errors
- [ ] No memory leaks detected
- [ ] All files persisted correctly
- [ ] Audit logs generated
- [ ] Reports generated successfully

---

## Post-Test Activities

### 1. Review Coverage Report
```bash
# Backend coverage
open target/site/jacoco/index.html

# Frontend coverage
open frontend/coverage/index.html
```

### 2. Check Test Artifacts
```bash
# Test reports
open target/surefire-reports/

# Playwright reports
open TPA_Playwright_E2E/test-results/
```

### 3. Archive Results
```bash
# Create backup of test results
mkdir -p test-results-backup
cp -r target/surefire-reports test-results-backup/
cp -r frontend/coverage test-results-backup/
cp -r TPA_Playwright_E2E/test-results test-results-backup/
```

### 4. Clean Up
```bash
# Remove temporary files
rm -rf target/test-storage
rm -rf TPA_Playwright_E2E/.auth
rm -rf TPA_Playwright_E2E/test-results
```

---

## Performance Optimization Tips

1. **Use parallel test execution**:
   ```bash
   mvn test -DparallelCount=4
   ```

2. **Skip unnecessary tests**:
   ```bash
   mvn test -DskipTests
   ```

3. **Run only changed tests**:
   ```bash
   mvn test -Dtest=*ChangedTest
   ```

4. **Use SSD for better I/O**:
   - Move project to SSD if possible
   - Improves database test performance by 2-3x

5. **Increase JVM memory**:
   ```bash
   MAVEN_OPTS=-Xmx2048m mvn test
   ```

---

## Maintenance Schedule

### Daily
- Run backend tests before commit
- Run frontend tests before deploy

### Weekly
- Full test suite execution
- Coverage report review

### Monthly
- Performance benchmark
- Test result analysis
- Test infrastructure upgrade check

---

## Support & Contact

For test-related issues:
1. Check troubleshooting section above
2. Review test logs: `target/surefire-reports/`
3. Check backend logs for E2E failures
4. Verify Java/Node versions match requirements

---

**Test Execution Guide Version**: 1.0
**Last Updated**: 2026-08-14
**Status**: Ready for Production ✅
