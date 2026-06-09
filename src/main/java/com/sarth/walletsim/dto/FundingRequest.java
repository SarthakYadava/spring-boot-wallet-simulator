package com.sarth.walletsim.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FundingRequest {
    @NotBlank(message = "UPI ID is required")
    @Pattern(regexp = "^\\d{10}@upi$", message = "Invalid UPI ID format")
    String upiId;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    BigDecimal amount;

    @NotBlank(message = "Bank reference ID is required")
    String bankReferenceId;
}
