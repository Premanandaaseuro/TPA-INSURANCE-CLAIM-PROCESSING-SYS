/**
 * DashboardPage Object Model
 */
export class DashboardPage {
  /**
   * @param {import('@playwright/test').Page} page
   */
  constructor(page) {
    this.page = page;
    this.searchInput = page.getByTestId('search-input');
    this.statusFiltersContainer = page.getByTestId('status-filters');
    this.filterAll = page.getByTestId('filter-ALL');
    this.filterApproved = page.getByTestId('filter-APPROVED');
    this.filterReview = page.getByTestId('filter-NEEDS_MANUAL_REVIEW');
    this.filterRejected = page.getByTestId('filter-REJECTED');
    this.claimsTableContainer = page.getByTestId('claims-table-container');
    this.claimsTable = page.getByTestId('claims-table');
    this.claimsTableBody = page.getByTestId('claims-table-body');
    this.emptyState = page.getByTestId('empty-claims-state');
    this.submitFirstClaimButton = page.getByTestId('submit-first-claim-button');
    this.loadingState = page.getByTestId('loading-state');
    this.metricTotal = page.getByTestId('metric-card-total-submissions');
    this.metricTotalValue = page.getByTestId('metric-card-total-submissions-value');
    this.metricApproved = page.getByTestId('metric-card-auto-approved');
    this.metricApprovedValue = page.getByTestId('metric-card-auto-approved-value');
    this.metricReview = page.getByTestId('metric-card-needs-review');
    this.metricReviewValue = page.getByTestId('metric-card-needs-review-value');
    this.metricRejected = page.getByTestId('metric-card-rejected-claims');
    this.metricRejectedValue = page.getByTestId('metric-card-rejected-claims-value');
  }

  async search(query) {
    await this.searchInput.fill(query);
    await this.page.waitForTimeout(300);
  }

  async clearSearch() {
    await this.searchInput.fill('');
    await this.page.waitForTimeout(300);
  }

  async filterByStatus(status) {
    const filterBtn = this.page.getByTestId(`filter-${status}`);
    await filterBtn.click();
    await this.page.waitForTimeout(300);
  }

  getClaimRow(claimId) {
    return this.page.getByTestId(`claim-row-${claimId}`);
  }

  async clickClaimRow(claimId) {
    const row = this.getClaimRow(claimId);
    await row.click();
  }

  async clickAuditButton(claimId) {
    const auditBtn = this.page.getByTestId(`view-audit-button-${claimId}`);
    await auditBtn.click();
  }

  async getTableRowCount() {
    const rows = this.page.locator('[data-testid^="claim-row-"]');
    return await rows.count();
  }
}
