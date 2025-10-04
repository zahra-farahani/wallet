package com.snapppay.wallet.service.integration;

import com.snapppay.wallet.dto.request.*;
import com.snapppay.wallet.dto.response.*;
import com.snapppay.wallet.entity.*;
import com.snapppay.wallet.repository.*;
import com.snapppay.wallet.security.*;
import com.snapppay.wallet.service.impl.*;
import lombok.extern.slf4j.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.*;
import org.springframework.test.context.*;
import org.testcontainers.containers.*;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.junit.jupiter.Container;

import java.math.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Slf4j
@AutoConfigureMockMvc(addFilters = false)
public class WalletOrchestratorIntegrationTests {

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);

        // Fix Redis connection for Redisson
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);

        // Critical: Configure Redisson to use the container
        String redisAddress = String.format("redis://%s:%d",
                redis.getHost(),
                redis.getFirstMappedPort());
        registry.add("spring.redis.redisson.config", () ->
                "singleServerConfig:\n  address: \"" + redisAddress + "\"\n");

        registry.add("JWT_SECRET", () -> "test-secret-key-minimum-256-bits");
        registry.add("JWT_EXPIRATION", () -> "86400000");
        registry.add("spring.liquibase.enabled", () -> "false");
    }

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .withReuse(true);


    @Autowired
    private WalletOrchestrator walletOrchestrator;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private User testUser;
    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(User.builder()
                .phoneNumber("09105675678")
                .firstName("zahra")
                .lastName("shahrabi")
                .active(true)
                .passwordHash(PasswordUtil.hashPassword("12345678"))
                .build());

        testWallet = walletRepository.save(Wallet.builder()
                .user(testUser)
                .balance(new BigDecimal("1000"))
                .build());
    }

    @Test
    void topUp_Success() {
        TopUpRequest request = new TopUpRequest();
        request.setAmount(new BigDecimal("500"));
        request.setPaymentReference("PAY123");

        walletOrchestrator.topUp(testWallet.getId(), request);

        Wallet updated = walletRepository.findById(testWallet.getId()).orElseThrow();
        assertEquals(new BigDecimal("1500.00"), updated.getBalance());
    }

    @Test
    void balance_Success() {
        BalanceResponse response = walletOrchestrator.balance(testWallet.getId());

        assertNotNull(response);
        assertEquals(testWallet.getId(), response.getWalletId());
        assertEquals(new BigDecimal("1000.00"), response.getBalance());
        assertNotNull(response.getTime());
    }

    @Test
    void withdraw_Success() {
        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(new BigDecimal("300"));

        TransactionResponse response = walletOrchestrator.withdraw(testWallet.getId(), request);

        assertNotNull(response);
        Wallet updated = walletRepository.findById(testWallet.getId()).orElseThrow();
        assertEquals(new BigDecimal("700.00"), updated.getBalance());
    }

    @Test
    void transfer_Success() {
        Wallet toWallet = walletRepository.save(Wallet.builder()
                .user(testUser)
                .balance(new BigDecimal("500"))
                .build());

        TransferRequest request = new TransferRequest();
        request.setFromWalletId(testWallet.getId());
        request.setToWalletId(toWallet.getId());
        request.setAmount(new BigDecimal("200"));

        walletOrchestrator.transfer(request);

        Wallet fromUpdated = walletRepository.findById(testWallet.getId()).orElseThrow();
        Wallet toUpdated = walletRepository.findById(toWallet.getId()).orElseThrow();

        assertEquals(new BigDecimal("800.00"), fromUpdated.getBalance());
        assertEquals(new BigDecimal("700.00"), toUpdated.getBalance());
    }

    @Test
    void transfer_ConcurrentTransfers_NoDeadlock() throws InterruptedException {
        Wallet wallet2 = walletRepository.save(Wallet.builder()
                .user(testUser)
                .balance(new BigDecimal("1000"))
                .build());

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    TransferRequest request = new TransferRequest();
                    if (index % 2 == 0) {
                        request.setFromWalletId(testWallet.getId());
                        request.setToWalletId(wallet2.getId());
                    } else {
                        request.setFromWalletId(wallet2.getId());
                        request.setToWalletId(testWallet.getId());
                    }
                    request.setAmount(new BigDecimal("50"));

                    walletOrchestrator.transfer(request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("Transfer failed", e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(threadCount, successCount.get());

        Wallet w1 = walletRepository.findById(testWallet.getId()).orElseThrow();
        Wallet w2 = walletRepository.findById(wallet2.getId()).orElseThrow();

        assertEquals(new BigDecimal("2000.00"), w1.getBalance().add(w2.getBalance()));
    }
}