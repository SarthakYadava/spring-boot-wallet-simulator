package com.sarth.walletsim.service;

import com.sarth.walletsim.entity.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferOrchestrator {

    private final WalletService walletService;

    public void executeDistributedTransferSimulation(String senderUpiId, String receiverUpiId, BigDecimal amount) {
        log.info("Orchestrating distributed transfer of INR {} from {} to {}", amount, senderUpiId, receiverUpiId);

        Transaction pendingTx = walletService.debitSender(senderUpiId, receiverUpiId, amount);
        log.info("Debit successful. Ledger ID: {}. Attempting receiver bank confirmation...", pendingTx.getId());

        try {
            simulateReceiverBankTimeout();
        } catch (Exception e) {
            log.error("Receiver bank failed. Triggering compensation workflow...");
            walletService.markTransactionFailed(pendingTx.getId(), e.getMessage());
            walletService.refundSender(senderUpiId, amount, pendingTx.getId());
            throw new RuntimeException("Receiver bank timeout. INR " + amount + " has been refunded to your account.");
        }
    }

    private void simulateReceiverBankTimeout() {
        log.warn("Simulating 504 Gateway Timeout from a receiver bank switch...");
        throw new RuntimeException("HTTP 504: Receiver Bank Unreachable");
    }
}
