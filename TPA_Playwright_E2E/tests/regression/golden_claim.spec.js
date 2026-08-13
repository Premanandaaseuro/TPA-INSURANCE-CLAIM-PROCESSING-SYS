const { test, expect } = require('@playwright/test');
const path = require('path');
const { DashboardPage } = require('../../pages/DashboardPage');
const { SubmitClaimPage } = require('../../pages/SubmitClaimPage');
const { ClaimDetailsPage } = require('../../pages/ClaimDetailsPage');
const { RuleAuditPage } = require('../../pages/RuleAuditPage');
const { ClaimApiClient } = require('../../utils/api_client');
const { DbValidator } = require('../../utils/db_validator');

test.describe('REGRESSION - Golden Valid Claim', () => {
  test.beforeEach(async ({ request }) => {
    const apiClient = new ClaimApiClient(request);
    await apiClient.clearAllClaims();
  });

  test('Golden valid claim MUST result in APPROVED status and zero failed rules', async ({ page, request }) => {
    const dashboardPage = new DashboardPage(page);
    const submitClaimPage = new SubmitClaimPage(page);
    const claimDetailsPage = new ClaimDetailsPage(page);
    const ruleAuditPage = new RuleAuditPage(page);

    const goldenDir = path.join(__dirname, '../../fixtures/test-data/golden_valid_claim');
    const claimFormPath = path.join(goldenDir, 'ClaimForm_Golden.pdf');
    const combinedDocPath = path.join(goldenDir, 'CombinedHospitalDoc_Golden.pdf');

    await dashboardPage.goto();
    await dashboardPage.openNewClaimModal();
    await submitClaimPage.uploadClaimForm(claimFormPath);
    await submitClaimPage.uploadCombinedDoc(combinedDocPath);
    await submitClaimPage.processClaim();

    const statusText = await claimDetailsPage.getStatusText();
    expect(statusText).toContain('APPROVED');

    // Rule Audit validation: 10 rules must be PASS
    const totalCount = await ruleAuditPage.getAllRulesCount();
    expect(totalCount).toBe(10);

    for (let i = 1; i <= 10; i++) {
      const code = `R${i < 10 ? '0' + i : i}`;
      await ruleAuditPage.verifyRuleResult(code, 'PASS');
    }

    // Brief wait for backend DB persistence before querying API
    await page.waitForTimeout(500);

    // Backend state validation via API
    const apiClient = new ClaimApiClient(request);
    const dbValidator = new DbValidator(apiClient);
    const allClaimsRes = await apiClient.getAllClaims();
    expect(allClaimsRes.ok()).toBeTruthy();
    const claimsList = await allClaimsRes.json();
    expect(claimsList.length).toBeGreaterThan(0);
    const latestClaimId = claimsList[0].claimId;
    await dbValidator.validateAllRulesPresent(latestClaimId);
  });
});
