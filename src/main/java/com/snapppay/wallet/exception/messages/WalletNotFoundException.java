package com.snapppay.wallet.exception.messages;

import com.snapppay.wallet.exception.common.BaseException;
import org.springframework.http.HttpStatus;

public class WalletNotFoundException extends BaseException {
    private static final String DESC = "wallet.not.found.exception";

    public WalletNotFoundException() {
        super(DESC);
    }

    @Override
    public String getDescriptionKey() {
        return DESC;
    }

    @Override
    public int getHttpCode() {
        return HttpStatus.NOT_FOUND.value();
    }
}
