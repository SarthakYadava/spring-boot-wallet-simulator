package com.sarth.walletsim.controller;

import com.sarth.walletsim.dto.ApiResponse;
import com.sarth.walletsim.dto.WalletSummaryResponse;
import com.sarth.walletsim.entity.Wallet;
import com.sarth.walletsim.service.KycAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/kyc")
@RequiredArgsConstructor
public class AdminController {

    private final KycAdminService kycAdminService;

    @PostMapping("/{kycId}/approve")
    public ResponseEntity<ApiResponse<WalletSummaryResponse>> approveKyc(@PathVariable Long kycId) {
        Wallet wallet = kycAdminService.approveKycAndActivateWallet(kycId, "Documents verified");
        return ResponseEntity.ok(ApiResponse.ok(
                "KYC approved and wallet activated",
                WalletSummaryResponse.from(wallet)
        ));
    }
}
