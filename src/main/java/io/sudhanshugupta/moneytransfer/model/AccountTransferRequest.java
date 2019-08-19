package io.sudhanshugupta.moneytransfer.model;

import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Value;

@Value
public class AccountTransferRequest {
  @NotNull(message = "amount cannot be null")
  @Min(value = 0, message = "amount must be greater than 0")
  private BigDecimal amount;
  @NotNull(message = "receiverId cannot be null")
  @Min(value = 1, message = "receiverId must be greater than 0")
  private Long recipientAccountId;
}
