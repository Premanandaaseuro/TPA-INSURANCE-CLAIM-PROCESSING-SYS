const { test, expect } = require('@playwright/test');
const path = require('path');
const { ClaimApiClient } = require('../../utils/api_client');

test.describe('API - PDF Export Endpoint', () => {
  test('GET /api/claims/{claimId}/pdf should return HTTP 200 with application/pdf header', async ({ request }) => {
    const apiClient = new ClaimApiClient(request);
    const goldenDir = path.join(__dirname, '../../fixtures/test-data/golden_valid_claim');
    const claimFormPath = path.join(goldenDir, 'ClaimForm_Golden.pdf');
    const combinedDocPath = path.join(goldenDir, 'CombinedHospitalDoc_Golden.pdf');

    const uploadRes = await apiClient.uploadClaimFiles(claimFormPath, combinedDocPath);
    const claim = await uploadRes.json();

    const pdfRes = await apiClient.downloadClaimPdf(claim.claimId);
    expect(pdfRes.status()).toBe(200);
    expect(pdfRes.headers()['content-type']).toContain('application/pdf');

    const pdfBytes = await pdfRes.body();
    expect(pdfBytes.length).toBeGreaterThan(0);
  });
});
