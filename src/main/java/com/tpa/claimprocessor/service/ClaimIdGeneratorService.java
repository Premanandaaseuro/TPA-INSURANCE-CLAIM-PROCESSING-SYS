package com.tpa.claimprocessor.service;

import com.tpa.claimprocessor.domain.repository.ClaimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ClaimIdGeneratorService {

    private final ClaimRepository claimRepository;
    private final Set<String> generatedIds = ConcurrentHashMap.newKeySet();

    public ClaimIdGeneratorService(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    @Transactional
    public synchronized String generateNextClaimId() {
        int currentYear = LocalDate.now().getYear();
        List<String> claimIds = claimRepository.findClaimIdsByYear(currentYear);

        int maxSeq = 0;
        // Check persisted claim IDs
        for (String id : claimIds) {
            maxSeq = Math.max(maxSeq, parseSequence(id));
        }
        // Check in-progress generated IDs
        for (String id : generatedIds) {
            if (id.startsWith("CLM-" + currentYear + "-")) {
                maxSeq = Math.max(maxSeq, parseSequence(id));
            }
        }

        int nextSeq = maxSeq + 1;
        String nextId = String.format("CLM-%d-%06d", currentYear, nextSeq);
        generatedIds.add(nextId);
        return nextId;
    }

    public synchronized void clearGeneratedIds() {
        generatedIds.clear();
    }

    private int parseSequence(String id) {
        try {
            String[] parts = id.split("-");
            if (parts.length == 3) {
                return Integer.parseInt(parts[2]);
            }
        } catch (NumberFormatException ignored) {
        }
        return 0;
    }
}

