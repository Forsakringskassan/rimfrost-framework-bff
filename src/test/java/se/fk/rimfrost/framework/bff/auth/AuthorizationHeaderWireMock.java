package se.fk.rimfrost.framework.bff.auth;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import jakarta.ws.rs.core.HttpHeaders;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Reusable WireMock backend for verifying that OutgoingAuthorizationHeaderFilter forwards (or
// correctly omits) the Authorization header on outgoing REST client calls. Subclass and override
// wiremockMapping() to wire the base URL into a consuming BFF's own REST client config property,
// then register with @QuarkusTestResource(YourSubclass.class).
public abstract class AuthorizationHeaderWireMock implements QuarkusTestResourceLifecycleManager
{
   protected static WireMockServer server;

   @Override
   public Map<String, String> start()
   {
      server = new WireMockServer(options().dynamicPort());
      server.start();
      server.stubFor(any(anyUrl()).willReturn(aResponse().withStatus(200)));
      return wiremockMapping(server);
   }

   @Override
   public void stop()
   {
      if (server != null)
      {
         server.stop();
      }
   }

   protected abstract Map<String, String> wiremockMapping(WireMockServer server);

   public static WireMockServer getWireMockServer()
   {
      return server;
   }

   // Returns the Authorization header value from the most recent request WireMock received,
   // or empty if none was received or the header was absent.
   public static Optional<String> getLastReceivedAuthorizationHeader()
   {
      List<LoggedRequest> requests = server.findAll(anyRequestedFor(anyUrl()));
      if (requests.isEmpty())
      {
         return Optional.empty();
      }
      LoggedRequest last = requests.get(requests.size() - 1);
      return last.containsHeader(HttpHeaders.AUTHORIZATION)
            ? Optional.of(last.getHeader(HttpHeaders.AUTHORIZATION))
            : Optional.empty();
   }
}
