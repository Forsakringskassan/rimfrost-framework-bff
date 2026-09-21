package se.fk.rimfrost.framework.bff.errorhandling;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class GlobalExceptionMapperTest
{

   private final GlobalExceptionMapper mapper = new GlobalExceptionMapper();

   @Test
   @DisplayName("FBFF-FR-01.1: WebApplicationException mappas till samma statuskod med ErrorResponse")
   void handleWebApplicationException_returnsSameStatus()
   {
      WebApplicationException e = new WebApplicationException(Response.status(404).build());

      Response response = mapper.handleWebApplicationException(e);

      assertEquals(404, response.getStatus());
      assertEquals(new ErrorResponse("Upstream error"), response.getEntity());
   }

   @Test
   @DisplayName("FBFF-FR-01.2: ProcessingException mappas till HTTP 502 med ErrorResponse")
   void handleProcessingException_returns502()
   {
      Response response = mapper.handleProcessingException(new ProcessingException("connection refused"));

      assertEquals(502, response.getStatus());
      assertEquals(new ErrorResponse("Upstream unavailable"), response.getEntity());
   }

   @Test
   @DisplayName("FBFF-FR-01.5: Oväntade exceptions mappas till HTTP 500 med ErrorResponse")
   void handleException_returns500()
   {
      Response response = mapper.handleException(new RuntimeException("boom"));

      assertEquals(500, response.getStatus());
      assertEquals(new ErrorResponse("Internal server error"), response.getEntity());
   }

   @Test
   @DisplayName("FBFF-FR-01.3: Maskerat nätverksfel (suppressed IOException bakom NPE) mappas till HTTP 502")
   void handleException_maskedNetworkError_returns502()
   {
      NullPointerException npe = new NullPointerException("no response headers");
      npe.addSuppressed(new IOException("connection reset"));

      Response response = mapper.handleException(npe);

      assertEquals(502, response.getStatus());
      assertEquals(new ErrorResponse("Upstream unavailable"), response.getEntity());
   }

   @Test
   @DisplayName("FBFF-FR-01.3: Maskerat nätverksfel hittas även via orsakskedjan")
   void handleException_ioExceptionInCauseChain_returns502()
   {
      Exception wrapped = new RuntimeException("wrapper", new IOException("connection reset"));

      Response response = mapper.handleException(wrapped);

      assertEquals(502, response.getStatus());
      assertEquals(new ErrorResponse("Upstream unavailable"), response.getEntity());
   }

   @Test
   @DisplayName("FBFF-FR-01.3: Cirkulär orsakskedja fastnar inte i oändlig loop")
   void handleException_circularCause_doesNotHang()
   {
      RuntimeException a = new RuntimeException("a");
      RuntimeException b = new RuntimeException("b", a);
      a.initCause(b);

      Response response = mapper.handleException(a);

      assertEquals(500, response.getStatus());
      assertEquals(new ErrorResponse("Internal server error"), response.getEntity());
   }
}
