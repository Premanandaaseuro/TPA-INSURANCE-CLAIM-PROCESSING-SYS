const { test, expect } = require('@playwright/test');
const path = require('path');
const { ClaimApiClient } = require('../../utils/api_client');

test.describe('API - Claim Upload & Processing Endpoints', () => {
  test.beforeEach(async ({ request }) => {
    const apiClient = new ClaimApiClient(request);
    await apiClient.clearAllClaims();
  });

  test('POST /api/claims should accept valid multipart PDF upload and return HTTP 201 APPROVED', async ({ request }) => {
    const apiClient = new ClaimApiClient(request);
    const goldenDir = path.join(__dirname, '../../fixtures/test-data/golden_valid_claim');
    const claimFormPath = path.join(goldenDir, 'ClaimForm_Golden.pdf');
    const combinedDocPath = path.join(goldenDir, 'CombinedHospitalDoc_Golden.pdf');

    const res = await apiClient.uploadClaimFiles(claimFormPath, combinedDocPath);
    expect(res.status()).toBe(201);

    const body = await res.json();
    expect(body.claimId).toBeDefined();
    expect(body.status).toBe('APPROVED');
    expect(body.ruleResults).toHaveLength(10);
  });

  test('POST /api/claims should process inactive policy and return REJECTED', async ({ request }) => {
    const apiClient = new ClaimApiClient(request);
    const r03Dir = path.join(__dirname, '../../fixtures/test-data/03_r03_inactive_policy');
    const claimFormPath = path.join(r03Dir, 'ClaimForm_InactivePolicy.pdf');
    const combinedDocPath = path.join(r03Dir, 'CombinedHospitalDoc_InactivePolicy.pdf');

    const res = await apiClient.uploadClaimFiles(claimFormPath, combinedDocPath);
    const body = await res.json();
    console.log('R03 INACTIVE TEST RULE RESULTS:', JSON.stringify(body.ruleResults, null, 2));
    expect(body.status).toBe('REJECTED');
  });
});
