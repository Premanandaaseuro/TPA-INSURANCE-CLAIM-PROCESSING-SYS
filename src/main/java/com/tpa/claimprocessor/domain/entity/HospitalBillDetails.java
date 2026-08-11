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

    @Column(name = "bill_number")
    private String billNumber;

    @Column(name = "bill_date")
    private LocalDate billDate;

    @Column(name = "hospital_name")
    private String hospitalName;

    @Column(name = "patient_name")
    private String patientName;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "room_charges", precision = 15, scale = 2)
    private BigDecimal roomCharges;

    @Column(name = "pharmacy_charges", precision = 15, scale = 2)
    private BigDecimal pharmacyCharges;

    @Column(name = "consultation_charges", precision = 15, scale = 2)
    private BigDecimal consultationCharges;

    public HospitalBillDetails() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
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

    public BigDecimal getConsultationCharges() {
        return consultationCharges;
    }

    public void setConsultationCharges(BigDecimal consultationCharges) {
        this.consultationCharges = consultationCharges;
    }
}
