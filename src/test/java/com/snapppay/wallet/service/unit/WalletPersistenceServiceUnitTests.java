package com.snapppay.wallet.service.unit;

import com.snapppay.wallet.Mapper.TransactionMapper;
import com.snapppay.wallet.dto.enumeration.TransactionStatus;
import com.snapppay.wallet.dto.enumeration.TransactionType;
import com.snapppay.wallet.dto.request.TopUpRequest;
import com.snapppay.wallet.dto.request.TransferRequest;
import com.snapppay.wallet.dto.request.WithdrawRequest;
import com.snapppay.wallet.dto.response.TransactionResponse;
import com.snapppay.wallet.entity.Transaction;
import com.snapppay.wallet.entity.Wallet;
import com.snapppay.wallet.exception.messages.InsufficientBalanceException;
import com.snapppay.wallet.exception.messages.WalletNotFoundException;
import com.snapppay.wallet.exception.messages.WalletTransferSameDestinationAndOriginException;
import com.snapppay.wallet.repository.TransactionRepository;
import com.snapppay.wallet.repository.WalletRepository;
import com.snapppay.wallet.service.impl.WalletPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletPersistenceServiceUnitTests {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private WalletPersistenceService walletPersistenceService;

    private Wallet fromWallet;
    private Wallet toWallet;
    private TransferRequest request;
    private Wallet wallet;
    private Transaction transaction;
    private TransactionResponse transactionResponse;

    @BeforeEach
    void setUp() {
        fromWallet = Wallet.builder()
                .id(1L)
                .balance(new BigDecimal("1000"))
                .version(1L)
                .build();

        toWallet = Wallet.builder()
                .id(2L)
                .balance(new BigDecimal("500"))
                .version(1L)
                .build();

        request = new TransferRequest();
        request.setFromWalletId(1L);
        request.setToWalletId(2L);
        request.setAmount(new BigDecimal("100"));

        wallet = Wallet.builder()
                .id(1L)
                .balance(new BigDecimal("1000"))
                .build();

        transaction = Transaction.builder()
                .id(100L)
                .wallet(wallet)
                .amount(new BigDecimal("500"))
                .status(TransactionStatus.SUCCESS)
                .build();

        transactionResponse = new TransactionResponse();
    }

    // ========== TRANSFER TESTS ==========

    @Test
    void transferInTransaction_Success() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findById(2L)).thenReturn(Optional.of(toWallet));

        Transaction txFrom = Transaction.builder().id(1L).build();
        when(transactionRepository.save(any())).thenReturn(txFrom);

        TransactionResponse response = new TransactionResponse();
        when(transactionMapper.mapEntityToResponse(txFrom)).thenReturn(response);

        TransactionResponse result = walletPersistenceService.transferInTransaction(request);

        assertNotNull(result);
        assertEquals(new BigDecimal("900"), fromWallet.getBalance());
        assertEquals(new BigDecimal("600"), toWallet.getBalance());
        verify(transactionRepository, times(2)).save(any());
    }

    @Test
    void transferInTransaction_SameWallet_ThrowsException() {
        request.setFromWalletId(1L);
        request.setToWalletId(1L);

        assertThrows(WalletTransferSameDestinationAndOriginException.class,
                () -> walletPersistenceService.transferInTransaction(request));

        verify(walletRepository, never()).findById(any());
    }

    @Test
    void transferInTransaction_InsufficientBalance_ThrowsException() {
        request.setAmount(new BigDecimal("2000"));

        when(walletRepository.findById(1L)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findById(2L)).thenReturn(Optional.of(toWallet));

        assertThrows(InsufficientBalanceException.class,
                () -> walletPersistenceService.transferInTransaction(request));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transferInTransaction_FromWalletNotFound_ThrowsException() {
        when(walletRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class,
                () -> walletPersistenceService.transferInTransaction(request));
    }

    @Test
    void transferInTransaction_ToWalletNotFound_ThrowsException() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class,
                () -> walletPersistenceService.transferInTransaction(request));
    }

    @Test
    void transferInTransaction_LocksInDeterministicOrder() {
        request.setFromWalletId(2L);
        request.setToWalletId(1L);

        when(walletRepository.findById(1L)).thenReturn(Optional.of(toWallet));
        when(walletRepository.findById(2L)).thenReturn(Optional.of(fromWallet));
        when(transactionRepository.save(any())).thenReturn(Transaction.builder().build());

        walletPersistenceService.transferInTransaction(request);

        InOrder inOrder = inOrder(walletRepository);
        inOrder.verify(walletRepository).findById(1L);
        inOrder.verify(walletRepository).findById(2L);
    }

    // ========== TOP UP TESTS ==========

    @Test
    void topUpInTransaction_Success() {
        TopUpRequest request = new TopUpRequest();
        request.setAmount(new BigDecimal("500"));
        request.setPaymentReference("PAY123");

        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.mapEntityToResponse(transaction)).thenReturn(transactionResponse);

        TransactionResponse result = walletPersistenceService.topUpInTransaction(1L, request);

        assertNotNull(result);
        assertEquals(new BigDecimal("1500"), wallet.getBalance());
        verify(transactionRepository).save(argThat(tx ->
                tx.getType() == TransactionType.TOP_UP &&
                        tx.getAmount().compareTo(new BigDecimal("500")) == 0 &&
                        tx.getStatus() == TransactionStatus.SUCCESS
        ));
    }

    @Test
    void topUpInTransaction_WalletNotFound() {
        TopUpRequest request = new TopUpRequest();
        request.setAmount(new BigDecimal("500"));
        request.setPaymentReference("PAY123");

        when(walletRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class,
                () -> walletPersistenceService.topUpInTransaction(1L, request));

        verify(transactionRepository, never()).save(any());
    }

    // ========== WITHDRAW TESTS ==========

    @Test
    void withdrawInTransaction_Success() {
        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(new BigDecimal("300"));

        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.mapEntityToResponse(transaction)).thenReturn(transactionResponse);

        TransactionResponse result = walletPersistenceService.withdrawInTransaction(1L, request);

        assertNotNull(result);
        assertEquals(new BigDecimal("700"), wallet.getBalance());
        verify(transactionRepository).save(argThat(tx ->
                tx.getType() == TransactionType.WITHDRAW &&
                        tx.getAmount().compareTo(new BigDecimal("300")) == 0 &&
                        tx.getStatus() == TransactionStatus.SUCCESS
        ));
    }

    @Test
    void withdrawInTransaction_ExactBalance() {
        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(new BigDecimal("1000"));

        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.mapEntityToResponse(transaction)).thenReturn(transactionResponse);

        TransactionResponse result = walletPersistenceService.withdrawInTransaction(1L, request);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO.setScale(wallet.getBalance().scale()), wallet.getBalance());
    }

    @Test
    void withdrawInTransaction_InsufficientBalance() {
        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(new BigDecimal("1500"));

        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        assertThrows(InsufficientBalanceException.class,
                () -> walletPersistenceService.withdrawInTransaction(1L, request));

        assertEquals(new BigDecimal("1000"), wallet.getBalance());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void withdrawInTransaction_WalletNotFound() {
        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(new BigDecimal("100"));

        when(walletRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class,
                () -> walletPersistenceService.withdrawInTransaction(1L, request));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void withdrawInTransaction_ZeroBalance() {
        wallet.setBalance(BigDecimal.ZERO);
        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(new BigDecimal("100"));

        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        assertThrows(InsufficientBalanceException.class,
                () -> walletPersistenceService.withdrawInTransaction(1L, request));
    }
}
