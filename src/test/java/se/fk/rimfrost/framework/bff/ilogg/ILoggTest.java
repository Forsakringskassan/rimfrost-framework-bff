package se.fk.rimfrost.framework.bff.ilogg;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.fk.rimfrost.framework.bff.ilogg.exception.ILoggException;
import se.fk.rimfrost.informationsaccess.Idtyp;
import se.fk.rimfrost.informationsaccess.InformationsaccessEvent;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@QuarkusTest
public class ILoggTest
{
   @Inject
   @Connector("smallrye-in-memory")
   InMemoryConnector inMemoryConnector;

   @Inject
   ILogg ilogg;

   @BeforeEach
   void reset()
   {
      inMemoryConnector.sink("ilogg-event").clear();
   }

   @Test
   @DisplayName("FBFF-FR-05.1: Publicering med enbart obligatoriska fält satta i event")
   void publish_informationaccessEvent_onlyRequiredFieldsSet() throws ILoggException
   {
      var idtyp = createIdtyp();

      InformationsaccessEvent event = new InformationsaccessEvent();
      event.setAnvandare(idtyp);
      event.setKalla(UUID.randomUUID().toString());
      event.setTidpunkt(OffsetDateTime.now());

      ilogg.publish(event);

      var messages = waitForMessages("ilogg-event");

      assertEquals(1, messages.size());
      assertInstanceOf(InformationsaccessEvent.class, messages.getFirst().getPayload());
      var message = (InformationsaccessEvent) messages.getFirst().getPayload();

      assertEquals(event, message);
   }

   @Test
   @DisplayName("FBFF-FR-05.1: Publicering med alla fält satta i event")
   void publish_informationaccessEvent_allFieldsSet() throws ILoggException
   {
      var idtyp = createIdtyp();

      InformationsaccessEvent event = new InformationsaccessEvent();
      event.setAnvandare(idtyp);
      event.setKalla(UUID.randomUUID().toString());
      event.setTidpunkt(OffsetDateTime.now());
      event.setHandlaggningId(UUID.randomUUID().toString());
      event.setIndivider(new Idtyp[]
      {
            createIdtyp(), createIdtyp()
      });
      event.setRegelId(UUID.randomUUID().toString());
      event.setRegelVersion(UUID.randomUUID().toString());
      event.setUppgiftId(UUID.randomUUID().toString());

      ilogg.publish(event);

      var messages = waitForMessages("ilogg-event");

      assertEquals(1, messages.size());
      assertInstanceOf(InformationsaccessEvent.class, messages.getFirst().getPayload());
      var message = (InformationsaccessEvent) messages.getFirst().getPayload();

      assertEquals(event, message);
   }

   private List<? extends Message<?>> waitForMessages(String channel)
   {
      await()
            .atMost(5, TimeUnit.SECONDS)
            .until(() -> !inMemoryConnector.sink(channel).received().isEmpty());
      return inMemoryConnector.sink(channel).received();
   }

   private Idtyp createIdtyp()
   {
      Idtyp idtyp = new Idtyp();
      idtyp.setTypId(UUID.randomUUID().toString());
      idtyp.setVarde(UUID.randomUUID().toString());
      return idtyp;
   }
}
