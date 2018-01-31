/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ArrowheadExceptionMapper implements ExceptionMapper<ArrowheadException> {

  @Override
  public Response toResponse(ArrowheadException ex) {
    ex.printStackTrace();
    ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(), ex.getErrorCode(), ex.getExceptionType(), ex.getOrigin());
    return Response.status(ex.getErrorCode()).entity(errorMessage).header("Content-type", "application/json").build();
  }

}
