package com.snapppay.wallet.api.v1;

import com.snapppay.wallet.dto.TransactionDTO;
import com.snapppay.wallet.dto.request.TopUpRequest;
import com.snapppay.wallet.dto.request.TransferRequest;
import com.snapppay.wallet.dto.request.WithdrawRequest;
import com.snapppay.wallet.dto.response.BalanceResponse;
import com.snapppay.wallet.dto.response.TransactionResponse;
import com.snapppay.wallet.service.impl.WalletOrchestrator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletOrchestrator walletOrchestrator;

    @GetMapping("/{walletId}/balance")
    @PreAuthorize("@walletSecurityService.canAccessWallet(#walletId)")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable @Positive Long walletId) {
        BalanceResponse response = walletOrchestrator.balance(walletId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{walletId}/top-up")
    @PreAuthorize("@walletSecurityService.canAccessWallet(#walletId)")
    public ResponseEntity<TransactionResponse> topUp(
            @PathVariable @Positive Long walletId,
            @Valid @RequestBody TopUpRequest req) {
        TransactionResponse response = walletOrchestrator.topUp(walletId, req);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/{walletId}/withdraw")
    @PreAuthorize("@walletSecurityService.canAccessWallet(#walletId)")
    public ResponseEntity<TransactionResponse> withdraw(
            @PathVariable @Positive Long walletId,
            @Valid @RequestBody WithdrawRequest req) {
        TransactionResponse response = walletOrchestrator.withdraw(walletId, req);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/transfer")
    @PreAuthorize("@walletSecurityService.canAccessWallet(#req.fromWalletId)")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest req) {
        TransactionResponse response = walletOrchestrator.transfer(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{walletId}/transactions")
    @PreAuthorize("@walletSecurityService.canAccessWallet(#walletId)")
    public ResponseEntity<Page<TransactionDTO>> transactions(
            @PathVariable @Positive Long walletId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        Page<TransactionDTO> items = walletOrchestrator.transactions(walletId, page, size);
        return ResponseEntity.ok(items);
    }
}