package se.fk.rimfrost.framework.bff.health;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;

// Reusable WireMock backend for UpstreamHealthCheck, which probes with a HEAD request to "/".
// Subclass and override wiremockMapping() to wire the base URL into a consuming BFF's config
// property key, then register with @QuarkusTestResource(YourSubclass.class).
public abstract class HealthCheckWireMock implements QuarkusTestResourceLifecycleManager
{
   protected static WireMockServer server;

   @Override
   public Map<String, String> start()
   {
      server = new WireMockServer(options().dynamicPort());
      server.start();
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

   public static void stubUp()
   {
      server.stubFor(head(urlEqualTo("/")).willReturn(aResponse().withStatus(204)));
   }

   public static void stubDown(int status)
   {
      server.stubFor(head(urlEqualTo("/")).willReturn(aResponse().withStatus(status)));
   }

   public static void stubTimeout(int delayMillis)
   {
      server.stubFor(head(urlEqualTo("/")).willReturn(aResponse().withStatus(204).withFixedDelay(delayMillis)));
   }
}
