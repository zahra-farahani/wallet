package com.snapppay.wallet.exception.messages;

import com.snapppay.wallet.exception.common.BaseException;
import org.springframework.http.HttpStatus;

public class InsufficientBalanceException extends BaseException {

  private static final String DESC = "insufficient.balance.exception";

  public InsufficientBalanceException() {
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