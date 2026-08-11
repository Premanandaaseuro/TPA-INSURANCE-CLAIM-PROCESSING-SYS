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

        String fullText = data.getClaimFormRawText() + "\n" + data.getCombinedDocRawText();

        // 1. Policy Number
        String policyNo = extractLineRegex(fullText, "(?i)Policy\\s*(?:No|Number|#)?[\\t :]*([A-Z0-9\\-]{5,25})");
        if (policyNo == null) {
            policyNo = extractLineRegex(claimFormRawText, "(?i)POL-[A-Za-z0-9\\-]+");
        }
        data.setPolicyNumber(cleanString(policyNo));

        // 2. Policy ID
        String policyId = extractLineRegex(fullText, "(?i)Policy\\s*ID[\\t :]*([A-Z0-9\\-]{4,20})");
        data.setPolicyId(cleanString(policyId));

        // 3. Patient Name
        String patientName = extractLineRegex(claimFormRawText, "(?i)Patient\\s*Name[\\t :]*([A-Za-z \\.\\'-]{2,40})");
        if (patientName == null) {
            patientName = extractLineRegex(combinedDocRawText, "(?i)Patient\\s*Name[\\t :]*([A-Za-z \\.\\'-]{2,40})");
        }
        if (patientName == null) {
            patientName = extractLineRegex(fullText, "(?i)Patient[\\t :]*([A-Za-z \\.\\'-]{2,40})");
        }
        data.setPatientName(cleanString(patientName));

        // 4. Customer Name / Policy Holder
        String customerName = extractLineRegex(claimFormRawText, "(?i)(?:Policy\\s*Holder|Insured|Customer)\\s*Name[\\t :]*([A-Za-z \\.\\'-]{2,40})");
        if (customerName == null) {
            customerName = data.getPatientName();
        }
        data.setCustomerName(cleanString(customerName));

        // 5. Carrier Name / Insurer
        String carrierName = extractLineRegex(fullText, "(?i)(?:Insurance\\s*Company|Insurer|Carrier)[\\t :]*([A-Za-z0-9 \\.\\,]{3,50})");
        if (carrierName == null && fullText.contains("Star Health")) {
            carrierName = "Star Health Insurance";
        } else if (carrierName == null && fullText.contains("HDFC ERGO")) {
            carrierName = "HDFC ERGO Health Insurance";
        }
        data.setCarrierName(cleanString(carrierName));

        // 6. Policy Name
        String policyName = extractLineRegex(fullText, "(?i)(?:Policy\\s*Name|Plan\\s*Name|Scheme)[\\t :]*([A-Za-z0-9 \\-]{3,50})");
        data.setPolicyName(cleanString(policyName));

        // 7. Hospital Name
        String hospitalName = extractLineRegex(combinedDocRawText, "(?i)Hospital\\s*Name[\\t :]*([A-Za-z0-9 \\,\\.\\'-]{3,50})");
        if (hospitalName == null) {
            hospitalName = extractLineRegex(claimFormRawText, "(?i)Hospital\\s*Name[\\t :]*([A-Za-z0-9 \\,\\.\\'-]{3,50})");
        }
        if (hospitalName == null) {
            hospitalName = extractLineRegex(fullText, "(?i)(?:Hospital|Medical\\s*Center)[\\t :]*([A-Za-z0-9 \\,\\.\\'-]{3,50})");
        }
        data.setHospitalName(cleanString(hospitalName));

        // 8. Admission Date
        String admissionDateStr = extractLineRegex(claimFormRawText, "(?i)(?:Admission\\s*Date|Date\\s*of\\s*Admission|DOA)[\\t :]*(\\d{2,4}[\\/\\-]\\d{1,2}[\\/\\-]\\d{2,4})");
        if (admissionDateStr == null) {
            admissionDateStr = extractLineRegex(combinedDocRawText, "(?i)(?:Admission\\s*Date|Date\\s*of\\s*Admission|DOA)[\\t :]*(\\d{2,4}[\\/\\-]\\d{1,2}[\\/\\-]\\d{2,4})");
        }
        data.setAdmissionDate(parseDate(admissionDateStr));

        // 9. Discharge Date
        String dischargeDateStr = extractLineRegex(claimFormRawText, "(?i)(?:Discharge\\s*Date|Date\\s*of\\s*Discharge|DOD)[\\t :]*(\\d{2,4}[\\/\\-]\\d{1,2}[\\/\\-]\\d{2,4})");
        if (dischargeDateStr == null) {
            dischargeDateStr = extractLineRegex(combinedDocRawText, "(?i)(?:Discharge\\s*Date|Date\\s*of\\s*Discharge|DOD)[\\t :]*(\\d{2,4}[\\/\\-]\\d{1,2}[\\/\\-]\\d{2,4})");
        }
        data.setDischargeDate(parseDate(dischargeDateStr));

        // 10. Claimed Amount
        String claimedAmountStr = extractLineRegex(claimFormRawText, "(?i)(?:Claimed\\s*Amount|Claim\\s*Amount|Amount\\s*Claimed)[\\t :]*₹?\\s*([\\d\\,\\.]+)");
        data.setClaimedAmount(parseAmount(claimedAmountStr));

        // 11. Total Bill Amount
        String totalBillStr = extractLineRegex(combinedDocRawText, "(?i)(?:Total\\s*Bill\\s*Amount|Total\\s*Bill|Grand\\s*Total|Net\\s*Amount|Total\\s*Amount)[\\t :]*₹?\\s*([\\d\\,\\.]+)");
        if (totalBillStr == null) {
            totalBillStr = extractLineRegex(fullText, "(?i)(?:Total\\s*Bill\\s*Amount|Total\\s*Bill|Grand\\s*Total)[\\t :]*₹?\\s*([\\d\\,\\.]+)");
        }
        data.setTotalBillAmount(parseAmount(totalBillStr));

        // 12. Bill Number
        String billNo = extractLineRegex(combinedDocRawText, "(?i)(?:Bill\\s*No|Bill\\s*Number|Invoice\\s*No)[\\t :]*([A-Z0-9\\-]{3,20})");
        data.setBillNumber(cleanString(billNo));

        // 13. Primary Diagnosis
        String diagnosis = extractLineRegex(combinedDocRawText, "(?i)(?:Diagnosis|Primary\\s*Diagnosis|Illness)[\\t :]*([A-Za-z0-9 \\,]{3,100})");
        data.setPrimaryDiagnosis(cleanString(diagnosis));

        // 14. Treating Doctor
        String doctor = extractLineRegex(combinedDocRawText, "(?i)(?:Treating\\s*Doctor|Doctor\\s*Name|Dr\\.)[\\t :]*([A-Za-z \\.]{3,40})");
        data.setTreatingDoctor(cleanString(doctor));

        // 15. Room & Pharmacy Charges
        String roomChargesStr = extractLineRegex(combinedDocRawText, "(?i)(?:Room\\s*Charges|Room\\s*Rent)[\\t :]*₹?\\s*([\\d\\,\\.]+)");
        data.setRoomCharges(parseAmount(roomChargesStr));

        String pharmacyChargesStr = extractLineRegex(combinedDocRawText, "(?i)(?:Pharmacy|Medicines|Pharmacy\\s*Charges)[\\t :]*₹?\\s*([\\d\\,\\.]+)");
        data.setPharmacyCharges(parseAmount(pharmacyChargesStr));

        return data;
    }

    private String extractLineRegex(String text, String regex) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        String[] lines = text.split("\\r?\\n");
        Pattern pattern = Pattern.compile(regex);

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                if (matcher.groupCount() >= 1) {
                    return matcher.group(1).trim();
                }
                return matcher.group(0).trim();
            }
        }
        return null;
    }

    private String cleanString(String str) {
        if (str == null) return null;
        String cleaned = str.trim();
        return cleaned.isEmpty() ? null : cleaned;
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
            String cleaned = amountStr.replaceAll("[^0-9\\.]", "");
            if (cleaned.isEmpty()) return null;
            return new BigDecimal(cleaned);
        } catch (Exception e) {
            return null;
        }
    }
}
