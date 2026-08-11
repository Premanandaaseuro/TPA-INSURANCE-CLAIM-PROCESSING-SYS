package com.tpa.claimprocessor.extraction;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExtractedClaimData {

    private String policyNumber;
    private String policyId;
    private String customerName;
    private String carrierName;
    private String policyName;
    private String patientName;
    private String hospitalName;
    private LocalDate admissionDate;
    private LocalDate dischargeDate;
    private BigDecimal claimedAmount;
    private BigDecimal totalBillAmount;
    private String billNumber;
    private LocalDate billDate;
    private String primaryDiagnosis;
    private String treatingDoctor;
    private BigDecimal roomCharges;
    private BigDecimal pharmacyCharges;
    private String claimFormRawText;
    private String combinedDocRawText;

    public ExtractedClaimData() {
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

    public BigDecimal getTotalBillAmount() {
        return totalBillAmount;
    }

    public void setTotalBillAmount(BigDecimal totalBillAmount) {
        this.totalBillAmount = totalBillAmount;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public LocalDate getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDate billDate) {
        this.billDate = billDate;
    }

    public String getPrimaryDiagnosis() {
        return primaryDiagnosis;
    }

    public void setPrimaryDiagnosis(String primaryDiagnosis) {
        this.primaryDiagnosis = primaryDiagnosis;
    }

    public String getTreatingDoctor() {
        return treatingDoctor;
    }

    public void setTreatingDoctor(String treatingDoctor) {
        this.treatingDoctor = treatingDoctor;
    }

    public BigDecimal getRoomCharges() {
        return roomCharges;
    }

    public void setRoomCharges(BigDecimal roomCharges) {
        this.roomCharges = roomCharges;
    }

    public BigDecimal getPharmacyCharges() {
        return pharmacyCharges;
    }

    public void setPharmacyCharges(BigDecimal pharmacyCharges) {
        this.pharmacyCharges = pharmacyCharges;
    }

    public String getClaimFormRawText() {
        return claimFormRawText;
    }

    public void setClaimFormRawText(String claimFormRawText) {
        this.claimFormRawText = claimFormRawText;
    }

    public String getCombinedDocRawText() {
        return combinedDocRawText;
    }

    public void setCombinedDocRawText(String combinedDocRawText) {
        this.combinedDocRawText = combinedDocRawText;
    }
}
