/**
 * ClearDataModalPage Object Model
 */
export class ClearDataModalPage {
  /**
   * @param {import('@playwright/test').Page} page
   */
  constructor(page) {
    this.page = page;
    this.modalContainer = page.getByTestId('clear-data-modal');
    this.cancelButton = page.getByTestId('cancel-clear-data-button');
    this.confirmButton = page.getByTestId('confirm-clear-data-button');
  }

  async confirmClear() {
    await this.confirmButton.click();
  }

  async cancelClear() {
    await this.cancelButton.click();
  }
}
