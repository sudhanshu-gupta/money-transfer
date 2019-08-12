package io.sudhanshugupta.moneytransfer.model;

import java.math.BigDecimal;
import lombok.Value;

@Value
public class AccountRequest {

  private String name;
  private String email;
  private BigDecimal balance;
}
