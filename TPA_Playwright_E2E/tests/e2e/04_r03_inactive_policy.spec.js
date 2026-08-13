const { test, expect } = require('@playwright/test');
const path = require('path');
const { DashboardPage } = require('../../pages/DashboardPage');
const { SubmitClaimPage } = require('../../pages/SubmitClaimPage');
const { ClaimDetailsPage } = require('../../pages/ClaimDetailsPage');
const { RuleAuditPage } = require('../../pages/RuleAuditPage');
const { ClaimApiClient } = require('../../utils/api_client');

test.describe('E2E - 04 Rule R03 Inactive Policy', () => {
  test.beforeEach(async ({ request }) => {
    const apiClient = new ClaimApiClient(request);
    await apiClient.clearAllClaims();
  });

  test('Should trigger R03 FAIL (REJECTED) when Policy exists in DB but is INACTIVE', async ({ page }) => {
    const dashboardPage = new DashboardPage(page);
    const submitClaimPage = new SubmitClaimPage(page);
    const claimDetailsPage = new ClaimDetailsPage(page);
    const ruleAuditPage = new RuleAuditPage(page);

    const fixtureDir = path.join(__dirname, '../../fixtures/test-data/03_r03_inactive_policy');
    const claimFormPath = path.join(fixtureDir, 'ClaimForm_InactivePolicy.pdf');
    const combinedDocPath = path.join(fixtureDir, 'CombinedHospitalDoc_InactivePolicy.pdf');

    await dashboardPage.goto();
    await dashboardPage.openNewClaimModal();

    await submitClaimPage.uploadClaimForm(claimFormPath);
    await submitClaimPage.uploadCombinedDoc(combinedDocPath);
    await submitClaimPage.processClaim();

    const statusText = await claimDetailsPage.getStatusText();
    expect(statusText).toContain('REJECTED');

    const decisionReason = await claimDetailsPage.getDecisionReason();
    expect(decisionReason).toContain('R03');

    await ruleAuditPage.verifyRuleResult('R04', 'PASS');
    await ruleAuditPage.verifyRuleResult('R03', 'FAIL', 'REJECTED');
  });
});
