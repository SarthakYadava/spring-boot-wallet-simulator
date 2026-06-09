package com.sarth.walletsim.config;

import com.sarth.walletsim.entity.Wallet;
import com.sarth.walletsim.repository.WalletRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;

@Configuration
@Profile("dev")
public class DevDataConfig {

    @Bean
    CommandLineRunner seedDemoWallets(WalletRepository walletRepository) {
        return args -> {
            createWalletIfMissing(
                    walletRepository,
                    "sender.demo@example.com",
                    "9876543210",
                    "9876543210@upi"
            );
            createWalletIfMissing(
                    walletRepository,
                    "receiver.demo@example.com",
                    "9123456780",
                    "9123456780@upi"
            );
        };
    }

    private void createWalletIfMissing(
            WalletRepository walletRepository,
            String email,
            String mobileNumber,
            String upiId
    ) {
        if (walletRepository.findByUpiId(upiId).isPresent()) {
            return;
        }

        Wallet wallet = new Wallet();
        wallet.setUserEmail(email);
        wallet.setMobileNumber(mobileNumber);
        wallet.setUpiId(upiId);
        wallet.setBalance(BigDecimal.ZERO);
        walletRepository.save(wallet);
    }
}
