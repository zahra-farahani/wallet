package com.snapppay.wallet.exception.messages;

import com.snapppay.wallet.exception.common.BaseException;
import org.springframework.http.HttpStatus;

public class AlreadyExistedPhoneNumberException extends BaseException {

    private static final String DESC = "user.phonenumber.already.exist.exception";

    public AlreadyExistedPhoneNumberException() {
        super(DESC);
    }

    @Override
    public String getDescriptionKey() {
        return DESC;
    }

    @Override
    public int getHttpCode() {
        return HttpStatus.CONFLICT.value();
    }

}
