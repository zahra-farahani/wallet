package com.snapppay.wallet.Mapper;

import com.snapppay.wallet.dto.TransactionDTO;
import com.snapppay.wallet.dto.response.TransactionResponse;
import com.snapppay.wallet.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "id", target = "transactionId")
    TransactionResponse mapEntityToResponse(Transaction trx);

    TransactionDTO mapEntityToDTO(Transaction trx);
}
