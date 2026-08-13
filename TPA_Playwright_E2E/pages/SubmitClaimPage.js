/**
 * Submit Claim Page Object Model
 */
class SubmitClaimPage {
  constructor(page) {
    this.page = page;
    this.claimFileInput = page.locator('input[type="file"]').first();
    this.combinedFileInput = page.locator('input[type="file"]').nth(1);
    this.processClaimBtn = page.getByRole('button', { name: 'Ingest & Process Claim' });
    this.cancelBtn = page.getByRole('button', { name: 'Cancel' });
  }

  async uploadClaimForm(filePath) {
    await this.claimFileInput.setInputFiles(filePath);
  }

  async uploadCombinedDoc(filePath) {
    await this.combinedFileInput.setInputFiles(filePath);
  }

  async processClaim() {
    await this.processClaimBtn.click();
    // Wait for processing modal/loader & result transition
    await this.page.waitForLoadState('networkidle');
    await this.page.waitForTimeout(3000);
  }

  async cancel() {
    if (await this.cancelBtn.isVisible()) {
      await this.cancelBtn.click();
    }
  }
}

module.exports = { SubmitClaimPage };
