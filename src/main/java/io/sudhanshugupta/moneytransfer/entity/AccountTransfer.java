package io.sudhanshugupta.moneytransfer.entity;

import io.sudhanshugupta.moneytransfer.enumeration.TransactionStatus;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
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
@Table(name = "account_transfer")
@DynamicUpdate
public class AccountTransfer implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(targetEntity = Account.class)
  @JoinColumn(name = "sender_id")
  private Account sender;

  @JoinColumn(name = "recipient_id")
  private Account recipient;

  @Column(columnDefinition = "INTEGER")
  @Enumerated
  private TransactionStatus status;

  private String comment;

  @Column(columnDefinition = "numeric")
  private BigDecimal amount;

  private String transactionRef;

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
}
