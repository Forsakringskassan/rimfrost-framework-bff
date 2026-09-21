package se.fk.rimfrost.framework.bff.errorhandling;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class GlobalExceptionMapper
{
   private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionMapper.class);

   @ServerExceptionMapper
   public Response handleWebApplicationException(WebApplicationException e)
   {
      LOGGER.error("Upstream error status={}", e.getResponse().getStatus(), e);
      return Response.status(e.getResponse().getStatus())
            .entity(new ErrorResponse("Upstream error")).build();
   }

   // MicroProfile REST Client spec: transport-level errors throw ProcessingException
   @ServerExceptionMapper
   public Response handleProcessingException(ProcessingException e)
   {
      LOGGER.error("Backend unreachable", e);
      return Response.status(502).entity(new ErrorResponse("Upstream unavailable")).build();
   }

   @ServerExceptionMapper
   public Response handleException(Exception e)
   {
      LOGGER.error("Internal error", e);
      return Response.status(500).entity(new ErrorResponse("Internal server error")).build();
   }
}
