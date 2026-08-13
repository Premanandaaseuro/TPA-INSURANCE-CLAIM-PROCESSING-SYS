const { test, expect } = require('@playwright/test');
const path = require('path');
const { DashboardPage } = require('../../pages/DashboardPage');
const { SubmitClaimPage } = require('../../pages/SubmitClaimPage');
const { ClaimDetailsPage } = require('../../pages/ClaimDetailsPage');
const { RuleAuditPage } = require('../../pages/RuleAuditPage');

test.describe('E2E - 02 Rule R01 Claim Form Missing', () => {
  test('Should trigger R01 FAIL (REJECTED) when Claim Form is missing, and skip downstream R03-R10 as NOT_EVALUATED', async ({ page }) => {
    const dashboardPage = new DashboardPage(page);
    const submitClaimPage = new SubmitClaimPage(page);
    const claimDetailsPage = new ClaimDetailsPage(page);
    const ruleAuditPage = new RuleAuditPage(page);

    const fixtureDir = path.join(__dirname, '../../fixtures/test-data/golden_valid_claim');
    const combinedDocPath = path.join(fixtureDir, 'CombinedHospitalDoc_Golden.pdf');

    await dashboardPage.goto();
    await dashboardPage.openNewClaimModal();

    // Upload ONLY Combined Hospital Document (Omitting Claim Form)
    await submitClaimPage.uploadCombinedDoc(combinedDocPath);
    await submitClaimPage.processClaim();

    // Verify Final Status is REJECTED
    const statusText = await claimDetailsPage.getStatusText();
    expect(statusText).toContain('REJECTED');

    const decisionReason = await claimDetailsPage.getDecisionReason();
    expect(decisionReason).toContain('R01');

    // R01 = FAIL (REJECTED)
    await ruleAuditPage.verifyRuleResult('R01', 'FAIL', 'REJECTED');
    // R02 = PASS
    await ruleAuditPage.verifyRuleResult('R02', 'PASS');

    // R03 - R10 = NOT_EVALUATED
    for (let i = 3; i <= 10; i++) {
      const ruleCode = `R${i < 10 ? '0' + i : i}`;
      await ruleAuditPage.verifyRuleResult(ruleCode, 'NOT_EVALUATED');
    }
  });
});
