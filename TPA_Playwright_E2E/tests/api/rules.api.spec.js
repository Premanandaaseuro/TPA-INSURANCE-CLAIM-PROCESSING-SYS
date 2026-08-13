const { test, expect } = require('@playwright/test');
const path = require('path');
const { ClaimApiClient } = require('../../utils/api_client');

test.describe('API - Business Rule Results Validation', () => {
  test('POST /api/claims should return exact 10 rules R01-R10 in ruleResults array', async ({ request }) => {
    const apiClient = new ClaimApiClient(request);
    const goldenDir = path.join(__dirname, '../../fixtures/test-data/golden_valid_claim');
    const claimFormPath = path.join(goldenDir, 'ClaimForm_Golden.pdf');
    const combinedDocPath = path.join(goldenDir, 'CombinedHospitalDoc_Golden.pdf');

    const res = await apiClient.uploadClaimFiles(claimFormPath, combinedDocPath);
    expect(res.status()).toBe(201);

    const body = await res.json();
    const ruleCodes = body.ruleResults.map(r => r.ruleCode);
    const expected = ['R01', 'R02', 'R03', 'R04', 'R05', 'R06', 'R07', 'R08', 'R09', 'R10'];

    expect(ruleCodes).toEqual(expected);
  });
});
