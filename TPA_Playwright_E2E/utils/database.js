/**
 * Database Verification Utility
 */
export class DatabaseValidator {
  /**
   * @param {import('@playwright/test').APIRequestContext} request
   * @param {string} baseUrl
   */
  constructor(request, baseUrl = 'http://localhost:7002/api/claims') {
    this.request = request;
    this.baseUrl = baseUrl;
  }

  async verifyClaimInDb(claimId) {
    const res = await this.request.get(`${this.baseUrl}/${claimId}/debug`);
    if (res.status() !== 200) {
      return { exists: false, details: null };
    }
    const data = await res.json();
    return {
      exists: true,
      claim: {
        claimId: data.claimId,
        claimNumber: data.claimNumber,
        status: data.status,
        patientName: data.extractedFields?.patientName,
        hospitalName: data.extractedFields?.hospitalName,
        policyNumber: data.extractedFields?.policyNumber,
        claimedAmount: data.extractedFields?.claimedAmount,
        ruleResults: data.ruleResults,
      },
      claimId: data.claimId,
      claimNumber: data.claimNumber,
      status: data.status,
      decisionReason: data.decisionReason,
      extractedFields: data.extractedFields,
      ruleResults: data.ruleResults,
    };
  }

  async verifyClaimCleared(claimId) {
    const res = await this.request.get(`${this.baseUrl}/${claimId}`);
    return res.status() === 404 || res.status() === 500;
  }

  async getClaimsCount() {
    const res = await this.request.get(this.baseUrl);
    if (res.status() !== 200) return 0;
    const data = await res.json();
    return Array.isArray(data) ? data.length : 0;
  }
}
