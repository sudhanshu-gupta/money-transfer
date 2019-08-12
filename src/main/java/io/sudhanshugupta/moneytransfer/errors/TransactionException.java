package io.sudhanshugupta.moneytransfer.errors;

import java.util.Map;

public class TransactionException extends ServiceException {

  public TransactionException(ErrorEnum errorEnum) {
    super(errorEnum);
  }

  public TransactionException(ErrorEnum errorEnum, Map<String, String> context, Throwable ex) {
    super(errorEnum, context, ex);
  }
}
