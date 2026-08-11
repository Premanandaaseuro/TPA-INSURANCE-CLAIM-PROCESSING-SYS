package com.tpa.claimprocessor.extraction;

public interface StructuredDataParser {

    ExtractedClaimData parse(String claimFormRawText, String combinedDocRawText);
}
