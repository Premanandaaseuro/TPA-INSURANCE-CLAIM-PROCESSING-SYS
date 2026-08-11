package com.tpa.claimprocessor.controller;

import com.tpa.claimprocessor.domain.entity.Policy;
import com.tpa.claimprocessor.domain.repository.PolicyRepository;
import com.tpa.claimprocessor.util.PdfFixtureGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClaimControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PolicyRepository policyRepository;

    @BeforeEach
    void setUp() {
        if (policyRepository.findByPolicyNumber("POL-2026-8899").isEmpty()) {
            Policy policy = new Policy(
                    "POL-2026-8899",
                    "Comprehensive Health Plan",
                    "Rahul Sharma",
                    "Star Health Insurance",
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 12, 31),
                    new BigDecimal("500000.00"),
                    "ACTIVE"
            );
            policyRepository.save(policy);
        }
    }

    @Test
    void testUploadAndRetrieveClaimFlow() throws Exception {
        byte[] formBytes = PdfFixtureGenerator.generateSampleClaimFormPdf();
        byte[] combinedBytes = PdfFixtureGenerator.generateSampleCombinedHospitalDocPdf();

        MockMultipartFile claimForm = new MockMultipartFile(
                "claimForm",
                "Claim_Form_Sample.pdf",
                "application/pdf",
                formBytes
        );

        MockMultipartFile combinedDoc = new MockMultipartFile(
                "combinedHospitalDocument",
                "Combined_Summary_Bill.pdf",
                "application/pdf",
                combinedBytes
        );

        // 1. Upload 2 Files -> POST /api/claims
        String responseBody = mockMvc.perform(multipart("/api/claims")
                        .file(claimForm)
                        .file(combinedDoc))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.claimId", startsWith("CLM-")))
                .andExpect(jsonPath("$.status", is("APPROVED")))
                .andExpect(jsonPath("$.documents", hasSize(2)))
                .andReturn().getResponse().getContentAsString();

        // Extract generated claimId from response JSON
        String claimId = responseBody.replaceAll(".*\"claimId\":\"([^\"]+)\".*", "$1");

        // 2. Fetch Single Claim -> GET /api/claims/{claimId}
        mockMvc.perform(get("/api/claims/" + claimId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.claimId", is(claimId)))
                .andExpect(jsonPath("$.documents", hasSize(2)));

        // 3. Fetch All Claims -> GET /api/claims
        mockMvc.perform(get("/api/claims"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void testUploadMissingFile_Returns400() throws Exception {
        MockMultipartFile claimForm = new MockMultipartFile(
                "claimForm",
                "Claim_Form_Sample.pdf",
                "application/pdf",
                "Some PDF Content".getBytes()
        );

        // Omitting combinedHospitalDocument
        mockMvc.perform(multipart("/api/claims")
                        .file(claimForm))
                .andExpect(status().isBadRequest());
    }
}
