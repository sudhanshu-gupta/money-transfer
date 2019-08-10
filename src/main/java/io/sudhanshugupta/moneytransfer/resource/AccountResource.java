package io.sudhanshugupta.moneytransfer.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/account")
public class AccountResource {

  @GET
  @Path("/balance")
  @Produces(MediaType.APPLICATION_JSON)
  public String hello() {
    return "hello";
  }
}