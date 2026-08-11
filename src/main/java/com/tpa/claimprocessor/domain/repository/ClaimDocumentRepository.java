package com.tpa.claimprocessor.domain.repository;

import com.tpa.claimprocessor.domain.entity.ClaimDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimDocumentRepository extends JpaRepository<ClaimDocument, Long> {
    List<ClaimDocument> findByClaimClaimId(String claimId);
}
