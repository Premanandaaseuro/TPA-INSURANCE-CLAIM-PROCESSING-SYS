import { test, expect } from '../../fixtures/test-fixtures.js';

test.describe('Category A – Application & UI Smoke Tests (50 Tests)', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    // Make sure data is cleared for smoke visibility baseline
    const clearBtn = page.getByTestId('header-clear-data-button');
    if (await clearBtn.isVisible()) {
      await clearBtn.click();
      const confirmBtn = page.getByTestId('confirm-clear-data-button');
      if (await confirmBtn.isVisible()) {
        await confirmBtn.click();
      }
    }
  });

  const uiElements = [
    { id: 'A01', desc: 'Home page URL matches root path', check: async (page) => expect(page.url()).toContain(':7001/') },
    { id: 'A02', desc: 'Main header element is rendered', check: async (page) => expect(page.locator('header')).toBeVisible() },
    { id: 'A03', desc: 'Header brand container is visible', check: async (page, p) => expect(p.headerPage.brand).toBeVisible() },
    { id: 'A04', desc: 'Brand title has correct text', check: async (page, p) => expect(p.headerPage.title).toHaveText('TPA ClaimEngine') },
    { id: 'A05', desc: 'Brand subtitle has correct text', check: async (page, p) => expect(p.headerPage.subtitle).toHaveText('Third-Party Administrator Adjudication Portal') },
    { id: 'A06', desc: 'Header Clear Data button is visible', check: async (page, p) => expect(p.headerPage.clearDataButton).toBeVisible() },
    { id: 'A07', desc: 'Header Refresh button is visible', check: async (page, p) => expect(p.headerPage.refreshButton).toBeVisible() },
    { id: 'A08', desc: 'Header Dashboard button is visible', check: async (page, p) => expect(p.headerPage.dashboardButton).toBeVisible() },
    { id: 'A09', desc: 'Header Submit Claim button is visible', check: async (page, p) => expect(p.headerPage.submitClaimButton).toBeVisible() },
    { id: 'A10', desc: 'Main content tag exists', check: async (page) => expect(page.locator('main')).toBeVisible() },
    { id: 'A11', desc: 'Main content container has responsive classes', check: async (page) => expect(page.locator('main')).toHaveClass(/max-w-7xl/) },
    { id: 'A12', desc: 'Grid layout of metrics cards is visible', check: async (page) => expect(page.locator('[data-testid="metric-card-total-submissions"]').locator('xpath=..')).toHaveClass(/grid/) },
    { id: 'A13', desc: 'Total Submissions metric card is rendered', check: async (page, p) => expect(p.dashboardPage.metricTotal).toBeVisible() },
    { id: 'A14', desc: 'Auto Approved metric card is rendered', check: async (page, p) => expect(p.dashboardPage.metricApproved).toBeVisible() },
    { id: 'A15', desc: 'Needs Review metric card is rendered', check: async (page, p) => expect(p.dashboardPage.metricReview).toBeVisible() },
    { id: 'A16', desc: 'Rejected Claims metric card is rendered', check: async (page, p) => expect(p.dashboardPage.metricRejected).toBeVisible() },
    { id: 'A17', desc: 'Total Submissions card title is correct', check: async (page, p) => expect(p.dashboardPage.metricTotal).toContainText('Total Submissions') },
    { id: 'A18', desc: 'Auto Approved card title is correct', check: async (page, p) => expect(p.dashboardPage.metricApproved).toContainText('Auto Approved') },
    { id: 'A19', desc: 'Needs Review card title is correct', check: async (page, p) => expect(p.dashboardPage.metricReview).toContainText('Needs Review') },
    { id: 'A20', desc: 'Rejected Claims card title is correct', check: async (page, p) => expect(p.dashboardPage.metricRejected).toContainText('Rejected Claims') },
    { id: 'A21', desc: 'Total Submissions initial value is 0', check: async (page, p) => expect(p.dashboardPage.metricTotalValue).toHaveText('0') },
    { id: 'A22', desc: 'Auto Approved initial value is 0', check: async (page, p) => expect(p.dashboardPage.metricApprovedValue).toHaveText('0') },
    { id: 'A23', desc: 'Needs Review initial value is 0', check: async (page, p) => expect(p.dashboardPage.metricReviewValue).toHaveText('0') },
    { id: 'A24', desc: 'Rejected Claims initial value is 0', check: async (page, p) => expect(p.dashboardPage.metricRejectedValue).toHaveText('0') },
    { id: 'A25', desc: 'Metric card styling includes border-slate-200', check: async (page, p) => expect(p.dashboardPage.metricTotal).toHaveClass(/border-slate-200/) },
    { id: 'A26', desc: 'Metric card values use extra bold typography', check: async (page, p) => expect(p.dashboardPage.metricTotalValue).toHaveClass(/font-extrabold/) },
    { id: 'A27', desc: 'Search input container is visible', check: async (page) => expect(page.locator('input[data-testid="search-input"]').locator('xpath=..')).toBeVisible() },
    { id: 'A28', desc: 'Search input box is visible', check: async (page, p) => expect(p.dashboardPage.searchInput).toBeVisible() },
    { id: 'A29', desc: 'Search input placeholder text is correct', check: async (page, p) => expect(p.dashboardPage.searchInput).toHaveAttribute('placeholder', 'Search Claim ID, Patient, Policy...') },
    { id: 'A30', desc: 'Status filters pill container is visible', check: async (page, p) => expect(p.dashboardPage.statusFiltersContainer).toBeVisible() },
    { id: 'A31', desc: 'Filter ALL pill is visible', check: async (page, p) => expect(p.dashboardPage.filterAll).toBeVisible() },
    { id: 'A32', desc: 'Filter APPROVED pill is visible', check: async (page, p) => expect(p.dashboardPage.filterApproved).toBeVisible() },
    { id: 'A33', desc: 'Filter Review Queue pill is visible', check: async (page, p) => expect(p.dashboardPage.filterReview).toBeVisible() },
    { id: 'A34', desc: 'Filter REJECTED pill is visible', check: async (page, p) => expect(p.dashboardPage.filterRejected).toBeVisible() },
    { id: 'A35', desc: 'Filter ALL pill has active text color white', check: async (page, p) => expect(p.dashboardPage.filterAll).toHaveClass(/text-white/) },
    { id: 'A36', desc: 'Filter APPROVED pill is inactive initially', check: async (page, p) => expect(p.dashboardPage.filterApproved).toHaveClass(/bg-slate-100/) },
    { id: 'A37', desc: 'Filter Review Queue pill is inactive initially', check: async (page, p) => expect(p.dashboardPage.filterReview).toHaveClass(/bg-slate-100/) },
    { id: 'A38', desc: 'Filter REJECTED pill is inactive initially', check: async (page, p) => expect(p.dashboardPage.filterRejected).toHaveClass(/bg-slate-100/) },
    { id: 'A39', desc: 'Claims table container is rendered', check: async (page, p) => expect(p.dashboardPage.claimsTableContainer).toBeVisible() },
    { id: 'A40', desc: 'Table container includes border shadow', check: async (page, p) => expect(p.dashboardPage.claimsTableContainer).toHaveClass(/shadow-sm/) },
    { id: 'A41', desc: 'Table empty state is rendered initially', check: async (page, p) => expect(p.dashboardPage.emptyState).toBeVisible() },
    { id: 'A42', desc: 'Table empty state includes document icon', check: async (page, p) => expect(p.dashboardPage.emptyState.locator('svg')).toBeVisible() },
    { id: 'A43', desc: 'Table empty state title is rendered', check: async (page, p) => expect(p.dashboardPage.emptyState.locator('h3')).toHaveText('No claims found') },
    { id: 'A44', desc: 'Table empty state subtitle is rendered', check: async (page, p) => expect(p.dashboardPage.emptyState.locator('p')).toContainText('No insurance claims have been ingested yet') },
    { id: 'A45', desc: 'Submit First Claim button is visible', check: async (page, p) => expect(p.dashboardPage.submitFirstClaimButton).toBeVisible() },
    { id: 'A46', desc: 'Footer container is visible', check: async (page) => expect(page.locator('footer')).toBeVisible() },
    { id: 'A47', desc: 'Footer displays correct copyright notice', check: async (page) => expect(page.locator('footer')).toContainText('TPA Health Insurance Claim Processing System') },
    { id: 'A48', desc: 'Footer layout matches layout alignment classes', check: async (page) => expect(page.locator('footer')).toHaveClass(/bg-white border-t border-slate-200/) },
    { id: 'A49', desc: 'Root HTML page background class is slate-50', check: async (page) => expect(page.locator('#root > div')).toHaveClass(/bg-slate-50/) },
    { id: 'A50', desc: 'App uses Inter font-family', check: async (page) => expect(page.locator('#root > div')).toHaveClass(/font-\[Inter\]/) },
  ];

  for (const element of uiElements) {
    test(`A-${element.id}: ${element.desc}`, async ({ page, headerPage, dashboardPage }) => {
      await element.check(page, { headerPage, dashboardPage });
    });
  }
});
