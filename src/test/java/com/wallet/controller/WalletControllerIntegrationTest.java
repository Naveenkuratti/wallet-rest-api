package com.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.dto.WalletOperationRequest;
import com.wallet.entity.Wallet;
import com.wallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WalletControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WalletRepository walletRepository;

    private UUID existingWalletId;

    @BeforeEach
    void setUp() {
        walletRepository.deleteAll();
        existingWalletId = UUID.randomUUID();
        walletRepository.save(new Wallet(existingWalletId, 1000L));
    }

    @Nested
    @DisplayName("POST /api/v1/wallet")
    class ExecuteOperation {

        @Test
        void deposit_increasesBalance() throws Exception {
            String body = objectMapper.writeValueAsString(new RequestBody(existingWalletId, "DEPOSIT", 500L));

            mockMvc.perform(post("/api/v1/wallet")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/v1/wallets/" + existingWalletId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(1500));
        }

        @Test
        void withdraw_decreasesBalance() throws Exception {
            String body = objectMapper.writeValueAsString(new RequestBody(existingWalletId, "WITHDRAW", 300L));

            mockMvc.perform(post("/api/v1/wallet")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/v1/wallets/" + existingWalletId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(700));
        }

        @Test
        void withdraw_withInsufficientFunds_returns422() throws Exception {
            String body = objectMapper.writeValueAsString(new RequestBody(existingWalletId, "WITHDRAW", 2000L));

            mockMvc.perform(post("/api/v1/wallet")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.error").value("Insufficient Funds"))
                    .andExpect(jsonPath("$.status").value(422));
        }

        @Test
        void withdraw_fromNonExistentWallet_returns404() throws Exception {
            UUID unknownId = UUID.randomUUID();
            String body = objectMapper.writeValueAsString(new RequestBody(unknownId, "WITHDRAW", 100L));

            mockMvc.perform(post("/api/v1/wallet")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        void invalidJson_returns400() throws Exception {
            mockMvc.perform(post("/api/v1/wallet")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{ invalid json }"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        void missingWalletId_returns400() throws Exception {
            String body = "{\"operationType\":\"DEPOSIT\",\"amount\":100}";

            mockMvc.perform(post("/api/v1/wallet")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"));
        }

        @Test
        void deposit_toNewWallet_createsWallet() throws Exception {
            UUID newWalletId = UUID.randomUUID();
            String body = objectMapper.writeValueAsString(new RequestBody(newWalletId, "DEPOSIT", 200L));

            mockMvc.perform(post("/api/v1/wallet")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/v1/wallets/" + newWalletId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(200));
        }

        @Test
        void valletId_alias_works() throws Exception {
            String body = String.format(
                    "{\"valletId\":\"%s\",\"operationType\":\"DEPOSIT\",\"amount\":100}",
                    existingWalletId);

            mockMvc.perform(post("/api/v1/wallet")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/v1/wallets/" + existingWalletId))
                    .andExpect(jsonPath("$.balance").value(1100));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/wallets/{walletId}")
    class GetBalance {

        @Test
        void existingWallet_returnsBalance() throws Exception {
            mockMvc.perform(get("/api/v1/wallets/" + existingWalletId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.walletId").value(existingWalletId.toString()))
                    .andExpect(jsonPath("$.balance").value(1000));
        }

        @Test
        void nonExistentWallet_returns404() throws Exception {
            UUID unknownId = UUID.randomUUID();
            mockMvc.perform(get("/api/v1/wallets/" + unknownId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        void invalidUuid_returns400() throws Exception {
            mockMvc.perform(get("/api/v1/wallets/not-a-uuid"))
                    .andExpect(status().isBadRequest());
        }
    }

    private static class RequestBody {
        public final java.util.UUID walletId;
        public final String operationType;
        public final Long amount;

        RequestBody(UUID walletId, String operationType, Long amount) {
            this.walletId = walletId;
            this.operationType = operationType;
            this.amount = amount;
        }
    }
}
