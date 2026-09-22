package se.fk.rimfrost.framework.bff.logging;

import org.slf4j.MDC;

import java.util.Map;

// try-with-resources replacement for the manual try { MDC.put(...) } finally { MDC.remove(...) }
// pattern repeated across BFF controllers. Never pass the Authorization header's content as a
// value here — the keys set are logged in clear text.
public final class LogContext implements AutoCloseable
{
   private final String[] keys;

   private LogContext(String[] keys)
   {
      this.keys = keys;
   }

   public static LogContext put(String key, String value)
   {
      MDC.put(key, value);
      return new LogContext(new String[] { key });
   }

   public static LogContext put(Map<String, String> values)
   {
      values.forEach(MDC::put);
      return new LogContext(values.keySet().toArray(new String[0]));
   }

   @Override
   public void close()
   {
      for (String key : keys)
      {
         MDC.remove(key);
      }
   }
}
