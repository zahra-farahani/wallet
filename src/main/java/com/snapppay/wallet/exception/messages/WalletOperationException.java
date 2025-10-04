package com.snapppay.wallet.exception.messages;

import com.snapppay.wallet.exception.common.BaseException;
import org.springframework.http.HttpStatus;

public class WalletOperationException  extends BaseException {
    private static final String DESC = "wallet.operation.exception";

    public WalletOperationException() {
        super(DESC);
    }

    @Override
    public String getDescriptionKey() {
        return DESC;
    }

    @Override
    public int getHttpCode() {
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
}