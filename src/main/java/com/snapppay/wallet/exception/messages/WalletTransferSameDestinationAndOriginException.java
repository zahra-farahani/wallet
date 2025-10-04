package com.snapppay.wallet.exception.messages;

import com.snapppay.wallet.exception.common.BaseException;
import org.springframework.http.HttpStatus;

public class WalletTransferSameDestinationAndOriginException  extends BaseException {
    private static final String DESC = "wallet.transfer.to.from.equal.exception";

    public WalletTransferSameDestinationAndOriginException() {
        super(DESC);
    }

    @Override
    public String getDescriptionKey() {
        return DESC;
    }

    @Override
    public int getHttpCode() {
        return HttpStatus.NOT_ACCEPTABLE.value();
    }
}
