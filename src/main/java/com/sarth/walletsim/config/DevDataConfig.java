package com.sarth.walletsim.config;

import com.sarth.walletsim.constants.UserRole;
import com.sarth.walletsim.entity.AppUser;
import com.sarth.walletsim.entity.Wallet;
import com.sarth.walletsim.repository.AppUserRepository;
import com.sarth.walletsim.repository.WalletRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
@Profile("dev")
public class DevDataConfig {

    @Bean
    CommandLineRunner seedDemoWallets(
            WalletRepository walletRepository,
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            createUserIfMissing(
                    userRepository,
                    passwordEncoder,
                    "Demo User",
                    "user@wallet.dev",
                    "User@123",
                    UserRole.USER
            );
            createUserIfMissing(
                    userRepository,
                    passwordEncoder,
                    "Demo Admin",
                    "admin@wallet.dev",
                    "Admin@123",
                    UserRole.ADMIN
            );
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

    private void createUserIfMissing(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            String fullName,
            String email,
            String password,
            UserRole role
    ) {
        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }

        AppUser user = new AppUser();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        userRepository.save(user);
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
