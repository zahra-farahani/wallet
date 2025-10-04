package com.snapppay.wallet.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {
    private final RedissonClient redissonClient;

    private static final String WALLET_LOCK = "wallet:lock:%s";

    @Value("${app.lock.wait-seconds}")
    private long waitSeconds;

    @Value("${app.lock.lease-seconds}")
    private long leaseSeconds;

    public RLock tryAcquireWalletLock(Long walletId) {
        RLock lock = getWalletLock(walletId);
        return tryAcquireLock(lock, waitSeconds, leaseSeconds, TimeUnit.SECONDS);
    }

    public void releaseLock(RLock lock) {
        if (lock == null) return;
        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("Released redis lock {}", lock.getName());
            } else {
                log.debug("Lock {} is not held by current thread; skipping unlock.", lock.getName());
            }
        } catch (IllegalMonitorStateException imse) {
            // not held, ignore
            log.warn("Tried to unlock but lock not held by current thread: {}", lock.getName(), imse);
        } catch (Exception ex) {
            log.error("Error while releasing redis lock {}", lock.getName(), ex);
        }
    }


    private RLock getWalletLock(Long walletId) {
        return redissonClient.getLock(walletKey(walletId));
    }

    private RLock tryAcquireLock(RLock lock, long waitTime, long leaseTime, TimeUnit unit) {
        try {
            boolean acquired = lock.tryLock(waitTime, leaseTime, unit);
            if (acquired) {
                log.debug("Acquired redis lock for key={}", lock.getName());
                return lock;
            } else {
                log.debug("Could not acquire redis lock for key={}", lock.getName());
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while trying to acquire lock for key={}", lock.getName(), e);
            return null;
        } catch (Exception e) {
            log.error("Error while trying to acquire lock for key={}", lock.getName(), e);
            return null;
        }
    }

    private static String walletKey(Long walletId) {
        return String.format(WALLET_LOCK, walletId);
    }
}
