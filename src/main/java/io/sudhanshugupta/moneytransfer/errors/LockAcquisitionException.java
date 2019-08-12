package io.sudhanshugupta.moneytransfer.errors;

import java.util.Map;

public class LockAcquisitionException extends ServiceException {

  public LockAcquisitionException() {
    super(ErrorEnum.LOCK_ACQUISITION_EXCEPTION);
  }

  public LockAcquisitionException(Map<String, String> context, Throwable ex) {
    super(ErrorEnum.LOCK_ACQUISITION_EXCEPTION, context, ex);
  }
}
