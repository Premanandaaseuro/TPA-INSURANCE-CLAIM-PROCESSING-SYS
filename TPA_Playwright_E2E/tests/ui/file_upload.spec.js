const { test, expect } = require('@playwright/test');
const path = require('path');
const { DashboardPage } = require('../../pages/DashboardPage');
const { SubmitClaimPage } = require('../../pages/SubmitClaimPage');

test.describe('UI - File Upload Controls', () => {
  test('Should accept PDF file attachments in file input controls', async ({ page }) => {
    const dashboardPage = new DashboardPage(page);
    const submitClaimPage = new SubmitClaimPage(page);

    const goldenDir = path.join(__dirname, '../../fixtures/test-data/golden_valid_claim');
    const claimFormPath = path.join(goldenDir, 'ClaimForm_Golden.pdf');
    const combinedDocPath = path.join(goldenDir, 'CombinedHospitalDoc_Golden.pdf');

    await dashboardPage.goto();
    await dashboardPage.openNewClaimModal();

    await submitClaimPage.uploadClaimForm(claimFormPath);
    await submitClaimPage.uploadCombinedDoc(combinedDocPath);

    await expect(submitClaimPage.claimFileInput).toBeAttached();
    await expect(submitClaimPage.combinedFileInput).toBeAttached();
  });
});
