package se.fk.rimfrost.framework.bff.ilogg;

import se.fk.rimfrost.framework.bff.ilogg.exception.ILoggException;
import se.fk.rimfrost.informationsaccess.InformationsaccessEvent;

public interface ILogg
{
   /**
    * Publishes the provided event to the kafka topic used by the ILogg.
    *
    * @param event The event to send over kafka.
    *
    */
   /**
    * Publishes the provided event to the kafka topic used by the ILogg.
    *
    * @param event The event to send over kafka.
    * @throws ILoggException Thrown if the event could not be published.
    */
   void publish(InformationsaccessEvent event) throws ILoggException;
}
