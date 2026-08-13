const { test, expect } = require('@playwright/test');
const path = require('path');
const { DashboardPage } = require('../../pages/DashboardPage');
const { SubmitClaimPage } = require('../../pages/SubmitClaimPage');
const { ClaimDetailsPage } = require('../../pages/ClaimDetailsPage');
const { RuleAuditPage } = require('../../pages/RuleAuditPage');
const { ClaimApiClient } = require('../../utils/api_client');

test.describe('E2E - 05 Rule R04 Missing Policy Number in Claim Form', () => {
  test.beforeEach(async ({ request }) => {
    const apiClient = new ClaimApiClient(request);
    await apiClient.clearAllClaims();
  });

  test('Should trigger R04 FAIL (NEEDS_MANUAL_REVIEW) when Claim Form has Policy ID (PID-10008) but NO Policy Number', async ({ page }) => {
    const dashboardPage = new DashboardPage(page);
    const submitClaimPage = new SubmitClaimPage(page);
    const claimDetailsPage = new ClaimDetailsPage(page);
    const ruleAuditPage = new RuleAuditPage(page);

    const fixtureDir = path.join(__dirname, '../../fixtures/test-data/04_r04_missing_policy_number');
    const claimFormPath = path.join(fixtureDir, 'ClaimForm_MissingPolicyNumber.pdf');
    const combinedDocPath = path.join(fixtureDir, 'CombinedHospitalDoc_MissingPolicyNumber.pdf');

    await dashboardPage.goto();
    await dashboardPage.openNewClaimModal();

    await submitClaimPage.uploadClaimForm(claimFormPath);
    await submitClaimPage.uploadCombinedDoc(combinedDocPath);
    await submitClaimPage.processClaim();

    const statusText = await claimDetailsPage.getStatusText();
    expect(statusText).toContain('MANUAL REVIEW');

    const decisionReason = await claimDetailsPage.getDecisionReason();
    expect(decisionReason).toContain('R04');

    await ruleAuditPage.verifyRuleResult('R04', 'FAIL', 'NEEDS_MANUAL_REVIEW');
    await ruleAuditPage.verifyRuleResult('R03', 'NOT_EVALUATED');
  });
});
