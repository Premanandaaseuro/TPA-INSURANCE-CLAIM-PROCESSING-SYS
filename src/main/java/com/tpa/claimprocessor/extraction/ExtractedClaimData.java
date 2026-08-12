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

    public static class ExtractedFieldMetadata {
        private String fieldName;
        private String value;
        private String sourceDocument;
        private int pageNumber = 1;
        private double confidence = 0.95;

        public ExtractedFieldMetadata() {}

        public ExtractedFieldMetadata(String fieldName, String value, String sourceDocument, int pageNumber, double confidence) {
            this.fieldName = fieldName;
            this.value = value;
            this.sourceDocument = sourceDocument;
            this.pageNumber = pageNumber;
            this.confidence = confidence;
        }

        public String getFieldName() { return fieldName; }
        public void setFieldName(String fieldName) { this.fieldName = fieldName; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        public String getSourceDocument() { return sourceDocument; }
        public void setSourceDocument(String sourceDocument) { this.sourceDocument = sourceDocument; }
        public int getPageNumber() { return pageNumber; }
        public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
    }

    private java.util.List<ExtractedFieldMetadata> fieldMetadataList = new java.util.ArrayList<>();

    public java.util.List<ExtractedFieldMetadata> getFieldMetadataList() {
        return fieldMetadataList;
    }

    public void addFieldMetadata(String fieldName, String value, String sourceDocument, int pageNumber, double confidence) {
        if (value != null && !value.trim().isEmpty()) {
            this.fieldMetadataList.add(new ExtractedFieldMetadata(fieldName, value, sourceDocument, pageNumber, confidence));
        }
    }
}
