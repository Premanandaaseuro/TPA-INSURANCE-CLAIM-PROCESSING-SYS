/**
 * LoginPage Object Model
 */
export class LoginPage {
  /**
   * @param {import('@playwright/test').Page} page
   */
  constructor(page) {
    this.page = page;
  }

  async navigate() {
    await this.page.goto('/');
  }
}
