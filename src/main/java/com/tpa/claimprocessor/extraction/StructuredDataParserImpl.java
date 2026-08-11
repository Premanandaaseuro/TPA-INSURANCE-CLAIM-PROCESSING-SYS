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
        String policyNo = extractLineRegex(claimFormRawText, "(?i)Policy\\s*(?:Number|No|#)\\s*[:\\-]\\s*([A-Za-z0-9\\-]{3,30})");
        if (policyNo == null) {
            policyNo = extractLineRegex(fullText, "(?i)Policy\\s*(?:Number|No|#)\\s*[:\\-]\\s*([A-Za-z0-9\\-]{3,30})");
        }
        if (policyNo == null) {
            policyNo = extractLineRegex(claimFormRawText, "(?i)\\b(POL-[A-Za-z0-9\\-]+)\\b");
        }
        if (policyNo == null) {
            policyNo = extractLineRegex(fullText, "(?i)\\b(POL-[A-Za-z0-9\\-]+)\\b");
        }
        data.setPolicyNumber(cleanPolicyNumber(policyNo));

        // 2. Policy ID
        String policyId = extractLineRegex(claimFormRawText, "(?i)Policy\\s*ID\\s*[:\\-]\\s*([A-Za-z0-9\\-]{3,20})");
        if (policyId == null) {
            policyId = extractLineRegex(fullText, "(?i)Policy\\s*ID\\s*[:\\-]\\s*([A-Za-z0-9\\-]{3,20})");
        }
        data.setPolicyId(cleanString(policyId));

        // 3. Patient Name
        String patientName = extractLineRegex(claimFormRawText, "(?i)(?:Patient\\s*Name|Name\\s*of\\s*Patient)\\s*[:\\-]\\s*([A-Za-z \\.\\'-]{2,50})");
        if (patientName == null) {
            patientName = extractLineRegex(combinedDocRawText, "(?i)(?:Patient\\s*Name|Name\\s*of\\s*Patient)\\s*[:\\-]\\s*([A-Za-z \\.\\'-]{2,50})");
        }
        if (patientName == null) {
            patientName = extractLineRegex(fullText, "(?i)Patient\\s*[:\\-]\\s*([A-Za-z \\.\\'-]{2,50})");
        }
        data.setPatientName(cleanString(patientName));

        // 4. Customer Name / Policy Holder
        String customerName = extractLineRegex(claimFormRawText, "(?i)(?:Customer\\s*Name|Policy\\s*Holder|Insured\\s*Name|Insured|Proposer\\s*Name)\\s*[:\\-]\\s*([A-Za-z \\.\\'-]{2,50})");
        if (customerName == null) {
            customerName = data.getPatientName();
        }
        data.setCustomerName(cleanString(customerName));

        // 5. Carrier Name / Insurer
        String carrierName = extractLineRegex(claimFormRawText, "(?i)(?:Carrier\\s*Name|Insurance\\s*Company|Insurer|Carrier)\\s*[:\\-]\\s*([A-Za-z0-9 \\.\\,]{3,50})");
        if (carrierName == null) {
            carrierName = extractLineRegex(fullText, "(?i)(?:Carrier\\s*Name|Insurance\\s*Company|Insurer|Carrier)\\s*[:\\-]\\s*([A-Za-z0-9 \\.\\,]{3,50})");
        }
        data.setCarrierName(cleanString(carrierName));

        // 6. Policy Name
        String policyName = extractLineRegex(claimFormRawText, "(?i)(?:Policy\\s*Name|Plan\\s*Name|Scheme\\s*Name|Plan)\\s*[:\\-]\\s*([A-Za-z0-9 \\-]{3,50})");
        if (policyName == null) {
            policyName = extractLineRegex(fullText, "(?i)(?:Policy\\s*Name|Plan\\s*Name|Scheme\\s*Name|Plan)\\s*[:\\-]\\s*([A-Za-z0-9 \\-]{3,50})");
        }
        data.setPolicyName(cleanString(policyName));

        // 7. Hospital Name
        String hospitalName = extractLineRegex(combinedDocRawText, "(?i)(?:Hospital\\s*Name|Name\\s*of\\s*Hospital)\\s*[:\\-]\\s*([A-Za-z0-9 \\,\\.\\'-]{3,60})");
        if (hospitalName == null) {
            hospitalName = extractLineRegex(claimFormRawText, "(?i)(?:Hospital\\s*Name|Name\\s*of\\s*Hospital)\\s*[:\\-]\\s*([A-Za-z0-9 \\,\\.\\'-]{3,60})");
        }
        if (hospitalName == null) {
            hospitalName = extractLineRegex(fullText, "(?i)(?:Hospital|Medical\\s*Center)\\s*[:\\-]\\s*([A-Za-z0-9 \\,\\.\\'-]{3,60})");
        }
        data.setHospitalName(cleanString(hospitalName));

        // 8. Admission Date
        String admissionDateStr = extractLineRegex(claimFormRawText, "(?i)(?:Admission\\s*Date|Date\\s*of\\s*Admission|DOA)\\s*[:\\-]?\\s*(\\d{2,4}[\\/\\-]\\d{1,2}[\\/\\-]\\d{2,4})");
        if (admissionDateStr == null) {
            admissionDateStr = extractLineRegex(combinedDocRawText, "(?i)(?:Admission\\s*Date|Date\\s*of\\s*Admission|DOA)\\s*[:\\-]?\\s*(\\d{2,4}[\\/\\-]\\d{1,2}[\\/\\-]\\d{2,4})");
        }
        data.setAdmissionDate(parseDate(admissionDateStr));

        // 9. Discharge Date
        String dischargeDateStr = extractLineRegex(claimFormRawText, "(?i)(?:Discharge\\s*Date|Date\\s*of\\s*Discharge|DOD)\\s*[:\\-]?\\s*(\\d{2,4}[\\/\\-]\\d{1,2}[\\/\\-]\\d{2,4})");
        if (dischargeDateStr == null) {
            dischargeDateStr = extractLineRegex(combinedDocRawText, "(?i)(?:Discharge\\s*Date|Date\\s*of\\s*Discharge|DOD)\\s*[:\\-]?\\s*(\\d{2,4}[\\/\\-]\\d{1,2}[\\/\\-]\\d{2,4})");
        }
        data.setDischargeDate(parseDate(dischargeDateStr));

        // 10. Claimed Amount
        String claimedAmountLine = extractLineRegex(claimFormRawText, "(?i)(?:Claimed\\s*Amount|Claim\\s*Amount|Amount\\s*Claimed)\\s*[:\\-]?\\s*([^\\r\\n]+)");
        if (claimedAmountLine == null) {
            claimedAmountLine = extractLineRegex(fullText, "(?i)(?:Claimed\\s*Amount|Claim\\s*Amount|Amount\\s*Claimed)\\s*[:\\-]?\\s*([^\\r\\n]+)");
        }
        data.setClaimedAmount(parseAmount(claimedAmountLine));

        // 11. Total Bill Amount
        String totalBillLine = extractLineRegex(combinedDocRawText, "(?i)(?:Total\\s*Bill\\s*Amount|Total\\s*Bill|Grand\\s*Total|Net\\s*Amount|Total\\s*Amount)\\s*[:\\-]?\\s*([^\\r\\n]+)");
        if (totalBillLine == null) {
            totalBillLine = extractLineRegex(fullText, "(?i)(?:Total\\s*Bill\\s*Amount|Total\\s*Bill|Grand\\s*Total|Net\\s*Amount|Total\\s*Amount)\\s*[:\\-]?\\s*([^\\r\\n]+)");
        }
        data.setTotalBillAmount(parseAmount(totalBillLine));

        // 12. Claim Type
        String claimTypeStr = extractLineRegex(claimFormRawText, "(?i)(?:Claim\\s*Type|Type\\s*of\\s*Claim)\\s*[:\\-]?\\s*([A-Za-z]+)");
        data.setClaimType(cleanString(claimTypeStr));

        // 13. Bill Number
        String billNo = extractLineRegex(combinedDocRawText, "(?i)(?:Bill\\s*No|Bill\\s*Number|Invoice\\s*No)\\s*[:\\-]?\\s*([A-Z0-9\\-]{3,20})");
        data.setBillNumber(cleanString(billNo));

        // 14. Primary Diagnosis
        String diagnosis = extractLineRegex(combinedDocRawText, "(?i)(?:Diagnosis|Primary\\s*Diagnosis|Illness)\\s*[:\\-]?\\s*([A-Za-z0-9 \\,]{3,100})");
        data.setPrimaryDiagnosis(cleanString(diagnosis));

        // 15. Treating Doctor
        String doctor = extractLineRegex(combinedDocRawText, "(?i)(?:Treating\\s*Doctor|Doctor\\s*Name|Dr\\.)\\s*[:\\-]?\\s*([A-Za-z \\.]{3,40})");
        data.setTreatingDoctor(cleanString(doctor));

        // 16. Room & Pharmacy Charges
        String roomChargesLine = extractLineRegex(combinedDocRawText, "(?i)(?:Room\\s*Charges|Room\\s*Rent)\\s*[:\\-]?\\s*([^\\r\\n]+)");
        data.setRoomCharges(parseAmount(roomChargesLine));

        String pharmacyChargesLine = extractLineRegex(combinedDocRawText, "(?i)(?:Pharmacy|Medicines|Pharmacy\\s*Charges)\\s*[:\\-]?\\s*([^\\r\\n]+)");
        data.setPharmacyCharges(parseAmount(pharmacyChargesLine));

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
        if (upper.equals("N/A") || upper.equals("UNKNOWN") || upper.equals("NONE") || upper.equals("NULL")
                || upper.equals("NAME") || upper.equals("DETAILS") || upper.equals("NUMBER") || upper.equals("POLICY")) {
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
        } catch (Exception e) {
            return null;
        }
    }
}
