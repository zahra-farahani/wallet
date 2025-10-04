package com.snapppay.wallet.exception.messages;

import com.snapppay.wallet.exception.common.BaseException;
import org.springframework.http.HttpStatus;

public class MinimumAmountException extends BaseException {
    private static final String DESC = "min.amount.exception";

    public MinimumAmountException() {
        super(DESC);
    }

    @Override
    public String getDescriptionKey() {
        return DESC;
    }

    @Override
    public int getHttpCode() {
        return HttpStatus.BAD_REQUEST.value();
    }
}