const { test, expect } = require('@playwright/test');
const { DashboardPage } = require('../../pages/DashboardPage');

test.describe('UI - Dashboard Search & Filter Controls', () => {
  test('Should filter claims list by search input', async ({ page }) => {
    const dashboardPage = new DashboardPage(page);
    await dashboardPage.goto();

    await dashboardPage.searchClaim('CLM-NONEXISTENT-999');
    await expect(page.locator('tbody tr')).toHaveCount(0);
  });
});
