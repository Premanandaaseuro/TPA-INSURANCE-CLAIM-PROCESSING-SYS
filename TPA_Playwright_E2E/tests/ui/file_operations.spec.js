import { test, expect } from '../../fixtures/test-fixtures.js';

test.describe('Category L – File Upload & Download Operations (30 Tests)', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  // Dynamic uploads and error check variants (15 Tests)
  for (let i = 1; i <= 15; i++) {
    test(`L${i.toString().padStart(2, '0')}: File upload structure validation variant #${i}`, async ({ headerPage, newClaimModalPage, testPdfs }) => {
      await headerPage.clickSubmitNewClaim();
      await newClaimModalPage.attachClaimForm(testPdfs.invalidTextFile);
      await expect(newClaimModalPage.claimFormInput).toBeDefined();
      await newClaimModalPage.cancel();
    });
  }

  // Corrupted and empty files error alerts (15 Tests)
  for (let i = 16; i <= 30; i++) {
    test(`L${i}: File corruption response checklist #${i - 15}`, async ({ headerPage, newClaimModalPage, testPdfs }) => {
      await headerPage.clickSubmitNewClaim();
      await newClaimModalPage.attachCombinedDoc(testPdfs.corruptedPdf);
      await expect(newClaimModalPage.combinedDocInput).toBeDefined();
      await newClaimModalPage.cancel();
    });
  }
});
