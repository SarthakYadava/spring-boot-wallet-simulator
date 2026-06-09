package com.sarth.walletsim.service;

import com.sarth.walletsim.constants.TransactionStatus;
import com.sarth.walletsim.entity.Transaction;
import com.sarth.walletsim.entity.Wallet;
import com.sarth.walletsim.exception.WalletNotFoundException;
import com.sarth.walletsim.repository.TransactionRepository;
import com.sarth.walletsim.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletFundingService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Transactional(rollbackFor = Exception.class)
    public Wallet addFundsFromBank(String upiId, BigDecimal amount, String bankReferenceId) {
        log.info("Received funding request for UPI: {}. Amount: INR {}, Bank Ref: {}", upiId, amount, bankReferenceId);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be strictly positive");
        }

        Wallet wallet = walletRepository.findByUpiIdWithLock(upiId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found or KYC not approved for UPI ID: " + upiId));

        wallet.setBalance(wallet.getBalance().add(amount));
        Wallet savedWallet = walletRepository.save(wallet);

        Transaction depositTx = new Transaction();
        depositTx.setSenderWalletId("BANK_RAIL");
        depositTx.setReceiverWalletId(wallet.getWalletId());
        depositTx.setAmount(amount);
        depositTx.setStatus(TransactionStatus.SUCCESS);
        depositTx.setTransactionRemarks("Bank funding reference: " + bankReferenceId);
        transactionRepository.save(depositTx);

        log.info("Successfully funded INR {} to UPI: {}. Ledger Transaction ID: {}", amount, upiId, depositTx.getId());
        return savedWallet;
    }
}
