const { test, expect } = require('@playwright/test');
const { DashboardPage } = require('../../pages/DashboardPage');

test.describe('UI - Dashboard Page', () => {
  test('Should render dashboard title, search input, status filters and new claim button', async ({ page }) => {
    const dashboardPage = new DashboardPage(page);
    await dashboardPage.goto();

    await expect(page.getByRole('heading', { level: 1 })).toBeVisible();
    await expect(dashboardPage.newClaimBtn).toBeVisible();
    await expect(dashboardPage.searchInput).toBeVisible();
  });
});
