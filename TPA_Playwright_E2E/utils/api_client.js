const fs = require('fs');

/**
 * Backend API Client for Playwright API Test Suite
 */
class ClaimApiClient {
  constructor(request, baseUrl = 'http://localhost:7002') {
    this.request = request;
    this.baseUrl = baseUrl;
  }

  async getAllClaims() {
    return await this.request.get(`${this.baseUrl}/api/claims`);
  }

  async getClaimById(claimId) {
    return await this.request.get(`${this.baseUrl}/api/claims/${claimId}`);
  }

  async uploadClaimFiles(claimFormPath, combinedDocPath) {
    const multipartData = {};
    if (claimFormPath && fs.existsSync(claimFormPath)) {
      multipartData.claimForm = {
        name: 'ClaimForm.pdf',
        mimeType: 'application/pdf',
        buffer: fs.readFileSync(claimFormPath)
      };
    }
    if (combinedDocPath && fs.existsSync(combinedDocPath)) {
      multipartData.combinedHospitalDocument = {
        name: 'CombinedHospitalDoc.pdf',
        mimeType: 'application/pdf',
        buffer: fs.readFileSync(combinedDocPath)
      };
    }

    return await this.request.post(`${this.baseUrl}/api/claims`, {
      multipart: multipartData
    });
  }

  async downloadClaimPdf(claimId) {
    return await this.request.get(`${this.baseUrl}/api/claims/${claimId}/pdf`);
  }

  async clearAllClaims() {
    return await this.request.post(`${this.baseUrl}/api/claims/clear-test-data`);
  }
}

module.exports = { ClaimApiClient };
