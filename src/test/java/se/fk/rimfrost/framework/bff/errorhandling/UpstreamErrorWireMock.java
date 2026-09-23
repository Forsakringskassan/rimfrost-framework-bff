package se.fk.rimfrost.framework.bff.errorhandling;

import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Fault;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Reusable WireMock backend for the three failure branches GlobalExceptionMapper handles.
// Subclass and override wiremockMapping() to wire the base URL into a consuming BFF's own
// REST client config property/properties, then register with @QuarkusTestResource(YourSubclass.class).
// Servers are keyed by concrete subclass rather than held in a single shared field, so a BFF
// with several upstream dependencies can register several subclasses side by side.
public abstract class UpstreamErrorWireMock implements QuarkusTestResourceLifecycleManager
{
   private static final Map<Class<?>, WireMockServer> SERVERS = new ConcurrentHashMap<>();

   @Override
   public Map<String, String> start()
   {
      WireMockServer server = new WireMockServer(options().dynamicPort());
      server.start();
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

   public static WireMockServer getWireMockServer(Class<? extends UpstreamErrorWireMock> resourceClass)
   {
      WireMockServer server = SERVERS.get(resourceClass);
      if (server == null)
      {
         throw new IllegalStateException("No WireMock server started for " + resourceClass);
      }
      return server;
   }

   // WebApplicationException passthrough scenario (FKPOC-1065): upstream responds with a status.
   public static void stubUpstreamStatus(Class<? extends UpstreamErrorWireMock> resourceClass, int status)
   {
      getWireMockServer(resourceClass).stubFor(any(anyUrl()).willReturn(aResponse().withStatus(status)));
   }

   // ProcessingException scenario (FKPOC-1066): connection is abruptly reset, maps to HTTP 502.
   public static void stubConnectionReset(Class<? extends UpstreamErrorWireMock> resourceClass)
   {
      getWireMockServer(resourceClass)
            .stubFor(any(anyUrl()).willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));
   }

   // Best-effort approximation of the masked network error scenario (FKPOC-1067): an empty
   // response with no headers at all. This has not been verified against the real fk-logging
   // LoggingContextClientResponseFilter (this package doesn't depend on fk-logging) — it only
   // guarantees that the underlying HTTP call fails with an IOException, which callers can then
   // wrap the same way GlobalExceptionMapperTest does to exercise the masked-error branch.
   public static void stubEmptyResponse(Class<? extends UpstreamErrorWireMock> resourceClass)
   {
      getWireMockServer(resourceClass).stubFor(any(anyUrl()).willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));
   }
}
