package io.sudhanshugupta.moneytransfer;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.Header;
import io.sudhanshugupta.moneytransfer.enumeration.TransactionStatus;
import io.sudhanshugupta.moneytransfer.model.AccountRequest;
import io.sudhanshugupta.moneytransfer.model.AccountResponse;
import io.sudhanshugupta.moneytransfer.model.AccountTransferRequest;
import io.sudhanshugupta.moneytransfer.model.AccountTransferResponse;
import io.sudhanshugupta.moneytransfer.model.BalanceResponse;
import io.sudhanshugupta.moneytransfer.model.ErrorResponse;
import java.io.IOException;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import redis.embedded.RedisServer;

@QuarkusTest
public class AccountResourceIntegrationTest {

  @Test
  public void shouldTransferAmountWhenSenderHasSufficientBalance() {
    double senderInitialBalance = 50.0, recipientInitialBalance = 59.9, transferBalance = 25;
    long sender = createAccount("test1@gmail.com", senderInitialBalance);
    long recipient = createAccount("test2@gmail.com", recipientInitialBalance);
    AccountTransferRequest transferRequest = new AccountTransferRequest(
        BigDecimal.valueOf(transferBalance), recipient);
    AccountTransferResponse response = given()
        .when()
        .header(new Header("accountId", String.valueOf(sender)))
        .contentType("application/json")
        .body(transferRequest)
        .post("/account/transfer")
        .then()
        .statusCode(202)
        .extract().response().as(AccountTransferResponse.class);
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
    assertThat(response.getTransactionReference()).isNotNull();
    assertThat(getBalance(sender))
        .isEqualTo(BigDecimal.valueOf(senderInitialBalance - transferBalance));
    assertThat(getBalance(recipient))
        .isEqualTo(BigDecimal.valueOf(recipientInitialBalance + transferBalance));
  }

  @Test
  public void shouldThrowExceptionWhenSenderHasInSufficientBalance() {
    double senderInitialBalance = 50.0, recipientInitialBalance = 59.9, transferBalance = 55;
    long sender = createAccount("test3@gmail.com", senderInitialBalance);
    long recipient = createAccount("test4@gmail.com", recipientInitialBalance);
    AccountTransferRequest transferRequest = new AccountTransferRequest(
        BigDecimal.valueOf(transferBalance), recipient);
    ErrorResponse response = given()
        .when()
        .header(new Header("accountId", String.valueOf(sender)))
        .contentType("application/json")
        .body(transferRequest)
        .post("/account/transfer")
        .then()
        .statusCode(400)
        .extract().response().as(ErrorResponse.class);
    assertThat(response).isNotNull();
    assertThat(response.getContext().get("transactionStatus"))
        .isEqualTo(TransactionStatus.FAILED.toString());
    assertThat(response.getContext().get("transactionRef")).isNotNull();
    assertThat(getBalance(sender)).isEqualTo(BigDecimal.valueOf(senderInitialBalance));
    assertThat(getBalance(recipient)).isEqualTo(BigDecimal.valueOf(recipientInitialBalance));
  }

  @Test
  public void shouldThrowExceptionWhenSenderAccountDoesNotExist() {
    double senderInitialBalance = 50.0, recipientInitialBalance = 59.9, transferBalance = 55;
    long sender = 213;
    long recipient = createAccount("test5@gmail.com", recipientInitialBalance);
    AccountTransferRequest transferRequest = new AccountTransferRequest(
        BigDecimal.valueOf(transferBalance), recipient);
    ErrorResponse response = given()
        .when()
        .header(new Header("accountId", String.valueOf(sender)))
        .contentType("application/json")
        .body(transferRequest)
        .post("/account/transfer")
        .then()
        .statusCode(404)
        .extract().response().as(ErrorResponse.class);
    assertThat(response).isNotNull();
    assertThat(getBalance(recipient)).isEqualTo(BigDecimal.valueOf(recipientInitialBalance));
  }

  @Test
  public void shouldThrowExceptionWhenRecipientAccountDoesNotExist() {
    double senderInitialBalance = 50.0, recipientInitialBalance = 59.9, transferBalance = 55;
    long sender = createAccount("test6@gmail.com", senderInitialBalance);
    long recipient = 233;
    AccountTransferRequest transferRequest = new AccountTransferRequest(
        BigDecimal.valueOf(transferBalance), recipient);
    ErrorResponse response = given()
        .when()
        .header(new Header("accountId", String.valueOf(sender)))
        .contentType("application/json")
        .body(transferRequest)
        .post("/account/transfer")
        .then()
        .statusCode(404)
        .extract().response().as(ErrorResponse.class);
    assertThat(response).isNotNull();
    assertThat(getBalance(sender)).isEqualTo(BigDecimal.valueOf(senderInitialBalance));
  }

  @Test
  public void shouldCreateAccountSuccess() {
    AccountRequest accountRequest = new AccountRequest("test", "test@gmail.com",
        BigDecimal.valueOf(4));
    AccountResponse response = given().when().contentType("application/json").body(accountRequest)
        .post("/account").then()
        .statusCode(201).extract().response().as(AccountResponse.class);
    assertThat(response).isNotNull();
    assertThat(response.getAccountId()).isGreaterThanOrEqualTo(1);
  }

  @Test
  public void shouldNotCreateAccountWhenAccountWithEmailExist() {
    double senderInitialBalance = 50.0;
    long sender = createAccount("test9@gmail.com", senderInitialBalance);
    AccountRequest accountRequest = new AccountRequest("test", "test9@gmail.com",
        BigDecimal.valueOf(4));
    ErrorResponse response = given().when().contentType("application/json").body(accountRequest)
        .post("/account").then()
        .statusCode(400).extract().response().as(ErrorResponse.class);
    assertThat(response).isNotNull();
  }

  @Test
  public void shouldGetBalanceSuccess() {
    double senderInitialBalance = 50.0;
    long sender = createAccount("test7@gmail.com", senderInitialBalance);
    BalanceResponse response = given().when()
        .header(new Header("accountId", String.valueOf(sender)))
        .contentType("application/json")
        .get("/account/balance")
        .then()
        .statusCode(200)
        .extract().response().as(BalanceResponse.class);
    assertThat(response).isNotNull();
    assertThat(response.getAmount()).isEqualTo(BigDecimal.valueOf(senderInitialBalance));
  }

  @Test
  public void shouldThrowNotFoundExceptionWhenAccountDoesNotExist() {
    long sender = 330;
    ErrorResponse response = given().when().header(new Header("accountId", String.valueOf(sender)))
        .contentType("application/json")
        .get("/account/balance")
        .then()
        .statusCode(404)
        .extract().response().as(ErrorResponse.class);
    assertThat(response).isNotNull();
  }

  private BigDecimal getBalance(long accountId) {

    return given().when().header(new Header("accountId", String.valueOf(accountId)))
        .contentType("application/json")
        .get("/account/balance")
        .then()
        .statusCode(200)
        .extract().response().as(BalanceResponse.class).getAmount();
  }

  private long createAccount(String email, Double balance) {
    AccountRequest accountRequest = new AccountRequest("test", email, BigDecimal.valueOf(balance));
    return given().when().contentType("application/json").body(accountRequest).post("/account")
        .then()
        .statusCode(201).extract().response().as(AccountResponse.class).getAccountId();
  }
}