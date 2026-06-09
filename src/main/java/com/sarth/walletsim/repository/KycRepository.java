package com.sarth.walletsim.repository;

import com.sarth.walletsim.entity.KycDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KycRepository extends JpaRepository<KycDetails, Long> {
    Optional<KycDetails> findByUserEmail(String userEmail);
}