package se.fk.rimfrost.framework.bff.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import jakarta.ws.rs.core.HttpHeaders;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Exercises AuthorizationHeaderWireMock end-to-end: a real HTTP client sends a request the same
// way OutgoingAuthorizationHeaderFilter would (with or without an Authorization header), and the
// helper's request capture is verified against what WireMock actually received.
class AuthorizationHeaderWireMockSmokeTest
{
   private static class TestResource extends AuthorizationHeaderWireMock
   {
      @Override
      protected Map<String, String> wiremockMapping(WireMockServer server)
      {
         return Map.of("test.downstream.url", server.baseUrl());
      }
   }

   private final TestResource resource = new TestResource();
   private final HttpClient client = HttpClient.newHttpClient();

   @AfterEach
   void stopServer()
   {
      resource.stop();
   }

   @Test
   @DisplayName("getLastReceivedAuthorizationHeader: fångar headern från det senaste anropet")
   void getLastReceivedAuthorizationHeader_capturesForwardedHeader() throws Exception
   {
      resource.start();

      client.send(HttpRequest.newBuilder(URI.create(AuthorizationHeaderWireMock.getWireMockServer().baseUrl()))
            .header(HttpHeaders.AUTHORIZATION, "Bearer abc123")
            .GET().build(),
            HttpResponse.BodyHandlers.discarding());

      Optional<String> captured = AuthorizationHeaderWireMock.getLastReceivedAuthorizationHeader();

      assertTrue(captured.isPresent());
      assertEquals("Bearer abc123", captured.get());
   }

   @Test
   @DisplayName("getLastReceivedAuthorizationHeader: tom om ingen header skickades")
   void getLastReceivedAuthorizationHeader_emptyWhenHeaderAbsent() throws Exception
   {
      resource.start();

      client.send(HttpRequest.newBuilder(URI.create(AuthorizationHeaderWireMock.getWireMockServer().baseUrl()))
            .GET().build(),
            HttpResponse.BodyHandlers.discarding());

      assertTrue(AuthorizationHeaderWireMock.getLastReceivedAuthorizationHeader().isEmpty());
   }
}
