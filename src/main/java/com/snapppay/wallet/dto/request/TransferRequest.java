package com.snapppay.wallet.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
public class TransferRequest {
    @NotNull(message = "from.wallet.empty")
    private Long fromWalletId;

    @NotNull(message = "to.wallet.empty")
    private Long toWalletId;

    @NotNull(message = "amount.empty")
    @DecimalMin(value = "100000.0", message = "top.up.min.amount")
    private BigDecimal amount;
}
