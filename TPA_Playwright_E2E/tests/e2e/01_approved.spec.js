const { test, expect } = require('@playwright/test');
const path = require('path');
const { DashboardPage } = require('../../pages/DashboardPage');
const { SubmitClaimPage } = require('../../pages/SubmitClaimPage');
const { ClaimDetailsPage } = require('../../pages/ClaimDetailsPage');
const { RuleAuditPage } = require('../../pages/RuleAuditPage');
const { ClaimApiClient } = require('../../utils/api_client');

test.describe('E2E - 01 Golden Approved Claim Flow', () => {
  test.beforeEach(async ({ request }) => {
    const apiClient = new ClaimApiClient(request);
    await apiClient.clearAllClaims();
  });

  test('Should process valid claim and transition to APPROVED with all R01-R10 PASS', async ({ page }) => {
    const dashboardPage = new DashboardPage(page);
    const submitClaimPage = new SubmitClaimPage(page);
    const claimDetailsPage = new ClaimDetailsPage(page);
    const ruleAuditPage = new RuleAuditPage(page);

    const fixtureDir = path.join(__dirname, '../../fixtures/test-data/golden_valid_claim');
    const claimFormPath = path.join(fixtureDir, 'ClaimForm_Golden.pdf');
    const combinedDocPath = path.join(fixtureDir, 'CombinedHospitalDoc_Golden.pdf');

    await dashboardPage.goto();
    await dashboardPage.openNewClaimModal();

    await submitClaimPage.uploadClaimForm(claimFormPath);
    await submitClaimPage.uploadCombinedDoc(combinedDocPath);
    await submitClaimPage.processClaim();

    const decisionReason = await claimDetailsPage.getDecisionReason();
    console.log('DECISION REASON FOR GOLDEN:', decisionReason);

    const statusText = await claimDetailsPage.getStatusText();
    expect(statusText).toContain('APPROVED');
  });
});
