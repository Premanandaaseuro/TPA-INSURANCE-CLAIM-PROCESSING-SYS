/**
 * HeaderPage Object Model
 */
export class HeaderPage {
  /**
   * @param {import('@playwright/test').Page} page
   */
  constructor(page) {
    this.page = page;
    this.brand = page.getByTestId('header-brand');
    this.title = page.getByTestId('header-title');
    this.subtitle = page.getByTestId('header-subtitle');
    this.clearDataButton = page.getByTestId('header-clear-data-button');
    this.refreshButton = page.getByTestId('header-refresh-button');
    this.dashboardButton = page.getByTestId('header-dashboard-button');
    this.submitClaimButton = page.getByTestId('header-submit-claim-button');
  }

  async clickBrand() {
    await this.brand.click();
  }

  async clickClearData() {
    await this.clearDataButton.click();
  }

  async clickRefresh() {
    await this.refreshButton.click();
  }

  async clickDashboard() {
    await this.dashboardButton.click();
  }

  async clickSubmitNewClaim() {
    await this.submitClaimButton.click();
  }
}
