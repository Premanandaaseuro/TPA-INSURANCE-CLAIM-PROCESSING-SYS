/**
 * Claim Details Page Object Model
 */
class ClaimDetailsPage {
  constructor(page) {
    this.page = page;
    this.statusBadge = page.locator('div.border span.rounded-full').first();
    this.decisionReasonBanner = page.locator('p:has-text("Claim"), div:has-text("Decision Reason")').first();
    this.exportPdfBtn = page.getByRole('link', { name: /Export Summary PDF/i });
    this.backToDashboardBtn = page.getByRole('button', { name: /Back to Dashboard|Back/i });
    this.auditTable = page.locator('table');
  }

  async getStatusText() {
    await this.statusBadge.waitFor({ state: 'visible', timeout: 5000 });
    return await this.statusBadge.innerText();
  }

  async getDecisionReason() {
    return await this.decisionReasonBanner.innerText();
  }

  async exportPdf() {
    const downloadPromise = this.page.waitForEvent('download');
    await this.exportPdfBtn.click();
    return await downloadPromise;
  }

  async backToDashboard() {
    if (await this.backToDashboardBtn.isVisible()) {
      await this.backToDashboardBtn.click();
    } else {
      await this.page.goto('/');
    }
  }
}

module.exports = { ClaimDetailsPage };
