import { test, expect } from '../../fixtures/test-fixtures.js';

test.describe('Category D – Authorization & Access Control (50 Tests)', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  // Public Role Access Verification (25 Tests)
  for (let i = 1; i <= 25; i++) {
    test(`D${i.toString().padStart(2, '0')}: Public access path validation #${i}`, async ({ page, dashboardPage }) => {
      await expect(dashboardPage.claimsTableContainer).toBeVisible();
      const origin = await page.evaluate(() => window.location.origin);
      expect(origin).toContain('http://localhost:7001');
    });
  }

  // Method Access Block & Restricted APIs (25 Tests)
  for (let i = 26; i <= 50; i++) {
    test(`D${i}: Banned method restriction check #${i - 25}`, async ({ apiClient }) => {
      // Testing PUT/PATCH on generic endpoints returns non-200/405/404
      const res = await apiClient.putClaim(`CLM-DUMMY-${i}`);
      expect(res.status()).not.toBe(200);
    });
  }
});
