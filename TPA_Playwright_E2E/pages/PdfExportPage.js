const fs = require('fs');

/**
 * PDF Export Page Object Model
 */
class PdfExportPage {
  constructor(page) {
    this.page = page;
  }

  async verifyPdfDownload(claimId) {
    const exportBtn = this.page.getByRole('link', { name: /Export Summary PDF/i });
    await exportBtn.waitFor({ state: 'visible', timeout: 5000 });

    const downloadPromise = this.page.waitForEvent('download');
    await exportBtn.click();
    const download = await downloadPromise;

    const fileName = download.suggestedFilename();
    const filePath = await download.path();

    if (!filePath || !fs.existsSync(filePath)) {
      throw new Error(`PDF download failed for claim ${claimId}`);
    }

    const fileSize = fs.statSync(filePath).size;
    if (fileSize === 0) {
      throw new Error(`Downloaded PDF file is empty (0 bytes) for claim ${claimId}`);
    }

    return { fileName, filePath, fileSize };
  }
}

module.exports = { PdfExportPage };
