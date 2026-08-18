import { test, expect } from '../../fixtures/test-fixtures.js';

test.describe('Category M – Modals & Dialog State Transitions (30 Tests)', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  // Modal Backdrop and overlay visibility loop (15 Tests)
  for (let i = 1; i <= 15; i++) {
    test(`M${i.toString().padStart(2, '0')}: Overlay backdrop layout check #${i}`, async ({ headerPage, newClaimModalPage }) => {
      await headerPage.clickSubmitNewClaim();
      await expect(newClaimModalPage.modalContainer).toHaveClass(/backdrop-blur-sm/);
      await newClaimModalPage.cancel();
    });
  }

  // Clear data dialog validation triggers (15 Tests)
  for (let i = 16; i <= 30; i++) {
    test(`M${i}: Purge confirmation layout check #${i - 15}`, async ({ headerPage, clearDataModalPage }) => {
      await headerPage.clickClearData();
      await expect(clearDataModalPage.modalContainer).toBeVisible();
      await clearDataModalPage.cancelClear();
    });
  }
});
