package se.fk.rimfrost.framework.bff.errorhandling;

import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Fault;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;

// Reusable WireMock backend for the three failure branches GlobalExceptionMapper handles.
// Subclass and override wiremockMapping() to wire the base URL into a consuming BFF's own
// REST client config property/properties, then register with @QuarkusTestResource(YourSubclass.class).
public abstract class UpstreamErrorWireMock implements QuarkusTestResourceLifecycleManager
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

   // WebApplicationException passthrough scenario (FKPOC-1065): upstream responds with a status.
   public static void stubUpstreamStatus(int status)
   {
      server.stubFor(any(anyUrl()).willReturn(aResponse().withStatus(status)));
   }

   // ProcessingException scenario (FKPOC-1066): connection is abruptly reset, maps to HTTP 502.
   public static void stubConnectionReset()
   {
      server.stubFor(any(anyUrl()).willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));
   }

   // Masked network error scenario (FKPOC-1067): no response headers at all, reproducing the NPE
   // with a suppressed IOException that fk-logging's LoggingContextClientResponseFilter throws.
   public static void stubEmptyResponse()
   {
      server.stubFor(any(anyUrl()).willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));
   }
}
