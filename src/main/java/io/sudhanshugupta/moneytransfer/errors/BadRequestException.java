package io.sudhanshugupta.moneytransfer.errors;

import java.util.Map;

public class BadRequestException extends ServiceException {

  public BadRequestException(ErrorEnum errorEnum) {
    super(errorEnum);
  }

  public BadRequestException(ErrorEnum errorEnum, Map<String, String> context, Throwable ex) {
    super(errorEnum, context, ex);
  }
}
