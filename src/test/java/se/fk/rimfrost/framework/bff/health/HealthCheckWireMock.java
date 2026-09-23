package se.fk.rimfrost.framework.bff.health;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Reusable WireMock backend for UpstreamHealthCheck, which probes with a HEAD request to "/".
// Subclass and override wiremockMapping() to wire the base URL into a consuming BFF's config
// property key, then register with @QuarkusTestResource(YourSubclass.class).
// Servers are keyed by concrete subclass rather than held in a single shared field, so a BFF
// with several upstream dependencies (UpstreamHealthCheck supports registering more than one
// instance) can register several subclasses side by side and control them independently.
public abstract class HealthCheckWireMock implements QuarkusTestResourceLifecycleManager
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

   public static WireMockServer getWireMockServer(Class<? extends HealthCheckWireMock> resourceClass)
   {
      WireMockServer server = SERVERS.get(resourceClass);
      if (server == null)
      {
         throw new IllegalStateException("No WireMock server started for " + resourceClass);
      }
      return server;
   }

   public static void stubUp(Class<? extends HealthCheckWireMock> resourceClass)
   {
      getWireMockServer(resourceClass).stubFor(head(urlEqualTo("/")).willReturn(aResponse().withStatus(204)));
   }

   public static void stubDown(Class<? extends HealthCheckWireMock> resourceClass, int status)
   {
      getWireMockServer(resourceClass).stubFor(head(urlEqualTo("/")).willReturn(aResponse().withStatus(status)));
   }

   public static void stubTimeout(Class<? extends HealthCheckWireMock> resourceClass, int delayMillis)
   {
      getWireMockServer(resourceClass)
            .stubFor(head(urlEqualTo("/")).willReturn(aResponse().withStatus(204).withFixedDelay(delayMillis)));
   }
}
