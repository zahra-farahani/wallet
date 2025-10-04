package com.snapppay.wallet.dto;

import com.snapppay.wallet.dto.enumeration.TransactionStatus;
import com.snapppay.wallet.dto.enumeration.TransactionType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class TransactionDTO {
    private long transactionId;
    private TransactionStatus status;
    private TransactionType type;
    private String idempotencyKey;
    private double amount;
}
