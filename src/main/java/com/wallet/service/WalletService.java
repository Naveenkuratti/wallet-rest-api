package com.wallet.service;

import com.wallet.dto.WalletBalanceResponse;
import com.wallet.dto.WalletOperationRequest;
import com.wallet.entity.Wallet;
import com.wallet.exception.InsufficientFundsException;
import com.wallet.exception.WalletNotFoundException;
import com.wallet.repository.WalletRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void executeOperation(WalletOperationRequest request) {
        UUID walletId = request.getWalletId();
        Wallet wallet = findOrCreateWalletForUpdate(walletId, request.getOperationType());

        long amount = request.getAmount();
        if (request.getOperationType() == WalletOperationRequest.OperationType.WITHDRAW) {
            if (wallet.getBalance() < amount) {
                throw new InsufficientFundsException(wallet.getId(), wallet.getBalance(), amount);
            }
            wallet.setBalance(wallet.getBalance() - amount);
        } else {
            wallet.setBalance(wallet.getBalance() + amount);
        }
        walletRepository.save(wallet);
    }

    private Wallet findOrCreateWalletForUpdate(UUID walletId, WalletOperationRequest.OperationType operationType) {
        Wallet wallet = walletRepository.findByIdForUpdate(walletId).orElse(null);
        if (wallet != null) {
            return wallet;
        }
        if (operationType == WalletOperationRequest.OperationType.WITHDRAW) {
            throw new WalletNotFoundException(walletId);
        }
        try {
            walletRepository.saveAndFlush(new Wallet(walletId, 0L));
        } catch (DataIntegrityViolationException e) {
            // Concurrent create: another transaction inserted first
        }
        return walletRepository.findByIdForUpdate(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
    }

    @Transactional(readOnly = true)
    public WalletBalanceResponse getBalance(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
        return new WalletBalanceResponse(wallet.getId(), wallet.getBalance());
    }
}
