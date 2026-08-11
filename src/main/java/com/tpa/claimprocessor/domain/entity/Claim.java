package com.tpa.claimprocessor.domain.entity;

import com.tpa.claimprocessor.domain.enums.ClaimStatus;
import com.tpa.claimprocessor.domain.enums.ClaimType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "claims", indexes = {
    @Index(name = "idx_claim_id", columnList = "claim_id", unique = true),
    @Index(name = "idx_policy_number", columnList = "policy_number")
})
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "claim_id", nullable = false, unique = true)
    private String claimId;

    @Column(name = "policy_number")
    private String policyNumber;

    @Column(name = "policy_id")
    private String policyId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "carrier_name")
    private String carrierName;

    @Column(name = "policy_name")
    private String policyName;

    @Column(name = "patient_name")
    private String patientName;

    @Column(name = "hospital_name")
    private String hospitalName;

    @Column(name = "admission_date")
    private LocalDate admissionDate;

    @Column(name = "discharge_date")
    private LocalDate dischargeDate;

    @Column(name = "claimed_amount", precision = 15, scale = 2)
    private BigDecimal claimedAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "claim_type")
    private ClaimType claimType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ClaimStatus status;

    @Column(name = "decision_reason", columnDefinition = "TEXT")
    private String decisionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ClaimDocument> documents = new ArrayList<>();

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ClaimRuleResult> ruleResults = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "discharge_details_id")
    private DischargeDetails dischargeDetails;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_bill_details_id")
    private HospitalBillDetails hospitalBillDetails;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_json_id")
    private ClaimJson claimJson;

    public Claim() {
        this.createdAt = LocalDateTime.now();
        this.status = ClaimStatus.PENDING;
    }

    public Claim(String claimId) {
        this.claimId = claimId;
        this.createdAt = LocalDateTime.now();
        this.status = ClaimStatus.PENDING;
    }

    public void addDocument(ClaimDocument document) {
        documents.add(document);
        document.setClaim(this);
    }

    public void addRuleResult(ClaimRuleResult ruleResult) {
        ruleResults.add(ruleResult);
        ruleResult.setClaim(this);
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClaimId() {
        return claimId;
    }

    public void setClaimId(String claimId) {
        this.claimId = claimId;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public void setCarrierName(String carrierName) {
        this.carrierName = carrierName;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public LocalDate getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(LocalDate admissionDate) {
        this.admissionDate = admissionDate;
    }

    public LocalDate getDischargeDate() {
        return dischargeDate;
    }

    public void setDischargeDate(LocalDate dischargeDate) {
        this.dischargeDate = dischargeDate;
    }

    public BigDecimal getClaimedAmount() {
        return claimedAmount;
    }

    public void setClaimedAmount(BigDecimal claimedAmount) {
        this.claimedAmount = claimedAmount;
    }

    public ClaimType getClaimType() {
        return claimType;
    }

    public void setClaimType(ClaimType claimType) {
        this.claimType = claimType;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public void setDecisionReason(String decisionReason) {
        this.decisionReason = decisionReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public List<ClaimDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<ClaimDocument> documents) {
        this.documents = documents;
    }

    public List<ClaimRuleResult> getRuleResults() {
        return ruleResults;
    }

    public void setRuleResults(List<ClaimRuleResult> ruleResults) {
        this.ruleResults = ruleResults;
    }

    public DischargeDetails getDischargeDetails() {
        return dischargeDetails;
    }

    public void setDischargeDetails(DischargeDetails dischargeDetails) {
        this.dischargeDetails = dischargeDetails;
    }

    public HospitalBillDetails getHospitalBillDetails() {
        return hospitalBillDetails;
    }

    public void setHospitalBillDetails(HospitalBillDetails hospitalBillDetails) {
        this.hospitalBillDetails = hospitalBillDetails;
    }

    public ClaimJson getClaimJson() {
        return claimJson;
    }

    public void setClaimJson(ClaimJson claimJson) {
        this.claimJson = claimJson;
    }
}
