package com.sarth.walletsim.service;

import com.sarth.walletsim.entity.Transaction;
import com.sarth.walletsim.constants.TransactionStatus;
import com.sarth.walletsim.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionAuditService {

    private final TransactionRepository transactionRepository;

    // REQUIRES_NEW ensures this ledger entry is saved immediately and won't be erased when the parent transaction rolls back.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedTransaction(String senderWalletId, String receiverUpiId, BigDecimal amount, String errorMsg) {

        Transaction failedTx = new Transaction();
        failedTx.setSenderWalletId(senderWalletId);
        failedTx.setReceiverWalletId(receiverUpiId);
        failedTx.setAmount(amount);
        failedTx.setStatus(TransactionStatus.FAILED);
        failedTx.setTransactionRemarks("Transfer Failed: " + errorMsg);

        transactionRepository.save(failedTx);
        log.info("Persisted FAILED ledger entry for audit trail.");
    }
}