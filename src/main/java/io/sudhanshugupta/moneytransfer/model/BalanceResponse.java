package io.sudhanshugupta.moneytransfer.model;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BalanceResponse {
  private BigDecimal amount;
}
