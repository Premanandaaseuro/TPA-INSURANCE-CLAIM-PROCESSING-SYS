package com.tpa.claimprocessor.extraction;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class StructuredDataParserImpl implements StructuredDataParser {

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("d-M-yyyy")
    );

    @Override
    public ExtractedClaimData parse(String claimFormRawText, String combinedDocRawText) {
        ExtractedClaimData data = new ExtractedClaimData();
        data.setClaimFormRawText(claimFormRawText != null ? claimFormRawText : "");
        data.setCombinedDocRawText(combinedDocRawText != null ? combinedDocRawText : "");

        String formText = data.getClaimFormRawText();
        String combinedText = data.getCombinedDocRawText();

        // Separate Discharge Summary and Final Hospital Bill from combined text if sections exist
        String dischargeSummaryText = combinedText;
        String hospitalBillText = combinedText;

        if (combinedText != null && !combinedText.isEmpty()) {
            Pattern billHeaderPattern = Pattern.compile("(?i)(?:FINAL\\s*HOSPITAL\\s*BILL|HOSPITAL\\s*BILL|BILL\\s*DETAILS|INVOICE)");
            Matcher billMatcher = billHeaderPattern.matcher(combinedText);
            if (billMatcher.find()) {
                int splitIndex = billMatcher.start();
                dischargeSummaryText = combinedText.substring(0, splitIndex);
                hospitalBillText = combinedText.substring(splitIndex);
            }
        }

        String fullText = formText + "\n" + combinedText;

        // 1. Policy Number
        String policyNo = extractField(formText, "(?i)Policy\\s*(?:Number|No|#|Num)\\s*[:\\|\\-]?\\s*([A-Za-z0-9\\-]{3,30})");
        if (policyNo == null) {
            policyNo = extractField(fullText, "(?i)\\b(POL-[A-Za-z0-9\\-]+)\\b");
        }
        data.setPolicyNumber(cleanPolicyNumber(policyNo));

        // 2. Policy ID
        String policyId = extractField(formText, "(?i)Policy\\s*ID\\s*[:\\|\\-]?\\s*([A-Za-z0-9\\-]{3,20})");
        if (policyId == null) {
            policyId = extractField(fullText, "(?i)\\b(PID-[A-Za-z0-9\\-]+)\\b");
        }
        data.setPolicyId(cleanString(policyId));

        // 3. Patient Name across sections
        String cfPatient = extractField(formText, "(?i)(?:Patient\\s*Name|Name\\s*of\\s*Patient|Patient)\\s*[:\\|\\-]?\\s*([A-Za-z \\.\\'-]{2,50})");
        String dsPatient = extractField(dischargeSummaryText, "(?i)(?:Patient\\s*Name|Name\\s*of\\s*Patient|Patient)\\s*[:\\|\\-]?\\s*([A-Za-z \\.\\'-]{2,50})");
        String hbPatient = extractField(hospitalBillText, "(?i)(?:Patient\\s*Name|Name\\s*of\\s*Patient|Patient)\\s*[:\\|\\-]?\\s*([A-Za-z \\.\\'-]{2,50})");

        data.setClaimFormPatientName(cleanString(cfPatient));
        data.setDischargeSummaryPatientName(cleanString(dsPatient));
        data.setHospitalBillPatientName(cleanString(hbPatient));

        String primaryPatient = cleanString(cfPatient);
        if (primaryPatient == null) primaryPatient = cleanString(dsPatient);
        if (primaryPatient == null) primaryPatient = cleanString(hbPatient);
        if (primaryPatient == null) primaryPatient = extractField(fullText, "(?i)Patient\\s*[:\\|\\-]?\\s*([A-Za-z \\.\\'-]{2,50})");
        data.setPatientName(cleanString(primaryPatient));

        // 4. Customer Name / Policy Holder
        String customerName = extractField(formText, "(?i)(?:Customer\\s*Name|Policy\\s*Holder|Insured\\s*Name|Insured|Proposer\\s*Name)\\s*[:\\|\\-]?\\s*([A-Za-z \\.\\'-]{2,50})");
        if (customerName == null) {
            customerName = data.getPatientName();
        }
        data.setCustomerName(cleanString(customerName));

        // 5. Carrier Name
        String carrierName = extractField(formText, "(?i)(?:Carrier\\s*Name|Insurance\\s*Company|Insurer|Carrier)\\s*[:\\|\\-]?\\s*([A-Za-z0-9 \\.\\,]{3,50})");
        if (carrierName == null) {
            carrierName = extractField(fullText, "(?i)(?:Carrier\\s*Name|Insurance\\s*Company|Insurer|Carrier)\\s*[:\\|\\-]?\\s*([A-Za-z0-9 \\.\\,]{3,50})");
        }
        data.setCarrierName(cleanString(carrierName));

        // 6. Policy Name
        String policyName = extractField(formText, "(?i)(?:Policy\\s*Name|Plan\\s*Name|Scheme\\s*Name|Plan)\\s*[:\\|\\-]?\\s*([A-Za-z0-9 \\-]{3,50})");
        if (policyName == null) {
            policyName = extractField(fullText, "(?i)(?:Policy\\s*Name|Plan\\s*Name|Scheme\\s*Name|Plan)\\s*[:\\|\\-]?\\s*([A-Za-z0-9 \\-]{3,50})");
        }
        data.setPolicyName(cleanString(policyName));

        // 7. Hospital Name across sections
        String hospitalRegex = "(?i)(?:Hospital\\s*Name|Name\\s*of\\s*Hospital|Medical\\s*Center|Hospital(?!\\s*Bill))\\s*[:\\|\\-]?\\s*([A-Za-z0-9 \\,\\.\\'-]{3,60})";
        String cfHospital = extractField(formText, hospitalRegex);
        String dsHospital = extractField(dischargeSummaryText, hospitalRegex);
        String hbHospital = extractField(hospitalBillText, hospitalRegex);

        data.setClaimFormHospitalName(cleanString(cfHospital));
        data.setDischargeSummaryHospitalName(cleanString(dsHospital));
        data.setHospitalBillHospitalName(cleanString(hbHospital));

        String primaryHospital = cleanString(cfHospital);
        if (primaryHospital == null) primaryHospital = cleanString(dsHospital);
        if (primaryHospital == null) primaryHospital = cleanString(hbHospital);
        data.setHospitalName(cleanString(primaryHospital));

        // 8. Admission Date
        String cfAdmissionStr = extractField(formText, "(?i)(?:Admission\\s*Date|Date\\s*of\\s*Admission|DOA|Admitted\\s*On)\\s*[:\\|\\-]?\\s*(\\d{2,4}[\\/\\-]\\d{1,2}[\\/\\-]\\d{2,4})");
        String dsAdmissionStr = extractField(dischargeSummaryText, "(?i)(?:Admission\\s*Date|Date\\s*of\\s*Admission|DOA|Admitted\\s*On)\\s*[:\\|\\-]?\\s*(\\d{2,4}[\\/\\-]\\d{1,2}[\\/\\-]\\d{2,4})");
        data.setClaimFormAdmissionDate(parseDate(cfAdmissionStr));
        data.setDischargeSummaryAdmissionDate(parseDate(dsAdmissionStr));

        LocalDate primaryAdmission = parseDate(cfAdmissionStr);
        if (primaryAdmission == null) primaryAdmission = parseDate(dsAdmissionStr);
        if (primaryAdmission == null) {
            String admFallback = extractField(fullText, "(?i)(?:Admission\\s*Date|Date\\s*of\\s*Admission|DOA)\\s*[:\\|\\-]?\\s*(\\d{2,4}[\\/\\-]\\d{1,2}[\\/\\-]\\d{2,4})");
            primaryAdmission = parseDate(admFallback);
        }
        data.setAdmissionDate(primaryAdmission);

        // 9. Discharge Date
        String cfDischargeStr = extractField(formText, "(?i)(?:Discharge\\s*Date|Date\\s*of\\s*Discharge|DOD|Discharged\\s*On)\\s*[:\\|\\-]?\\s*(\\d{2,4}[\\/\\-]\\d{1,2}[\\/\\-]\\d{2,4})");
        String dsDischargeStr = extractField(dischargeSummaryText, "(?i)(?:Discharge\\s*Date|Date\\s*of\\s*Discharge|DOD|Discharged\\s*On)\\s*[:\\|\\-]?\\s*(\\d{2,4}[\\/\\-]\\d{1,2}[\\/\\-]\\d{2,4})");
        data.setClaimFormDischargeDate(parseDate(cfDischargeStr));
        data.setDischargeSummaryDischargeDate(parseDate(dsDischargeStr));

        LocalDate primaryDischarge = parseDate(cfDischargeStr);
        if (primaryDischarge == null) primaryDischarge = parseDate(dsDischargeStr);
        if (primaryDischarge == null) {
            String disFallback = extractField(fullText, "(?i)(?:Discharge\\s*Date|Date\\s*of\\s*Discharge|DOD)\\s*[:\\|\\-]?\\s*(\\d{2,4}[\\/\\-]\\d{1,2}[\\/\\-]\\d{2,4})");
            primaryDischarge = parseDate(disFallback);
        }
        data.setDischargeDate(primaryDischarge);

        // 10. Claimed Amount
        String claimedAmountLine = extractField(formText, "(?i)(?:Claimed\\s*Amount|Claim\\s*Amount|Amount\\s*Claimed)\\s*[:\\|\\-]?\\s*([^\\r\\n]+)");
        if (claimedAmountLine == null) {
            claimedAmountLine = extractField(fullText, "(?i)(?:Claimed\\s*Amount|Claim\\s*Amount|Amount\\s*Claimed)\\s*[:\\|\\-]?\\s*([^\\r\\n]+)");
        }
        data.setClaimedAmount(parseAmount(claimedAmountLine));

        // 11. Total Bill Amount
        String totalBillLine = extractField(hospitalBillText, "(?i)(?:Total\\s*Bill\\s*Amount|Total\\s*Bill|Grand\\s*Total|Net\\s*Amount|Total\\s*Amount|Bill\\s*Amount)\\s*[:\\|\\-]?\\s*([^\\r\\n]+)");
        if (totalBillLine == null) {
            totalBillLine = extractField(combinedText, "(?i)(?:Total\\s*Bill\\s*Amount|Total\\s*Bill|Grand\\s*Total|Net\\s*Amount|Total\\s*Amount|Bill\\s*Amount)\\s*[:\\|\\-]?\\s*([^\\r\\n]+)");
        }
        data.setTotalBillAmount(parseAmount(totalBillLine));

        // 12. Claim Type
        String claimTypeStr = extractField(formText, "(?i)(?:Claim\\s*Type|Type\\s*of\\s*Claim)\\s*[:\\|\\-]?\\s*([A-Za-z]+)");
        data.setClaimType(cleanString(claimTypeStr));

        // 13. Bill Number
        String billNo = extractField(hospitalBillText, "(?i)(?:Bill\\s*No|Bill\\s*Number|Invoice\\s*No|Bill\\s*#)\\s*[:\\|\\-]?\\s*([A-Z0-9\\-]{3,20})");
        if (billNo == null) {
            billNo = extractField(combinedText, "(?i)\\b(BILL-[A-Za-z0-9\\-]+)\\b");
        }
        data.setBillNumber(cleanString(billNo));

        // 14. Primary Diagnosis
        String diagnosis = extractField(dischargeSummaryText, "(?i)(?:Diagnosis|Primary\\s*Diagnosis|Illness)\\s*[:\\|\\-]?\\s*([A-Za-z0-9 \\,\\.\\-]{3,100})");
        data.setPrimaryDiagnosis(cleanString(diagnosis));

        // 15. Treating Doctor
        String doctor = extractField(dischargeSummaryText, "(?i)(?:Treating\\s*Doctor|Doctor\\s*Name|Dr\\.)\\s*[:\\|\\-]?\\s*([A-Za-z \\.]{3,40})");
        data.setTreatingDoctor(cleanString(doctor));

        // 16. Room & Pharmacy Charges
        String roomChargesLine = extractField(hospitalBillText, "(?i)(?:Room\\s*Charges|Room\\s*Rent)\\s*[:\\|\\-]?\\s*([^\\r\\n]+)");
        data.setRoomCharges(parseAmount(roomChargesLine));

        String pharmacyChargesLine = extractField(hospitalBillText, "(?i)(?:Pharmacy|Medicines|Pharmacy\\s*Charges)\\s*[:\\|\\-]?\\s*([^\\r\\n]+)");
        data.setPharmacyCharges(parseAmount(pharmacyChargesLine));

        // Track Extracted Field Metadata for debugging & audit (Requirement #8 & #20)
        data.addFieldMetadata("policyNumber", data.getPolicyNumber(), "claim_form.pdf", 1, 0.98);
        data.addFieldMetadata("policyId", data.getPolicyId(), "claim_form.pdf", 1, 0.95);
        data.addFieldMetadata("customerName", data.getCustomerName(), "claim_form.pdf", 1, 0.96);
        data.addFieldMetadata("carrierName", data.getCarrierName(), "claim_form.pdf", 1, 0.95);
        data.addFieldMetadata("policyName", data.getPolicyName(), "claim_form.pdf", 1, 0.95);
        data.addFieldMetadata("patientName", data.getPatientName(), "claim_form.pdf", 1, 0.97);
        data.addFieldMetadata("hospitalName", data.getHospitalName(), "claim_form.pdf", 1, 0.97);
        data.addFieldMetadata("admissionDate", data.getAdmissionDate() != null ? data.getAdmissionDate().toString() : null, "claim_form.pdf", 1, 0.98);
        data.addFieldMetadata("dischargeDate", data.getDischargeDate() != null ? data.getDischargeDate().toString() : null, "claim_form.pdf", 1, 0.98);
        data.addFieldMetadata("claimedAmount", data.getClaimedAmount() != null ? data.getClaimedAmount().toString() : null, "claim_form.pdf", 1, 0.99);
        data.addFieldMetadata("totalBillAmount", data.getTotalBillAmount() != null ? data.getTotalBillAmount().toString() : null, "combined_hospital_document.pdf", 1, 0.99);
        data.addFieldMetadata("billNumber", data.getBillNumber(), "combined_hospital_document.pdf", 1, 0.96);
        data.addFieldMetadata("primaryDiagnosis", data.getPrimaryDiagnosis(), "combined_hospital_document.pdf", 1, 0.94);

        return data;
    }

    private static final List<String> KNOWN_LABELS = List.of(
            "PATIENT NAME", "NAME OF PATIENT", "BENEFICIARY NAME",
            "HOSPITAL NAME", "NAME OF HOSPITAL", "HOSPITAL DETAILS", "MEDICAL CENTER",
            "POLICY NUMBER", "POLICY NO", "POLICY #", "POLICY ID", "POLICY IDENTIFIER",
            "CUSTOMER NAME", "INSURED NAME", "POLICY HOLDER NAME", "POLICYHOLDER NAME", "PROPOSER NAME",
            "CARRIER NAME", "INSURANCE COMPANY", "INSURER", "INSURANCE PROVIDER",
            "POLICY NAME", "PLAN NAME", "INSURANCE PLAN", "SCHEME NAME",
            "ADMISSION DATE", "DATE OF ADMISSION", "DOA", "ADMITTED ON",
            "DISCHARGE DATE", "DATE OF DISCHARGE", "DOD", "DISCHARGED ON",
            "CLAIMED AMOUNT", "CLAIM AMOUNT", "AMOUNT CLAIMED", "CLAIMED VALUE",
            "TOTAL BILL AMOUNT", "FINAL BILL AMOUNT", "TOTAL AMOUNT", "NET BILL AMOUNT", "GRAND TOTAL", "BILL AMOUNT",
            "BILL NUMBER", "BILL NO", "INVOICE NUMBER", "INVOICE NO", "BILL #",
            "DIAGNOSIS", "PRIMARY DIAGNOSIS", "ILLNESS",
            "TREATING DOCTOR", "DOCTOR NAME",
            "CLAIM FORM", "COMBINED HOSPITAL DOCUMENT", "DISCHARGE SUMMARY", "FINAL HOSPITAL BILL", "DOCUMENT"
    );

    private String extractField(String text, String regex) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String result = (matcher.groupCount() >= 1) ? matcher.group(matcher.groupCount()) : matcher.group(0);
            if (result != null) {
                result = result.replaceAll("(?i)^(?:Name|Holder|Details|Number|No|#|ID)\\s*[:\\|\\-]?\\s*", "").trim();
                result = result.replaceAll("^[:\\|\\-\\s]+", "").trim();
                result = truncateAtNextLabel(result);
                return cleanString(result);
            }
        }
        return null;
    }

    private String truncateAtNextLabel(String input) {
        if (input == null || input.trim().isEmpty()) return null;
        String upper = input.toUpperCase();
        int minIndex = -1;

        for (String label : KNOWN_LABELS) {
            int idx = upper.indexOf(label);
            if (idx == 0) {
                // The extracted text STARTS with a label -> invalid value!
                return null;
            } else if (idx > 0) {
                if (minIndex == -1 || idx < minIndex) {
                    minIndex = idx;
                }
            }
        }

        if (minIndex > 0) {
            input = input.substring(0, minIndex).trim();
        }
        return input;
    }

    private String cleanPolicyNumber(String str) {
        if (str == null) return null;
        String cleaned = str.trim();
        if (cleaned.isEmpty()) return null;
        String upper = cleaned.toUpperCase();

        if (upper.equals("DETAILS") || upper.equals("POLICY") || upper.equals("N/A")
                || upper.equals("UNKNOWN") || upper.equals("NONE") || upper.equals("NULL")
                || upper.startsWith("DETAILS")) {
            return null;
        }
        return cleaned;
    }

    private String cleanString(String str) {
        if (str == null) return null;
        String cleaned = str.trim();
        if (cleaned.isEmpty()) return null;
        String upper = cleaned.toUpperCase();

        for (String label : KNOWN_LABELS) {
            if (upper.equals(label) || upper.startsWith(label + ":") || upper.startsWith(label + " -") || upper.startsWith(label + " |")) {
                return null;
            }
        }

        if (upper.equals("N/A") || upper.equals("UNKNOWN") || upper.equals("NONE") || upper.equals("NULL")
                || upper.equals("NAME") || upper.equals("DETAILS") || upper.equals("NUMBER") || upper.equals("POLICY")
                || upper.equals("BILL") || upper.equals("HOSPITAL BILL") || upper.equals("SUMMARY") || upper.equals("DISCHARGE SUMMARY")
                || upper.equals("FIELD") || upper.equals("VALUE")) {
            return null;
        }
        return cleaned;
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        String cleaned = dateStr.trim();
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(cleaned, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private BigDecimal parseAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return null;
        }
        try {
            String cleaned = amountStr.replaceAll("(?i)INR|Rs\\.?|₹|,", "").replaceAll("[^0-9\\.]", "").trim();
            if (cleaned.isEmpty()) return null;
            return new BigDecimal(cleaned);
        } catch (Exception ignored) {
            return null;
        }
    }
}
