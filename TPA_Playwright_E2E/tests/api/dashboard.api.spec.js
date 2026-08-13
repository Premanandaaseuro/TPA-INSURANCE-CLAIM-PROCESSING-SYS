const { test, expect } = require('@playwright/test');
const { ClaimApiClient } = require('../../utils/api_client');

test.describe('API - Dashboard Data Endpoint', () => {
  test('GET /api/claims should return HTTP 200 and list of claim summary objects', async ({ request }) => {
    const apiClient = new ClaimApiClient(request);
    const res = await apiClient.getAllClaims();
    expect(res.status()).toBe(200);

    const body = await res.json();
    expect(Array.isArray(body)).toBe(true);
  });
});
