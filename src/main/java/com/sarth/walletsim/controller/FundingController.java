package com.sarth.walletsim.controller;

import com.sarth.walletsim.dto.ApiResponse;
import com.sarth.walletsim.dto.FundingRequest;
import com.sarth.walletsim.dto.WalletSummaryResponse;
import com.sarth.walletsim.entity.Wallet;
import com.sarth.walletsim.service.WalletFundingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class FundingController {

    private final WalletFundingService fundingService;

    @PostMapping("/fund")
    public ResponseEntity<ApiResponse<WalletSummaryResponse>> fundWallet(@Valid @RequestBody FundingRequest request) {

        Wallet wallet = fundingService.addFundsFromBank(
                request.getUpiId(),
                request.getAmount(),
                request.getBankReferenceId()
        );
        return ResponseEntity.ok(ApiResponse.ok("Wallet funded successfully", WalletSummaryResponse.from(wallet)));
    }
}
