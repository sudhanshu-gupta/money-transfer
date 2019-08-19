package io.sudhanshugupta.moneytransfer.model;

import java.math.BigDecimal;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Value;

@Value
public class AccountRequest {
  @NotNull
  private String name;
  @Email(message = "invalid email address")
  private String email;
  @Min(value = 0, message = "initial balance must be greater or equal to zero")
  private BigDecimal balance;
}
