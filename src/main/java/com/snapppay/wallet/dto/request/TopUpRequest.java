package com.snapppay.wallet.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
public class TopUpRequest {
    @NotNull(message = "amount.empty")
    @DecimalMin(value = "100000.0", message = "min.amount")
    private BigDecimal amount;

    @NotNull(message = "payment.ref.empty")
    private String paymentReference;
}
