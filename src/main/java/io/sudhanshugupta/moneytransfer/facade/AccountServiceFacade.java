package io.sudhanshugupta.moneytransfer.facade;

import io.sudhanshugupta.moneytransfer.errors.BadRequestException;
import io.sudhanshugupta.moneytransfer.errors.ErrorEnum;
import io.sudhanshugupta.moneytransfer.model.AccountRequest;
import io.sudhanshugupta.moneytransfer.model.AccountResponse;
import io.sudhanshugupta.moneytransfer.model.BalanceResponse;
import io.sudhanshugupta.moneytransfer.model.MoneyTransferRequest;
import io.sudhanshugupta.moneytransfer.model.MoneyTransferResponse;
import io.sudhanshugupta.moneytransfer.service.AccountLockService;
import io.sudhanshugupta.moneytransfer.service.AccountService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class AccountServiceFacade {

  private final AccountService accountService;
  private final AccountLockService accountLockService;

  public CompletionStage<AccountResponse> createAccount(AccountRequest accountRequest) {
    return CompletableFuture.supplyAsync(
        () -> accountService.createAccount(accountRequest))
        .thenApply(res -> AccountResponse.builder()
            .accountId(res.getId())
            .balance(res.getBalance())
            .name(res.getName())
            .email(res.getEmail())
            .createdAt(res.getCreatedAt())
            .build());
  }

  public CompletionStage<BalanceResponse> getBalance(long accountId) {
    return CompletableFuture.supplyAsync(
        () -> {
          accountLockService.acquireLock(accountId);
          try {
            return accountService.getAccount(accountId);
          } finally {
            accountLockService.releaseLock(accountId);
          }
        })
        .thenApply(acc -> BalanceResponse.builder().amount(acc.getBalance()).build());
  }

  public CompletionStage<MoneyTransferResponse> transfer(long accountId,
      MoneyTransferRequest transferRequest) {
    if (transferRequest.getRecipientAccountId().equals(accountId)) {
      throw new BadRequestException(ErrorEnum.SENDER_RECIPIENT_SAME);
    }
    return CompletableFuture.supplyAsync(() -> {
      try {
        accountLockService.acquireLock(accountId);
        accountLockService.acquireLock(transferRequest.getRecipientAccountId());
        return accountService
            .transfer(accountId, transferRequest.getRecipientAccountId(),
                transferRequest.getAmount());
      } finally {
        accountLockService.releaseLock(accountId);
        accountLockService.releaseLock(transferRequest.getRecipientAccountId());
      }
    })
        .thenApply(transfer -> MoneyTransferResponse
            .of(transfer.getTransactionRef(), transfer.getStatus()));
  }
}
