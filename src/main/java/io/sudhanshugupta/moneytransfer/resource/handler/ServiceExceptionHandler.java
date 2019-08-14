package io.sudhanshugupta.moneytransfer.resource.handler;

import io.sudhanshugupta.moneytransfer.errors.ServiceException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ServiceExceptionHandler implements ExceptionMapper<ServiceException> {

  @Override
  public Response toResponse(ServiceException ex) {
    return Response.status(ex.getErrorEnum().getCode()).entity(
        ExceptionUtil
            .errorResponse(ex.getErrorEnum().getServiceCode(), ex.getErrorEnum().getMessage(),
                ex.getContext())).type(MediaType.APPLICATION_JSON).build();
  }
}
