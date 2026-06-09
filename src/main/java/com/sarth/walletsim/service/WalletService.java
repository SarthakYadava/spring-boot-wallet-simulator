package com.sarth.walletsim.service;

import com.sarth.walletsim.constants.TransactionStatus;
import com.sarth.walletsim.entity.Transaction;
import com.sarth.walletsim.entity.Wallet;
import com.sarth.walletsim.exception.InsufficientBalanceException;
import com.sarth.walletsim.exception.WalletNotFoundException;
import com.sarth.walletsim.repository.TransactionRepository;
import com.sarth.walletsim.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Transactional(rollbackFor = Exception.class)
    public Transaction transferMoney(String senderUpiId, String receiverUpiId, BigDecimal amount) {
        log.info("Initiating transfer of INR {} from UPI: {} to UPI: {}", amount, senderUpiId, receiverUpiId);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }

        Wallet sender = walletRepository.findByUpiIdWithLock(senderUpiId)
                .orElseThrow(() -> new WalletNotFoundException("Sender wallet not found for UPI ID: " + senderUpiId));

        Wallet receiver = walletRepository.findByUpiIdWithLock(receiverUpiId)
                .orElseThrow(() -> new WalletNotFoundException("Receiver wallet not found for UPI ID: " + receiverUpiId));

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Transfer failed: Insufficient funds in sender's wallet.");
        }

        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        walletRepository.save(sender);
        walletRepository.save(receiver);

        Transaction tx = new Transaction();
        tx.setSenderWalletId(sender.getWalletId());
        tx.setReceiverWalletId(receiver.getWalletId());
        tx.setAmount(amount);
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setTransactionRemarks("UPI transfer from " + senderUpiId + " to " + receiverUpiId);

        Transaction savedTransaction = transactionRepository.save(tx);
        log.info("Transfer successful. Ledger Transaction ID: {}", savedTransaction.getId());
        return savedTransaction;
    }

    @Transactional(readOnly = true)
    public Wallet getWalletByUpiId(String upiId) {
        return walletRepository.findByUpiId(upiId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for UPI ID: " + upiId));
    }

    @Transactional(readOnly = true)
    public List<Transaction> getRecentTransactions(String upiId) {
        Wallet wallet = getWalletByUpiId(upiId);
        return transactionRepository.findTop25BySenderWalletIdOrReceiverWalletIdOrderByTimestampDesc(
                wallet.getWalletId(),
                wallet.getWalletId()
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transaction debitSender(String senderUpiId, String receiverUpiId, BigDecimal amount) {
        Wallet sender = walletRepository.findByUpiIdWithLock(senderUpiId)
                .orElseThrow(() -> new WalletNotFoundException("Sender wallet not found for UPI ID: " + senderUpiId));

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient funds.");
        }

        sender.setBalance(sender.getBalance().subtract(amount));
        walletRepository.save(sender);

        Transaction tx = new Transaction();
        tx.setSenderWalletId(sender.getWalletId());
        tx.setReceiverWalletId(receiverUpiId);
        tx.setAmount(amount);
        tx.setStatus(TransactionStatus.PENDING);
        tx.setTransactionRemarks("Sender debited, awaiting receiver bank confirmation.");

        return transactionRepository.save(tx);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void refundSender(String senderUpiId, BigDecimal amount, Long failedTransactionId) {
        Wallet sender = walletRepository.findByUpiIdWithLock(senderUpiId)
                .orElseThrow(() -> new WalletNotFoundException("Sender wallet not found for refund: " + senderUpiId));

        sender.setBalance(sender.getBalance().add(amount));
        walletRepository.save(sender);

        Transaction refundTx = new Transaction();
        refundTx.setSenderWalletId("REVERSAL_ENGINE");
        refundTx.setReceiverWalletId(sender.getWalletId());
        refundTx.setAmount(amount);
        refundTx.setStatus(TransactionStatus.REFUNDED);
        refundTx.setTransactionRemarks("Refund for failed transaction Ref: " + failedTransactionId);

        transactionRepository.save(refundTx);
        log.info("Compensating refund completed for UPI: {}", senderUpiId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markTransactionFailed(Long transactionId, String reason) {
        Transaction tx = transactionRepository.findById(transactionId).orElseThrow();
        tx.setStatus(TransactionStatus.FAILED);
        tx.setTransactionRemarks("Failed: " + reason);
        transactionRepository.save(tx);
    }
}
