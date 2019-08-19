package io.sudhanshugupta.moneytransfer.entity;

import io.sudhanshugupta.moneytransfer.errors.ErrorEnum;
import io.sudhanshugupta.moneytransfer.errors.TransactionException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@DynamicUpdate
public class Account implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String email;

  @Column(columnDefinition = "numeric")
  private BigDecimal balance;

  @Column(columnDefinition = "timestamp")
  private Timestamp createdAt;

  @Column(columnDefinition = "timestamp")
  private Timestamp updatedAt;

  @PrePersist
  public void prePersist() {
    createdAt = Optional.ofNullable(createdAt).orElse(new Timestamp(Instant.now().toEpochMilli()));
    updatedAt = Optional.ofNullable(updatedAt).orElse(new Timestamp(Instant.now().toEpochMilli()));
  }

  @PreUpdate
  public void preUpdate() {
    updatedAt = Optional.ofNullable(updatedAt).orElse(new Timestamp(Instant.now().toEpochMilli()));
  }

  public void credit(BigDecimal amount) {
    balance = Optional.ofNullable(balance).orElse(BigDecimal.ZERO);
    if (amount.compareTo(BigDecimal.ZERO) < 0 && balance.compareTo(amount.abs()) < 0) {
      throw new TransactionException(ErrorEnum.INSUFFICIENT_BALANCE);
    }
    balance = balance.add(amount);
  }

  public void debit(BigDecimal amount) {
    balance = Optional.ofNullable(balance).orElse(BigDecimal.ZERO);
    if (amount.compareTo(BigDecimal.ZERO) > 0 && balance.compareTo(amount) < 0) {
      throw new TransactionException(ErrorEnum.INSUFFICIENT_BALANCE);
    }
    balance = balance.subtract(amount);
  }
}
