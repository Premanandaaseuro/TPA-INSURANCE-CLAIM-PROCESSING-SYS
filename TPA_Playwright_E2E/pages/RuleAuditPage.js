/**
 * Rule Audit Page Object Model for R01 to R10 verification
 */
class RuleAuditPage {
  constructor(page) {
    this.page = page;
  }

  async getRuleRowText(ruleCode) {
    const row = this.page.locator(`tr:has-text("${ruleCode}")`);
    await row.waitFor({ state: 'visible', timeout: 5000 });
    return await row.innerText();
  }

  async verifyRuleResult(ruleCode, expectedResult, expectedSeverity = null) {
    const rowText = await this.getRuleRowText(ruleCode);
    if (!rowText.includes(expectedResult)) {
      throw new Error(`Expected rule ${ruleCode} to have result '${expectedResult}', but row text was: '${rowText}'`);
    }
    if (expectedSeverity) {
      const normalizedSeverity = expectedSeverity === 'NEEDS_MANUAL_REVIEW' ? 'MANUAL REVIEW' : expectedSeverity;
      if (!rowText.includes(normalizedSeverity) && !rowText.includes(expectedSeverity)) {
        throw new Error(`Expected rule ${ruleCode} to have severity '${expectedSeverity}' or '${normalizedSeverity}', but row text was: '${rowText}'`);
      }
    }
    return true;
  }

  async getAllRulesCount() {
    const rows = this.page.locator('tbody tr:has-text("R0"), tbody tr:has-text("R10")');
    return await rows.count();
  }
}

module.exports = { RuleAuditPage };
