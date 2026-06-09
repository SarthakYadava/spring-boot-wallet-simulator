package com.sarth.walletsim.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Table(name = "wallet_accounts")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String walletId;

    @Column(unique = true, nullable = false)
    String userEmail;

    @Column(unique = true, nullable = false, length = 10)
    String mobileNumber;

    @Column(unique = true, nullable = false)
    String upiId;

    @Column(nullable = false)
    BigDecimal balance = BigDecimal.ZERO;

    @Version
    Long version;
}
