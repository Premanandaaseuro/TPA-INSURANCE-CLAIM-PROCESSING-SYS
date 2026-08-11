package com.tpa.claimprocessor.dto;

import com.tpa.claimprocessor.domain.enums.RuleSeverity;
import java.time.LocalDateTime;

public class ClaimRuleResultDto {

    private Long id;
    private String ruleCode;
    private String ruleName;
    private boolean passed;
    private RuleSeverity severity;
    private String details;
    private LocalDateTime evaluatedAt;

    public ClaimRuleResultDto() {}

    public ClaimRuleResultDto(Long id, String ruleCode, String ruleName, boolean passed,
                               RuleSeverity severity, String details, LocalDateTime evaluatedAt) {
        this.id = id;
        this.ruleCode = ruleCode;
        this.ruleName = ruleName;
        this.passed = passed;
        this.severity = severity;
        this.details = details;
        this.evaluatedAt = evaluatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }

    public RuleSeverity getSeverity() { return severity; }
    public void setSeverity(RuleSeverity severity) { this.severity = severity; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}
