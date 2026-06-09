package com.sarth.walletsim.dto;

import com.sarth.walletsim.constants.TransactionStatus;
import com.sarth.walletsim.entity.Transaction;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionResponse {
    Long id;
    String senderWalletId;
    String receiverWalletId;
    BigDecimal amount;
    TransactionStatus status;
    String remarks;
    LocalDateTime timestamp;

    public static TransactionResponse from(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .senderWalletId(transaction.getSenderWalletId())
                .receiverWalletId(transaction.getReceiverWalletId())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .remarks(transaction.getTransactionRemarks())
                .timestamp(transaction.getTimestamp())
                .build();
    }
}
