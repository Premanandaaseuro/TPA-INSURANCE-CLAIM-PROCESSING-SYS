package com.tpa.claimprocessor.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tpa.claimprocessor.domain.enums.RuleSeverity;
import com.tpa.claimprocessor.domain.enums.RuleStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "claim_rule_results")
public class ClaimRuleResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    @JsonIgnore
    private Claim claim;

    @Column(name = "rule_code", nullable = false)
    private String ruleCode;

    @Column(name = "rule_name", nullable = false)
    private String ruleName;

    @Column(name = "passed", nullable = false)
    private boolean passed;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RuleStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity")
    private RuleSeverity severity;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "evaluated_at")
    private LocalDateTime evaluatedAt;

    public ClaimRuleResult() {
        this.evaluatedAt = LocalDateTime.now();
    }

    public ClaimRuleResult(Claim claim, String ruleCode, String ruleName, boolean passed, RuleSeverity severity, String details) {
        this.claim = claim;
        this.ruleCode = ruleCode;
        this.ruleName = ruleName;
        this.passed = passed;
        this.status = passed ? RuleStatus.PASS : RuleStatus.FAIL;
        this.severity = severity;
        this.details = details;
        this.evaluatedAt = LocalDateTime.now();
    }

    public ClaimRuleResult(Claim claim, String ruleCode, String ruleName, boolean passed, RuleStatus status, RuleSeverity severity, String details) {
        this.claim = claim;
        this.ruleCode = ruleCode;
        this.ruleName = ruleName;
        this.passed = passed;
        this.status = status;
        this.severity = severity;
        this.details = details;
        this.evaluatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Claim getClaim() {
        return claim;
    }

    public void setClaim(Claim claim) {
        this.claim = claim;
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

    public RuleStatus getStatus() {
        return status != null ? status : (passed ? RuleStatus.PASS : RuleStatus.FAIL);
    }

    public void setStatus(RuleStatus status) {
        this.status = status;
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

    public LocalDateTime getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(LocalDateTime evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }
}
