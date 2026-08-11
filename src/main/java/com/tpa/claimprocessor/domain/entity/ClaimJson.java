package com.tpa.claimprocessor.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "claim_jsons")
public class ClaimJson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id")
    private Claim claim;

    @Column(name = "extracted_payload", columnDefinition = "TEXT")
    private String extractedPayload;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public ClaimJson() {
        this.createdAt = LocalDateTime.now();
    }

    public ClaimJson(Claim claim, String extractedPayload) {
        this.claim = claim;
        this.extractedPayload = extractedPayload;
        this.createdAt = LocalDateTime.now();
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

    public String getExtractedPayload() {
        return extractedPayload;
    }

    public void setExtractedPayload(String extractedPayload) {
        this.extractedPayload = extractedPayload;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
