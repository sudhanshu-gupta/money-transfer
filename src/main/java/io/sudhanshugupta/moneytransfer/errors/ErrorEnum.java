package io.sudhanshugupta.moneytransfer.errors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorEnum {

  ACCOUNT_NOT_FOUND(404, "SE-4004", "Account (sender/recipient) doesn't exist"),
  INSUFFICIENT_BALANCE(400, "SE-4005", "Account has insufficient balance"),
  UNKNOWN(500, "SE-5001", "Unknown error occurred"),
  ACCOUNT_EXIST(400, "SE-4006", "Account with same email already exist"),
  ACCOUNT_CREATION_FAILED(500, "SE-4007",
      "Due to unkown issue account creation failed, please try again"),
  SENDER_RECIPIENT_SAME(400, "SE-4007", "Sender and recipient cannot be same"),
  LOCK_ACQUISITION_EXCEPTION(423, "SE-4008", "Unable to acquire user lock");
  private final int code;
  private final String serviceCode;
  private final String message;
}
