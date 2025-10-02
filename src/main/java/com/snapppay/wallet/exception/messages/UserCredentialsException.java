package com.snapppay.wallet.exception.messages;

import com.snapppay.wallet.exception.common.BaseException;
import org.springframework.http.HttpStatus;

public class UserCredentialsException extends BaseException {
    private static final String DESC = "user.credential.exception";

    public UserCredentialsException() {
        super(DESC);
    }

    @Override
    public String getDescriptionKey() {
        return DESC;
    }

    @Override
    public int getHttpCode() {
        return HttpStatus.UNAUTHORIZED.value();
    }
}
