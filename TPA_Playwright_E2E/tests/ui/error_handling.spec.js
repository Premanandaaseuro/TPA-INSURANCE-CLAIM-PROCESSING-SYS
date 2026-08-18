import { test, expect } from '../../fixtures/test-fixtures.js';

test.describe('Category N – Network & HTTP Exception Handlers (50 Tests)', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  // Client API response status simulation (25 Tests)
  for (let i = 1; i <= 25; i++) {
    test(`N${i.toString().padStart(2, '0')}: HTTP status simulation loop #${i}`, async ({ page }) => {
      // Mocking endpoint to return 500 error to verify error-banner appears
      await page.route('**/api/claims', route => route.fulfill({
        status: 500,
        body: JSON.stringify({ message: "Internal Server Database Exception" })
      }));
      await page.reload();
      const errorBanner = page.getByTestId('error-banner');
      await expect(errorBanner).toBeVisible();
    });
  }

  // Intercepting route mappings (25 Tests)
  for (let i = 26; i <= 50; i++) {
    test(`N${i}: HTTP bad request mock response check #${i - 25}`, async ({ page }) => {
      await page.route('**/api/claims', route => route.fulfill({
        status: 400,
        body: JSON.stringify({ message: "Bad JSON Parameter Payload" })
      }));
      await page.reload();
      const errorBanner = page.getByTestId('error-banner');
      await expect(errorBanner).toBeVisible();
    });
  }
});
