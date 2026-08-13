const { test, expect } = require('@playwright/test');
const path = require('path');
const { DashboardPage } = require('../../pages/DashboardPage');
const { SubmitClaimPage } = require('../../pages/SubmitClaimPage');
const { ClaimDetailsPage } = require('../../pages/ClaimDetailsPage');
const { ClaimApiClient } = require('../../utils/api_client');

test.describe('REGRESSION - Negative Safety Checks (Invalid Claims MUST NEVER be APPROVED)', () => {
  test.beforeEach(async ({ request }) => {
    const apiClient = new ClaimApiClient(request);
    await apiClient.clearAllClaims();
  });

  test('Missing Claim Form MUST NEVER be APPROVED', async ({ page }) => {
    const dashboardPage = new DashboardPage(page);
    const submitClaimPage = new SubmitClaimPage(page);
    const claimDetailsPage = new ClaimDetailsPage(page);

    const fixtureDir = path.join(__dirname, '../../fixtures/test-data/golden_valid_claim');
    const combinedDocPath = path.join(fixtureDir, 'CombinedHospitalDoc_Golden.pdf');

    await dashboardPage.goto();
    await dashboardPage.openNewClaimModal();
    await submitClaimPage.uploadCombinedDoc(combinedDocPath);
    await submitClaimPage.processClaim();

    const statusText = await claimDetailsPage.getStatusText();
    expect(statusText).not.toContain('APPROVED');
    expect(statusText).toContain('REJECTED');
  });

  test('Missing Policy Number MUST NEVER be APPROVED', async ({ page }) => {
    const dashboardPage = new DashboardPage(page);
    const submitClaimPage = new SubmitClaimPage(page);
    const claimDetailsPage = new ClaimDetailsPage(page);

    const fixtureDir = path.join(__dirname, '../../fixtures/test-data/04_r04_missing_policy_number');
    const claimFormPath = path.join(fixtureDir, 'ClaimForm_MissingPolicyNumber.pdf');
    const combinedDocPath = path.join(fixtureDir, 'CombinedHospitalDoc_MissingPolicyNumber.pdf');

    await dashboardPage.goto();
    await dashboardPage.openNewClaimModal();
    await submitClaimPage.uploadClaimForm(claimFormPath);
    await submitClaimPage.uploadCombinedDoc(combinedDocPath);
    await submitClaimPage.processClaim();

    const statusText = await claimDetailsPage.getStatusText();
    expect(statusText).not.toContain('APPROVED');
    expect(statusText).toContain('MANUAL REVIEW');
  });

  test('Inactive Policy MUST NEVER be APPROVED', async ({ page }) => {
    const dashboardPage = new DashboardPage(page);
    const submitClaimPage = new SubmitClaimPage(page);
    const claimDetailsPage = new ClaimDetailsPage(page);

    const fixtureDir = path.join(__dirname, '../../fixtures/test-data/03_r03_inactive_policy');
    const claimFormPath = path.join(fixtureDir, 'ClaimForm_InactivePolicy.pdf');
    const combinedDocPath = path.join(fixtureDir, 'CombinedHospitalDoc_InactivePolicy.pdf');

    await dashboardPage.goto();
    await dashboardPage.openNewClaimModal();
    await submitClaimPage.uploadClaimForm(claimFormPath);
    await submitClaimPage.uploadCombinedDoc(combinedDocPath);
    await submitClaimPage.processClaim();

    const statusText = await claimDetailsPage.getStatusText();
    expect(statusText).not.toContain('APPROVED');
    expect(statusText).toContain('REJECTED');
  });
});
