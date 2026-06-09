package com.sarth.walletsim.entity;

import com.sarth.walletsim.constants.KycDocumentType;
import com.sarth.walletsim.constants.KycStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "kyc_submissions")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KycDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false, length = 10)
    String mobileNumber;

    @Column(nullable = false, unique = true)
    String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    KycDocumentType documentType;

    @Column(nullable = false)
    String documentNumber;

    @Column(nullable = false)
    String documentFilePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    KycStatus status = KycStatus.PENDING;

    LocalDateTime uploadedAt = LocalDateTime.now();

    LocalDateTime verifiedAt;
}
