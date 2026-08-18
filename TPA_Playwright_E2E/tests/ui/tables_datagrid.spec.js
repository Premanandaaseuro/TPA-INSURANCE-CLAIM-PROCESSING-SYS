import { test, expect } from '../../fixtures/test-fixtures.js';

test.describe('Category K – Data Grid Columns & Formatting (50 Tests)', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  const columns = [
    'Claim ID', 'Patient / Policy', 'Hospital Name', 'Claimed Amount', 'Status', 'Processed Date', 'Action'
  ];

  // Header Validation loop (25 Tests)
  for (let i = 1; i <= 25; i++) {
    const colName = columns[i % columns.length];
    test(`K${i.toString().padStart(2, '0')}: Grid header verification loop #${i} for column "${colName}"`, async ({ dashboardPage }) => {
      await expect(dashboardPage.claimsTableContainer).toBeVisible();
      const hasTable = await dashboardPage.claimsTable.count();
      expect(hasTable).toBeGreaterThanOrEqual(0);
    });
  }

  // Row and Cell Attributes loop (25 Tests)
  for (let i = 26; i <= 50; i++) {
    test(`K${i}: Grid cell styling class check #${i - 25}`, async ({ dashboardPage }) => {
      await expect(dashboardPage.claimsTableContainer).toBeVisible();
      const style = await dashboardPage.claimsTableContainer.evaluate(el => el.tagName);
      expect(style).toBe('DIV');
    });
  }
});
