const { test, expect } = require('@playwright/test');
const { DashboardPage } = require('../../pages/DashboardPage');
const { SubmitClaimPage } = require('../../pages/SubmitClaimPage');

test.describe('UI - Claim Submission Flow', () => {
  test('Should open claim submission modal and display file upload dropzones', async ({ page }) => {
    const dashboardPage = new DashboardPage(page);
    const submitClaimPage = new SubmitClaimPage(page);

    await dashboardPage.goto();
    await dashboardPage.openNewClaimModal();

    await expect(submitClaimPage.claimFileInput).toBeAttached();
    await expect(submitClaimPage.combinedFileInput).toBeAttached();
    await expect(submitClaimPage.processClaimBtn).toBeVisible();
  });
});
