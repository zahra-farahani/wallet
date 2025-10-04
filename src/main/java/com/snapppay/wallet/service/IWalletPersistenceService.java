package com.snapppay.wallet.service;

import com.snapppay.wallet.dto.TransactionDTO;
import com.snapppay.wallet.dto.request.TopUpRequest;
import com.snapppay.wallet.dto.request.TransferRequest;
import com.snapppay.wallet.dto.request.WithdrawRequest;
import com.snapppay.wallet.dto.response.BalanceResponse;
import com.snapppay.wallet.dto.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface IWalletPersistenceService {
    TransactionResponse topUpInTransaction(Long walletId, TopUpRequest request);
    Page<TransactionDTO> fetchTransactions(Long walletId, PageRequest page);
    TransactionResponse transferInTransaction(TransferRequest req);
    TransactionResponse withdrawInTransaction(Long walletId, WithdrawRequest req);
    BalanceResponse getBalance(Long walletId);
}
