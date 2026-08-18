package com.tpa.claimprocessor.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tpa.claimprocessor.domain.entity.Policy;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    Optional<Policy> findByPolicyNumber(String policyNumber);
    
    boolean existsByPolicyNumber(String policyNumber);
    
    List<Policy> findByStatus(String status);
}
