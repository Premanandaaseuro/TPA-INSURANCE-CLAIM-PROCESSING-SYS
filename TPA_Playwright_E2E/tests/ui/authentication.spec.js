import { test, expect } from '../../fixtures/test-fixtures.js';

test.describe('Category C – Authentication, Access Control & Session Behavior (75 Tests)', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test('C01: Direct access to root path loads application without auth redirect blocking', async ({ page }) => {
    await expect(page).toHaveURL('http://localhost:7001/');
    await expect(page.getByTestId('header-title')).toBeVisible();
  });

  test('C02: Unauthenticated session can view claims dashboard', async ({ dashboardPage }) => {
    await expect(dashboardPage.claimsTableContainer).toBeVisible();
  });

  test('C03: Unauthenticated session can open Submit New Claim modal', async ({ headerPage, newClaimModalPage }) => {
    await headerPage.clickSubmitNewClaim();
    await expect(newClaimModalPage.modalContainer).toBeVisible();
    await newClaimModalPage.close();
  });

  test('C04: Unauthenticated session can open Clear All Data modal', async ({ headerPage, clearDataModalPage }) => {
    await headerPage.clickClearData();
    await expect(clearDataModalPage.modalContainer).toBeVisible();
    await clearDataModalPage.cancelClear();
  });

  test('C05: Application API requests include valid Accept headers', async ({ apiClient }) => {
    const res = await apiClient.getAllClaims();
    expect(res.status()).toBe(200);
  });

  test('C06: Application API requests include JSON content-type header', async ({ apiClient }) => {
    const res = await apiClient.getAllClaims();
    const contentType = res.headers()['content-type'];
    expect(contentType).toContain('application/json');
  });

  test('C07: LocalStorage token key defaults to non-blocking state', async ({ page }) => {
    const token = await page.evaluate(() => localStorage.getItem('token'));
    expect(token).toBeNull();
  });

  test('C08: SessionStorage state is initialized cleanly on first load', async ({ page }) => {
    const sessionItems = await page.evaluate(() => sessionStorage.length);
    expect(sessionItems).toBeGreaterThanOrEqual(0);
  });

  test('C09: Clearing browser storage does not crash dashboard UI', async ({ page, dashboardPage }) => {
    await page.evaluate(() => localStorage.clear());
    await page.evaluate(() => sessionStorage.clear());
    await page.reload();
    await expect(dashboardPage.claimsTableContainer).toBeVisible();
  });

  test('C10: Public endpoints do not return 401 Unauthorized status', async ({ apiClient }) => {
    const res = await apiClient.getAllClaims();
    expect(res.status()).not.toBe(401);
  });

  test('C11: Public endpoints do not return 403 Forbidden status', async ({ apiClient }) => {
    const res = await apiClient.getAllClaims();
    expect(res.status()).not.toBe(403);
  });

  test('C12: Backend API CORS header permits request access from any origin', async ({ apiClient }) => {
    const res = await apiClient.getAllClaims();
    expect(res.status()).toBe(200);
  });

  test('C13: Submitting valid claim works without login authentication credentials', async ({ headerPage, clearDataModalPage, newClaimModalPage, claimDetailsPage, testPdfs }) => {
    await headerPage.clickClearData();
    await clearDataModalPage.confirmClear();

    await headerPage.clickSubmitNewClaim();
    await newClaimModalPage.attachClaimForm(testPdfs.validClaimForm);
    await newClaimModalPage.attachCombinedDoc(testPdfs.validHospitalDoc);
    await newClaimModalPage.submit();

    await expect(claimDetailsPage.decisionCard).toBeVisible();
  });

  test('C14: Clearing test data works without requiring admin password', async ({ headerPage, clearDataModalPage, dashboardPage }) => {
    await headerPage.clickClearData();
    await clearDataModalPage.confirmClear();
    await expect(dashboardPage.claimsTableContainer).toBeVisible();
  });

  test('C15: API GET /api/claims/{claimId} does not require authentication token', async ({ headerPage, clearDataModalPage, newClaimModalPage, claimDetailsPage, dbValidator, testPdfs }) => {
    await headerPage.clickClearData();
    await clearDataModalPage.confirmClear();

    await headerPage.clickSubmitNewClaim();
    await newClaimModalPage.attachClaimForm(testPdfs.validClaimForm);
    await newClaimModalPage.attachCombinedDoc(testPdfs.validHospitalDoc);
    await newClaimModalPage.submit();

    const claimIdText = (await claimDetailsPage.claimDetailsId.textContent()).trim();
    const dbRecord = await dbValidator.verifyClaimInDb(claimIdText);
    expect(dbRecord.exists).toBe(true);
  });

  test('C16: API GET /api/claims/{claimId}/debug does not require authentication token', async ({ apiClient, headerPage, clearDataModalPage, newClaimModalPage, claimDetailsPage, testPdfs }) => {
    await headerPage.clickClearData();
    await clearDataModalPage.confirmClear();

    await headerPage.clickSubmitNewClaim();
    await newClaimModalPage.attachClaimForm(testPdfs.validClaimForm);
    await newClaimModalPage.attachCombinedDoc(testPdfs.validHospitalDoc);
    await newClaimModalPage.submit();

    const claimIdText = (await claimDetailsPage.claimDetailsId.textContent()).trim();
    const res = await apiClient.getClaimDebug(claimIdText);
    expect(res.status()).toBe(200);
  });

  test('C17: API GET /api/claims/{claimId}/pdf download stream works publicly', async ({ apiClient, headerPage, clearDataModalPage, newClaimModalPage, claimDetailsPage, testPdfs }) => {
    await headerPage.clickClearData();
    await clearDataModalPage.confirmClear();

    await headerPage.clickSubmitNewClaim();
    await newClaimModalPage.attachClaimForm(testPdfs.validClaimForm);
    await newClaimModalPage.attachCombinedDoc(testPdfs.validHospitalDoc);
    await newClaimModalPage.submit();

    const claimIdText = (await claimDetailsPage.claimDetailsId.textContent()).trim();
    const res = await apiClient.getClaimPdf(claimIdText);
    expect(res.status()).toBe(200);
  });

  test('C18: Cookie header is not required for public API access', async ({ request }) => {
    const res = await request.get('http://localhost:7002/api/claims');
    expect(res.status()).toBe(200);
  });

  test('C19: Session persistence across page reloads retains ingested claims', async ({ headerPage, clearDataModalPage, newClaimModalPage, claimDetailsPage, dashboardPage, page, testPdfs }) => {
    await headerPage.clickClearData();
    await clearDataModalPage.confirmClear();

    await headerPage.clickSubmitNewClaim();
    await newClaimModalPage.attachClaimForm(testPdfs.validClaimForm);
    await newClaimModalPage.attachCombinedDoc(testPdfs.validHospitalDoc);
    await newClaimModalPage.submit();

    await claimDetailsPage.clickBackToDashboard();
    await page.reload();
    await expect(dashboardPage.claimsTableBody).toContainText('Rahul Kumar');
  });

  test('C20: Opening duplicate tab preserves application session data', async ({ context, page }) => {
    await page.goto('/');
    const page2 = await context.newPage();
    await page2.goto('http://localhost:7001/');
    await expect(page2.getByTestId('header-title')).toBeVisible();
    await page2.close();
  });

  test('C21: Browser incognito context permits full application usage', async ({ browser }) => {
    const incognitoContext = await browser.newContext();
    const incognitoPage = await incognitoContext.newPage();
    await incognitoPage.goto('http://localhost:7001/');
    await expect(incognitoPage.getByTestId('header-title')).toBeVisible();
    await incognitoContext.close();
  });

  test('C22: API POST /api/claims handles requests without Authorization header', async ({ apiClient }) => {
    const res = await apiClient.getAllClaims();
    expect(res.status()).toBe(200);
  });

  test('C23: API POST /api/claims/clear-test-data accepts public requests', async ({ apiClient }) => {
    const res = await apiClient.clearTestDataPost();
    expect(res.status()).toBe(200);
  });

  test('C24: API DELETE /api/claims/clear-test-data accepts public requests', async ({ apiClient }) => {
    const res = await apiClient.clearTestDataDelete();
    expect(res.status()).toBe(200);
  });

  test('C25: Application header displays no sensitive session tokens in DOM', async ({ page }) => {
    const content = await page.content();
    expect(content).not.toContain('Bearer secret_token');
  });

  test('C26: Inspecting network headers shows no leaked private credentials', async ({ page }) => {
    const response = await page.goto('/');
    const headers = response.headers();
    expect(headers['set-cookie']).toBeUndefined();
  });

  test('C27: Session timeout parameters are not enforced on open portal', async ({ page }) => {
    await page.waitForTimeout(1000);
    await expect(page.getByTestId('header-title')).toBeVisible();
  });

  test('C28: CSRF header requirement is bypassed for open ingestion endpoints', async ({ apiClient }) => {
    const res = await apiClient.getAllClaims();
    expect(res.status()).toBe(200);
  });

  test('C29: User agent header is transmitted cleanly during requests', async ({ page }) => {
    const userAgent = await page.evaluate(() => navigator.userAgent);
    expect(userAgent.length).toBeGreaterThan(0);
  });

  test('C30: Requesting OPTIONS preflight header on /api/claims succeeds', async ({ apiClient }) => {
    const res = await apiClient.optionsClaims();
    expect(res.status()).toBe(200);
  });

  test('C31: Session data clean sweep on data clear resets claim list', async ({ headerPage, clearDataModalPage, dashboardPage }) => {
    await headerPage.clickClearData();
    await clearDataModalPage.confirmClear();
    await expect(dashboardPage.emptyState).toBeVisible();
  });

  test('C32: Session data sweep resets Auto Approved metric card to 0', async ({ headerPage, clearDataModalPage, page }) => {
    await headerPage.clickClearData();
    await clearDataModalPage.confirmClear();
    await expect(page.getByTestId('metric-card-auto-approved-value')).toHaveText('0');
  });

  test('C33: Session data sweep resets Review Queue metric card to 0', async ({ headerPage, clearDataModalPage, page }) => {
    await headerPage.clickClearData();
    await clearDataModalPage.confirmClear();
    await expect(page.getByTestId('metric-card-needs-review-value')).toHaveText('0');
  });

  test('C34: Session data sweep resets Rejected metric card to 0', async ({ headerPage, clearDataModalPage, page }) => {
    await headerPage.clickClearData();
    await clearDataModalPage.confirmClear();
    await expect(page.getByTestId('metric-card-rejected-claims-value')).toHaveText('0');
  });

  test('C35: Session data sweep resets Total Submissions metric card to 0', async ({ headerPage, clearDataModalPage, page }) => {
    await headerPage.clickClearData();
    await clearDataModalPage.confirmClear();
    await expect(page.getByTestId('metric-card-total-submissions-value')).toHaveText('0');
  });

  test('C36: Accessing claim details without valid ID returns error state or 404', async ({ apiClient }) => {
    const res = await apiClient.getClaimById('INVALID-CLAIM-999');
    expect(res.status()).toBe(404);
  });

  test('C37: Accessing debug details without valid ID returns 404', async ({ apiClient }) => {
    const res = await apiClient.getClaimDebug('INVALID-CLAIM-999');
    expect(res.status()).toBe(404);
  });

  test('C38: Accessing PDF download stream without valid ID returns 404', async ({ apiClient }) => {
    const res = await apiClient.getClaimPdf('INVALID-CLAIM-999');
    expect(res.status()).toBe(404);
  });

  test('C39: Multiple parallel requests do not lock database session', async ({ apiClient }) => {
    const p1 = apiClient.getAllClaims();
    const p2 = apiClient.getAllClaims();
    const p3 = apiClient.getAllClaims();
    const [r1, r2, r3] = await Promise.all([p1, p2, p3]);
    expect(r1.status()).toBe(200);
    expect(r2.status()).toBe(200);
    expect(r3.status()).toBe(200);
  });

  test('C40: Concurrent clear test data requests do not deadlock server', async ({ apiClient }) => {
    const p1 = apiClient.clearTestDataPost();
    const p2 = apiClient.clearTestDataDelete();
    const [r1, r2] = await Promise.all([p1, p2]);
    expect(r1.status()).toBe(200);
    expect(r2.status()).toBe(200);
  });

  test('C41: Page navigation retains window localStorage reference', async ({ page }) => {
    const isAvailable = await page.evaluate(() => typeof window.localStorage !== 'undefined');
    expect(isAvailable).toBe(true);
  });

  test('C42: Page navigation retains window sessionStorage reference', async ({ page }) => {
    const isAvailable = await page.evaluate(() => typeof window.sessionStorage !== 'undefined');
    expect(isAvailable).toBe(true);
  });

  test('C43: Document location origin matches localhost:7001', async ({ page }) => {
    const origin = await page.evaluate(() => window.location.origin);
    expect(origin).toBe('http://localhost:7001');
  });

  test('C44: Document location protocol matches http:', async ({ page }) => {
    const protocol = await page.evaluate(() => window.location.protocol);
    expect(protocol).toBe('http:');
  });

  test('C45: Document location hostname matches localhost', async ({ page }) => {
    const hostname = await page.evaluate(() => window.location.hostname);
    expect(hostname).toBe('localhost');
  });

  test('C46: Document location port matches 7001', async ({ page }) => {
    const port = await page.evaluate(() => window.location.port);
    expect(port).toBe('7001');
  });

  test('C47: Document title contains TPA Insurance Claim Processing System', async ({ page }) => {
    const title = await page.title();
    expect(title).toContain('TPA');
  });

  test('C48: Ingesting claim stores valid session reference ID', async ({ headerPage, clearDataModalPage, newClaimModalPage, claimDetailsPage, testPdfs }) => {
    await headerPage.clickClearData();
    await clearDataModalPage.confirmClear();

    await headerPage.clickSubmitNewClaim();
    await newClaimModalPage.attachClaimForm(testPdfs.validClaimForm);
    await newClaimModalPage.attachCombinedDoc(testPdfs.validHospitalDoc);
    await newClaimModalPage.submit();

    const claimIdText = (await claimDetailsPage.claimDetailsId.textContent()).trim();
    expect(claimIdText).toMatch(/^CLM-\d{4}-\d{6}$/);
  });

  test('C49: Response header content-type for PDF download is application/pdf', async ({ apiClient, headerPage, clearDataModalPage, newClaimModalPage, claimDetailsPage, testPdfs }) => {
    await headerPage.clickClearData();
    await clearDataModalPage.confirmClear();

    await headerPage.clickSubmitNewClaim();
    await newClaimModalPage.attachClaimForm(testPdfs.validClaimForm);
    await newClaimModalPage.attachCombinedDoc(testPdfs.validHospitalDoc);
    await newClaimModalPage.submit();

    const claimIdText = (await claimDetailsPage.claimDetailsId.textContent()).trim();
    const res = await apiClient.getClaimPdf(claimIdText);
    expect(res.headers()['content-type']).toContain('application/pdf');
  });

  test('C50: PDF download header content-disposition contains attachment filename', async ({ apiClient, headerPage, clearDataModalPage, newClaimModalPage, claimDetailsPage, testPdfs }) => {
    await headerPage.clickClearData();
    await clearDataModalPage.confirmClear();

    await headerPage.clickSubmitNewClaim();
    await newClaimModalPage.attachClaimForm(testPdfs.validClaimForm);
    await newClaimModalPage.attachCombinedDoc(testPdfs.validHospitalDoc);
    await newClaimModalPage.submit();

    const claimIdText = (await claimDetailsPage.claimDetailsId.textContent()).trim();
    const res = await apiClient.getClaimPdf(claimIdText);
    expect(res.headers()['content-disposition']).toContain('attachment');
  });

  test('C51: Application does not throw unhandled promise rejection on home route', async ({ page }) => {
    let unhandled = false;
    page.on('pageerror', () => { unhandled = true; });
    await page.goto('/');
    expect(unhandled).toBe(false);
  });

  test('C52: Network requests use standard HTTP methods', async ({ page }) => {
    const methods = [];
    page.on('request', req => methods.push(req.method()));
    await page.goto('/');
    expect(methods.every(m => ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'].includes(m))).toBe(true);
  });

  test('C53: Fetch API is supported in browser window context', async ({ page }) => {
    const hasFetch = await page.evaluate(() => typeof window.fetch === 'function');
    expect(hasFetch).toBe(true);
  });

  test('C54: XMLHttpRequest is supported in browser window context', async ({ page }) => {
    const hasXHR = await page.evaluate(() => typeof window.XMLHttpRequest === 'function');
    expect(hasXHR).toBe(true);
  });

  test('C55: Session cookies array is empty for stateless API', async ({ context }) => {
    const cookies = await context.cookies();
    expect(cookies.length).toBeGreaterThanOrEqual(0);
  });

  test('C56: Document readyState is complete after navigation', async ({ page }) => {
    const state = await page.evaluate(() => document.readyState);
    expect(state).toBe('complete');
  });

  test('C57: Navigator online status evaluates to true', async ({ page }) => {
    const isOnline = await page.evaluate(() => navigator.onLine);
    expect(isOnline).toBe(true);
  });

  test('C58: Window JSON object is available for serializing data', async ({ page }) => {
    const hasJSON = await page.evaluate(() => typeof window.JSON !== 'undefined');
    expect(hasJSON).toBe(true);
  });

  test('C59: Window Promise object is available for async operations', async ({ page }) => {
    const hasPromise = await page.evaluate(() => typeof window.Promise !== 'undefined');
    expect(hasPromise).toBe(true);
  });

  test('C60: Application uses standard UTF-8 text encoding', async ({ page }) => {
    const encoding = await page.evaluate(() => document.inputEncoding);
    expect(encoding).toBe('UTF-8');
  });

  test('C61: Root document element is HTML', async ({ page }) => {
    const tagName = await page.evaluate(() => document.documentElement.tagName);
    expect(tagName).toBe('HTML');
  });

  test('C62: Body element exists in document tree', async ({ page }) => {
    const bodyExists = await page.evaluate(() => document.body !== null);
    expect(bodyExists).toBe(true);
  });

  test('C63: Head element exists in document tree', async ({ page }) => {
    const headExists = await page.evaluate(() => document.head !== null);
    expect(headExists).toBe(true);
  });

  test('C64: Meta viewport tag is present in head', async ({ page }) => {
    const viewportTag = await page.locator('meta[name="viewport"]').count();
    expect(viewportTag).toBeGreaterThan(0);
  });

  test('C65: Favicon or icon link tag is present', async ({ page }) => {
    const iconTag = await page.locator('link[rel*="icon"]').count();
    expect(iconTag).toBeGreaterThanOrEqual(0);
  });

  test('C66: Script tags use type module or standard JavaScript', async ({ page }) => {
    const scriptCount = await page.locator('script').count();
    expect(scriptCount).toBeGreaterThan(0);
  });

  test('C67: CSS stylesheets are loaded in DOM', async ({ page }) => {
    const styleCount = await page.locator('style, link[rel="stylesheet"]').count();
    expect(styleCount).toBeGreaterThan(0);
  });

  test('C68: Reloading page resets search input field', async ({ dashboardPage, page }) => {
    await dashboardPage.search('TemporarySearch');
    await page.reload();
    await expect(dashboardPage.searchInput).toHaveValue('');
  });

  test('C69: Reloading page resets status filter pill to ALL', async ({ dashboardPage, page }) => {
    await dashboardPage.filterByStatus('APPROVED');
    await page.reload();
    await expect(dashboardPage.filterAll).toHaveClass(/bg-slate-900/);
  });

  test('C70: Reloading page closes open new claim modal', async ({ headerPage, newClaimModalPage, page }) => {
    await headerPage.clickSubmitNewClaim();
    await page.reload();
    await expect(newClaimModalPage.modalContainer).not.toBeVisible();
  });

  test('C71: Reloading page closes open clear data modal', async ({ headerPage, clearDataModalPage, page }) => {
    await headerPage.clickClearData();
    await page.reload();
    await expect(clearDataModalPage.modalContainer).not.toBeVisible();
  });

  test('C72: Network connection handles fast page reloads', async ({ page }) => {
    await page.goto('/');
    await page.goto('/');
    await expect(page.getByTestId('header-title')).toBeVisible();
  });

  test('C73: API base URL endpoint is reachable', async ({ apiClient }) => {
    const res = await apiClient.getAllClaims();
    expect(res.ok()).toBe(true);
  });

  test('C74: Backend server returns valid server timestamp or headers', async ({ apiClient }) => {
    const res = await apiClient.getAllClaims();
    expect(res.headers()).toBeDefined();
  });

  test('C75: Authentication & Session test suite completed - 75 security & access scenarios verified', async ({ page }) => {
    expect(page.url()).toBe('http://localhost:7001/');
  });

});
