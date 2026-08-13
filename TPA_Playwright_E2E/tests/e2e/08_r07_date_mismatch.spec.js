const { test, expect } = require('@playwright/test');
const path = require('path');
const { DashboardPage } = require('../../pages/DashboardPage');
const { SubmitClaimPage } = require('../../pages/SubmitClaimPage');
const { ClaimDetailsPage } = require('../../pages/ClaimDetailsPage');
const { RuleAuditPage } = require('../../pages/RuleAuditPage');
const { ClaimApiClient } = require('../../utils/api_client');

test.describe('E2E - 08 Rule R07 Date Mismatch', () => {
  test.beforeEach(async ({ request }) => {
    const apiClient = new ClaimApiClient(request);
    await apiClient.clearAllClaims();
  });

  test('Should trigger R07 FAIL (NEEDS_MANUAL_REVIEW) when admission or discharge dates differ', async ({ page }) => {
    const dashboardPage = new DashboardPage(page);
    const submitClaimPage = new SubmitClaimPage(page);
    const claimDetailsPage = new ClaimDetailsPage(page);
    const ruleAuditPage = new RuleAuditPage(page);

    const fixtureDir = path.join(__dirname, '../../fixtures/test-data/07_r07_date_mismatch');
    const claimFormPath = path.join(fixtureDir, 'ClaimForm_DateMismatch.pdf');
    const combinedDocPath = path.join(fixtureDir, 'CombinedHospitalDoc_DateMismatch.pdf');

    await dashboardPage.goto();
    await dashboardPage.openNewClaimModal();

    await submitClaimPage.uploadClaimForm(claimFormPath);
    await submitClaimPage.uploadCombinedDoc(combinedDocPath);
    await submitClaimPage.processClaim();

    const statusText = await claimDetailsPage.getStatusText();
    expect(statusText).toContain('MANUAL REVIEW');

    await ruleAuditPage.verifyRuleResult('R07', 'FAIL', 'NEEDS_MANUAL_REVIEW');
  });
});
