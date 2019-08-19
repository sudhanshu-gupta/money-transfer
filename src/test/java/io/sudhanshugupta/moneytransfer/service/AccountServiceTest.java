package io.sudhanshugupta.moneytransfer.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
class AccountServiceTest {

  @Mock
  AccountRepository accountRepository;
  @Mock
  AccountTransferRepository accountTransferRepository;
  @InjectMocks
  AccountService accountService;

  @Test
  public void shouldCreditTransferMoneyWhenSenderHasSufficientMoney() {
    doNothing().when(accountRepository)
        .persist(any(Account.class), any(Account.class));
    doNothing().when(accountTransferRepository).persist(any(AccountTransfer.class));
    Optional<Account> sender = Optional
        .of(Account.builder().id(1L).balance(BigDecimal.valueOf(50)).build());
    Optional<Account> recipient = Optional
        .of(Account.builder().id(2L).balance(BigDecimal.valueOf(10)).build());
    doReturn(sender).when(accountRepository).getById(eq(1L));
    doReturn(recipient).when(accountRepository).getById(eq(2L));
    AccountTransfer accountTransfer = accountService.transfer(1L, 2L, BigDecimal.valueOf(20));
    assertThat(accountTransfer).isNotNull();
    assertThat(accountTransfer.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
    assertThat(accountTransfer.getTransactionRef()).isNotNull();
    verify(accountTransferRepository).persist(any(AccountTransfer.class));
    verify(accountRepository).persist(any(Account.class), any(Account.class));
    verify(accountRepository).getById(eq(1L));
    verify(accountRepository).getById(eq(2L));
  }

  @Test
  public void shouldDebitTransferMoneyWhenSenderHasSufficientMoney() {
    doNothing().when(accountRepository)
        .persist(any(Account.class), any(Account.class));
    doNothing().when(accountTransferRepository).persist(any(AccountTransfer.class));
    Optional<Account> sender = Optional
        .of(Account.builder().id(1L).balance(BigDecimal.valueOf(50)).build());
    Optional<Account> recipient = Optional
        .of(Account.builder().id(2L).balance(BigDecimal.valueOf(10)).build());
    doReturn(sender).when(accountRepository).getById(eq(1L));
    doReturn(recipient).when(accountRepository).getById(eq(2L));
    AccountTransfer accountTransfer = accountService.transfer(1L, 2L, BigDecimal.valueOf(-10));
    assertThat(accountTransfer).isNotNull();
    assertThat(accountTransfer.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
    assertThat(accountTransfer.getTransactionRef()).isNotNull();
    verify(accountTransferRepository).persist(any(AccountTransfer.class));
    verify(accountRepository).persist(any(Account.class), any(Account.class));
    verify(accountRepository).getById(eq(1L));
    verify(accountRepository).getById(eq(2L));
  }



  @Test
  public void shouldThrowExceptionWhenSenderHasInSufficientMoney() {
    doNothing().when(accountTransferRepository).persist(any(AccountTransfer.class));
    Optional<Account> sender = Optional
        .of(Account.builder().id(1L).balance(BigDecimal.valueOf(10)).build());
    Optional<Account> recipient = Optional
        .of(Account.builder().id(2L).balance(BigDecimal.valueOf(10)).build());
    doReturn(sender).when(accountRepository).getById(eq(1L));
    doReturn(recipient).when(accountRepository).getById(eq(2L));
    assertThatExceptionOfType(TransactionException.class)
        .isThrownBy(() -> accountService.transfer(1L, 2L, BigDecimal.valueOf(20)))
        .matches((e) -> e.getErrorEnum().getMessage().equals("Account has insufficient balance"));
    verify(accountTransferRepository).persist(any(AccountTransfer.class));
    verify(accountRepository).getById(eq(1L));
    verify(accountRepository).getById(eq(2L));
    verify(accountRepository, never()).persist(any(Account.class), any(Account.class));
  }

  @Test
  public void shouldThrowExceptionWhenSenderDoesNotExist() {
    doReturn(Optional.empty()).when(accountRepository).getById(eq(1L));
    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> accountService.transfer(1L, 2L, BigDecimal.valueOf(20)))
        .matches((e) -> e.getErrorEnum().getMessage()
            .equals("Account (sender/recipient) doesn't exist"));
    verify(accountTransferRepository, never()).persist(any(AccountTransfer.class));
    verify(accountRepository).getById(eq(1L));
    verify(accountRepository, never()).getById(eq(2L));
    verify(accountRepository, never()).persist(any(Account.class), any(Account.class));
  }

  @Test
  public void shouldThrowExceptionWhenRecipientDoesNotExist() {
    Optional<Account> sender = Optional
        .of(Account.builder().id(1L).balance(BigDecimal.valueOf(10)).build());
    doReturn(sender).when(accountRepository).getById(eq(1L));
    doReturn(Optional.empty()).when(accountRepository).getById(eq(2L));
    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> accountService.transfer(1L, 2L, BigDecimal.valueOf(20)))
        .matches((e) -> e.getErrorEnum().getMessage()
            .equals("Account (sender/recipient) doesn't exist"));
    verify(accountTransferRepository, never()).persist(any(AccountTransfer.class));
    verify(accountRepository).getById(eq(1L));
    verify(accountRepository).getById(eq(2L));
    verify(accountRepository, never()).persist(any(Account.class), any(Account.class));
  }

  @Test
  public void shouldThrowExceptionWhenDBExceptionOccur() {
    doThrow(RuntimeException.class).when(accountRepository)
        .persist(any(Account.class), any(Account.class));
    doNothing().when(accountTransferRepository).persist(any(AccountTransfer.class));
    Optional<Account> sender = Optional
        .of(Account.builder().id(1L).balance(BigDecimal.valueOf(50)).build());
    Optional<Account> recipient = Optional
        .of(Account.builder().id(2L).balance(BigDecimal.valueOf(10)).build());
    doReturn(sender).when(accountRepository).getById(eq(1L));
    doReturn(recipient).when(accountRepository).getById(eq(2L));
    assertThatExceptionOfType(TransactionException.class)
        .isThrownBy(() -> accountService.transfer(1L, 2L, BigDecimal.valueOf(20)))
        .matches((e) -> e.getErrorEnum().getMessage().equals("Unknown error occurred"));
    verify(accountTransferRepository).persist(any(AccountTransfer.class));
    verify(accountRepository).getById(eq(1L));
    verify(accountRepository).getById(eq(2L));
    verify(accountRepository).persist(any(Account.class), any(Account.class));
  }

  @Test
  public void shouldCreateAccountWhenSuccess() {
    Account expected = Account.builder().id(1L).email("test@gmail.com").build();
    AccountRequest accountRequest = new AccountRequest("test", "test@gmail.com", BigDecimal.TEN);
    doNothing().when(accountRepository).persist(any(Account.class));
    when(accountRepository.findByEmail(anyString())).thenReturn(Optional.empty())
        .thenReturn(Optional.of(expected));
    Account actual = accountService.createAccount(accountRequest);
    assertThat(actual).isNotNull();
    assertThat(actual.getId()).isGreaterThanOrEqualTo(1L);
    assertThat(actual.getEmail()).isEqualTo("test@gmail.com");
    verify(accountRepository, times(2)).findByEmail(anyString());
    verify(accountRepository).persist(any(Account.class));
  }

  @Test
  public void shouldNotCreateAccountWhenAccountWithSameEmailExist() {
    Account expected = Account.builder().id(1L).email("test@gmail.com").build();
    AccountRequest accountRequest = new AccountRequest("test", "test@gmail.com", BigDecimal.TEN);
    when(accountRepository.findByEmail(anyString())).thenReturn(Optional.of(expected));
    assertThatExceptionOfType(BadRequestException.class)
        .isThrownBy(() -> accountService.createAccount(accountRequest))
        .matches(ex -> ex.getErrorEnum() == ErrorEnum.ACCOUNT_EXIST);
    verify(accountRepository).findByEmail(anyString());
    verify(accountRepository, never()).persist(any(Account.class));
  }

  @Test
  public void shouldNotCreateAccountWhenDbExceptionThrown() {
    doThrow(RuntimeException.class).when(accountRepository).persist(any(Account.class));
    AccountRequest accountRequest = new AccountRequest("test", "test@gmail.com", BigDecimal.TEN);
    when(accountRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> accountService.createAccount(accountRequest));
    verify(accountRepository).findByEmail(anyString());
    verify(accountRepository).persist(any(Account.class));
  }

  @Test
  public void shouldNotCreateAccountForUnknownReason() {
    AccountRequest accountRequest = new AccountRequest("test", "test@gmail.com", BigDecimal.TEN);
    doNothing().when(accountRepository).persist(any(Account.class));
    when(accountRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    assertThatExceptionOfType(TransactionException.class)
        .isThrownBy(() -> accountService.createAccount(accountRequest))
        .matches(ex -> ex.getErrorEnum() == ErrorEnum.ACCOUNT_CREATION_FAILED);
    verify(accountRepository, times(2)).findByEmail(anyString());
    verify(accountRepository).persist(any(Account.class));
  }

  @Test
  public void shouldGetAccountSuccess() {
    Account expected = Account.builder().id(1L).email("test@gmail.com").balance(BigDecimal.TEN)
        .build();
    when(accountRepository.getById(eq(1L))).thenReturn(Optional.of(expected));
    Account actual = accountService.getAccount(1L);
    assertThat(actual).isNotNull();
    assertThat(actual.getEmail()).isEqualTo("test@gmail.com");
    assertThat(actual.getId()).isEqualTo(1L);
    assertThat(actual.getBalance()).isEqualTo(BigDecimal.TEN);
    verify(accountRepository).getById(1L);
  }

  @Test
  public void shouldGetAccountFailureWhenAccountDoesNotExist() {
    when(accountRepository.getById(eq(1L))).thenReturn(Optional.empty());
    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> accountService.getAccount(1L))
        .matches(ex -> ex.getErrorEnum() == ErrorEnum.ACCOUNT_NOT_FOUND);
    verify(accountRepository).getById(1L);
  }
}