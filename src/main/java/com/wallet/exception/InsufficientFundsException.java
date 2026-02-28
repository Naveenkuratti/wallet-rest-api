package com.wallet.exception;

import java.util.UUID;

public class InsufficientFundsException extends RuntimeException {

    private final UUID walletId;
    private final long balance;
    private final long requested;

    public InsufficientFundsException(UUID walletId, long balance, long requested) {
        super(String.format("Insufficient funds. Wallet %s balance: %d, requested: %d", walletId, balance, requested));
        this.walletId = walletId;
        this.balance = balance;
        this.requested = requested;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public long getBalance() {
        return balance;
    }

    public long getRequested() {
        return requested;
    }
}
