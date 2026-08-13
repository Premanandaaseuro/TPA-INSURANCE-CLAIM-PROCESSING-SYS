const { test, expect } = require('@playwright/test');
const path = require('path');
const { DashboardPage } = require('../../pages/DashboardPage');
const { SubmitClaimPage } = require('../../pages/SubmitClaimPage');
const { ClaimDetailsPage } = require('../../pages/ClaimDetailsPage');
const { RuleAuditPage } = require('../../pages/RuleAuditPage');
const { ClaimApiClient } = require('../../utils/api_client');

test.describe('E2E - 09 Rule R08 Claimed Amount Exceeds Bill', () => {
  test.beforeEach(async ({ request }) => {
    const apiClient = new ClaimApiClient(request);
    await apiClient.clearAllClaims();
  });

  test('Should trigger R08 FAIL (NEEDS_MANUAL_REVIEW) when Claimed Amount (45000) > Bill Amount (30000)', async ({ page }) => {
    const dashboardPage = new DashboardPage(page);
    const submitClaimPage = new SubmitClaimPage(page);
    const claimDetailsPage = new ClaimDetailsPage(page);
    const ruleAuditPage = new RuleAuditPage(page);

    const fixtureDir = path.join(__dirname, '../../fixtures/test-data/08_r08_claim_greater_bill');
    const claimFormPath = path.join(fixtureDir, 'ClaimForm_ClaimGreaterBill.pdf');
    const combinedDocPath = path.join(fixtureDir, 'CombinedHospitalDoc_ClaimGreaterBill.pdf');

    await dashboardPage.goto();
    await dashboardPage.openNewClaimModal();

    await submitClaimPage.uploadClaimForm(claimFormPath);
    await submitClaimPage.uploadCombinedDoc(combinedDocPath);
    await submitClaimPage.processClaim();

    const statusText = await claimDetailsPage.getStatusText();
    expect(statusText).toContain('MANUAL REVIEW');

    await ruleAuditPage.verifyRuleResult('R08', 'FAIL', 'NEEDS_MANUAL_REVIEW');
  });
});
