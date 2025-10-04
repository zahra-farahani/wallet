package com.snapppay.wallet.service.unit;

import com.snapppay.wallet.dto.*;
import com.snapppay.wallet.dto.enumeration.*;
import com.snapppay.wallet.dto.request.*;
import com.snapppay.wallet.dto.response.*;
import com.snapppay.wallet.exception.messages.*;
import com.snapppay.wallet.service.*;
import com.snapppay.wallet.service.impl.*;
import com.snapppay.wallet.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.redisson.api.*;
import org.springframework.dao.*;
import org.springframework.data.domain.*;
import org.springframework.test.util.*;

import java.math.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletOrchestratorUnitTests {

    @Mock
    private IWalletPersistenceService walletPersistenceService;

    @Mock
    private RedisUtil redisUtil;

    @Mock
    private RLock rLock;

    @InjectMocks
    private WalletOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orchestrator, "maxRetries", 3);
        ReflectionTestUtils.setField(orchestrator, "baseBackoffMs", 100L);
        ReflectionTestUtils.setField(orchestrator, "maxBackoffMs", 5000L);
    }

    // ========== BALANCE TESTS ==========

    @Test
    void balance_Success() {
        BalanceResponse expected = BalanceResponse.builder()
                .walletId(1L)
                .balance(new BigDecimal("1000"))
                .build();

        when(walletPersistenceService.getBalance(1L)).thenReturn(expected);

        BalanceResponse result = orchestrator.balance(1L);

        assertEquals(expected, result);
        verify(walletPersistenceService).getBalance(1L);
        verifyNoInteractions(redisUtil);
    }

    @Test
    void balance_WalletNotFound() {
        when(walletPersistenceService.getBalance(1L))
                .thenThrow(new WalletNotFoundException());

        assertThrows(WalletNotFoundException.class, () -> orchestrator.balance(1L));
    }

    // ========== TOP UP TESTS ==========

    @Test
    void topUp_Success() {
        TopUpRequest request = new TopUpRequest();
        request.setAmount(new BigDecimal("500"));

        TransactionResponse expected = new TransactionResponse(100L, TransactionStatus.SUCCESS, TransactionType.TOP_UP, new BigDecimal(500));

        when(redisUtil.tryAcquireWalletLock(1L)).thenReturn(rLock);
        when(walletPersistenceService.topUpInTransaction(1L, request)).thenReturn(expected);

        TransactionResponse result = orchestrator.topUp(1L, request);

        assertEquals(expected, result);
        verify(redisUtil).tryAcquireWalletLock(1L);
        verify(redisUtil).releaseLock(rLock);
    }

    @Test
    void topUp_LockAcquisitionFails_Retries() {
        TopUpRequest request = new TopUpRequest();
        request.setAmount(new BigDecimal("500"));

        TransactionResponse expected = new TransactionResponse();

        when(redisUtil.tryAcquireWalletLock(1L))
                .thenReturn(null)
                .thenReturn(rLock);
        when(walletPersistenceService.topUpInTransaction(1L, request)).thenReturn(expected);

        TransactionResponse result = orchestrator.topUp(1L, request);

        assertEquals(expected, result);
        verify(redisUtil, times(2)).tryAcquireWalletLock(1L);
        verify(redisUtil).releaseLock(rLock);
    }

    @Test
    void topUp_MaxRetriesExceeded() {
        TopUpRequest request = new TopUpRequest();
        request.setAmount(new BigDecimal("500"));

        when(redisUtil.tryAcquireWalletLock(1L)).thenReturn(null);

        assertThrows(WalletOperationException.class, () -> orchestrator.topUp(1L, request));
        verify(redisUtil, times(3)).tryAcquireWalletLock(1L);
    }

    @Test
    void topUp_OptimisticLockException_Retries() {
        TopUpRequest request = new TopUpRequest();
        request.setAmount(new BigDecimal("500"));

        TransactionResponse expected = new TransactionResponse();

        when(redisUtil.tryAcquireWalletLock(1L)).thenReturn(rLock);
        when(walletPersistenceService.topUpInTransaction(1L, request))
                .thenThrow(new OptimisticLockingFailureException("Conflict"))
                .thenReturn(expected);

        TransactionResponse result = orchestrator.topUp(1L, request);

        assertEquals(expected, result);
        verify(walletPersistenceService, times(2)).topUpInTransaction(1L, request);
        verify(redisUtil, times(2)).releaseLock(rLock);
    }

    @Test
    void topUp_BusinessException_NoRetry() {
        TopUpRequest request = new TopUpRequest();
        request.setAmount(new BigDecimal("500"));

        when(redisUtil.tryAcquireWalletLock(1L)).thenReturn(rLock);
        when(walletPersistenceService.topUpInTransaction(1L, request))
                .thenThrow(new WalletNotFoundException());

        assertThrows(WalletNotFoundException.class, () -> orchestrator.topUp(1L, request));
        verify(walletPersistenceService, times(1)).topUpInTransaction(1L, request);
        verify(redisUtil).releaseLock(rLock);
    }

    // ========== WITHDRAW TESTS ==========

    @Test
    void withdraw_Success() {
        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(new BigDecimal("300"));

        TransactionResponse expected = new TransactionResponse();

        when(redisUtil.tryAcquireWalletLock(1L)).thenReturn(rLock);
        when(walletPersistenceService.withdrawInTransaction(1L, request)).thenReturn(expected);

        TransactionResponse result = orchestrator.withdraw(1L, request);

        assertEquals(expected, result);
        verify(redisUtil).releaseLock(rLock);
    }

    @Test
    void withdraw_InsufficientBalance_NoRetry() {
        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(new BigDecimal("5000"));

        when(redisUtil.tryAcquireWalletLock(1L)).thenReturn(rLock);
        when(walletPersistenceService.withdrawInTransaction(1L, request))
                .thenThrow(new InsufficientBalanceException());

        assertThrows(InsufficientBalanceException.class, () -> orchestrator.withdraw(1L, request));
        verify(walletPersistenceService, times(1)).withdrawInTransaction(1L, request);
    }

    // ========== TRANSFER TESTS ==========

    @Test
    void transfer_Success() {
        TransferRequest request = new TransferRequest();
        request.setFromWalletId(1L);
        request.setToWalletId(2L);
        request.setAmount(new BigDecimal("100"));

        TransactionResponse expected = new TransactionResponse();

        when(redisUtil.tryAcquireWalletLock(1L)).thenReturn(rLock);
        when(redisUtil.tryAcquireWalletLock(2L)).thenReturn(rLock);
        when(walletPersistenceService.transferInTransaction(request)).thenReturn(expected);

        TransactionResponse result = orchestrator.transfer(request);

        assertEquals(expected, result);

        InOrder inOrder = inOrder(redisUtil);
        inOrder.verify(redisUtil).tryAcquireWalletLock(1L);
        inOrder.verify(redisUtil).tryAcquireWalletLock(2L);
        verify(redisUtil, times(2)).releaseLock(rLock);
    }

    @Test
    void transfer_LocksInDeterministicOrder() {
        TransferRequest request = new TransferRequest();
        request.setFromWalletId(5L);
        request.setToWalletId(2L);
        request.setAmount(new BigDecimal("100"));

        TransactionResponse expected = new TransactionResponse();

        when(redisUtil.tryAcquireWalletLock(2L)).thenReturn(rLock);
        when(redisUtil.tryAcquireWalletLock(5L)).thenReturn(rLock);
        when(walletPersistenceService.transferInTransaction(request)).thenReturn(expected);

        TransactionResponse result = orchestrator.transfer(request);

        assertEquals(expected, result);

        InOrder inOrder = inOrder(redisUtil);
        inOrder.verify(redisUtil).tryAcquireWalletLock(2L);
        inOrder.verify(redisUtil).tryAcquireWalletLock(5L);
    }

    @Test
    void transfer_FirstLockFails() {
        TransferRequest request = new TransferRequest();
        request.setFromWalletId(1L);
        request.setToWalletId(2L);
        request.setAmount(new BigDecimal("100"));

        when(redisUtil.tryAcquireWalletLock(1L)).thenReturn(null);

        assertThrows(WalletOperationException.class, () -> orchestrator.transfer(request));
        verify(redisUtil, times(3)).tryAcquireWalletLock(1L);
        verify(redisUtil, never()).tryAcquireWalletLock(2L);
    }

    @Test
    void transfer_SecondLockFails_ReleasesFirst() {
        TransferRequest request = new TransferRequest();
        request.setFromWalletId(1L);
        request.setToWalletId(2L);
        request.setAmount(new BigDecimal("100"));

        when(redisUtil.tryAcquireWalletLock(1L)).thenReturn(rLock);
        when(redisUtil.tryAcquireWalletLock(2L)).thenReturn(null);

        assertThrows(WalletOperationException.class, () -> orchestrator.transfer(request));

        verify(redisUtil, times(3)).tryAcquireWalletLock(1L);
        verify(redisUtil, times(3)).tryAcquireWalletLock(2L);
        verify(redisUtil, times(3)).releaseLock(rLock);
    }

    @Test
    void transfer_SameWallet_NoRetry() {
        TransferRequest request = new TransferRequest();
        request.setFromWalletId(1L);
        request.setToWalletId(1L);
        request.setAmount(new BigDecimal("100"));

        when(redisUtil.tryAcquireWalletLock(1L)).thenReturn(rLock);
        when(walletPersistenceService.transferInTransaction(request))
                .thenThrow(new WalletTransferSameDestinationAndOriginException());

        assertThrows(WalletTransferSameDestinationAndOriginException.class,
                () -> orchestrator.transfer(request));

        verify(walletPersistenceService, times(1)).transferInTransaction(request);
    }

    // ========== TRANSACTIONS TESTS ==========

    @Test
    void transactions_Success() {
        Page<TransactionDTO> expected = new PageImpl<>(
                List.of(
                        new TransactionDTO(),
                        new TransactionDTO()
                )
        );

        when(walletPersistenceService.fetchTransactions(eq(1L), any(PageRequest.class)))
                .thenReturn(expected);

        Page<TransactionDTO> result = orchestrator.transactions(1L, 0, 10);

        assertEquals(expected, result);
        verify(walletPersistenceService).fetchTransactions(eq(1L), argThat(pr ->
                pr.getPageNumber() == 0 && pr.getPageSize() == 10
        ));
        verifyNoInteractions(redisUtil);
    }

    @Test
    void transactions_EmptyList() {
        when(walletPersistenceService.fetchTransactions(eq(1L), any(PageRequest.class)))
                .thenReturn(Page.empty());

        Page<TransactionDTO> result = orchestrator.transactions(1L, 0, 10);

        assertTrue(result.isEmpty());
    }
}
