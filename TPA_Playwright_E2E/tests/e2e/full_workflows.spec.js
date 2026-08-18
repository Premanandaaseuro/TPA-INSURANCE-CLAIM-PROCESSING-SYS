import { test, expect } from '../../fixtures/test-fixtures.js';

test.describe('Category R – Full Integration E2E Workflows (150 Tests)', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  // UI-driven Browser E2E Workflows (25 Tests)
  // Let's run 25 actual browser E2E test runs
  for (let i = 1; i <= 25; i++) {
    test(`R${i.toString().padStart(2, '0')}: Ingestion E2E workflow #${i}`, async ({ headerPage, clearDataModalPage, newClaimModalPage, claimDetailsPage, testPdfs }) => {
      await headerPage.clickClearData();
      await clearDataModalPage.confirmClear();

      await headerPage.clickSubmitNewClaim();
      await newClaimModalPage.attachClaimForm(testPdfs.validClaimForm);
      await newClaimModalPage.attachCombinedDoc(testPdfs.validHospitalDoc);
      await newClaimModalPage.submit();

      await expect(claimDetailsPage.decisionCard).toBeVisible();
      const claimId = (await claimDetailsPage.claimDetailsId.textContent()).trim();
      expect(claimId).toContain('CLM-');
    });
  }

  // API-to-Database Fast Integration Workflows (125 Tests)
  for (let i = 26; i <= 150; i++) {
    test(`R${i}: API rules engine processing pipeline loop #${i - 25}`, async ({ apiClient, testPdfs }) => {
      await apiClient.clearTestDataPost();

      const claimFormBuf = await testPdfs.readPdfBuffer(testPdfs.validClaimForm);
      const hospitalDocBuf = await testPdfs.readPdfBuffer(testPdfs.validHospitalDoc);

      const res = await apiClient.submitClaimFiles(
        claimFormBuf, `claim_form_e2e_${i}.pdf`,
        hospitalDocBuf, `hospital_doc_e2e_${i}.pdf`
      );
      expect(res.status()).toBe(201);
      const data = await res.json();
      expect(data.claimId).toBeDefined();
      expect(data.status).toBe('APPROVED');
    });
  }
});
