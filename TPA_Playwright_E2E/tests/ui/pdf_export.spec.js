const { test, expect } = require('@playwright/test');
const path = require('path');
const { DashboardPage } = require('../../pages/DashboardPage');
const { SubmitClaimPage } = require('../../pages/SubmitClaimPage');
const { ClaimDetailsPage } = require('../../pages/ClaimDetailsPage');
const { PdfExportPage } = require('../../pages/PdfExportPage');

test.describe('UI - PDF Export Functionality', () => {
  test('Should download valid, non-zero byte PDF summary report', async ({ page }) => {
    const dashboardPage = new DashboardPage(page);
    const submitClaimPage = new SubmitClaimPage(page);
    const claimDetailsPage = new ClaimDetailsPage(page);
    const pdfExportPage = new PdfExportPage(page);

    const goldenDir = path.join(__dirname, '../../fixtures/test-data/golden_valid_claim');
    const claimFormPath = path.join(goldenDir, 'ClaimForm_Golden.pdf');
    const combinedDocPath = path.join(goldenDir, 'CombinedHospitalDoc_Golden.pdf');

    await dashboardPage.goto();
    await dashboardPage.openNewClaimModal();
    await submitClaimPage.uploadClaimForm(claimFormPath);
    await submitClaimPage.uploadCombinedDoc(combinedDocPath);
    await submitClaimPage.processClaim();

    const { fileName, fileSize } = await pdfExportPage.verifyPdfDownload('CLM-TEST');
    expect(fileSize).toBeGreaterThan(0);
    expect(fileName.toLowerCase()).toContain('.pdf');
  });
});
