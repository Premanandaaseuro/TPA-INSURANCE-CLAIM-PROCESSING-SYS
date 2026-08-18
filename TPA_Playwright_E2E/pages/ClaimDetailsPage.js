/**
 * ClaimDetailsPage Object Model
 */
export class ClaimDetailsPage {
  /**
   * @param {import('@playwright/test').Page} page
   */
  constructor(page) {
    this.page = page;
    this.backToDashboardButton = page.getByTestId('back-to-dashboard-button');
    this.exportPdfLink = page.getByTestId('export-pdf-link');
    this.decisionCard = page.getByTestId('decision-card');
    this.claimDetailsId = page.getByTestId('claim-details-id');
    this.detailsPolicyNumber = page.getByTestId('details-policy-number');
    this.detailsPatientName = page.getByTestId('details-patient-name');
    this.detailsHospitalName = page.getByTestId('details-hospital-name');
    this.detailsClaimedAmount = page.getByTestId('details-claimed-amount');
    this.ruleAuditTable = page.getByTestId('rule-audit-table');
  }

  async clickBackToDashboard() {
    await this.backToDashboardButton.click();
  }

  async clickExportPdf() {
    await this.exportPdfLink.click();
  }
}
