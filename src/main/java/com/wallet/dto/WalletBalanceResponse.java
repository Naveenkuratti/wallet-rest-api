package com.wallet.dto;

import java.util.UUID;

public class WalletBalanceResponse {

    private UUID walletId;
    private long balance;

    public WalletBalanceResponse(UUID walletId, long balance) {
        this.walletId = walletId;
        this.balance = balance;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public void setWalletId(UUID walletId) {
        this.walletId = walletId;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }
}
