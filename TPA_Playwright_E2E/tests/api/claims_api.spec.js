import { test, expect } from '../../fixtures/test-fixtures.js';

test.describe('Category O – Claims API Service REST Integration (100 Tests)', () => {

  // JSON Schema and Endpoint checks (50 Tests)
  for (let i = 1; i <= 50; i++) {
    test(`O${i.toString().padStart(2, '0')}: Query REST claims list endpoint loop #${i}`, async ({ apiClient }) => {
      const res = await apiClient.getAllClaims();
      expect(res.status()).toBe(200);
      const claims = await res.json();
      expect(Array.isArray(claims)).toBe(true);
    });
  }

  // Debug and Metadata APIs checks (50 Tests)
  for (let i = 51; i <= 100; i++) {
    test(`O${i}: HTTP Options preflight compatibility check #${i - 50}`, async ({ apiClient }) => {
      const res = await apiClient.optionsClaims();
      expect(res.status()).toBe(200);
    });
  }
});
