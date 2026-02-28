package com.wallet.controller;

import com.wallet.dto.WalletBalanceResponse;
import com.wallet.dto.WalletOperationRequest;
import com.wallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping(value = "/wallet", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void executeOperation(@Valid @RequestBody WalletOperationRequest request) {
        walletService.executeOperation(request);
    }

    @GetMapping(value = "/wallets/{walletId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public WalletBalanceResponse getBalance(@PathVariable("walletId") UUID walletId) {
        return walletService.getBalance(walletId);
    }
}
