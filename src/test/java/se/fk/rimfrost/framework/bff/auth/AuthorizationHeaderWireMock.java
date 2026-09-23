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
import java.util.concurrent.ConcurrentHashMap;

// Reusable WireMock backend for verifying that OutgoingAuthorizationHeaderFilter forwards (or
// correctly omits) the Authorization header on outgoing REST client calls. Subclass and override
// wiremockMapping() to wire the base URL into a consuming BFF's own REST client config property,
// then register with @QuarkusTestResource(YourSubclass.class).
// Servers are keyed by concrete subclass rather than held in a single shared field, so a BFF
// with several downstream dependencies can register several subclasses side by side.
public abstract class AuthorizationHeaderWireMock implements QuarkusTestResourceLifecycleManager
{
   private static final Map<Class<?>, WireMockServer> SERVERS = new ConcurrentHashMap<>();

   @Override
   public Map<String, String> start()
   {
      WireMockServer server = new WireMockServer(options().dynamicPort());
      server.start();
      server.stubFor(any(anyUrl()).willReturn(aResponse().withStatus(200)));
      SERVERS.put(getClass(), server);
      return wiremockMapping(server);
   }

   @Override
   public void stop()
   {
      WireMockServer server = SERVERS.remove(getClass());
      if (server != null)
      {
         server.stop();
      }
   }

   protected abstract Map<String, String> wiremockMapping(WireMockServer server);

   public static WireMockServer getWireMockServer(Class<? extends AuthorizationHeaderWireMock> resourceClass)
   {
      WireMockServer server = SERVERS.get(resourceClass);
      if (server == null)
      {
         throw new IllegalStateException("No WireMock server started for " + resourceClass);
      }
      return server;
   }

   // Returns the Authorization header value from the most recent request WireMock received,
   // or empty if none was received or the header was absent.
   public static Optional<String> getLastReceivedAuthorizationHeader(
         Class<? extends AuthorizationHeaderWireMock> resourceClass)
   {
      List<LoggedRequest> requests = getWireMockServer(resourceClass).findAll(anyRequestedFor(anyUrl()));
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
