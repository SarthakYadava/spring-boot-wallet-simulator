package com.sarth.walletsim.controller;

import com.sarth.walletsim.dto.ApiResponse;
import com.sarth.walletsim.dto.TransactionResponse;
import com.sarth.walletsim.dto.TransferRequest;
import com.sarth.walletsim.dto.WalletSummaryResponse;
import com.sarth.walletsim.entity.Transaction;
import com.sarth.walletsim.entity.Wallet;
import com.sarth.walletsim.service.TransferOrchestrator;
import com.sarth.walletsim.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final TransferOrchestrator orchestrator;

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> processTransfer(@Valid @RequestBody TransferRequest request) {

        Transaction transaction = walletService.transferMoney(
                request.getSenderUpiId(),
                request.getReceiverUpiId(),
                request.getAmount()
        );

        return ResponseEntity.ok(ApiResponse.ok("Transfer completed", TransactionResponse.from(transaction)));
    }

    @PostMapping("/simulate-failure")
    public ResponseEntity<ApiResponse<String>> simulateFailedTransfer(@Valid @RequestBody TransferRequest request) {
        try {
            orchestrator.executeDistributedTransferSimulation(
                    request.getSenderUpiId(),
                    request.getReceiverUpiId(),
                    request.getAmount()
            );
            return ResponseEntity.ok(ApiResponse.ok("Failure simulation completed without refund", "SUCCESS"));
        } catch (RuntimeException ex) {
            return ResponseEntity.ok(ApiResponse.ok("Failure simulation completed with refund", ex.getMessage()));
        }
    }

    @GetMapping("/{upiId}")
    public ResponseEntity<ApiResponse<WalletSummaryResponse>> getWallet(@PathVariable String upiId) {
        Wallet wallet = walletService.getWalletByUpiId(upiId);
        return ResponseEntity.ok(ApiResponse.ok("Wallet found", WalletSummaryResponse.from(wallet)));
    }

    @GetMapping("/{upiId}/transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactions(@PathVariable String upiId) {
        List<TransactionResponse> transactions = walletService.getRecentTransactions(upiId)
                .stream()
                .map(TransactionResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok("Recent wallet transactions", transactions));
    }
}
