/**
 * API Client Utility for Playwright API testing
 */
export class ApiClient {
  /**
   * @param {import('@playwright/test').APIRequestContext} request
   * @param {string} baseUrl
   */
  constructor(request, baseUrl = 'http://localhost:7002/api/claims') {
    this.request = request;
    this.baseUrl = baseUrl;
  }

  async getAllClaims() {
    return await this.request.get(this.baseUrl);
  }

  async getClaimByClaimId(claimId) {
    return await this.request.get(`${this.baseUrl}/${claimId}`);
  }

  async getClaimDebugDetails(claimId) {
    return await this.request.get(`${this.baseUrl}/${claimId}/debug`);
  }

  async exportClaimPdf(claimId) {
    return await this.request.get(`${this.baseUrl}/${claimId}/pdf`);
  }

  async clearAllClaimData() {
    return await this.request.post(`${this.baseUrl}/clear-test-data`);
  }

  async deleteClearAllClaimData() {
    return await this.request.delete(`${this.baseUrl}/clear-test-data`);
  }

  async getClaimById(claimId) {
    return await this.getClaimByClaimId(claimId);
  }

  async getClaimDebug(claimId) {
    return await this.getClaimDebugDetails(claimId);
  }

  async getClaimPdf(claimId) {
    return await this.exportClaimPdf(claimId);
  }

  async clearTestDataPost() {
    return await this.clearAllClaimData();
  }

  async clearTestDataDelete() {
    return await this.deleteClearAllClaimData();
  }

  async optionsClaims() {
    return await this.request.fetch(this.baseUrl, { method: 'OPTIONS' });
  }

  async headClaims() {
    return await this.request.fetch(this.baseUrl, { method: 'HEAD' });
  }

  async patchClaim(claimId) {
    return await this.request.fetch(`${this.baseUrl}/${claimId}`, { method: 'PATCH' });
  }

  async putClaim(claimId) {
    return await this.request.fetch(`${this.baseUrl}/${claimId}`, { method: 'PUT' });
  }

  async submitClaimFiles(claimFormBuffer, claimFormName, combinedDocBuffer, combinedDocName) {
    const multipartData = {};
    if (claimFormBuffer) {
      multipartData.claimForm = {
        name: claimFormName || 'claimForm.pdf',
        mimeType: 'application/pdf',
        buffer: claimFormBuffer,
      };
    }
    if (combinedDocBuffer) {
      multipartData.combinedHospitalDocument = {
        name: combinedDocName || 'combinedHospitalDocument.pdf',
        mimeType: 'application/pdf',
        buffer: combinedDocBuffer,
      };
    }

    return await this.request.post(this.baseUrl, {
      multipart: multipartData,
    });
  }
}
