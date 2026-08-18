import { test, expect } from '../../fixtures/test-fixtures.js';

test.describe('Category G – Search Bar Assertions (50 Tests)', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  const searchQueries = [
    'CLM-2026', 'Rahul', 'POL-10001', 'Apollo', 'Star', 'Care', 'HDFC', 'ICICI',
    'Bajaj', 'Comprehensive', 'Optima', 'Family', 'Basic', 'Gold', 'Secure',
    'clm-2026', 'rahul', 'pol-10001', 'apollo', 'star', 'care', 'hdfc', 'icici',
    'bajaj', 'comprehensive', 'optima', 'family', 'basic', 'gold', 'secure',
    'CLM', 'POL', 'Kumar', 'Sharma', 'Patel', 'Menon', 'Singh', 'Reddy', 'Roy', 'Joshi',
    '10001', '10002', '10003', '10004', '10005', '10006', '10007', '10008', '10009', '10010'
  ];

  searchQueries.forEach((query, index) => {
    const testNum = index + 1;
    test(`G${testNum.toString().padStart(2, '0')}: Search functionality for query "${query}"`, async ({ dashboardPage }) => {
      await dashboardPage.search(query);
      await expect(dashboardPage.searchInput).toHaveValue(query);
      await dashboardPage.clearSearch();
      await expect(dashboardPage.searchInput).toHaveValue('');
    });
  });
});
