package com.tpa.claimprocessor.controller;

import com.tpa.claimprocessor.export.PdfExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/claims")
@CrossOrigin(origins = "*")
public class ClaimPdfController {


    private final PdfExportService pdfExportService;

    public ClaimPdfController(PdfExportService pdfExportService) {
        this.pdfExportService = pdfExportService;
    }

    @GetMapping("/{claimId}/pdf")
    public ResponseEntity<byte[]> exportClaimSummaryPdf(@PathVariable("claimId") String claimId) {
        byte[] pdfBytes = pdfExportService.generateClaimSummaryPdf(claimId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", claimId + "_Summary.pdf");
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
