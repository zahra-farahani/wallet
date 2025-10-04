package com.snapppay.wallet.service.impl;

import com.snapppay.wallet.dto.TransactionDTO;
import com.snapppay.wallet.dto.request.TopUpRequest;
import com.snapppay.wallet.dto.request.TransferRequest;
import com.snapppay.wallet.dto.request.WithdrawRequest;
import com.snapppay.wallet.dto.response.BalanceResponse;
import com.snapppay.wallet.dto.response.TransactionResponse;
import com.snapppay.wallet.exception.messages.WalletOperationException;
import com.snapppay.wallet.service.IWalletPersistenceService;
import com.snapppay.wallet.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.LockAcquisitionException;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletOrchestrator {

    private final IWalletPersistenceService walletPersistenceService;
    private final RedisUtil redisUtil;

    @Value("${app.lock.max-retries:3}")
    private int maxRetries;

    @Value("${app.lock.base-backoff-ms:200}")
    private long baseBackoffMs;

    @Value("${app.lock.max-backoff-ms:5000}")
    private long maxBackoffMs;

    public BalanceResponse balance(Long walletId) {
        log.debug("Fetch balance for {} ", walletId);
        return walletPersistenceService.getBalance(walletId);
    }

    public TransactionResponse topUp(Long walletId, TopUpRequest req) {
        return executeWithWalletLock(walletId, maxRetries,
                () -> walletPersistenceService.topUpInTransaction(walletId, req)
        );
    }

    public TransactionResponse withdraw(Long walletId, WithdrawRequest req) {
        return executeWithWalletLock(walletId, maxRetries,
                () -> walletPersistenceService.withdrawInTransaction(walletId, req)
        );
    }

    public TransactionResponse transfer(TransferRequest req) {
        Long firstId = Math.min(req.getFromWalletId(), req.getToWalletId());
        Long secondId = Math.max(req.getFromWalletId(), req.getToWalletId());

        return executeWithMultiWalletLock(
                List.of(firstId, secondId),
                maxRetries,
                () -> walletPersistenceService.transferInTransaction(req)
        );
    }

    public Page<TransactionDTO> transactions(Long walletId, int page, int size) {
        return walletPersistenceService.fetchTransactions(walletId, PageRequest.of(page, size));
    }

    private <T> T executeWithMultiWalletLock(List<Long> walletIds, int maxRetries, Supplier<T> dbOperation) {
        List<RLock> acquiredLocks = new ArrayList<>();

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.debug("Wallet operation attempt={} walletIds={}", attempt, walletIds);

                for (Long walletId : walletIds) {
                    RLock lock = redisUtil.tryAcquireWalletLock(walletId);
                    if (lock == null) {
                        throw new LockAcquisitionException("Failed to acquire lock for wallet " + walletId, null);
                    }
                    acquiredLocks.add(lock);
                }

                return dbOperation.get();

            } catch (LockAcquisitionException | ConcurrencyFailureException ex) {
                log.warn("Retry attempt {} for wallets {} failed: {}", attempt, walletIds, ex.getMessage());
                if (attempt >= maxRetries) {
                    throw new WalletOperationException();
                }
                sleepWithBackoff(attempt);
            } catch (Exception ex) {
                log.error("Non-retriable error for wallets {}: {}", walletIds, ex.getMessage());
                throw ex;
            } finally {
                acquiredLocks.forEach(lock -> {
                    try {
                        redisUtil.releaseLock(lock);
                    } catch (Exception e) {
                        log.error("Failed to release lock: {}", e.getMessage());
                    }
                });
                acquiredLocks.clear();
            }
        }
        throw new WalletOperationException();
    }

    private <T> T executeWithWalletLock(Long walletId, int maxRetries, Supplier<T> dbOperation) {
        return executeWithMultiWalletLock(List.of(walletId), maxRetries, dbOperation);
    }

    private void sleepWithBackoff(int attempt) {
        try {
            long sleep = Math.min(baseBackoffMs * (1L << (attempt - 1)), maxBackoffMs);
            Thread.sleep(sleep);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new WalletOperationException();
        }
    }
}
