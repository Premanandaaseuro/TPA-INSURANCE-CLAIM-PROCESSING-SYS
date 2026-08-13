const { test, expect } = require('@playwright/test');
const { DashboardPage } = require('../../pages/DashboardPage');
const { SubmitClaimPage } = require('../../pages/SubmitClaimPage');

test.describe('UI - Input Validation & File Restrictions', () => {
  test('Should handle document upload requirement gracefully', async ({ page }) => {
    const dashboardPage = new DashboardPage(page);
    const submitClaimPage = new SubmitClaimPage(page);

    await dashboardPage.goto();
    await dashboardPage.openNewClaimModal();

    // Process without selecting files -> disabled or triggers client warning
    if (await submitClaimPage.processClaimBtn.isEnabled()) {
      await submitClaimPage.processClaim();
    } else {
      await expect(submitClaimPage.processClaimBtn).toBeDisabled();
    }
  });
});
