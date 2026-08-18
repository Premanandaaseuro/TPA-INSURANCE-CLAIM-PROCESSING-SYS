/**
 * NewClaimModalPage Object Model
 */
export class NewClaimModalPage {
  /**
   * @param {import('@playwright/test').Page} page
   */
  constructor(page) {
    this.page = page;
    this.modalContainer = page.getByTestId('new-claim-modal');
    this.modalTitle = page.getByTestId('modal-title');
    this.closeButton = page.getByTestId('modal-close-button');
    this.form = page.getByTestId('new-claim-form');
    this.errorMessage = page.getByTestId('upload-error-message');
    this.claimFormInput = page.getByTestId('claim-form-input');
    this.combinedDocInput = page.getByTestId('combined-doc-input');
    this.cancelButton = page.getByTestId('cancel-claim-button');
    this.submitButton = page.getByTestId('submit-claim-button');
  }

  async attachClaimForm(filePath) {
    await this.claimFormInput.setInputFiles(filePath);
  }

  async attachCombinedDoc(filePath) {
    await this.combinedDocInput.setInputFiles(filePath);
  }

  async submit() {
    await this.submitButton.click();
  }

  async close() {
    await this.closeButton.click();
  }

  async cancel() {
    await this.cancelButton.click();
  }
}
