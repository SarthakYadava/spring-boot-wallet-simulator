package com.sarth.walletsim.dto;

import com.sarth.walletsim.constants.KycDocumentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level= AccessLevel.PRIVATE)
public class KycMetadataDto {

    @NotBlank(message = "Email is required to link KYC to a user")
    @Email
     String userEmail;

    @NotNull(message = "Document type must be specified")
     KycDocumentType documentType;

    @NotBlank(message = "Document ID number is required")
     String documentNumber;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Mobile number must be exactly 10 digits")
    String mobileNumber;
}