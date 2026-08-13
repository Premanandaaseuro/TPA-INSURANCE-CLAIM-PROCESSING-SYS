/**
 * Dashboard Page Object Model
 */
class DashboardPage {
  constructor(page) {
    this.page = page;
    this.newClaimBtn = page.getByRole('button', { name: 'Submit New Claim' });
    this.searchInput = page.getByPlaceholder(/Search Claim ID/i);
    this.clearDataBtn = page.getByRole('button', { name: /Clear Data/i });
    this.claimsTableRows = page.locator('tbody tr');
  }

  async goto() {
    await this.page.goto('/');
    await this.page.waitForLoadState('networkidle');
  }

  async openNewClaimModal() {
    await this.newClaimBtn.click();
    await this.page.waitForSelector('form, div[role="dialog"]', { timeout: 5000 });
  }

  async searchClaim(query) {
    await this.searchInput.fill(query);
    await this.page.waitForTimeout(500); // UI filter debounce
  }

  async filterByStatus(status) {
    const btn = this.page.getByRole('button', { name: new RegExp(status, 'i') });
    if (await btn.isVisible()) {
      await btn.click();
    }
    await this.page.waitForTimeout(500);
  }

  async openClaimDetails(claimId) {
    const row = this.page.locator(`tr:has-text("${claimId}")`);
    await row.getByRole('button', { name: /View Details|View/i }).click();
  }

  async clearAllData() {
    if (await this.clearDataBtn.isVisible()) {
      this.page.once('dialog', dialog => dialog.accept());
      await this.clearDataBtn.click();
      await this.page.waitForTimeout(1000);
    }
  }

  async getClaimRowText(claimId) {
    const row = this.page.locator(`tr:has-text("${claimId}")`);
    return await row.innerText();
  }
}

module.exports = { DashboardPage };
