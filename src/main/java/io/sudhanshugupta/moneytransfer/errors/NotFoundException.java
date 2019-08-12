package io.sudhanshugupta.moneytransfer.errors;

import java.util.Map;

public class NotFoundException extends ServiceException {

  public NotFoundException(ErrorEnum errorEnum) {
    super(errorEnum);
  }

  public NotFoundException(ErrorEnum errorEnum, Map<String, String> context, Throwable ex) {
    super(errorEnum, context, ex);
  }
}
