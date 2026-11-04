package se.fk.rimfrost.framework.bff.ilogg;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import se.fk.rimfrost.framework.bff.ilogg.exception.ILoggException;
import se.fk.rimfrost.informationsaccess.InformationsaccessEvent;

@ApplicationScoped
public class ILoggKafkaClient implements ILogg
{
   @Inject
   @Channel("ilogg-event")
   @OnOverflow(value = OnOverflow.Strategy.BUFFER, bufferSize = 1024)
   Emitter<InformationsaccessEvent> iloggEmitter;

   @Override
   public void publish(InformationsaccessEvent event) throws ILoggException
   {
      try
      {
         iloggEmitter.send(event);
      }
      catch (Exception e)
      {
         throw new ILoggException(e.getMessage(), e);
      }
   }
}
