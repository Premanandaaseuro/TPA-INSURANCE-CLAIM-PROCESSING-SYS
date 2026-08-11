package com.tpa.claimprocessor.export;

import com.tpa.claimprocessor.domain.entity.Claim;
import com.tpa.claimprocessor.domain.enums.ClaimStatus;
import com.tpa.claimprocessor.domain.repository.ClaimRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PdfExportServiceTest {

    @Autowired
    private PdfExportService pdfExportService;

    @Autowired
    private ClaimRepository claimRepository;

    @Test
    void generateClaimSummaryPdf_Success() {
        Claim claim = new Claim("CLM-2026-999999");
        claim.setPolicyNumber("POL-2026-8899");
        claim.setPatientName("Rahul Sharma");
        claim.setHospitalName("Apollo Hospital");
        claim.setAdmissionDate(LocalDate.of(2026, 7, 10));
        claim.setDischargeDate(LocalDate.of(2026, 7, 15));
        claim.setClaimedAmount(new BigDecimal("45000.00"));
        claim.setStatus(ClaimStatus.APPROVED);
        claim.setDecisionReason("Auto approved");

        claimRepository.save(claim);

        byte[] pdfBytes = pdfExportService.generateClaimSummaryPdf("CLM-2026-999999");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500);
        // Header magic bytes check %PDF
        assertEquals('%', (char) pdfBytes[0]);
        assertEquals('P', (char) pdfBytes[1]);
        assertEquals('D', (char) pdfBytes[2]);
        assertEquals('F', (char) pdfBytes[3]);
    }
}
