const { test, expect } = require('@playwright/test');
const path = require('path');
const { DashboardPage } = require('../../pages/DashboardPage');
const { SubmitClaimPage } = require('../../pages/SubmitClaimPage');
const { ClaimDetailsPage } = require('../../pages/ClaimDetailsPage');
const { RuleAuditPage } = require('../../pages/RuleAuditPage');
const { ClaimApiClient } = require('../../utils/api_client');

test.describe('E2E - 11 Rule R10 Possible Duplicate Claim', () => {
  test.beforeEach(async ({ request }) => {
    const apiClient = new ClaimApiClient(request);
    await apiClient.clearAllClaims();
  });

  test('Should trigger R10 FAIL (NEEDS_MANUAL_REVIEW) when identical claim parameters are submitted twice', async ({ page }) => {
    const dashboardPage = new DashboardPage(page);
    const submitClaimPage = new SubmitClaimPage(page);
    const claimDetailsPage = new ClaimDetailsPage(page);
    const ruleAuditPage = new RuleAuditPage(page);

    const goldenDir = path.join(__dirname, '../../fixtures/test-data/golden_valid_claim');
    const claimFormPath = path.join(goldenDir, 'ClaimForm_Golden.pdf');
    const combinedDocPath = path.join(goldenDir, 'CombinedHospitalDoc_Golden.pdf');

    // 1. Submit Claim #1 (Passes -> APPROVED)
    await dashboardPage.goto();
    await dashboardPage.openNewClaimModal();
    await submitClaimPage.uploadClaimForm(claimFormPath);
    await submitClaimPage.uploadCombinedDoc(combinedDocPath);
    await submitClaimPage.processClaim();

    let statusText = await claimDetailsPage.getStatusText();
    expect(statusText).toContain('APPROVED');

    await claimDetailsPage.backToDashboard();

    // 2. Submit Claim #2 with duplicate dataset (Triggers R10)
    const dupDir = path.join(__dirname, '../../fixtures/test-data/10_r10_duplicate');
    const dupClaimFormPath = path.join(dupDir, 'ClaimForm_Duplicate.pdf');
    const dupCombinedDocPath = path.join(dupDir, 'CombinedHospitalDoc_Duplicate.pdf');

    await dashboardPage.openNewClaimModal();
    await submitClaimPage.uploadClaimForm(dupClaimFormPath);
    await submitClaimPage.uploadCombinedDoc(dupCombinedDocPath);
    await submitClaimPage.processClaim();

    statusText = await claimDetailsPage.getStatusText();
    expect(statusText).toContain('MANUAL REVIEW');

    await ruleAuditPage.verifyRuleResult('R10', 'FAIL', 'NEEDS_MANUAL_REVIEW');
  });
});
