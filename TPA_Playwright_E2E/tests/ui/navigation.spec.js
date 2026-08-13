const { test, expect } = require('@playwright/test');
const { DashboardPage } = require('../../pages/DashboardPage');
const { SubmitClaimPage } = require('../../pages/SubmitClaimPage');

test.describe('UI - Navigation & Modal Transitions', () => {
  test('Should open and cancel new claim modal cleanly', async ({ page }) => {
    const dashboardPage = new DashboardPage(page);
    const submitClaimPage = new SubmitClaimPage(page);

    await dashboardPage.goto();
    await dashboardPage.openNewClaimModal();
    await submitClaimPage.cancel();

    await expect(dashboardPage.newClaimBtn).toBeVisible();
  });
});
