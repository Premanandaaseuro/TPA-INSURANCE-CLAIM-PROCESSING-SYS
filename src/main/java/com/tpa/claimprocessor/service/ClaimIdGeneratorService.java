package com.tpa.claimprocessor.service;

import com.tpa.claimprocessor.domain.repository.ClaimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ClaimIdGeneratorService {

    private final ClaimRepository claimRepository;

    public ClaimIdGeneratorService(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    @Transactional
    public synchronized String generateNextClaimId() {
        int currentYear = LocalDate.now().getYear();
        List<String> claimIds = claimRepository.findClaimIdsByYear(currentYear);

        int maxSeq = 0;
        for (String id : claimIds) {
            try {
                // Expected format: CLM-YYYY-XXXXXX
                String[] parts = id.split("-");
                if (parts.length == 3) {
                    int seq = Integer.parseInt(parts[2]);
                    if (seq > maxSeq) {
                        maxSeq = seq;
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }

        int nextSeq = maxSeq + 1;
        return String.format("CLM-%d-%06d", currentYear, nextSeq);
    }
}
