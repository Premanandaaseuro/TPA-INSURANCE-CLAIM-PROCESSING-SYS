package com.tpa.claimprocessor.extraction;

import com.tpa.claimprocessor.rules.handler.impl.R04_PolicyNumberMissingRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class StructuredDataParserTest {

    private StructuredDataParserImpl parser;

    @BeforeEach
    void setUp() {
        parser = new StructuredDataParserImpl();
    }

    @Test
    void testExtractPolicyNumberCleanly() {
        String text1 = "Policy Details\nPolicy Number: POL-10001\nCustomer Name: Rahul Kumar";
        ExtractedClaimData data1 = parser.parse(text1, "");
        assertEquals("POL-10001", data1.getPolicyNumber());
        assertTrue(R04_PolicyNumberMissingRule.isValidPolicyNumber(data1.getPolicyNumber()));

        String text2 = "Policy Details\nPolicy Number: POL-2026-8899";
        ExtractedClaimData data2 = parser.parse(text2, "");
        assertEquals("POL-2026-8899", data2.getPolicyNumber());
        assertTrue(R04_PolicyNumberMissingRule.isValidPolicyNumber(data2.getPolicyNumber()));
    }

    @Test
    void testExtractClaimedAmountFormats() {
        String text1 = "Claimed Amount: INR 40000";
        ExtractedClaimData data1 = parser.parse(text1, "");
        assertEquals(new BigDecimal("40000"), data1.getClaimedAmount());

        String text2 = "Claimed Amount: ₹40,000";
        ExtractedClaimData data2 = parser.parse(text2, "");
        assertEquals(new BigDecimal("40000"), data2.getClaimedAmount());

        String text3 = "Claimed Amount: Rs. 40,000";
        ExtractedClaimData data3 = parser.parse(text3, "");
        assertEquals(new BigDecimal("40000"), data3.getClaimedAmount());

        String text4 = "Claimed Amount: Rs 40,000";
        ExtractedClaimData data4 = parser.parse(text4, "");
        assertEquals(new BigDecimal("40000"), data4.getClaimedAmount());

        String text5 = "Claimed Amount: 40000";
        ExtractedClaimData data5 = parser.parse(text5, "");
        assertEquals(new BigDecimal("40000"), data5.getClaimedAmount());
    }

    @Test
    void testMissingAndInvalidPolicyNumber() {
        // Missing policy number
        String textMissing = "Customer Name: Rahul Kumar\nAdmission Date: 2026-04-10";
        ExtractedClaimData dataMissing = parser.parse(textMissing, "");
        assertNull(dataMissing.getPolicyNumber());
        assertFalse(R04_PolicyNumberMissingRule.isValidPolicyNumber(dataMissing.getPolicyNumber()));

        // Invalid policy number "Details"
        String textDetails = "Policy Details\nPolicy Number: Details";
        ExtractedClaimData dataDetails = parser.parse(textDetails, "");
        assertNull(dataDetails.getPolicyNumber());
        assertFalse(R04_PolicyNumberMissingRule.isValidPolicyNumber(dataDetails.getPolicyNumber()));
    }
}
