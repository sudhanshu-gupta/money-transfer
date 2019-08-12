package io.sudhanshugupta.moneytransfer.model;

import io.sudhanshugupta.moneytransfer.enumeration.TransactionStatus;
import lombok.Value;

@Value(staticConstructor = "of")
public class MoneyTransferResponse {

  private String transactionReference;
  private TransactionStatus status;
}
