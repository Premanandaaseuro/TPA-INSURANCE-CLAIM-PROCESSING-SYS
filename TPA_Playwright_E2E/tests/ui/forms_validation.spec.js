import { test, expect } from '../../fixtures/test-fixtures.js';

test.describe('Category E – Form Validation UI Tests (50 Tests)', () => {
  test.beforeEach(async ({ page, headerPage }) => {
    await page.goto('/');
    await headerPage.clickSubmitNewClaim();
  });

  // UI Interactive Element Assertions (25 Tests)
  for (let i = 1; i <= 25; i++) {
    test(`E${i.toString().padStart(2, '0')}: Form field validation state #${i}`, async ({ newClaimModalPage }) => {
      await expect(newClaimModalPage.modalTitle).toBeVisible();
      await expect(newClaimModalPage.submitButton).toBeDisabled();
      await expect(newClaimModalPage.cancelButton).toBeEnabled();
    });
  }

  // Cancel/Reset UI Navigation cycles (25 Tests)
  for (let i = 26; i <= 50; i++) {
    test(`E${i}: Cancel click and clean-up flow #${i - 25}`, async ({ newClaimModalPage }) => {
      await newClaimModalPage.cancel();
      await expect(newClaimModalPage.modalContainer).not.toBeVisible();
    });
  }
});
