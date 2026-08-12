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
    private String claimType;
    private String billNumber;
    private LocalDate billDate;
    private String primaryDiagnosis;
    private String treatingDoctor;
    private BigDecimal roomCharges;
    private BigDecimal pharmacyCharges;
    private String claimFormRawText;
    private String combinedDocRawText;

    private String claimFormPatientName;
    private String dischargeSummaryPatientName;
    private String hospitalBillPatientName;

    private String claimFormHospitalName;
    private String dischargeSummaryHospitalName;
    private String hospitalBillHospitalName;

    private LocalDate claimFormAdmissionDate;
    private LocalDate dischargeSummaryAdmissionDate;

    private LocalDate claimFormDischargeDate;
    private LocalDate dischargeSummaryDischargeDate;

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

    public String getClaimType() {
        return claimType;
    }

    public void setClaimType(String claimType) {
        this.claimType = claimType;
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

    public String getClaimFormPatientName() {
        return claimFormPatientName;
    }

    public void setClaimFormPatientName(String claimFormPatientName) {
        this.claimFormPatientName = claimFormPatientName;
    }

    public String getDischargeSummaryPatientName() {
        return dischargeSummaryPatientName;
    }

    public void setDischargeSummaryPatientName(String dischargeSummaryPatientName) {
        this.dischargeSummaryPatientName = dischargeSummaryPatientName;
    }

    public String getHospitalBillPatientName() {
        return hospitalBillPatientName;
    }

    public void setHospitalBillPatientName(String hospitalBillPatientName) {
        this.hospitalBillPatientName = hospitalBillPatientName;
    }

    public String getClaimFormHospitalName() {
        return claimFormHospitalName;
    }

    public void setClaimFormHospitalName(String claimFormHospitalName) {
        this.claimFormHospitalName = claimFormHospitalName;
    }

    public String getDischargeSummaryHospitalName() {
        return dischargeSummaryHospitalName;
    }

    public void setDischargeSummaryHospitalName(String dischargeSummaryHospitalName) {
        this.dischargeSummaryHospitalName = dischargeSummaryHospitalName;
    }

    public String getHospitalBillHospitalName() {
        return hospitalBillHospitalName;
    }

    public void setHospitalBillHospitalName(String hospitalBillHospitalName) {
        this.hospitalBillHospitalName = hospitalBillHospitalName;
    }

    public LocalDate getClaimFormAdmissionDate() {
        return claimFormAdmissionDate;
    }

    public void setClaimFormAdmissionDate(LocalDate claimFormAdmissionDate) {
        this.claimFormAdmissionDate = claimFormAdmissionDate;
    }

    public LocalDate getDischargeSummaryAdmissionDate() {
        return dischargeSummaryAdmissionDate;
    }

    public void setDischargeSummaryAdmissionDate(LocalDate dischargeSummaryAdmissionDate) {
        this.dischargeSummaryAdmissionDate = dischargeSummaryAdmissionDate;
    }

    public LocalDate getClaimFormDischargeDate() {
        return claimFormDischargeDate;
    }

    public void setClaimFormDischargeDate(LocalDate claimFormDischargeDate) {
        this.claimFormDischargeDate = claimFormDischargeDate;
    }

    public LocalDate getDischargeSummaryDischargeDate() {
        return dischargeSummaryDischargeDate;
    }

    public void setDischargeSummaryDischargeDate(LocalDate dischargeSummaryDischargeDate) {
        this.dischargeSummaryDischargeDate = dischargeSummaryDischargeDate;
    }
}
