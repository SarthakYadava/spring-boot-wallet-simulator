package com.sarth.walletsim.service;

import com.sarth.walletsim.dto.KycMetadataDto;
import com.sarth.walletsim.entity.KycDetails;
import com.sarth.walletsim.constants.KycStatus;
import com.sarth.walletsim.repository.KycRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KycService {

    private final KycRepository kycRepository;

    @Value("${kyc.upload.directory:uploads/kyc-documents}")
    private String uploadDirectory;

    @Transactional(rollbackFor = Exception.class)
    public KycDetails processKycUpload(KycMetadataDto metadata, MultipartFile document) {

        log.info("Received KYC upload request for email: {} and mobile: {}",
                metadata.getUserEmail(), metadata.getMobileNumber());

        if (document.isEmpty()) {
            log.error("KYC upload failed: Document is empty for email {}", metadata.getUserEmail());
            throw new IllegalArgumentException("Cannot upload an empty file");
        }

        // 1. Check if user already submitted KYC to prevent duplicates
        kycRepository.findByUserEmail(metadata.getUserEmail()).ifPresent(k -> {
            log.error("KYC upload failed: Existing KYC found for email {}", metadata.getUserEmail());
            throw new IllegalStateException("KYC details already exist for this user.");
        });

        // 2. Generate a secure, unique filename to prevent path traversal attacks and overwrites
        String originalFilename = StringUtils.cleanPath(document.getOriginalFilename());
        String fileExtension = "";
        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String secureFilename = UUID.randomUUID().toString() + fileExtension;

        // 3. Save the binary file to the server's file system
        Path storageDirectory = Paths.get(uploadDirectory);
        Path targetLocation = storageDirectory.resolve(secureFilename);

        try {
            // Create directories dynamically if they don't exist yet
            if (!Files.exists(storageDirectory)) {
                Files.createDirectories(storageDirectory);
            }

            // Save the file
            Files.copy(document.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("Saved KYC document securely to disk: {}", targetLocation.toString());

        } catch (IOException ex) {
            log.error("Could not store KYC file {} for mobile {}", secureFilename, metadata.getMobileNumber(), ex);
            throw new RuntimeException("Could not store file on the server. Please try again!", ex);
        }

        KycDetails kycDetails = new KycDetails();
        kycDetails.setUserEmail(metadata.getUserEmail());
        kycDetails.setMobileNumber(metadata.getMobileNumber());
        kycDetails.setDocumentType(metadata.getDocumentType());
        kycDetails.setDocumentNumber(metadata.getDocumentNumber());
        kycDetails.setDocumentFilePath(targetLocation.toString());
        kycDetails.setStatus(KycStatus.PENDING);

        KycDetails savedKyc = kycRepository.save(kycDetails);

        log.info("KYC metadata successfully saved to database and queued for Admin verification. Mobile: {}", metadata.getMobileNumber());

        return savedKyc;
    }
}
