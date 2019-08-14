package io.sudhanshugupta.moneytransfer.resource.handler;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class UncaughtExceptionHandler implements ExceptionMapper<Exception> {

  @Override
  public Response toResponse(Exception ex) {
    return Response.serverError().entity(
        ExceptionUtil.errorResponse(String.valueOf(Status.INTERNAL_SERVER_ERROR.getStatusCode()),
            ex.getMessage(), null)).type(MediaType.APPLICATION_JSON).build();
  }
}
