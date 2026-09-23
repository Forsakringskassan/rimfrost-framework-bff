package se.fk.rimfrost.framework.bff.health;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.Map;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Exercises HealthCheckWireMock end-to-end against the real UpstreamHealthCheck class, the same
// way a consuming BFF would use it in a @QuarkusTest integration test.
class HealthCheckWireMockSmokeTest
{
   private static class TestResource extends HealthCheckWireMock
   {
      @Override
      protected Map<String, String> wiremockMapping(WireMockServer server)
      {
         return Map.of("test.oul.url", server.baseUrl());
      }
   }

   private static class OtherTestResource extends HealthCheckWireMock
   {
      @Override
      protected Map<String, String> wiremockMapping(WireMockServer server)
      {
         return Map.of("test.arende.url", server.baseUrl());
      }
   }

   private final TestResource resource = new TestResource();
   private final OtherTestResource otherResource = new OtherTestResource();

   @AfterEach
   void stopServer()
   {
      resource.stop();
      otherResource.stop();
   }

   @Test
   @DisplayName("stubUp: hälsokontrollen klassificeras som up")
   void stubUp_healthCheckReportsUp()
   {
      resource.start();
      HealthCheckWireMock.stubUp(TestResource.class);

      UpstreamHealthCheck check = new UpstreamHealthCheck("oul-backend",
            HealthCheckWireMock.getWireMockServer(TestResource.class).baseUrl(), 2000);

      assertEquals(HealthCheckResponse.Status.UP, check.call().getStatus());
   }

   @Test
   @DisplayName("stubDown: hälsokontrollen klassificeras som down")
   void stubDown_healthCheckReportsDown()
   {
      resource.start();
      HealthCheckWireMock.stubDown(TestResource.class, 503);

      UpstreamHealthCheck check = new UpstreamHealthCheck("oul-backend",
            HealthCheckWireMock.getWireMockServer(TestResource.class).baseUrl(), 2000);

      assertEquals(HealthCheckResponse.Status.DOWN, check.call().getStatus());
   }

   @Test
   @DisplayName("stubTimeout: hälsokontrollen klassificeras som down vid överskriden timeout")
   void stubTimeout_healthCheckReportsDown()
   {
      resource.start();
      HealthCheckWireMock.stubTimeout(TestResource.class, 2000);

      UpstreamHealthCheck check = new UpstreamHealthCheck("oul-backend",
            HealthCheckWireMock.getWireMockServer(TestResource.class).baseUrl(), 200);

      assertEquals(HealthCheckResponse.Status.DOWN, check.call().getStatus());
   }

   @Test
   @DisplayName("Två registrerade instanser klassificeras oberoende av varandra")
   void twoRegisteredInstances_areIndependentlyClassified()
   {
      resource.start();
      otherResource.start();
      HealthCheckWireMock.stubUp(TestResource.class);
      HealthCheckWireMock.stubDown(OtherTestResource.class, 503);

      UpstreamHealthCheck oul = new UpstreamHealthCheck("oul-backend",
            HealthCheckWireMock.getWireMockServer(TestResource.class).baseUrl(), 2000);
      UpstreamHealthCheck arende = new UpstreamHealthCheck("arende-backend",
            HealthCheckWireMock.getWireMockServer(OtherTestResource.class).baseUrl(), 2000);

      assertEquals(HealthCheckResponse.Status.UP, oul.call().getStatus());
      assertEquals(HealthCheckResponse.Status.DOWN, arende.call().getStatus());
   }
}
