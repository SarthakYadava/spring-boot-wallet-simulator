package com.sarth.walletsim.service;

import com.sarth.walletsim.entity.KycDetails;
import com.sarth.walletsim.constants.KycStatus;
import com.sarth.walletsim.entity.Wallet;
import com.sarth.walletsim.exception.WalletNotFoundException;
import com.sarth.walletsim.repository.KycRepository;
import com.sarth.walletsim.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class KycAdminService {

    private final KycRepository kycRepository;
    private final WalletRepository walletRepository;

    @Transactional(rollbackFor = Exception.class)
    public Wallet approveKycAndActivateWallet(Long kycId, String adminRemarks) {

        KycDetails kyc = kycRepository.findById(kycId)
                .orElseThrow(() -> new RuntimeException("KYC record not found"));

        if (kyc.getStatus() == KycStatus.APPROVED) {
            throw new IllegalStateException("KYC is already approved.");
        }

        kyc.setStatus(KycStatus.APPROVED);
        kyc.setVerifiedAt(LocalDateTime.now());
        kycRepository.save(kyc);

        String generatedUpiId = kyc.getMobileNumber() + "@upi";

        Wallet newWallet = new Wallet();
        newWallet.setUserEmail(kyc.getUserEmail());
        newWallet.setMobileNumber(kyc.getMobileNumber());
        newWallet.setUpiId(generatedUpiId);
        newWallet.setBalance(BigDecimal.ZERO);

        Wallet savedWallet = walletRepository.save(newWallet);

        log.info("KYC approved for {}. Wallet generated. Assigned UPI ID: {}", kyc.getUserEmail(), generatedUpiId);
        return savedWallet;
    }
}
