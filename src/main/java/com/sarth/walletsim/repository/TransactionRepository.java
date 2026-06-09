package com.sarth.walletsim.repository;

import com.sarth.walletsim.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findTop25BySenderWalletIdOrReceiverWalletIdOrderByTimestampDesc(
            String senderWalletId,
            String receiverWalletId
    );
}
