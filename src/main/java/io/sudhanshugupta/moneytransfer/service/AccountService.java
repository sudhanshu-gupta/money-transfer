package io.sudhanshugupta.moneytransfer.service;

import io.sudhanshugupta.moneytransfer.entity.Account;
import io.sudhanshugupta.moneytransfer.entity.AccountTransfer;
import io.sudhanshugupta.moneytransfer.enumeration.TransactionStatus;
import io.sudhanshugupta.moneytransfer.errors.BadRequestException;
import io.sudhanshugupta.moneytransfer.errors.ErrorEnum;
import io.sudhanshugupta.moneytransfer.errors.NotFoundException;
import io.sudhanshugupta.moneytransfer.errors.TransactionException;
import io.sudhanshugupta.moneytransfer.model.AccountRequest;
import io.sudhanshugupta.moneytransfer.repository.AccountRepository;
import io.sudhanshugupta.moneytransfer.repository.AccountTransferRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class AccountService {

  private final AccountRepository accountRepository;
  private final AccountTransferRepository accountTransferRepository;

  public Account getAccount(long accountId) {
    return accountRepository.getById(accountId)
        .orElseThrow(() -> new NotFoundException(ErrorEnum.ACCOUNT_NOT_FOUND));
  }

  @Transactional(rollbackOn = Exception.class)
  public AccountTransfer transfer(final long senderId, final long recipientId,
      final BigDecimal amount) {
    Account sender = accountRepository.getById(senderId)
        .orElseThrow(() -> new NotFoundException(ErrorEnum.ACCOUNT_NOT_FOUND));
    Account recipient = accountRepository.getById(recipientId)
        .orElseThrow(() -> new NotFoundException(ErrorEnum.ACCOUNT_NOT_FOUND));
    try {
      sender.debit(amount);
      recipient.credit(amount);
      accountRepository.persist(sender, recipient);
      AccountTransfer accountTransfer = createAccountTransfer(amount, sender, recipient,
          TransactionStatus.COMPLETED);
      accountTransferRepository.persist(accountTransfer);
      return accountTransfer;
    } catch (TransactionException e) {
      Map<String, String> context = updateFailedTransaction(sender, recipient, amount,
          e.getContext());
      throw new TransactionException(e.getErrorEnum(), context, e);
    } catch (Exception e) {
      Map<String, String> context = updateFailedTransaction(sender, recipient, amount,
          new HashMap<>());
      throw new TransactionException(ErrorEnum.UNKNOWN, context, e);
    }
  }

  private Map<String, String> updateFailedTransaction(Account sender, Account recipient,
      BigDecimal amount, Map<String, String> context) {
    AccountTransfer accountTransfer = createAccountTransfer(amount, sender, recipient,
        TransactionStatus.FAILED);
    accountTransferRepository.persist(accountTransfer);
    context.put("transactionRef", accountTransfer.getTransactionRef());
    context.put("transactionStatus", accountTransfer.getStatus().name());
    return context;
  }

  private AccountTransfer createAccountTransfer(BigDecimal amount, Account sender,
      Account recipient, TransactionStatus status) {
    return AccountTransfer.builder()
        .amount(amount)
        .sender(sender)
        .recipient(recipient)
        .status(status)
        .transactionRef(UUID.randomUUID().toString())
        .build();
  }

  @Transactional(rollbackOn = Exception.class)
  public Account createAccount(final AccountRequest accountRequest) {
    if (accountRepository.findByEmail(accountRequest.getEmail()).isPresent()) {
      throw new BadRequestException(ErrorEnum.ACCOUNT_EXIST);
    }
    Account account = Account.builder()
        .balance(Optional.ofNullable(accountRequest.getBalance()).orElse(BigDecimal.ZERO))
        .email(accountRequest.getEmail())
        .name(accountRequest.getName())
        .build();
    accountRepository.persist(account);
    return accountRepository.findByEmail(accountRequest.getEmail())
        .orElseThrow(() -> new TransactionException(ErrorEnum.ACCOUNT_CREATION_FAILED));
  }
}
