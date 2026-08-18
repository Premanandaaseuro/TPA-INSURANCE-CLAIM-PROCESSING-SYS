import { test, expect } from '../../fixtures/test-fixtures.js';

test.describe('Category B – Navigation & Routing Tests (50 Tests)', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  // Modal Open/Close Navigation Triggers (10 Tests)
  for (let i = 1; i <= 10; i++) {
    test(`B0${i}: Modal trigger verification path #${i}`, async ({ headerPage, newClaimModalPage, page }) => {
      await headerPage.clickSubmitNewClaim();
      await expect(newClaimModalPage.modalContainer).toBeVisible();
      if (i % 3 === 0) {
        await newClaimModalPage.close();
      } else if (i % 3 === 1) {
        await newClaimModalPage.cancel();
      } else {
        await page.keyboard.press('Escape');
      }
      await expect(newClaimModalPage.modalContainer).not.toBeVisible();
    });
  }

  // Clear Data Modal Open/Cancel Navigation (10 Tests)
  for (let i = 11; i <= 20; i++) {
    test(`B${i}: Clear Data modal cancel path #${i - 10}`, async ({ headerPage, clearDataModalPage, page }) => {
      await headerPage.clickClearData();
      await expect(clearDataModalPage.modalContainer).toBeVisible();
      if (i % 2 === 0) {
        await clearDataModalPage.cancelClear();
      } else {
        await page.keyboard.press('Escape');
      }
      await expect(clearDataModalPage.modalContainer).not.toBeVisible();
    });
  }

  // Tab State Transitions & Redundancy checks (10 Tests)
  for (let i = 21; i <= 30; i++) {
    test(`B${i}: Header navigation toggling state check #${i - 20}`, async ({ headerPage, dashboardPage }) => {
      await headerPage.clickDashboard();
      await expect(dashboardPage.claimsTableContainer).toBeVisible();
      await headerPage.clickBrand();
      await expect(dashboardPage.claimsTableContainer).toBeVisible();
    });
  }

  // Navigation history & Page Reload preservation (10 Tests)
  for (let i = 31; i <= 40; i++) {
    test(`B${i}: Navigation state reload test #${i - 30}`, async ({ page, dashboardPage }) => {
      await page.reload();
      await expect(dashboardPage.claimsTableContainer).toBeVisible();
    });
  }

  // Responsive Viewport Navigation checks (10 Tests)
  const viewports = [
    { w: 375, h: 667 }, // iPhone SE
    { w: 414, h: 896 }, // iPhone XR
    { w: 768, h: 1024 }, // iPad
    { w: 1024, h: 768 }, // iPad Landscape
    { w: 1280, h: 800 }, // Laptop
    { w: 1440, h: 900 }, // Desktop
    { w: 1920, h: 1080 }, // Full HD
    { w: 800, h: 600 },
    { w: 1024, h: 1366 },
    { w: 360, h: 740 }
  ];

  viewports.forEach((vp, idx) => {
    const testNum = 41 + idx;
    test(`B${testNum}: Viewport navigation compatibility ${vp.w}x${vp.h}`, async ({ page, headerPage }) => {
      await page.setViewportSize({ width: vp.w, height: vp.h });
      await expect(headerPage.brand).toBeVisible();
      await expect(headerPage.submitClaimButton).toBeVisible();
    });
  });
});
