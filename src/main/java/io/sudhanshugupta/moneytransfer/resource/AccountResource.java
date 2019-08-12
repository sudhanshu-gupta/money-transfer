package io.sudhanshugupta.moneytransfer.resource;

import io.sudhanshugupta.moneytransfer.facade.AccountServiceFacade;
import io.sudhanshugupta.moneytransfer.model.AccountRequest;
import io.sudhanshugupta.moneytransfer.model.MoneyTransferRequest;
import java.net.URI;
import java.util.concurrent.CompletionStage;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.annotations.jaxrs.HeaderParam;

@Path("/account")
@RequiredArgsConstructor
public class AccountResource {

  private static final String ACCOUNT_URI_FORMAT = "http://localhost:8080/account/%s";
  private final AccountServiceFacade accountServiceFacade;

  @GET
  @Path("/balance")
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getBalance(@HeaderParam("accountId") final long accountId) {
    return accountServiceFacade.getBalance(accountId).thenApply(res -> Response.ok(res).build());
  }

  @POST
  @Path("/transfer")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> transferMoney(@HeaderParam("accountId") final long accountId,
      @Valid MoneyTransferRequest moneyTransferRequest) {
    return accountServiceFacade.transfer(accountId, moneyTransferRequest)
        .thenApply(res -> Response.ok(res).build());
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createAccount(@Valid AccountRequest accountRequest) {
    return accountServiceFacade.createAccount(accountRequest)
        .thenApply(res -> Response.created(getLocation(res.getAccountId())).entity(res).build());
  }

  private URI getLocation(long accountId) {
    return URI.create(String.format(ACCOUNT_URI_FORMAT, accountId));
  }
}