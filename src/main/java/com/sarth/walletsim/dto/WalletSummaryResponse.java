package com.sarth.walletsim.dto;

import com.sarth.walletsim.entity.Wallet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WalletSummaryResponse {
    String walletId;
    String userEmail;
    String mobileNumber;
    String upiId;
    BigDecimal balance;

    public static WalletSummaryResponse from(Wallet wallet) {
        return WalletSummaryResponse.builder()
                .walletId(wallet.getWalletId())
                .userEmail(wallet.getUserEmail())
                .mobileNumber(wallet.getMobileNumber())
                .upiId(wallet.getUpiId())
                .balance(wallet.getBalance())
                .build();
    }
}
