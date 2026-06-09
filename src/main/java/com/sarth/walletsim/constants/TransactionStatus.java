package com.sarth.walletsim.constants;

public enum TransactionStatus {
    PENDING,    // Money deducted, but not yet credited to receiver
    SUCCESS,    // Transfer complete
    FAILED,     // Transfer failed at receiver end
    REFUNDED    // Compensating transaction successfully returned money to sender
}

