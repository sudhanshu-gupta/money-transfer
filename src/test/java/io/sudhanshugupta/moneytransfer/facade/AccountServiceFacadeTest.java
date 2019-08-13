package io.sudhanshugupta.moneytransfer.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.sudhanshugupta.moneytransfer.entity.Account;
import io.sudhanshugupta.moneytransfer.entity.AccountTransfer;
import io.sudhanshugupta.moneytransfer.enumeration.TransactionStatus;
import io.sudhanshugupta.moneytransfer.errors.LockAcquisitionException;
import io.sudhanshugupta.moneytransfer.errors.NotFoundException;
import io.sudhanshugupta.moneytransfer.errors.TransactionException;
import io.sudhanshugupta.moneytransfer.model.AccountRequest;
import io.sudhanshugupta.moneytransfer.model.AccountResponse;
import io.sudhanshugupta.moneytransfer.model.AccountTransferRequest;
import io.sudhanshugupta.moneytransfer.model.AccountTransferResponse;
import io.sudhanshugupta.moneytransfer.model.BalanceResponse;
import io.sudhanshugupta.moneytransfer.service.AccountLockService;
import io.sudhanshugupta.moneytransfer.service.AccountService;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
class AccountServiceFacadeTest {

  @Mock
  AccountService accountService;
  @Mock
  AccountLockService accountLockService;
  @InjectMocks
  AccountServiceFacade accountServiceFacade;

  @BeforeEach
  void setUp() {
    lenient().doNothing().when(accountLockService).acquireLock(anyLong());
    lenient().doNothing().when(accountLockService).releaseLock(anyLong());
  }

  @Test
  public void shouldCreateAccountSuccess() {
    Account expected = Account.builder().id(1L).email("test@gmail.com")
        .createdAt(new Timestamp(Instant.now().toEpochMilli())).build();
    AccountRequest accountRequest = new AccountRequest("test", "test@gmail.com", BigDecimal.TEN);
    doReturn(expected).when(accountService).createAccount(any(AccountRequest.class));
    AccountResponse actual = accountServiceFacade.createAccount(accountRequest)
        .toCompletableFuture().join();
    assertThat(actual).isNotNull();
    assertThat(actual.getAccountId()).isEqualTo(1L);
    assertThat(actual.getEmail()).isEqualTo("test@gmail.com");
    verify(accountService).createAccount(any(AccountRequest.class));
  }

  @Test
  public void shouldCreateAccountFailureWhenAccountServiceThrowsException() {
    AccountRequest accountRequest = new AccountRequest("test", "test@gmail.com", BigDecimal.TEN);
    doThrow(TransactionException.class).when(accountService)
        .createAccount(any(AccountRequest.class));
    assertThatExceptionOfType(CompletionException.class).isThrownBy(
        () -> accountServiceFacade.createAccount(accountRequest).toCompletableFuture().join())
        .withCauseInstanceOf(TransactionException.class);
    verify(accountService).createAccount(any(AccountRequest.class));
  }

  @Test
  public void shouldGetBalanceSuccess() {
    Account expected = Account.builder().id(1L).email("test@gmail.com").balance(BigDecimal.TEN)
        .createdAt(new Timestamp(Instant.now().toEpochMilli())).build();
    when(accountService.getAccount(eq(1L))).thenReturn(expected);
    BalanceResponse response = accountServiceFacade.getBalance(1L).toCompletableFuture().join();
    assertThat(response).isNotNull();
    assertThat(response.getAmount()).isEqualTo(BigDecimal.TEN);
    verify(accountService).getAccount(1L);
    verify(accountLockService).releaseLock(eq(1L));
    verify(accountLockService).acquireLock(eq(1L));
  }

  @Test
  public void shouldGetBalanceFailureWhenAccountServiceThrowsException() {
    when(accountService.getAccount(eq(1L))).thenThrow(NotFoundException.class);
    assertThatExceptionOfType(CompletionException.class)
        .isThrownBy(() -> accountServiceFacade.getBalance(1L).toCompletableFuture().join())
        .withCauseInstanceOf(NotFoundException.class);
    verify(accountService).getAccount(1L);
    verify(accountLockService).releaseLock(eq(1L));
    verify(accountLockService).acquireLock(eq(1L));
  }

  @Test
  public void shouldGetBalanceFailureWhenLockAcquisitionException() {
    doThrow(LockAcquisitionException.class).when(accountLockService).acquireLock(eq(1L));
    assertThatExceptionOfType(CompletionException.class)
        .isThrownBy(() -> accountServiceFacade.getBalance(1L).toCompletableFuture().join())
        .withCauseInstanceOf(LockAcquisitionException.class);
    verify(accountService, never()).getAccount(1L);
    verify(accountLockService, never()).releaseLock(eq(1L));
    verify(accountLockService).acquireLock(eq(1L));
  }

  @Test
  public void shouldTransferMoneySuccess() {
    AccountTransferRequest transferRequest = new AccountTransferRequest(BigDecimal.ONE, 2L);
    String transactionRef = UUID.randomUUID().toString();
    AccountTransfer accountTransfer = AccountTransfer.builder()
        .transactionRef(transactionRef).status(TransactionStatus.COMPLETED).build();
    when(accountService.transfer(eq(1L), eq(2L), eq(BigDecimal.ONE))).thenReturn(accountTransfer);
    AccountTransferResponse response = accountServiceFacade.transfer(1L, transferRequest)
        .toCompletableFuture().join();
    assertThat(response).isNotNull();
    assertThat(response.getTransactionReference()).isEqualTo(transactionRef);
    assertThat(response.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
    verify(accountService).transfer(eq(1L), eq(2L), eq(BigDecimal.ONE));
    verify(accountLockService).releaseLock(eq(1L));
    verify(accountLockService).releaseLock(eq(2L));
    verify(accountLockService).acquireLock(eq(2L));
    verify(accountLockService).acquireLock(eq(1L));
  }

  @Test
  public void shouldTransferMoneyFailureWhenAccountServiceThrowsException() {
    AccountTransferRequest transferRequest = new AccountTransferRequest(BigDecimal.ONE, 2L);
    when(accountService.transfer(eq(1L), eq(2L), eq(BigDecimal.ONE)))
        .thenThrow(TransactionException.class);
    assertThatExceptionOfType(CompletionException.class).isThrownBy(
        () -> accountServiceFacade.transfer(1L, transferRequest).toCompletableFuture().join())
        .withCauseInstanceOf(TransactionException.class);
    verify(accountService).transfer(eq(1L), eq(2L), eq(BigDecimal.ONE));
    verify(accountLockService).releaseLock(eq(1L));
    verify(accountLockService).releaseLock(eq(2L));
    verify(accountLockService).acquireLock(eq(2L));
    verify(accountLockService).acquireLock(eq(1L));
  }

  @Test
  public void shouldTransferMoneyFailureWhenSenderLockAcquisitionException() {
    doThrow(LockAcquisitionException.class).when(accountLockService).acquireLock(eq(1L));
    AccountTransferRequest transferRequest = new AccountTransferRequest(BigDecimal.ONE, 2L);
    assertThatExceptionOfType(CompletionException.class).isThrownBy(
        () -> accountServiceFacade.transfer(1L, transferRequest).toCompletableFuture().join())
        .withCauseInstanceOf(LockAcquisitionException.class);
    verify(accountService, never()).transfer(eq(1L), eq(2L), eq(BigDecimal.ONE));
    verify(accountLockService, never()).releaseLock(eq(1L));
    verify(accountLockService, never()).releaseLock(eq(2L));
    verify(accountLockService, never()).acquireLock(eq(2L));
    verify(accountLockService).acquireLock(eq(1L));
  }

  @Test
  public void shouldTransferMoneyFailureWhenRecipientLockAcquisitionException() {
    doNothing().when(accountLockService).acquireLock(eq(1L));
    doThrow(LockAcquisitionException.class).when(accountLockService).acquireLock(eq(2L));
    AccountTransferRequest transferRequest = new AccountTransferRequest(BigDecimal.ONE, 2L);
    assertThatExceptionOfType(CompletionException.class).isThrownBy(
        () -> accountServiceFacade.transfer(1L, transferRequest).toCompletableFuture().join())
        .withCauseInstanceOf(LockAcquisitionException.class);
    verify(accountService, never()).transfer(eq(1L), eq(2L), eq(BigDecimal.ONE));
    verify(accountLockService).releaseLock(eq(1L));
    verify(accountLockService, never()).releaseLock(eq(2L));
    verify(accountLockService).acquireLock(eq(2L));
    verify(accountLockService).acquireLock(eq(1L));
  }
}