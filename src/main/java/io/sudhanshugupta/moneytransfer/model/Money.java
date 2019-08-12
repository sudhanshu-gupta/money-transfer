package io.sudhanshugupta.moneytransfer.model;

import java.math.BigDecimal;
import lombok.Value;

@Value(staticConstructor = "of")
public class Money {

  private BigDecimal amount;
}
