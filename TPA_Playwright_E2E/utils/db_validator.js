/**
 * Read-Only Database & State Validator via API endpoints
 */
class DbValidator {
  constructor(apiClient) {
    this.apiClient = apiClient;
  }

  async validateClaimInDb(claimId) {
    const res = await this.apiClient.getClaimById(claimId);
    if (!res.ok()) {
      throw new Error(`Claim ${claimId} not found in database/backend (HTTP ${res.status()})`);
    }
    const data = await res.json();
    return {
      claimId: data.claimId,
      status: data.status,
      decisionReason: data.decisionReason,
      ruleResults: data.ruleResults || [],
      documents: data.documents || []
    };
  }

  async validateAllRulesPresent(claimId) {
    const claim = await this.validateClaimInDb(claimId);
    const ruleCodes = claim.ruleResults.map(r => r.ruleCode);
    const expected = ['R01', 'R02', 'R03', 'R04', 'R05', 'R06', 'R07', 'R08', 'R09', 'R10'];
    
    for (const code of expected) {
      if (!ruleCodes.includes(code)) {
        throw new Error(`Rule ${code} missing from DB claim_rule_results for claim ${claimId}`);
      }
    }
    return true;
  }
}

module.exports = { DbValidator };
