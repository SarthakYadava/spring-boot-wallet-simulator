package com.sarth.walletsim;

import com.sarth.walletsim.constants.KycDocumentType;
import com.sarth.walletsim.dto.KycMetadataDto;
import com.sarth.walletsim.entity.KycDetails;
import com.sarth.walletsim.entity.Transaction;
import com.sarth.walletsim.entity.Wallet;
import com.sarth.walletsim.service.KycAdminService;
import com.sarth.walletsim.service.KycService;
import com.sarth.walletsim.service.WalletFundingService;
import com.sarth.walletsim.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class WalletWorkflowIntegrationTests {

    @Autowired
    private KycService kycService;

    @Autowired
    private KycAdminService kycAdminService;

    @Autowired
    private WalletFundingService fundingService;

    @Autowired
    private WalletService walletService;

    @Test
    void kycApprovalFundingAndTransferUpdateWalletsAndLedger() {
        KycDetails senderKyc = submitKyc("sender@example.com", "9876543210", "DOC-SENDER");
        KycDetails receiverKyc = submitKyc("receiver@example.com", "9123456780", "DOC-RECEIVER");

        Wallet senderWallet = kycAdminService.approveKycAndActivateWallet(senderKyc.getId(), "verified");
        Wallet receiverWallet = kycAdminService.approveKycAndActivateWallet(receiverKyc.getId(), "verified");

        fundingService.addFundsFromBank(senderWallet.getUpiId(), new BigDecimal("500.00"), "BANK-REF-1");
        Transaction transfer = walletService.transferMoney(
                senderWallet.getUpiId(),
                receiverWallet.getUpiId(),
                new BigDecimal("125.50")
        );

        Wallet refreshedSender = walletService.getWalletByUpiId(senderWallet.getUpiId());
        Wallet refreshedReceiver = walletService.getWalletByUpiId(receiverWallet.getUpiId());
        List<Transaction> senderTransactions = walletService.getRecentTransactions(senderWallet.getUpiId());

        assertThat(refreshedSender.getBalance()).isEqualByComparingTo("374.50");
        assertThat(refreshedReceiver.getBalance()).isEqualByComparingTo("125.50");
        assertThat(transfer.getId()).isNotNull();
        assertThat(senderTransactions).hasSize(2);
    }

    private KycDetails submitKyc(String email, String mobileNumber, String documentNumber) {
        KycMetadataDto metadata = new KycMetadataDto();
        metadata.setUserEmail(email);
        metadata.setMobileNumber(mobileNumber);
        metadata.setDocumentType(KycDocumentType.NATIONAL_ID);
        metadata.setDocumentNumber(documentNumber);

        MockMultipartFile document = new MockMultipartFile(
                "document",
                "kyc.txt",
                "text/plain",
                "sample document".getBytes()
        );

        return kycService.processKycUpload(metadata, document);
    }
}
