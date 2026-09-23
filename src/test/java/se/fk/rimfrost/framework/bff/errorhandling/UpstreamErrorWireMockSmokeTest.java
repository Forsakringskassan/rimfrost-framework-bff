package se.fk.rimfrost.framework.bff.errorhandling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.WireMockServer;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Exercises UpstreamErrorWireMock end-to-end: a real HTTP client hits the stubbed WireMock
// server, and the resulting real exception/response is fed into GlobalExceptionMapper, the same
// way a consuming BFF's REST client and exception mapper would interact in practice.
class UpstreamErrorWireMockSmokeTest
{
   private final GlobalExceptionMapper mapper = new GlobalExceptionMapper();
   // Pinned to HTTP/1.1: WireMock's fault injection (connection reset, empty response) is only
   // reliably observable without HTTP/2 upgrade negotiation getting in the way.
   private final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

   private static class TestResource extends UpstreamErrorWireMock
   {
      @Override
      protected Map<String, String> wiremockMapping(WireMockServer server)
      {
         return Map.of("test.upstream.url", server.baseUrl());
      }
   }

   private final TestResource resource = new TestResource();

   @AfterEach
   void stopServer()
   {
      resource.stop();
   }

   @Test
   @DisplayName("stubUpstreamStatus: upstreamstatus mappas genom mappern till samma status")
   void stubUpstreamStatus_mapsThroughExceptionMapper() throws Exception
   {
      resource.start();
      UpstreamErrorWireMock.stubUpstreamStatus(404);

      HttpResponse<Void> response = client.send(
            HttpRequest.newBuilder(URI.create(UpstreamErrorWireMock.getWireMockServer().baseUrl())).GET().build(),
            HttpResponse.BodyHandlers.discarding());
      WebApplicationException upstreamError = new WebApplicationException(
            Response.status(response.statusCode()).build());

      Response mapped = mapper.handleWebApplicationException(upstreamError);

      assertEquals(404, mapped.getStatus());
      assertEquals(new ErrorResponse("Upstream error"), mapped.getEntity());
   }

   @Test
   @DisplayName("stubConnectionReset: verklig connection-reset mappas till 502")
   void stubConnectionReset_mapsTo502()
   {
      resource.start();
      UpstreamErrorWireMock.stubConnectionReset();

      IOException ioException = assertThrows(IOException.class, () -> client.send(
            HttpRequest.newBuilder(URI.create(UpstreamErrorWireMock.getWireMockServer().baseUrl())).GET().build(),
            HttpResponse.BodyHandlers.discarding()));

      Response mapped = mapper.handleProcessingException(new ProcessingException(ioException));

      assertEquals(502, mapped.getStatus());
      assertEquals(new ErrorResponse("Upstream unavailable"), mapped.getEntity());
   }

   @Test
   @DisplayName("stubEmptyResponse: maskerat nätverksfel från ett tomt svar mappas till 502")
   void stubEmptyResponse_mapsTo502()
   {
      resource.start();
      UpstreamErrorWireMock.stubEmptyResponse();

      IOException ioException = assertThrows(IOException.class, () -> client.send(
            HttpRequest.newBuilder(URI.create(UpstreamErrorWireMock.getWireMockServer().baseUrl())).GET().build(),
            HttpResponse.BodyHandlers.discarding()));
      NullPointerException maskedByLoggingFilter = new NullPointerException("no response headers");
      maskedByLoggingFilter.addSuppressed(ioException);

      Response mapped = mapper.handleException(maskedByLoggingFilter);

      assertEquals(502, mapped.getStatus());
      assertEquals(new ErrorResponse("Upstream unavailable"), mapped.getEntity());
   }
}
