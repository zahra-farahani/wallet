package com.snapppay.wallet.dto.response;

import com.snapppay.wallet.dto.enumeration.TransactionStatus;
import com.snapppay.wallet.dto.enumeration.TransactionType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class TransactionResponse {
    private long transactionId;
    private TransactionStatus status;
    private TransactionType type;
    private BigDecimal amount;
}
