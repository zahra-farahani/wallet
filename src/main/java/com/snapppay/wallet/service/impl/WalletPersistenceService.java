package com.snapppay.wallet.service.impl;

import com.snapppay.wallet.Mapper.TransactionMapper;
import com.snapppay.wallet.dto.TransactionDTO;
import com.snapppay.wallet.dto.enumeration.TransactionStatus;
import com.snapppay.wallet.dto.enumeration.TransactionType;
import com.snapppay.wallet.dto.request.TopUpRequest;
import com.snapppay.wallet.dto.request.TransferRequest;
import com.snapppay.wallet.dto.request.WithdrawRequest;
import com.snapppay.wallet.dto.response.BalanceResponse;
import com.snapppay.wallet.dto.response.TransactionResponse;
import com.snapppay.wallet.entity.Transaction;
import com.snapppay.wallet.entity.Wallet;
import com.snapppay.wallet.exception.messages.InsufficientBalanceException;
import com.snapppay.wallet.exception.messages.WalletNotFoundException;
import com.snapppay.wallet.exception.messages.WalletTransferSameDestinationAndOriginException;
import com.snapppay.wallet.repository.TransactionRepository;
import com.snapppay.wallet.repository.WalletRepository;
import com.snapppay.wallet.service.IWalletPersistenceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletPersistenceService implements IWalletPersistenceService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional
    public TransactionResponse topUpInTransaction(Long walletId, TopUpRequest req) {
        Wallet w = findWalletById(walletId);

        w.setBalance(w.getBalance().add(req.getAmount()));

        Transaction tx = createAndSaveTransaction(w, TransactionType.TOP_UP, req.getAmount());
        log.info("TopUp success walletId={} txId={} amount={} newBalance={}",
                walletId, tx.getId(), req.getAmount(), w.getBalance());
        return transactionMapper.mapEntityToResponse(tx);
    }

    @Override
    @Transactional
    public TransactionResponse transferInTransaction(TransferRequest req) {
        if (req.getFromWalletId().equals(req.getToWalletId())) {
            throw new WalletTransferSameDestinationAndOriginException();
        }

        // Lock both wallets in deterministic order to avoid deadlocks
        Long firstId = req.getFromWalletId() < req.getToWalletId() ? req.getFromWalletId() : req.getToWalletId();
        Long secondId = req.getFromWalletId() < req.getToWalletId() ? req.getToWalletId() : req.getFromWalletId();

        Wallet first = findWalletById(firstId);
        Wallet second = findWalletById(secondId);

        // map them into source/destination references regardless of locking order
        Wallet from = req.getFromWalletId().equals(first.getId()) ? first : second;
        Wallet to = req.getToWalletId().equals(first.getId()) ? first : second;

        if (from.getBalance().compareTo(req.getAmount()) < 0) {
            throw new InsufficientBalanceException();
        }

        from.setBalance(from.getBalance().subtract(req.getAmount()));
        to.setBalance(to.getBalance().add(req.getAmount()));

        // create transactions: one for debit (from) and one for credit (to)
        Transaction txFrom = createAndSaveTransaction(from, TransactionType.TRANSFER, req.getAmount());
        Transaction txTo   = createAndSaveTransaction(to, TransactionType.TRANSFER, req.getAmount());

        log.info("Transfer success fromWalletId={} toWalletId={} amount={} txFrom={} txTo={}",
                from.getId(), to.getId(), req.getAmount(), txFrom.getId(), txTo.getId());

        return transactionMapper.mapEntityToResponse(txFrom);
    }

    @Override
    @Transactional
    public TransactionResponse withdrawInTransaction(Long walletId, WithdrawRequest req) {
        Wallet w = findWalletById(walletId);

        if (req.getAmount().compareTo(w.getBalance()) > 0) {
            throw new InsufficientBalanceException();
        }

        w.setBalance(w.getBalance().subtract(req.getAmount()));

        Transaction tx = createAndSaveTransaction(w, TransactionType.WITHDRAW, req.getAmount());
        log.info("Withdraw success walletId={} txId={} amount={} newBalance={}",
                walletId, tx.getId(), req.getAmount(), w.getBalance());
        return transactionMapper.mapEntityToResponse(tx);
    }

    @Override
    public BalanceResponse getBalance(Long walletId) {
        Wallet w = walletRepository.findById(walletId).orElseThrow(WalletNotFoundException::new);
        return BalanceResponse.builder()
                .balance(w.getBalance())
                .walletId(w.getId())
                .time(Instant.now())
                .build();
    }

    @Override
    public Page<TransactionDTO> fetchTransactions(Long walletId, PageRequest page) {
        Page<Transaction> p = transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId, page);
        return p.map(transactionMapper::mapEntityToDTO);
    }


    private Wallet findWalletById(Long walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(WalletNotFoundException::new);
    }

    private Transaction createAndSaveTransaction(Wallet wallet, TransactionType type, BigDecimal amount) {
        Transaction tx = Transaction.builder()
                .wallet(wallet)
                .type(type)
                .amount(amount)
                .status(TransactionStatus.SUCCESS)
                .build();
        return transactionRepository.save(tx);
    }
}
