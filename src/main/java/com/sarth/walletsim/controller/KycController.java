package com.sarth.walletsim.controller;

import com.sarth.walletsim.constants.KycDocumentType;
import com.sarth.walletsim.dto.ApiResponse;
import com.sarth.walletsim.dto.KycMetadataDto;
import com.sarth.walletsim.entity.KycDetails;
import com.sarth.walletsim.service.KycService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
@Validated
public class KycController {

    private final KycService kycService;

    @PostMapping(value = "/kyc/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<KycDetails>> uploadKyc(
            @RequestParam("userEmail") @NotBlank(message = "Email is required to link KYC to a user") @Email String userEmail,
            @RequestParam("documentType") @NotNull(message = "Document type must be specified") KycDocumentType documentType,
            @RequestParam("documentNumber") @NotBlank(message = "Document ID number is required") String documentNumber,
            @RequestParam("mobileNumber") @NotBlank(message = "Mobile number is required") @Pattern(regexp = "^\\d{10}$", message = "Mobile number must be exactly 10 digits") String mobileNumber,
            @RequestPart("document") MultipartFile document) {

        KycMetadataDto kycDetails = new KycMetadataDto();
        kycDetails.setUserEmail(userEmail);
        kycDetails.setDocumentType(documentType);
        kycDetails.setDocumentNumber(documentNumber);
        kycDetails.setMobileNumber(mobileNumber);

        KycDetails submission = kycService.processKycUpload(kycDetails, document);

        return ResponseEntity.ok(ApiResponse.ok("KYC submitted for review", submission));
    }
}
