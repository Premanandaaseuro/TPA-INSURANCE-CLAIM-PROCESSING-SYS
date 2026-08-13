const { test, expect } = require('@playwright/test');
const path = require('path');
const { DashboardPage } = require('../../pages/DashboardPage');
const { SubmitClaimPage } = require('../../pages/SubmitClaimPage');
const { ClaimDetailsPage } = require('../../pages/ClaimDetailsPage');
const { RuleAuditPage } = require('../../pages/RuleAuditPage');

test.describe('E2E - 03 Rule R02 Combined Document Missing', () => {
  test('Should trigger R02 FAIL (REJECTED) when Combined Hospital Document is missing', async ({ page }) => {
    const dashboardPage = new DashboardPage(page);
    const submitClaimPage = new SubmitClaimPage(page);
    const claimDetailsPage = new ClaimDetailsPage(page);
    const ruleAuditPage = new RuleAuditPage(page);

    const fixtureDir = path.join(__dirname, '../../fixtures/test-data/02_r02_missing_combined');
    const claimFormPath = path.join(fixtureDir, 'ClaimForm_Only.pdf');

    await dashboardPage.goto();
    await dashboardPage.openNewClaimModal();

    // Upload ONLY Claim Form (Omitting Combined Hospital Document)
    await submitClaimPage.uploadClaimForm(claimFormPath);
    await submitClaimPage.processClaim();

    // Verify Final Status is REJECTED
    const statusText = await claimDetailsPage.getStatusText();
    expect(statusText).toContain('REJECTED');

    const decisionReason = await claimDetailsPage.getDecisionReason();
    expect(decisionReason).toContain('R02');

    // R01 = PASS
    await ruleAuditPage.verifyRuleResult('R01', 'PASS');
    // R02 = FAIL (REJECTED)
    await ruleAuditPage.verifyRuleResult('R02', 'FAIL', 'REJECTED');
  });
});
