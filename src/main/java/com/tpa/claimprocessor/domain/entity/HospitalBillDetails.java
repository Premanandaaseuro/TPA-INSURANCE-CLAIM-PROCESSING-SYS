package com.tpa.claimprocessor.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "hospital_bill_details")
public class HospitalBillDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id")
    private Claim claim;

    @Column(name = "bill_number")
    private String billNumber;

    @Column(name = "bill_date")
    private LocalDate billDate;

    @Column(name = "hospital_name")
    private String hospitalName;

    @Column(name = "patient_name")
    private String patientName;

    @Column(name = "room_rent_charges", precision = 15, scale = 2)
    private BigDecimal roomRentCharges;

    @Column(name = "icu_charges", precision = 15, scale = 2)
    private BigDecimal icuCharges;

    @Column(name = "doctor_fee", precision = 15, scale = 2)
    private BigDecimal doctorFee;

    @Column(name = "medicine_charges", precision = 15, scale = 2)
    private BigDecimal medicineCharges;

    @Column(name = "investigation_charges", precision = 15, scale = 2)
    private BigDecimal investigationCharges;

    @Column(name = "total_bill_amount", precision = 15, scale = 2)
    private BigDecimal totalBillAmount;

    public HospitalBillDetails() {
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

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public BigDecimal getRoomRentCharges() {
        return roomRentCharges;
    }

    public void setRoomRentCharges(BigDecimal roomRentCharges) {
        this.roomRentCharges = roomRentCharges;
    }

    public BigDecimal getIcuCharges() {
        return icuCharges;
    }

    public void setIcuCharges(BigDecimal icuCharges) {
        this.icuCharges = icuCharges;
    }

    public BigDecimal getDoctorFee() {
        return doctorFee;
    }

    public void setDoctorFee(BigDecimal doctorFee) {
        this.doctorFee = doctorFee;
    }

    public BigDecimal getMedicineCharges() {
        return medicineCharges;
    }

    public void setMedicineCharges(BigDecimal medicineCharges) {
        this.medicineCharges = medicineCharges;
    }

    public BigDecimal getInvestigationCharges() {
        return investigationCharges;
    }

    public void setInvestigationCharges(BigDecimal investigationCharges) {
        this.investigationCharges = investigationCharges;
    }

    public BigDecimal getTotalBillAmount() {
        return totalBillAmount;
    }

    public void setTotalBillAmount(BigDecimal totalBillAmount) {
        this.totalBillAmount = totalBillAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalBillAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalBillAmount = totalAmount;
    }

    public BigDecimal getRoomCharges() {
        return roomRentCharges;
    }

    public void setRoomCharges(BigDecimal roomCharges) {
        this.roomRentCharges = roomCharges;
    }

    public BigDecimal getPharmacyCharges() {
        return medicineCharges;
    }

    public void setPharmacyCharges(BigDecimal pharmacyCharges) {
        this.medicineCharges = pharmacyCharges;
    }

    public BigDecimal getConsultationCharges() {
        return doctorFee;
    }

    public void setConsultationCharges(BigDecimal consultationCharges) {
        this.doctorFee = consultationCharges;
    }
}
