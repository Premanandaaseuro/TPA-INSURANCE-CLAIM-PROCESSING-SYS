package com.tpa.claimprocessor.rules;

import com.tpa.claimprocessor.domain.enums.RuleSeverity;

public class RuleEvaluationResult {

    private String ruleCode;
    private String ruleName;
    private boolean passed;
    private RuleSeverity severity;
    private String details;

    public RuleEvaluationResult() {
    }

    public RuleEvaluationResult(String ruleCode, String ruleName, boolean passed, RuleSeverity severity, String details) {
        this.ruleCode = ruleCode;
        this.ruleName = ruleName;
        this.passed = passed;
        this.severity = severity;
        this.details = details;
    }

    public static RuleEvaluationResult pass(String ruleCode, String ruleName, String details) {
        return new RuleEvaluationResult(ruleCode, ruleName, true, null, details);
    }

    public static RuleEvaluationResult fail(String ruleCode, String ruleName, RuleSeverity severity, String details) {
        return new RuleEvaluationResult(ruleCode, ruleName, false, severity, details);
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public RuleSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(RuleSeverity severity) {
        this.severity = severity;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
